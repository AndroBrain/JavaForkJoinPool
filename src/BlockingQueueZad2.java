import java.util.LinkedList;
import java.util.Queue;

public class BlockingQueueZad2<E> {
    private final Queue<E> blockingQueue;
    public final int MAX_SIZE;

    public BlockingQueueZad2(int size) {
        MAX_SIZE = size;
        this.blockingQueue = new LinkedList<>();
    }

    public synchronized void put(E element) throws InterruptedException  {
        while (blockingQueue.size() == MAX_SIZE) {
            wait();
        }
        blockingQueue.add(element);
        notifyAll();
    }

    public synchronized E take() throws InterruptedException {
        while (blockingQueue.isEmpty()) {
            wait();
        }
        E element = blockingQueue.poll();
        notifyAll();
        return element;
    }
}
