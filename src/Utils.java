import java.awt.*;
import java.util.Random;

public class Utils {
    public static int THRESHOLD = 1;
    public static final Random random = new Random();

    public static int[] createRandomArray(int size) {
        Random random = new Random();
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(size);
        }
        return array;
    }

    public static Color generateRandomColor() {
        return new Color(
                random.nextInt(200) + 55,
                random.nextInt(200) + 55,
                random.nextInt(200) + 55
        );
    }

    public static void merge(int[] arr, int start, int mid, int end) {
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
