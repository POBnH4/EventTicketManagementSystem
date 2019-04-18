package com.company;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CM3113 Session 2018-19 Starting point for Coursework TicketedEvent.java
 * encapsulated properties of an event that has tickets allocated to customers
 */
public class TicketedEvent {

    private String eventName;
    private int capacity; // AtomicInteger double check
    private AtomicInteger numberTicketsRemaining; // add volatile to it as well?
    private AtomicBoolean theEventIsOpen;
    private ConcurrentHashMap<String, Ticket> tickets;

    TicketedEvent(String name, int cap) {
        this.eventName = name;
        this.capacity = cap;
        this.numberTicketsRemaining = new AtomicInteger(cap);
        this.theEventIsOpen = new AtomicBoolean(true);
        this.tickets = new ConcurrentHashMap<>();
    }

    synchronized String getEventName() {
        return eventName;
    }

    public synchronized void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public synchronized AtomicInteger getNumberTicketsRemaining() {
        return numberTicketsRemaining;
    }

    public synchronized ConcurrentHashMap<String, Ticket> getTickets() {
        return tickets;
    }

    public synchronized void addTicket(String contact, Ticket ticket) {
        this.tickets.put(contact, ticket);

    }

    synchronized Ticket getTicket(String contact, AtomicInteger number) {
        Ticket t = new Ticket(contact, number);
        this.tickets.put(contact, t);
        this.numberTicketsRemaining.getAndSet(this.numberTicketsRemaining.intValue() - number.get());
        //this.numberTicketsRemaining -= number; used to be like that
        return t;
    }

    synchronized AtomicBoolean isOpen() {
        return theEventIsOpen;
    }
}
