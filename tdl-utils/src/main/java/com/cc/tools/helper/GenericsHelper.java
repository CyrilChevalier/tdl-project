package com.cc.tools.helper;

import io.vavr.Function1;
import io.vavr.control.Option;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;

/**
 * <p>
 *                  Classe d'aide pour la gestion de la généricité Java
 * </p>
 *
 * @author Cyril Chevalier
 * @since 0.1.0
 */
public abstract class GenericsHelper {

    // Constantes publiques
    public static final String                                      ERR_GENERICS_INDEX_MISMATCH = "La classe ne contient que {0} arguments génériques : l''index demandé {1} n''existe pas";

    /**
     * Constructeur interne
     */
    private GenericsHelper(){

    }

    // ------------------------------------------ Méthodes statiques publiques --------------------------------------
    /**
     * Effectue une recherche d'argument générique pour la classe passée en paramètre
     * @param clazz
     * @param argIndex
     * @return
     */
    public static <T> Class<T> getGenericArgumentForClass(Class<?> clazz, int argIndex) {
        return searchGenericArgumentForClass(clazz, argIndex, false);
    }

    /**
     * Effectue une recherche d'argument générique pour la classe passée en paramètre
     * @param clazz
     * @param argIndex
     * @param safeMode Mode sans exception (retourne null si non trouvé)
     * @return
     */
    public static <T> Class<T> getGenericArgumentForClass(Class<?> clazz, int argIndex, boolean safeMode) {
        return searchGenericArgumentForClass(clazz, argIndex, safeMode);
    }

    /**
     * Effectue une recherche d'argument générique pour l'interface passée en paramètre
     * @param clazz
     * @param argIndex
     * @return
     */
    public static <T> Class<T> getGenericArgumentForInterface(Class<?> clazz, int argIndex) {
        return searchGenericArgumentForInterface(clazz, null, argIndex, false);
    }

    /**
     * Effectue une recherche d'argument générique pour l'interface passée en paramètre
     * @param <T>
     * @param clazz
     * @param argIndexSupplier
     * @return
     */
    public static <T> Class<T> getGenericArgumentForInterface(Class<?> clazz, Function1<Class<?>,Integer> argIndexSupplier) {
        return searchGenericArgumentForInterface(clazz, null, argIndexSupplier, false);
    }

    /**
     * Effectue une recherche d'argument générique pour l'interface passée en paramètre
     * @param clazz
     * @param argIndex
     * @param safeMode Mode sans exception (retourne null si non trouvé)
     * @return
     */
    public static <T> Class<T> getGenericArgumentForInterface(Class<?> clazz, int argIndex, boolean safeMode) {
        return searchGenericArgumentForInterface(clazz, null, argIndex, safeMode);
    }

    /**
     * Effectue une recherche d'argument générique pour l'interface passée en paramètre
     * @param <T>
     * @param clazz
     * @param argIndexSupplier
     * @param safeMode
     * @return
     */
    public static <T> Class<T> getGenericArgumentForInterface(Class<?> clazz, Function1<Class<?>,Integer> argIndexSupplier, boolean safeMode) {
        return searchGenericArgumentForInterface(clazz, null, argIndexSupplier, safeMode);
    }

    /**
     * Effectue une recherche d'argument générique pour l'interface passée en paramètre
     * @param <T>
     * @param clazz
     * @param specificClass
     * @param argIndex
     * @param safeMode
     * @return
     */
    public static <T> Class<T> getGenericArgumentForInterface(Class<?> clazz, Class<?> specificClass, int argIndex, boolean safeMode) {
        return searchGenericArgumentForInterface(clazz, specificClass, argIndex, safeMode);
    }

    /**
     * Effectue une recherche d'argument générique pour l'interface passée en paramètre
     * @param <T>
     * @param clazz
     * @param specificClass
     * @param argIndex
     * @return
     * @since 1.0.19
     */
    public static <T> Class<T> getGenericArgumentForInterface(Class<?> clazz, Class<?> specificClass, int argIndex) {
        return searchGenericArgumentForInterface(clazz, specificClass, argIndex, false);
    }

