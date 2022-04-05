package hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.password;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.R;

public class ChangePasswordActivity extends AppCompatActivity {
    private TextInputEditText etPassword, etConfirmPassword;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        progressBar = findViewById(R.id.progressBar);

    }

    public void btnChangePasswordClick(View view) {
        final String password = Objects.requireNonNull(etPassword.getText()).toString().trim();
        final String confirmPassword = Objects.requireNonNull(etConfirmPassword.getText()).toString();

        if (password.equals("")) {
            etPassword.setError(getString(R.string.enter_password));
        } else if (confirmPassword.equals("")) {
            etConfirmPassword.setError(getString(R.string.confirm_password));
        } else if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.password_mismatch));
        } else {
            progressBar.setVisibility(View.VISIBLE);
            final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

            if (firebaseUser != null) {
                firebaseUser.updatePassword(password).addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, R.string.password_change_successfully, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, getString(R.string.something_went_wrong, task.getException()), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}