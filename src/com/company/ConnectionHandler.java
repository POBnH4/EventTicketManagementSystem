package com.company;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CM3113 Session 2018-19 Starting point for Coursework
 * ConnectionHandler.java communicates with client to perform ticket transaction
 */
public class ConnectionHandler extends Thread {

    private Server server;
    private Socket incoming;
    private BufferedReader theInput;
    private PrintWriter theOutput;
    private TicketedEvent theEvent;
    private boolean go = true;

    /**
     * Creates a new instance of Class
     */
    ConnectionHandler(Server server, Socket incoming, TicketedEvent event) {
        this.server = server;
        this.incoming = incoming;
        this.theEvent = event;
    }

    public void close() {
        go = false;
    }

    private synchronized void sendToClient(String s) {
        theOutput.println(s);
    }

    public void run() {
        final String DOUBLE_DOTS_DELIMITER = ":";

        String remoteIPAddress = incoming.getLocalAddress().getHostName() + DOUBLE_DOTS_DELIMITER + incoming.getLocalPort();
        LocalDateTime bidDateTime = LocalDateTime.now();

        final String CONNECTED_TO_TICKET_SERVER_MESSAGE = "You are connected to ticket server: "
                + remoteIPAddress + " at date / time " + bidDateTime + " \n";

        final String EVENT_MESSAGE = "EVENT"
                + "," + theEvent.getNumberTicketsRemaining()
                + "," + theEvent.getEventName();

        try {
            // set up streams for bidirectional transfer across connection socket
            theInput = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
            theOutput = new PrintWriter(incoming.getOutputStream(), true /* auto flush */);
            // acknowledge connection
            sendToClient(CONNECTED_TO_TICKET_SERVER_MESSAGE);

            if (theEvent.isOpen().get()) {
                sendToClient(EVENT_MESSAGE);
            }

            while (go) {
                if (theEvent.isOpen().get()) {
                    // read bid line and confirmation line
                    String line = theInput.readLine().trim();
                    System.out.println(line);
                    if (line.length() > 0) {
                        processMessage(line);
                    }
                    incoming.close();
                }
                theOutput.flush();
            }
            theInput.close();
            theOutput.close();
            incoming.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void processMessage(String message) {

        final String COMMA_DELIMITER = ",", TYPE_ENQUIRE = "ENQUIRE", SOLD_OUT = "SOLDOUT";
        final String NEW_LINE_DELIMITER = "\n", TYPE_ORDER = "ORDER";
        final String UPDATE_MESSAGE = "UPDATE"
                + "," + theEvent.getNumberTicketsRemaining()
                + "," + theEvent.getEventName();
        final String DASH_DELIMITER = " - ";

        String[] bits = message.split(COMMA_DELIMITER);
        String processedMessage;

        if (bits[0].toUpperCase().equals(TYPE_ORDER)) {
            AtomicInteger numberTicketsRequested = new AtomicInteger(Integer.parseInt(bits[1]));
            String contactName = bits[2];
            if (numberTicketsRequested.get() <= theEvent.getNumberTicketsRemaining().get()) {
                Ticket ticket = theEvent.getTicket(contactName, numberTicketsRequested);

                final String REPLY_MESSAGE = "Ticket issued to: " + contactName
                        + " for " + numberTicketsRequested + " people "
                        + incoming.getInetAddress().getHostName()
                        + " on port " + incoming.getPort()
                        + " at " + LocalDateTime.now();

                final String CONFIRM_MESSAGE = "CONFIRM"
                        + "," + ticket.getTicketRef()
                        + "," + ticket.getNumberOfPeople()
                        + "," + ticket.getContactName();

                final String TICKET_ORDERED = ticket.getTicketRef()
                        + DASH_DELIMITER + ticket.getContactName()
                        + DASH_DELIMITER + ticket.getNumberOfPeople();

                this.server.addTicketOrder(TICKET_ORDERED);

                System.out.println(REPLY_MESSAGE);
                processedMessage = REPLY_MESSAGE + NEW_LINE_DELIMITER + CONFIRM_MESSAGE;
                server.historyBuffer.add(processedMessage + NEW_LINE_DELIMITER + "Time:" + LocalDate.now()); // add to history buffer monitor;
                sendToClient(CONFIRM_MESSAGE);

            } else {
                sendToClient(SOLD_OUT);
                processedMessage = SOLD_OUT;
            }
            go = false;

        } else if (bits[0].toUpperCase().equals(TYPE_ENQUIRE)) {
            sendToClient(UPDATE_MESSAGE);

//            TODO DO I need this part here for the JComboBox?
//            final String TICKET_UPDATE = "";
//            this.server.addTicketOrder(TICKET_UPDATE);

            processedMessage = UPDATE_MESSAGE;
        } else {
            System.out.println(message);
            processedMessage = message;
        }
    }
}
