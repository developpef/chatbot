package com.developpef.voicebot;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class MainActivity extends AppCompatActivity {

    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    /**
     * chat
     */
    private ListView messagesContainer;
    private ChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        // chat
        initControls();
    }

    private void initControls() {
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        adapter = new ChatAdapter(this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);
    }

    public void displayMessage(ChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        /*Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }*/
        onActivityResult(REQ_CODE_SPEECH_INPUT, RESULT_OK, null);
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            switch (requestCode) {
                case REQ_CODE_SPEECH_INPUT: {
                    //if (resultCode == RESULT_OK && null != data) {
                    if (resultCode == RESULT_OK ) {

                        /*ArrayList<String> result = data
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        String firstExtra = result.get(0);*/
                        String firstExtra = "où est la caisse 2";
                        startConversation(firstExtra);
                    }
                    break;
                }

            }
        } catch(Exception e) {
            Toast.makeText(getApplicationContext(),
                    "Exception:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void startConversation(String userInput) throws JSONException, UnsupportedEncodingException {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(122);//dummy
        chatMessage.setMessage(userInput);
        chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatMessage.setMe(true);
        displayMessage(chatMessage);

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Authorization", "Token ed665077a9496e5fafc6a49f065bf225");
        JSONObject jsonParams = new JSONObject();
        jsonParams.put("text", userInput);
        StringEntity entity = new StringEntity(jsonParams.toString());

        client.post(this, "https://run.recast.ai/developpef-chatbot", entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String intentResp = "", messageTxt = "";
                JSONObject data = null;
                try {
                    intentResp = response.getString("intent");
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            "jsonexce:" + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
                try {
                    messageTxt = response.getString("result");
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            "jsonexce:" + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }

                String posStr = "";
                try {
                    data = response.getJSONObject("data");
                    posStr = data.getDouble("lat")+","+data.getDouble("lng");
                } catch (JSONException e) { }

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(122);//dummy
                if(intentResp!=null && intentResp.equals("c8y_geoloc")) {
                    chatMessage.setMessage(" <u>Voir une carte</u>");
                    chatMessage.setMap(true);
                } else {
                    chatMessage.setMessage(messageTxt);
                }
                chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                chatMessage.setMe(false);
                chatMessage.setData(data);
                displayMessage(chatMessage);

                if(chatMessage.isMap()) {
                    // Creates an Intent that will load a map
                    Uri gmmIntentUri = Uri.parse("geo:" + posStr + "?q=" + posStr);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            }
        });
    }
}
