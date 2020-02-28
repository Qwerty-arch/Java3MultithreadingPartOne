package chat.server.core;

import java.sql.*;

public class SqlClient {

        private static Connection connection;       // нам нужен объект соединения, чтобы соединяться
        private static Statement statement;         // того, чтобы отправлять нашей базе данных какие-то запросы, получать какие-то ответы, нам понадобится объект Statement - это объект, который символизирует все запросы и может как-то по разному формировать эти запросы

        synchronized static void connect() {        // SqlClient должен уметь коннектится к базе, static - т.к. зачем нам объект
            try {                                   // из wiki от том, как работать с SQL
                Class.forName("org.sqlite.JDBC");   // Нам нужно упомянуть наш драйвер JDBC, чтобы его положило в память // для этого используется конструкция  Class.forName // внутри org.JDBC есть пустой конструкторя, а статическая секция не пустая // т.е. что нам надо сделать фактически нам надо зарегестрировать наш драйвер в нашей программе, виртуальной машине, что делают за нас автоматически в статической секции этого драйвера // т.е. это сделано автоматически за нас, посредством вызова Class.forName // т.е. мы не просто создаем новый объект, а регистрируем его таким образом(тема java 3)
                connection = DriverManager.getConnection("jdbc:sqlite:chat-server/chat.db");    // для того, чтобы как-то использовать этот драйвер нам нужно создать с ним соединение(точнее соединение с базой данных) // DriverManager (драйверменеджер) getConnection (дай мне пожалуйста connection) в документации мы можем посмотреть в каком формате нужно давать эту штуку(в каком формате нужно запрашиваь соединение с базой данных для конкретного драйвера) // jdbc:sqlite: база и будет она находится внутри -> chat-server/ и будет называться -> chat.db // эта конструкция - она стандартная - не только для sqlite
                statement = connection.createStatement();                                           // важно, чтобы объект Statement был внутри, забираем мы его у connection // теперь мы говы подключатся к базе данных
            } catch (ClassNotFoundException | SQLException e) {
                throw new RuntimeException(e);
            }
        }

        synchronized static void disconnect() {         // SqlClient должен уметь дисконектится от базы
            try {
                connection.close();                     // отключаемся от базы данных
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        synchronized static String getNickname(String login, String password) {                                             // SqlClient должен уметь getNickname из login and password // основу этого метода будет составлять "запрос"
            String query = String.format("select nickname from users where login='%s' and password='%s'", login, password); // наш запрос практически анологен, как в SQlLite, select nickname где логин равен 'тому-то', paassword равен 'тому-то' // передаем логин, передаем пароль // наша строка с запросом
            try (ResultSet set = statement.executeQuery(query)) {                                                           // Как получить результат ? Результат можно получить у Statement execute query // т.е. выполнив некий запрос, наш  с вами // execute query может плеваться исключениями // в случае успеха execute query вернет нам странную вещь, вернет нам ResultSet, т.е. не смотря на то, что мы подрозумеваем, что у нас будет только один результат, любой возврат сведений из базы данных - это как-бы набор из результатов(множество), даже если в этом множестве всего 1 строчка - это всё равно всегда множество результатов
                if (set.next())                                                                                             // если у set есть что-то следующее, т.е. если set не пустой
                    return set.getString(1);                                                                    // возвращаем из set строку, которая находится прямо в самой первой (1) колонке. !!!!!!!!!!!!!!!!!!В SQL нумерация идёт не с 0, а с 1 !!!!!!!!!!!!!!!!!!!!!! # берем nickname - только одна колонка и нам нужно взять строковое значение(понятно, что тут всё будет строки) и к счастью ResultSet умеет преобразовывать эти данные (# getInt, getFloat и тд)
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;                                                                                                    // если нечего нам не вернулось, если в set нет next, то возвращаем null => авторизация не пройдена
        }

    }


