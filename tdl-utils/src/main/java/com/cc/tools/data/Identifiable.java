package com.cc.tools.data;

/**
 * <p>
 *                  Interface représentant un élément identifiable par un id
 * </p>
 *
 * @author Cyril Chevalier
 * @since 0.1.0
 */
public interface Identifiable<T extends Comparable<T>> {

    /**
     * Retourne l'identifiant
     * @return
     */
    T getId();
}
