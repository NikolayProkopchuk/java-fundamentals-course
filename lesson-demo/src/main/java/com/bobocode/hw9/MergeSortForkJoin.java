package com.bobocode.hw9;

import com.bobocode.data.Accounts;
import com.bobocode.model.Account;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class MergeSortForkJoin<T extends Comparable<T>> extends RecursiveAction {

    public static void main(String[] args) {
        String[] accountsFistNames = Accounts.generateAccountList(20).stream().map(Account::getFirstName).toArray(String[]::new);
        RecursiveAction recursiveAction = new MergeSortForkJoin<>(accountsFistNames);
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(recursiveAction);
        System.out.println(Arrays.toString(accountsFistNames));
    }

    private final T[] source;
    private final T[] dest;
    private final int start;
    private final int end;

    public MergeSortForkJoin(T[] array) {
        Objects.requireNonNull(array);
        this.source = array;
        this.dest = Arrays.copyOf(array, array.length);
        this.start = 0;
        this.end = array.length;
    }

    public MergeSortForkJoin(T[] source, T[] dest, int start, int end) {
        this.source = source;
        this.dest = dest;
        this.start = start;
        this.end = end;
    }

    @Override
    protected void compute() {
        if (end - start <= 1) {
            return;
        }
        int middle = (start + end) / 2;
        MergeSortForkJoin<T> leftAction = new MergeSortForkJoin<>(dest, source, start, middle);
        leftAction.fork();
        MergeSortForkJoin<T> rightAction = new MergeSortForkJoin<>(dest, source, middle, end);
        rightAction.compute();
        leftAction.join();
        merge();
    }

    private void merge() {
        int i = start;
        int middle = (start + end) / 2;
        int j = middle;
        for (int k = start; k < end; k++) {
            if (i < middle && (j >= end || dest[i].compareTo(dest[j]) < 0)) {
                source[k] = dest[i];
                i++;
            } else {
                source[k] = dest[j];
                j++;
            }
        }
    }
}
