package com.example.user.mythesisclient;


import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


public class CustomArrayAdapter extends ArrayAdapter {

    private final Context context;
    private final List<TripInfo> values;


    public CustomArrayAdapter(Context context, List<TripInfo> values) {
        super(context,R.layout.row, values);
        this.context = context;
        this.values = values;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowview=inflater.inflate(R.layout.row,parent,false);

        TextView rowdate= rowview.findViewById(R.id.row_date);
        TextView rowfrom= rowview.findViewById(R.id.row_from);
        TextView rowto= rowview.findViewById(R.id.row_to);

        TripInfo tripInfo=values.get(position);

        rowdate.setText(tripInfo.getTdate());
        rowfrom.setText(tripInfo.getFrom_name() + ", " + tripInfo.getFrom_address());
        rowto.setText(tripInfo.getTo_name() + ", " + tripInfo.getTo_address());

        return rowview;
    }
}

