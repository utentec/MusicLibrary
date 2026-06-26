package com.musiclibrary.controller;

import com.musiclibrary.controller.MusicLibraryFacade;
import com.musiclibrary.model.Track;
import com.musiclibrary.view.MainViewController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Controller del dialog "Modifica Brano": pre-compila il form con i dati del
 * brano selezionato (incluso il file audio) e delega l'aggiornamento (con
 * validazione) alla Facade.
 */
public class EditTrackController {

    @FXML private TextField fieldTitle;
    @FXML private TextField fieldAuthor;
    @FXML private TextField fieldYear;
    @FXML private TextField fieldLength;
    @FXML private ComboBox<String> comboGenre;
    @FXML private Label labelFile;
    @FXML private Label labelError;

    private MusicLibraryFacade facade;
    private MainViewController mainController;
    private Track track;
    private String selectedFilePath = "";

    /**
     * Inietta la Facade del dominio.
     * @param facade la Facade
     */
    public void setFacade(MusicLibraryFacade facade) {
        this.facade = facade;
    }

    /**
     * Imposta il controller principale, usato per aggiornare la tabella dopo il salvataggio.
     * @param mainController il controller della finestra principale
     */
    public void setMainController(MainViewController mainController) {
        this.mainController = mainController;
    }

    /**
     * Imposta il brano da modificare e pre-compila i campi del form con i suoi dati.
     * @param track il brano da modificare
     */
    public void setTrack(Track track) {
        this.track = track;
        fieldTitle.setText(track.getTitle());
        fieldAuthor.setText(track.getAuthor());
        fieldYear.setText(String.valueOf(track.getYear()));
        fieldLength.setText(String.valueOf(track.getLength()));
        comboGenre.setValue(track.getGenre());

        selectedFilePath = track.getFilePath();
        if (selectedFilePath != null && !selectedFilePath.isEmpty()) {
            labelFile.setText(new File(selectedFilePath).getName());
        } else {
            labelFile.setText("nessun file");
        }
    }

    /** Inizializza il menu dei generi. */
    @FXML
    public void initialize() {
        comboGenre.setItems(FXCollections.observableArrayList(
                "Rock", "Pop", "Jazz", "Soul", "Hip-Hop",
                "Classica", "Elettronica", "R&B", "Altro"
        ));
    }

    @FXML
    private void onChooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Scegli un file audio");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("File audio (MP3, WAV)", "*.mp3", "*.wav"));
        File file = chooser.showOpenDialog(fieldTitle.getScene().getWindow());
        if (file != null) {
            selectedFilePath = file.getAbsolutePath();
            labelFile.setText(file.getName());
        }
    }

    @FXML
    private void onSave() {
        labelError.setText("");

        String title      = fieldTitle.getText().trim();
        String author     = fieldAuthor.getText().trim();
        String yearText   = fieldYear.getText().trim();
        String lengthText = fieldLength.getText().trim();
        String genre      = comboGenre.getValue();

        // Controlli "campo mancante" NELL'ORDINE voluto, prima di parsare i numeri
        if (title.isEmpty())      { labelError.setText("Il titolo non può essere vuoto."); return; }
        if (author.isEmpty())     { labelError.setText("L'autore non può essere vuoto."); return; }
        if (yearText.isEmpty())   { labelError.setText("L'anno non può essere vuoto."); return; }
        if (lengthText.isEmpty()) { labelError.setText("La durata non può essere vuota."); return; }

        // Solo ora i numeri: errore specifico per campo
        int year, length;
        try {
            year = Integer.parseInt(yearText);
        } catch (NumberFormatException e) {
            labelError.setText("L'anno deve essere un numero intero."); return;
        }
        try {
            length = Integer.parseInt(lengthText);
        } catch (NumberFormatException e) {
            labelError.setText("La durata deve essere un numero intero."); return;
        }

        // Validazione di dominio + aggiornamento (incluso il file audio)
        try {
            facade.updateTrack(track.getId(), title, author, year, length, genre, selectedFilePath);
            if (mainController != null) mainController.refreshTable();
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
        Stage stage = (Stage) fieldTitle.getScene().getWindow();
        stage.close();
    }
}
