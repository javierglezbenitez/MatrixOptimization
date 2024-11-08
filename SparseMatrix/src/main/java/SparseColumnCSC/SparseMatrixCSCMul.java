package SparseColumnCSC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SparseMatrixCSCMul {

    // Sparse matrix in CSC format
    public static class CSCMatrix {
        double[] values;          // Non-zero values
        int[] rowIndices;         // Row indices corresponding to values
        int[] colPointers;        // Column pointers

        int rows, cols;           // Number of rows and columns in the matrix

        CSCMatrix(double[] values, int[] rowIndices, int[] colPointers, int rows, int cols) {
            this.values = values;
            this.rowIndices = rowIndices;
            this.colPointers = colPointers;
            this.rows = rows;
            this.cols = cols;
        }

        // Method to print the CSC matrix details
        public void printCSCDetails() {
            System.out.println("CSC Representation:");
            System.out.println("Values: " + Arrays.toString(values));
            System.out.println("Row Indices: " + Arrays.toString(rowIndices));
            System.out.println("Column Pointers: " + Arrays.toString(colPointers));
        }

        // Method to print the matrix in its full (dense) form
        public void printDenseMatrix() {
            double[][] denseMatrix = new double[rows][cols];

            for (int j = 0; j < cols; j++) {
                for (int i = colPointers[j]; i < colPointers[j + 1]; i++) {
                    denseMatrix[rowIndices[i]][j] = values[i];
                }
            }

            System.out.println("Dense Matrix:");
            for (double[] row : denseMatrix) {
                System.out.println(Arrays.toString(row));
            }
        }

        // Method to multiply two CSC matrices
        public CSCMatrix multiply(CSCMatrix B) {
            if (this.cols != B.rows) {
                throw new IllegalArgumentException("Matrix dimensions do not match for multiplication.");
            }

            List<Double> resultValues = new ArrayList<>();
            List<Integer> resultRowIndices = new ArrayList<>();
            List<Integer> resultColPointers = new ArrayList<>();
            resultColPointers.add(0);

            // Temporary array to store result for a single column in C
            double[] colResult = new double[this.rows];

            // Perform CSC matrix multiplication (this * B)
            for (int jB = 0; jB < B.cols; jB++) {
                // Clear colResult
                Arrays.fill(colResult, 0.0);

                // For each non-zero element in column jB of matrix B
                for (int k = B.colPointers[jB]; k < B.colPointers[jB + 1]; k++) {
                    int rowB = B.rowIndices[k];   // Row index in matrix B
                    double valB = B.values[k];    // Value of B at rowB and column jB

                    // Multiply column jB of B by corresponding row of this matrix
                    for (int i = this.colPointers[rowB]; i < this.colPointers[rowB + 1]; i++) {
                        int rowA = this.rowIndices[i];
                        double valA = this.values[i];
                        colResult[rowA] += valA * valB;
                    }
                }

                // Save the result of column jB in CSC format
                int nonZeroCount = 0;
                for (int i = 0; i < this.rows; i++) {
                    if (colResult[i] != 0.0) {
                        resultValues.add(colResult[i]);
                        resultRowIndices.add(i);
                        nonZeroCount++;
                    }
                }

                resultColPointers.add(resultColPointers.get(resultColPointers.size() - 1) + nonZeroCount);
            }

            // Convert lists to arrays
            double[] resultValuesArray = resultValues.stream().mapToDouble(Double::doubleValue).toArray();
            int[] resultRowIndicesArray = resultRowIndices.stream().mapToInt(Integer::intValue).toArray();
            int[] resultColPointersArray = resultColPointers.stream().mapToInt(Integer::intValue).toArray();

            return new CSCMatrix(resultValuesArray, resultRowIndicesArray, resultColPointersArray, this.rows, B.cols);
        }
    }

    // Method to convert a sparse matrix to CSC format
    public static CSCMatrix convertToCSC(double[][] matrix) {
        List<Double> valuesList = new ArrayList<>();
        List<Integer> rowIndicesList = new ArrayList<>();
        List<Integer> colPointersList = new ArrayList<>();

        int rows = matrix.length;
        int cols = matrix[0].length;

        colPointersList.add(0);

        // Traverse the matrix column by column and convert it into CSC format
        for (int j = 0; j < cols; j++) {
            int nonZeroCount = 0;
            for (int i = 0; i < rows; i++) {
                if (matrix[i][j] != 0) {
                    valuesList.add(matrix[i][j]);
                    rowIndicesList.add(i);
                    nonZeroCount++;
                }
            }
            colPointersList.add(colPointersList.get(colPointersList.size() - 1) + nonZeroCount);
        }

        // Convert lists to arrays
        double[] values = valuesList.stream().mapToDouble(Double::doubleValue).toArray();
        int[] rowIndices = rowIndicesList.stream().mapToInt(Integer::intValue).toArray();
        int[] colPointers = colPointersList.stream().mapToInt(Integer::intValue).toArray();

        return new CSCMatrix(values, rowIndices, colPointers, rows, cols);
    }

    public static void main(String[] args) {
        // Example sparse matrix A
        double[][] matrixA = {
                {1, 0, 0, 0},
                {0, 2, 0, 0},
                {0, 0, 0, 3},
                {4, 0, 5, 0}
        };

        // Example sparse matrix B
        double[][] matrixB = {
                {0, 0, 1, 0},
                {2, 0, 0, 0},
                {0, 0, 0, 3},
                {0, 4, 0, 0}
        };

        // Convert matrices A and B to CSC format
        CSCMatrix cscA = convertToCSC(matrixA);
        CSCMatrix cscB = convertToCSC(matrixB);

        // Print CSC representation of both matrices
        System.out.println("Matrix A in CSC format:");
        cscA.printCSCDetails();
        System.out.println("Matrix A in dense format:");
        cscA.printDenseMatrix();

        System.out.println("\nMatrix B in CSC format:");
        cscB.printCSCDetails();
        System.out.println("Matrix B in dense format:");
        cscB.printDenseMatrix();

        // Perform matrix multiplication (A * B)
        System.out.println("\nMultiplying Matrix A and Matrix B:");
        CSCMatrix resultMatrix = cscA.multiply(cscB);

        // Print the CSC representation of the result
        System.out.println("\nResulting Matrix in CSC format:");
        resultMatrix.printCSCDetails();
        System.out.println("Resulting Matrix in dense format:");
        resultMatrix.printDenseMatrix();
    }
}
