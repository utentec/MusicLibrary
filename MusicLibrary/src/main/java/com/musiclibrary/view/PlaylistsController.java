package com.musiclibrary.view;

import com.musiclibrary.controller.MusicLibraryFacade;
import com.musiclibrary.model.Playlist;
import com.musiclibrary.model.Track;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller della schermata "Playlists" (master-detail), inclusa in MainView
 * tramite fx:include. A sinistra l'elenco delle playlist, a destra i brani di
 * quella selezionata. Copre gli AC di US-H1.5 (la nuova playlist appare nella
 * lista) e US-H1.6 (i brani aggiunti appaiono nella playlist scelta).
 */
public class PlaylistsController {

    @FXML private ListView<Playlist> playlistListView;
    @FXML private ListView<Track>    trackListView;
    @FXML private Label              detailHeader;

    private MusicLibraryFacade facade;

    /**
     * Inietta la Facade del dominio.
     * @param facade la Facade
     */
    public void setFacade(MusicLibraryFacade facade) {
        this.facade = facade;
    }

    /** Imposta i placeholder e registra l'ascoltatore di selezione delle playlist. */
    @FXML
    public void initialize() {
        playlistListView.setPlaceholder(
                new Label("Nessuna playlist. Crea la tua prima playlist."));
        trackListView.setPlaceholder(
                new Label("Seleziona una playlist per vederne i brani."));

        playlistListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSel, newSel) -> showTracksOf(newSel));
    }

    /** Carica (o ricarica) l'elenco delle playlist, mantenendo la selezione corrente. */
    public void loadPlaylists() {
        Playlist selected = playlistListView.getSelectionModel().getSelectedItem();

        playlistListView.setItems(
                FXCollections.observableArrayList(facade.getAllPlaylists()));

        if (selected != null) {
            for (Playlist p : playlistListView.getItems()) {
                if (p.getId().equals(selected.getId())) {
                    playlistListView.getSelectionModel().select(p);
                    break;
                }
            }
        }
    }

    private void showTracksOf(Playlist playlist) {
        if (playlist == null) {
            detailHeader.setText("Nessuna playlist selezionata");
            trackListView.getItems().clear();
            trackListView.setPlaceholder(
                    new Label("Seleziona una playlist per vederne i brani."));
            return;
        }
        detailHeader.setText(playlist.getName());
        trackListView.setPlaceholder(
                new Label("La playlist è vuota. Aggiungi dei brani per iniziare."));
        trackListView.setItems(
                FXCollections.observableArrayList(playlist.getTracks()));
    }

    @FXML
    private void onAddPlaylist() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/AddPlaylistView.fxml"));
            javafx.scene.Parent root = loader.load();

            AddPlaylistController controller = loader.getController();
            controller.setFacade(facade);

            Stage dialog = new Stage();
            dialog.setTitle("Nuova Playlist");
            dialog.setScene(new javafx.scene.Scene(root));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();

            loadPlaylists(); // US-H1.5 Scenario 1: la nuova playlist appare subito
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
