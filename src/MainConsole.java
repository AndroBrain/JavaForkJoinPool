
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

public class MainConsole {
    public static void main(String[] args) {
        int SIZE = 100_000_000;
        int[] array = Utils.createRandomArray(SIZE);
        runTests(array);
        Utils.THRESHOLD = 10;
        runTests(array);
        Utils.THRESHOLD = 1;
        runTests(array);
    }

    private static void runTests(int[] array) {
        sortInPool(Arrays.copyOf(array, array.length), ForkJoinPool.commonPool());
        sortInPool(Arrays.copyOf(array, array.length), new ForkJoinPool());
        sortInPool(Arrays.copyOf(array, array.length), new ForkJoinPool(1));
        sortInPool(Arrays.copyOf(array, array.length), new ForkJoinPool(2));
        sortInPool(Arrays.copyOf(array, array.length), new ForkJoinPool(4));
        sortInPool(Arrays.copyOf(array, array.length), new ForkJoinPool(8));
        sortInPool(Arrays.copyOf(array, array.length), new ForkJoinPool(16));
        sortInPool(Arrays.copyOf(array, array.length), new ForkJoinPool(32));
        sortInPool(Arrays.copyOf(array, array.length), new ForkJoinPool(64));
        sortInPool(Arrays.copyOf(array, array.length), new ForkJoinPool(128));
    }

    private static void sortInPool(int[] array, ForkJoinPool pool) {
        MergeSortTask sortTask = new MergeSortTask(array);
        long start = System.currentTimeMillis();
        pool.invoke(sortTask);
        long end = System.currentTimeMillis();
        formatToCSV(pool, end - start);
    }

    private static void formatToCSV(ForkJoinPool pool, long time) {
        System.out.println(Utils.THRESHOLD + "," + pool.getParallelism() + "," + pool.getStealCount() + "," + time);
    }
}
