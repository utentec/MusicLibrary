package com.musiclibrary.player;

import com.musiclibrary.model.Playlist;
import com.musiclibrary.model.Track;

/**
 * Stato corrente del player: brano in riproduzione, stato
 * (STOPPED/PLAYING/PAUSED) e, quando si riproduce un'intera playlist, il
 * riferimento alla playlist e l'indice del brano corrente al suo interno.
 */
public class PlaybackState {

    /** Valore dell'indice quando non si sta riproducendo una playlist. */
    private static final int NO_INDEX = -1;

    private Track currentTrack;
    private PlaybackStatus status;
    private Playlist currentPlaylist;
    private int currentIndex;

    /**
     * Crea lo stato iniziale: nessun brano, riproduzione ferma.
     */
    public PlaybackState() {
        this.currentTrack = null;
        this.status = PlaybackStatus.STOPPED;
        this.currentPlaylist = null;
        this.currentIndex = NO_INDEX;
    }

    /**
     * Avvia la riproduzione di un singolo brano, fuori da una playlist.
     * @param track il brano da riprodurre
     */
    public void startTrack(Track track) {
        this.currentTrack = track;
        this.status = PlaybackStatus.PLAYING;
        this.currentPlaylist = null;
        this.currentIndex = NO_INDEX;
    }

    /**
     * Avvia la riproduzione di una playlist a partire dal suo primo brano.
     * La playlist deve contenere almeno un brano.
     * @param playlist la playlist da riprodurre
     */
    public void startPlaylist(Playlist playlist) {
        this.currentPlaylist = playlist;
        this.currentIndex = 0;
        this.currentTrack = playlist.getTracks().get(0);
        this.status = PlaybackStatus.PLAYING;
    }

    /**
     * Avanza al brano successivo della playlist corrente, se esiste.
     * @return {@code true} se è avanzato a un nuovo brano, {@code false} se non
     *         c'erano altri brani o non si sta riproducendo una playlist
     */
    public boolean advanceToNext() {
        if (currentPlaylist == null) {
            return false;
        }
        int nextIndex = currentIndex + 1;
        if (nextIndex < currentPlaylist.getTracks().size()) {
            this.currentIndex = nextIndex;
            this.currentTrack = currentPlaylist.getTracks().get(nextIndex);
            return true;
        }
        return false;
    }

    /**
     * Ri-sincronizza lo stato dopo che un brano è stato rimosso dalla playlist
     * in riproduzione. Se il brano corrente è ancora presente ne aggiorna solo
     * l'indice (la rimozione di un brano precedente lo fa scalare); se il brano
     * corrente era proprio quello rimosso, fa diventare corrente il successivo
     * (che scivola nella stessa posizione) oppure ferma la riproduzione se la
     * playlist non ha più brani da quella posizione in poi.
     */
    public void handleTrackRemoved() {
        if (currentPlaylist == null) {
            return;
        }
        int newIndex = currentPlaylist.getTracks().indexOf(currentTrack);
        if (newIndex >= 0) {
            this.currentIndex = newIndex; // il brano corrente è ancora in lista
            return;
        }
        if (currentIndex < currentPlaylist.getTracks().size()) {
            this.currentTrack = currentPlaylist.getTracks().get(currentIndex); // il successivo scala qui
        } else {
            stop(); // è stato rimosso l'ultimo brano in riproduzione
        }
    }

    /**
     * Ferma la riproduzione e riporta lo stato a quello iniziale.
     */
    public void stop() {
        this.currentTrack = null;
        this.status = PlaybackStatus.STOPPED;
        this.currentPlaylist = null;
        this.currentIndex = NO_INDEX;
    }

    // ── Getter ────────────────────────────────────────────
    /** @return il brano in riproduzione, oppure {@code null} se la riproduzione è ferma */
    public Track getCurrentTrack() {
        return currentTrack;
    }

    /** @return lo stato corrente della riproduzione */
    public PlaybackStatus getStatus() {
        return status;
    }

    /** @return la playlist in riproduzione, oppure {@code null} se si riproduce un singolo brano */
    public Playlist getCurrentPlaylist() {
        return currentPlaylist;
    }

    /** @return l'indice del brano corrente nella playlist, oppure -1 se non applicabile */
    public int getCurrentIndex() {
        return currentIndex;
    }
}
