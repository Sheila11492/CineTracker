package com.example.cinetracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.cinetracker.models.Contenido;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cinetracker.db";

    // Subimos versión a 8 para añadir capitulos_vistos
    private static final int DATABASE_VERSION = 8;

    public static final String TABLE_USUARIOS         = "usuarios";
    public static final String TABLE_SERIES_PELICULAS = "series_peliculas";
    public static final String TABLE_SEGUIMIENTO      = "seguimiento";
    public static final String TABLE_FAVORITOS        = "favoritos";
    public static final String TABLE_RECOMENDACIONES  = "recomendaciones";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Usuarios
        db.execSQL("CREATE TABLE " + TABLE_USUARIOS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, " +
                "email TEXT UNIQUE NOT NULL, " +
                "contrasena TEXT NOT NULL" +
                ");");

        // Contenidos
        db.execSQL("CREATE TABLE " + TABLE_SERIES_PELICULAS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "titulo TEXT NOT NULL, " +
                "tipo TEXT NOT NULL, " +            // 'Película' o 'Serie'
                "genero TEXT, " +
                "anio INTEGER, " +
                "duracion INTEGER DEFAULT 0, " +    // minutos (para serie = minutos por capítulo)
                "director TEXT, " +                 // solo Película
                "temporadas INTEGER, " +            // solo Serie
                "episodios INTEGER" +               // solo Serie
                ");");

        // Seguimiento
        db.execSQL("CREATE TABLE " + TABLE_SEGUIMIENTO + " (" +
                "usuario_id INTEGER NOT NULL, " +
                "serie_pelicula_id INTEGER NOT NULL, " +
                "estado TEXT, " +                 // 'Pendiente', 'Viendo', 'Visto' o NULL
                "fecha_inicio INTEGER, " +
                "fecha_fin INTEGER, " +
                "capitulos_vistos INTEGER, " +    // número de capítulos vistos (nullable)
                "PRIMARY KEY (usuario_id, serie_pelicula_id), " +
                "FOREIGN KEY (usuario_id) REFERENCES " + TABLE_USUARIOS + "(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (serie_pelicula_id) REFERENCES " + TABLE_SERIES_PELICULAS + "(id) ON DELETE CASCADE" +
                ");");
        db.execSQL("CREATE INDEX idx_seg_usuario  ON " + TABLE_SEGUIMIENTO + " (usuario_id);");
        db.execSQL("CREATE INDEX idx_seg_contenido ON " + TABLE_SEGUIMIENTO + " (serie_pelicula_id);");

        // Favoritos (con fecha_marcado)
        db.execSQL("CREATE TABLE " + TABLE_FAVORITOS + " (" +
                "usuario_id INTEGER NOT NULL, " +
                "serie_pelicula_id INTEGER NOT NULL, " +
                "fecha_marcado INTEGER, " +        // ms
                "PRIMARY KEY (usuario_id, serie_pelicula_id), " +
                "FOREIGN KEY (usuario_id) REFERENCES " + TABLE_USUARIOS + "(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (serie_pelicula_id) REFERENCES " + TABLE_SERIES_PELICULAS + "(id) ON DELETE CASCADE" +
                ");");
        db.execSQL("CREATE INDEX idx_fav_usuario  ON " + TABLE_FAVORITOS + " (usuario_id);");
        db.execSQL("CREATE INDEX idx_fav_contenido ON " + TABLE_FAVORITOS + " (serie_pelicula_id);");

        // Recomendaciones
        db.execSQL("CREATE TABLE " + TABLE_RECOMENDACIONES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "usuario_id INTEGER NOT NULL, " +
                "contenido_id INTEGER NOT NULL, " +
                "criterio TEXT, " +
                "fecha_generada INTEGER, " +       // ms
                "FOREIGN KEY (usuario_id)  REFERENCES " + TABLE_USUARIOS + "(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (contenido_id) REFERENCES " + TABLE_SERIES_PELICULAS + "(id) ON DELETE CASCADE" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Asegura columnas específicas
        if (oldVersion < 5) {
            try { db.execSQL("ALTER TABLE " + TABLE_SERIES_PELICULAS + " ADD COLUMN director TEXT"); } catch (Exception ignore) {}
            try { db.execSQL("ALTER TABLE " + TABLE_SERIES_PELICULAS + " ADD COLUMN temporadas INTEGER"); } catch (Exception ignore) {}
            try { db.execSQL("ALTER TABLE " + TABLE_SERIES_PELICULAS + " ADD COLUMN episodios INTEGER"); } catch (Exception ignore) {}
        }
        if (oldVersion < 6) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_RECOMENDACIONES + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "usuario_id INTEGER NOT NULL, " +
                    "contenido_id INTEGER NOT NULL, " +
                    "criterio TEXT, " +
                    "fecha_generada INTEGER, " +
                    "FOREIGN KEY (usuario_id)  REFERENCES " + TABLE_USUARIOS + "(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (contenido_id) REFERENCES " + TABLE_SERIES_PELICULAS + "(id) ON DELETE CASCADE" +
                    ");");
        }
        // fecha_marcado en favoritos
        if (oldVersion < 7) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_FAVORITOS + " ADD COLUMN fecha_marcado INTEGER");
            } catch (Exception ignore) {}
        }
        // capitulos_vistos en seguimiento
        if (oldVersion < 8) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_SEGUIMIENTO + " ADD COLUMN capitulos_vistos INTEGER");
            } catch (Exception ignore) {}
        }
    }

    // USUARIOS

    public long insertUsuario(String nombre, String email, String contrasena) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("nombre", nombre);
        cv.put("email", email);
        cv.put("contrasena", contrasena);
        long id = db.insert(TABLE_USUARIOS, null, cv);
        db.close();
        return id;
    }

    public boolean isEmailTaken(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT 1 FROM " + TABLE_USUARIOS + " WHERE email=? LIMIT 1",
                new String[]{email});
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    public int loginUser(String email, String contrasena) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT id FROM " + TABLE_USUARIOS + " WHERE email=? AND contrasena=? LIMIT 1",
                new String[]{email, contrasena});
        int id = -1;
        if (c.moveToFirst()) id = c.getInt(0);
        c.close();
        return id;
    }

    //  CONTENIDOS

    public long addContenido(Contenido c) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("titulo",    c.getTitulo());
        v.put("tipo",      c.getTipo());
        v.put("genero",    c.getGenero());
        v.put("anio",      c.getAño());
        v.put("duracion",  c.getDuracion());
        v.put("director",  c.getDirector());
        v.put("temporadas", c.getTemporadas());
        v.put("episodios",  c.getEpisodios());
        long id = db.insert(TABLE_SERIES_PELICULAS, null, v);
        db.close();
        return id;
    }

    public void updateContenido(Contenido c) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("titulo",     c.getTitulo());
        v.put("tipo",       c.getTipo());
        v.put("genero",     c.getGenero());
        v.put("anio",       c.getAño());
        v.put("duracion",   c.getDuracion());
        v.put("director",   c.getDirector());
        v.put("temporadas", c.getTemporadas());
        v.put("episodios",  c.getEpisodios());
        db.update(TABLE_SERIES_PELICULAS, v, "id=?", new String[]{String.valueOf(c.getId())});
        db.close();
    }

    public void deleteContenido(int contenidoId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SERIES_PELICULAS, "id=?", new String[]{String.valueOf(contenidoId)});
        db.close();
    }

    public List<Contenido> getAllContenidoList() {
        List<Contenido> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SERIES_PELICULAS + " ORDER BY titulo ASC", null);
        if (cursor.moveToFirst()) {
            do {
                Contenido c = new Contenido();
                c.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                c.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow("titulo")));
                c.setTipo(cursor.getString(cursor.getColumnIndexOrThrow("tipo")));
                c.setGenero(cursor.getString(cursor.getColumnIndexOrThrow("genero")));
                c.setAño(cursor.getInt(cursor.getColumnIndexOrThrow("anio")));
                c.setDuracion(cursor.getInt(cursor.getColumnIndexOrThrow("duracion")));
                int colDir = cursor.getColumnIndex("director");
                if (colDir >= 0) c.setDirector(cursor.getString(colDir));
                int colTem = cursor.getColumnIndex("temporadas");
                if (colTem >= 0) c.setTemporadas(cursor.getInt(colTem));
                int colEps = cursor.getColumnIndex("episodios");
                if (colEps >= 0) c.setEpisodios(cursor.getInt(colEps));
                lista.add(c);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    // FAVORITOS

    public void addFavorito(int usuarioId, int contenidoId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("usuario_id", usuarioId);
        v.put("serie_pelicula_id", contenidoId);
        v.put("fecha_marcado", System.currentTimeMillis());
        db.insertWithOnConflict(TABLE_FAVORITOS, null, v, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    public void removeFavorito(int usuarioId, int contenidoId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_FAVORITOS, "usuario_id=? AND serie_pelicula_id=?",
                new String[]{String.valueOf(usuarioId), String.valueOf(contenidoId)});
        db.close();
    }

    public boolean isFavorito(int usuarioId, int contenidoId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT 1 FROM " + TABLE_FAVORITOS + " WHERE usuario_id=? AND serie_pelicula_id=? LIMIT 1",
                new String[]{String.valueOf(usuarioId), String.valueOf(contenidoId)});
        boolean fav = c.moveToFirst();
        c.close();
        return fav;
    }

    // Alterna favorito y devuelve el nuevo estado.
    public boolean toggleFavorito(int usuarioId, int contenidoId) {
        if (isFavorito(usuarioId, contenidoId)) {
            removeFavorito(usuarioId, contenidoId);
            return false;
        } else {
            addFavorito(usuarioId, contenidoId);
            return true;
        }
    }

    // Devuelve la fecha de marcado o null si no es favorito.
    public Long getFechaFavorito(int usuarioId, int contenidoId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT fecha_marcado FROM " + TABLE_FAVORITOS +
                        " WHERE usuario_id=? AND serie_pelicula_id=? LIMIT 1",
                new String[]{String.valueOf(usuarioId), String.valueOf(contenidoId)});
        Long fecha = null;
        if (c.moveToFirst()) {
            if (!c.isNull(0)) fecha = c.getLong(0);
        }
        c.close();
        return fecha;
    }

    public List<Contenido> getFavoritos(int usuarioId) {
        List<Contenido> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT sp.* FROM " + TABLE_SERIES_PELICULAS + " sp " +
                        "JOIN " + TABLE_FAVORITOS + " f ON sp.id = f.serie_pelicula_id " +
                        "WHERE f.usuario_id=? ORDER BY f.fecha_marcado DESC, sp.titulo ASC",
                new String[]{String.valueOf(usuarioId)});
        if (cursor.moveToFirst()) {
            do {
                Contenido c = new Contenido();
                c.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                c.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow("titulo")));
                c.setTipo(cursor.getString(cursor.getColumnIndexOrThrow("tipo")));
                c.setGenero(cursor.getString(cursor.getColumnIndexOrThrow("genero")));
                c.setAño(cursor.getInt(cursor.getColumnIndexOrThrow("anio")));
                c.setDuracion(cursor.getInt(cursor.getColumnIndexOrThrow("duracion")));
                try { c.setDirector(cursor.getString(cursor.getColumnIndexOrThrow("director"))); } catch (Exception ignore) {}
                try { c.setTemporadas(cursor.getInt(cursor.getColumnIndexOrThrow("temporadas"))); } catch (Exception ignore) {}
                try { c.setEpisodios(cursor.getInt(cursor.getColumnIndexOrThrow("episodios"))); } catch (Exception ignore) {}
                lista.add(c);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    // SEGUIMIENTO

    public void upsertSeguimiento(int usuarioId, int contenidoId, String estado, Long fechaInicioMs, Long fechaFinMs) {
        upsertSeguimiento(usuarioId, contenidoId, estado, fechaInicioMs, fechaFinMs, null);
    }

    public void upsertSeguimiento(int usuarioId, int contenidoId, String estado,
                                  Long fechaInicioMs, Long fechaFinMs, Integer capitulosVistos) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        if (estado != null) cv.put("estado", estado);
        if (fechaInicioMs != null) cv.put("fecha_inicio", fechaInicioMs);
        if (fechaFinMs != null) cv.put("fecha_fin", fechaFinMs);
        if (capitulosVistos != null) cv.put("capitulos_vistos", capitulosVistos);

        int updated = db.update(TABLE_SEGUIMIENTO, cv,
                "usuario_id=? AND serie_pelicula_id=?",
                new String[]{String.valueOf(usuarioId), String.valueOf(contenidoId)});

        if (updated == 0) {
            cv.put("usuario_id", usuarioId);
            cv.put("serie_pelicula_id", contenidoId);
            db.insert(TABLE_SEGUIMIENTO, null, cv);
        }
        db.close();
    }

    public void registrarInicio(int usuarioId, int contenidoId) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor c = db.rawQuery(
                "SELECT estado, fecha_inicio FROM " + TABLE_SEGUIMIENTO +
                        " WHERE usuario_id=? AND serie_pelicula_id=?",
                new String[]{String.valueOf(usuarioId), String.valueOf(contenidoId)});
        boolean existe = c.moveToFirst();
        String estado = null;
        Long fechaInicio = null;
        if (existe) {
            estado = c.isNull(0) ? null : c.getString(0);
            fechaInicio = c.isNull(1) ? null : c.getLong(1);
        }
        c.close();

        ContentValues cv = new ContentValues();
        if (fechaInicio == null) cv.put("fecha_inicio", System.currentTimeMillis());
        if (estado == null || "Pendiente".equalsIgnoreCase(estado)) cv.put("estado", "Viendo");

        if (existe) {
            db.update(TABLE_SEGUIMIENTO, cv,
                    "usuario_id=? AND serie_pelicula_id=?",
                    new String[]{String.valueOf(usuarioId), String.valueOf(contenidoId)});
        } else {
            cv.put("usuario_id", usuarioId);
            cv.put("serie_pelicula_id", contenidoId);
            db.insert(TABLE_SEGUIMIENTO, null, cv);
        }
        db.close();
    }

    public void registrarFin(int usuarioId, int contenidoId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("estado", "Visto");
        cv.put("fecha_fin", System.currentTimeMillis());
        int updated = db.update(TABLE_SEGUIMIENTO, cv,
                "usuario_id=? AND serie_pelicula_id=?",
                new String[]{String.valueOf(usuarioId), String.valueOf(contenidoId)});
        if (updated == 0) {
            cv.put("usuario_id", usuarioId);
            cv.put("serie_pelicula_id", contenidoId);
            db.insert(TABLE_SEGUIMIENTO, null, cv);
        }
        db.close();
    }

    public String getEstadoSeguimiento(int usuarioId, int contenidoId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT estado FROM " + TABLE_SEGUIMIENTO + " WHERE usuario_id=? AND serie_pelicula_id=?",
                new String[]{String.valueOf(usuarioId), String.valueOf(contenidoId)});
        String estado = null;
        if (c.moveToFirst()) estado = c.getString(0);
        c.close();
        return estado;
    }

    // Devuelve la fecha de inicio del seguimiento o null si no existe.
    public Long getFechaInicioSeguimiento(int usuarioId, int contenidoId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT fecha_inicio FROM " + TABLE_SEGUIMIENTO +
                        " WHERE usuario_id=? AND serie_pelicula_id=?",
                new String[]{String.valueOf(usuarioId), String.valueOf(contenidoId)});
        Long fecha = null;
        if (c.moveToFirst() && !c.isNull(0)) fecha = c.getLong(0);
        c.close();
        return fecha;
    }

    // Devuelve la fecha de fin del seguimiento o null si no existe.
    public Long getFechaFinSeguimiento(int usuarioId, int contenidoId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT fecha_fin FROM " + TABLE_SEGUIMIENTO +
                        " WHERE usuario_id=? AND serie_pelicula_id=?",
                new String[]{String.valueOf(usuarioId), String.valueOf(contenidoId)});
        Long fecha = null;
        if (c.moveToFirst() && !c.isNull(0)) fecha = c.getLong(0);
        c.close();
        return fecha;
    }
    public Integer getCapitulosVistosForContent(int usuarioId, int contenidoId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = null;
        try {
            // Intentamos obtener capitulos_vistos (si existe), estado y episodios usando JOIN
            c = db.rawQuery(
                    "SELECT s.*, sp.episodios FROM " + TABLE_SEGUIMIENTO + " s " +
                            "LEFT JOIN " + TABLE_SERIES_PELICULAS + " sp ON sp.id = s.serie_pelicula_id " +
                            "WHERE s.usuario_id=? AND s.serie_pelicula_id=? LIMIT 1",
                    new String[]{ String.valueOf(usuarioId), String.valueOf(contenidoId) }
            );
            if (!c.moveToFirst()) return null;

            int idxCap = c.getColumnIndex("capitulos_vistos");
            if (idxCap >= 0 && !c.isNull(idxCap)) {
                return c.getInt(idxCap);
            }

            int idxEstado = c.getColumnIndex("estado");
            String estado = (idxEstado >= 0 && !c.isNull(idxEstado)) ? c.getString(idxEstado) : null;

            int idxEps = c.getColumnIndex("episodios");
            Integer eps = (idxEps >= 0 && !c.isNull(idxEps)) ? c.getInt(idxEps) : null;

            if ("Visto".equalsIgnoreCase(estado) && eps != null) {
                // Si está marcado Visto asumimos que se vieron todos los episodios
                return eps;
            }
            // si no hay dato significativo devolvemos null
            return null;
        } finally {
            if (c != null) c.close();
        }
    }


    // Devuelve capitulos_vistos (nullable)
    public Integer getCapitulosVistos(int usuarioId, int contenidoId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT capitulos_vistos FROM " + TABLE_SEGUIMIENTO +
                        " WHERE usuario_id=? AND serie_pelicula_id=?",
                new String[]{String.valueOf(usuarioId), String.valueOf(contenidoId)});
        Integer val = null;
        if (c.moveToFirst() && !c.isNull(0)) val = c.getInt(0);
        c.close();
        return val;
    }

    //  ESTADÍSTICAS

    public int contarPorTipo(String tipo) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_SERIES_PELICULAS + " WHERE tipo=?",
                new String[]{tipo});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public HashMap<String, Integer> contarPorGenero() {
        HashMap<String, Integer> map = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT genero, COUNT(*) FROM " + TABLE_SERIES_PELICULAS + " GROUP BY genero",
                null);
        if (cursor.moveToFirst()) {
            do { map.put(cursor.getString(0), cursor.getInt(1)); } while (cursor.moveToNext());
        }
        cursor.close();
        return map;
    }

    public int contarVistosPorTipo(int usuarioId, String tipo) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) " +
                        "FROM " + TABLE_SERIES_PELICULAS + " sp " +
                        "JOIN " + TABLE_SEGUIMIENTO + " s ON sp.id = s.serie_pelicula_id " +
                        "WHERE s.usuario_id=? AND s.estado='Visto' AND sp.tipo=?",
                new String[]{String.valueOf(usuarioId), tipo});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    public HashMap<String, Integer> contarVistosPorGenero(int usuarioId) {
        HashMap<String, Integer> map = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT sp.genero, COUNT(*) " +
                        "FROM " + TABLE_SERIES_PELICULAS + " sp " +
                        "JOIN " + TABLE_SEGUIMIENTO + " s ON sp.id = s.serie_pelicula_id " +
                        "WHERE s.usuario_id=? AND s.estado='Visto' " +
                        "GROUP BY sp.genero ORDER BY COUNT(*) DESC",
                new String[]{String.valueOf(usuarioId)});
        if (c.moveToFirst()) {
            do {
                String genero = c.getString(0);
                int cnt = c.getInt(1);
                if (genero != null) map.put(genero, cnt);
            } while (c.moveToNext());
        }
        c.close();
        return map;
    }

    public int minutosTotalesVistos(int usuarioId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT IFNULL(SUM(CASE WHEN sp.tipo='Serie' THEN (IFNULL(sp.duracion,0) * IFNULL(sp.episodios,0)) " +
                        "                      ELSE IFNULL(sp.duracion,0) END), 0) " +
                        "FROM " + TABLE_SERIES_PELICULAS + " sp " +
                        "JOIN " + TABLE_SEGUIMIENTO + " s ON sp.id = s.serie_pelicula_id " +
                        "WHERE s.usuario_id=? AND s.estado='Visto'",
                new String[]{String.valueOf(usuarioId)}
        );
        int minutos = 0;
        if (c.moveToFirst()) minutos = c.getInt(0);
        c.close();
        return minutos;
    }

    //Cuenta el número total de capítulos (episodios) de contenidos marcados como VISTO por el usuario.
    public int contarCapitulosVistos(int usuarioId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT IFNULL(SUM(IFNULL(sp.episodios,0)), 0) " +
                        "FROM " + TABLE_SERIES_PELICULAS + " sp " +
                        "JOIN " + TABLE_SEGUIMIENTO + " s ON sp.id = s.serie_pelicula_id " +
                        "WHERE s.usuario_id=? AND s.estado='Visto' AND sp.tipo='Serie'",
                new String[]{String.valueOf(usuarioId)}
        );
        int capitulos = 0;
        if (c.moveToFirst()) capitulos = c.getInt(0);
        c.close();
        return capitulos;
    }

    //  RECOMENDACIONES

    // Helper (usa la misma conexión)
    private void clearRecomendacionesUsuario(SQLiteDatabase db, int usuarioId) {
        db.delete(TABLE_RECOMENDACIONES, "usuario_id=?", new String[]{String.valueOf(usuarioId)});
    }

    private void insertRecomendacion(SQLiteDatabase db, int usuarioId, int contenidoId, String criterio) {
        ContentValues cv = new ContentValues();
        cv.put("usuario_id", usuarioId);
        cv.put("contenido_id", contenidoId);
        cv.put("criterio", criterio);
        cv.put("fecha_generada", System.currentTimeMillis());
        db.insert(TABLE_RECOMENDACIONES, null, cv);
    }

    // Lee recomendaciones del usuario (cierra solo el cursor)
    public List<Contenido> getRecomendacionesContenido(int usuarioId) {
        List<Contenido> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = null;
        try {
            c = db.rawQuery(
                    "SELECT sp.* " +
                            "FROM " + TABLE_SERIES_PELICULAS + " sp " +
                            "JOIN " + TABLE_RECOMENDACIONES + " r ON r.contenido_id = sp.id " +
                            "WHERE r.usuario_id=? " +
                            "ORDER BY r.fecha_generada DESC",
                    new String[]{String.valueOf(usuarioId)}
            );
            if (c.moveToFirst()) {
                do {
                    Contenido x = new Contenido();
                    x.setId(c.getInt(c.getColumnIndexOrThrow("id")));
                    x.setTitulo(c.getString(c.getColumnIndexOrThrow("titulo")));
                    x.setTipo(c.getString(c.getColumnIndexOrThrow("tipo")));
                    x.setGenero(c.getString(c.getColumnIndexOrThrow("genero")));
                    x.setAño(c.getInt(c.getColumnIndexOrThrow("anio")));
                    x.setDuracion(c.getInt(c.getColumnIndexOrThrow("duracion")));
                    int i;
                    if ((i = c.getColumnIndex("director"))   >= 0) x.setDirector(c.getString(i));
                    if ((i = c.getColumnIndex("temporadas")) >= 0) x.setTemporadas(c.getInt(i));
                    if ((i = c.getColumnIndex("episodios"))  >= 0) x.setEpisodios(c.getInt(i));
                    lista.add(x);
                } while (c.moveToNext());
            }
        } finally {
            if (c != null) c.close();
        }
        return lista;
    }

    /**
     * Genera recomendaciones simples usando UNA sola conexión y transacción:
     * 1) Detecta TOP géneros por vistos + favoritos.
     * 2) Limpia recomendaciones previas del usuario.
     * 3) Inserta hasta maxRecs contenidos de esos géneros que no estén vistos.
     */
    public void generarRecomendacionesBasicas(int usuarioId, int topGeneros, int maxRecs) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor g = null, cand = null;
        try {
            db.beginTransaction();

            // 1) Top géneros (vistos + favoritos)
            ArrayList<String> generosTop = new ArrayList<>();
            g = db.rawQuery(
                    "SELECT genero, COUNT(*) as cnt FROM (" +
                            "   SELECT sp.genero FROM " + TABLE_SERIES_PELICULAS + " sp " +
                            "   JOIN " + TABLE_SEGUIMIENTO + " s ON s.serie_pelicula_id = sp.id " +
                            "   WHERE s.usuario_id=? AND s.estado='Visto' AND sp.genero IS NOT NULL AND sp.genero<>''" +
                            "   UNION ALL " +
                            "   SELECT sp.genero FROM " + TABLE_SERIES_PELICULAS + " sp " +
                            "   JOIN " + TABLE_FAVORITOS + " f ON f.serie_pelicula_id = sp.id " +
                            "   WHERE f.usuario_id=? AND sp.genero IS NOT NULL AND sp.genero<>''" +
                            ") t GROUP BY genero ORDER BY cnt DESC LIMIT ?",
                    new String[]{ String.valueOf(usuarioId), String.valueOf(usuarioId), String.valueOf(topGeneros) }
            );
            if (g.moveToFirst()) {
                do { generosTop.add(g.getString(0)); } while (g.moveToNext());
            }
            if (g != null) { g.close(); g = null; }

            if (generosTop.isEmpty()) {
                db.setTransactionSuccessful();
                return;
            }

            // 2) Limpiar previas del usuario (misma conexión)
            clearRecomendacionesUsuario(db, usuarioId);

            // 3) Candidatos por género NO vistos
            String placeholders = new String(new char[generosTop.size()])
                    .replace("\0", "?,").replaceAll(",$", "");
            String[] args = concatArgs(
                    new String[]{String.valueOf(usuarioId)},
                    generosTop.toArray(new String[0])
            );

            cand = db.rawQuery(
                    "SELECT sp.id, sp.genero " +
                            "FROM " + TABLE_SERIES_PELICULAS + " sp " +
                            "LEFT JOIN " + TABLE_SEGUIMIENTO + " s " +
                            "  ON s.serie_pelicula_id = sp.id AND s.usuario_id=? AND s.estado='Visto' " +
                            "WHERE sp.genero IN (" + placeholders + ") AND s.serie_pelicula_id IS NULL " +
                            "ORDER BY RANDOM() LIMIT " + maxRecs,
                    args
            );

            int inserted = 0;
            if (cand.moveToFirst()) {
                do {
                    int contenidoId = cand.getInt(0);
                    String gen = cand.getString(1);
                    insertRecomendacion(db, usuarioId, contenidoId, "género: " + gen);
                    if (++inserted >= maxRecs) break;
                } while (cand.moveToNext());
            }

            db.setTransactionSuccessful();
        } finally {
            if (g != null)    g.close();
            if (cand != null) cand.close();
            if (db.inTransaction()) db.endTransaction();
        }
    }

    // Versión pública para otras clases (como el DAO)
    public void clearRecomendacionesUsuario(int usuarioId) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            db.delete(TABLE_RECOMENDACIONES, "usuario_id=?", new String[]{String.valueOf(usuarioId)});
            db.setTransactionSuccessful();
        } finally {
            if (db.inTransaction()) db.endTransaction();
        }
    }

    // Versión pública para insertar una recomendación (para uso desde DAOs/Controllers)
    public void insertRecomendacion(int usuarioId, int contenidoId, String criterio) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            ContentValues cv = new ContentValues();
            cv.put("usuario_id", usuarioId);
            cv.put("contenido_id", contenidoId);
            cv.put("criterio", criterio);
            cv.put("fecha_generada", System.currentTimeMillis());
            db.insert(TABLE_RECOMENDACIONES, null, cv);
            db.setTransactionSuccessful();
        } finally {
            if (db.inTransaction()) db.endTransaction();
        }
    }

    // UTIL

    private String[] concatArgs(String[] a, String[] b) {
        String[] out = new String[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }
}
