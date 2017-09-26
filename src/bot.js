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
					headers : {"Authorization" : "Basic cGF1bC1lbW1hbnVlbC5mYWlkaGVyYmVAZ2ZpLmZyOiFhbjEyUEVG"}
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
					callback(null, { result: varcontent, intent : intent.slug })
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
					headers : {"Authorization" : "Basic cGF1bC1lbW1hbnVlbC5mYWlkaGVyYmVAZ2ZpLmZyOiFhbjEyUEVG"}
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