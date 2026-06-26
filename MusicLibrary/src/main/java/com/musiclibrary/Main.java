package com.musiclibrary;

import com.musiclibrary.controller.MusicLibraryFacade;
import com.musiclibrary.repository.JsonPlaylistRepository;
import com.musiclibrary.repository.JsonTrackRepository;
import com.musiclibrary.repository.PlaylistRepository;
import com.musiclibrary.repository.TrackRepository;
import com.musiclibrary.view.MainViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;

/**
 * Punto di ingresso dell'applicazione JavaFX: costruisce la Facade con i
 * repository su file (persistenza JSON), carica la finestra principale e la mostra.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // I dati vengono salvati nella cartella "data" del progetto.
        Path dataDir = Path.of("data");

        // Ordine importante: prima i brani, poi le playlist (che risolvono gli ID dei brani)
        TrackRepository trackRepository =
                new JsonTrackRepository(dataDir.resolve("tracks.json"));
        PlaylistRepository playlistRepository =
                new JsonPlaylistRepository(dataDir.resolve("playlists.json"), trackRepository);

        MusicLibraryFacade facade =
                new MusicLibraryFacade(trackRepository, playlistRepository);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
        Scene scene = new Scene(loader.load(), 1000, 650);

        MainViewController controller = loader.getController();
        controller.setFacade(facade);

        stage.setTitle("Music Library");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
