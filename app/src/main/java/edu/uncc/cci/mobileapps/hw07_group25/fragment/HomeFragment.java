package edu.uncc.cci.mobileapps.hw07_group25.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import edu.uncc.cci.mobileapps.hw07_group25.CreateTripActivity;
import edu.uncc.cci.mobileapps.hw07_group25.TripsRecyclerAdapter;
import edu.uncc.cci.mobileapps.hw07_group25.R;
import edu.uncc.cci.mobileapps.hw07_group25.Trip;

public class HomeFragment extends Fragment {
    RecyclerView.LayoutManager layoutManager;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    ArrayList<Trip> tripList;
    TripsRecyclerAdapter adapter;
    ProgressBar progressBar;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        progressBar = root.findViewById(R.id.progressBar_home);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        tripList = new ArrayList<>();
        adapter = new TripsRecyclerAdapter(getActivity(), tripList);
        recyclerView.setAdapter(adapter);
        if (currentUser != null && currentUser.getEmail() != null) {
            progressBar.setVisibility(View.VISIBLE);
            db.collection(CreateTripActivity.TRIP_COLLECTION)
                    .whereArrayContains("users", currentUser.getEmail())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (queryDocumentSnapshots != null) {
                                tripList.clear();
                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    if (documentSnapshot.getData() != null) {
                                        Trip trip = new Trip(documentSnapshot.getData());
                                        tripList.add(trip);
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }
        return root;
    }
}