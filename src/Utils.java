import java.awt.*;
import java.util.Random;

public class Utils {
    public static int THRESHOLD = 100;
    public static final Random random = new Random();

    public static int[] createRandomArray(int size) {
        Random random = new Random();
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(size);
        }
        return array;
    }

    public Color generateRandomColor() {
        return new Color(
                random.nextInt(200) + 55,
                random.nextInt(200) + 55,
                random.nextInt(200) + 55
        );
    }

}
