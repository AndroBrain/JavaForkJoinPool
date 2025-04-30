import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A visualization panel for rendering merge sort steps.
 * This class is responsible for all drawing operations related to visualizing
 * the parallel merge sort algorithm, with special focus on maintaining
 * consistent colors between tasks and their merges.
 */
public class MergeSortVisualizer extends JPanel {
    private int[] originalArray;
    private Map<Integer, MainVisualized.MergeStep> steps = new HashMap<>();
    private final Map<Integer, Point> nodePositions = new HashMap<>();
    private boolean showFinalResult = false;

    // Constants for visualization
    private static final int CELL_WIDTH = 30;
    private static final int CELL_HEIGHT = 20;
    private static final int INITIAL_Y = 80;
    private static final int LEVEL_HEIGHT = CELL_HEIGHT * 2;

    // Special level for merge operations
    private static final int MERGE_LEVEL_OFFSET = 3;

    public MergeSortVisualizer() {
        setPreferredSize(new Dimension(1000, 800));
        setBackground(Color.WHITE);
    }

    public void setOriginalArray(int[] array) {
        this.originalArray = array;
        this.showFinalResult = false;
        repaint();
    }

    public void updateSteps(Map<Integer, MainVisualized.MergeStep> steps) {
        synchronized (MainVisualized.lock) {
            this.steps = new HashMap<>(steps);
        }
        SwingUtilities.invokeLater(this::repaint);
    }

    public void displayResult() {
        this.showFinalResult = true;
        SwingUtilities.invokeLater(this::repaint);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (originalArray == null) return;

        g2d.setFont(new Font("Arial", Font.BOLD, 14));

        // Draw title
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Parallel Merge Sort Visualization", 50, 40);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));

        // Draw original array
        int startX = (getWidth() - originalArray.length * CELL_WIDTH) / 2;
        int startY = INITIAL_Y;

        g2d.drawString("Original Array:", 50, startY - 10);
        drawArray(g2d, originalArray, startX, startY);

        if (showFinalResult && !steps.isEmpty()) {
            // Get the final sorted array from the last merge step
            int[] sortedArray = findFinalSortedArray();

            if (sortedArray != null) {
                // Draw sorted array below the original array
                int sortedArrayY = startY + (CELL_HEIGHT + 10) * 2; // Add more spacing here
                g2d.drawString("Sorted Array:", 50, sortedArrayY - 10);
                drawArray(g2d, sortedArray, startX, sortedArrayY);

                // Draw the merge sort tree below the sorted array
                drawMergeSortTree(g2d, startX, sortedArrayY + CELL_HEIGHT + 40); // Adjust this spacing as needed
            }
        } else if (!steps.isEmpty()) {
            // Draw the current state of the merge sort tree below the original array
            drawMergeSortTree(g2d, startX, startY + (CELL_HEIGHT + 10) * 2); // Adjust this spacing as needed
        }
    }

    private int[] findFinalSortedArray() {
        for (MainVisualized.MergeStep step : steps.values()) {
            if (step.isMerge && step.start == 0 && step.end == originalArray.length - 1) {
                return step.array;
            }
        }
        return null;
    }

