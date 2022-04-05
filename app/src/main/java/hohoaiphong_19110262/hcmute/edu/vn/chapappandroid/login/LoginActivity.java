package hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.Common.Util;
import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.MainActivity;
import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.MessageActivity;
import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.R;
import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.password.ResetPasswordActivity;
import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.signup.SignupActivity;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPassword;
    private String email, password;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = (TextInputEditText) findViewById(R.id.etEmail);
        etPassword = (TextInputEditText) findViewById(R.id.etPassword);

        progressBar = findViewById(R.id.progressBar);

    }

    public void tvSignupClick(View v) {
        startActivity(new Intent(this, SignupActivity.class));
    }

    public void btnLoginClick(View v) {
        email = Objects.requireNonNull(etEmail.getText()).toString();
        password = Objects.requireNonNull(etPassword.getText()).toString().trim();

        if (email.equals("")) {
            etEmail.setError(getString(R.string.enter_email));
        } else if (password.equals("")) {
            etPassword.setError(getString(R.string.enter_password));
        } else {
            if (Util.connectionAvailable(this)) {
                progressBar.setVisibility(View.VISIBLE);
                final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Login Failed : " +
                                        task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                startActivity(new Intent(LoginActivity.this, MessageActivity.class));
            }
        }
    }

    public void tvRestPasswordClick(View view) {
        startActivity(new Intent(this, ResetPasswordActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }
}