    // ------------------------------------------ Méthodes statiques privées --------------------------------------
    /**
     * Effectue une recherche d'argument générique sur une classe
     * @param clazz Classe
     * @param argIndex Index (basé sur 0) de l'argument générique
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<T> searchGenericArgumentForClass(Class<?> clazz, int argIndex, boolean safeMode) {
        Class<T> result = null;
        Type supertype = clazz.getGenericSuperclass();
        if (supertype instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType)supertype).getActualTypeArguments();
            if (types.length <= argIndex) {
                if (!safeMode) {
                    throw new RuntimeException(MessageFormat.format(ERR_GENERICS_INDEX_MISMATCH, types.length, argIndex));
                }
            }
            else {
                Type type = types[argIndex];
                if (type instanceof ParameterizedType) {
                    result = (Class<T>) ((ParameterizedType)type).getRawType();
                }
                else {
                    result = (Class<T>)types[argIndex];
                }
            }
        } else if (clazz.getSuperclass()!=null) {
            result = searchGenericArgumentForClass(clazz.getSuperclass(), argIndex, safeMode);
        }
        return result;
    }

    /**
     * Recherche un argument générique pour une interface
     * @param clazz
     * @param specificClass
     * @param argIndex
     * @param safeMode
     * @return
     */
    private static <T> Class<T> searchGenericArgumentForInterface(Class<?> clazz, Class<?> specificClass, int argIndex, boolean safeMode) {
        return searchGenericArgumentForInterface(clazz, specificClass, c -> argIndex, safeMode);
    }

    /**
     * Recherche un argument générique pour une interface
     * @param clazz Classe de l'interface sur laquelle on fait la recherche
     * @param specificClass Recherche d'une interface spécifique
     * @param argIndexSupplier Index de l'argument
     * @param safeMode Indique si on est en mode "sécurisé" (sans exception)
     * @return
     */
    private static <T> Class<T> searchGenericArgumentForInterface(Class<?> clazz, Class<?> specificClass, Function1<Class<?>, Integer> argIndexSupplier, boolean safeMode) {
        Class<T> result = null;
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        if (genericInterfaces!=null && genericInterfaces.length > 0) {
            for (Type supertype : genericInterfaces) {
                if (supertype instanceof ParameterizedType) {
                    result = searchParameterizedTypeForInterface((ParameterizedType)supertype, specificClass, argIndexSupplier, safeMode);
                }
                if (result!=null) {
                    break;
                }
            }
            if (result == null && clazz.getSuperclass()!=null) {
                result = searchGenericArgumentForInterface(clazz.getSuperclass(), specificClass, argIndexSupplier, safeMode);
            }
        }
        return result;
    }

    /**
     * Recherche la classe d'un type paramétré particulier
     * @param <T>
     * @param type
     * @param specificClass
     * @param argIndexSupplier
     * @param safeMode
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<T> searchParameterizedTypeForInterface(ParameterizedType type, Class<?> specificClass, Function1<Class<?>, Integer> argIndexSupplier, boolean safeMode) {
        Class<T> result = null;
        if (checkIsSpecificClass(type, specificClass)) {
            int argIndex = argIndexSupplier.apply(Option
                    .of(type.getRawType())
                    .flatMap(t -> t instanceof Class ? Option.of((Class<?>)t) : Option.none())
                    .getOrNull());
            Type[] types = type.getActualTypeArguments();
            if (types.length <= argIndex) {
                if (!safeMode) {
                    throw new RuntimeException(MessageFormat.format(ERR_GENERICS_INDEX_MISMATCH, types.length, argIndex));
                }
            }
            else {
                result = (Class<T>)types[argIndex];
            }
        }
        return result;
    }

    /**
     * Vérifie si le type représente une classe spécifique
     * @param type
     * @param specificClass
     * @return
     */
    private static boolean checkIsSpecificClass(ParameterizedType type, Class<?> specificClass) {
        boolean result = true;
        // Si aucune classe spécifique n'est spécifiée, c'est toujours vrai
        if (specificClass!=null) {
            // Sinon on vérifie
            Type rawType = type.getRawType();
            if (rawType instanceof Class) {
                result = ((Class<?>)rawType).equals(specificClass);
            }
        }
        return result;
    }
}
