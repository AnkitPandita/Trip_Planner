package edu.uncc.cci.mobileapps.hw07_group25.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import edu.uncc.cci.mobileapps.hw07_group25.CreateTripActivity;
import edu.uncc.cci.mobileapps.hw07_group25.R;
import edu.uncc.cci.mobileapps.hw07_group25.SelectTripAdapter;
import edu.uncc.cci.mobileapps.hw07_group25.Trip;

public class DashboardFragment extends Fragment {
    ArrayList<Trip> selectTripList;
    ArrayList<Trip> tripList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        Button btnCreateTrip = root.findViewById(R.id.btn_create_trip);
        Button btnFindTrips = root.findViewById(R.id.btn_find_trips);
        btnCreateTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CreateTripActivity.class);
                startActivity(intent);
            }
        });
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        tripList = new ArrayList<>();
        if (currentUser != null && currentUser.getEmail() != null) {
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
                            }
                        }
                    });
        }
        btnFindTrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectTripList = new ArrayList<>();
                db.collection(CreateTripActivity.TRIP_COLLECTION)
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                if (queryDocumentSnapshots != null) {
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        if (documentSnapshot.getData() != null) {
                                            Trip trip = new Trip(documentSnapshot.getData());
                                            selectTripList.add(trip);
                                        }
                                    }
                                    selectTripList.removeAll(tripList);
                                }
                            }
                        });
                SelectTripAdapter adapter = new SelectTripAdapter(getActivity(), selectTripList);
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Select the trip to join...");
                alert.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if (currentUser != null && currentUser.getEmail() != null) {
                            db.collection(CreateTripActivity.TRIP_COLLECTION)
                                    .document(selectTripList.get(i).getId())
                                    .update("users", FieldValue.arrayUnion(currentUser.getEmail()))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getActivity(), "Trip joined!", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getActivity(), e + "", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });
                alert.show();
            }
        });
        return root;
    }
}