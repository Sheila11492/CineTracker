package com.example.cinetracker.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cinetracker.R;
import com.example.cinetracker.database.DatabaseHelper;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNombre, etEmail, etPassword, etPassword2;
    private Button btnCrearCuenta;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);

        etNombre     = findViewById(R.id.etNombre);
        etEmail      = findViewById(R.id.etEmail);
        etPassword   = findViewById(R.id.etPassword);
        etPassword2  = findViewById(R.id.etPassword2);
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);

        btnCrearCuenta.setOnClickListener(v -> doRegister());
    }

    private void doRegister() {
        String nombre = etNombre.getText().toString().trim();
        String email  = etEmail.getText().toString().trim();
        String pass1  = etPassword.getText().toString().trim();
        String pass2  = etPassword2.getText().toString().trim();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(pass1) || TextUtils.isEmpty(pass2)) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email no válido", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pass1.equals(pass2)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }
        if (db.isEmailTaken(email)) {
            Toast.makeText(this, "Ese email ya está registrado", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = db.insertUsuario(nombre, email, pass1);
        if (id > 0) {
            Toast.makeText(this, "Cuenta creada. Inicia sesión.", Toast.LENGTH_SHORT).show();
            finish(); // volvemos al LoginActivity
        } else {
            Toast.makeText(this, "Error al crear la cuenta", Toast.LENGTH_SHORT).show();
        }
    }
}
