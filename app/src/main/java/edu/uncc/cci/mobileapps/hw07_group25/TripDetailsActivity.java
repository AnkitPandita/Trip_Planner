package edu.uncc.cci.mobileapps.hw07_group25;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class TripDetailsActivity extends AppCompatActivity {
    Trip trip;
    AppBarLayout appBarLayout;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<User> userList;
    MembersRecyclerAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    TextView tvLocationTrip;
    Button btnAddParticipants;
    Button btnLeaveTrip;
    ArrayList<User> selectUserList;
    Bitmap bitmapAvatar;
    FirebaseStorage firebaseStorage;
    StorageReference imageRepo;
    public static final String KEY_TRIP_CHAT = "KEY_TRIP_CHAT";
    static final int REQUEST_IMAGE_CAPTURE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        appBarLayout = findViewById(R.id.app_bar);
        tvLocationTrip = findViewById(R.id.tv_location_trip);
        btnAddParticipants = findViewById(R.id.btn_add_participants);
        btnLeaveTrip = findViewById(R.id.btn_leave_trip);
        recyclerView = findViewById(R.id.recyclerView_members);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        FloatingActionButton fab = findViewById(R.id.fab);
        appBarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        if (getIntent().getExtras() != null) {
            trip = getIntent().getExtras().getParcelable(TripsRecyclerAdapter.KEY_TRIP);
            if (trip != null) {
                setTitle(trip.getTitle());
                userList = new ArrayList<>();
                boolean isAdmin = false;
                if (trip.getAdmin().equals(currentUser.getEmail())) {
                    isAdmin = true;
                }
                adapter = new MembersRecyclerAdapter(TripDetailsActivity.this, userList, db, currentUser, trip.getId(), isAdmin);
                recyclerView.setAdapter(adapter);
                tvLocationTrip.setText(String.format("%s, %s", trip.getLatitude(), trip.getLongitude()));
                db.collection(CreateTripActivity.TRIP_COLLECTION)
                        .document(trip.getId())
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Toast.makeText(TripDetailsActivity.this, e + "", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (snapshot != null && snapshot.exists() && snapshot.getData() != null) {
                                    trip = new Trip(snapshot.getData());
                                    if (trip.getCoverImageUrl() != null) {
                                        Picasso.get().load(trip.getCoverImageUrl())
                                                .placeholder(R.drawable.avatar_placeholder)
                                                .error(R.drawable.avatar_placeholder)
                                                .into(new Target(){
                                                    @Override
                                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                                        appBarLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
                                                    }

                                                    @Override
                                                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                                                    }

                                                    @Override
                                                    public void onPrepareLoad(final Drawable placeHolderDrawable) {
                                                        //Log.d("TAG", "Prepare Load");
                                                    }
                                                });
                                    }
                                    getUsers();
                                    adapter.notifyDataSetChanged();
                                    if (trip.getAdmin().equals(currentUser.getEmail())) {
                                        btnAddParticipants.setVisibility(View.VISIBLE);
                                    } else {
                                        btnAddParticipants.setVisibility(View.GONE);
                                    }
                                }
                            }
                        });
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(TripDetailsActivity.this, ChatActivity.class);
                        intent.putExtra(KEY_TRIP_CHAT, trip);
                        startActivity(intent);
                    }
                });
            }
        }
        btnAddParticipants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectUserList = new ArrayList<>();
                db.collection(SignUpActivity.USER_COLLECTION)
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Toast.makeText(TripDetailsActivity.this, e + "", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (queryDocumentSnapshots != null) {
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        if (documentSnapshot.getData() != null) {
                                            User user = new User(documentSnapshot.getData());
                                            selectUserList.add(user);
                                        }
                                    }
                                    selectUserList.removeAll(userList);
                                }
                            }
                        });
                SelectUserAdapter adapter = new SelectUserAdapter(TripDetailsActivity.this, selectUserList);
                AlertDialog.Builder alert = new AlertDialog.Builder(TripDetailsActivity.this);
                alert.setTitle("Select the user to add...");
                alert.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        db.collection(CreateTripActivity.TRIP_COLLECTION)
                                .document(trip.getId())
                                .update("users", FieldValue.arrayUnion(selectUserList.get(i).getId()))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(TripDetailsActivity.this, "Member added!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(TripDetailsActivity.this, e + "", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
                alert.show();
            }
        });

        btnLeaveTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(TripDetailsActivity.this);
                alert.setTitle("Leave Trip Group");
                alert.setMessage("Do you want to leave the group?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        db.collection(CreateTripActivity.TRIP_COLLECTION)
                                .document(trip.getId())
                                .update("users", FieldValue.arrayRemove(currentUser.getEmail()))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        btnLeaveTrip.setVisibility(View.GONE);
                                        getUsers();
                                        Toast.makeText(TripDetailsActivity.this, "You left!", Toast.LENGTH_SHORT).show();
                                        if (trip.getAdmin().equals(currentUser.getEmail())) {
                                            if (trip.getUsers().size() != 0) {
                                                db.collection(CreateTripActivity.TRIP_COLLECTION)
                                                        .document(trip.getId())
                                                        .update("admin", trip.getUsers().get(0));
                                            } else {
                                                db.collection(CreateTripActivity.TRIP_COLLECTION)
                                                        .document(trip.getId())
                                                        .delete();
                                            }
                                        }
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(TripDetailsActivity.this, e + "", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alert.show();
            }
        });
    }

    private void getUsers() {
        if (trip.getUsers().size() != 0) {
            db.collection(SignUpActivity.USER_COLLECTION)
                    .whereIn("id", trip.getUsers())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Toast.makeText(TripDetailsActivity.this, e + "", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (queryDocumentSnapshots != null) {
                                userList.clear();
                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    if (documentSnapshot.getData() != null) {
                                        User user = new User(documentSnapshot.getData());
                                        userList.add(user);
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
        } else {
            userList.clear();
        }
    }

    // Upload Camera Photo to Cloud Storage
    private void uploadImage(Bitmap photoBitmap) {
        // Converting the Bitmap into a ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        photoBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] data = outputStream.toByteArray();

        String path = "tripCoverImages/" + trip.getId() + ".png";
        imageRepo = firebaseStorage.getReference().child(path);
        UploadTask uploadTask = imageRepo.putBytes(data);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imageRepo.getDownloadUrl();
            }
        });

        urlTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    String imageURL = task.getResult() + "";
                    trip.setCoverImageUrl(imageURL);
                    db.collection(CreateTripActivity.TRIP_COLLECTION).document(trip.getId())
                            .set(trip.toHashMap())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //progressDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        Toast.makeText(TripDetailsActivity.this, "Trip Cover updated!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(TripDetailsActivity.this, task.getException() + "", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    // TAKE PHOTO USING CAMERA
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                bitmapAvatar = (Bitmap) extras.get("data");
                if (bitmapAvatar != null) {
                    uploadImage(bitmapAvatar);
                }
            }
        }
    }
}
