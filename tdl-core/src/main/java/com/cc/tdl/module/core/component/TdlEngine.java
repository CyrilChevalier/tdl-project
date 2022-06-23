package com.cc.tdl.module.core.component;

import com.cc.tdl.module.core.business.ETdlEngineStatus;

/**
 * <p>
 *                  Moteur principal de jeu
 * </p>
 *
 * @author Cyril Chevalier
 * @since 0.1.0
 */
public class TdlEngine {

    // ----------------------------------------------- Méthodes publiques -------------------------------------------

    /**
     * Retourne le statut du moteur
     * @return
     */
    public ETdlEngineStatus getStatus() {
        return ETdlEngineStatus.NONE;
    }
}
