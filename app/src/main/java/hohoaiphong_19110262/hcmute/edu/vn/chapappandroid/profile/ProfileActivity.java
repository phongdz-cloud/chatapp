package hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
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
import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.password.ChangePasswordActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etName;
    private String email, name;

    private ImageView ivProfile;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference fileStorage;
    private Uri localFileUri, serverFileUri;
    private FirebaseAuth firebaseAuth;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        bindingView();
    }

    private void bindingView() {
        etEmail = findViewById(R.id.etEmail);
        etName = findViewById(R.id.etName);
        name = Objects.requireNonNull(etName.getText()).toString();
        ivProfile = findViewById(R.id.ivProfile);
        progressBar = findViewById(R.id.progressBar);

        fileStorage = FirebaseStorage.getInstance().getReference();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            etName.setText(firebaseUser.getDisplayName());
            etEmail.setText(firebaseUser.getEmail());
            serverFileUri = firebaseUser.getPhotoUrl();

            if (serverFileUri != null) {
                Glide.with(this)
                        .load(serverFileUri)
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .into(ivProfile);
            }
        }
    }

    private void removePhoto() {
        name = Objects.requireNonNull(etName.getText()).toString();
        progressBar.setVisibility(View.VISIBLE);
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(null)
                .build();

        firebaseUser.updateProfile(request).addOnCompleteListener(
                responseCreateElement -> {
                    progressBar.setVisibility(View.GONE);
                    if (responseCreateElement.isSuccessful()) {
                        String userID = firebaseUser.getUid();
                        databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);

                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put(NodeNames.NAME, name);
                        hashMap.put(NodeNames.PHOTO, "");

                        databaseReference.child(userID).setValue(hashMap)
                                .addOnCompleteListener(task1 -> {
                                    Toast.makeText(ProfileActivity.this,
                                            R.string.photo_removed_successfully, Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, getString(R.string.failed_to_update_profile,
                                responseCreateElement.getException()), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void updateNameAndPhoto() {
        name = Objects.requireNonNull(etName.getText()).toString();
        final String strFileName = firebaseUser.getUid() + ".jpg";
        progressBar.setVisibility(View.VISIBLE);
        final StorageReference fileRef = fileStorage.child("images/" + strFileName);

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
                                            hashMap.put(NodeNames.PHOTO, serverFileUri.getPath());

                                            databaseReference.child(userID).setValue(hashMap)
                                                    .addOnCompleteListener(task1 -> {
                                                        finish();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            localFileUri = data.getData();
            ivProfile.setImageURI(localFileUri);
        }
    }

    public void btnLogoutClick(View v) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();
        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
        finish(); // terminate
    }

    public void btnSaveClick(View v) {
        if (Objects.requireNonNull(etName.getText()).toString().trim().equals("")) {
            etName.setError(getString(R.string.enter_name));
        } else {
            if (localFileUri != null) {
                updateNameAndPhoto();
            } else {
                updateOnlyName();
            }
        }
    }

    public void changeImage(View v) {
        if (serverFileUri == null) {
            pickImage();
        } else {
            PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.getMenuInflater().inflate(R.menu.menu_picture, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                int id = menuItem.getItemId();
                if (id == R.id.mnuChangePic) {
                    pickImage();
                } else if (id == R.id.mnuRemovePic) {
                    removePhoto();
                }
                return false;
            });
            popupMenu.show();
        }
    }

    private void pickImage() {

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

    public void updateOnlyName() {
        name = Objects.requireNonNull(etName.getText()).toString();
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        progressBar.setVisibility(View.VISIBLE);

        firebaseUser.updateProfile(request).addOnCompleteListener(
                responseCreateElement -> {
                    progressBar.setVisibility(View.GONE);
                    if (responseCreateElement.isSuccessful()) {
                        String userID = firebaseUser.getUid();
                        databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);

                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put(NodeNames.NAME, name);

                        databaseReference.child(userID).setValue(hashMap)
                                .addOnCompleteListener(task1 -> {
                                    finish();
                                });
                    } else {
                        Toast.makeText(this, getString(R.string.failed_to_update_profile,
                                responseCreateElement.getException()), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    public void btnChangePasswordClick(View view) {
        startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));
    }
}