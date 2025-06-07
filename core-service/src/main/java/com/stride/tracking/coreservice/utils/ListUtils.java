package com.stride.tracking.coreservice.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListUtils {
    private ListUtils() {}

    public static List<Integer> minimized(List<Integer> list, int partitions) {
        int size = list.size();

        if (size <= partitions) {
            return list;
        }

        List<Integer> result = new ArrayList<>(partitions);
        double step = (double) size / partitions;
        
        Integer maxValue = Collections.max(list, Comparator.comparingDouble(Integer::doubleValue));
        int maxIndex = -1;
        for (int i = 0; i < size; i++) {
            if (list.get(i).doubleValue() == maxValue.doubleValue()) {
                maxIndex = i;
                break;
            }
        }
        
        int maxPartitionIndex = (int) Math.floor(maxIndex / step);

        for (int i = 0; i < partitions; i++) {
            int start = (int) Math.floor(i * step);
            int end = Math.min((int) Math.floor((i + 1) * step), size);

            List<Integer> partition = list.subList(start, end);
            if (partition.isEmpty()) {
                result.add(0);
                continue;
            }

            if (i == maxPartitionIndex) {
                result.add(maxValue);
            } else {
                double sum = 0;
                for (Integer num : partition) {
                    sum += num.doubleValue();
                }
                double avg = sum / partition.size();
                result.add((int) Math.floor(avg));
            }
        }

        return result;
    }

    public static List<Integer> minimizedIndices(List<Double> list, int partitions) {
        int size = list.size();
        if (size <= partitions) {
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                indices.add(i);
            }
            return indices;
        }

        List<Integer> resultIndices = new ArrayList<>();
        double step = (double) size / partitions;

        double maxValue = Collections.max(list);
        int maxIndex = list.indexOf(maxValue);
        int maxPartitionIndex = (int) Math.floor(maxIndex / step);

        for (int i = 0; i < partitions; i++) {
            int start = (int) Math.floor(i * step);
            int end = Math.min((int) Math.floor((i + 1) * step), size);

            if (start >= end) {
                resultIndices.add(start);
                continue;
            }

            if (i == maxPartitionIndex) {
                resultIndices.add(maxIndex);
            } else {
                List<Double> partition = list.subList(start, end);
                double avg = partition.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                int closestIndex = start;
                double minDiff = Double.MAX_VALUE;
                for (int j = start; j < end; j++) {
                    double diff = Math.abs(list.get(j) - avg);
                    if (diff < minDiff) {
                        minDiff = diff;
                        closestIndex = j;
                    }
                }
                resultIndices.add(closestIndex);
            }
        }

        return resultIndices;
    }

}
