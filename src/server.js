const express = require('express')
const bodyParser = require('body-parser')

// Load configuration
require('./config')
const bot = require('./bot').bot

// Start Express server
const app = express()
app.set('port', process.env.PORT || 5000)
app.use(bodyParser.json())

// Handle / route
app.use('/', (request, response) => {
  // Call bot main function
  bot(request.body, response, (error, success) => {
    if (error) {
      console.log('Error in your bot:', error)
      if (!response.headersSent) { response.sendStatus(400) }
    } else if (success) {
      console.log(success)
      if (!response.headerSent) { response.status(200).json(success) }
    }
  })
})

if (!process.env.REQUEST_TOKEN.length) {
  console.log('ERROR: process.env.REQUEST_TOKEN variable in src/config.js file is empty ! You must fill this field with the request_token of your bot before launching your bot locally')
  process.exit(0)
} else {
  // Run Express server, on right port
  app.listen(app.get('port'), () => {
    console.log('Our bot is running on port', app.get('port'))
  })
}