package chat.common;

public class Library { // опишем внутри library проток, иными словами "формат сообщения. по которому мы будем однозначно понимать, какой тип у этого сообщения" // решили описать как в irk(internrt relei chat) был такой когда-то
    /*
    /auth_request±login±password        // нам нужн запрос авторизации  // сдесь будет логин и пароль
    /auth_accept±nickname               // нам нужно, чтобы у нас было подтверждение авторизации    // возвращатся будет nickname
    /auth_error                         // нам нужно, чтобы у нас была Ошибка авторизации
    /broadcast±msg                      // нам нужно, чтобы большие broadcast сообщения(т.е. сообщения, которые будут посылатся всем)
    /msg_format_error±msg               // нам нужен свой аналог Exception  // если мы на сервере или на клиенте не смогли разобрать что это за сообщение // возвращаем сообщение
    /user_list±user1±user2±user3±....   // Думаем, как мы будем синхронить юзеров ?Придумали некое служебное сообщение
    * */
    public static final String DELIMITER = "±";                          // нам нужен некий разделитель // это какой-то паттерн, по которому мы будем отделять параметры нашего сообщения от , сообственно, сообщения и префикс сообщения от параметров // проще всего использовать пробел, но тогаа при broadcast сообщениях, если мы будем внутри broadcast сообщения давать через пробел bradcast msg и само сообщение тоже будет содежать пробелы, то у нас будет всё-всё-всё делиться // тут нужно брать либо какой-то редкий символ, либо какой-то набор из редких символов, редкая штука, которая будет являтся разделителем для нашего чата, Естественно если внутри сообщения будет содержатся этот набор, то у нас всё сломается, это не безопасно => либо этот набор должен быть очень-очень уникальным, случайно сгенерированным, либо надо придумывать что-то ещё
   // у нас есть строки, которые символизируют смысл сообщения
    public static final String AUTH_REQUEST = "/auth_request";           // запрос авторизации
    public static final String AUTH_ACCEPT = "/auth_accept";             // accept авторизации
    public static final String AUTH_DENIED = "/auth_denied";             // denied авторизации
    public static final String MSG_FORMAT_ERROR = "/msg_format_error";   // msg_format_error
    // если мы вдруг не поняли, что за сообщение и не смогли разобрать
    public static final String TYPE_BROADCAST = "/bcast";                // broadcast
    // то есть сообщение, которое будет посылаться всем
    public static final String TYPE_BCAST_CLIENT = "/client_msg";           // сообщение клиента
    public static final String USER_LIST = "/user_list";                    // нам нужна константа "юзер лист", что-бы создать сообщение

    public static String getTypeBcastClient(String msg) { return TYPE_BCAST_CLIENT + DELIMITER + msg; }   // getter для сообщения клиента

    public static String getUserList(String users) { return USER_LIST + DELIMITER + users; }       // Сразу формируем строку, не массив, из которго нужно достать строку, а сразу формируем строку

    // есть геттеры, которые формотируют для нас "такое" сообщение // т.е. для того, чтобы полслать от клиента правильный запрос авторизации мы должны взять getAuthRequest передать туда логи и пароль и нам вернется строка с правильным запросом на авторизацию для него сервераа, наш сервер в свою очередь будет принимать эту строчку и знать что она "такого-то" формата, сможет соответсвенно её разобратье
    // сдесь мы можем сколько угодно геттеров поставить # захотели приватные сообщения посылать => пишем typePrivateMessage, придумывем формат этого самого typePrivateMessage, придумываем ему гетер, всё работает. # захотели груповые сообщения посылать, придумали формат и по аналогии
    public static String getAuthRequest(String login, String password) {
        return AUTH_REQUEST + DELIMITER + login + DELIMITER + password;
    }

    public static String getAuthAccept(String nickname) {
        return AUTH_ACCEPT + DELIMITER + nickname;
    }

    public static String getAuthDenied() {
        return AUTH_DENIED;
    }

    public static String getMsgFormatError(String message) {
        return MSG_FORMAT_ERROR + DELIMITER + message;
    }

    public static String getTypeBroadcast(String src, String message) {
        return TYPE_BROADCAST + DELIMITER + System.currentTimeMillis() +
                DELIMITER + src + DELIMITER + message;
    }

}
