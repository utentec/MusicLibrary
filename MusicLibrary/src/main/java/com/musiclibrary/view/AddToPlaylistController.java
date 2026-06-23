package com.musiclibrary.view;

import com.musiclibrary.controller.MusicLibraryFacade;
import com.musiclibrary.model.Playlist;
import com.musiclibrary.model.Track;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller del dialog "Aggiungi a Playlist": consente di scegliere una
 * playlist di destinazione per un brano e delega l'aggiunta alla Facade.
 */
public class AddToPlaylistController {

    @FXML private Label             labelTrack;
    @FXML private ComboBox<String>  comboPlaylist;
    @FXML private Label             labelError;

    private MusicLibraryFacade facade;
    private Track track;
    private List<Playlist> playlists;

    /**
     * Inietta la Facade del dominio.
     * @param facade la Facade
     */
    public void setFacade(MusicLibraryFacade facade) {
        this.facade = facade;
    }

    /**
     * Imposta il brano da aggiungere e ne mostra titolo e autore.
     * @param track il brano da aggiungere a una playlist
     */
    public void setTrack(Track track) {
        this.track = track;
        labelTrack.setText("Brano: " + track.getTitle()
                + " — " + track.getAuthor());
    }

    /** Carica nel menu a tendina i nomi delle playlist disponibili. */
    public void loadPlaylists() {
        playlists = facade.getAllPlaylists();
        if (playlists.isEmpty()) {
            labelError.setText(
                    "Nessuna playlist disponibile. Crea prima una playlist.");
            return;
        }
        comboPlaylist.setItems(FXCollections.observableArrayList(
                playlists.stream()
                        .map(Playlist::getName)
                        .toList()
        ));
        comboPlaylist.getSelectionModel().selectFirst();
    }

    @FXML
    private void onAdd() {
        labelError.setText("");
        if (playlists == null || playlists.isEmpty()) {
            labelError.setText("Crea prima una playlist.");
            return;
        }
        int index = comboPlaylist.getSelectionModel().getSelectedIndex();
        if (index < 0) {
            labelError.setText("Seleziona una playlist.");
            return;
        }
        Playlist selected = playlists.get(index);
        facade.addTrackToPlaylist(selected, track);
        closeDialog();
    }

    @FXML
    private void onCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) comboPlaylist.getScene().getWindow();
        stage.close();
    }
}
