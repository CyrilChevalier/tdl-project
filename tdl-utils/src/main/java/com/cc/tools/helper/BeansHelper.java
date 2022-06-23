package com.cc.tools.helper;

import io.vavr.CheckedFunction1;
import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.Comparator;

/**
 * <p>
 *                  Classe singleton permettant d'effectuer des actions sur
 *                  la manipulation de beans.
 *
 *                  Cette classe est abstraite.
 * </p>
 *
 * @author Cyril Chevalier
 * @since 0.1.0
 */
public abstract class BeansHelper {

    // Membres internes
    private static Map<Class<?>, BeanInfo<?>>                           beansMap = HashMap.empty();

    /**
     * Constructeur privé
     */
    private BeansHelper() {

    }

    // ------------------------------------------ Méthodes statiques publiques --------------------------------------
    /**
     * Renvoie vrai si deux beans sont égaux, faux dans le cas contraire
     * @param bean1 Bean 1
     * @param bean2 Bean 2
     * @return
     */
    public static <T> Boolean areEquals(T bean1, T bean2) {
        if(bean1 != null && bean2 != null) {
            return bean1.equals(bean2);
        }
        return bean1 == null && bean2 == null ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Retourne un comparateur sur le bean, basé sur les champs et l'ordre fournis en paramètre
     * @param beanClass Classe du bean
     * @param sort
     * @return
     */
    @SafeVarargs
    public static <T> Comparator<T> getComparator(Class<T> beanClass,Tuple2<Boolean, String> ... sort){
        BeanInfo<T> beanInfo = getBeanInfo(beanClass);
        return beanInfo.getComparator(sort);
    }

    /**
     * Retourne un comparateur sur le bean, basé sur les champs passés avec l'ordre croissant
     * @param beanClass Classe de bean
     * @param sort Champs à trier
     * @return
     */
    public static <T> Comparator<T> getComparator(Class<T> beanClass,String ...sort){
        return getComparator(beanClass,true,sort);
    }

    /**
     * Retourne un comparateur sur le bean, basé sur les champs passés avec l'ordre fourni dans direction
     * @param beanClass Classe de bean
     * @param direction Direction
     * @param sort Champs à trier
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> Comparator<T> getComparator(Class<T> beanClass,boolean direction,String ...sort){
        return getComparator(beanClass,List.of(sort).map(s->(Tuple2)Tuple.of(direction, s)).toJavaArray(Tuple2[]::new));
    }

    /**
     * Retourne la valeur d'un champ de bean
     * @param bean
     * @param fieldName
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T,R> R getFieldValue(T bean,String fieldName){
        return bean!=null && fieldName!=null ? (R)Try.of(()->getBeanInfo(bean.getClass()).getField(fieldName).get(bean)).getOrElse((R)null) : null;
    }

    /**
     * Positionne la valeur d'un champ de bean
     * @param bean
     * @param fieldName
     * @param value
     * @return
     */
    @SneakyThrows
    public static <T,R> T setFieldValue(T bean,String fieldName, R value) {
        if (bean!=null && fieldName!=null) {
            getBeanInfo(bean.getClass()).getField(fieldName).set(bean, value);
        }
        return bean;
    }

    /**
     * Retourne un champ dans une classe. La recherche peut se faire hiérarchiquement
     * @param beanClass Classe du bean
     * @param fieldName Nom du champ à rechercher
     * @return
     */
    public static <T> Field getField(Class<T> beanClass, String fieldName) {
        return beanClass!=null && fieldName!=null ? getBeanInfo(beanClass).getField(fieldName) : null;
    }

    /**
     * Récupère un comparateur sur un bean à partir d'information de tri comprenant une fonction pour récupérer une valeur
     * @param <T>
     * @param sortInfos
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked"})
    public static <T> Comparator<T> getComparatorFromSuppliers(Tuple2<Boolean, Function1<T,Comparable>> ... sortInfos) {
        return getComparatorFromSuppliers(List.of(sortInfos));
    }

    /**
     * Récupère un comparateur sur un bean à partir d'information de tri comprenant une fonction pour récupérer une valeur
     * @param <T>
     * @param sortInfos
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked"})
    public static <T> Comparator<T> getComparatorFromSuppliers(Seq<Tuple2<Boolean, Function1<T,Comparable>>> sortInfos) {
        return (b1,b2)->
                sortInfos.foldLeft(0, (result,sortInfo)->{
                    if (result==0){
                        result = Try.of(()->{
                            Comparable c1 = sortInfo._2().apply(b1);
                            Comparable c2 = sortInfo._2().apply(b2);
                            if (c1==null){
                                if (c2==null) return 0;
                                return sortInfo._1().booleanValue() ? -1 : 1;
                            }
                            else if (c2==null){
                                return sortInfo._1().booleanValue() ? 1 : -1;
                            }
                            return sortInfo._1().booleanValue() ? c1.compareTo(c2) : c2.compareTo(c1);
                        }).getOrElse(0);
                    }
                    return result;
                });
    }

    /**
     * Compare deux éléments de même type qui étendent Comparable
     * @param <T>
     * @param c1
     * @param c2
     * @return
     */
    public static <T extends Comparable<T>> int compare(T c1, T c2) {
        int result;
        if (c1!=null) {
            if (c2!=null) {
                result = c1.compareTo(c2);
            }
            else {
                result = 1;
            }
        }
        else {
            if (c2!=null) {
                result = -1;
            }
            else {
                result = 0;
            }
        }
        return result;
    }

    /**
     * Retourne la liste des champs d'un bean
     * @param beanClass
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Seq<Tuple2<String,Field>> getBeanFields(@NonNull Class<T> beanClass) {
        return getBeanInfo(beanClass).forceLoadAllFields().getFields();
    }

    // ------------- Méthodes statiques privées -------------

    /**
     * Retourne les informations liées au bean
     * @param beanClass Classe de bean
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T> BeanInfo<T> getBeanInfo(Class<T> beanClass){
        synchronized(BeansHelper.class){
            Tuple2<BeanInfo<?>,Map<Class<?>, BeanInfo<?>>> result = (Tuple2<BeanInfo<?>, Map<Class<?>, BeanInfo<?>>>) beansMap.computeIfAbsent(beanClass, BeanInfo::new);
            beansMap = result._2();
            return (BeanInfo<T>) result._1();
        }
    }

    // ----------------------------------------------- Classes internes -------------------------------------------

    /**
     * <p>
     *                  Classe interne d'informations sur un bean
     * </p>
     */
    private static class BeanInfo<T> {

        // Membres internes
        private Class<T>                            beanClass;
        private Map<String, Field>                  beanFields;
        private boolean                             allFieldsLoaded;

        /**
         * @param beanClass
         */
        public BeanInfo(Class<T> beanClass){
            this.beanClass = beanClass;
            this.beanFields = HashMap.empty();
            this.allFieldsLoaded = false;
        }

        // ----------------------- Méthodes publiques ---------------------
        /**
         * Construit un comparateur sur le bean, basé sur les champs et l'ordre fournis en paramètre
         * @param sort Informations de tri sous la forme d'un ensemble de tuples contenant l'ordre (true=ascendant,false=descendant) et le champ
         * @return
         * @since 0.0.1
         */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public Comparator<T> getComparator(Tuple2<Boolean, String>... sort){
            Seq<Tuple2<Boolean, Function1<T,Comparable>>> fields = List.of(sort)
                    .map(s->s.map2(this::getField))
                    .filter(s->s._2()!=null && Comparable.class.isAssignableFrom(s._2().getType()))
                    .map(s->s.map2(f -> CheckedFunction1.<T,Comparable>of(o -> (Comparable)f.get(o)).unchecked()));
            return getComparatorFromSuppliers(fields);
        }

        /**
         * Trouve le champ
         * @param name
         * @return
         * @since 0.0.1
         */
        public Field getField(String name){
            Field result = this.beanFields.getOrElse(name, null);
            if (result==null){
                result = /*Try.of(()->beanClass.getDeclaredField(name))*/
                        this.findFieldInHierarchy(beanClass, name)
                                .onSuccess(f->{f.setAccessible(true);this.beanFields = this.beanFields.put(name, f);})
                                .getOrElse((Field)null);
            }
            return result;
        }

        /**
         * Retourne la liste des champs de ce bean
         * @return
         */
        public Seq<Tuple2<String,Field>> getFields() {
            return this.beanFields.toList();
        }

        /**
         * Force le chargement de tous les champs
         * @return
         */
        @SuppressWarnings("rawtypes")
        public BeanInfo forceLoadAllFields() {
            if (!this.allFieldsLoaded) {
                this.beanFields = this.loadFields(this.beanClass)
                        .toMap(t -> t);
                this.allFieldsLoaded = true;
            }
            return this;
        }

        // --------------------------------------- Méthodes privées ---------------------------------------------
        /**
         * Trouve un champ dans la hiérarchie
         * @param clazz
         * @param name
         * @return
         * @since 1.0.2
         */
        private Try<Field> findFieldInHierarchy(Class<?> clazz, String name) {
            return Try.of(()-> clazz.getDeclaredField(name))
                    .recoverWith(e -> Option
                            .of(clazz.getSuperclass())
                            .map(sc -> this.findFieldInHierarchy(sc,name))
                            .getOrElse(Try.failure(e)));
        }

        /**
         * Charge tous les champs
         * @param clazz
         * @return
         */
        private Seq<Tuple2<String,Field>> loadFields(Class<?> clazz) {
            if (clazz == null) {
                return List.empty();
            }
            return loadFields(clazz.getSuperclass())
                    .appendAll(List.of(clazz.getDeclaredFields())
                            .map(f -> {
                                f.setAccessible(true);
                                return Tuple.of(f.getName(),f);
                            }));
        }

    }
}
