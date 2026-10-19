package com.example.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.net.URL;

public class HelloApplication extends Application {

    // Los archivos de imagen y música van en src/main/resources/com/example/demo/
    String NOMBRE_IMAGEN = "fondo.jpg";
    double OPACIDAD_IMAGEN = 1.0;          // de 0 (invisible) a 1 (completa)
    String NOMBRE_MUSICA = "musica.mp3";
    double VOLUMEN_MUSICA = 0.4;           // de 0 (silencio) a 1 (máximo)

    // En rgba(...) el último número es la transparencia: de 0 (invisible) a 1 (sólido)
    String COLOR_FONDO_VENTANA = "#2b2d42";
    String COLOR_TEXTO_MENSAJE = "white";
    String COLOR_CUADRO_CLARO = "rgba(255, 255, 255, 0.60)";
    String COLOR_CUADRO_OSCURO = "rgba(225, 232, 245, 0.35)";
    String COLOR_NUMERO_FIJO = "black";
    String COLOR_NUMERO_USUARIO = "#0b3d91";
    String COLOR_ERROR_FONDO = "rgba(255, 90, 90, 0.75)";
    String COLOR_ERROR_NUMERO = "#7a0010";
    String COLOR_BORDE = "rgba(255, 255, 255, 0.55)";

    // Plantillas: 0 = casilla vacía
    int[][] facil = {
            {0, 7, 0, 0, 0, 4, 0, 0, 8},
            {8, 6, 5, 7, 0, 1, 3, 0, 0},
            {2, 4, 0, 0, 0, 8, 9, 0, 6},
            {6, 0, 8, 9, 4, 0, 2, 0, 5},
            {5, 9, 2, 1, 3, 0, 4, 0, 0},
            {0, 0, 0, 0, 8, 0, 0, 0, 9},
            {0, 0, 6, 4, 0, 9, 0, 2, 0},
            {0, 0, 0, 6, 2, 5, 7, 9, 0},
            {9, 2, 0, 0, 0, 3, 6, 0, 4}
    };

    int[][] normal = {
            {0, 0, 8, 0, 5, 0, 7, 0, 3},
            {0, 5, 9, 7, 0, 3, 4, 0, 8},
            {0, 0, 0, 6, 0, 8, 0, 0, 0},
            {5, 0, 0, 0, 0, 0, 6, 0, 0},
            {9, 0, 6, 0, 1, 0, 0, 4, 2},
            {4, 7, 0, 0, 0, 0, 9, 0, 0},
            {0, 0, 0, 0, 9, 0, 2, 0, 0},
            {7, 9, 1, 0, 8, 0, 0, 0, 0},
            {3, 0, 4, 0, 7, 5, 8, 0, 6}
    };

    int[][] dificil = {
            {0, 6, 0, 0, 0, 7, 0, 2, 0},
            {0, 0, 4, 5, 3, 0, 0, 0, 0},
            {0, 0, 0, 9, 6, 1, 0, 0, 8},
            {0, 0, 5, 0, 0, 0, 0, 8, 0},
            {3, 0, 0, 0, 7, 0, 1, 5, 0},
            {9, 0, 0, 0, 0, 8, 0, 0, 0},
            {4, 5, 0, 1, 0, 0, 0, 6, 0},
            {0, 0, 1, 0, 0, 0, 0, 0, 0},
            {7, 0, 0, 0, 4, 0, 3, 0, 0}
    };

    int[][] tablero = new int[9][9];
    boolean[][] fija = new boolean[9][9];
    TextField[][] campos = new TextField[9][9];

    // Evita que escribio() reaccione cuando es el programa quien cambia el texto
    boolean pintando = false;
    String nivelActual = "";

    Label mensaje = new Label("");
    MediaPlayer reproductor;   // se guarda en un campo para que no lo borre el recolector de basura
    Button botonMusica = new Button("🔊 Música");

