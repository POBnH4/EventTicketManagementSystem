package com.company;

import java.awt.event.ActionListener;

public class TimerTask extends Thread {
    private Timing target;
    private long period;
    public TimerTask(int hit, ActionListener when){
        //target = hit;
        //period = when;
        setPriority(MAX_PRIORITY);
        setDaemon(true);
    }

    @Override
    public void run() {
        for(;;){
            try{
                sleep(period);
            }catch(InterruptedException e){
                target.tick();
            }
        }
    }
}
