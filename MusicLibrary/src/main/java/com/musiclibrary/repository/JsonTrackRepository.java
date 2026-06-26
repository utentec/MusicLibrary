package com.musiclibrary.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
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
 * Implementazione di {@link TrackRepository} che persiste i brani su file in
 * formato JSON. Carica i dati alla costruzione e li risalva dopo ogni operazione
 * di modifica, così che nulla vada perso alla chiusura dell'applicazione.
 */
public class JsonTrackRepository implements TrackRepository {

    private static final Type LIST_TYPE = new TypeToken<List<Track>>() {}.getType();

    private final Path file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, Track> tracks = new LinkedHashMap<>();

    /**
     * Costruisce il repository e carica i brani dal file indicato, se esiste.
     * @param file percorso del file JSON dei brani
     */
    public JsonTrackRepository(Path file) {
        this.file = file;
        load();
    }

    @Override
    public void addTrack(Track track) {
        tracks.put(track.getId(), track);
        save();
    }

    @Override
    public void removeTrack(String id) {
        tracks.remove(id);
        save();
    }

    @Override
    public void updateTrack(Track track) {
        tracks.put(track.getId(), track);
        save();
    }

    @Override
    public Optional<Track> findById(String id) {
        return Optional.ofNullable(tracks.get(id));
    }

    @Override
    public Optional<Track> findByName(String title) {
        return tracks.values().stream()
                .filter(t -> t.getTitle().equals(title))
                .findFirst();
    }

    @Override
    public List<Track> findAll() {
        return new ArrayList<>(tracks.values());
    }

    private void load() {
        if (!Files.exists(file)) {
            return; // primo avvio / file assente: libreria vuota
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            List<Track> loaded = gson.fromJson(reader, LIST_TYPE);
            if (loaded != null) {
                for (Track t : loaded) {
                    tracks.put(t.getId(), t);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Impossibile leggere il file dei brani: " + file, e);
        }
    }

    private void save() {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                gson.toJson(findAll(), LIST_TYPE, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Impossibile scrivere il file dei brani: " + file, e);
        }
    }
}
