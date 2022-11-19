package dte.masteriot.mdp.mdprojectsensors;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
///new push
public class ThirdActivity extends AppCompatActivity {
    public static final String LOGSLOADWEBCONTENT = "LOGSLOADWEBCONTENT"; // to easily filter logs
    private String logTag; // to clearly identify logs
    //private static final String URL_PARKS = "https://short.upm.es/3qnno";
    private static final String URL_PARKS = "https://api.open-meteo.com/v1/forecast?latitude=51.5002&longitude=-0.1262&hourly=temperature_2m,relativehumidity_2m,cloudcover&timezone=Europe%2FLondon&start_date=2022-11-01&end_date=2022-11-01";


    private static final String CONTENT_TYPE_JSON = "application/json";

    private String time_now,temp_now,hum_now,cloud_now;
    private Button btPNG;
    private Button btJSON;
    private Button btKML;
    private TextView text;
    private ImageView imgView;
    ExecutorService es;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        // Build the logTag with the Thread and Class names:
        logTag = LOGSLOADWEBCONTENT + ", Thread = " + Thread.currentThread().getName() + ", Class = " +
                this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".") + 1);

        // Get references to UI elements:

        //btJSON = findViewById(R.id.loadJSON);

        imgView = findViewById(R.id.imageView);

        // Create an executor for the background tasks:
        es = Executors.newSingleThreadExecutor();
    }

    // Define the handler that will receive the messages from the background thread:
    Handler handler = new Handler(Looper.getMainLooper()) {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void handleMessage(@NonNull Message msg) {
            // message received from background thread: load complete (or failure)
            String string_result;
            Bitmap bitmap;

            super.handleMessage(msg);
            Log.d(logTag, "message received from background thread");
            if((string_result = msg.getData().getString("text")) != null) {
                try {
                    JSONObject json_obj = new JSONObject(string_result);
                    JSONObject hourly = json_obj.getJSONObject("hourly");
                    JSONArray time = hourly.getJSONArray("time");
                    JSONArray temperature = hourly.getJSONArray("temperature_2m");
                    JSONArray humidity = hourly.getJSONArray("relativehumidity_2m");
                    JSONArray cloudcover = hourly.getJSONArray("cloudcover");
                    List<String> timeArray=new ArrayList<String>();
                    List<String> tempArray=new ArrayList<String>();
                    List<String> humArray=new ArrayList<String>();
                    List<String> cloudArray=new ArrayList<String>();
                    for (int r = 0; r < time.length(); r++) {
                        timeArray.add(time.getString(r));
                        tempArray.add(temperature.getString(r));
                        humArray.add(humidity.getString(r));
                        cloudArray.add(cloudcover.getString(r));
                    }
                    LocalDateTime now = java.time.LocalDateTime.now();
                    Integer hour = now.toLocalTime().getHour();
                    String tim,temp,hum, cloud;
                    tim   = timeArray.get(hour);
                    temp  = tempArray.get(hour);
                    hum   = humArray.get(hour);
                    cloud = cloudArray.get(hour);
                    time_now  = timeArray.get(hour);
                    temp_now  = tempArray.get(hour);
                    hum_now   = humArray.get(hour);
                    cloud_now = cloudArray.get(hour);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                text.setText(string_result);
            }
            toggle_buttons(true); // re-enable the buttons
        }
    };


    public void readJSON(View view) {
        toggle_buttons(false); // disable the buttons until the load is complete
        text.setText("Loading " + URL_PARKS + "..."); // Inform the user by means of the TextView

        // Execute the loading task in background:
        LoadURLContents loadURLContents = new LoadURLContents(handler, CONTENT_TYPE_JSON, URL_PARKS);
        es.execute(loadURLContents);
    }


    private void toggle_buttons(boolean state) {
        // enable or disable buttons (depending on state)
        btJSON.setEnabled(state);
    }

}