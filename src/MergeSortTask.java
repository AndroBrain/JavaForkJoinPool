import java.util.Arrays;
import java.util.concurrent.RecursiveAction;

class MergeSortTask extends RecursiveAction {
    private final int[] array;
    private final int start;
    private final int end;

    public MergeSortTask(int[] array) {
        this(array, 0, array.length - 1);
    }

    private MergeSortTask(int[] array, int start, int end) {
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
        MergeSortTask leftTask = new MergeSortTask(array, start, mid);
        MergeSortTask rightTask = new MergeSortTask(array, mid + 1, end);

        invokeAll(leftTask, rightTask);
        merge(array, start, mid, end);
    }

    private void merge(int[] arr, int start, int mid, int end) {
        int n1 = mid - start + 1;
        int n2 = end - mid;

        int[] leftArray = new int[n1];
        int[] rightArray = new int[n2];

        System.arraycopy(arr, start, leftArray, 0, n1);
        System.arraycopy(arr, mid + 1, rightArray, 0, n2);

        int i = 0, j = 0;
        int k = start;

        while (i < n1 && j < n2) {
            if (leftArray[i] <= rightArray[j]) {
                arr[k] = leftArray[i];
                i++;
            } else {
                arr[k] = rightArray[j];
                j++;
            }
            k++;
        }

        while (i < n1) {
            arr[k] = leftArray[i];
            i++;
            k++;
        }

        while (j < n2) {
            arr[k] = rightArray[j];
            j++;
            k++;
        }
    }
}
