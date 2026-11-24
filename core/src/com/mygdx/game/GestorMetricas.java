package com.mygdx.game;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.TimeUtils;

public final class GestorMetricas {

    private static final String PREFS_NAME = "PunchIt_Metricas_Save";

    private static final String KEY_ENTERS = "enters";
    private static final String KEY_RETENCION_TOTAL = "retencion_total_ms";
    private static final String KEY_RETENCION_MUESTRAS = "retencion_muestras";
    private static final String KEY_ESQUIVADOS = "esquivados";
    private static final String KEY_CHOQUES = "choques";
    private static final String KEY_RACHA_MAXIMA = "racha_maxima";
    private static final String KEY_ANTICIPACION_TOTAL = "anticipacion_total_ms";
    private static final String KEY_ANTICIPACION_MUESTRAS = "anticipacion_muestras";
    private static final String KEY_TIEMPO_AUTO = "tiempo_auto";
    private static final String KEY_TIEMPO_AVION = "tiempo_avion";
    private static final String KEY_TIEMPO_BARCO = "tiempo_barco";
    private static final String KEY_TIEMPO_TREN = "tiempo_tren";

    private static final String PLANTILLA_METRICAS = "artifacts/punch_it_metricas.html";
    private static final String PLANTILLA_GUIA = "artifacts/punch_it_guia.html";
    private static final String ARCHIVO_METRICAS = "punch_it_metricas.html";
    private static final String ARCHIVO_GUIA = "punch_it_guia.html";

    private static final long PRESS_NONE = -1L;

    private static Preferences prefs;

    private static long pressStartMs = PRESS_NONE;
    private static int rachaActual;
    private static float pendienteAuto;
    private static float pendienteAvion;
    private static float pendienteBarco;
    private static float pendienteTren;
    private static float pendienteVentana;

    private GestorMetricas() {
    }

    private static Preferences prefs() {
        if (prefs == null) {
            prefs = Gdx.app.getPreferences(PREFS_NAME);
        }
        return prefs;
    }

    public static void registrarInicioPulsacion() {
        Preferences p = prefs();
        p.putInteger(KEY_ENTERS, p.getInteger(KEY_ENTERS, 0) + 1);
        pressStartMs = TimeUtils.millis();
        p.flush();
    }

    public static void registrarFinPulsacion() {
        Preferences p = prefs();
        if (pressStartMs != PRESS_NONE) {
            long duracion = TimeUtils.millis() - pressStartMs;
            pressStartMs = PRESS_NONE;
            if (duracion >= 10L && duracion <= 10000L) {
                p.putLong(KEY_RETENCION_TOTAL, p.getLong(KEY_RETENCION_TOTAL, 0L) + duracion);
                p.putInteger(KEY_RETENCION_MUESTRAS, p.getInteger(KEY_RETENCION_MUESTRAS, 0) + 1);
            }
        }
        p.flush();
    }

    public static void registrarTiempoJuego(String modo, float deltaSegundos) {
        if (modo == null || deltaSegundos <= 0f) {
            return;
        }
        if ("auto".equals(modo)) {
            pendienteAuto += deltaSegundos;
        } else if ("avion".equals(modo)) {
            pendienteAvion += deltaSegundos;
        } else if ("barco".equals(modo)) {
            pendienteBarco += deltaSegundos;
        } else if ("tren".equals(modo)) {
            pendienteTren += deltaSegundos;
        } else {
            return;
        }
        pendienteVentana += deltaSegundos;
        if (pendienteVentana >= 5f) {
            flushPendingPlaytime();
            prefs().flush();
        }
    }

    public static void registrarObstaculoEsquivado() {
        Preferences p = prefs();
        p.putInteger(KEY_ESQUIVADOS, p.getInteger(KEY_ESQUIVADOS, 0) + 1);
        rachaActual++;
        if (rachaActual > p.getInteger(KEY_RACHA_MAXIMA, 0)) {
            p.putInteger(KEY_RACHA_MAXIMA, rachaActual);
        }
        p.flush();
    }

