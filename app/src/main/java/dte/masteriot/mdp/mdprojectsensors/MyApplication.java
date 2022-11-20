package dte.masteriot.mdp.mdprojectsensors;

import android.app.Application;

import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {
    Integer OneArriving = 0;
    Integer TwoArriving = 0;
    Integer ThreeArriving = 0;
    Integer FourArriving = 0;
    Integer OneInside = 10;
    Integer TwoInside = 8;
    Integer ThreeInside = 8;
    Integer FourInside = 4;

    String OneError, TwoError, ThreeError, FourError;

    private static final List<Item> listofitems = new ArrayList<Item>();
    private List<BarEntry> Entries1 = new ArrayList<>();
    private List<BarEntry> Entries2 = new ArrayList<>();
    private List<BarEntry> Entries3 = new ArrayList<>();
    private List<BarEntry> Entries4 = new ArrayList<>();


    public static List<Item> getListofitems() {
        return listofitems;
    }


    public MyApplication() {
        initListOfItems();
        initListofEntries();
    }

    private void initListOfItems() {

        listofitems.add(new Item("Warehouse 1", OneArriving, OneInside, OneError, (long) 0, R.drawable.number_1, true, Entries1));
        listofitems.add(new Item("Warehouse 2", ThreeArriving, ThreeInside, ThreeError, (long) 1, R.drawable.number_2, true, Entries2));
        listofitems.add(new Item("Warehouse 3", TwoArriving, TwoInside, TwoError, (long) 2, R.drawable.number_3, true, Entries3));
        listofitems.add(new Item("Warehouse 4", FourArriving, FourInside, FourError, (long) 3, R.drawable.number_4, true, Entries4));


    }
    private void initListofEntries(){
        for (int i = 1; i <16; i ++){
            Entries1.add(new BarEntry(i,10));
            Entries2.add(new BarEntry(i,8));
            Entries3.add(new BarEntry(i,8));
            Entries4.add(new BarEntry(i,4));
        }
    }

}