    @Override
    public void start(Stage ventana) {
        Button botonFacil = new Button("Fácil");
        botonFacil.setOnAction(e -> cargarPlantilla(facil, "Fácil"));

        Button botonNormal = new Button("Normal");
        botonNormal.setOnAction(e -> cargarPlantilla(normal, "Normal"));

        Button botonDificil = new Button("Difícil");
        botonDificil.setOnAction(e -> cargarPlantilla(dificil, "Difícil"));

        botonMusica.setOnAction(e -> alternarMusica());

        HBox niveles = new HBox(10, botonFacil, botonNormal, botonDificil, botonMusica);
        niveles.setAlignment(Pos.CENTER);

        GridPane cuadricula = new GridPane();
        for (int f = 0; f < 9; f++) {
            for (int c = 0; c < 9; c++) {
                TextField t = new TextField("");
                t.setPrefSize(50, 50);
                t.setAlignment(Pos.CENTER);

                final int ff = f;
                final int cc = c;
                t.textProperty().addListener((obs, viejo, nuevo) -> escribio(ff, cc, viejo, nuevo));

                // Al entrar a la casilla se selecciona su contenido, así lo que escribas lo reemplaza
                t.focusedProperty().addListener((obs, antes, ahora) -> {
                    if (ahora) Platform.runLater(() -> t.selectAll());
                });

                campos[f][c] = t;
                cuadricula.add(t, c, f);
            }
        }

        mensaje.setStyle("-fx-text-fill: " + COLOR_TEXTO_MENSAJE + "; -fx-font-size: 14px;"
                + "-fx-background-color: rgba(0, 0, 0, 0.45); -fx-padding: 4 10 4 10;");

        VBox contenido = new VBox(10, niveles, cuadricula, mensaje);
        contenido.setAlignment(Pos.CENTER);
        contenido.setPadding(new Insets(20));

        StackPane raiz = new StackPane(crearFondo(), contenido);
        raiz.setStyle("-fx-background-color: " + COLOR_FONDO_VENTANA + ";");

        ventana.setTitle("Sudoku");
        ventana.setScene(new Scene(raiz));
        ventana.show();

        cargarPlantilla(facil, "Fácil");
        iniciarMusica();
    }

    Region crearFondo() {
        Region fondo = new Region();
        URL url = getClass().getResource(NOMBRE_IMAGEN);

        if (url != null) {
            Image imagen = new Image(url.toExternalForm());
            BackgroundSize tamano = new BackgroundSize(1.0, 1.0, true, true, false, true);
            fondo.setBackground(new Background(new BackgroundImage(
                    imagen, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER, tamano)));
            fondo.setOpacity(OPACIDAD_IMAGEN);
        } else {
            fondo.setStyle("-fx-background-color: linear-gradient(to bottom right, #1e3c8c, #783caa, #f07896);");
        }
        return fondo;
    }

    void iniciarMusica() {
        try {
            URL url = getClass().getResource(NOMBRE_MUSICA);
            if (url == null) {
                falloMusica("No encuentro " + NOMBRE_MUSICA + " en src/main/resources/com/example/demo/");
                return;
            }

            Media media = new Media(url.toExternalForm());
            media.setOnError(() -> falloMusica("Error con el archivo: " + media.getError()));

            reproductor = new MediaPlayer(media);
            reproductor.setOnError(() -> falloMusica("Error al reproducir: " + reproductor.getError()));
            reproductor.setCycleCount(MediaPlayer.INDEFINITE);
            reproductor.setVolume(VOLUMEN_MUSICA);
            reproductor.play();
        } catch (Exception e) {
            falloMusica("Error: " + e);
        }
    }

    void falloMusica(String texto) {
        System.out.println("MÚSICA NO FUNCIONA -> " + texto);
        mensaje.setText(texto);
        botonMusica.setText("🔇 Sin música");
        botonMusica.setDisable(true);
    }

