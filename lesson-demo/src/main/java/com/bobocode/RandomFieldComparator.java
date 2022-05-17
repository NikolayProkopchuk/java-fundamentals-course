package com.bobocode;

import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A generic comparator that is comparing a random field of the given class. The field is either primitive or
 * {@link Comparable}. It is chosen during comparator instance creation and is used for all comparisons.
 * <p>
 * By default it compares only accessible fields, but this can be configured via a constructor property. If no field is
 * available to compare, the constructor throws {@link IllegalArgumentException}
 *
 * @param <T> the type of the objects that may be compared by this comparator
 */
public class RandomFieldComparator<T> implements Comparator<T> {

    private final Class<T> targetType;
    private final Field comparableField;

    public RandomFieldComparator(Class<T> targetType) {
        this(targetType, true);
    }

    /**
     * A constructor that accepts a class and a property indicating which fields can be used for comparison. If property
     * value is true, then only public fields or fields with public getters can be used.
     *
     * @param targetType                  a type of objects that may be compared
     * @param compareOnlyAccessibleFields config property indicating if only publicly accessible fields can be used
     */
    public RandomFieldComparator(Class<T> targetType, boolean compareOnlyAccessibleFields) {
        this.targetType = targetType;
        List<Field> comparableFields = Arrays.stream(targetType.getDeclaredFields())
                .filter(field -> field.getType().isPrimitive() || Comparable.class.isAssignableFrom(field.getType()))
                .toList();
        if (compareOnlyAccessibleFields) {
            comparableFields = comparableFields.stream().filter(field -> !Modifier.isPrivate(field.getModifiers())).toList();
        }
        if (comparableFields.isEmpty()) {
            throw new IllegalArgumentException();
        }
        comparableField = comparableFields.get(ThreadLocalRandom.current().nextInt(comparableFields.size()));
        if (!compareOnlyAccessibleFields) {
            comparableField.setAccessible(true);
        }
    }

    /**
     * Compares two objects of the class T by the value of the field that was randomly chosen. It allows null values
     * for the fields, and it treats null value grater than a non-null value (nulls last).
     */
    @SneakyThrows
    @Override
    public int compare(T o1, T o2) {
        Comparable o1FieldValue = (Comparable) comparableField.get(o1);
        Comparable o2FieldValue = (Comparable) comparableField.get(o2);

        if (o1FieldValue == null && o2FieldValue == null) {
            return 0;
        } else if (o1FieldValue == null) {
            return 1;
        } else if (o2FieldValue == null) {
            return -1;
        }

        return o1FieldValue.compareTo(o2FieldValue);
    }

    /**
     * Returns a statement "Random field comparator of class '%s' is comparing '%s'" where the first param is the name
     * of the type T, and the second parameter is the comparing field name.
     *
     * @return a predefined statement
     */
    @Override
    public String toString() {
        return String.format("Random field comparator of class '%s' is comparing '%s'", targetType.getSimpleName(), comparableField.getName());
    }
}
