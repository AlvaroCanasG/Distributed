package dte.masteriot.mdp.mdprojectsensors;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
///new push


public class ThirdActivity extends AppCompatActivity {
    String Name;
    Integer Inside,Arriving;
    List<BarEntry> entries;
    TextView Storage, Incoming;
    @SuppressLint("DefaultLocale")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);


        List<Item> listofitems = ((MyApplication) this.getApplication()).getListofitems();
        // Get measures and time from subscriber
        BarChart chart = findViewById(R.id.chart);
        Storage = findViewById(R.id.storage);
        Incoming = findViewById(R.id.arriving);

        Intent inputIntent = getIntent();
        Inside = inputIntent.getIntExtra("Inside",8);
        Storage.setText(String.format("Storage level: %.2f %%", getStorage(Inside)));
        Arriving = inputIntent.getIntExtra("Arriving",0);
        if(Arriving != null){
            Incoming.setText("Arriving containers: " + Arriving);
        }


        Name = inputIntent.getStringExtra("Name");
        switch(Name){
            case "Warehouse 1":
                entries = listofitems.get(0).getListofEntries();
                break;
            case  "Warehouse 2":
                entries = listofitems.get(1).getListofEntries();
                break;
            case "Warehouse 3":
                entries = listofitems.get(2).getListofEntries();
                break;
            case "Warehouse 4":
                entries = listofitems.get(3).getListofEntries();
                break;
        }
        BarDataSet dataset = new BarDataSet(entries,"Containers");
        BarData bardata = new BarData(dataset);
        bardata.setBarWidth(0.8f);
        chart.setData(bardata);
        chart.setFitBars(true);
        chart.getDescription().setEnabled(false);

        XAxis xaxis = chart.getXAxis();
        xaxis.setDrawGridLines(false);
        xaxis.setDrawLabels(false);
        xaxis.setDrawAxisLine(false);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMaximum(12);
        leftAxis.setAxisMinimum(0);
        leftAxis.setValueFormatter(new ValueFormatter()
        {
            @Override
            public String getFormattedValue(float v)
            {
                return ((int) v)+"";
            }
        });
        YAxis rightaxis = chart.getAxisRight();
        rightaxis.setDrawGridLines(false);
        rightaxis.setDrawLabels(false);
        chart.invalidate();


    }


    public float getStorage(int containers){
       return ((containers/(float)12.0)*100);
    }


}