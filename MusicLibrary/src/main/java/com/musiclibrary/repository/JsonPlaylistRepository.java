package com.musiclibrary.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.musiclibrary.model.Playlist;
import com.musiclibrary.model.Track;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementazione di {@link PlaylistRepository} che persiste le playlist su file
 * in formato JSON. Per non duplicare i brani e preservare la condivisione dei
 * riferimenti con la libreria, ogni playlist viene salvata con i soli ID dei
 * propri brani; al caricamento gli ID vengono risolti contro il
 * {@link TrackRepository}.
 */
public class JsonPlaylistRepository implements PlaylistRepository {

    /** Forma serializzabile di una playlist: nome + ID dei brani contenuti. */
    private static final class PlaylistData {
        String name;
        List<String> trackIds;
    }

    private static final Type LIST_TYPE = new TypeToken<List<PlaylistData>>() {}.getType();

    private final Path file;
    private final TrackRepository trackRepository;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, Playlist> playlists = new LinkedHashMap<>();

    /**
     * Costruisce il repository e carica le playlist dal file, risolvendo gli ID
     * dei brani contro il repository dei brani fornito.
     * @param file            percorso del file JSON delle playlist
     * @param trackRepository repository da cui risolvere i brani per ID
     */
    public JsonPlaylistRepository(Path file, TrackRepository trackRepository) {
        this.file = file;
        this.trackRepository = trackRepository;
        load();
    }

    @Override
    public void addPlaylist(Playlist playlist) {
        playlists.put(playlist.getId(), playlist);
        save();
    }

    @Override
    public void removePlaylist(String id) {
        playlists.remove(id);
        save();
    }

    @Override
    public void update(Playlist playlist) {
        playlists.put(playlist.getId(), playlist);
        save();
    }

    @Override
    public Optional<Playlist> findById(String id) {
        return Optional.ofNullable(playlists.get(id));
    }

    @Override
    public Optional<Playlist> findByName(String name) {
        return playlists.values().stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public List<Playlist> findAll() {
        return new ArrayList<>(playlists.values());
    }

    private void load() {
        if (!Files.exists(file)) {
            return; // primo avvio / file assente
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            List<PlaylistData> data = gson.fromJson(reader, LIST_TYPE);
            if (data == null) {
                return;
            }
            for (PlaylistData d : data) {
                Playlist playlist = new Playlist(d.name);
                if (d.trackIds != null) {
                    for (String trackId : d.trackIds) {
                        // Risoluzione per ID;
                        // gli ID non più esistenti vengono scartati senza errori
                        trackRepository.findById(trackId).ifPresent(playlist::addTrack);
                    }
                }
                playlists.put(playlist.getId(), playlist);
            }
        } catch (IOException e) {
            throw new RuntimeException("Impossibile leggere il file delle playlist: " + file, e);
        }
    }

    private void save() {
        List<PlaylistData> data = new ArrayList<>();
        for (Playlist playlist : playlists.values()) {
            PlaylistData d = new PlaylistData();
            d.name = playlist.getName();
            d.trackIds = new ArrayList<>();
            for (Track track : playlist.getTracks()) {
                d.trackIds.add(track.getId());
            }
            data.add(d);
        }
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                gson.toJson(data, LIST_TYPE, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Impossibile scrivere il file delle playlist: " + file, e);
        }
    }
}
