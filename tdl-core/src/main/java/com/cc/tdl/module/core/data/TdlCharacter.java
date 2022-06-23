package com.cc.tdl.module.core.data;

import com.cc.tools.data.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 *                  Classe de base pour les données correspondant
 *                  à un personnage (joueur/non joueur)
 * </p>
 *
 * @author Cyril Chevalier
 * @since 0.1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TdlCharacter implements Identifiable<Long> {

    // Membres internes
    private Long                id;

}