    public static void registrarChoque() {
        Preferences p = prefs();
        p.putInteger(KEY_CHOQUES, p.getInteger(KEY_CHOQUES, 0) + 1);
        rachaActual = 0;
        p.flush();
    }

    public static void registrarAnticipacion(long ms) {
        if (ms <= 0L || ms > 10000L) {
            return;
        }
        Preferences p = prefs();
        p.putLong(KEY_ANTICIPACION_TOTAL, p.getLong(KEY_ANTICIPACION_TOTAL, 0L) + ms);
        p.putInteger(KEY_ANTICIPACION_MUESTRAS, p.getInteger(KEY_ANTICIPACION_MUESTRAS, 0) + 1);
        p.flush();
    }

    public static void reiniciarMetricas() {
        Preferences p = prefs();
        p.clear();
        p.flush();
        rachaActual = 0;
        pressStartMs = PRESS_NONE;
        pendienteAuto = 0f;
        pendienteAvion = 0f;
        pendienteBarco = 0f;
        pendienteTren = 0f;
        pendienteVentana = 0f;
        generarPaginasHtml();
    }

    public static void abrirPaginaMetricas() {
        guardarMetricas();
        generarPaginasHtml();
        FileHandle archivoMetricas = Gdx.files.local(ARCHIVO_METRICAS);
        if (!archivoMetricas.exists()) {
            Gdx.app.error("GestorMetricas", "No se pudo generar " + ARCHIVO_METRICAS);
            return;
        }
        Gdx.net.openURI(archivoMetricas.file().toURI().toString());
    }

    public static void guardarMetricas() {
        flushPendingPlaytime();
        prefs().flush();
    }

    private static void flushPendingPlaytime() {
        if (pendienteAuto == 0f && pendienteAvion == 0f
                && pendienteBarco == 0f && pendienteTren == 0f) {
            pendienteVentana = 0f;
            return;
        }
        Preferences p = prefs();
        if (pendienteAuto != 0f) {
            p.putFloat(KEY_TIEMPO_AUTO, p.getFloat(KEY_TIEMPO_AUTO, 0f) + pendienteAuto);
            pendienteAuto = 0f;
        }
        if (pendienteAvion != 0f) {
            p.putFloat(KEY_TIEMPO_AVION, p.getFloat(KEY_TIEMPO_AVION, 0f) + pendienteAvion);
            pendienteAvion = 0f;
        }
        if (pendienteBarco != 0f) {
            p.putFloat(KEY_TIEMPO_BARCO, p.getFloat(KEY_TIEMPO_BARCO, 0f) + pendienteBarco);
            pendienteBarco = 0f;
        }
        if (pendienteTren != 0f) {
            p.putFloat(KEY_TIEMPO_TREN, p.getFloat(KEY_TIEMPO_TREN, 0f) + pendienteTren);
            pendienteTren = 0f;
        }
        pendienteVentana = 0f;
    }

