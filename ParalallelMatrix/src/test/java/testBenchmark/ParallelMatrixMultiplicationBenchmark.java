package testBenchmark;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.*;
import java.util.Random;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class ParallelMatrixMultiplicationBenchmark {

    private double[][] a;
    private double[][] b;
    private double[][] c;

    @Param({"10", "100", "1000"}) // Define los tamaños de las matrices a probar
    private int n;

    @Setup(Level.Trial)
    public void setup() {
        a = new double[n][n];
        b = new double[n][n];
        c = new double[n][n];

        Random random = new Random();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                a[i][j] = random.nextDouble();
                b[i][j] = random.nextDouble();
                c[i][j] = 0;
            }
        }
    }

    @Benchmark
    @Fork(1)
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)  // Tiempo de warmup
    @Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)  // Medición de rendimiento
    public void benchmarkMatrixMultiplication() throws InterruptedException {
        parallelMatrixMultiply(a, b, c);
    }

    // Método de multiplicación de matrices en paralelo usando ExecutorService
    public void parallelMatrixMultiply(double[][] a, double[][] b, double[][] c) throws InterruptedException {
        int numThreads = Runtime.getRuntime().availableProcessors();
        int blockSize = n / numThreads;

        // ExecutorService para gestionar el pool de hilos
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        // Dividir las filas de la matriz en bloques para procesarlas en paralelo
        for (int i = 0; i < numThreads; i++) {
            final int rowStart = i * blockSize;
            final int rowEnd = (i == numThreads - 1) ? n : (i + 1) * blockSize;

            executorService.submit(() -> {
                for (int iRow = rowStart; iRow < rowEnd; iRow++) {
                    for (int j = 0; j < n; j++) {
                        for (int k = 0; k < n; k++) {
                            c[iRow][j] += a[iRow][k] * b[k][j];
                        }
                    }
                }
                latch.countDown(); // Decrementar el contador del latch cuando termine este hilo
            });
        }

        latch.await();  // Esperar a que todos los hilos terminen
        executorService.shutdown();  // Finalizar el ExecutorService
    }
}
