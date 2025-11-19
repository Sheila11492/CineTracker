package com.example.cinetracker.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cinetracker.MainActivity;
import com.example.cinetracker.R;
import com.example.cinetracker.database.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // DB
        db = new DatabaseHelper(this);

        // 1) Si ya hay sesión válida -> Main
        int userId = SessionManager.getUserId(this);
        if (userId > 0) {
            Log.d(TAG, "Sesion activa userId=" + userId + " -> entrando a Main");
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // 2) Si hay credenciales guardadas, intentar auto-login
        String savedEmail = SessionManager.getSavedEmail(this);
        String savedPass  = SessionManager.getSavedPassword(this);
        if (savedEmail != null && savedPass != null) {
            Log.d(TAG, "Credenciales guardadas encontradas, intentando auto-login para " + savedEmail);
            int id = db.loginUser(savedEmail, savedPass);
            Log.d(TAG, "auto-login returned id=" + id);
            if (id > 0) {
                SessionManager.saveLogin(getApplicationContext(), id);
                Toast.makeText(this, "Auto-login correcto", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return;
            } else {
                Log.d(TAG, "Auto-login fallido: credenciales guardadas incorrectas o usuario no existe");
            }
        }

        // Inicializar vistas
        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        if (savedEmail != null) etEmail.setText(savedEmail);
        if (savedPass  != null) etPassword.setText(savedPass);

        btnLogin.setOnClickListener(v -> doLogin());
        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String pass  = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Introduce email y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        int id = db.loginUser(email, pass);
        Log.d(TAG, "loginUser returned id=" + id);

        if (id > 0) {
            // Guardar sesión y credenciales (texto plano)
            boolean ok1 = SessionManager.saveLogin(getApplicationContext(), id);
            boolean ok2 = SessionManager.saveCredentials(getApplicationContext(), email, pass);
            Log.d(TAG, "saveLogin commit result=" + ok1 + ", saveCredentials=" + ok2);

            Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
        }
    }
}