    private static void generarPaginasHtml() {
        Preferences p = prefs();

        int esquivados = p.getInteger(KEY_ESQUIVADOS, 0);
        int choques = p.getInteger(KEY_CHOQUES, 0);
        int rachaMaxima = p.getInteger(KEY_RACHA_MAXIMA, 0);
        long anticipacionTotal = p.getLong(KEY_ANTICIPACION_TOTAL, 0L);
        int anticipacionMuestras = p.getInteger(KEY_ANTICIPACION_MUESTRAS, 0);
        long retencionTotal = p.getLong(KEY_RETENCION_TOTAL, 0L);
        int retencionMuestras = p.getInteger(KEY_RETENCION_MUESTRAS, 0);
        int enters = p.getInteger(KEY_ENTERS, 0);
        float tiempoAuto = p.getFloat(KEY_TIEMPO_AUTO, 0f);
        float tiempoAvion = p.getFloat(KEY_TIEMPO_AVION, 0f);
        float tiempoBarco = p.getFloat(KEY_TIEMPO_BARCO, 0f);
        float tiempoTren = p.getFloat(KEY_TIEMPO_TREN, 0f);

        long anticipacionMedia = anticipacionMuestras > 0
                ? anticipacionTotal / anticipacionMuestras : 0L;
        long retencionMedia = retencionMuestras > 0
                ? retencionTotal / retencionMuestras : 0L;

        int encuentros = esquivados + choques;
        float pctEsquivados = encuentros > 0
                ? (esquivados * 100f) / encuentros : 0f;

        double tiempoTotal = (double) tiempoAuto + (double) tiempoAvion
                + (double) tiempoBarco + (double) tiempoTren;
        float pctAuto = porcentaje(tiempoAuto, tiempoTotal);
        float pctAvion = porcentaje(tiempoAvion, tiempoTotal);
        float pctBarco = porcentaje(tiempoBarco, tiempoTotal);
        float pctTren = porcentaje(tiempoTren, tiempoTotal);

        String html = leerPlantilla(PLANTILLA_METRICAS);
        if (html != null) {
            html = html.replace("{{PCT_ESQUIVADOS}}", formatoPorcentaje(pctEsquivados));
            html = html.replace("{{ESQUIVADOS}}", String.valueOf(esquivados));
            html = html.replace("{{CHOCADOS}}", String.valueOf(choques));
            html = html.replace("{{RACHA_MAXIMA}}", String.valueOf(rachaMaxima));
            html = html.replace("{{ANTICIPACION_MS}}", String.valueOf(anticipacionMedia));
            html = html.replace("{{RETENCION_MS}}", String.valueOf(retencionMedia));
            html = html.replace("{{ENTERS}}", String.valueOf(enters));
            html = html.replace("{{TIEMPO_TOTAL}}", formatearTiempo(tiempoTotal));
            html = html.replace("{{TIEMPO_AUTO}}", formatearTiempo(tiempoAuto));
            html = html.replace("{{PCT_AUTO}}", formatoPorcentaje(pctAuto));
            html = html.replace("{{TIEMPO_AVION}}", formatearTiempo(tiempoAvion));
            html = html.replace("{{PCT_AVION}}", formatoPorcentaje(pctAvion));
            html = html.replace("{{TIEMPO_BARCO}}", formatearTiempo(tiempoBarco));
            html = html.replace("{{PCT_BARCO}}", formatoPorcentaje(pctBarco));
            html = html.replace("{{TIEMPO_TREN}}", formatearTiempo(tiempoTren));
            html = html.replace("{{PCT_TREN}}", formatoPorcentaje(pctTren));
            escribirLocal(ARCHIVO_METRICAS, html);
        }

        String guia = leerPlantilla(PLANTILLA_GUIA);
        if (guia != null) {
            escribirLocal(ARCHIVO_GUIA, guia);
        }
    }

    private static float porcentaje(float parte, double total) {
        if (total <= 0d) {
            return 0f;
        }
        return (float) ((parte * 100d) / total);
    }

    private static String formatoPorcentaje(float valor) {
        return String.format(Locale.US, "%.1f", valor);
    }

    private static String formatearTiempo(double segundos) {
        long total = Math.round(segundos);
        if (total < 0L) {
            total = 0L;
        }
        if (total < 60L) {
            return total + "s";
        }
        long minutos = total / 60L;
        long resto = total % 60L;
        return minutos + "m " + resto + "s";
    }

    private static String leerPlantilla(String ruta) {
        try {
            return Gdx.files.internal(ruta).readString("UTF-8");
        } catch (RuntimeException e) {
            Gdx.app.error("GestorMetricas", "No se pudo leer " + ruta, e);
            return null;
        }
    }

    private static void escribirLocal(String nombre, String contenido) {
        try {
            FileHandle destino = Gdx.files.local(nombre);
            Writer writer = destino.writer(false, "UTF-8");
            try {
                writer.write(contenido);
            } catch (IOException e) {
                Gdx.app.error("GestorMetricas", "No se pudo escribir " + nombre, e);
            } finally {
                try {
                    writer.close();
                } catch (IOException e) {
                    Gdx.app.error("GestorMetricas", "No se pudo cerrar " + nombre, e);
                }
            }
        } catch (RuntimeException e) {
            Gdx.app.error("GestorMetricas", "No se pudo crear " + nombre, e);
        }
    }
}
