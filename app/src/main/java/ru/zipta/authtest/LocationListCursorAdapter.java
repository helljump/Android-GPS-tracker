package ru.zipta.authtest;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class LocationListCursorAdapter extends CursorRecyclerViewAdapter<LocationListCursorAdapter.ViewHolder>{

    public LocationListCursorAdapter(Context context,Cursor cursor){
        super(context,cursor);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView lng_tv;
        private TextView alt_tv;
        private TextView time_tv;
        private TextView lat_tv;

        public ViewHolder(View view) {
            super(view);
            lat_tv = (TextView)view.findViewById(R.id.lat_tv);
            lng_tv = (TextView)view.findViewById(R.id.lng_tv);
            alt_tv = (TextView)view.findViewById(R.id.alt_tv);
            time_tv = (TextView)view.findViewById(R.id.time_tv);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.location_view, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        LocationListItem myListItem = LocationListItem.fromCursor(cursor);
        viewHolder.lat_tv.setText("" + myListItem.getLat());
        viewHolder.lng_tv.setText("" + myListItem.getLng());
        viewHolder.alt_tv.setText("" + myListItem.getAlt());
        viewHolder.time_tv.setText("" + myListItem.getTime());
    }
}