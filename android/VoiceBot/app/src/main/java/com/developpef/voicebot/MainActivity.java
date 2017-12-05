package com.developpef.voicebot;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;

import org.json.JSONArray;
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
    private Button btnInput;
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

        btnInput = (Button) findViewById(R.id.inputBtn);
        btnInput.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptTextInput();
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


    private void promptTextInput() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Saisie");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        //input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    startConversation(input.getText().toString());
                } catch(Exception e) {
                    Toast.makeText(getApplicationContext(),
                            "Exception:" + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
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
            promptTextInput();
        }
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
                    if (resultCode == RESULT_OK && null != data) {
                    //if (resultCode == RESULT_OK ) {

                        ArrayList<String> result = data
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        String firstExtra = result.get(0);
                        //String firstExtra = "où est la caisse 2";
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


                double lat = 0.0, lng = 0.0;

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(122);//dummy
                if(intentResp!=null && intentResp.equals("c8y_geoloc")) {
                    try {
                        data = response.getJSONObject("data");
                        lat = data.getDouble("lat");
                        lng = data.getDouble("lng");
                    } catch (JSONException e) {
                    }
                    chatMessage.setMessage(" <u>Voici une carte</u>");
                    chatMessage.setMap(true);
                } else if(intentResp!=null && intentResp.equals("c8y_list")) {
                    StringBuilder sb = new StringBuilder("Vos objets connus :<br>");
                    try {
                        data = response.getJSONObject("data");
                        JSONArray array = data.getJSONArray("list");
                        for(int i=0; i<array.length(); i++) {
                            sb.append(array.optJSONObject(i).getString("nom")).append("<br>");
                        }
                    } catch (JSONException e) {
                    }
                    chatMessage.setMessage(sb.toString());
                } else {
                    chatMessage.setMessage(messageTxt);
                }
                chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                chatMessage.setMe(false);
                chatMessage.setData(data);
                displayMessage(chatMessage);

                if(chatMessage.isMap()) {
                    startActivity(MapActivityIntentFactory.startGoogleMaps(lat,lng));
                    //startActivity(MapActivityIntentFactory.startMapBox(MainActivity.this, lat, lng));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(getApplicationContext(),
                        "erreur http "+statusCode+" "+throwable.getMessage(),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(),
                        "erreur2 http "+statusCode+" "+throwable.getMessage(),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Toast.makeText(getApplicationContext(),
                        "erreur3 http "+statusCode+" "+throwable.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
