const recastai = require('recastai').default
const client = new recastai(process.env.REQUEST_TOKEN)
const request = require('request')

export const bot = (body, response, callback) => {
  console.log(body)

  if (body.message) {
    client.connect.handleMessage({ body }, response, replyMessage)
  } else if (body.text) {
    callback(null, { result: 'Bot echo '+body.text+' :)' })
  } else {
    callback('No text provided')
  }
}

const replyMessage = (message, text, res) => {
  const recastaiReq = new recastai.request(process.env.REQUEST_TOKEN, process.env.LANGUAGE)
  const content = message.content
	console.log("content:"+content)
  recastaiReq.analyseText(content)
  .then(recastaiRes => {
	var varcontent = 'réponse!'
	
	// get the intent detected
    var intent = recastaiRes.intent()
	if(intent) {
		console.log("intent:"+intent.slug+"/"+intent.confidence)
		if (intent.slug === 'c8y_geoloc' && intent.confidence > 0.7) {
			var asset = recastaiRes.get('asset-type').slug
			console.log("asset:"+asset)
			var number = recastaiRes.get('number').slug
			console.log("number:"+number)
			varcontent = 'je vais chercher la '+asset+' '+number
			 
			request(
				{
					url:'https://pefgfi.cumulocity.com/identity/externalIds/stelia_id/'+asset+'_'+number, 
					headers : {"Authorization" : "Basic cGF1bC1lbW1hbnVlbC5mYWlkaGVyYmVAZ2ZpLmZyOiFhbjEyUEVG"}
				},
				(_err, _res, body) => {
					body = JSON.parse(body)
					varcontent = 'Juste ici : '+body.self+' !'
					return message.reply([{ type: 'text', content: varcontent }]).then()
				  })
		}
	} else {
		return message.reply([{ type: 'text', content: varcontent }]).then()
	}
  })
}