package jmud;

/*
 * jmud.java
 *
 * Created on April 18, 2002, 11:51 PM
 *
 * History
 *
 * Programmer:     Change:                                           Date:
 * ----------------------------------------------------------------------------------
 * Chris M         Cleaned up comments                               Feb 13, 2007
 */

/**
 * Starts all the threads with their Selectors and Lists.
 *
 * @author Chris Maguire
 * @version
 */

//import java.io.*;
//import java.net.*;
//import java.nio.*;

import jmud.command.Command;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

public class JMud {

    private static final int HTTPD_PORT = 9000;
    public static boolean verbose = true;

    // thread to listen for new connections
    private AcceptThread acceptor;

    // thread to turn player input in to command objects
    private CommandListenerThread commandListener;

    // thread to handle logging players in
    private LoginThread login;

    // thread to run command objects
    private EngineThread engine;

    // authConnections was for passing channels from the login thread to the command listener
    // but now we just attach it to the command listener's selector directly
    //private ConnectionList authConnections;

    /**
     * Start a new instance of the jmud thread and have it listen on a predefined port
     *
     * @param args input parameters. These are ignored.
     */
    public static void main(String[] args) {
        int port = HTTPD_PORT;

        try {
            JMud j = new JMud(port);
            j.start();
        } catch(Exception ex) {
            System.err.println(ex);
        }
    }

    /**
     * No idea why I have this
     */
    public JMud() {
    }

    /**
     * Creates all the threads, all the selectors to listen for incoming data,
     * and all the lists for managing connections, players, incomplete commands (commands)
     * <p/>
     * Here's an overview of how players log in:
     * <p/>
     * Acceptor Selector sees new connection
     * Acceptor thread passes connection to log in thread, registers the connection with
     * the login selector and wakes up the login selector
     * Login thread sends login prompts, verifies login data, authenticates player or rejects
     * player, passes authenticated connections (with player data) to the "commandListener" (command
     * parser) thread, registers the authenticated connection with the commandListener selector
     * and wakes up the commandListener selector to listen for incoming command data.
     *
     * @param port port to start the server on
     * @throws Exception (no idea)
     */
    public JMud(int port) throws Exception {
        LinkedList<PlayerChannel> playerChannelList = new LinkedList<PlayerChannel>();
        LinkedList<Command> commands = new LinkedList<Command>();

        // create the select that will listen for new connections
        Selector acceptSelector = Selector.open();
        // create the selector that will listen for command data
        Selector readSelector = Selector.open();
        // create the selector that will listen for login data
        Selector loginSelector = Selector.open();

        // create a list of connections that all work with the login selector
        ConnectionList<SocketChannel> connections = new ConnectionList<SocketChannel>(loginSelector);

        // Warning: this is a bit confusing:

        /*
         Create an "acceptor" thread that will:
           - accept new connections,
           - add them to the "connections" linked list,
           - register them with the login selector and
           - "wake up" the login selector.
        */
        acceptor = new AcceptThread(acceptSelector, connections, port);

        /*
         we create a login thread with its own selector, plus the selector for the command listener
         (to register the SocketChannels of authenticated users) plus the connections list (filled
         by the acceptor thread), plus the player channel list for the command listener.

         The login thread will:
           - Send the user username and login prompts
           - Validate the username and login
           - disconnect invalid connections
           - Create "player channels" from player login information (a player and a socket channel)
           - Register player channels with the "commandListener" selector
           - Put the player channels in the player channel list for the "command Listener" thread
        */
        login = new LoginThread(loginSelector, readSelector, connections, playerChannelList);

        /*
         we create a command listener thread with it's own read selector and the same player channel
         list that the login thread has. When the login thread calls wakeup, we'll check the list.

         the command listener (commandListener) also takes the commands LinkedList so that it can put
         recurring commands in the queue for the engine thread
        */
        commandListener = new CommandListenerThread(readSelector, playerChannelList, commands);

        /*
         Create a new engine thread with the commands list that is shared with the command listener.
         The engine thread will process incoming commands at an interval specified in the second
         parameter of the EngineThread constructor.
        */
        engine = new EngineThread(commands, 1500);
    }

    // ToDo: set some priorities:
    //       acceptor  lowest
    //       login     low
    //       engine    normal
    //       commandListener normal

    /**
     * Start all the threads
     */
    public void start() {
        commandListener.start();
        acceptor.start();
        login.start();
        engine.start();
    }
}

