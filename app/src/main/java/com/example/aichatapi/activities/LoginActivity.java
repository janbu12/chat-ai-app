package com.example.aichatapi.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aichatapi.R; // Sesuaikan dengan package Anda
import com.example.aichatapi.models.LoginResponse;
import com.example.aichatapi.tasks.LoginTask;

// Contoh penyimpanan token sederhana (untuk development, bukan produksi!)
import android.content.SharedPreferences;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private TextView registerLink;
    private Button btnLogin;
    private ProgressBar progressBar;

    // Ganti dengan BASE URL N8N Anda (misal: "https://allowing-perfectly-lynx.ngrok-free.app/webhook-test/api")
    private static final String N8N_BASE_URL = "https://allowing-perfectly-lynx.ngrok-free.app/webhook/fec1022f-7840-4b53-9dcd-cce4f525a9fc/api";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login); // Pastikan Anda punya activity_login.xml

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);
        registerLink = findViewById(R.id.register_link);

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Username dan password tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    return;
                }

                showLoading(true);

                // Panggil AsyncTask untuk login
                new LoginTask(N8N_BASE_URL, new LoginTask.LoginCallback() {
                    @Override
                    public void onLoginSuccess(LoginResponse response) {
                        showLoading(false);
                        Toast.makeText(LoginActivity.this, response.getMessage(), Toast.LENGTH_LONG).show();
                        // Simpan token (Contoh sederhana, gunakan EncryptedSharedPreferences untuk produksi)
                        saveTokens(response.getToken(), response.getUser().getUsername());
                        // Lanjutkan ke Activity utama atau Dashboard
                         Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                         startActivity(intent);
                         finish();
                    }

                    @Override
                    public void onLoginFailure(String errorMessage, int httpCode) {
                        showLoading(false);
                        Log.e("LOGIN_FAIL", "Error: " + errorMessage + " HTTP Code: " + httpCode);
                        // Tampilkan pesan error spesifik dari API (misal: "Invalid username or password")
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        // Anda bisa menampilkan pesan yang lebih spesifik berdasarkan httpCode
                        // if (httpCode == 404 || httpCode == 401) { ... }
                    }
                }).execute(username, password);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE); // Tampilkan ProgressBar
            btnLogin.setEnabled(false); // Nonaktifkan tombol Login
            etUsername.setEnabled(false); // Nonaktifkan input username
            etPassword.setEnabled(false); // Nonaktifkan input password
        } else {
            progressBar.setVisibility(View.GONE); // Sembunyikan ProgressBar
            btnLogin.setEnabled(true); // Aktifkan tombol Login
            etUsername.setEnabled(true); // Aktifkan input username
            etPassword.setEnabled(true); // Aktifkan input password
        }
    }

    // --- Contoh Penyimpanan Token (SANGAT DASAR & TIDAK AMAN UNTUK PRODUKSI) ---
    // Gunakan EncryptedSharedPreferences atau Android Keystore untuk produksi
    private void saveTokens(String token, String username) {
        SharedPreferences sharedPref = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("token", token);
        editor.putString("username", username);
        editor.apply();
        Log.d("TOKEN_SAVE", "Access Token Saved: " + token);
        Log.d("USERNAME_SAVE", "Username Saved: " + username);
    }

    private String getToken() {
        SharedPreferences sharedPref = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        return sharedPref.getString("token", null);
    }
}
