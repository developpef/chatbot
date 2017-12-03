const recastai = require('recastai').default
const client = new recastai(process.env.REQUEST_TOKEN)
const request = require('request')

export const bot = (body, response, callback) => {
    console.log(body)

    if (body.message) {
        // pour gérer les appels par BotConnector (Slack...)
        client.connect.handleMessage({body}, response, replyMessage)
    } else if (body.text) {
        // pour gérer les appels par API REST en direct
        replyMessage(null, body.text, response)
    } else {
        callback('Requete vide?!')
    }
}
function replyMessage(message, textMessage, response) {
    const recastaiReq = new recastai.request(process.env.REQUEST_TOKEN, process.env.LANGUAGE);
    const contentMessage = message ? message.content : textMessage;
    console.log("content2:" + contentMessage);
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

                            request(
                                    {
                                        url: 'https://pefgfi.cumulocity.com/identity/externalIds/stelia_id/' + asset + '_' + number,
                                        headers: {"Authorization": "Basic Y2hhdGJvdDpjaGF0Ym90Y2hhdGJvdA=="}
                                    },
                                    (_err, _res, body) => {
                                if (_err) {
                                    varcontent = 'Il y a eu un problème...';
                                    return message ? message.reply([{type: 'text', content: varcontent + _err}]) :
                                            response.status(500).json(_err);
                                } else {
                                    body = JSON.parse(body);

                                    if (body.managedObject) {
                                        varcontent = 'Juste ici : ' + body.managedObject['self'] + ' !';

                                        var assetId = body.managedObject.id;

                                        //recherche de localisation
                                        request(
                                                {
                                                    url: 'https://pefgfi.cumulocity.com/event/events?source=' + assetId + '&type=c8y_LocationUpdate&dateFrom=2017-09-26',
                                                    headers: {"Authorization": "Basic Y2hhdGJvdDpjaGF0Ym90Y2hhdGJvdA=="}
                                                },
                                                (_err, _res, body2) => {
                                            var dataResp = {};
                                            if (_err) {
                                                varcontent = 'Il y a eu un problème...'
                                            } else {
                                                body2 = JSON.parse(body2);
                                                if (body2.events && body2.events.length > 0) {
                                                    dataResp = body2.events[0].c8y_Position;
                                                    varcontent = 'Juste ici : https://maps.google.fr/maps?hl=fr&q=' + dataResp.lat + ',' + dataResp.lng;
                                                } else {
                                                    varcontent = 'Je n\'ai pas trouvé de position...';
                                                }
                                            }
                                            return message ? message.reply([{type: 'text', content: varcontent}]).then() :
                                                    response.status(200).json({result: varcontent, intent: intent.slug, data: dataResp});
                                        })
                                    } else {
                                        varcontent = 'Je n\'ai rien trouvé!';
                                        return message ? message.reply([{type: 'text', content: varcontent}]).then() :
                                                response.status(200).json({result: varcontent, intent: intent.slug});
                                    }
                                }
                            })
                        } else {
                            varcontent = 'Je ne sais pas quoi chercher...';
                            return message ? message.reply([{type: 'text', content: varcontent}]).then() :
                                    response.status(200).json({result: varcontent, intent: intent.slug, data: dataResp});
                        }
                    } else {
                        // on fait appel au moteur de conversation, pour conserver l'intelligence par defaut du bot
                        const converseReq = new recastai.request(process.env.REQUEST_TOKEN, process.env.LANGUAGE)

                        return converseReq.converseText(contentMessage, {conversationToken: message.senderId})
                                .then(function (res2) {
                                    // ...extract the reply...
                                    var reply = res2.reply()
                                    console.log('converse2 reply', reply);

                                    return message ? message.reply([{type: 'text', content: reply}]).then() :
                                            response.status(200).json({result: varcontent, intent: 'null'});
                                })
                                .catch(err => {
                                    console.error('Something went wrong', err);
                                    return message ? message.reply([{type: 'text', content: 'Something went wrong' + err}]) :
                                            response.status(500).json(err);
                                })
                    }
                } else {
                    return message ? message.reply([{type: 'text', content: varcontent}]) :
                            response.status(200).json({result: varcontent, intent: 'null'});
                }
            })
            .catch(err => {
                console.error('Something went wrong', err);
                return message ? message.reply([{type: 'text', content: 'Something went wrong' + err}]) :
                        response.status(500).json(err);
            })
}
