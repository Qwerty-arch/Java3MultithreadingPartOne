package network;

import java.net.Socket;

public interface SocketThreadListener {
    void onSocketStart(SocketThread thread, Socket socket);
    void onSocketStop(SocketThread thread);

    void onSocketReady(SocketThread thread, Socket socket);                  // SocketThread готов
    void onReceiveString(SocketThread thread, Socket socket, String msg);    // получение строки

    void onSocketException(SocketThread thread, Exception exception);
}
