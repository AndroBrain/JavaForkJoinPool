import java.util.Arrays;
import java.util.concurrent.RecursiveAction;

class MergeSortTaskConsole extends RecursiveAction {
    private final int[] array;
    private final int start;
    private final int end;

    public MergeSortTaskConsole(int[] array) {
        this(array, 0, array.length - 1);
    }

    private MergeSortTaskConsole(int[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }

    @Override
    protected void compute() {
        if (start >= end) {
            return;
        }

        if (end - start < Utils.THRESHOLD) {
            Arrays.sort(array, start, end + 1);
            return;
        }

        int mid = start + (end - start) / 2;
        MergeSortTaskConsole leftTask = new MergeSortTaskConsole(array, start, mid);
        MergeSortTaskConsole rightTask = new MergeSortTaskConsole(array, mid + 1, end);

        invokeAll(leftTask, rightTask);
        Utils.merge(array, start, mid, end);
    }
}
