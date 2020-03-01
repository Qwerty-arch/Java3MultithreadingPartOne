package chat.server.core;

import chat.common.Library;
import network.ServerSocketThread;
import network.ServerSocketThreadListener;
import network.SocketThread;
import network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer implements ServerSocketThreadListener, SocketThreadListener {

    private final ChatServerListener listener;                  // создаем listener от и добавляем в конструктор, далее в putLog
    private ServerSocketThread server;
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss: ");
    private Vector<SocketThread> clients = new Vector<>();      // Те сокеты, которые мы создаем, нужно складывать в список, т.е. у нас может подключится много клиентов и нус должен быть спиоок клиентов// создаем потоко-безопасный аналог эррей-листа// вектор с клиентами, в вектор мы складываем сокет-треды, т.е. все те треды, которые МЫ с вами, как сервак, нагенерили с НАШЕЙ стороны, это сокет-треды НАШИ, которые мы сгенерили и они будет соеденины с теми сокет-тредыми, которые на клиенте. Это НЕ сокет-треды клиентов. Это сокет-треды, которые сгенерил наш сервак
    // у нас существовала большая проблема, что внутри vector у нас лежат как авторизованные клиенты, так и не авторизованные - это плохо // Как узнать авторизован поток или нет ? => никак , т.к. потому что ServerSocket, также как и SocketThread - эти все вещи у нас отвязаны от нашего чата, они вообще нечего не знают о том, чтоо у нас будут какие-то там nickname или ещё что-то такое  // Как выйти из этой ситуации ? => нам нужен свой собственный класс, который будет символизировать нашего подключенного пользователя с набором флажков (# является он там авторизованым или нет, поле с nickname добавить ему и тд)
    private SocketThread socketThread;
    private ExecutorService executorService = Executors.newCachedThreadPool();;                      ////////////////////////////////////////////////
    public ChatServer(ChatServerListener listener) {
        this.listener = listener;
    }

    public void start(int port) {
        if (server != null && server.isAlive())          /////////////////////////////////////////  if (server != null && server.t.isAlive())                                                          // Метод isAlive() возвращает логическое значение true, если поток, для кото­рого он вызван, еще исполняется.
            putLog("Server already started");
        else {
            server = new ServerSocketThread(this, "Server", port, 2000);
        }
    }

    public void stop() {
        if (server == null || !server.isAlive()) {                      //////////////////////////!server.t.isAlive()
            putLog("Server is not running");
        } else {
            server.interrupt();   //////////////////////////////////////////server.t.interrupt();
        }
    }

    private void putLog(String msg) {
        msg = DATE_FORMAT.format(System.currentTimeMillis()) +          // формирует сообщение
                Thread.currentThread().getName() + ": " + msg;
        listener.onChatServerMessage(msg);                              // Кидаем теперь не System.out.println, а listener// когда происходит что-то в лог, мы кидаем это listener // просто отдаем это какому-то слушателю внаружу
    }

    /**
     * Server methods
     */

    @Override
    public void onServerStart(ServerSocketThread thread) {  // по старту сервака
        putLog("Server thread started");
        SqlClient.connect();                                // подключаемся к базе
    }

    @Override
    public void onServerStop(ServerSocketThread thread) {   // по остановке сервака
        putLog("Server thread stopped");
        SqlClient.disconnect();                             // отключаемся от базы
        for (int i = 0; i < clients.size(); i++) {          // бежим по всем клиентам
            clients.get(i).close();                         // делаем close
        }

    }

    @Override
    public void onServerSocketCreated(ServerSocketThread thread, ServerSocket server) {
        putLog("Server socket created");
    }

    @Override
    public void onServerTimeout(ServerSocketThread thread, ServerSocket server) {
//        putLog("Server timeout");  //убрали, т.к. каждые 2 сек будет кричать, что сервер TimeOut
    }

    @Override
    public void onSocketAccepted(ServerSocketThread thread, ServerSocket server, Socket socket) {  // ServerSocketThread thread - где всё произошло зачачатие Socket, ServerSocket server - кто зачал Socket, Socket socket - результат этого зачатия
        putLog("Client connected");
        String name = "SocketThread " + socket.getInetAddress() + ":" + socket.getPort();
        //new ClientThread(this, name, socket);                                              // ((SocketThread поменяли на ClienThread))передаем нас, имя, и сокет в который нужно будет обурнуть наш этот поток // получается, что на каждое новое соединение у нас генерится новый поток // создаем ClientThread
        executorService.submit(new ClientThread(this, name, socket));          /////////////////////////////
    }

    @Override
    public void onServerException(ServerSocketThread thread, Throwable exception) {
        exception.printStackTrace();
    }

    /**
     * Socket methods
     */

    @Override
    public synchronized void onSocketStart(SocketThread thread, Socket socket) {
        putLog("Socket created");   // synchronized мы добавили на будущее, # у нас много putLog и выполнение прервется другим потоком на середине, # между putLog8 putLog9
    }

    @Override
    public synchronized void onSocketStop(SocketThread thread) {
        ClientThread client = (ClientThread) thread;                     // (ClientThread) thread - к ClientThread приведенный клиент просто, который прилетел
        clients.remove(thread);                                          // из листа клиентов убираем
        if (client.isAuthorized() && !client.isReconnecting() ) {        // Если клиент, которого мы remove был авторизован И не реконнектятся
            sendToAuthClients(Library.getTypeBroadcast("Server",    // то нужно послать всем авторизованным клиентам сообщение из бибилотеки типа broadcast от сервера, что клиент getnickname + "disconnected"
                    client.getNickname() + " disconnected"));
        }
        sendToAuthClients(Library.getUserList(getUsers()));              // посылаем всем авторизованным клиентам сообщение о том, что "юзер лист" у нас поменялся   // убрали его из if, т.к. чем чаще мы будем синхронить наш "юзер лист", тем лучше
    }

    @Override
    public synchronized void onSocketReady(SocketThread thread, Socket socket) {
        clients.add(thread);
    }   // добавляем клиента мы делаев в onSocketReady (thread - который к нам прилеетел)

    @Override
    public synchronized void onReceiveString(SocketThread thread, Socket socket, String msg) {          // разослать сообщение ВСЕМ нашим клиенам   // метод получения строки
        ClientThread client = (ClientThread) thread;
        // если нам прислал кто-то не авторизованный, то это сообщение нужно игнорировать
        if (client.isAuthorized()) {            // если тот, кто прилал сообщение авторизован
            handleAuthMessage(client, msg);     // послать всем авторизованным людям
        } else {
            handleNonAuthMessage(client, msg);  // надо как-то хендлить неавторизованных чуваков
        }
    }

    private void handleNonAuthMessage(ClientThread client, String msg) {    // в хендлере сдесь мы не авторизованных пользователь должны описать процесс login. Т.к. первое - что должен сделать пользователь - это авторизоватя // процесс авторизации мы должны сдесь описать // как только мы авторизовались мы можем слать кому-то там чего-то, если логин файл, то нижняя панелька не появляется // вылетает exception, что извините, ваш логин / пароль неправильные
        String[] arr = msg.split(Library.DELIMITER);                        // берем стринг arr и msg split по library.DELIMITER, по DELIMITER разобрали наше сообщение
        if (arr.length != 3 || !arr[0].equals(Library.AUTH_REQUEST)) {      // в нашем протоколе написано, что сообщение авторизации должно быть длинной в три слова(arr.length != 3, т.е. префикс, логин и пароль) ИЛИ в arr[0] не лежит library.AUTH_REQUEST, то это вообще-то не сообщение об авторизации(не запрос авторизации )
            client.msgFormatError(msg);                                     // клиенту говорим, что если вы прислали это мне, то это скорее всего НЕ сообщение об авторизации
            return;                                                         // а я очень сильно жду сообщения об авторизации, поэтому формат неправильный => return
        }
        // если это собщение ОБ авторизации, то мы можем смело заходить прямо в ячейки
        String login = arr[1];                                      // логин - это ячейка первая
        String password = arr[2];                                   // пароль - это ячейка вторая
        String nickname = SqlClient.getNickname(login, password);   // получаем у SqlClient.getNickname из логина и пароля
        if (nickname == null) {                                     // если полсле всех этих манипуляций nickname = null
            putLog("Invalid login attempt: " + login);        // для себя залогировали
            client.authFail();                                      // authFail посылаем человеку
            return;                                                 // из метода возвращаемся
        } else {
            ClientThread oldClient = findClientByNickname(nickname);                                      // клиента ищем по nickname // нам нужно посмотреть, есть ли такой же клиент
            client.authAccept(nickname);                                                                  // если всё хорошо, то Accept(мы говорим всем клиентам о том, что он авторизован) // авторизовали у нового клиента, всё хорошо ему сделали в любом случае
            if (oldClient == null) {
            sendToAuthClients(Library.getTypeBroadcast("Server", nickname + " connected")); // всем посылаем сообщение, что новый участник connected // "Server" - источник
            } else {
                oldClient.reconnect();                                                                   // реконекктим
                clients.remove(oldClient);                                                               // remove нашего старого клента    // если старого нашли, то надо старого отконектить
            }
        }

    }

    private void handleAuthMessage(ClientThread client, String msg) {
        String[] arr = msg.split(Library.DELIMITER);                        // сплитим сообщение пользователя по Delimetr
        String msgType = arr[0];                                            // префикс
        switch (msgType) {                                                  // могут быть разные варианты msg Type
            case Library.TYPE_BCAST_CLIENT:                                 // общее сообщение авторизованного пользователя всем остальным
                sendToAuthClients(Library.getTypeBroadcast(                 // мы должны всем авторизованным клиентам послать сообщение, но не полностью // броадкас сообщение от nickname + само сообщение (arr[1])
                        client.getNickname(), arr[1]));
                break;
            default:
                client.sendMessage(Library.getMsgFormatError(msg));          // сдесь не в коем случае не может быть эксепшн, если клиент послал чушь, то сервак падать не должен!Не ронять же по каждому проблемному клиенту сервак // логично послать клиенту обратно сообщение
        }
    }
// launch4j

    private void sendToAuthClients(String msg) {                    // послать всем АВТОРИЗОВАННЫМ клиентам
        for (int i = 0; i < clients.size(); i++) {                  // пробегаемся по всем клиентам
            ClientThread client = (ClientThread) clients.get(i);    // чтобы только авторизованным
            if (!client.isAuthorized()) continue;                   // если не авторизованный клиент, то нечего не делать
            client.sendMessage(msg);
        }
    }

    @Override
    public synchronized void onSocketException(SocketThread thread, Exception exception) {
        exception.printStackTrace();
    }

    private String getUsers() {                                      // как собрать всех пользователей в одну кучку
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            sb.append(client.getNickname()).append(Library.DELIMITER);
        }
        return sb.toString();
    }

    private synchronized ClientThread findClientByNickname(String nickname) {       // понять, есть ли такой пользователь уже в системе
        for (int i = 0; i < clients.size(); i++) {                                  // метод бежит по всему вектру с клиентами
            ClientThread client = (ClientThread) clients.get(i);                    // ищет такого же клиента
            if (!client.isAuthorized()) continue;
            if (client.getNickname().equals(nickname))                              // с таким же nickname
                return client;
        }
        return null;
    }
}