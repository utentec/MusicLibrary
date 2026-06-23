package com.musiclibrary.command;

import com.musiclibrary.model.Playlist;
import com.musiclibrary.model.Track;

/**
 * Comando concreto che aggiunge un brano in fondo a una playlist.
 * L'annullamento rimuove il brano precedentemente aggiunto.
 */
public class AddTrackCommand implements Command {

    private final Playlist playlist;
    private final Track track;
    private int savedPosition;

    /**
     * @param playlist la playlist di destinazione
     * @param track    il brano da aggiungere
     */
    public AddTrackCommand(Playlist playlist, Track track) {
        this.playlist = playlist;
        this.track    = track;
    }

    /** Aggiunge il brano in fondo alla playlist e ne memorizza la posizione. */
    @Override
    public void execute() {
        playlist.addTrack(track);
        savedPosition = playlist.size() - 1;
    }

    /** Rimuove dalla playlist il brano aggiunto da {@link #execute()}. */
    @Override
    public void undo() {
        playlist.removeTrack(track);
    }
}
