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
	console.log("ai:"+recastaiRes)
	const varcontent = 'réponse!'
	return message.reply([{ type: 'text', content: varcontent }]).then()
  })
}