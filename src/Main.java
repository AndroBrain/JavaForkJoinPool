import java.util.Queue;

public class Main {
    public static void main(String[] args) {
        new Thread(() -> System.out.println("Hello from thread")).start();
        new Queue<>()
    }
}

