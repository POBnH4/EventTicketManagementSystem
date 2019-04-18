package com.company;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client extends Thread {

    private int thePortNo;
    private volatile boolean connected = false;
    private ClientGUI gui;
    private BufferedReader theInput;
    private PrintWriter theOutput;

    public Client(int portNo) {
        thePortNo = portNo;
    }

    public Client(int portNo, ClientGUI g) {
        thePortNo = portNo;
        gui = g;
    }

    void sendTicketRequest(String name, int num) {

        final String message = "ORDER," + num + "," + name;
        final String CONNECTED_MESSAGE = "Send order for " + num + " from " + name;
        final String NOT_CONNECTED_MESSAGE = "Not connected. Cant send: " + message;
        final long SLEEP_DELAY = 50L;

        while(!this.connected){
            try {
                Thread.sleep(SLEEP_DELAY);
            } catch (InterruptedException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (this.connected) {
            gui.updateText(CONNECTED_MESSAGE);
            theOutput.println(message);
            theOutput.flush();
        } else {
            System.out.println(NOT_CONNECTED_MESSAGE);
        }
    }

    public void stopClient() throws IOException {
        final String STOP_CLIENT_MESSAGE = "BYE";
        theOutput.println(STOP_CLIENT_MESSAGE);
        theOutput.close();
        theInput.close();
        connected = false;
    }

    public void run() {
        try {
            final String NAME_OF_LOCAL_HOST = "localhost";
            Socket socket = new Socket(NAME_OF_LOCAL_HOST, thePortNo);
            connected = true;
            theInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            theOutput = new PrintWriter(socket.getOutputStream(), true /* auto flush */);

            while (connected) {
                String message = theInput.readLine();
                if (message != null) {
                    processMessage(message);
                }
            }
            socket.close();
        } catch (Exception e) {
            final int EXIT_PROGRAM_STATUS = 0;
            final String EXCEPTION_MESSAGE = "Oh no";
            e.printStackTrace();
            System.out.println(EXCEPTION_MESSAGE + e.toString());
            System.exit(EXIT_PROGRAM_STATUS);
        }
    }

    private void processMessage(String message) {

        final String COMMA_DELIMITER = ",", CONFIRM_CASE = "CONFIRM";
        final String UPDATE_CASE = "UPDATE", EVENT_CASE = "EVENT";
        final String SOLD_OUT_CASE = "SOLDOUT", CONFIRM_TICKER_MESSAGE = "Confirmed Ticket Ref:";
        final String NUMBER_OF_TICKETS_MESSAGE = " Number of tickets: ", CUSTOMER_MESSAGE = " Customer: ";
        final String SOLD_OUT_MESSAGE = "Event is sold out";

        String[] bits = message.split(COMMA_DELIMITER);
        switch (bits[0].toUpperCase()) {
            case CONFIRM_CASE:
                gui.updateText( CONFIRM_TICKER_MESSAGE + bits[1]
                        + NUMBER_OF_TICKETS_MESSAGE + bits[2]
                        + CUSTOMER_MESSAGE + bits[3]);
                System.out.println(message);
                break;
            case UPDATE_CASE:
                gui.updateText(message);
                System.out.println(message);
                break;
            case EVENT_CASE:
                gui.updateEventDetails(bits[1], bits[2]);
                System.out.println(message);
                break;
            case SOLD_OUT_CASE:
                gui.updateText(SOLD_OUT_MESSAGE);
                System.out.println(message);
                break;
            default:
                gui.updateText(message);
                System.out.println(message);
                break;
        }
    }

    public boolean isConnected() {
        return connected;
    }
}
