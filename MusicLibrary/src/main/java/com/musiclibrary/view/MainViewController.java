package com.musiclibrary.view;

import com.musiclibrary.controller.MusicLibraryFacade;
import com.musiclibrary.model.Playlist;
import com.musiclibrary.model.Track;
import com.musiclibrary.player.PlaybackState;
import com.musiclibrary.player.PlaybackStatus;
import com.musiclibrary.player.PlayerService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
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
 * swap in-window tra i pannelli Song List, Playlists e Player, la tabella dei
 * brani con le azioni di riga (play, modifica, aggiunta a playlist, eliminazione)
 * e la schermata di riproduzione.
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
    @FXML private Button btnPlayer;
    @FXML private Button btnAddTrack;
    @FXML private VBox   songListPane;
    @FXML private javafx.scene.Node   playlistsPane;          // root incluso da PlaylistsView.fxml
    @FXML private PlaylistsController playlistsPaneController; // controller incluso (fx:include)

    // ── Pannello Player ───────────────────────────────────────────────────
    @FXML private VBox  playerPane;
    @FXML private Label labelNowPlaying;
    @FXML private Label labelPlaybackStatus;

    private MusicLibraryFacade facade;
    private PlayerService playerService;

    /**
     * Inietta la Facade, configura colonne e celle, e mostra la vista iniziale.
     * @param facade la Facade del dominio
     */
    public void setFacade(MusicLibraryFacade facade) {
        this.facade = facade;

        // Configura le colonne
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("length"));

        // Colonna Actions
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnPlay      = new Button("▶");
            private final Button btnEdit      = new Button("✎");
            private final Button btnAddToPlay = new Button("+");
            private final Button btnDelete    = new Button("✕");
            private final HBox   box = new HBox(4, btnPlay, btnEdit, btnAddToPlay, btnDelete);
            {
                btnPlay.setStyle("-fx-background-color: transparent; -fx-text-fill: #2C8A3A; -fx-font-size: 14px;");
                btnEdit.setStyle("-fx-background-color: transparent; -fx-font-size: 14px;");
                btnAddToPlay.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-font-weight: bold;");
                btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #cc0000; -fx-font-size: 14px;");

                btnPlay.setOnAction(e -> {
                    Track t = getTableRow().getItem();
                    if (t != null) onPlayTrack(t);
                });
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

    /**
     * Inietta il servizio di riproduzione e registra l'aggiornamento della
     * schermata Player a ogni cambio di stato della riproduzione.
     * @param playerService il servizio di riproduzione
     */
    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
        this.playerService.setOnPlaybackChanged(this::refreshPlayer);
        playlistsPaneController.setOnPlayPlaylist(this::onPlayPlaylistRequested);
        playlistsPaneController.setOnRemoveTrackFromPlaylist(this::onRemoveTrackFromPlaylistRequested);
    }

    /** Ricarica la tabella dei brani dalla Facade. */
    public void refreshTable() {
        trackTable.setItems(
                FXCollections.observableArrayList(facade.getAllTracks()));
        trackTable.refresh(); // forza JavaFX a ridisegnare tutte le celle
    }

    // ── Navigazione in-window ─────────────────────────────────────────────
    @FXML private void onNavSongList()  { showSongList(); }
    @FXML private void onNavPlaylists() { showPlaylists(); }
    @FXML private void onNavPlayer()    { showPlayer(); }
    @FXML private void onNavHome()      { System.out.println("→ Home"); } // stub: sprint futuri

    private void showSongList() {
        songListPane.setVisible(true);   songListPane.setManaged(true);
        playlistsPane.setVisible(false); playlistsPane.setManaged(false);
        playerPane.setVisible(false);    playerPane.setManaged(false);
        btnAddTrack.setVisible(true);    btnAddTrack.setManaged(true);
        setActiveTab(btnSongList);
        refreshTable();
    }

    private void showPlaylists() {
        songListPane.setVisible(false);  songListPane.setManaged(false);
        playlistsPane.setVisible(true);  playlistsPane.setManaged(true);
        playerPane.setVisible(false);    playerPane.setManaged(false);
        btnAddTrack.setVisible(false);   btnAddTrack.setManaged(false);
        setActiveTab(btnPlaylists);
        playlistsPaneController.loadPlaylists();
    }

    private void showPlayer() {
        songListPane.setVisible(false);  songListPane.setManaged(false);
        playlistsPane.setVisible(false); playlistsPane.setManaged(false);
        playerPane.setVisible(true);     playerPane.setManaged(true);
        btnAddTrack.setVisible(false);   btnAddTrack.setManaged(false);
        setActiveTab(btnPlayer);
        refreshPlayer();
    }

    private void setActiveTab(Button active) {
        btnSongList.setStyle(active == btnSongList ? "-fx-background-color: #d0d0d0;" : "");
        btnPlaylists.setStyle(active == btnPlaylists ? "-fx-background-color: #d0d0d0;" : "");
        btnPlayer.setStyle(active == btnPlayer ? "-fx-background-color: #d0d0d0;" : "");
    }

    // ── Riproduzione ───────────────────────────────────────────────────

    private void onPlayTrack(Track track) {
        if (track.getFilePath() == null || track.getFilePath().isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION,
                    "Questo brano non ha un file audio associato. "
                            + "Modificalo per aggiungerne uno.",
                    ButtonType.OK).showAndWait();
            return;
        }
        playerService.play(track);
        showPlayer();
    }

    private void onPlayPlaylistRequested(Playlist playlist) {
        if (playlist.getTracks().isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION,
                    "La playlist è vuota. Aggiungi dei brani per iniziare.",
                    ButtonType.OK).showAndWait();
            return;
        }
        playerService.playPlaylist(playlist);
        showPlayer();
    }

    private void onRemoveTrackFromPlaylistRequested(Playlist playlist, Track track) {
        facade.removeTrackFromPlaylist(playlist, track); // toglie dalla playlist, resta in libreria
        playerService.handleTrackRemoved(playlist);      // adatta la riproduzione in corso
    }

    private void refreshPlayer() {
        PlaybackState state = playerService.getPlaybackState();
        Track current = state.getCurrentTrack();
        if (current != null && state.getStatus() == PlaybackStatus.PLAYING) {
            labelNowPlaying.setText("♪  " + current.getTitle() + " — " + current.getAuthor());
            labelPlaybackStatus.setText("In riproduzione");
        } else {
            labelNowPlaying.setText("Nessun brano in riproduzione");
            labelPlaybackStatus.setText("Fermo");
        }
    }

    /**
     * ADD / EDIT / DELETE
     */
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
