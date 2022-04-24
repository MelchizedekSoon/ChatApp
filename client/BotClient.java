package com.codegym.task.task30.task3008.client;

import com.codegym.task.task30.task3008.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BotClient extends Client {

    public static void main(String[] args) {
        new BotClient().run();
    }

    public class BotSocketThread extends Client.SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Hello, there. I'm a bot. I understand the following commands: date, day, month, year, time, hour, minutes, seconds.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if (message.contains(":")) {
                String[] split = message.split(": ");
                if (split.length != 2) {
                    return;
                }
                String sender = split[0];
                message = split[1];
                String outputMessage = "Information for " + sender + ": ";
                Date currentTime = Calendar.getInstance().getTime();
                SimpleDateFormat df = null;
                switch (message) {
                    case "date":
                        df = new SimpleDateFormat("d.MM.YYYY");
                        break;
                    case "day":
                        df = new SimpleDateFormat("d");
                        break;
                    case "month":
                        df = new SimpleDateFormat("MMMM");
                        break;
                    case "year":
                        df = new SimpleDateFormat("YYYY");
                        break;
                    case "time":
                        df = new SimpleDateFormat("H:mm:ss");
                        break;
                    case "hour":
                        df = new SimpleDateFormat("H");
                        break;
                    case "minutes":
                        df = new SimpleDateFormat("m");
                        break;
                    case "seconds":
                        df = new SimpleDateFormat("s");
                        break;
                }
                if (df != null) {
                    outputMessage += df.format(currentTime);
                    BotClient.this.sendTextMessage(outputMessage);
                }
            }
        }
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        int X = (int) (Math.random() * 100);
        return "date_bot_" + X;
    }

}
