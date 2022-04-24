package com.codegym.task.task30.task3008.client;

import com.codegym.task.task30.task3008.Connection;
import com.codegym.task.task30.task3008.Message;
import com.codegym.task.task30.task3008.MessageType;
import com.codegym.task.task30.task3008.ConsoleHelper;

import java.io.IOException;
import java.net.Socket;

public class Client {
    public Connection connection;
    private volatile boolean clientConnected = false;

    protected String getServerAddress() {
        System.out.println("Enter Server Address");

        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        System.out.println("Enter Server Port");

        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        System.out.println("Enter User Name");

        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String message) {
        try {
            Message msg = new Message(MessageType.TEXT, message);
            connection.send(msg);
        } catch (IOException e) {
            clientConnected = false;
        }
    }

    public void run() {
        SocketThread thread = getSocketThread();
        thread.setDaemon(true);
        thread.start();

        try {
            synchronized (this) {
                wait();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("An error occurred during the wait of main method. Exiting the program.");
            return;
        }

        if (clientConnected) {
            System.out.println("Connection established. To exit, enter 'exit'");
        } else
            System.out.println("An error occurred while working with the client.");

        while (clientConnected) {
            String message = ConsoleHelper.readString();
            if (message.equals("exit"))
                return;

            if (shouldSendTextFromConsole())
                sendTextMessage(message);
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }


    public class SocketThread extends Thread {

        public void run() {
            String serverAddress = getServerAddress();
            int serverPort = getServerPort();
            try {
                java.net.Socket socket = new Socket(serverAddress, serverPort);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(clientConnected = false);
            }
        }

        protected void processIncomingMessage(String message) {
            System.out.println(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            System.out.println("User " + userName + " Has joined");
        }

        protected void informAboutDeletingNewUser(String userName) {
            System.out.println("User " + userName + " is deleted");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (!clientConnected) {
                MessageType message = connection.receive().getType();
                if (message != MessageType.NAME_REQUEST && message != MessageType.NAME_ACCEPTED)
                    throw new IOException("Unexpected MessageType");
                if (message.equals(MessageType.NAME_REQUEST))
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                if (message.equals(MessageType.NAME_ACCEPTED)) {
                    notifyConnectionStatusChanged(true);
                    return;
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message msg = connection.receive();

                if (msg.getType() != MessageType.TEXT && msg.getType() != MessageType.USER_REMOVED && msg.getType() != MessageType.USER_ADDED) {
                    throw new IOException("Unexpected MessageType");
                }

                if (msg.getType().equals(MessageType.TEXT)) {
                    processIncomingMessage(msg.getData());
                }
                if (msg.getType().equals(MessageType.USER_ADDED)) {
                    informAboutAddingNewUser(msg.getData());
                }
                if (msg.getType().equals(MessageType.USER_REMOVED)) {
                    informAboutDeletingNewUser(msg.getData());
                }
            }
        }

    }
}