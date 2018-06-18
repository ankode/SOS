package com.example.mahe.sos;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CustomLocationListAdapter extends ArrayAdapter {

    private Context mContext;
    private List<FirebaseLocationData> locationList= new ArrayList<>();

    public CustomLocationListAdapter(@NonNull Context context, ArrayList<FirebaseLocationData> list) {
        super(context,0,list);
        mContext=context;
        locationList=list;
    }


    @Override
    public int getCount() {
        return locationList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return locationList.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.custom_layout_list_view,parent,false);

        FirebaseLocationData fld = locationList.get(position);



        TextView name = listItem.findViewById(R.id.pname);
        name.setText(fld.getEmail().split("@")[0]);
        TextView email = listItem.findViewById(R.id.pemail);
        email.setText(fld.getEmail());
        TextView loc = listItem.findViewById(R.id.plocation);
        loc.setText(Double.toString(fld.getLatitude())+"\n"+Double.toString(fld.getLongitude()));
        TextView time = listItem.findViewById(R.id.ptime);
        time.setText(fld.getSos_time());

        return listItem;
    }
}
