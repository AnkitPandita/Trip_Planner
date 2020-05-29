package edu.uncc.cci.mobileapps.hw07_group25;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    RadioGroup radioGroupGender;
    RadioButton rbMale;
    RadioButton rbFemale;
    EditText etFname;
    EditText etLname;
    EditText etEmail;
    EditText etPassword;
    EditText etConfirmPassword;
    TextInputLayout tilFname;
    TextInputLayout tilLname;
    TextInputLayout tilEmail;
    TextInputLayout tilPassword;
    TextInputLayout tilConfirmPassword;
    Button btnSignUp;
    Button btnLoginPage;
    ImageView imageViewAvatar;
    Bitmap bitmapAvatar;
    User user;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    FirebaseStorage firebaseStorage;
    StorageReference imageRepo;
    ProgressDialog progressDialog;
    public static final String EMAIL_REGEX = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
    public static final String NAME_REGEX = "^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*$";
    public static final String PASS_REGEX = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
    public static final String USER_COLLECTION = "user";
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        radioGroupGender = findViewById(R.id.radioGroup_gender);
        rbMale = findViewById(R.id.rb_male);
        rbFemale = findViewById(R.id.rb_female);
        etFname = findViewById(R.id.et_fname);
        etLname = findViewById(R.id.et_lname);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        tilFname = findViewById(R.id.til_fname);
        tilLname = findViewById(R.id.til_lname);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        btnSignUp = findViewById(R.id.button_sign_up);
        btnLoginPage = findViewById(R.id.button_login_page);
        imageViewAvatar = findViewById(R.id.iv_avatar);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Wait");
        progressDialog.setCancelable(false);

        imageViewAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        btnLoginPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fName = etFname.getText().toString().trim();
                String lName = etLname.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confPass = etConfirmPassword.getText().toString().trim();
                if (validateAll(fName, lName, email, password, confPass)) {
                    progressDialog.show();
                    user = new User();
                    user.setId(email);
                    user.setFname(fName);
                    user.setLname(lName);
                    if (radioGroupGender.getCheckedRadioButtonId() == R.id.rb_male) {
                        user.setGender("Male");
                    } else if (radioGroupGender.getCheckedRadioButtonId() == R.id.rb_female) {
                        user.setGender("Female");
                    }
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                        if (bitmapAvatar != null) {
                                            uploadImage(bitmapAvatar);
                                        } else {
                                            saveUser();
                                        }
                                    } else {
                                        Toast.makeText(SignUpActivity.this, task.getException() + "", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                }
                            });
                }
            }
        });
    }

    void saveUser() {
        db.collection(USER_COLLECTION).document(user.getId())
                .set(user.toHashMap())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            // Toast.makeText(AddEditMovieActivity.this, getResources().getString(R.string.movie_added), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SignUpActivity.this, task.getException() + "", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
                    saveUser();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                bitmapAvatar = (Bitmap) extras.get("data");
                imageViewAvatar.setImageBitmap(bitmapAvatar);
            }
        }
    }

    public static boolean validateEmail(String emailStr) {
        Pattern VALID_EMAIL = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = VALID_EMAIL.matcher(emailStr);
        return !matcher.find();
    }

    public static boolean validateName(String nameStr) {
        Pattern VALID_NAME = Pattern.compile(NAME_REGEX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = VALID_NAME.matcher(nameStr);
        return !matcher.find();
    }

    public static boolean validatePass(String passStr) {
        Pattern VALID_PASS = Pattern.compile(PASS_REGEX);
        Matcher matcher = VALID_PASS.matcher(passStr);
        return matcher.find();
    }

    public boolean validateAll(String fName, String lName, String email, String pass, String confirmPass) {
        boolean result = true;
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(tilEmail.getHint() + " cannot be blank!");
            result = false;
        } else if (validateEmail(email)) {
            tilEmail.setError("Provide valid Email address!");
            result = false;
        } else {
            tilEmail.setError(null);
        }
        if (TextUtils.isEmpty(fName)) {
            tilFname.setError(tilFname.getHint() + " cannot be blank!");
            result = false;
        } else if (validateName(fName)) {
            tilFname.setError("Provide valid First Name!");
            result = false;
        } else {
            tilFname.setError(null);
        }
        if (TextUtils.isEmpty(lName)) {
            tilLname.setError(tilLname.getHint() + " cannot be blank!");
            result = false;
        } else if (validateName(lName)) {
            tilLname.setError("Provide valid Last Name!");
            result = false;
        } else {
            tilLname.setError(null);
        }
        if (TextUtils.isEmpty(pass)) {
            tilPassword.setError(tilPassword.getHint() + " cannot be blank!");
            result = false;
        } else if (!validatePass(pass)) {
            tilPassword.setError("Password must be of 8 characters or more, must contain atleast a letter, a digit & a special character, no white spaces allowed");
            result = false;
        } else {
            tilPassword.setError(null);
        }
        if (!pass.equals(confirmPass)) {
            tilConfirmPassword.setError("Password does not match!");
            result = false;
        } else {
            tilConfirmPassword.setError(null);
        }
        return result;
    }
}
