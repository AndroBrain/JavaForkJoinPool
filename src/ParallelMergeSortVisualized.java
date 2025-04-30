import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ParallelMergeSortVisualized {
    private static MergeSortVisualizer visualizer;
    private static final Random random = new Random();
    private static int counter = 0;
    static final Object lock = new Object();
    private static final Map<Integer, MergeStep> steps = new HashMap<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ParallelMergeSortVisualized::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Parallel Merge Sort Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        visualizer = new MergeSortVisualizer();
        frame.add(visualizer, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        JButton startButton = new JButton("Start Sorting");
        controlPanel.add(startButton);

        startButton.addActionListener((ActionEvent event) -> {
            startButton.setEnabled(false);
            new Thread(() -> {
                int size = 8;
                int[] array = createRandomArray(size);

                // Clear previous steps
                synchronized (lock) {
                    counter = 0;
                    steps.clear();
                }

                // Initialize visualization with the original array
                visualizer.setOriginalArray(array);

                // Start sorting
                sortInPool(Arrays.copyOf(array, array.length), new ForkJoinPool(4));

                // Show final result
                visualizer.displayResult();

                SwingUtilities.invokeLater(() -> startButton.setEnabled(true));
            }).start();
        });

        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.setSize(1000, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void sortInPool(int[] array, ForkJoinPool pool) {
        MergeSortTask sortTask = new MergeSortTask(array);
        long start = System.currentTimeMillis();
        pool.invoke(sortTask);
        long end = System.currentTimeMillis();
        System.out.println(pool + " isSorted: " + isSorted(array) + " it took " + (end - start) + " millis.");
    }

    static class MergeStep {
        int[] array;
        int start;
        int end;
        Color color;
        java.util.List<Integer> parentIds = new java.util.ArrayList<>();
        java.util.List<Integer> childIds = new java.util.ArrayList<>();
        boolean isMerge;

        MergeStep(int[] array, int start, int end, Color color, boolean isMerge) {
            this.array = Arrays.copyOfRange(array, start, end + 1);
            this.start = start;
            this.end = end;
            this.color = color;
            this.isMerge = isMerge;
        }
    }

    static class MergeSortTask extends RecursiveAction {
        private final int[] array;
        private final int start;
        private final int end;
        private static final int THRESHOLD = 1;
        private final Color taskColor;
        private final int taskId;

        public MergeSortTask(int[] array) {
            this(array, 0, array.length - 1);
        }

        private MergeSortTask(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
            this.taskColor = generateRandomColor();

            synchronized (lock) {
                this.taskId = counter++;
                // Record the initial state
                MergeStep step = new MergeStep(array, start, end, taskColor, false);
                steps.put(taskId, step);
            }
        }

        private Color generateRandomColor() {
            return new Color(
                    random.nextInt(200) + 55,
                    random.nextInt(200) + 55,
                    random.nextInt(200) + 55
            );
        }

        @Override
        protected void compute() {
            if (start >= end) {
                return;
            }

            if (end - start < THRESHOLD) {
                Arrays.sort(array, start, end + 1);
                return;
            }

            int mid = start + (end - start) / 2;

            // Create subtasks
            MergeSortTask leftTask = new MergeSortTask(array, start, mid);
            MergeSortTask rightTask = new MergeSortTask(array, mid + 1, end);

            // Record relationships between tasks
            synchronized (lock) {
                MergeStep parentStep = steps.get(taskId);
                parentStep.childIds.add(leftTask.taskId);
                parentStep.childIds.add(rightTask.taskId);

                steps.get(leftTask.taskId).parentIds.add(taskId);
                steps.get(rightTask.taskId).parentIds.add(taskId);
            }

            // Execute subtasks
            invokeAll(leftTask, rightTask);

            // Merge results
            merge(array, start, mid, end);

            // Record merge step
            synchronized (lock) {
                MergeStep mergeStep = new MergeStep(array, start, end, taskColor, true);
                mergeStep.parentIds.add(leftTask.taskId);
                mergeStep.parentIds.add(rightTask.taskId);
                steps.put(-taskId - 1, mergeStep); // Negative ID to distinguish merge steps
            }

            // Update visualization
            visualizer.updateSteps(steps);
            try {
                Thread.sleep(500); // Pause to show the step
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
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

    private static int[] createRandomArray(int size) {
        Random random = new Random();
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(100);
        }
        return array;
    }

    private static boolean isSorted(int[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
                return false;
            }
        }
        return true;
    }
}