package Java3MultithreadingPartOne;

public class Main {

    static Object monitor = new Object();
    static volatile char currentMsg = 'A';

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    synchronized (monitor) {
                        while (currentMsg != 'A') {
                            monitor.wait();
                        }
                        System.out.println("A");
                        currentMsg = 'B';
                        monitor.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    synchronized (monitor) {
                        while (currentMsg != 'B') {
                            monitor.wait();
                        }
                        System.out.println("B");
                        currentMsg = 'C';
                        monitor.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    synchronized (monitor) {
                        while (currentMsg != 'C') {
                            monitor.wait();
                        }
                        System.out.println("C");
                        currentMsg = 'A';
                        monitor.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

    }
}

