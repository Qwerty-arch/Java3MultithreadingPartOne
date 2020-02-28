package chat.server.core;

import chat.common.Library;
import network.SocketThread;
import network.SocketThreadListener;

import java.net.Socket;

public class ClientThread extends SocketThread {        // у нас было общее поведение - это SocketThread(он у нас умелл что-там коннектится, умел как-то взаимодействовать с другим SocketThread и тд и это всё хорошо и нас это устраивает, но теперь нам нужна СПЕЦИФИКА ДЛЯ ЧАТА, нам нужно наделить этот SocketThread какими-то свойствами, чтобы не делать сам SocketThread специфичным - мы делаем наследника)
    private String nickname;                            // делаем ему свойство String с nickname
    private boolean isAuthorized;                       // и boolean с авторизацией
    private boolean isReconnecting;                     // кто-то может быть в состоянии реконнекта

    public boolean isReconnecting() {
        return isReconnecting;
    }   // getter

    void reconnect() {
        isReconnecting = true;          // тут мы говорим, что мы реконектимся, для отображения в других частях
        close();                        // сокет - ты реконектишься
    }


    public ClientThread(SocketThreadListener listener, String name, Socket socket) { super(listener, name, socket); }   // делаем соответствующий конструктор, который всё получает

    public String getNickname() {
        return nickname;
    }           // getter fo nickname

    public boolean isAuthorized() {
        return isAuthorized;
    }     // обычный getter авторизации

    void authAccept(String nickname) {                   // Мы скажем клиенту, что он авторизован
        isAuthorized = true;                             // клиент ставит себе isAuthorized true
        this.nickname = nickname;                        // клиент ставит себе nickname
        sendMessage(Library.getAuthAccept(nickname));    // client поылает сообщение в SocketThread о том, что он Accept (getAuthAccept)
    }                                                    // т.е. у нас же всё ещё сталась пара сокетов Server Client, о туда и от туда вылетел сокет и соединились сокеты, сдесь сокет авторизовался, сделал AuthRequest, получил AuthAccept и отправил сообщение своей паре о том, что AuthAccept, таким образом message приходит клиенту, о том, что ты был авторизован, если AuthFail, то соответсвенно мы посылаем сообщение о том, что AuthFail и закрываем наш сокет с нашей стороны т.е. сервак - ну фейл. ну и до свидания. Наклиенте в этот момент происходит разрыв соединения, клиент не смог авторизоваться

    void authFail() {
        sendMessage(Library.getAuthDenied());
        close();
    }

    void msgFormatError(String msg) {
        sendMessage(Library.getMsgFormatError(msg));
        close();                        // закрывает сокет
    }
}
