package testBenchmark;

import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)


public class BlockMultiplicationMatrixBenchmark {
    @Param({"10", "100", "1000"})
    int n;
    static double[][] a;
    static double[][] b;
    static double[][] c;

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
    public void testBlockMultiplication() {
        int blockSize = 32; // Tamaño del bloque, ajustable según el tamaño de la caché y la matriz
        for (int i = 0; i < n; i += blockSize) {
            for (int j = 0; j < n; j += blockSize) {
                for (int k = 0; k < n; k += blockSize) {
                    for (int ii = i; ii < Math.min(i + blockSize, n); ii++) {
                        for (int jj = j; jj < Math.min(j + blockSize, n); jj++) {
                            for (int kk = k; kk < Math.min(k + blockSize, n); kk++) {
                                c[ii][jj] += a[ii][kk] * b[kk][jj];
                            }
                        }
                    }
                }
            }
        }
    }
}
