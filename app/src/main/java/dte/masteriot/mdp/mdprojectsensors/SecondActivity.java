package dte.masteriot.mdp.mdprojectsensors;

import static java.lang.Math.sqrt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class SecondActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Calendar calendar;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    SimpleDateFormat dateFormat1 = new SimpleDateFormat("H");
    SimpleDateFormat dateFormat2 = new SimpleDateFormat("M");
    SimpleDateFormat dateFormat3 = new SimpleDateFormat("s");
    
    public static EditText nb_containers;
    public static String from_wh;
    public static String to_wh;
    Button bPublish;
    public Integer OneInside, TwoInside, ThreeInside, FourInside;

    boolean WarehousesSelected;
    boolean from_ok = false;
    boolean to_ok= false;
    boolean from_sent=false;


    String firstRoot;
    MQTTClient myMQTT;
    Spinner spinner_from, spinner_to;
    long lastUpdate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        myMQTT = new MQTTClient(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        nb_containers=(EditText) findViewById(R.id.nb_cont); //Get the number of containers


        spinner_from = (Spinner) findViewById(R.id.spinner_from);
        spinner_to = (Spinner) findViewById(R.id.spinner_to);

        List<Item> listofitems = ((MyApplication) this.getApplication()).getListofitems();
        ArrayList<String> Title = new ArrayList<>();
        for (int i = 0; i<listofitems.size(); i++){

            Title.add(listofitems.get(i).getTitle());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item , Title);

        //Get the updated list of Warehouses
        Intent myintent = getIntent();
        OneInside = myintent.getIntExtra("Inside1",0);
        TwoInside = myintent.getIntExtra("Inside2",0);
        ThreeInside = myintent.getIntExtra("Inside3",0);
        FourInside = myintent.getIntExtra("Inside4",0);


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_from.setAdapter(adapter);
        spinner_to.setAdapter(adapter);
        spinner_from.setOnItemSelectedListener(this);
        spinner_to.setOnItemSelectedListener(this);

        bPublish = findViewById(R.id.bPublish);
        bPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!myMQTT.mqttAndroidClient.isConnected()){
                    myMQTT.SendNotification("No connection");
                }
                else if (!WarehousesSelected) {
                    myMQTT.SendNotification("Please select a Warehouse");
                }
                else {
                    checkFrom(); //Checks that there's enough boxes to send
                    checkTo(); //Checks that there's enough space to receive
                    if(!from_ok){

                        myMQTT.SendNotification("Not enough containers in source Warehouse");
                    }
                    else if(!to_ok){

                        myMQTT.SendNotification("Not enough space in target Warehouse");
                    }
                    else if (from_ok && to_ok)
                    {
                        from_wh = spinner_from.getSelectedItem().toString();
                        myMQTT.publishTopic = from_wh.replaceAll(" ", "_") + "/Leaving";
                        try {
                            myMQTT.publishMessage(nb_containers.getText().toString());
                            from_sent=true;
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        to_wh = spinner_to.getSelectedItem().toString();
                        myMQTT.publishTopic = to_wh.replaceAll(" ", "_") + "/Arriving";
                        try {
                            myMQTT.publishMessage(nb_containers.getText().toString());
                            myMQTT.SendNotification("Message published");
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        Random random = new Random();
        myMQTT.clientId = myMQTT.clientId + random.nextInt(100000);
        myMQTT.mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), myMQTT.serverUri, myMQTT.clientId);
        myMQTT.mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    myMQTT.SendNotification("Reconnected to : " + serverURI);
                } else {
                    myMQTT.SendNotification("Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                myMQTT.SendNotification("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                myMQTT.SendNotification("Incoming message: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);

        //Last Will message
        mqttConnectOptions.setWill(myMQTT.publishTopic,myMQTT.LWillmessage.getBytes(),0,false);

        //SendNotification("Connecting to " + serverUri);
        try {
            myMQTT.mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    myMQTT.mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    myMQTT.SendNotification("Failed to connect to: " + myMQTT.serverUri);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        WarehousesSelected = false;



    }

    private void checkFrom()
    {
        String from =  (String)spinner_from.getSelectedItem();
        int inside = 0;
        switch(from){
            case "Warehouse 1":
                inside = OneInside;
                break;
            case "Warehouse 2":
                inside = TwoInside;
                break;
            case "Warehouse 3":
                inside = ThreeInside;
                break;
            case "Warehouse 4":
                inside = FourInside;
                break;

        }
        int number = Integer.parseInt(nb_containers.getText().toString());
        if(inside>=number) from_ok = true;
        else from_ok = false;

    }

    private void checkTo()
    {
        String to = (String) spinner_to.getSelectedItem();
        int inside = 0;
        switch(to){
            case "Warehouse 1":
                inside = OneInside;
                break;
            case "Warehouse 2":
                inside = TwoInside;
                break;
            case "Warehouse 3":
                inside = ThreeInside;
                break;
            case "Warehouse 4":
                inside = FourInside;
                break;

        }
        int number = Integer.parseInt(nb_containers.getText().toString());
        if(inside+number<=12) to_ok = true;
        else to_ok = false;

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(spinner_from.getSelectedItem()!= spinner_to.getSelectedItem()) WarehousesSelected = true;

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
            WarehousesSelected = false;
    }
}
