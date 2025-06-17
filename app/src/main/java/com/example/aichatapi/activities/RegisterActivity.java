package com.example.aichatapi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aichatapi.R;
import com.example.aichatapi.tasks.RegisterTask;

public class RegisterActivity extends AppCompatActivity {
    private EditText etUsername, etFullname, etPassword, etConfirmPassword;
    private TextView loginLink;
    private ProgressBar progressBar;
    private Button btnRegister;

    private static final String N8N_BASE_URL = "https://allowing-perfectly-lynx.ngrok-free.app/webhook/fec1022f-7840-4b53-9dcd-cce4f525a9fc/api";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.et_username);
        etFullname = findViewById(R.id.et_name);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirmPassword);
        loginLink = findViewById(R.id.login_link);
        progressBar = findViewById(R.id.progress_bar_register);
        btnRegister = findViewById(R.id.btn_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Login Link Function
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegistration();
            }
        });

    }

    private void performRegistration() {
        String username = etUsername.getText().toString().trim();
        String name = etFullname.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // --- Client-side Validation ---
        if (username.isEmpty() || name.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Password dan konfirmasi password tidak cocok!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        new RegisterTask(N8N_BASE_URL, new RegisterTask.RegisterCallback() {
            @Override
            public void onRegisterSuccess() {
                showLoading(false);
                Toast.makeText(RegisterActivity.this, "Registrasi Berhasil! Silakan Login.", Toast.LENGTH_LONG).show();
                // Redirect ke LoginActivity
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onRegisterFailure(String errorMessage, int httpCode) {
                showLoading(false);
                Log.e("REGISTER_FAIL", "Error: " + errorMessage + " HTTP Code: " + httpCode);
                // Tampilkan pesan error spesifik dari API
                Toast.makeText(RegisterActivity.this, "Registrasi gagal: " + errorMessage, Toast.LENGTH_LONG).show();
                // Anda bisa tambahkan logika lebih lanjut berdasarkan httpCode (misal 409 Conflict untuk username sudah ada)
            }
        }, this).execute(username, name, password, confirmPassword);
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(false);
            etUsername.setEnabled(false);
            etFullname.setEnabled(false);
            etPassword.setEnabled(false);
            etConfirmPassword.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnRegister.setEnabled(true);
            etUsername.setEnabled(true);
            etFullname.setEnabled(true);
            etPassword.setEnabled(true);
            etConfirmPassword.setEnabled(true);
        }
    }
}