    private void drawArray(Graphics2D g2d, int[] array, int x, int y) {
        g2d.setStroke(new BasicStroke(2));
        for (int i = 0; i < array.length; i++) {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(x + i * CELL_WIDTH, y, CELL_WIDTH, CELL_HEIGHT);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x + i * CELL_WIDTH, y, CELL_WIDTH, CELL_HEIGHT);
            g2d.drawString(String.valueOf(array[i]), x + i * CELL_WIDTH + CELL_WIDTH / 2 - 10, y + CELL_HEIGHT / 2 + 5);
        }
    }

    private void drawMergeSortTree(Graphics2D g2d, int baseX, int baseY) {
        // Find root tasks (with no parents)
        List<Integer> rootTaskIds = findRootTasks();
        // Find merge operations
        List<Integer> mergeOperations = findMergeOperations();

        if (rootTaskIds.isEmpty()) return;

        // Clear position cache
        nodePositions.clear();

        // Calculate tree levels
        Map<Integer, List<Integer>> levels = new HashMap<>();
        calculateLevels(rootTaskIds, 0, levels);

        // Add merge operations to appropriate levels
        organizeMergeOperations(mergeOperations, levels);

        // Determine the maximum depth
        int maxLevel = levels.keySet().stream().max(Integer::compareTo).orElse(0);

        // Calculate positions for each node using baseX and baseY
        calculateNodePositions(levels, maxLevel, baseX, baseY);

        // Draw connections between nodes
        drawNodeConnections(g2d);

        // Draw special connections for merge operations
        drawMergeConnections(g2d);

        // Draw nodes
        drawNodes(g2d);
    }


    private List<Integer> findRootTasks() {
        List<Integer> rootTaskIds = new ArrayList<>();
        for (Map.Entry<Integer, MainVisualized.MergeStep> entry : steps.entrySet()) {
            if (entry.getKey() >= 0 && entry.getValue().parentIds.isEmpty()) {
                rootTaskIds.add(entry.getKey());
            }
        }
        return rootTaskIds;
    }

    private List<Integer> findMergeOperations() {
        List<Integer> mergeIds = new ArrayList<>();
        for (Map.Entry<Integer, MainVisualized.MergeStep> entry : steps.entrySet()) {
            if (entry.getKey() < 0 && entry.getValue().isMerge) {
                mergeIds.add(entry.getKey());
            }
        }
        return mergeIds;
    }

    private void organizeMergeOperations(List<Integer> mergeIds, Map<Integer, List<Integer>> levels) {
        // Build a map of original task IDs to their levels
        Map<Integer, Integer> taskToLevel = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : levels.entrySet()) {
            for (Integer nodeId : entry.getValue()) {
                taskToLevel.put(nodeId, entry.getKey());
            }
        }

        // Place merge operations at an appropriate level based on their parent tasks
        for (Integer mergeId : mergeIds) {
            MainVisualized.MergeStep mergeStep = steps.get(mergeId);

            if (mergeStep != null && !mergeStep.parentIds.isEmpty()) {
                // Find the level for this merge based on its parents' level
                int maxParentLevel = -1;
                for (Integer parentId : mergeStep.parentIds) {
                    Integer parentLevel = taskToLevel.get(parentId);
                    if (parentLevel != null && parentLevel > maxParentLevel) {
                        maxParentLevel = parentLevel;
                    }
                }

                // Place merge one level below its lowest parent
                int mergeLevel = maxParentLevel + 1 + MERGE_LEVEL_OFFSET;

                // Add the merge to its level
                if (!levels.containsKey(mergeLevel)) {
                    levels.put(mergeLevel, new ArrayList<>());
                }
                levels.get(mergeLevel).add(mergeId);
            }
        }
    }

    private void calculateLevels(List<Integer> currentLevelNodes, int level, Map<Integer, List<Integer>> levels) {
        if (currentLevelNodes.isEmpty()) return;

        // Add current nodes to this level
        if (!levels.containsKey(level)) {
            levels.put(level, new ArrayList<>());
        }
        levels.get(level).addAll(currentLevelNodes);

        // Get all children for the next level
        List<Integer> nextLevelNodes = new ArrayList<>();
        for (int nodeId : currentLevelNodes) {
            MainVisualized.MergeStep step = steps.get(nodeId);
            if (step != null) {
                nextLevelNodes.addAll(step.childIds);
            }
        }

        // Process next level
        if (!nextLevelNodes.isEmpty()) {
            calculateLevels(nextLevelNodes, level + 1, levels);
        }
    }


    private void calculateNodePositions(Map<Integer, List<Integer>> levels, int maxLevel, int baseX, int baseY) {
        int verticalOffset = 0;  // Initialize a vertical offset to space nodes out

        for (int level = 0; level <= maxLevel; level++) {
            List<Integer> levelNodes = levels.getOrDefault(level, new ArrayList<>());
            int nodesCount = levelNodes.size();
            int totalWidth = nodesCount * CELL_WIDTH * 2;
            int startNodeX = baseX + (getWidth() - totalWidth) / 2 - 500;

            for (int i = 0; i < nodesCount; i++) {
                int nodeId = levelNodes.get(i);
                MainVisualized.MergeStep step = steps.get(nodeId);
                if (step != null) {
                    int x = startNodeX + i * CELL_WIDTH * 2 + (CELL_WIDTH * step.array.length) / 2;
                    int y = baseY + verticalOffset + 100;  // Apply verticalOffset to prevent overlap
                    nodePositions.put(nodeId, new Point(x, y));
                }
            }

            verticalOffset += LEVEL_HEIGHT;  // Increment the offset for the next level
        }
    }


    private void drawNodeConnections(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(1.5f));
        for (Map.Entry<Integer, MainVisualized.MergeStep> entry : steps.entrySet()) {
            int nodeId = entry.getKey();

            // Skip merge operations (they'll be handled separately)
            if (nodeId < 0) continue;

            MainVisualized.MergeStep step = entry.getValue();

            // Draw connections to parent (regular tasks)
            for (int parentId : step.parentIds) {
                if (nodePositions.containsKey(nodeId) && nodePositions.containsKey(parentId)) {
                    Point childPos = nodePositions.get(nodeId);
                    Point parentPos = nodePositions.get(parentId);

                    g2d.setColor(steps.get(parentId).color);
                    g2d.drawLine(childPos.x, childPos.y, parentPos.x, parentPos.y);
                }
            }
        }
    }

    private void drawMergeConnections(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{8}, 0));

        for (Map.Entry<Integer, MainVisualized.MergeStep> entry : steps.entrySet()) {
            int nodeId = entry.getKey();

            // Only process merge operations (negative IDs)
            if (nodeId >= 0) continue;

            MainVisualized.MergeStep step = entry.getValue();

            // Draw dashed connections from parent tasks to merge result
            if (nodePositions.containsKey(nodeId)) {
                Point mergePos = nodePositions.get(nodeId);

                for (int parentId : step.parentIds) {
                    if (nodePositions.containsKey(parentId)) {
                        Point parentPos = nodePositions.get(parentId);

                        // Use the color of the original task for its merge
                        g2d.setColor(step.color);
                        g2d.drawLine(parentPos.x, parentPos.y, mergePos.x, mergePos.y);
                    }
                }
            }
        }
    }

    private void drawNodes(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(2));

        // Draw all task and merge nodes
        for (Map.Entry<Integer, Point> entry : nodePositions.entrySet()) {
            int nodeId = entry.getKey();
            Point position = entry.getValue();
            MainVisualized.MergeStep step = steps.get(nodeId);

            if (step != null) {
                // Add visual indicator for merge operations
                boolean isMerge = step.isMerge;

                drawArrayNode(g2d, step.array, position.x - (CELL_WIDTH * step.array.length) / 2,
                        position.y, step.color, isMerge);

                // Add label for merges
                if (isMerge) {
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Arial", Font.BOLD, 12));
                    g2d.drawString("Merge",
                            position.x - 20,
                            position.y - 5);
                }
            }
        }
    }

    private void drawArrayNode(Graphics2D g2d, int[] array, int x, int y, Color color, boolean isMerge) {
        // Create color variations based on whether this is a merge operation
        Color fillColor;
        Color strokeColor;

        if (isMerge) {
            // Make fill color slightly more saturated for merge operations
            fillColor = new Color(
                    Math.min(255, color.getRed() + 80),
                    Math.min(255, color.getGreen() + 80),
                    Math.min(255, color.getBlue() + 80)
            );
            // Use a more pronounced stroke color
            strokeColor = color.darker().darker();

            // Use thicker stroke for merge nodes
            g2d.setStroke(new BasicStroke(3));
        } else {
            fillColor = new Color(
                    Math.min(255, color.getRed() + 100),
                    Math.min(255, color.getGreen() + 100),
                    Math.min(255, color.getBlue() + 100)
            );
            strokeColor = color.darker();
            g2d.setStroke(new BasicStroke(2));
        }

        // Draw the array cells
        for (int i = 0; i < array.length; i++) {
            g2d.setColor(fillColor);
            g2d.fillRect(x + i * CELL_WIDTH, y, CELL_WIDTH, CELL_HEIGHT);
            g2d.setColor(strokeColor);
            g2d.drawRect(x + i * CELL_WIDTH, y, CELL_WIDTH, CELL_HEIGHT);
            g2d.setColor(Color.BLACK);
            g2d.drawString(String.valueOf(array[i]), x + i * CELL_WIDTH + CELL_WIDTH / 2 - 10, y + CELL_HEIGHT / 2 + 5);
        }

        // If it's a merge operation, add a highlight border around the whole array
        if (isMerge) {
            g2d.setColor(strokeColor);
            g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawRect(x - 3, y - 3, array.length * CELL_WIDTH + 6, CELL_HEIGHT + 6);
        }
    }
}