package network;

import java.net.ServerSocket;
import java.net.Socket;

public interface ServerSocketThreadListener {                                               // односторонний, т.е. у нас есть только события сервера // содержит инфу о сервере
    void onServerStart(ServerSocketThread thread);                                          // сервер старт
    void onServerStop(ServerSocketThread thread);                                           // сервер стоп
    void onServerSocketCreated(ServerSocketThread thread, ServerSocket server);             // сервер говорит, что он создал Socket
    void onServerTimeout(ServerSocketThread thread, ServerSocket server);                   // thread, в котором произошёл этот TimeOut и Server, на котором произошёл этот TimeOut
    void onSocketAccepted(ServerSocketThread thread, ServerSocket server, Socket socket);   // Сервер говорит, что он подключил Socket// есть ServerSocketThread, есть Socket, который создался и сервер
    void onServerException(ServerSocketThread thread, Throwable exception);                 // Сервер говорит произошло исключение // тут Thread, который есть и сам Exception
}
