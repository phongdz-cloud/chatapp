package hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.signup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Objects;

import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.Common.NodeNames;
import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.R;
import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.login.LoginActivity;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etName, etPassword, etConfirmPassword;
    private String email, name, password, confirmPassword;

    private ImageView ivProfile;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference fileStorage;
    private Uri localFileUri, serverFileUri;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        bindingView();

    }

    private void bindingView() {
        etEmail = findViewById(R.id.etEmail);
        etName = findViewById(R.id.etName);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        ivProfile = findViewById(R.id.ivProfile);
        fileStorage = FirebaseStorage.getInstance().getReference();
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            localFileUri = data.getData();
            ivProfile.setImageURI(localFileUri);
        }
    }

    public void pickImage(View v) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 101);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 102);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 101);
        } else {
            Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNameAndPhoto() {
        final String strFileName = firebaseUser.getUid() + ".jpg";

        final StorageReference fileRef = fileStorage.child("images/" + strFileName);
        progressBar.setVisibility(View.VISIBLE);
        fileRef.putFile(localFileUri).addOnCompleteListener(
                task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        fileRef.getDownloadUrl().addOnSuccessListener(uriSuccess -> {
                            serverFileUri = uriSuccess;

                            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .setPhotoUri(serverFileUri)
                                    .build();

                            firebaseUser.updateProfile(request).addOnCompleteListener(
                                    responseCreateElement -> {
                                        if (responseCreateElement.isSuccessful()) {
                                            String userID = firebaseUser.getUid();
                                            databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);

                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put(NodeNames.NAME, name);
                                            hashMap.put(NodeNames.EMAIL, email);
                                            hashMap.put(NodeNames.ONLINE, "true");
                                            hashMap.put(NodeNames.PHOTO, serverFileUri.getPath());

                                            databaseReference.child(userID).setValue(hashMap)
                                                    .addOnCompleteListener(task1 -> {
                                                        Toast.makeText(SignupActivity.this,
                                                                R.string.user_created_successfully, Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                                    });
                                        } else {
                                            Toast.makeText(this, getString(R.string.failed_to_update_profile,
                                                    responseCreateElement.getException()), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
                        });
                    }
                }
        );
    }

    public void updateOnlyName() {
        progressBar.setVisibility(View.VISIBLE);
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        firebaseUser.updateProfile(request).addOnCompleteListener(
                responseCreateElement -> {
                    progressBar.setVisibility(View.GONE);
                    if (responseCreateElement.isSuccessful()) {
                        String userID = firebaseUser.getUid();
                        databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);

                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put(NodeNames.NAME, name);
                        hashMap.put(NodeNames.EMAIL, email);
                        hashMap.put(NodeNames.ONLINE, "true");
                        hashMap.put(NodeNames.PHOTO, "");
                        progressBar.setVisibility(View.VISIBLE);
                        databaseReference.child(userID).setValue(hashMap)
                                .addOnCompleteListener(task1 -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(SignupActivity.this,
                                            R.string.user_created_successfully, Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                });
                    } else {
                        Toast.makeText(this, getString(R.string.failed_to_update_profile,
                                responseCreateElement.getException()), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    public void btnSignupClick(View v) {
        email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        name = Objects.requireNonNull(etName.getText()).toString().trim();
        password = Objects.requireNonNull(etPassword.getText()).toString().trim();
        confirmPassword = Objects.requireNonNull(etConfirmPassword.getText()).toString().trim();

        if (email.equals("")) {
            etEmail.setError(getString(R.string.enter_email));
        } else if (name.equals("")) {
            etName.setError(getString(R.string.enter_name));
        } else if (password.equals("")) {
            etPassword.setError(getString(R.string.enter_password));
        } else if (confirmPassword.equals("")) {
            etConfirmPassword.setError(getString(R.string.confirm_password));
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.enter_correct_email));
        } else if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.password_mismatch));
        } else {
            progressBar.setVisibility(View.VISIBLE);
            final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(responseCreateAuth -> {
                        progressBar.setVisibility(View.GONE);
                        if (responseCreateAuth.isSuccessful()) {
                            firebaseUser = firebaseAuth.getCurrentUser();
                            if (localFileUri != null) {
                                updateNameAndPhoto();
                            } else {
                                updateOnlyName();
                            }
                        } else {
                            Toast.makeText(SignupActivity.this,
                                    getString(R.string.sign_failed, responseCreateAuth.getException()), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}