package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.awt.EventQueue.invokeLater;
import static java.lang.String.valueOf;

public class ServerGUI extends JFrame implements ActionListener {

    private JTextField currentTimeField;
    private JButton addNewEventButton;
    private JButton startServerButton;
    private JButton stopServerButton;
    private JTextField eventNameField;
    private JTextField eventCapacityField;
    private JTextPane connectionHistory;
    private JTextField ticketsRemaining;
    private JTextField totalCustomers;
    private JComboBox<String> ticketsInformation;
    private JPanel mainPanel;
    private Timer timer, timerTwo;
    private TicketedEvent ticketedEvent;

    private static ServerGUI serverGUI;
    private Server server = Server.getInstance();

    public static void main(String[] args) {
        invokeLater(ServerGUI::new);
    }

    private ServerGUI() {
        serverGUI = ServerGUI.this;
        init();
        buttonsListeners();
        setUpTimer();
        setUpTicketInformation();
        setUpTicketInformationTimer();
    }

    static ServerGUI getInstance() {
        return serverGUI;
    }

    private void init() {
        final int FRAME_WIDTH = 600, FRAME_HEIGHT = 500;
        final String FRAME_NAME = "Server";
        final String DEFAULT_TICKET_INFORMATION = "Ticket Reference - Customer Name - Number of Tickets";
        final JFrame frame = new JFrame(FRAME_NAME);
        frame.setContentPane(this.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setVisible(true);

        this.currentTimeField.setEditable(false);
        this.ticketsRemaining.setEditable(false);
        this.totalCustomers.setEditable(false);
        this.ticketsInformation.addItem(DEFAULT_TICKET_INFORMATION);
        ((JLabel) this.ticketsInformation.getRenderer()).setHorizontalAlignment(JLabel.CENTER); // align the text in the center;
        this.ticketsInformation.setSelectedItem(DEFAULT_TICKET_INFORMATION);
        connectionHistory.setBorder(BorderFactory.createLineBorder(Color.black));
    }

    private void setNewEvent() {
        String newEventName = this.eventNameField.getText();
        final int NO_CUSTOMERS = 0, DEFAULT_EVENT_CAPACITY = 10;
        int newEventCapacity = 0;
        try {
            final int EVENT_CAPACITY = Integer.parseInt(this.eventCapacityField.getText());

            if (EVENT_CAPACITY > NO_CUSTOMERS) newEventCapacity = EVENT_CAPACITY;
            else newEventCapacity = DEFAULT_EVENT_CAPACITY;

        } catch (Exception e) {
            e.printStackTrace();
        }

        this.ticketedEvent = new TicketedEvent(newEventName, newEventCapacity);
        final int PORT = 8189;
        this.server = new Server(PORT, this.ticketedEvent);

        this.ticketsRemaining.setText(valueOf(newEventCapacity));
        this.totalCustomers.setText(valueOf(NO_CUSTOMERS));
        server.totalClients.set(NO_CUSTOMERS);

        //set name and capacity fields to be NON-editable;
        this.eventNameField.setEditable(false);
        this.eventCapacityField.setEditable(false);
    }

    private void setUpTicketInformationTimer(){
        final int TIMER_DELAY = 10000; // once every 10 seconds;

        timerTwo = new Timer(TIMER_DELAY, e -> {
            setUpNumberOfCustomers();
            System.out.println("Updated ticket information");
        });
    }

    private void setUpTicketInformation() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        Object[] array = server.getTicketOrders().toArray();
        Arrays.stream(array)
                .map(Object::toString)
                .forEach(model::addElement);
        ticketsInformation.setModel(model);
    }

    private void setUpNumberOfCustomers() {
        this.totalCustomers.setText(server.totalClients.toString());

    }

    void setUpTicketsRemaining() {
        final String TOTAL_REMAINING_TICKETS = String.valueOf(this.server.getTheEvent().getNumberTicketsRemaining().get());
        this.ticketsRemaining.setText(TOTAL_REMAINING_TICKETS);
    }

    private void setUpTimer() {

        final int TIMER_DELAY = 100; // once every 0.1 of a second;
        final String PATTERN = "HH:mm:ss";
        final DateTimeFormatter TIMER_PATTERN = DateTimeFormatter.ofPattern(PATTERN);
        timer = new Timer(TIMER_DELAY, e -> {
            currentTimeField.setText(LocalTime.now().format(TIMER_PATTERN));
            serverGUI.setUpNumberOfCustomers();
        });
    }

    private void buttonsListeners() {
        this.addNewEventButton.addActionListener(e -> setNewEvent());
        this.startServerButton.addActionListener(e -> startServer());
        this.stopServerButton.addActionListener(e -> stopServer());
    }

    private void stopServer() {
        this.server.stopServer();
        this.timer.stop();

        this.eventNameField.setEditable(true);
        this.eventCapacityField.setEditable(true);

        final String CONNECTION_HISTORY_STOP_SERVER_DEFAULT_TEXT = "\nServer stopped: \n Date: " + LocalDate.now() + " \nTime: " + LocalTime.now();

        this.connectionHistory.setText(CONNECTION_HISTORY_STOP_SERVER_DEFAULT_TEXT);

        Object[] array = server.getTicketOrders().toArray();
        String clientsDetails = Arrays.stream(array).map(o -> o + " \n").collect(Collectors.joining());
        this.connectionHistory.setText("\n\nSummary\n");
        this.connectionHistory.setText("\nTickets issued: " + ticketsRemaining.getText());
        this.connectionHistory.setText("\nTickets unissued: " + ticketsRemaining.getText());
        this.connectionHistory.setText(clientsDetails);

        setUpTicketInformation();
    }

    private void startServer() {
        final String CONNECTION_HISTORY_START_SERVER_DEFAULT_TEXT = "\nServer started: \n Date: " + LocalDate.now() + " \nTime: " + LocalTime.now();

        this.connectionHistory.setText(CONNECTION_HISTORY_START_SERVER_DEFAULT_TEXT);
        this.server.startServer();
        this.timer.start();
        this.timerTwo.start();

    }

    private int getNumberOfConnections() {
        return this.server.numberConnections();
    }

    void updateHistory() {

        final String CURRENT_TEXT = connectionHistory.getText();
        final String NEW_LINE_DELIMITER = "\n", LINE_SEPARATOR = "\n- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -";

        connectionHistory.setText(CURRENT_TEXT
                + NEW_LINE_DELIMITER
                + server.historyBuffer.remove() /* returns what was added(i.e. last action) */
                + LINE_SEPARATOR);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }
}
