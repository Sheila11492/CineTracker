package com.example.cinetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinetracker.adapters.ContenidoAdapter;
import com.example.cinetracker.auth.LoginActivity;
import com.example.cinetracker.auth.SessionManager;
import com.example.cinetracker.database.DatabaseHelper;
import com.example.cinetracker.models.Contenido;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ContenidoAdapter adapter;
    private DatabaseHelper db;
    private int usuarioId = -1;
    private boolean mostrandoFavoritos = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Lista
        recyclerView = findViewById(R.id.recyclerViewPeliculas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // DB
        db = new DatabaseHelper(this);

        // Comprobar sesión
        usuarioId = SessionManager.getUserId(this);
        if (usuarioId <= 0) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Cargar lista inicial
        cargarLista();

        // FAB para añadir
        FloatingActionButton fab = findViewById(R.id.fabAdd);
        if (fab != null) {
            fab.setOnClickListener(v -> showAddDialog());
        }
    }

    // Cargar lista
    private void cargarLista() {
        List<Contenido> lista = mostrandoFavoritos
                ? db.getFavoritos(usuarioId)
                : db.getAllContenidoList();

        if (adapter == null) {
            adapter = new ContenidoAdapter(this, lista, db, usuarioId);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(lista);
        }
    }

    // Diálogo para AÑADIR contenido
    private void showAddDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_contenido, null);

        // Referencias del layout
        EditText etTitulo     = view.findViewById(R.id.etTitulo);
        Spinner  spTipo       = view.findViewById(R.id.spTipo);
        Spinner  spinnerGenero= view.findViewById(R.id.spinnerGenero);
        EditText etAnio       = view.findViewById(R.id.etAnio);
        EditText etDuracion   = view.findViewById(R.id.etDuracion);
        EditText etDirector   = view.findViewById(R.id.etDirector);
        EditText etTemporadas = view.findViewById(R.id.etTemporadas);
        EditText etEpisodios  = view.findViewById(R.id.etEpisodios);
        RadioGroup rgEstado   = view.findViewById(R.id.rgEstado);
        CheckBox cbFavorito   = view.findViewById(R.id.cbFavorito);

        // Spinner de tipo
        if (spTipo != null) {
            ArrayAdapter<CharSequence> tiposAdapter = ArrayAdapter.createFromResource(
                    this, R.array.tipos_contenido, android.R.layout.simple_spinner_item);
            tiposAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spTipo.setAdapter(tiposAdapter);
        }

        // Spinner de género (usa array generos_array)
        if (spinnerGenero != null) {
            try {
                ArrayAdapter<CharSequence> generosAdapter = ArrayAdapter.createFromResource(
                        this, R.array.generos_array, android.R.layout.simple_spinner_item);
                generosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerGenero.setAdapter(generosAdapter);
            } catch (Exception ignored) {

            }
        }

        // Mostrar/ocultar campos según el tipo seleccionado
        Runnable refrescarCampos = () -> {
            String tipoSel = (spTipo == null) ? "Película" : String.valueOf(spTipo.getSelectedItem());
            boolean esSerie = "Serie".equalsIgnoreCase(tipoSel);
            if (etDirector != null) etDirector.setVisibility(esSerie ? View.GONE : View.VISIBLE);
            if (etTemporadas != null) etTemporadas.setVisibility(esSerie ? View.VISIBLE : View.GONE);
            if (etEpisodios != null) etEpisodios.setVisibility(esSerie ? View.VISIBLE : View.GONE);
        };
        refrescarCampos.run();
        if (spTipo != null) {
            spTipo.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(android.widget.AdapterView<?> parent, View v, int position, long id) { refrescarCampos.run(); }
                @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
            });
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Añadir contenido")
                .setView(view)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String titulo   = safeText(etTitulo);
                    String tipo     = (spTipo == null) ? "Película" : String.valueOf(spTipo.getSelectedItem());
                    // Género: preferimos spinnerGenero; si no existe usamos cadena vacía
                    String genero;
                    if (spinnerGenero != null && spinnerGenero.getSelectedItem() != null) {
                        genero = String.valueOf(spinnerGenero.getSelectedItem());
                    } else {
                        genero = "";
                    }

                    int anio        = parseIntSafe(etAnio == null ? "" : etAnio.getText().toString(), 0);
                    int duracion    = parseIntSafe(etDuracion == null ? "" : etDuracion.getText().toString(), 0);

                    if (titulo.isEmpty()) {
                        Toast.makeText(this, "El título es obligatorio", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Crear objeto Contenido
                    Contenido c = new Contenido();
                    c.setTitulo(titulo);
                    c.setTipo(tipo);
                    c.setGenero(genero);
                    c.setAño(anio);
                    c.setDuracion(duracion);

                    if ("Serie".equalsIgnoreCase(tipo)) {
                        c.setDirector(null);
                        c.setTemporadas(parseIntSafe(etTemporadas == null ? "" : etTemporadas.getText().toString(), 0));
                        c.setEpisodios(parseIntSafe(etEpisodios == null ? "" : etEpisodios.getText().toString(), 0));
                    } else { // Película
                        c.setDirector(safeText(etDirector));
                        c.setTemporadas(0);
                        c.setEpisodios(0);
                    }

                    // Insertar en DB
                    long id = db.addContenido(c);
                    if (id <= 0) {
                        Toast.makeText(this, "Error al insertar en la base de datos", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    c.setId((int) id);

                    // Favorito
                    if (cbFavorito != null && cbFavorito.isChecked()) {
                        db.addFavorito(usuarioId, c.getId());
                    }

                    // Estado (seguimiento)
                    String estado = (rgEstado == null) ? null : getEstadoSeleccionado(rgEstado);
                    if (estado != null) {
                        Long now = System.currentTimeMillis();
                        Long fin = "Visto".equalsIgnoreCase(estado) ? now : null;
                        db.upsertSeguimiento(usuarioId, c.getId(), estado, now, fin);
                    }

                    // Refrescar lista según filtro
                    if (mostrandoFavoritos) {
                        cargarLista();
                    } else {
                        if (adapter == null) {
                            cargarLista();
                        } else {
                            adapter.addContenido(c);
                        }
                    }

                    Toast.makeText(this, "Contenido añadido", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String getEstadoSeleccionado(RadioGroup rg) {
        if (rg == null) return null;
        int id = rg.getCheckedRadioButtonId();
        if (id == View.NO_ID) return null;
        RadioButton rb = rg.findViewById(id);
        if (rb == null) return null;
        String txt = rb.getText().toString();
        if ("Pendiente".equalsIgnoreCase(txt)) return "Pendiente";
        if ("Viendo".equalsIgnoreCase(txt)) return "Viendo";
        if ("Visto".equalsIgnoreCase(txt)) return "Visto";
        return null;
    }

    private String safeText(EditText et) {
        return (et == null || et.getText() == null) ? "" : et.getText().toString().trim();
    }

    private int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    // Menú superior
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem fav = menu.findItem(R.id.action_favoritos);
        if (fav != null) fav.setIcon(mostrandoFavoritos
                ? R.drawable.ic_favorito_on
                : R.drawable.ic_favorito_off);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_favoritos) {
            mostrandoFavoritos = !mostrandoFavoritos;
            cargarLista();
            item.setIcon(mostrandoFavoritos ? R.drawable.ic_favorito_on : R.drawable.ic_favorito_off);
            Toast.makeText(this, mostrandoFavoritos ? "Mostrando favoritos" : "Mostrando todo", Toast.LENGTH_SHORT).show();
            return true;

        } else if (id == R.id.action_estadisticas) {
            startActivity(new Intent(this, EstadisticasActivity.class));
            return true;

        } else if (id == R.id.action_logout) {
            SessionManager.logout(this);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        } else if (id == R.id.action_recomendaciones) {
            startActivity(new Intent(this, RecomendacionesActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
