package com.example.cinetracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinetracker.adapters.ContenidoAdapter;
import com.example.cinetracker.auth.LoginActivity;
import com.example.cinetracker.auth.SessionManager;
import com.example.cinetracker.database.DatabaseHelper;
import com.example.cinetracker.models.Contenido;

import java.util.List;

public class RecomendacionesActivity extends AppCompatActivity {

    private RecyclerView rv;
    private DatabaseHelper db;
    private int usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recomendaciones);

        rv = findViewById(R.id.rvRecs);
        rv.setLayoutManager(new LinearLayoutManager(this));

        db = new DatabaseHelper(this);
        usuarioId = SessionManager.getUserId(this);
        if (usuarioId <= 0) {
            // sin sesión -> vuelve a login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Genera y lee recomendaciones
        try {
            // topGeneros=3, maxRecs=20
            db.generarRecomendacionesBasicas(usuarioId, 3, 20);
            List<Contenido> recs = db.getRecomendacionesContenido(usuarioId);

            if (recs == null || recs.isEmpty()) {
                Toast.makeText(this, "No hay suficientes datos para recomendar aún.", Toast.LENGTH_LONG).show();
            }

            // Reutilizamos el ContenidoAdapter para poder marcar favs y estado también aquí
            ContenidoAdapter adapter = new ContenidoAdapter(this, recs, db, usuarioId);
            rv.setAdapter(adapter);

        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar recomendaciones: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
