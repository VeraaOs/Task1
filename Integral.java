public class Integral {
    public static final double A = 0.0;
    public static final double B = Math.PI;
    public static final int INTERVALS = 10000000;
    public static final int THREADS = 6;
    public static final int ITEMS_PER_THREAD = INTERVALS / THREADS;

    public static double easy(double x) {
        return x + 1.0;
    }

    public static double hard(double x) {
        var sum = 0.0;
        for (int k = 1; k <= 30; k++) {
            var t = k * x;
            sum += Math.sin(t) * Math.cos(t / 1000.0) * Math.sqrt(Math.abs(t)) * Math.exp(-t / 100.0) / k;
        }
        return sum;
    }

    public static Thread taskThread(int n, int[] schedule, double[] results, double a, double dx, int func) {
        return new Thread(() -> {
            var start = schedule[n];
            var finish = schedule[n] + ITEMS_PER_THREAD;
            var acc = 0.0;

            if (func == 0) {
                for (int i = start; i < finish; i++) {
                    acc += easy(a + (i + 0.5) * dx);
                }
            } else {
                for (int i = start; i < finish; i++) {
                    acc += hard(a + (i + 0.5) * dx);
                }
            }

            results[n] = acc;
        });
    }

    public static double measureSequential(int func, double a, double dx) {
        var start = System.nanoTime();
        var acc = 0.0;

        if (func == 0) {
            for (int i = 0; i < INTERVALS; i++) {
                acc += easy(a + (i + 0.5) * dx);
            }
        } else {
            for (int i = 0; i < INTERVALS; i++) {
                acc += hard(a + (i + 0.5) * dx);
            }
        }

        var result = acc * dx;
        var finish = System.nanoTime();

        System.out.println("Sequential result");
        System.out.println(result);
        System.out.println("Sequential time (ms)");
        var ms = (double) (finish - start) / 1000000;
        System.out.println(ms);
        return ms;
    }

    public static double measureP(int func, double a, double dx) throws InterruptedException {
        var threadsStart = new int[THREADS];
        var threads = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            threadsStart[i] = i * ITEMS_PER_THREAD;
        }

        double[] results = new double[THREADS];
        var pStart = System.nanoTime();

        for (int i = 0; i < THREADS; i++) {
            threads[i] = taskThread(i, threadsStart, results, a, dx, func);
        }
        for (int i = 0; i < THREADS; i++)
            threads[i].start();
        for (int i = 0; i < THREADS; i++)
            threads[i].join();

        var pResult = 0.0;
        for (int i = 0; i < THREADS; i++) {
            pResult += results[i];
        }
        pResult *= dx;

        var pFinish = System.nanoTime();
        System.out.println("Parallel result");
        System.out.println(pResult);
        System.out.println("Parallel time (ms)");
        var ms = (double) (pFinish - pStart) / 1000000;
        System.out.println(ms);
        return ms;
    }

    public static void main(String[] args) throws InterruptedException {
        var a = A;
        var dx = (B - A) / INTERVALS;

        System.out.println("Easy function");
        var seqEasy = measureSequential(0, a, dx);
        var parEasy = measureP(0, a, dx);
        System.out.println("Speedup");
        System.out.println(seqEasy / parEasy);

        System.out.println("Hard function");
        var seqHard = measureSequential(1, a, dx);
        var parHard = measureP(1, a, dx);
        System.out.println("Speedup");
        System.out.println(seqHard / parHard);
    }
}