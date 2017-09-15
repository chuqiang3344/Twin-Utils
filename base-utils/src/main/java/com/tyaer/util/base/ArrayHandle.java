package com.tyaer.util.base;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Twin on 2017/9/12.
 */
public class ArrayHandle {
    public static void main(String[] args) {
        ArrayList<Integer> integers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            integers.add(i);
        }
        List[] loop = spiltList(integers, 11);
        for (List list : loop) {
            System.out.println(list);
        }
    }

    /**
     *
     * @param list
     * @param batchSize
     * @return
     */
    public static List[] spiltList(List list, int batchSize) {
        if (list != null) {
            int size = list.size();
            if (size > 0) {
                int n = size % batchSize == 0 ? size / batchSize : size / batchSize + 1;
                List[] lists = new List[n];
                for (int i = 0; i < n; i++) {
                    int start = i * batchSize;
                    int end = (i + 1) * batchSize > size ? size : (i + 1) * batchSize;
                    lists[i] = list.subList(start, end);
                }
                return lists;
            }
        }
        return null;
    }
}
