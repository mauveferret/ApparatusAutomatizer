package ru.mauveferret;

import java.io.*;
import java.net.Socket;

    public class Client {

        private static Socket clientSocket; //сокет для общения
        private static BufferedReader reader; // нам нужен ридер читающий с консоли, иначе как
        // мы узнаем что хочет сказать клиент?
        private static BufferedReader in; // поток чтения из сокета
        private static BufferedWriter out; // поток записи в сокет

        public static void main(String[] args) {
            try {
                try {
                    // адрес - локальный хост, порт - 4004, такой же как у сервера
                    clientSocket = new Socket("localhost", 4004); // этой строкой мы запрашиваем
                    //  у сервера доступ на соединение
                    reader = new BufferedReader(new InputStreamReader(System.in));
                    // читать соообщения с сервера
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    // писать туда же
                    out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                    String stop = "";
                    while (!stop.equals("stop")) {
                        System.out.println("Вы что-то хотели сказать? Введите это здесь:");
                        // если соединение произошло и потоки успешно созданы - мы можем
                        //  работать дальше и предложить клиенту что то ввести
                        // если нет - вылетит исключение
                        String word = reader.readLine(); // ждём пока клиент что-нибудь
                        stop = word;
                        // не напишет в консоль
                        System.out.println(word);
                        out.write(word + "\n"); // отправляем сообщение на сервер
                        out.flush();
                        String serverWord = in.readLine(); // ждём, что скажет сервер
                        System.out.println(serverWord); // получив - выводим на экран
                    }
                } finally { // в любом случае необходимо закрыть сокет и потоки
                    System.out.println("Клиент был закрыт...");
                    clientSocket.close();
                    in.close();
                    out.close();
                }
            } catch (IOException e) {
                System.err.println(e);
            }

        }
    }
