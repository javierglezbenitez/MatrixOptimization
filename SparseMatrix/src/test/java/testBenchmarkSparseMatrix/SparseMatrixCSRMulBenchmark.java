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
public class SparseMatrixCSRMulBenchmark {

    @Param({"10", "100", "1000"})
    int matrixSize;

    CSRMatrix csrA;
    CSRMatrix csrB;

    @Setup(Level.Trial)
    public void setup() {
        double[][] denseMatrixA = generateRandomSparseMatrix(matrixSize);
        double[][] denseMatrixB = generateRandomSparseMatrix(matrixSize);

        csrA = convertToCSR(denseMatrixA);
        csrB = convertToCSR(denseMatrixB);
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
    public void testCSRMatrixMultiplication() {
        csrA.multiply(csrB);
    }

    static class CSRMatrix {
        double[] values;
        int[] columnIndices;
        int[] rowPointers;
        int rows, cols;

        CSRMatrix(double[] values, int[] columnIndices, int[] rowPointers, int rows, int cols) {
            this.values = values;
            this.columnIndices = columnIndices;
            this.rowPointers = rowPointers;
            this.rows = rows;
            this.cols = cols;
        }

        public CSRMatrix multiply(CSRMatrix B) {
            if (this.cols != B.rows) {
                throw new IllegalArgumentException("Matrix dimensions do not match for multiplication.");
            }

            List<Double> resultValues = new ArrayList<>();
            List<Integer> resultColumnIndices = new ArrayList<>();
            List<Integer> resultRowPointers = new ArrayList<>();
            resultRowPointers.add(0);

            double[] rowResult = new double[B.cols];

            for (int i = 0; i < this.rows; i++) {
                Arrays.fill(rowResult, 0.0);
                for (int j = this.rowPointers[i]; j < this.rowPointers[i + 1]; j++) {
                    int colA = this.columnIndices[j];
                    double valA = this.values[j];
                    for (int k = B.rowPointers[colA]; k < B.rowPointers[colA + 1]; k++) {
                        int colB = B.columnIndices[k];
                        double valB = B.values[k];
                        rowResult[colB] += valA * valB;
                    }
                }

                int nonZeroCount = 0;
                for (int j = 0; j < B.cols; j++) {
                    if (rowResult[j] != 0.0) {
                        resultValues.add(rowResult[j]);
                        resultColumnIndices.add(j);
                        nonZeroCount++;
                    }
                }

                resultRowPointers.add(resultRowPointers.get(resultRowPointers.size() - 1) + nonZeroCount);
            }

            return new CSRMatrix(
                    resultValues.stream().mapToDouble(Double::doubleValue).toArray(),
                    resultColumnIndices.stream().mapToInt(Integer::intValue).toArray(),
                    resultRowPointers.stream().mapToInt(Integer::intValue).toArray(),
                    this.rows, B.cols);
        }
    }

    public static CSRMatrix convertToCSR(double[][] matrix) {
        List<Double> valuesList = new ArrayList<>();
        List<Integer> columnIndicesList = new ArrayList<>();
        List<Integer> rowPointersList = new ArrayList<>();

        int rows = matrix.length;
        int cols = matrix[0].length;
        rowPointersList.add(0);

        for (int i = 0; i < rows; i++) {
            int nonZeroCount = 0;
            for (int j = 0; j < cols; j++) {
                if (matrix[i][j] != 0) {
                    valuesList.add(matrix[i][j]);
                    columnIndicesList.add(j);
                    nonZeroCount++;
                }
            }
            rowPointersList.add(rowPointersList.get(rowPointersList.size() - 1) + nonZeroCount);
        }

        return new CSRMatrix(
                valuesList.stream().mapToDouble(Double::doubleValue).toArray(),
                columnIndicesList.stream().mapToInt(Integer::intValue).toArray(),
                rowPointersList.stream().mapToInt(Integer::intValue).toArray(),
                rows, cols);
    }
}
