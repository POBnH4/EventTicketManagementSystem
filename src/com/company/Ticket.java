package com.company;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * CM3113 Session 2018-19 Starting point for Coursework
 * Ticket.java encapsulates properties of ticket issued to customer
 */
public class Ticket {
    private String contactName;
    private AtomicInteger numberOfPeople; // TODO is it actually supposed to be Atomic?
    private long ticketRef;
    
    private static long nextTicketRef; //atomicLong

    Ticket(String c, AtomicInteger n){
        contactName = c;
        numberOfPeople = n;
        ticketRef = nextTicketRef++;
    }

    @Override
    public String toString() {
        return "Item{" + "description=" + contactName + ", highestBid=" + numberOfPeople + '}';
    }

    synchronized String getContactName() {
        return contactName;
    }

    public synchronized void setContactName(String contactName) {
        this.contactName = contactName;
    }

    synchronized AtomicInteger getNumberOfPeople() {
        return numberOfPeople;
    }

    public synchronized void setNumberOfPeople(AtomicInteger numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    synchronized long getTicketRef() {
        return ticketRef;
    }

    public void setTicketRef(long ticketRef) {
        this.ticketRef = ticketRef;
    }
       
}
