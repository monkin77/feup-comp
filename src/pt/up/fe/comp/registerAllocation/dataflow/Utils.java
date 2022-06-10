package pt.up.fe.comp.registerAllocation.dataflow;

import java.util.Arrays;

public class Utils {
    public static String[][] deepCopyMatrix(String[][] matrix) {
        if (matrix == null){
            return null;
        }

        String[][] result = matrix.clone();
        for (int i = 0 ; i  < matrix.length; i++){
            if (matrix[i] == null)  result[i] = new String[]{};
            else result[i] = matrix[i].clone();
        }
        return result;
    }

    /**
     *
     * @return true if they are equal. False otherwise.
     */
    public static Boolean compareMatrix(Object[][] arr1, Object[][] arr2){
        if (arr1.length != arr2.length)
            return false;
        for (int i = 0; i < arr1.length; i++){
            if (!Arrays.equals(arr1[i], arr2[i]))
                return false;
        }
        return true;
    }
}
