package com.example.cinetracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinetracker.R;
import com.example.cinetracker.database.DatabaseHelper;
import com.example.cinetracker.models.Contenido;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ContenidoAdapter extends RecyclerView.Adapter<ContenidoAdapter.ViewHolder> {

    private final Context context;
    private List<Contenido> lista;
    private final DatabaseHelper db;
    private final int usuarioId;

    public ContenidoAdapter(Context context, List<Contenido> lista, DatabaseHelper db, int usuarioId) {
        this.context = context;
        this.lista = lista == null ? new ArrayList<>() : lista;
        this.db = db;
        this.usuarioId = usuarioId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contenido, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Contenido c = lista.get(position);

        // Título
        h.titulo.setText(c.getTitulo());

        // Subtítulo por tipo
        if ("Serie".equalsIgnoreCase(c.getTipo())) {
            int temp = c.getTemporadas() == null ? 0 : c.getTemporadas();
            int eps  = c.getEpisodios() == null  ? 0 : c.getEpisodios();
            h.subtitulo.setText("Serie | " + temp + " temp · " + eps + " eps");
        } else {
            h.subtitulo.setText("Película | " + c.getDuracion() + " min");
        }

        // Estado (seguimiento)
        String estado = db.getEstadoSeguimiento(usuarioId, c.getId());
        h.estado.setText(estado == null ? "Sin estado" : estado);

        // Favorito (icono)
        boolean esFav = db.isFavorito(usuarioId, c.getId());
        h.btnFav.setImageResource(esFav ? R.drawable.ic_favorito_on : R.drawable.ic_favorito_off);

        // Fechas: favorito + seguimiento
        pintarFechas(h, c);

        // Click en estrella -> alternar favorito + refrescar fechas
        h.btnFav.setOnClickListener(v -> {
            boolean nuevo = db.toggleFavorito(usuarioId, c.getId());
            h.btnFav.setImageResource(nuevo ? R.drawable.ic_favorito_on : R.drawable.ic_favorito_off);
            pintarFechas(h, c);
        });

        // Tap corto -> diálogo estado + favorito
        h.cardRoot.setOnClickListener(v -> abrirDialogoEstadoFavorito(c, h));

        // Long press -> Editar / Eliminar
        h.cardRoot.setOnLongClickListener(v -> {
            mostrarMenuEditarEliminar(c, h, h.getAdapterPosition());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return (lista == null) ? 0 : lista.size();
    }

    public void updateData(List<Contenido> nuevaLista) {
        this.lista = nuevaLista == null ? new ArrayList<>() : nuevaLista;
        notifyDataSetChanged();
    }

    public void addContenido(Contenido c) {
        lista.add(c);
        notifyItemInserted(lista.size() - 1);
    }

    // Fechas (favorito + seguimiento)
    private void pintarFechas(ViewHolder h, Contenido c) {
        StringBuilder sb = new StringBuilder();

        // Fecha favorito
        Long fechaFav = db.getFechaFavorito(usuarioId, c.getId());
        if (fechaFav != null) {
            sb.append("⭐ desde ").append(formatFecha(fechaFav));
        }

        // Fechas seguimiento
        Long fIni = db.getFechaInicioSeguimiento(usuarioId, c.getId());
        Long fFin = db.getFechaFinSeguimiento(usuarioId, c.getId());
        if (fIni != null || fFin != null) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("Inicio: ").append(fIni == null ? "—" : formatFecha(fIni))
                    .append(" · Fin: ").append(fFin == null ? "—" : formatFecha(fFin));
        }

        String txt = sb.toString();
        h.fechas.setText(txt);
        h.fechas.setVisibility(txt.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private String formatFecha(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(millis));
    }

    // Diálogo para cambiar ESTADO y añadir/quitar FAVORITO (tap corto)
    private void abrirDialogoEstadoFavorito(Contenido c, ViewHolder h) {
        final String[] estados = new String[]{"Sin estado", "Pendiente", "Viendo", "Visto"};
        String actual = db.getEstadoSeguimiento(usuarioId, c.getId());
        int seleccionado = 0; // Sin estado
        if ("Pendiente".equalsIgnoreCase(actual)) seleccionado = 1;
        else if ("Viendo".equalsIgnoreCase(actual)) seleccionado = 2;
        else if ("Visto".equalsIgnoreCase(actual)) seleccionado = 3;

        boolean esFavAhora = db.isFavorito(usuarioId, c.getId());

        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setTitle(c.getTitulo());
        b.setSingleChoiceItems(estados, seleccionado, null);
        b.setPositiveButton("Guardar", (dlg, w) -> {
            AlertDialog ad = (AlertDialog) dlg;
            int which = ad.getListView().getCheckedItemPosition();

            String nuevoEstado = null;
            if (which == 1) nuevoEstado = "Pendiente";
            else if (which == 2) nuevoEstado = "Viendo";
            else if (which == 3) nuevoEstado = "Visto";

            Long now = System.currentTimeMillis();
            Long fin = ("Visto".equalsIgnoreCase(nuevoEstado)) ? now : null;

            // calcular capitulos_vistos: si es Serie y se marca como Visto => todos los episodios
            Integer capitulosVistos = null;
            if ("Serie".equalsIgnoreCase(c.getTipo()) && "Visto".equalsIgnoreCase(nuevoEstado)) {
                capitulosVistos = c.getEpisodios() == null ? 0 : c.getEpisodios();
            }

            try {
                java.lang.reflect.Method m = DatabaseHelper.class.getMethod(
                        "upsertSeguimiento", int.class, int.class, String.class, Long.class, Long.class, Integer.class);
                m.invoke(db, usuarioId, c.getId(), nuevoEstado, now, fin, capitulosVistos);
            } catch (NoSuchMethodException nsme) {
                db.upsertSeguimiento(usuarioId, c.getId(), nuevoEstado, now, fin);
            } catch (Exception ex) {
                db.upsertSeguimiento(usuarioId, c.getId(), nuevoEstado, now, fin);
            }

            String e = db.getEstadoSeguimiento(usuarioId, c.getId());
            h.estado.setText(e == null ? "Sin estado" : e);

            // refrescar fechas tras cambiar estado
            pintarFechas(h, c);
        });
        b.setNeutralButton(esFavAhora ? "Quitar favorito" : "Añadir favorito", (d, w) -> {
            boolean nuevo = db.toggleFavorito(usuarioId, c.getId());
            h.btnFav.setImageResource(nuevo ? R.drawable.ic_favorito_on : R.drawable.ic_favorito_off);
            pintarFechas(h, c);
        });
        b.setNegativeButton("Cancelar", null);
        b.show();
    }

    // Menú Editar / Eliminar (long press)
    private void mostrarMenuEditarEliminar(Contenido c, ViewHolder h, int position) {
        String[] opciones = new String[]{"Editar", "Eliminar"};
        new AlertDialog.Builder(context)
                .setTitle(c.getTitulo())
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) abrirDialogoEditar(c, h);
                    else confirmarEliminar(c, position);
                })
                .show();
    }

    private void confirmarEliminar(Contenido c, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar")
                .setMessage("¿Deseas eliminar \"" + c.getTitulo() + "\"?")
                .setPositiveButton("Eliminar", (d, w) -> {
                    db.deleteContenido(c.getId());
                    lista.remove(position);
                    notifyItemRemoved(position);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Diálogo de edición
    private void abrirDialogoEditar(Contenido c, ViewHolder h) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_contenido, null);

        EditText etTitulo     = view.findViewById(R.id.etTitulo);
        Spinner  spTipo       = view.findViewById(R.id.spTipo);
        Spinner  spGenero     = view.findViewById(R.id.spinnerGenero);
        EditText etAnio       = view.findViewById(R.id.etAnio);
        EditText etDuracion   = view.findViewById(R.id.etDuracion);
        EditText etDirector   = view.findViewById(R.id.etDirector);
        EditText etTemporadas = view.findViewById(R.id.etTemporadas);
        EditText etEpisodios  = view.findViewById(R.id.etEpisodios);

        // Spinner tipo
        ArrayAdapter<CharSequence> adapterTipos = ArrayAdapter.createFromResource(
                context, R.array.tipos_contenido, android.R.layout.simple_spinner_item);
        adapterTipos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTipo.setAdapter(adapterTipos);

        // Spinner genero (usa R.array.generos_array en strings.xml)
        try {
            ArrayAdapter<CharSequence> adapterGeneros = ArrayAdapter.createFromResource(
                    context, R.array.generos_array, android.R.layout.simple_spinner_item);
            adapterGeneros.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            if (spGenero != null) spGenero.setAdapter(adapterGeneros);

            // Pre-rellenar selección de género si existe
            if (c.getGenero() != null && spGenero != null) {
                for (int i = 0; i < adapterGeneros.getCount(); i++) {
                    if (c.getGenero().equalsIgnoreCase(adapterGeneros.getItem(i).toString())) {
                        spGenero.setSelection(i);
                        break;
                    }
                }
            }
        } catch (Exception ignored) {}

        // Pre-rellenar otros campos
        etTitulo.setText(c.getTitulo());
        etAnio.setText(c.getAño() == 0 ? "" : String.valueOf(c.getAño()));
        etDuracion.setText(c.getDuracion() == 0 ? "" : String.valueOf(c.getDuracion()));
        int idxTipo = "Serie".equalsIgnoreCase(c.getTipo()) ? 1 : 0;
        spTipo.setSelection(idxTipo);

        // Visibilidad inicial según tipo
        toggleCamposPorTipo(spTipo, etDirector, etTemporadas, etEpisodios);

        if ("Serie".equalsIgnoreCase(c.getTipo())) {
            etTemporadas.setText(String.valueOf(c.getTemporadas() == null ? 0 : c.getTemporadas()));
            etEpisodios.setText(String.valueOf(c.getEpisodios() == null ? 0 : c.getEpisodios()));
        } else {
            etDirector.setText(c.getDirector() == null ? "" : c.getDirector());
        }

        spTipo.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View v, int position, long id) {
                toggleCamposPorTipo(spTipo, etDirector, etTemporadas, etEpisodios);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        new AlertDialog.Builder(context)
                .setTitle("Editar contenido")
                .setView(view)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String titulo   = etTitulo.getText().toString().trim();
                    String tipo     = String.valueOf(spTipo.getSelectedItem());
                    String genero   = (spGenero == null || spGenero.getSelectedItem() == null) ? "" : String.valueOf(spGenero.getSelectedItem());
                    int anio        = parseIntSafe(etAnio.getText().toString(), 0);
                    int duracion    = parseIntSafe(etDuracion.getText().toString(), 0);

                    if (titulo.isEmpty()) return;

                    c.setTitulo(titulo);
                    c.setTipo(tipo);
                    c.setGenero(genero);
                    c.setAño(anio);
                    c.setDuracion(duracion);

                    if ("Serie".equalsIgnoreCase(tipo)) {
                        c.setDirector(null);
                        c.setTemporadas(parseIntSafe(etTemporadas.getText().toString(), 0));
                        c.setEpisodios(parseIntSafe(etEpisodios.getText().toString(), 0));
                    } else {
                        c.setDirector(etDirector.getText().toString().trim());
                        c.setTemporadas(0);
                        c.setEpisodios(0);
                    }

                    db.updateContenido(c);

                    // Refrescar la tarjeta
                    h.titulo.setText(c.getTitulo());
                    if ("Serie".equalsIgnoreCase(c.getTipo())) {
                        int temp = c.getTemporadas() == null ? 0 : c.getTemporadas();
                        int eps  = c.getEpisodios() == null  ? 0 : c.getEpisodios();
                        h.subtitulo.setText("Serie | " + temp + " temp · " + eps + " eps");
                    } else {
                        h.subtitulo.setText("Película | " + c.getDuracion() + " min");
                    }
                    notifyItemChanged(h.getAdapterPosition());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void toggleCamposPorTipo(Spinner spTipo, EditText etDirector, EditText etTemporadas, EditText etEpisodios) {
        String tipoSel = String.valueOf(spTipo.getSelectedItem());
        boolean esSerie = "Serie".equalsIgnoreCase(tipoSel);
        etDirector.setVisibility(esSerie ? View.GONE : View.VISIBLE);
        etTemporadas.setVisibility(esSerie ? View.VISIBLE : View.GONE);
        etEpisodios.setVisibility(esSerie ? View.VISIBLE : View.GONE);
    }

    private int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    // ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        View cardRoot;
        TextView titulo, subtitulo, estado, fechas;
        ImageView btnFav;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot = itemView.findViewById(R.id.cardRoot);
            titulo   = itemView.findViewById(R.id.textTitulo);
            subtitulo= itemView.findViewById(R.id.textSubtitulo);
            estado   = itemView.findViewById(R.id.textEstado);
            fechas   = itemView.findViewById(R.id.textFechas);
            btnFav   = itemView.findViewById(R.id.btnFav);
        }
    }
}
