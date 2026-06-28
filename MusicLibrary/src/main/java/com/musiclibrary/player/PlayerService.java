package com.musiclibrary.player;

import com.musiclibrary.model.Playlist;
import com.musiclibrary.model.Track;

/**
 * Servizio di riproduzione: orchestra lo stato del player ({@link PlaybackState})
 * e delega la riproduzione audio vera e propria a un {@link AudioPlayer}. La
 * logica di stato è qui ed è testabile in isolamento, mentre l'audio reale è
 * incapsulato dietro l'interfaccia AudioPlayer.
 */
public class PlayerService {

    private final AudioPlayer audioPlayer;
    private final PlaybackState state;
    private Runnable onPlaybackChanged;

    /**
     * Crea il servizio iniettando il player audio da usare (reale o doppio di test).
     * @param audioPlayer il player audio su cui delegare la riproduzione
     */
    public PlayerService(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.state = new PlaybackState();
        this.onPlaybackChanged = () -> { };
        this.audioPlayer.setOnEndOfMedia(this::onTrackEnded);
    }

    /**
     * Avvia la riproduzione di un singolo brano. Se il brano non ha un file
     * audio associato, non avvia nulla e lo stato resta invariato.
     * @param track il brano da riprodurre
     */
    public void play(Track track) {
        if (track.getFilePath() == null || track.getFilePath().isEmpty()) {
            return;
        }
        state.startTrack(track);
        audioPlayer.play(track.getFilePath());
        onPlaybackChanged.run();
    }

    /**
     * Avvia la riproduzione di un'intera playlist dal primo brano. Se la playlist
     * è vuota non avvia nulla e lo stato resta invariato.
     * @param playlist la playlist da riprodurre
     */
    public void playPlaylist(Playlist playlist) {
        if (playlist.getTracks().isEmpty()) {
            return;
        }
        state.startPlaylist(playlist);
        audioPlayer.play(state.getCurrentTrack().getFilePath());
        onPlaybackChanged.run();
    }

    /**
     * Adatta la riproduzione dopo che un brano è stato rimosso da una playlist.
     * Se la playlist non è quella in riproduzione non fa nulla. Altrimenti
     * aggiorna lo stato: se il brano rimosso era quello in riproduzione avvia il
     * successivo, o ferma l'audio se la playlist è terminata; se invece era un
     * altro brano la riproduzione prosegue senza interruzioni.
     * @param playlist la playlist da cui è stato rimosso un brano
     */
    public void handleTrackRemoved(Playlist playlist) {
        if (state.getCurrentPlaylist() != playlist) {
            return; // non è la playlist in riproduzione: niente da adattare
        }
        Track before = state.getCurrentTrack();
        state.handleTrackRemoved();
        Track after = state.getCurrentTrack();
        if (after == null) {
            audioPlayer.stop(); // è stato rimosso l'ultimo brano in riproduzione
        } else if (after != before) {
            audioPlayer.play(after.getFilePath()); // rimosso il brano corrente: parte il successivo
        }
        onPlaybackChanged.run();
    }

    /**
     * Registra un'azione eseguita a ogni cambio di stato della riproduzione
     * (avvio, stop, fine del brano): usata dalla UI per aggiornarsi.
     * @param callback l'azione di aggiornamento; {@code null} per nessuna azione
     */
    public void setOnPlaybackChanged(Runnable callback) {
        this.onPlaybackChanged = (callback != null) ? callback : () -> { };
    }

    private void onTrackEnded() {
        // In una playlist, a fine brano si passa al successivo; se non ce ne sono
        // (o è un brano singolo) la riproduzione si ferma.
        if (state.getCurrentPlaylist() != null && state.advanceToNext()) {
            audioPlayer.play(state.getCurrentTrack().getFilePath());
        } else {
            state.stop();
        }
        onPlaybackChanged.run();
    }

    /** @return lo stato corrente della riproduzione (in sola lettura per la UI) */
    public PlaybackState getPlaybackState() {
        return state;
    }
}
