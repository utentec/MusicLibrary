package com.musiclibrary.controller;

import com.musiclibrary.controller.MusicLibraryFacade;
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
 * Controller del dialog "Nuovo Brano": raccoglie i dati dal form (incluso il
 * file audio), delega la creazione (con validazione) alla Facade e mostra
 * eventuali errori.
 */
public class AddTrackController {

    @FXML private TextField fieldTitle;
    @FXML private TextField fieldAuthor;
    @FXML private TextField fieldYear;
    @FXML private TextField fieldLength;
    @FXML private ComboBox<String> comboGenre;
    @FXML private Label labelFile;
    @FXML private Label labelError;

    private MusicLibraryFacade facade;
    private MainViewController mainController;
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

    /** Inizializza il menu dei generi con un valore predefinito. */
    @FXML
    public void initialize() {
        comboGenre.setItems(FXCollections.observableArrayList(
                "Rock", "Pop", "Jazz", "Soul", "Hip-Hop",
                "Classica", "Elettronica", "R&B", "Altro"
        ));
        comboGenre.getSelectionModel().select("Pop");
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

        // Validazione di dominio (range anno, durata > 0, lunghezza titolo) + creazione
        try {
            facade.createTrack(title, author, year, length, genre, selectedFilePath);
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
