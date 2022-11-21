// Parts of the code of this example app have ben taken from:
// https://enoent.fr/posts/recyclerview-basics/
// https://developer.android.com/guide/topics/ui/layout/recyclerview

package dte.masteriot.mdp.mdprojectsensors;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.snackbar.Snackbar;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    public static final String LOGSLOADWEBCONTENT = "LOGSLOADWEBCONTENT";
    private static final String TAG = "ListOfItems, MainActivity";

    MyApplication myApplication = (MyApplication) this.getApplication();
    // App-specific dataset:
    List<Item> listofitems =  myApplication.getListofitems();

    private RecyclerView recyclerView;
    private MyAdapter recyclerViewAdapter;
    private SelectionTracker tracker;
    private MyOnItemActivatedListener onItemActivatedListener;
    private Object next;

    String OneArrivingS, OneLeaving, OneError, OneArrived;
    String TwoArrivingS, TwoLeaving, TwoError, TwoArrived;
    String ThreeArrivingS, ThreeLeaving, ThreeError, ThreeArrived;
    String FourArrivingS, FourLeaving, FourError, FourArrived;
    Integer OneInside, OneArriving;
    Integer TwoInside, TwoArriving;
    Integer ThreeInside, ThreeArriving;
    Integer FourInside, FourArriving;



    ExecutorService es;//[MGM] Background
   // Handler handler;
    MQTTClient Warehouse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the list of items (the dataset):
        //initListOfItems();

        // Prepare the RecyclerView:
        recyclerView = findViewById(R.id.recyclerView);
        recyclerViewAdapter = new MyAdapter(this, listofitems);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Choose the layout manager to be set.
        // some options for the layout manager:  GridLayoutManager, LinearLayoutManager, StaggeredGridLayoutManager
        // initially, a linear layout is chosen:
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Selection tracker (to allow for selection of items):
        onItemActivatedListener = new MyOnItemActivatedListener(this, recyclerViewAdapter);
        tracker = new SelectionTracker.Builder<>(
                "my-selection-id",
                recyclerView,
                new MyItemKeyProvider(ItemKeyProvider.SCOPE_MAPPED, recyclerViewAdapter),
//                new StableIdKeyProvider(recyclerView), // This caused the app to crash on long clicks
                new MyItemDetailsLookup(recyclerView),
                StorageStrategy.createLongStorage())
                .withOnItemActivatedListener(onItemActivatedListener)
                .build();
        recyclerViewAdapter.setSelectionTracker(tracker);

        if (savedInstanceState != null) {
            // Restore state related to selections previously made
            tracker.onRestoreInstanceState(savedInstanceState);
        }

        // Creation of the MQTT Client and subscription to the topics
        Warehouse = new MQTTClient(this);
        Random random = new Random();
        Warehouse.clientId = Warehouse.clientId + random.nextInt(100000);
        Warehouse.mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), Warehouse.serverUri, Warehouse.clientId);
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);

        //Last Will message
        mqttConnectOptions.setWill(Warehouse.publishTopic, Warehouse.LWillmessage.getBytes(),0,false);

        try {
            Warehouse.mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    Warehouse.mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    Warehouse.subscriptionTopic = "project/Warehouse_1/#";
                    Warehouse.subscribeToTopic();
                    Warehouse.subscriptionTopic = "project/Warehouse_2/#";
                    Warehouse.subscribeToTopic();
                    Warehouse.subscriptionTopic = "project/Warehouse_3/#";
                    Warehouse.subscribeToTopic();
                    Warehouse.subscriptionTopic = "project/Warehouse_4/#";
                    Warehouse.subscribeToTopic();
                    Snackbar.make(findViewById(R.id.bNewMovement), "Client connected and subscribed", 2000).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }




        es = Executors.newSingleThreadExecutor();
        MQTTSub task = new MQTTSub(handler, Warehouse);
        es.execute(task);

    }

    @Override
    protected void onResume() {
        super.onResume();
        OneInside = recyclerViewAdapter.getItemWithKey(0).getInside();
        TwoInside = recyclerViewAdapter.getItemWithKey(1).getInside();
        ThreeInside = recyclerViewAdapter.getItemWithKey(2).getInside();
        FourInside = recyclerViewAdapter.getItemWithKey(3).getInside();
        OneArriving = recyclerViewAdapter.getItemWithKey(0).getArriving();
        TwoArriving = recyclerViewAdapter.getItemWithKey(1).getArriving();
        ThreeArriving = recyclerViewAdapter.getItemWithKey(2).getArriving();
        FourArriving = recyclerViewAdapter.getItemWithKey(3).getArriving();
    }

    Handler handler = new Handler(Looper.getMainLooper()) { //Handler for the message received from the background. Depending on the key (topic), the message will be assigned to a specif String variable
        @Override                                   //Then, the attributes (Inside, Leaving and Arriving) of each item are upErrord.
        public void handleMessage(Message inputMessage) {
            this.obtainMessage();
//WAREHOUSE 1
            OneArrivingS = inputMessage.getData().getString("project/Warehouse_1/Arriving");
            if(OneArrivingS != null){
                int Add = Integer.parseInt(OneArrivingS);
                OneArriving = recyclerViewAdapter.getItemWithKey(0).getArriving() + Add;// Add to Arriving
            }
            OneLeaving = inputMessage.getData().getString("project/Warehouse_1/Leaving");
            if(OneLeaving != null){
                int Subtract = Integer.parseInt(OneLeaving);
                OneInside = recyclerViewAdapter.getItemWithKey(0).getInside() - Subtract;
                recyclerViewAdapter.getItemWithKey(0).setListofEntries(UpdateEntries(recyclerViewAdapter.getItemWithKey(0).getListofEntries(), OneInside));
            }

            OneError = inputMessage.getData().getString("project/Warehouse_1/Error");
            OneArrived = inputMessage.getData().getString("project/Warehouse_1/Arrived");

            if(OneArrived != null){ //When a box Arrives, we need to update Arriving and Inside fields
                int Total = recyclerViewAdapter.getItemWithKey(0).getInside();
                int Add = Integer.parseInt(OneArrived);
                Total = Add + Total;
                OneInside = Total;
                int TotalArr = recyclerViewAdapter.getItemWithKey(0).getArriving();
                OneArriving =  TotalArr - Add;
                recyclerViewAdapter.getItemWithKey(0).setListofEntries(UpdateEntries(recyclerViewAdapter.getItemWithKey(0).getListofEntries(), OneInside));
            }
            recyclerViewAdapter.getItemWithKey(0).setParameters(OneArriving,OneInside,OneError);

//WAREHOUSE 2
            TwoArrivingS = inputMessage.getData().getString("project/Warehouse_2/Arriving");
            if(TwoArrivingS != null){
                int Add = Integer.parseInt(TwoArrivingS);
                TwoArriving = recyclerViewAdapter.getItemWithKey(1).getArriving() + Add;// Add to Arriving
            }
            TwoLeaving = inputMessage.getData().getString("project/Warehouse_2/Leaving");
            if(TwoLeaving != null){
                int Subtract = Integer.parseInt(TwoLeaving);
                TwoInside = recyclerViewAdapter.getItemWithKey(1).getInside() - Subtract;
                recyclerViewAdapter.getItemWithKey(1).setListofEntries(UpdateEntries(recyclerViewAdapter.getItemWithKey(1).getListofEntries(), TwoInside));
            }
            TwoError = inputMessage.getData().getString("project/Warehouse_2/Error");
            TwoArrived = inputMessage.getData().getString("project/Warehouse_2/Arrived");

            if(TwoArrived != null){ //When a box Arrives, we need to update Arriving and Inside fields
                int Total = recyclerViewAdapter.getItemWithKey(1).getInside();
                int Add = Integer.parseInt(TwoArrived);
                Total = Add + Total;
                TwoInside = Total;
                int TotalArr = recyclerViewAdapter.getItemWithKey(1).getArriving();
                TwoArriving =  TotalArr - Add;
                recyclerViewAdapter.getItemWithKey(1).setListofEntries(UpdateEntries(recyclerViewAdapter.getItemWithKey(1).getListofEntries(), TwoInside));
            }

            recyclerViewAdapter.getItemWithKey(1).setParameters(TwoArriving, TwoInside, TwoError); //UPDATE



// WAREHOUSE 3
            ThreeArrivingS = inputMessage.getData().getString("project/Warehouse_3/Arriving");
            if(ThreeArrivingS != null){
                int Add = Integer.parseInt(ThreeArrivingS);
                ThreeArriving = recyclerViewAdapter.getItemWithKey(2).getArriving() + Add;// Add to Arriving
            }
            ThreeLeaving = inputMessage.getData().getString("project/Warehouse_3/Leaving");
            if(ThreeLeaving != null){
                int Subtract = Integer.parseInt(ThreeLeaving);
                ThreeInside = recyclerViewAdapter.getItemWithKey(2).getInside() - Subtract;
                recyclerViewAdapter.getItemWithKey(2).setListofEntries(UpdateEntries(recyclerViewAdapter.getItemWithKey(2).getListofEntries(), ThreeInside));


            }
            ThreeError = inputMessage.getData().getString("project/Warehouse_3/Error");
            ThreeArrived = inputMessage.getData().getString("project/Warehouse_3/Arrived");

            if(ThreeArrived != null){ //When a box Arrives, we need to update Arriving and Inside fields
                int Total = recyclerViewAdapter.getItemWithKey(2).getInside();
                int Add = Integer.parseInt(ThreeArrived);
                Total = Add + Total;
                ThreeInside = Total;
                int TotalArr = recyclerViewAdapter.getItemWithKey(2).getArriving();
                ThreeArriving =  TotalArr - Add;
                recyclerViewAdapter.getItemWithKey(2).setListofEntries(UpdateEntries(recyclerViewAdapter.getItemWithKey(2).getListofEntries(), ThreeInside));
            }

            recyclerViewAdapter.getItemWithKey(2).setParameters(ThreeArriving, ThreeInside, ThreeError); //UPDATE

            // WAREHOUSE 4

            FourArrivingS = inputMessage.getData().getString("project/Warehouse_4/Arriving");
            if(FourArrivingS != null){
                int Add = Integer.parseInt(FourArrivingS);
                FourArriving= recyclerViewAdapter.getItemWithKey(3).getArriving() + Add; // Add to Arriving
            }
            FourLeaving = inputMessage.getData().getString("project/Warehouse_4/Leaving");
            if(FourLeaving != null){
                int Subtract = Integer.parseInt(FourLeaving);
                FourInside = recyclerViewAdapter.getItemWithKey(3).getInside() - Subtract;
                recyclerViewAdapter.getItemWithKey(3).setListofEntries(UpdateEntries(recyclerViewAdapter.getItemWithKey(3).getListofEntries(), FourInside));

            }
            FourError = inputMessage.getData().getString("project/Warehouse_4/Error");
            FourArrived = inputMessage.getData().getString("project/Warehouse_4/Arrived");

            if(FourArrived != null){ //When a box Arrives, we need to update Arriving and Inside fields
                int Total = recyclerViewAdapter.getItemWithKey(3).getInside();
                int Add = Integer.parseInt(FourArrived);
                Total = Add + Total;
                FourInside = Total;
                int TotalArr = recyclerViewAdapter.getItemWithKey(3).getArriving();
                FourArriving =  TotalArr - Add;
                recyclerViewAdapter.getItemWithKey(3).setListofEntries(UpdateEntries(recyclerViewAdapter.getItemWithKey(3).getListofEntries(), FourInside));
            }

            recyclerViewAdapter.getItemWithKey(3).setParameters(FourArriving, FourInside, FourError); //UPDATE


            recyclerViewAdapter.notifyDataSetChanged();


        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_delete:
                confirmation();
                return true;
            case R.id.sort_AZ:
                Collections.sort(listofitems, GreenhouseAZ);
                Toast.makeText(MainActivity.this, "Sort A to Z", Toast.LENGTH_SHORT).show();
                recyclerViewAdapter.notifyDataSetChanged();
                return true;
            case R.id.sort_za:
                Collections.sort(listofitems, GreenhouseZA);
                Toast.makeText(MainActivity.this, "Sort Z to A", Toast.LENGTH_SHORT).show();
                recyclerViewAdapter.notifyDataSetChanged();
                return true;
            case R.id.sort_status:
                Collections.sort(listofitems, GreenhouseStatus);
                Toast.makeText(MainActivity.this, "Sort by Status", Toast.LENGTH_SHORT).show();
                recyclerViewAdapter.notifyDataSetChanged();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }




    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        tracker.onSaveInstanceState(outState); // Save state about selections.
    }


    

    // ------ Buttons' on-click listeners ------ //

    public void listLayout(View view) {
        // Button to see in a linear fashion has been clicked:
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void gridLayout(View view) {
        // Button to see in a grid fashion has been clicked:
        //recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        Intent i = new Intent(MainActivity.this, ThirdActivity.class);
        startActivity(i);
    }

    public void NewMovement(View view) {
        Intent i = new Intent(this, SecondActivity.class);
        i.putExtra("Inside1", recyclerViewAdapter.getItemWithKey(0).getInside());
        i.putExtra("Inside2", recyclerViewAdapter.getItemWithKey(1).getInside());
        i.putExtra("Inside3", recyclerViewAdapter.getItemWithKey(2).getInside());
        i.putExtra("Inside4", recyclerViewAdapter.getItemWithKey(3).getInside());
        startActivity(i);
    }

    private void confirmation() {
        Iterator iteratorSelectedItemsKeys = tracker.getSelection().iterator();

        if (iteratorSelectedItemsKeys.hasNext()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want delete these items?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            Eliminate();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            //recyclerViewAdapter.notifyItemRemoved(Integer.parseInt(iteratorSelectedItemsKeys.next().toString()));

        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("No items selected");
        }
    }

    public void Eliminate() {
        // Button "see current selection" has been clicked:

        Iterator iteratorSelectedItemsKeys = tracker.getSelection().iterator();

        while (iteratorSelectedItemsKeys.hasNext()) {
            recyclerViewAdapter.removeItem(Long.parseLong(iteratorSelectedItemsKeys.next().toString()));
            //recyclerViewAdapter.notifyItemRemoved(Integer.parseInt(iteratorSelectedItemsKeys.next().toString()));
        }
        recyclerViewAdapter.notifyDataSetChanged();
    }







      public List<BarEntry>  UpdateEntries(List<BarEntry> entries,int newBarEntry ){
        List<BarEntry> newList = new ArrayList<>();
        newList.add(new BarEntry(1,(int)newBarEntry));
        for(int i = 2; i < 16; i++){
            newList.add(new BarEntry(i, (int)entries.get(i-2).getY()));
        }

        return newList;
     }


    public static Comparator<Item> GreenhouseAZ = new Comparator<Item>() {
        @Override
        public int compare(Item t1, Item t2) {

            return t1.getTitle().compareTo(t2.getTitle());
        }
    };
    public static Comparator<Item> GreenhouseZA = new Comparator<Item>() {
        @Override
        public int compare(Item t1, Item t2) {

            return t2.getTitle().compareTo(t1.getTitle());
        }
    };
    public static Comparator<Item> GreenhouseStatus = new Comparator<Item>() {
        @Override
        public int compare(Item t1, Item t2) {

            return Boolean.compare(t1.getStatus(),t2.getStatus());
        }
    };




}