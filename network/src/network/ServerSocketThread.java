package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerSocketThread extends Thread {                                                            // тут будет происходить взааимодействие // ServerSocketThread будет отдельным потоком сервера, который будет "фабрикой Socket"(фактически будет генерировать Socket(-ы)) // это поток, который слушает какой-то порт

    private int port;
    private int timeout;
    ServerSocketThreadListener listener;                                                                    // что-бы нас кто-то слушал

    public ServerSocketThread(ServerSocketThreadListener listener, String name, int port, int timeout) {
        super(name);
        this.port = port;
        this.timeout = timeout;
        this.listener = listener;                                                                           // у нас должен быть слушатель событий, которые мы генерим
        start();
    }

    @Override
    public void run() {
        listener.onServerStart(this);                                                               // listener, я start
        try (ServerSocket server = new ServerSocket(port)) {                                               // передаем порт // сервер будет ожидать входящее соединение по accept,
            server.setSoTimeout(timeout);                                                                  // устанавливаем у ServerSocket TimeOut, это означет, что мы конечно же будем в accept ожидать подключение, но переодически, когда у нас наступает TimeOut нас будет отпускать из этого accept для дальнейшего прохождения(естественно мы зациклимся, но иногда будет отпускать на проверку флага "!isInterrupted()" с эти самым TimeOut)
            listener.onServerSocketCreated(this, server);                                           // listener, server created
            while (!isInterrupted()) {                                                                     // будем ожидать бесконечное кол-во клиентов(while), пока его не прервут
                Socket socket;                                                                             // создали экземпляр
                try {
                    socket = server.accept();                                                              // вызываем accept
                } catch (SocketTimeoutException e) {
                    listener.onServerTimeout(this, server);                                         // слушатель, у меня (this) случилось исключение(e)
                    continue;
                }
                listener.onSocketAccepted(this, server, socket);                                    // listener , у меня Socked Accepted, передаем у кого, у меня (this), у моего сервака server, этот Socket
            }
        } catch (IOException e) {
            listener.onServerException(this, e);
        } finally {
            listener.onServerStop(this);
        }
    }
}
