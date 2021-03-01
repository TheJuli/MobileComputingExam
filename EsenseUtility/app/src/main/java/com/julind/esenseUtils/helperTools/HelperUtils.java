package com.julind.esenseUtils.helperTools;

public class HelperUtils {
    public static double absoluteMean(Double[] m) {
        double mean = 0;

        for (Double aFloat : m) {
            mean += Math.abs(aFloat);
        }

        return mean / (float) m.length;
    }

    public static int getIndexOfLargest(int[] array) {
        if (array == null || array.length == 0) return -1;

        int largest = 0;
        for (int i = 1; i < array.length; i++)
        {
            if (array[i] > array[largest]) largest = i;
        }
        return largest;
    }
}
