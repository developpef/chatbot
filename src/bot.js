const recastai = require('recastai').default;
const client = new recastai(process.env.REQUEST_TOKEN);
const request = require('request');
const axios = require('axios');

export const bot = (body, response, callback) => {
    console.log(body);

// response, the response object from the server in case of a local run
// callback, the object called in case of a bot hosting run

    if (body.message) {
        // pour gérer les appels par BotConnector (Slack...)
        client.connect.handleMessage({body}, response, replyMessage);
    } else if (body.text) {
        // pour gérer les appels par API REST en direct
        replyMessage(null, body.text, callback);
    } else {
        callback('Requete vide?!');
    }
};

function replyMessage(message, textMessage, callback) {
    if (message) {
        console.log("handling BotConnector message");
    } else {
        console.log("handling API message");
    }
    const recastaiReq = new recastai.request(process.env.REQUEST_TOKEN, process.env.LANGUAGE);
    const contentMessage = message ? message.content : textMessage;
    console.log("content:" + contentMessage);
    recastaiReq.analyseText(contentMessage)
            .then(recastaiRes => {
                var varcontent = 'Je ne comprends pas...';

                // get the intent detected
                var intent = recastaiRes.intent();
                if (intent) {
                    console.log("intent:" + intent.slug + "/" + intent.confidence);
                    if (intent.slug === 'c8y_geoloc' && intent.confidence > 0.7) {
                        if (recastaiRes.get('asset-type') && recastaiRes.get('number')) {
                            var asset = recastaiRes.get('asset-type').raw;
                            console.log("asset:" + asset);
                            var number = recastaiRes.get('number').raw;
                            console.log("number:" + number);
                            varcontent = 'je vais chercher la ' + asset + ' ' + number;

                            axios.get('https://pefgfi.cumulocity.com/identity/externalIds/stelia_id/' + asset + '_' + number,
                                    {
                                        headers: {"Authorization": "Basic Y2hhdGJvdDpjaGF0Ym90Y2hhdGJvdA=="} // chatbot:chatbotchatbot
                                    })
                                    .then(response => {
                                        var body = response.data;

                                        if (body.managedObject) {
                                            varcontent = 'Juste ici : ' + body.managedObject['self'] + ' !';

                                            var assetId = body.managedObject.id;

                                            //recherche de localisation
                                            axios.get('https://pefgfi.cumulocity.com/event/events?source=' + assetId + '&type=c8y_LocationUpdate&dateFrom=2017-09-26',
                                                    {
                                                        headers: {"Authorization": "Basic Y2hhdGJvdDpjaGF0Ym90Y2hhdGJvdA=="} // chatbot:chatbotchatbot
                                                    })
                                                    .then(response => {
                                                        var dataResp = {};
                                                        var body2 = response.data;
                                                        if (body2.events && body2.events.length > 0) {
                                                            dataResp = body2.events[0].c8y_Position;
                                                            varcontent = 'Juste ici : https://maps.google.fr/maps?hl=fr&q=' + dataResp.lat + ',' + dataResp.lng;
                                                        } else {
                                                            varcontent = 'Je n\'ai pas trouvé de position...';
                                                        }
                                                        return message ? message.reply([{type: 'text', content: varcontent}]).then() :
                                                                callback(null, {result: varcontent, intent: intent.slug, data: dataResp});
                                                    })
                                                    .catch(error => {
                                                        varcontent = 'Il y a eu un problème...';
                                                        return message ? message.reply([{type: 'text', content: varcontent + error}]) :
                                                                callback(error, null);
                                                    });
                                        } else {
                                            varcontent = 'Je n\'ai rien trouvé!';
                                            return message ? message.reply([{type: 'text', content: varcontent}]).then() :
                                                    callback(null, {result: varcontent, intent: intent.slug});
                                        }
                                    })
                                    .catch(error => {
                                        varcontent = 'Il y a eu un problème...';
                                        return message ? message.reply([{type: 'text', content: varcontent + error}]) :
                                                callback(error, null);
                                    });
                        } else {
                            varcontent = 'Je ne sais pas quoi chercher...';
                            return message ? message.reply([{type: 'text', content: varcontent}]).then() :
                                    callback(null, {result: varcontent, intent: intent.slug});
                        }
                    } else if (intent.slug === 'c8y_list' && intent.confidence > 0.7) {
                        varcontent = 'Voici la liste des objets';
                        axios.get('https://pefgfi.cumulocity.com//inventory/managedObjects',
                                {
                                    headers: {"Authorization": "Basic Y2hhdGJvdDpjaGF0Ym90Y2hhdGJvdA=="} // chatbot:chatbotchatbot
                                })
                                .then(response => {
                                    var dataResp = {list: []};
                                    var body = response.data;
                                    for (var i = 0; i < body.managedObjects.length; i++) {
                                        if (body.managedObjects[i].c8y_SupportedMeasurements) {
                                            dataResp.list.push({nom: body.managedObjects[i].name});
                                        }
                                    }
                                    return message ? message.reply([{type: 'text', content: varcontent}]) :
                                            callback(null, {result: varcontent, intent: intent.slug, data: dataResp});
                                })
                                .catch(err => {
                                    console.error('Something went wrong', err);
                                    return message ? message.reply([{type: 'text', content: 'Something went wrong' + err}]) :
                                            callback(err, null);
                                });
                    } else {
                        // on fait appel au moteur de conversation, pour conserver l'intelligence par defaut du bot
                        const converseReq = new recastai.request(process.env.REQUEST_TOKEN, process.env.LANGUAGE);

                        return converseReq.converseText(contentMessage)
                                .then(function (res2) {
                                    // ...extract the reply...
                                    varcontent = res2.reply();
                                    console.log('converse2 reply', varcontent);

                                    return message ? message.reply([{type: 'text', content: varcontent}]).then() :
                                            callback(null, {result: varcontent, intent: 'null'});
                                })
                                .catch(err => {
                                    console.error('Something went wrong', err);
                                    return message ? message.reply([{type: 'text', content: 'Something went wrong' + err}]) :
                                            callback(err, null);
                                });
                    }
                } else {
                    return message ? message.reply([{type: 'text', content: varcontent}]) :
                            callback(null, {result: varcontent, intent: 'null'});
                }
            })
            .catch(err => {
                console.error('Something went wrong', err);
                return message ? message.reply([{type: 'text', content: 'Something went wrong' + err}]) :
                        callback(err, null);
            });
}
