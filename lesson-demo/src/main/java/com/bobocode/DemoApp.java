package com.bobocode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DemoApp {
    public static void main(String[] args) {
        List<String> list = Arrays.asList("java", "nikolay", "test", "comparator", "available", "generic", "annotation");
        mergeSort(list);
        System.out.println(list);
    }

    public static <T extends Comparable<T>> void mergeSort(List<T> list) {
        checkIfModifiable(list);
        List<T> copy = new ArrayList<>(list);
        splitSort(copy, list, 0, list.size());
    }

    private static <T extends Comparable<T>> void splitSort(List<T> src, List<T> dest, int start, int end) {
        if (end - start <= 1) {
            return;
        }
        int middle = (end + start) / 2;
        splitSort(dest, src, start, middle);
        splitSort(dest, src, middle, end);
        merge(src, dest, start, middle, end);
    }

    private static <T extends Comparable<T>> void merge(List<T> src, List<T> dest, int start, int middle, int end) {
        int i = start;
        int j = middle;
        for (int k = start; k <
                end; k++) {
            Comparator<T> nullSaveComparator = Comparator.nullsLast(T::compareTo);
            if (i < middle &&(j >= end || nullSaveComparator.compare(src.get(i), src.get(j)) <= 0)) {
                dest.set(k, src.get(i));
                i++;
            } else {
                dest.set(k, src.get(j));
                j++;
            }
        }
    }

    private static <T> void checkIfModifiable(List<T> list) {
        try {
            var element = list.get(0);
            list.set(0, null);
            list.set(0, element);
        } catch (UnsupportedOperationException e) {
            throw new IllegalArgumentException("List is unmodifiable");
        }
    }
}
