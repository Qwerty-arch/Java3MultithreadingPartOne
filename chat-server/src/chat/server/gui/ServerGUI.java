package chat.server.gui;

import chat.server.core.ChatServer;
import chat.server.core.ChatServerListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, ChatServerListener {

    private static final int POS_X = 800;
    private static final int POS_Y = 200;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 300;

    private final ChatServer chatServer = new ChatServer((ChatServerListener) this);  // можем отдельны передать какой-то объект, но и можем простосказать, что "мы"
    private final JButton btnStart = new JButton("Start");
    private final JButton btnStop = new JButton("Stop");
    private final JPanel panelTop = new JPanel(new GridLayout(1, 2));
    private final JTextArea log = new JTextArea();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() { // Event Dispatching Thread
                new ServerGUI();
            }
        });
    }

    private ServerGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this); // устанавливаем свой диспетчер ошибок, чтобы ошибки попадали не в поток Error, к дефолтному обработчику, а к нам в GUI (дословно: установить диспетчер непойманых исключений по умолчанию();)
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(POS_X, POS_Y, WIDTH, HEIGHT);
        setResizable(false);                             //установили невозможность изменить размеры нашего окна
        setTitle("Chat server");
        setAlwaysOnTop(true);                            // всегда поверх всех окон
        log.setEditable(false);
        log.setLineWrap(true);
        JScrollPane scrollLog = new JScrollPane(log);
        btnStart.addActionListener(this);
        btnStop.addActionListener(this);

        panelTop.add(btnStart);                          // кнопка старт у сервера
        panelTop.add(btnStop);                           // кнопка стоп у сервера
        add(panelTop, BorderLayout.NORTH);
        add(scrollLog, BorderLayout.CENTER);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();                     // берем само событие и забираем источник, сохраняем в некий Object
        if (src == btnStart) {
            chatServer.start(8189);
        } else if (src == btnStop) {
//            throw new RuntimeException("Hello from EDT"); // напоминание можем поставить(что-бы потом заполнить это поле)
            chatServer.stop();
        } else {
            throw new RuntimeException("Unknown source: " + src);
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) { // наш собственный обрабочк всех исключений(метод из интерфейса)
        e.printStackTrace();
        String msg;
        StackTraceElement[] ste = e.getStackTrace();       // чтобы достать StackTrace исключение нам нужен массив из StackTrace элементов, его можно достать из Throwable
        msg = "Exception in " + t.getName() + " " +
                e.getClass().getCanonicalName() + ": " +
                e.getMessage() + "\n\t at " + ste[0];
        JOptionPane.showMessageDialog(this, msg, "Exception", JOptionPane.ERROR_MESSAGE); // диалоговое окно для оповещения пользователя
        System.exit(1); // закрывем окно
    }

    @Override
    public void onChatServerMessage(String msg) {   // когда на серваке происходит какой-то onChatServer с каким-то сообщением
        SwingUtilities.invokeLater(() -> {          // пишем в TextArea(выводим в log)
            log.append(msg + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        });
    }
}
