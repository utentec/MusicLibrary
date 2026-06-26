package com.musiclibrary;

import com.musiclibrary.controller.MusicLibraryFacade;
import com.musiclibrary.player.JavaFxAudioPlayer;
import com.musiclibrary.player.PlayerService;
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
 * repository su file (persistenza JSON) e il servizio di riproduzione, carica
 * la finestra principale e la mostra.
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

        // Servizio di riproduzione: usa il player audio reale basato su JavaFX
        PlayerService playerService = new PlayerService(new JavaFxAudioPlayer());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
        Scene scene = new Scene(loader.load(), 1000, 650);

        MainViewController controller = loader.getController();
        controller.setFacade(facade);
        controller.setPlayerService(playerService);

        stage.setTitle("Music Library");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
