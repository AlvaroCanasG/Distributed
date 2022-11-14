// Parts of the code of this example app have ben taken from:
// https://enoent.fr/posts/recyclerview-basics/
// https://developer.android.com/guide/topics/ui/layout/recyclerview

package dte.masteriot.mdp.mdprojectsensors;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class MainActivity extends AppCompatActivity {

    public static final String LOGSLOADWEBCONTENT = "LOGSLOADWEBCONTENT";
    private static final String TAG = "ListOfItems, MainActivity";

    // App-specific dataset:
    private static final List<Item> listofitems = new ArrayList<>();
    private static boolean listofitemsinitialized = false;

    private RecyclerView recyclerView;
    private MyAdapter recyclerViewAdapter;
    private SelectionTracker tracker;
    private MyOnItemActivatedListener onItemActivatedListener;
    private Object next;

    ExecutorService es; //[MGM] Background

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the list of items (the dataset):
        initListOfItems();

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
    }

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

    // ------ Initialization of the dataset ------ //

    private void initListOfItems () {

        listofitems.add(new Item("Tomato", "https://www.tierraburritos.com/", "March - April - May" , (long) 0 , R.drawable.tomato, true ));
        listofitems.add(new Item("Peper", "https://ochentagrados.com/", "March - April - May" , (long) 1 , R.drawable.peper , false ));
        listofitems.add(new Item("Eggplant", "https://grupolamusa.com/restaurante-musa-malasana/", "July - August" , (long) 2 , R.drawable.eggplant , true ));
        listofitems.add(new Item("Green bean", "https://lamejorhamburguesa.com/", "May - Jun" , (long) 3 , R.drawable.green_bean , false ));
        listofitems.add(new Item("Zucchini", "https://www.sublimeworldrestaurant.com//", "May" , (long) 4 , R.drawable.zucchini, true ));
        listofitems.add(new Item("Cucumber", "https://www.loscervecistas.es/locales-cervecistas/el-2-de-fortuny/", "April" , (long) 5 , R.drawable.cucumber , true ));
        listofitems.add(new Item("Melon", "https://www.loscervecistas.es/locales-cervecistas/el-2-de-fortuny/", "March - April - May" , (long) 6 , R.drawable.melon , true ));
        listofitems.add(new Item("Watermelon", "https://www.loscervecistas.es/locales-cervecistas/el-2-de-fortuny/", "February - March - April" , (long) 7 , R.drawable.watermelon , true ));

        listofitemsinitialized = true;




        // Populate the list of items if not done before:
        /*final int ITEM_COUNT = 50;
        if (listofitemsinitialized == false) {
            for (int i = 0; i < ITEM_COUNT; ++i) {
                listofitems.add(new Item("Item " + i, "This is the item number " + i, (long) i));
            }
            listofitemsinitialized = true;
        }*/

    }

    // ------ Buttons' on-click listeners ------ //
/*
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
*/

    public void seeCurrentSelection(View view) {
        // Button "see current selection" has been clicked:

        Iterator iteratorSelectedItemsKeys = tracker.getSelection().iterator();
        // This iterator allows to navigate through the keys of the currently selected items.
        // Complete info on getSelection():
        // https://developer.android.com/reference/androidx/recyclerview/selection/SelectionTracker#getSelection()
        // Complete info on class Selection (getSelection() returns an object of this class):
        // https://developer.android.com/reference/androidx/recyclerview/selection/Selection

        String text = "";
        while (iteratorSelectedItemsKeys.hasNext()) {
            text += iteratorSelectedItemsKeys.next().toString();
            if (iteratorSelectedItemsKeys.hasNext()) {
                text += ", ";
            }
        }
        text = "Keys of currently selected items = \n" + text;
        Intent i = new Intent(this, SecondActivity.class);
        i.putExtra("text", text);
        startActivity(i);
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

    public void confirmation(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want delete ?")
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
    /*
    public void buttonAsyncListener(View view) {
        //Log.d(logTag, "Scheduling new task in background thread");
        es.execute(new LengthyTask());
    }

    Test1 Git
    Test2
     */

}