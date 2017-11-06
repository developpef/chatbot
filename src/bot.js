const recastai = require('recastai').default
const client = new recastai(process.env.REQUEST_TOKEN)
const request = require('request')

export const bot = (body, response, callback) => {
  console.log(body)

  if (body.message) {
	// pour gérer les appels par Slack
    client.connect.handleMessage({ body }, response, replyMessage)
  } else if (body.text) {
    // pour gérer les appels par API REST en direct
	replyRaw(body.text, callback)
  } else {
    callback('Requete vide?!') 
  }
}

function replyRaw (text, callback) {
  const recastaiReq = new recastai.request(process.env.REQUEST_TOKEN, process.env.LANGUAGE)
  const content = text
	console.log("content:"+content)
  recastaiReq.analyseText(content)
  .then(recastaiRes => {
	var varcontent = 'Je ne comprends pas...'
	
	// get the intent detected
    var intent = recastaiRes.intent()
	if(intent) {
		console.log("intent:"+intent.slug+"/"+intent.confidence)
		if (intent.slug === 'c8y_geoloc' && intent.confidence > 0.7) {
			var asset = recastaiRes.get('asset-type').raw
			console.log("asset:"+asset)
			var number = recastaiRes.get('number').raw
			console.log("number:"+number)
			varcontent = 'je vais chercher la '+asset+' '+number
			 
			request(
				{
					url:'https://pefgfi.cumulocity.com/identity/externalIds/stelia_id/'+asset+'_'+number, 
					headers : {"Authorization" : "Basic Y2hhdGJvdDpjaGF0Ym90Y2hhdGJvdA=="}
				},
				(_err, _res, body) => {
					if(_err) {
						varcontent = 'Il y a eu un problème...'
						callback(null, { result: varcontent, intent : intent.slug })
					} else {
						body = JSON.parse(body)
						console.log("C8Y resp:"+body)
						if(body.managedObject) {
							varcontent = 'Juste ici : '+body.managedObject['self']+' !'
						} else {
							varcontent = 'Je n\'ai rien trouvé!'
						}
						var assetId = body.managedObject.id;
						
						//recherche de localisation
						request(
						{
							url:'https://pefgfi.cumulocity.com/event/events?source='+assetId+'&type=c8y_LocationUpdate&dateFrom=2017-09-26', 
							headers : {"Authorization" : "Basic Y2hhdGJvdDpjaGF0Ym90Y2hhdGJvdA=="}
						},
						(_err, _res, body2) => {
							var dataResp = {};
							if(_err) {
								varcontent = 'Il y a eu un problème...'
							} else {
								body2 = JSON.parse(body2)
								console.log("C8Y resp:"+body2)
								if(body2.events) {
									dataResp = body2.events[0].c8y_Position
								}
							}
							callback(null, { result: varcontent, intent : intent.slug, data : dataResp })
						  })
					}
				  })
		} else if (intent.slug === 'c8y_list' && intent.confidence > 0.7) {
			request(
				{
					url:'https://pefgfi.cumulocity.com//inventory/managedObjects', 
					headers : {"Authorization" : "Basic Y2hhdGJvdDpjaGF0Ym90Y2hhdGJvdA=="} // chatbot:chatbotchatbot
				},
				(_err, _res, body) => {
					var dataResp = {list:[]};
					if(_err) {
						varcontent = 'Il y a eu un problème...'
					} else {
						body = JSON.parse(body)
						for(var i=0; i<body.managedObjects.length; i++) {
							if(body.managedObjects[i].c8y_SupportedMeasurements) {
								dataResp.list.push({nom:body.managedObjects[i].name});
							}
						}
					}
					callback(null, { result: varcontent, intent : intent.slug, data : dataResp })
				})
		} else {
			// on fait appel au moteur de conversation, pour conserver l'intelligence par defaut du bot
			const converseReq = new recastai.request(process.env.REQUEST_TOKEN, process.env.LANGUAGE)
			converseReq.converseText(content)
			.then(recastaiConvRes => {
				callback(null, { result: recastaiConvRes.reply(), intent : intent.slug })
			})
		}
	} else {
		callback(null, { result: varcontent, intent : 'null' })
	}
  })
}

const replyMessage = (message, text, res) => {
  const recastaiReq = new recastai.request(process.env.REQUEST_TOKEN, process.env.LANGUAGE)
  const content = message.content
	console.log("content:"+content)
  recastaiReq.analyseText(content)
  .then(recastaiRes => {
	var varcontent = 'Je ne comprends pas...'
	
	// get the intent detected
    var intent = recastaiRes.intent()
	if(intent) {
		console.log("intent:"+intent.slug+"/"+intent.confidence)
		if (intent.slug === 'c8y_geoloc' && intent.confidence > 0.7) {
			var asset = recastaiRes.get('asset-type').raw
			console.log("asset:"+asset)
			var number = recastaiRes.get('number').raw
			console.log("number:"+number)
			varcontent = 'je vais chercher la '+asset+' '+number
			 
			request(
				{
					url:'https://pefgfi.cumulocity.com/identity/externalIds/stelia_id/'+asset+'_'+number, 
					headers : {"Authorization" : "Basic Y2hhdGJvdDpjaGF0Ym90Y2hhdGJvdA=="}
				},
				(_err, _res, body) => {
					if(_err) {
						varcontent = 'Il y a eu un problème...'
					} else {
						body = JSON.parse(body)
						console.log("C8Y resp:"+body)
						if(body.managedObject) {
							varcontent = 'Juste ici : '+body.managedObject['self']+' !'
						} else {
							varcontent = 'Je n\'ai rien trouvé!'
						}
					}
					return message.reply([{ type: 'text', content: varcontent }]).then()
				  })
		} else {
			// on fait appel au moteur de conversation, pour conserver l'intelligence par defaut du bot
			const converseReq = new recastai.request(process.env.REQUEST_TOKEN, process.env.LANGUAGE)
			converseReq.converseText(content)
			.then(recastaiConvRes => {
				return message.reply([{ type: 'text', content: recastaiConvRes.reply()}]).then()
			})
		}
	} else {
		return message.reply([{ type: 'text', content: varcontent }]).then()
	}
  })
}