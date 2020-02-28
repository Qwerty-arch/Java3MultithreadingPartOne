package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketThread extends Thread {                                               // тут будет происходить взаимодействие // отдельный поток, ожидающий прилета строки(read() у него будет) и соответственно будет как-то записывать строки в ответный ему полу-сокет // !!!наш класс очень универсален!!!

    private final SocketThreadListener listener;
    private final Socket socket;
    private DataOutputStream out;

    public SocketThread(SocketThreadListener listener, String name, Socket socket) {
        super(name);                                                                    // имя Socket
        this.socket = socket;                                                           // сам Socket
        this.listener = listener;
        start();
    }

    @Override
    public void run() {
        try {
            listener.onSocketStart(this, socket);                               // Listener, SocketThread start
            DataInputStream in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            listener.onSocketReady(this, socket);                               // сообщаем listener, что мы готовы(сформировался этот метод благодоря многопоточности, out нам нужно сначала получить из Socket, а потом его использовать  в методе sendMessage)
            while (!isInterrupted()) {
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
        interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            listener.onSocketException(this, e);
        }
    }

}
