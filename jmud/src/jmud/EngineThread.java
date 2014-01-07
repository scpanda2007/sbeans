package jmud;

import jmud.command.Command;

import java.util.Date;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/*
 * EngineThread.java
 *
 * Created on ?
 *
 * History
 *
 * Programmer:     Change:                                           Date:
 * ----------------------------------------------------------------------------------
 * Chris M         Cleaned up comments                               Feb 13, 2007
 */

/**
 * This thread tracks a list of commands that have submitted
 * for processing and processes the commands at a specified interval.
 * Commands are added directly to the Linked List that is used in the
 * constructor.
 *
 * @author Chris Maguire
 * @version 0.1
 *          <p/>
 *          ToDO: Have the EngineThread manage it's own LinkedList so that we don't have
 *          to pass it around separately.
 */
class EngineThread extends Thread {

    private Timer timer;
    private Tick tick;
    private final Object lock = new Object();
    private int milliseconds;

    /**
     * Constructs an engine thread with a LinkedList onto which will be added commands
     * and the number of milliseconds between command execution loops (the "tick" delay)
     *
     * @param commands The list of commands that are to be processed
     * @param millis   The number of milliseconds between each command execution loop
     */
    public EngineThread(LinkedList<Command> commands, int millis) {
        super("MUD Engine");
        milliseconds = millis;
        // never used this I guess
        timer = new Timer();
        tick = new Tick(commands);
    }

    /**
     * Creates the execution loop schedule
     */
    public void run() {
        timer.schedule(tick, new Date(), milliseconds);
    }


    /**
     * The Tick class is a singleton that simply executes any commands that
     * are in the command LinkedList.
     */
    public class Tick extends TimerTask {
        private LinkedList<Command> commands;

        /**
         * Constructs a Tick with the LinkedList where new commands will be stored
         *
         * @param commands The LinkedList where new command objects are stored.
         */
        public Tick(LinkedList<Command> commands) {
            //System.out.println("creating Ticker");
            this.commands = commands;
        }

        /**
         * The command list management and command execution
         */
        public void run() {

            // If there are no commands then bail
            if(commands == null || commands.isEmpty()) {
                // DEBUG:
                //System.out.println("commands linked list is null or empty, returning.");
                //System.out.print(".");
                return;
            }

            // only mess with the commands linked list when we have exclusive access
            LinkedList currentCommands;
            synchronized(lock) {
                // grab all the current commands by making a
                // new LinkedList with the commands list in it
                currentCommands = new LinkedList<Command>(commands);

                // clear out the list of commands waiting to be processed
                commands.clear();
            }

            // Loop through and process all the commands
            while(!currentCommands.isEmpty()) {
                Command command = (Command) currentCommands.removeFirst();

                // if the command isn't finished then add it too the end of the list
                // of the non-current command list
                // (or else we'll never hit the end of the list!)
                if(!command.exec()) {
                    synchronized(lock) {
                        commands.addLast(command);
                    }
                } else {
                    command = null;
                }
            }
        }
    }

}
