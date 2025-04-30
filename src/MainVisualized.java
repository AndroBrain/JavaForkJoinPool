import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class MainVisualized {
    private static MergeSortVisualizer visualizer;
    private static final Random random = new Random();
    private static int counter = 0;
    static final Object lock = new Object();
    private static final Map<Integer, MergeStep> steps = new HashMap<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainVisualized::createAndShowGUI);
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
        MergeSortTaskWithVisualization sortTask = new MergeSortTaskWithVisualization(array);
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

    static class MergeSortTaskWithVisualization extends RecursiveAction {
        private final int[] array;
        private final int start;
        private final int end;
        private final Color taskColor;
        private final int taskId;

        public MergeSortTaskWithVisualization(int[] array) {
            this(array, 0, array.length - 1);
        }

        private MergeSortTaskWithVisualization(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
            this.taskColor = Utils.generateRandomColor();

            synchronized (lock) {
                this.taskId = counter++;
                // Record the initial state
                MergeStep step = new MergeStep(array, start, end, taskColor, false);
                steps.put(taskId, step);
            }
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

            // Create subtasks
            MergeSortTaskWithVisualization leftTask = new MergeSortTaskWithVisualization(array, start, mid);
            MergeSortTaskWithVisualization rightTask = new MergeSortTaskWithVisualization(array, mid + 1, end);

            // Record relationships between tasks
            synchronized (lock) {
                MergeStep parentStep = steps.get(taskId);
                parentStep.childIds.add(leftTask.taskId);
                parentStep.childIds.add(rightTask.taskId);

                steps.get(leftTask.taskId).parentIds.add(taskId);
                steps.get(rightTask.taskId).parentIds.add(taskId);
            }

            invokeAll(leftTask, rightTask);

            Utils.merge(array, start, mid, end);

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