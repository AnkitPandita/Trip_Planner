package edu.uncc.cci.mobileapps.hw07_group25;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class CreateTripActivity extends AppCompatActivity {
    Button btnCreateTrip;
    Button btnBack;
    EditText etTitle;
    EditText etLatitude;
    EditText etLongitude;
    TextInputLayout tilTitle;
    TextInputLayout tilLatitude;
    TextInputLayout tilLongitude;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    public static final String TRIP_COLLECTION = "trips";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip);
        btnCreateTrip = findViewById(R.id.button_create_trip);
        btnBack = findViewById(R.id.button_back);
        etTitle = findViewById(R.id.et_trip_title);
        etLatitude = findViewById(R.id.et_trip_latitude);
        etLongitude = findViewById(R.id.et_trip_longitude);
        tilTitle = findViewById(R.id.til_trip_title);
        tilLatitude = findViewById(R.id.til_trip_latitude);
        tilLongitude = findViewById(R.id.til_trip_longitude);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        btnCreateTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = etTitle.getText().toString().trim();
                String lat = etLatitude.getText().toString().trim();
                String lon = etLongitude.getText().toString().trim();

                if (validateAll(title, lat, lon)) {
                    String id = UUID.randomUUID() + "";
                    Trip trip = new Trip();
                    trip.setId(id);
                    trip.setTitle(title);
                    trip.setLatitude(Double.parseDouble(lat));
                    trip.setLongitude(Double.parseDouble(lon));
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        trip.setAdmin(currentUser.getEmail());
                        trip.users.add(currentUser.getEmail());
                    }
                    btnCreateTrip.setEnabled(false);
                    db.collection(TRIP_COLLECTION).document(id)
                            .set(trip.toHashMap())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        btnCreateTrip.setEnabled(true);
                                        Toast.makeText(CreateTripActivity.this, "Trip created!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(CreateTripActivity.this, task.getException() + "", Toast.LENGTH_SHORT).show();
                                        btnCreateTrip.setEnabled(true);
                                    }
                                }
                            });
                }
            }
        });
    }

    public boolean validateAll(String title, String lat, String lon) {
        boolean result = true;
        if (TextUtils.isEmpty(title)) {
            tilTitle.setError(tilTitle.getHint() + " cannot be blank!");
            result = false;
        } else {
            tilTitle.setError(null);
        }
        if (TextUtils.isEmpty(lat)) {
            tilLatitude.setError(tilLatitude.getHint() + " cannot be blank!");
            result = false;
        } else {
            tilLatitude.setError(null);
        }
        if (TextUtils.isEmpty(lon)) {
            tilLongitude.setError(tilLongitude.getHint() + " cannot be blank!");
            result = false;
        } else {
            tilLongitude.setError(null);
        }
        return result;
    }
}