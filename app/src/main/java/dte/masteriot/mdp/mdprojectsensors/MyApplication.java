package dte.masteriot.mdp.mdprojectsensors;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {
    Integer OneArriving, OneInside;
    Integer TwoArriving, TwoInside;
    Integer ThreeArriving, ThreeInside;
    Integer FourArriving, FourInside;
    String OneError, TwoError, ThreeError, FourError;

    private static final List<Item> listofitems = new ArrayList<Item>();

    public static List<Item> getListofitems() {
        return listofitems;
    }


    public MyApplication() {
        initListOfItems();


    }

    private void initListOfItems() {

        listofitems.add(new Item("Warehouse 1", OneArriving, OneInside, OneError, (long) 0, R.drawable.tomato, true));
        listofitems.add(new Item("Warehouse 2", ThreeArriving, ThreeInside, ThreeError, (long) 1, R.drawable.peper, true));
        listofitems.add(new Item("Warehouse 3", TwoArriving, TwoInside, TwoError, (long) 2, R.drawable.eggplant, true));
        listofitems.add(new Item("Warehouse 4", FourArriving, FourInside, FourError, (long) 3, R.drawable.green_bean, true));


    }

}




