package com.musiclibrary.view;

import com.musiclibrary.controller.MusicLibraryFacade;
import com.musiclibrary.model.Track;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller della finestra principale. Gestisce la barra di navigazione, lo
 * swap in-window tra il pannello Song List e il pannello Playlists, e la tabella
 * dei brani con le azioni di riga (modifica, aggiunta a playlist, eliminazione).
 */
public class MainViewController {

    @FXML private TableView<Track>            trackTable;
    @FXML private TableColumn<Track, String>  colTitle;
    @FXML private TableColumn<Track, String>  colAuthor;
    @FXML private TableColumn<Track, String>  colGenre;
    @FXML private TableColumn<Track, Integer> colYear;
    @FXML private TableColumn<Track, Integer> colDuration;
    @FXML private TableColumn<Track, Void>    colActions;

    // ── Navigazione in-window ─────────────────────────────────────────────
    @FXML private Button btnSongList;
    @FXML private Button btnPlaylists;
    @FXML private Button btnAddTrack;
    @FXML private VBox   songListPane;
    @FXML private javafx.scene.Node   playlistsPane;          // root incluso da PlaylistsView.fxml
    @FXML private PlaylistsController playlistsPaneController; // controller incluso (fx:include)

    private MusicLibraryFacade facade;

    /**
     * Inietta la Facade, configura colonne e celle, e mostra la vista iniziale.
     * @param facade la Facade del dominio
     */
    public void setFacade(MusicLibraryFacade facade) {
        this.facade = facade;

        // Configura le colonne UNA SOLA VOLTA
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("length"));

        // Colonna Actions
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit      = new Button("✎");
            private final Button btnAddToPlay = new Button("+");
            private final Button btnDelete    = new Button("✕");
            private final HBox   box = new HBox(4, btnEdit, btnAddToPlay, btnDelete);
            {
                btnEdit.setStyle("-fx-background-color: transparent; -fx-font-size: 14px;");
                btnAddToPlay.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-font-weight: bold;");
                btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #cc0000; -fx-font-size: 14px;");

                btnEdit.setOnAction(e -> {
                    Track t = getTableRow().getItem();
                    if (t != null) onEditTrack(t);
                });
                btnAddToPlay.setOnAction(e -> {
                    Track t = getTableRow().getItem();
                    if (t != null) onAddToPlaylist(t);
                });
                btnDelete.setOnAction(e -> {
                    Track t = getTableRow().getItem();
                    if (t != null) onDeleteTrack(t);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        // Collega la Facade alla schermata Playlists inclusa (fx:include)
        playlistsPaneController.setFacade(facade);

        // Vista iniziale: Song List
        showSongList();
    }

    /** Ricarica la tabella dei brani dalla Facade. */
    public void refreshTable() {
        trackTable.setItems(
                FXCollections.observableArrayList(facade.getAllTracks()));
        trackTable.refresh(); // forza JavaFX a ridisegnare tutte le celle
    }

    /** Mantenuto per compatibilità con AddPlaylistController; non più usato dal flusso principale. */
    public void refreshPlaylists() {
        System.out.println("Playlists aggiornate: "
                + facade.getAllPlaylists().size());
    }

    // ── Navigazione in-window ─────────────────────────────────────────────
    @FXML private void onNavSongList()  { showSongList(); }
    @FXML private void onNavPlaylists() { showPlaylists(); }
    @FXML private void onNavPlayer()    { System.out.println("→ Player"); }   // stub: sprint futuri
    @FXML private void onNavHome()      { System.out.println("→ Home"); }     // stub: sprint futuri

    private void showSongList() {
        songListPane.setVisible(true);   songListPane.setManaged(true);
        playlistsPane.setVisible(false); playlistsPane.setManaged(false);
        btnAddTrack.setVisible(true);    btnAddTrack.setManaged(true);
        setActiveTab(btnSongList);
        refreshTable();
    }

    private void showPlaylists() {
        songListPane.setVisible(false);  songListPane.setManaged(false);
        playlistsPane.setVisible(true);  playlistsPane.setManaged(true);
        btnAddTrack.setVisible(false);   btnAddTrack.setManaged(false);
        setActiveTab(btnPlaylists);
        playlistsPaneController.loadPlaylists();
    }

    private void setActiveTab(Button active) {
        btnSongList.setStyle(active == btnSongList ? "-fx-background-color: #d0d0d0;" : "");
        btnPlaylists.setStyle(active == btnPlaylists ? "-fx-background-color: #d0d0d0;" : "");
    }

    // ── Add / Edit / Delete (US-H1.1, H1.2, H1.3) ───────────────────────
    @FXML
    private void onAddTrack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/AddTrackView.fxml"));
            javafx.scene.Parent root = loader.load();

            AddTrackController controller = loader.getController();
            controller.setFacade(facade);
            controller.setMainController(this);

            Stage dialog = new Stage();
            dialog.setTitle("Nuovo Brano");
            dialog.setScene(new javafx.scene.Scene(root));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onEditTrack(Track track) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/EditTrackView.fxml"));
            javafx.scene.Parent root = loader.load();

            EditTrackController controller = loader.getController();
            controller.setFacade(facade);
            controller.setMainController(this);
            controller.setTrack(track);

            Stage dialog = new Stage();
            dialog.setTitle("Modifica Brano");
            dialog.setScene(new javafx.scene.Scene(root));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();

            refreshTable();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onDeleteTrack(Track track) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Eliminare \"" + track.getTitle() + "\"?",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Elimina brano");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.YES) {
                facade.deleteTrack(track.getId());
                refreshTable();
            }
        });
    }

    private void onAddToPlaylist(Track track) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/AddToPlaylistView.fxml"));
            javafx.scene.Parent root = loader.load();

            AddToPlaylistController controller = loader.getController();
            controller.setFacade(facade);
            controller.setTrack(track);
            controller.loadPlaylists();

            Stage dialog = new Stage();
            dialog.setTitle("Aggiungi a Playlist");
            dialog.setScene(new javafx.scene.Scene(root));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
