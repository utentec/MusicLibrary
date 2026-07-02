package com.musiclibrary.player;

/**
 * Astrazione minimale del player audio. Isola il resto dell'applicazione dal
 * MediaPlayer di JavaFX, così la logica di riproduzione ({@link PlayerService})
 * resta testabile sostituendo questa interfaccia con un doppio di test.
 */
public interface AudioPlayer {

    /**
     * Avvia la riproduzione del file audio indicato, interrompendo l'eventuale
     * riproduzione già in corso.
     * @param filePath percorso del file audio da riprodurre
     */
    void play(String filePath);

    /**
     * Ferma la riproduzione in corso, se presente.
     */
    void stop();

    /**
     * Mette in pausa la riproduzione in corso, mantenendo la posizione. Non ha
     * effetto se non c'è nulla in riproduzione.
     */
    void pause();

    /**
     * Riprende la riproduzione dal punto in cui era stata messa in pausa. Non ha
     * effetto se non c'è una riproduzione in pausa.
     */
    void resume();


    /**
     * Registra l'azione da eseguire quando il brano termina naturalmente.
     * @param callback l'azione da eseguire alla fine del brano
     */
    void setOnEndOfMedia(Runnable callback);
}
