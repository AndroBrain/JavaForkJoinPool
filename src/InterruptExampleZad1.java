
public class InterruptExampleZad1 {
    public static void main(String[] args) {
        Thread interruptedThread = new Thread(() -> {
           try {
               Thread.sleep(5_000);
           } catch (InterruptedException e) {
               System.out.println("I've been interrupted");
           }
        });

        Thread interruptingThread = new Thread(() -> {
            try {
                Thread.sleep(2_000);
                interruptedThread.interrupt();
            } catch (InterruptedException e) {

            }
        }, "interruptingThread");

        interruptedThread.start();
        interruptingThread.start();
    }
}
