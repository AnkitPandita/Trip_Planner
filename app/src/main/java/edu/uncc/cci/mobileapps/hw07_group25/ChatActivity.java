package edu.uncc.cci.mobileapps.hw07_group25;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {
    EditText etWriteMessage;
    FloatingActionButton fab;
    ListView listView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    FirebaseStorage firebaseStorage;
    FirebaseUser currentUser;
    Trip trip;
    ArrayList<Message> messageList;
    ChatAdapter adapter;
    StorageReference imageRepo;
    Bitmap bitmapAvatar;
    Message message;
    String id;
    ImageButton ivCamera;
    ProgressDialog progressDialog;
    static final int REQUEST_IMAGE_CAPTURE = 3;
    public static final String MESSAGES_COLLECTION = "messages";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        etWriteMessage = findViewById(R.id.et_write_message);
        fab = findViewById(R.id.fab_send);
        ivCamera = findViewById(R.id.iv_camera);
        listView = findViewById(R.id.listview_chat);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        currentUser = mAuth.getCurrentUser();
        messageList = new ArrayList<>();
        adapter = new ChatAdapter(ChatActivity.this, messageList, currentUser);
        listView.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending image...");
        progressDialog.setCancelable(false);

        if (getIntent().getExtras() != null) {
            trip = getIntent().getExtras().getParcelable(TripDetailsActivity.KEY_TRIP_CHAT);
            if (trip != null) {
                setTitle(trip.getTitle());
                ivCamera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dispatchTakePictureIntent();
                    }
                });
                db.collection(CreateTripActivity.TRIP_COLLECTION)
                        .document(trip.getId())
                        .collection(MESSAGES_COLLECTION)
                        .orderBy("timestamp")
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Toast.makeText(ChatActivity.this, e + "", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (queryDocumentSnapshots != null) {
                                    messageList.clear();
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        if (documentSnapshot.getData() != null) {
                                            Message message = new Message(documentSnapshot.getData());
                                            messageList.add(message);
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        });

                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String messageText = etWriteMessage.getText().toString();
                        if (!TextUtils.isEmpty(messageText)) {
                            etWriteMessage.setEnabled(false);
                            String id = UUID.randomUUID() + "";
                            Message message = new Message();
                            message.setId(id);
                            message.setSenderEmail(currentUser.getEmail());
                            message.setBody(messageText);
                            message.setPhoto(false);
                            db.collection(CreateTripActivity.TRIP_COLLECTION)
                                    .document(trip.getId())
                                    .collection(MESSAGES_COLLECTION)
                                    .document(id)
                                    .set(message.toHashMap())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                etWriteMessage.setText(null);
                                                etWriteMessage.setEnabled(true);
                                            } else {
                                                Toast.makeText(ChatActivity.this, task.getException() + "", Toast.LENGTH_SHORT).show();
                                                etWriteMessage.setEnabled(true);
                                            }
                                        }
                                    });
                        }
                    }
                });

                listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                        final Message message = messageList.get(i);
                        if (message.getSenderEmail().equals(currentUser.getEmail())) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(ChatActivity.this);
                            alert.setTitle("Delete message");
                            alert.setMessage("Do you want to this message?");
                            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    db.collection(CreateTripActivity.TRIP_COLLECTION)
                                            .document(trip.getId())
                                            .collection(MESSAGES_COLLECTION)
                                            .document(message.getId())
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(ChatActivity.this, "Message deleted!", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(ChatActivity.this, e + "", Toast.LENGTH_SHORT).show();
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
                        return false;
                    }
                });
            }
        }
    }

    // Upload Camera Photo to Cloud Storage
    private void uploadImage(Bitmap photoBitmap) {
        // Converting the Bitmap into a ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        photoBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] data = outputStream.toByteArray();
        id = UUID.randomUUID() + "";
        message = new Message();
        message.setId(id);
        message.setSenderEmail(currentUser.getEmail());
        message.setPhoto(true);
        String path = "messages/" + id + ".png";
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
                    message.setBody(imageURL);
                    db.collection(CreateTripActivity.TRIP_COLLECTION)
                            .document(trip.getId())
                            .collection(MESSAGES_COLLECTION)
                            .document(id)
                            .set(message.toHashMap())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(ChatActivity.this, task.getException() + "", Toast.LENGTH_SHORT).show();
                                    }
                                    progressDialog.dismiss();
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
                    progressDialog.show();
                    uploadImage(bitmapAvatar);
                }
            }
        }
    }
}
