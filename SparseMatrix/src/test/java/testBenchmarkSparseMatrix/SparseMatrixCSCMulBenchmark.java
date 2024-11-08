package testBenchmarkSparseMatrix;

import org.openjdk.jmh.annotations.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class SparseMatrixCSCMulBenchmark {

    @Param({"10", "100", "1000"})
    int matrixSize;

    CSCMatrix cscA;
    CSCMatrix cscB;

    @Setup(Level.Trial)
    public void setup() {
        double[][] denseMatrixA = generateRandomSparseMatrix(matrixSize);
        double[][] denseMatrixB = generateRandomSparseMatrix(matrixSize);

        cscA = convertToCSC(denseMatrixA);
        cscB = convertToCSC(denseMatrixB);
    }

    private double[][] generateRandomSparseMatrix(int size) {
        double[][] matrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = Math.random() > 0.7 ? Math.random() : 0; // Sparse matrix
            }
        }
        return matrix;
    }

    @Benchmark
    public void testCSCMatrixMultiplication() {
        cscA.multiply(cscB);
    }

    static class CSCMatrix {
        double[] values;
        int[] rowIndices;
        int[] colPointers;
        int rows, cols;

        CSCMatrix(double[] values, int[] rowIndices, int[] colPointers, int rows, int cols) {
            this.values = values;
            this.rowIndices = rowIndices;
            this.colPointers = colPointers;
            this.rows = rows;
            this.cols = cols;
        }

        public CSCMatrix multiply(CSCMatrix B) {
            if (this.cols != B.rows) {
                throw new IllegalArgumentException("Matrix dimensions do not match for multiplication.");
            }

            List<Double> resultValues = new ArrayList<>();
            List<Integer> resultRowIndices = new ArrayList<>();
            List<Integer> resultColPointers = new ArrayList<>();
            resultColPointers.add(0);

            double[] colResult = new double[this.rows];

            for (int jB = 0; jB < B.cols; jB++) {
                Arrays.fill(colResult, 0.0);
                for (int k = B.colPointers[jB]; k < B.colPointers[jB + 1]; k++) {
                    int rowB = B.rowIndices[k];
                    double valB = B.values[k];
                    for (int i = this.colPointers[rowB]; i < this.colPointers[rowB + 1]; i++) {
                        int rowA = this.rowIndices[i];
                        double valA = this.values[i];
                        colResult[rowA] += valA * valB;
                    }
                }

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

            return new CSCMatrix(
                    resultValues.stream().mapToDouble(Double::doubleValue).toArray(),
                    resultRowIndices.stream().mapToInt(Integer::intValue).toArray(),
                    resultColPointers.stream().mapToInt(Integer::intValue).toArray(),
                    this.rows, B.cols);
        }
    }

    public static CSCMatrix convertToCSC(double[][] matrix) {
        List<Double> valuesList = new ArrayList<>();
        List<Integer> rowIndicesList = new ArrayList<>();
        List<Integer> colPointersList = new ArrayList<>();

        int rows = matrix.length;
        int cols = matrix[0].length;
        colPointersList.add(0);

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

        return new CSCMatrix(
                valuesList.stream().mapToDouble(Double::doubleValue).toArray(),
                rowIndicesList.stream().mapToInt(Integer::intValue).toArray(),
                colPointersList.stream().mapToInt(Integer::intValue).toArray(),
                rows, cols);
    }
}
