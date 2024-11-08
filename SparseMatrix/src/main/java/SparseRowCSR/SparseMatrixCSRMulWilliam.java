package SparseRowCSR;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SparseMatrixCSRMulWilliam {
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

        public void printCSRDetails() {
            System.out.println("CSR Representation:");
            System.out.println("Values: " + Arrays.toString(values));
            System.out.println("Column Indices: " + Arrays.toString(columnIndices));
            System.out.println("Row Pointers: " + Arrays.toString(rowPointers));
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

            double[] resultValuesArray = resultValues.stream().mapToDouble(Double::doubleValue).toArray();
            int[] resultColumnIndicesArray = resultColumnIndices.stream().mapToInt(Integer::intValue).toArray();
            int[] resultRowPointersArray = resultRowPointers.stream().mapToInt(Integer::intValue).toArray();

            return new CSRMatrix(resultValuesArray, resultColumnIndicesArray, resultRowPointersArray, this.rows, B.cols);
        }
    }

    public static CSRMatrix loadMatrixFromMTX(String filename) throws IOException {
        List<Double> valuesList = new ArrayList<>();
        List<Integer> columnIndicesList = new ArrayList<>();
        List<Integer> rowPointersList = new ArrayList<>();

        int rows = 0, cols = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("%")) continue;
                String[] parts = line.trim().split("\\s+");
                if (rows == 0 && parts.length == 3) {
                    rows = Integer.parseInt(parts[0]);
                    cols = Integer.parseInt(parts[1]);
                    rowPointersList.add(0);
                } else {
                    int row = Integer.parseInt(parts[0]) - 1;
                    int col = Integer.parseInt(parts[1]) - 1;
                    double value = Double.parseDouble(parts[2]);

                    while (rowPointersList.size() <= row) {
                        rowPointersList.add(valuesList.size());
                    }

                    valuesList.add(value);
                    columnIndicesList.add(col);
                }
            }
        }
        while (rowPointersList.size() <= rows) {
            rowPointersList.add(valuesList.size());
        }

        double[] values = valuesList.stream().mapToDouble(Double::doubleValue).toArray();
        int[] columnIndices = columnIndicesList.stream().mapToInt(Integer::intValue).toArray();
        int[] rowPointers = rowPointersList.stream().mapToInt(Integer::intValue).toArray();

        return new CSRMatrix(values, columnIndices, rowPointers, rows, cols);
    }

    public static void main(String[] args) {
        try {
            CSRMatrix williamMatrix = loadMatrixFromMTX("C:\\Users\\cgsos\\Documents\\Tercero\\Big Data\\MatrixOptimization\\SparseMatrix\\mc2depi.mtx");


            // Medir tiempo de inicio
            long startTime = System.nanoTime();

            // Medir memoria antes de la multiplicación
            Runtime runtime = Runtime.getRuntime();
            runtime.gc();  // Solicitar la recolección de basura para liberar memoria
            long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

            System.out.println("\nMultiplying William Matrix by itself:");
            CSRMatrix resultMatrix = williamMatrix.multiply(williamMatrix);

            // Medir tiempo de finalización
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000;  // Convertir a milisegundos

            // Medir memoria después de la multiplicación
            long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
            long memoryUsed = memoryAfter - memoryBefore;


            System.out.println("\nTiempo de ejecución: " + duration + " ms");
            System.out.println("Memoria utilizada: " + memoryUsed / 1024 + " KB");
        } catch (IOException e) {
            System.out.println("Error reading matrix file: " + e.getMessage());
        }
    }
}
