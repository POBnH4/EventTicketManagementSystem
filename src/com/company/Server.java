package com.company;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class Server extends Thread {

    private ServerSocket serversocket;
    private int thePortNo; //atomic maybe?
    private TicketedEvent theEvent;
    private boolean theStopFlag;
    private List<ConnectionHandler> connections;
    private List<String> ticketOrders;
    HistoryBuffer historyBuffer;
    AtomicInteger totalClients = new AtomicInteger(0);
    private ServerGUI serverGUI = ServerGUI.getInstance();
    private static Server server;

    public Server(int portNo, TicketedEvent event) {
        // share references to all the shared data
        server = Server.this;
        thePortNo = portNo;
        theEvent = event;
        // create data for managing server and set of connections
        theStopFlag = false;
        final String DEFAULT_TICKET_ORDER = "Ticket Reference - Customer Name - Number of Tickets";
        connections = Collections.synchronizedList(new ArrayList<>());
        ticketOrders = Collections.synchronizedList(new ArrayList<>());
        ticketOrders.add(DEFAULT_TICKET_ORDER);
        final int HISTORY_BUFFER_SIZE = 10; // should it be 10?
        this.historyBuffer = new HistoryBuffer(HISTORY_BUFFER_SIZE);

    }

    static Server getInstance() {
        return server;
    }

    void startServer() {
        this.start();
    }

    void stopServer() {
        theStopFlag = true;
    }

    int numberConnections() {
        return connections.size();
    }

    public void run() {
        try {
            serversocket = new ServerSocket(thePortNo);
            System.out.println("Server started");
            while (!theStopFlag) {
                // listen for a connection request on server socket
                // incoming is the connection socket
                Socket incoming = serversocket.accept();
                // start new thread to service client
                ConnectionHandler conn = new ConnectionHandler(this, incoming, theEvent);
                System.out.println("Connected to:" + incoming);
                conn.start();
                connections.add(conn);
                this.serverGUI.updateHistory();
                this.serverGUI.setUpTicketsRemaining();
            }
            serversocket.close();
        } catch (Exception e) {
            e.printStackTrace();
            final int CLOSE_PROGRAM = 1;
            System.exit(CLOSE_PROGRAM);
        }
    }

    public void addTicketOrder(String ticketOrder) {
        this.ticketOrders.add(ticketOrder);
    }

    public void setTheStopFlag(boolean theStopFlag) {
        this.theStopFlag = theStopFlag;
    }

    public boolean isTheStopFlag() {
        return theStopFlag;
    }

    public TicketedEvent getTheEvent() {
        return theEvent;
    }

    public void setTheEvent(TicketedEvent theEvent) {
        this.theEvent = theEvent;
    }

    public List<String> getTicketOrders() {
        return ticketOrders;
    }


}
