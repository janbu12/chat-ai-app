package com.example.aichatapi.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.auth0.android.jwt.JWT;

import java.util.Date;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_splash); // Opsional layout splash

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkLoginStatus();
            }
        }, 1000); // Penundaan opsional
    }

    private void checkLoginStatus() {
        SharedPreferences sharedPref = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        String accessToken = sharedPref.getString("token", null);
        String username = sharedPref.getString("username", null); // Asumsi Anda menyimpan username juga

        if (accessToken != null && !accessToken.isEmpty()) {
            try {
                JWT jwt = new JWT(accessToken);

                // Periksa apakah token sudah kedaluwarsa
                // jwt.isExpired(0) akan memeriksa apakah sudah kedaluwarsa TEPAT pada saat ini.
                // Anda bisa tambahkan grace period jika mau, misal jwt.isExpired(60) (dalam detik)
                if (jwt.isExpired(0)) {
                    Log.d(TAG, "Access Token has expired. Navigating to LoginActivity.");
                    clearTokens(); // Hapus token yang kedaluwarsa
                    navigateToLoginActivity();
                } else {
                    // Token masih valid, lanjutkan ke MainActivity
                    Log.d(TAG, "Access Token is valid. Navigating to MainActivity.");
                    // Opsional: simpan username dari token jika belum tersimpan atau untuk validasi
                    // sharedPref.edit().putString("username", jwt.getClaim("username").asString()).apply();
                    navigateToMainActivity();
                }
            } catch (Exception e) {
                // Token tidak valid (misalnya, format rusak, bukan JWT yang valid)
                Log.e(TAG, "Invalid Access Token format or signature: " + e.getMessage());
                clearTokens(); // Hapus token yang rusak
                navigateToLoginActivity();
            }
        } else {
            // Tidak ada Access Token yang tersimpan
            Log.d(TAG, "No Access Token found. Navigating to LoginActivity.");
            navigateToLoginActivity();
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToLoginActivity() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // Modifikasi saveTokens untuk tidak menyimpan refresh_token
    private void saveTokens(String accessToken, String username) { // Tambahkan username jika Anda ingin menyimpannya
        SharedPreferences sharedPref = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("token", accessToken);
        editor.putString("username", username); // Simpan username
        editor.apply();
        Log.d(TAG, "Access Token and Username Saved.");
    }

    // Modifikasi clearTokens untuk tidak menghapus refresh_token
    private void clearTokens() {
        SharedPreferences sharedPref = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("token");
        editor.remove("username");
        editor.apply();
        Log.d(TAG, "Access Token and Username cleared.");
    }
}