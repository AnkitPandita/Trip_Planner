package edu.uncc.cci.mobileapps.hw07_group25;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class TripsRecyclerAdapter extends RecyclerView.Adapter<TripsRecyclerAdapter.ViewHolder> {
    private Activity activity;
    private ArrayList<Trip> tripList;
    static final String KEY_TRIP = "KEY_TRIP";

    public TripsRecyclerAdapter(Activity activity, ArrayList<Trip> tripList) {
        this.activity = activity;
        this.tripList = tripList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_trip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trip trip = tripList.get(position);
        holder.trip = trip;
        holder.tvTripTitle.setText(trip.getTitle());
        Picasso.get().load(trip.getCoverImageUrl())
                .placeholder(R.drawable.trip_image_placeholder)
                .error(R.drawable.trip_image_placeholder)
                .into(holder.imgTrip);
    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTripTitle;
        ImageView imgTrip;
        Trip trip;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTripTitle = itemView.findViewById(R.id.tv_trip_layout);
            imgTrip = itemView.findViewById(R.id.iv_trip_layout);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(activity, TripDetailsActivity.class);
                    intent.putExtra(KEY_TRIP, trip);
                    activity.startActivity(intent);
                }
            });
        }
    }
}


