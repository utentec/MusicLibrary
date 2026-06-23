package com.musiclibrary.command;
/**
 * Pattern Command: incapsula un'operazione reversibile.
 * Ogni comando concreto implementa l'esecuzione e il suo annullamento.
 */
public interface Command {

    /** Esegue l'operazione. */
    void execute();

    /** Annulla l'operazione, ripristinando lo stato precedente. */
    void undo();
}
