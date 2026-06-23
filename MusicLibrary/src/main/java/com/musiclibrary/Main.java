package com.musiclibrary;

import com.musiclibrary.controller.MusicLibraryFacade;
import com.musiclibrary.repository.InMemoryPlaylistRepository;
import com.musiclibrary.repository.InMemoryTrackRepository;
import com.musiclibrary.view.MainViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto di ingresso dell'applicazione JavaFX: costruisce la Facade con i
 * repository in memoria, carica la finestra principale e la mostra.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Setup della Facade con entrambi i repository iniettati dal costruttore
        MusicLibraryFacade facade = new MusicLibraryFacade(
                new InMemoryTrackRepository(),
                new InMemoryPlaylistRepository());

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/MainView.fxml"));
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
