package edu.uncc.cci.mobileapps.hw07_group25.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

import edu.uncc.cci.mobileapps.hw07_group25.LoginActivity;
import edu.uncc.cci.mobileapps.hw07_group25.R;
import edu.uncc.cci.mobileapps.hw07_group25.SignUpActivity;
import edu.uncc.cci.mobileapps.hw07_group25.User;

public class ProfileFragment extends Fragment {
    ImageView imgAvatar;
    TextView tvName;
    TextView tvGender;
    TextView tvEmail;
    ImageButton imageButtonEdit;
    ConstraintLayout constraintLayout;
    LinearLayout linearLayout;
    RadioGroup radioGroupGender;
    RadioButton rbMale;
    RadioButton rbFemale;
    EditText etFname;
    EditText etLname;
    TextInputLayout tilFname;
    TextInputLayout tilLname;
    Button btnSave;
    Button btnCancel;
    Button btnLogout;
    private FirebaseAuth mAuth;
    String email;
    private FirebaseFirestore db;
    User user;
    Bitmap bitmapAvatar;
    FirebaseStorage firebaseStorage;
    StorageReference imageRepo;
    static final int REQUEST_IMAGE_CAPTURE = 2;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        radioGroupGender = root.findViewById(R.id.radioGroup_gender_profile);
        rbMale = root.findViewById(R.id.rb_male_profile);
        rbFemale = root.findViewById(R.id.rb_female_profile);
        etFname = root.findViewById(R.id.et_fname_profile);
        etLname = root.findViewById(R.id.et_lname_profile);
        tilFname = root.findViewById(R.id.til_fname_profile);
        tilLname = root.findViewById(R.id.til_lname_profile);
        btnSave = root.findViewById(R.id.button_save);
        btnCancel = root.findViewById(R.id.button_cancel);
        btnLogout = root.findViewById(R.id.button_logout);
        imgAvatar = root.findViewById(R.id.iv_avatar_profile);
        tvName = root.findViewById(R.id.tv_name);
        tvGender = root.findViewById(R.id.tv_gender);
        tvEmail = root.findViewById(R.id.tv_email);
        imageButtonEdit = root.findViewById(R.id.imagebutton_edit_profile);
        constraintLayout = root.findViewById(R.id.constraint_layout);
        linearLayout = root.findViewById(R.id.linear_layout);
        linearLayout.setVisibility(View.GONE);

        firebaseStorage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            email = currentUser.getEmail();
            DocumentReference docRef = db.collection(SignUpActivity.USER_COLLECTION).document(email);
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot != null && documentSnapshot.exists() && documentSnapshot.getData() != null) {
                        user = new User(documentSnapshot.getData());
                        tvName.setText(String.format("%s %s", user.getFname(), user.getLname()));
                        tvGender.setText(user.getGender());
                        tvEmail.setText(user.getId());
                        if (user.getAvatarUrl() != null) {
                            Picasso.get().load(user.getAvatarUrl())
                                    .placeholder(R.drawable.avatar_placeholder)
                                    .error(R.drawable.avatar_placeholder)
                                    .into(imgAvatar);
                        }
                    }
                }
            });
        }

        imageButtonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                constraintLayout.setVisibility(View.GONE);
                etFname.setText(user.getFname());
                etLname.setText(user.getLname());
                if (user.getGender().equals("Male")) {
                    rbMale.setChecked(true);
                } else if (user.getGender().equals("Female")) {
                    rbFemale.setChecked(true);
                }
                linearLayout.setVisibility(View.VISIBLE);
            }
        });

        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fName = etFname.getText().toString().trim();
                String lName = etLname.getText().toString().trim();
                if (validateAll(fName, lName)) {
                    user.setFname(fName);
                    user.setLname(lName);
                    if (rbMale.isChecked()) {
                        user.setGender("Male");
                    } else if (rbFemale.isChecked()) {
                        user.setGender("Female");
                    }
                    db.collection(SignUpActivity.USER_COLLECTION).document(user.getId())
                            .set(user.toHashMap())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //progressDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        // Toast.makeText(AddEditMovieActivity.this, getResources().getString(R.string.movie_added), Toast.LENGTH_SHORT).show();
                                        linearLayout.setVisibility(View.GONE);
                                        constraintLayout.setVisibility(View.VISIBLE);
                                    } else {
                                        Toast.makeText(getActivity(), task.getException() + "", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayout.setVisibility(View.GONE);
                constraintLayout.setVisibility(View.VISIBLE);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Logout");
                alert.setMessage("Do you want to logout?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();
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

        return root;
    }

    public boolean validateAll(String fName, String lName) {
        boolean result = true;
        if (TextUtils.isEmpty(fName)) {
            tilFname.setError(tilFname.getHint() + " cannot be blank!");
            result = false;
        } else if (SignUpActivity.validateName(fName)) {
            tilFname.setError("Provide valid First Name!");
            result = false;
        } else {
            tilFname.setError(null);
        }
        if (TextUtils.isEmpty(lName)) {
            tilLname.setError(tilLname.getHint() + " cannot be blank!");
            result = false;
        } else if (SignUpActivity.validateName(lName)) {
            tilLname.setError("Provide valid Last Name!");
            result = false;
        } else {
            tilLname.setError(null);
        }
        return result;
    }

    // Upload Camera Photo to Cloud Storage
    private void uploadImage(Bitmap photoBitmap) {
        // Converting the Bitmap into a ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        photoBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] data = outputStream.toByteArray();

        String path = "avatars/" + user.getId() + ".png";
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
                    user.setAvatarUrl(imageURL);
                    db.collection(SignUpActivity.USER_COLLECTION).document(user.getId())
                            .set(user.toHashMap())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getActivity(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getActivity(), task.getException() + "", Toast.LENGTH_SHORT).show();
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
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
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