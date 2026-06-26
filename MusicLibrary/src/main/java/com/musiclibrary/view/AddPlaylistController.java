package com.musiclibrary.view;

import com.musiclibrary.controller.MusicLibraryFacade;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller del dialog "Nuova Playlist": delega la creazione (con validazione
 * del nome univoco e non vuoto) alla Facade e mostra eventuali errori.
 */
public class AddPlaylistController {

    @FXML private TextField fieldName;
    @FXML private Label     labelError;

    private MusicLibraryFacade facade;

    /**
     * Inietta la Facade del dominio.
     * @param facade la Facade
     */
    public void setFacade(MusicLibraryFacade facade) {
        this.facade = facade;
    }

    @FXML
    private void onSave() {
        labelError.setText("");
        String name = fieldName.getText().trim();
        try {
            facade.createPlaylist(name);
            closeDialog();
        } catch (IllegalArgumentException e) {
            labelError.setText(e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) fieldName.getScene().getWindow();
        stage.close();
    }
}
