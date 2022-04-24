package com.codegym.task.task30.task3008;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        int serverPort = ConsoleHelper.readInt();

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {

            System.out.println("Server is running.");

            while (true) {

                Socket socket = serverSocket.accept();

                Handler handler = new Handler(socket);
                handler.start();

            }

        } catch (Exception exc) {
            System.out.println("An error occurred while trying to get a server connection.");
        }


    }


    private static class Handler extends Thread {

        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }


        public void run() {
            String userName;
            Connection connection;

            do {
                try {
                    ConsoleHelper.writeMessage("New connection was established with a remote address " + socket.getRemoteSocketAddress());
                    connection = new Connection(socket);
                } catch (Exception ig) {
                    break;
                }
                try {
                    userName = serverHandshake(connection);
                } catch (Exception ig) {
                    try {
                        connection.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                try {
                    sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                    notifyUsers(connection, userName);
                    serverMainLoop(connection, userName);
                } catch (Exception ignored) {
                } finally {
                    connectionMap.remove(userName);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                    try {
                        connection.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            } while (false);
            ConsoleHelper.writeMessage("Connection closed: " + socket.getRemoteSocketAddress());
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {

            while (true) {

                connection.send(new Message(MessageType.NAME_REQUEST));

                Message response = connection.receive();

                if (response.getType() == MessageType.USER_NAME) {

                    String data = response.getData();

                    if (data == null || data.equals(" ") || data.isEmpty() || connectionMap.containsKey(data)) {
                    } else {

                        connectionMap.put(data, connection);

                        connection.send(new Message(MessageType.NAME_ACCEPTED));

                        return data;

                    }


                }

            }

        }

        private void notifyUsers(Connection connection, String userName) throws IOException {

            for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {

                String name = entry.getKey();

                if (!name.equals(userName)) {

                    Message message = new Message(MessageType.USER_ADDED, name);

                    connection.send(message);

                }
            }


        }

        private void serverMainLoop(Connection connection, String userName) throws IOException,
                ClassNotFoundException {

            while (true) {

                Message receive = connection.receive();

                if (receive.getType() != MessageType.TEXT)
                    ConsoleHelper.writeMessage("An error occured while trying to modify the received message data.");

                else {
                    String message = userName + ": " + receive.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT, message));

                }


            }

        }


    }


    public static void sendBroadcastMessage(Message message) {

        try {

            for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {

                Connection con = entry.getValue();

                con.send(message);

            }


        } catch (IOException exc) {
            System.out.println("Message couldn't be sent.");
        }

    }


}