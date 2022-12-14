package dte.masteriot.mdp.mdprojectsensors;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<dte.masteriot.mdp.mdprojectsensors.MyViewHolder> {

    // https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter

    private static final String TAG = "ListOfItems, MyAdapter";

    private List<Item> items; // reference to the dataset
    private SelectionTracker selectionTracker; // set through method setSelectionTracker()
    Context context;
    Activity activity;

    MainViewModel mainViewModel;

    boolean isEnable = false;
    boolean isSelectedAll = false;
    ArrayList<String> selectList = new ArrayList<>();

    public MyAdapter(Context ctxt, List<Item> listofitems) {
        super();
        context = ctxt;
        items = listofitems;
        this.activity = activity;
    }
    /*
    public MyAdapter(Context ctxt, List<Item> listofitems, Activity activity) {
        super();
        context = ctxt;
        items = listofitems;
        this.activity = activity;
    }
    */

    // ------ Implementation of methods of RecyclerView.Adapter ------ //

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // this method has to actually inflate the item view and return the view holder.
        // it does not give values to the elements of the view holder.
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        //Initialize View Model
        //mainViewModel = ViewModelProviders.of((FragmentActivity)activity).get(MainViewModel.class)



        return new MyViewHolder(context, v, this);
    }

    @Override
    public void onBindViewHolder(dte.masteriot.mdp.mdprojectsensors.MyViewHolder holder, int position) {
        // this method gives values to the elements of the view holder 'holder'
        // (values corresponding to the item in 'position')

        final Item item = items.get(position);
        boolean status = item.getStatus();
        Log.d(TAG, "onBindViewHolder() called for element in position " + position +
                ", Selected? = " + selectionTracker.isSelected(holder.getItemDetails().getSelectionKey()));

        holder.bindValues(item, selectionTracker.isSelected(holder.getItemDetails().getSelectionKey()), status );

        //holder.bindValues(item, holder.getItemDetails().getStatusItem()); //[MGM]
        /*
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {

            public boolean onLongClick(View view){
                if(!isEnable){
                   //When action mode is not enable
                    ActionMode.Callback callback = new ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                            //Initialize menu inflater
                            MenuInflater menuInflater = actionMode.getMenuInflater();
                            // Inflate menu
                            menuInflater.inflate(R.menu.menu,menu);
                            //Return true
                            return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                            //When action mode is prepare
                            // Set isEnable true
                            isEnable = true;
                            ClickItem(holder);
                            setSelectionTracker();

                            return false;
                        }

                        @Override
                        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                            return false;
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode actionMode) {

                        }
                    }
                }
            }
            )


        };

         */
    }
/*
    private void ClickItem(MyViewHolder holder) {
        String s = arrayList.get(holder.getAdapterPosition());

        if(holder.ivCheckBox.getVisibility() == View.GONE){
            //When item not selected
            holder.ivCheckBox.setVisibility(View.VISIBLE);

            holder.itemView.setBackgroundColor(Color.LTGRAY);

            selectList.add(s);

        }
        else{
            holder.ivCheckBox.setVisibility(View.GONE);

            holder.itemView.setBackgroundColor(Color.TRANSPARENT);

            selectList.remove(s);
        }
        mainViewModel.setText(String.valueOf(selectList.size()));
    }
*/
    @Override
    public int getItemCount() {
        return items.size();
    }

    // ------ Other methods useful for the app ------ //

    public Item getItemAtPosition(int pos) {
        return (items.get(pos));
    }

    public Long getKeyAtPosition(int pos) {
        return (items.get(pos).getKey());
    }



    public int getPositionOfKey(Long searchedkey) {
        // Look for the position of the Item with key = searchedkey.
        // The following works because in Item, the method "equals" is overriden to compare only keys:
        int position = items.indexOf(new Item("placeholder", "placeholder","placeholder","placeholder", "placeholder,","placeholder", searchedkey, 0, true));
        Log.d(TAG, "getPositionOfKey() called for key " + searchedkey + ", will return " + position);
        return position;
    }
    public Item getItemWithKey(long key){ //Added for simplification
        return(getItemAtPosition(getPositionOfKey(key)));
    }
    public void setSelectionTracker(SelectionTracker selectionTracker) {
        this.selectionTracker = selectionTracker;
    }
/*
    public boolean getStatusAtPosition(int pos) {
        return (items.get(pos).getStatus());
    } //[MGM]
*/
    public void removeItem(Long removekey){
        for(int i= 0 ; i<items.size(); ++i){
            if(items.get(i).getKey() == removekey){
                items.remove(i);
            }
        }
    }

}
