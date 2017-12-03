const express = require('express');
const bodyParser = require('body-parser');
const url = require('url');

// Load configuration
require('./config');
const bot = require('./bot').bot;

// Start Express server
const app = express();
app.set('port', process.env.PORT || 5000);
app.use(bodyParser.json());

// Handle / route
app.use('/', (request, response) => {
    // Call bot main function
    bot(request.body, response, (error, success) => {
        if (error) {
            console.log('Error in your bot:', error);
            if (!response.headersSent) {
                response.status(500).json(error);
            }
        } else if (success) {
            console.log('success: ' + success);
            if (!response.headerSent) {
                response.status(200).json(success);
            }
        }
    });
});

// Chatfuel proxy
app.use('/chatfuel', (req, res) => {
    const query = url.parse(req.url, true).query;
    const userId = query['chatfuel user id'];
    const userMessage = query['user_message'];

    // todo

    res.json({
        messages: [chatfuelFormat({type: 'text', content: 'test message'})]
    });
});

function chatfuelFormat(message) {
    // Source : { type: 'text', content: 'XXX' }
    // Destination { text: 'XXX' }
    if (message.type === 'text') {
        return {text: message.content};
    }

    // Source: { type: 'picture', content: 'URL' }
    // Destination: { attachment: { type: 'image', payload: { url: 'URL' } } }
    if (message.type === 'picture') {
        return {
            attachment: {
                type: 'image',
                payload: {url: message.content}
            }
        };
    }

    console.error('Unsupported message format: ', message.type);
    return {text: 'An error occured'};
}

// Recast will send a post request to /errors to notify important errors
// described in a json body
app.post('/errors', (req, res) => {
    console.error("error posted : " + req.body);
    res.sendStatus(200);
});

if (!process.env.REQUEST_TOKEN.length) {
    console.log('ERROR: process.env.REQUEST_TOKEN variable in src/config.js file is empty ! You must fill this field with the request_token of your bot before launching your bot locally');
    process.exit(0);
} else {
    // Run Express server, on right port
    app.listen(app.get('port'), () => {
        console.log('Our bot is running on port', app.get('port'));
    });
}