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
        playCurrentOrAdvanceToPlayable(); // salta eventuali brani senza file audio
        onPlaybackChanged.run();
    }

    /**
     * Alterna pausa e ripresa della riproduzione corrente: se un brano sta
     * suonando lo mette in pausa, se è in pausa lo riprende dal punto in cui era.
     * Non ha effetto se la riproduzione è ferma.
     */
    public void togglePause() {
        PlaybackStatus status = state.getStatus();
        if (status == PlaybackStatus.PLAYING) {
            state.pause();
            audioPlayer.pause();
        } else if (status == PlaybackStatus.PAUSED) {
            state.resume();
            audioPlayer.resume();
        } else {
            return; // riproduzione ferma: niente da mettere in pausa o riprendere
        }
        onPlaybackChanged.run();
    }

    /**
     * Passa al brano successivo della playlist in riproduzione e lo avvia. Se il
     * brano corrente era l'ultimo, la riproduzione si ferma (modalità sequenziale).
     * Non ha effetto quando si riproduce un brano singolo.
     */
    public void skipNext() {
        if (state.getCurrentPlaylist() == null) {
            return; // brano singolo: nessuna playlist su cui spostarsi
        }
        if (state.advanceToNext()) {
            audioPlayer.play(state.getCurrentTrack().getFilePath()); // Scenario 1
        } else {
            state.stop();      // Scenario 2: era l'ultimo brano
            audioPlayer.stop();
        }
        onPlaybackChanged.run();
    }

    /**
     * Torna al brano precedente della playlist in riproduzione e lo avvia. Se il
     * brano corrente era il primo, riavvia lo stesso brano dall'inizio. Non ha
     * effetto quando si riproduce un brano singolo.
     */
    public void skipPrevious() {
        if (state.getCurrentPlaylist() == null) {
            return; // brano singolo
        }
        state.advanceToPrevious(); // va al precedente se c'è; se è il primo resta lì
        audioPlayer.play(state.getCurrentTrack().getFilePath()); // Scenario 3 o Scenario 4 (riavvio)
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

    /**
     * Riproduce il brano corrente della playlist; se non ha un file audio associato
     * prosegue in avanti fino al primo brano riproducibile, oppure ferma la
     * riproduzione se non ne restano. Evita così di passare un percorso vuoto
     * all'AudioPlayer (che altrimenti tenterebbe di creare un Media non valido).
     */
    private void playCurrentOrAdvanceToPlayable() {
        while (!isPlayable(state.getCurrentTrack())) {
            if (!state.advanceToNext()) {
                state.stop();
                audioPlayer.stop();
                return;
            }
        }
        audioPlayer.play(state.getCurrentTrack().getFilePath());
    }

    /**
     * Indica se un brano è riproducibile, cioè ha un file audio associato.
     * @param track il brano da controllare (può essere {@code null})
     * @return {@code true} se il brano ha un percorso di file non vuoto
     */
    private boolean isPlayable(Track track) {
        return track != null && track.getFilePath() != null && !track.getFilePath().isEmpty();
    }

    /** @return lo stato corrente della riproduzione (in sola lettura per la UI) */
    public PlaybackState getPlaybackState() {
        return state;
    }
}
