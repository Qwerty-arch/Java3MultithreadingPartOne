package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class SocketThread implements Runnable {//extends Thread {                                               // тут будет происходить взаимодействие // отдельный поток, ожидающий прилета строки(read() у него будет) и соответственно будет как-то записывать строки в ответный ему полу-сокет // !!!наш класс очень универсален!!!

    private final SocketThreadListener listener;
    private final Socket socket;
    private DataOutputStream out;
    private Thread t;
    private String name;

    public SocketThread(SocketThreadListener listener, String name, Socket socket) {
        //super(name);                                                                    // имя Socket
        this.socket = socket;                                                           // сам Socket
        this.listener = listener;
        this.name = name;
        t = new Thread(this, name);

    }

    public Thread getT() {
        return t;
    }

    @Override
    public void run() {
        try {
            listener.onSocketStart(this, socket);                               // Listener, SocketThread start
            DataInputStream in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            listener.onSocketReady(this, socket);                               // сообщаем listener, что мы готовы(сформировался этот метод благодоря многопоточности, out нам нужно сначала получить из Socket, а потом его использовать  в методе sendMessage)
            Thread cur = Thread.currentThread();
            while (!cur.isInterrupted()) {
                String msg = in.readUTF();
                listener.onReceiveString(this, socket, msg);
            }
        } catch (IOException e) {
            listener.onSocketException(this, e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                listener.onSocketException(this, e);                                    // сообщаем listener
            }
            listener.onSocketStop(this);
        }
    }

    public synchronized boolean sendMessage(String msg) {                               // нужно синхронизировать, т.к. мы не можем использовать out, пока мы его не достали из Socket // sendMessage просто сокету writeUTF
        try {
            out.writeUTF(msg);
            out.flush();
            return true;
        } catch (IOException e) {
            listener.onSocketException(this, e);
            close();
            return false;
        }
    }

    public synchronized void close() {                                                   // чтобы правильно закрыть
        Thread cur = Thread.currentThread();
        cur.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            listener.onSocketException(this, e);
        }
    }

}