    void alternarMusica() {
        if (reproductor == null) return;

        if (reproductor.getStatus() == MediaPlayer.Status.PLAYING) {
            reproductor.pause();
            botonMusica.setText("🔇 Música");
        } else {
            reproductor.play();
            botonMusica.setText("🔊 Música");
        }
    }

    void cargarPlantilla(int[][] plantilla, String nombre) {
        int descubiertos = 0;
        for (int f = 0; f < 9; f++) {
            for (int c = 0; c < 9; c++) {
                tablero[f][c] = plantilla[f][c];
                fija[f][c] = plantilla[f][c] != 0;
                campos[f][c].setEditable(!fija[f][c]);
                if (fija[f][c]) descubiertos++;
            }
        }
        nivelActual = nombre;
        mensaje.setText("Nivel " + nombre + " (" + descubiertos + " números descubiertos)");
        pintar();
    }

    void escribio(int fila, int col, String viejo, String nuevo) {
        if (pintando) return;
        if (fija[fila][col]) return;

        // Solo se acepta un número del 1 al 9 (o vacío)
        if (!nuevo.matches("[1-9]?")) {
            pintando = true;
            campos[fila][col].setText(viejo);
            pintando = false;
            return;
        }

        tablero[fila][col] = nuevo.isEmpty() ? 0 : Integer.parseInt(nuevo);
        pintar();

        if (gano()) {
            mensaje.setText("¡Ganaste el nivel " + nivelActual + "! 🎉");
        } else {
            mensaje.setText("Nivel " + nivelActual + " - Las casillas en rojo tienen un número repetido");
        }
    }

    // Revisa que n no esté repetido en la fila, la columna ni el cuadro de 3x3
    boolean esValido(int fila, int col, int n) {
        for (int i = 0; i < 9; i++) {
            if (tablero[fila][i] == n) return false;
            if (tablero[i][col] == n) return false;
        }
        int inicioFila = fila / 3 * 3;
        int inicioCol = col / 3 * 3;
        for (int f = inicioFila; f < inicioFila + 3; f++) {
            for (int c = inicioCol; c < inicioCol + 3; c++) {
                if (tablero[f][c] == n) return false;
            }
        }
        return true;
    }

    // Quita el número un momento para compararlo solo con los demás
    boolean hayChoque(int fila, int col) {
        int n = tablero[fila][col];
        if (n == 0) return false;

        tablero[fila][col] = 0;
        boolean ok = esValido(fila, col, n);
        tablero[fila][col] = n;
        return !ok;
    }

    boolean gano() {
        for (int f = 0; f < 9; f++) {
            for (int c = 0; c < 9; c++) {
                if (tablero[f][c] == 0) return false;
                if (hayChoque(f, c)) return false;
            }
        }
        return true;
    }

    void pintar() {
        pintando = true;

        for (int f = 0; f < 9; f++) {
            for (int c = 0; c < 9; c++) {
                TextField t = campos[f][c];

                String texto = tablero[f][c] == 0 ? "" : "" + tablero[f][c];
                if (!t.getText().equals(texto)) {
                    t.setText(texto);
                }

                String fondo = (f / 3 + c / 3) % 2 == 0 ? COLOR_CUADRO_CLARO : COLOR_CUADRO_OSCURO;
                String color = fija[f][c] ? COLOR_NUMERO_FIJO : COLOR_NUMERO_USUARIO;

                if (hayChoque(f, c)) {
                    fondo = COLOR_ERROR_FONDO;
                    color = COLOR_ERROR_NUMERO;
                }

                t.setStyle("-fx-background-color: " + fondo + ";"
                        + "-fx-text-fill: " + color + ";"
                        + "-fx-font-size: 20px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-border-color: " + COLOR_BORDE + ";"
                        + "-fx-border-width: 0.5;"
                        + "-fx-background-radius: 0;"
                        + "-fx-border-radius: 0;");
            }
        }

        pintando = false;
    }

    public static void main(String[] args) {
        launch(args);
    }
}