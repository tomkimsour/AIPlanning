package aiplanning;

import java.util.ArrayList;
import java.util.List;

public class Combinations {

    // Could use this to generate the indexes of positions to select
    private static void helper(List<int[]> combinations, int[] data, int start, int end, int index) {
        if (index == data.length) {
            int[] combination = data.clone();
            combinations.add(combination);
        } else if (start <= end) {
            data[index] = start;
            // Include current element
            helper(combinations, data, start + 1, end, index +1);
            // Discard current element
            helper(combinations, data, start + 1, end, index);
        }
    }

    // For generating indexes for r positions of n possible
    public static List<int[]> generateIndexes(int n, int r) {
        List<int[]> combinations = new ArrayList<>();
        helper(combinations, new int[r], 0, n-1, 0);
        return combinations;
    }
}
