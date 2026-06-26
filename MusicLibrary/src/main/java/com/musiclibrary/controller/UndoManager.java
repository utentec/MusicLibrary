package com.musiclibrary.controller;

import com.musiclibrary.command.Command;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Gestore della cronologia dei comandi: mantiene due pile (undo e redo)
 * per supportare l'annullamento e il ripristino delle operazioni.
 */
public class UndoManager {

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    /**
     * Esegue un comando e lo registra nella pila di undo, svuotando la pila di redo.
     * @param command il comando da eseguire
     */
    public void executeCommand(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    /** Annulla l'ultimo comando eseguito, se presente, spostandolo nella pila di redo. */
    public void undo() {
        if (canUndo()) {
            Command command = undoStack.pop();
            command.undo();
            redoStack.push(command);
        }
    }

    /** Riesegue l'ultimo comando annullato, se presente, riportandolo nella pila di undo. */
    public void redo() {
        if (canRedo()) {
            Command command = redoStack.pop();
            command.execute();
            undoStack.push(command);
        }
    }

    /**
     * Indica se esiste almeno un comando annullabile.
     * @return {@code true} se la pila di undo non è vuota
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Indica se esiste almeno un comando ripristinabile.
     * @return {@code true} se la pila di redo non è vuota
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
