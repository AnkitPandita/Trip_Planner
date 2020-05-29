package edu.uncc.cci.mobileapps.hw07_group25;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SelectTripAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<Trip> tripList;

    public SelectTripAdapter(Activity activity, ArrayList<Trip> tripList) {
        this.activity = activity;
        this.tripList = tripList;
    }

    @Override
    public int getCount() {
        return tripList.size();
    }

    @Override
    public Object getItem(int i) {
        return tripList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.layout_trip, null);
        Trip trip = tripList.get(i);
        TextView tv_trip_layout = viewGroup.findViewById(R.id.tv_trip_layout);
        ImageView iv_trip_layout = viewGroup.findViewById(R.id.iv_trip_layout);
        tv_trip_layout.setText(trip.getTitle());
        Picasso.get().load(trip.getCoverImageUrl())
                .placeholder(R.drawable.trip_image_placeholder)
                .error(R.drawable.trip_image_placeholder)
                .into(iv_trip_layout);
        return viewGroup;
    }
}
