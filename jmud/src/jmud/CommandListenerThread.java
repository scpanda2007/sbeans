package jmud;

import jmud.command.Command;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.sql.SQLException;
import java.util.*;

class CommandListenerThread extends Thread {
    private static final int READ_BUFFER_SIZE = 20;

    private Selector readSelector;
    private LinkedList<PlayerChannel> playerChannelList;
    private LinkedList<Command> commands;
    private MudMap mudmap;

    // for outgoing messages)
    private ChannelWriter channelwriter;

    // for incoming messages
    private ByteBuffer readBuffer;
    private CharsetDecoder asciiDecoder;

    // for the command class storage
    private Hashtable<String, Constructor> commandAliases = new Hashtable<String, Constructor>();

    /**
     * - Creates a new map
     * - Loads the map with rooms, players, monsters, etc.
     * - Loads command aliases (e.g. e, ea, eas and east for "East")
     *
     * @param readSelector   Watches for new data on attached sockets (SocketChannels)
     * @param playerChannels A list of attached sockets for authenticated players
     * @param commands       The list where we'll put command objects created from incoming data
     * @throws Exception Exception, ClassNotFoundException
     */
    public CommandListenerThread(Selector readSelector,
                                 LinkedList<PlayerChannel> playerChannels,
                                 LinkedList<Command> commands) throws Exception {
        // call the Thread constructor to give this thread a name
        super("Command Listener");

        String[] names;
        Constructor con;

        this.readSelector = readSelector;
        this.playerChannelList = playerChannels;
        this.commands = commands;
        this.channelwriter = new ChannelWriter();
        this.readBuffer = ByteBuffer.allocateDirect(READ_BUFFER_SIZE);

        Charset ascii = Charset.forName("US-ASCII");
        asciiDecoder = ascii.newDecoder();

        // create a connection to the database
        MysqlConnector mSqlConn = new MysqlConnector();

        // create a linked list to store all the command aliases in the database
        // (a command alias is a partial command, e.g. "ea" which is mapped to a
        //  full command: e.g. "east")
        LinkedList<String[]> commandAliases = new LinkedList<String[]>();
        try {
            mSqlConn.setup();

            // fill the linked list with the command aliases
            // (a "command alias" is a two element array consisting of the
            //  command alias and the full command)
            mSqlConn.getCommandAliases(commandAliases);

            mSqlConn.close();
        } catch(SQLException se) {
            System.out.println("getCommandAliases() --> Could not connect to the database. returning." + se);
            return;
        }

        // create a new mud map (of rooms) and then
        // call the functions to load the map from the database
        mudmap = new MudMap(2);
        mudmap.loadRooms();
        mudmap.loadMobs();

        // Create constructors for the commands that each
        // alias maps to and store the command constructors
        // in a commandAliases keyed on the alias.
        //
        // By doing this we can locate the constructor for a command
        // very quickly when the user enters any mapped alias
        // for that command.
        //
        // Typically command aliases are simply portions of a command.
        // For example, the command "East" has 4 aliases:
        //   e
        //   ea
        //   eas
        //   east - we treat the full command as an alias (this is basically semantics)
        try {
            // iterate through the linked list of command alias arrays
            Iterator i = commandAliases.iterator();

            // DEBUG:
            System.out.print("Creating commands: ");

            // loop through each command alias (2-element array containing the alias-command pair)
            while(i.hasNext()) {
                // DEBUG: this prints out a line of dots as command aliases get processed
                System.out.print(".");

                // get the 2-element alias-command string array
                names = (String[]) i.next();

                // get the command constructor that matches this alias using the full command name
                // found in the 2nd element of the array
                con = getConstructor(names[1]);

                // DEBUG:
                //System.out.println(con.getName());
                //System.out.println(names[1]);

                // as long as we found the constructor for the command, add the
                // constructor object to the command alias commandAliases and key it on the alias
                if(con != null) {
                    this.commandAliases.put(names[0], con);

                    // DEBUG: show the aliases we loaded
                    //System.out.println(names[0] + " alias added with constructor " + con.getName());
                }
            }

            // DEBUG: keep the debug output nice and clean
            System.out.print("\n");

        } catch(Exception e) {
            System.out.println("CommandListener kacked while loading command aliases");
        }
    }

    /**
     * Listen for input on active connections using a Selector. When the Selector
     * indicates there is activity on a SocketChannel, process the input.
     */
    public void run() {

        // run indefinitely
        while(true) {

            try {
                // process any new socket channels that have been added
                // to the playerChannel list by the login thread
                registerNewChannels();

                // wait for active keys (.select() blocks) and process their input
                if(readSelector.select() > 0) {
                    handleReadyKeys();
                    readSelector.selectedKeys().clear();

                    // why would we Selector.select() return if their were no active keys?
                } else {
                    if(readSelector.selectedKeys().size() > 0) {
                        System.out.println("Removing leftover ready keys with: loginSelector.selectedKeys().clear()");
                        readSelector.selectedKeys().clear();
                    }
                }

                // if we get an exception just continue on like nothing happened
            } catch(Exception ex) {
                // DEBUG:
                ex.printStackTrace();
            }
        }
    }

    /**
     * The login thread puts incoming SocketChannels into the
     * "playerChannel" linked list once the players authenticate themselves.
     * <p/>
     * This function goes through each SocketChannel and registers it
     * with the "read" selector which will then list for incoming data
     * from that authenticated players socket channel
     *
     * @throws Exception if the networking channel code come off the rails
     */
    protected void registerNewChannels() throws Exception {
        SocketChannel channel;
        Player p;
        Room r;
        PlayerChannel playerchannel;

        // loop through each "player channel" that's been added to the list
        // by the login thread
        for(Iterator i = playerChannelList.iterator(); i.hasNext();) {
            playerchannel = (PlayerChannel) i.next();
            channel = playerchannel.getSocketChannel();

            // get the player and room from the "Player Channel"
            p = playerchannel.getPlayer();
            r = mudmap.getStart();

            // the user has only logged on so far, so add them (i.e. their player channel)
            // to their initial room
            synchronized(p) {
                p.setRoom(r);
                r.add(playerchannel);
            }

            // uh, a note here would have been handy
            channel.configureBlocking(false);

            // register this socket channel with the selector that listens for
            // incoming command data
            channel.register(readSelector, SelectionKey.OP_READ, p);

            // show the player the description of their current room and their prompt
            try {
                channelwriter.sendMessage(r.getShortDescription() + "\n\r" + r.getExits() + "\n\r", channel);
                channelwriter.sendMessage(p.getPrompt(), channel);
            } catch(Exception e) {
                System.out.println("CommandListenerThread: parseCommand: failed channelwriter.sendMessage()");
            }
            i.remove();
        }
    }

    /**
     * Each socket channel that sends input has it's "key"
     * marked Active.
     * <p/>
     * This function goes through all the active keys and calls the
     * "parseCommand" function on them
     */
    protected void handleReadyKeys() throws Exception {

        // get all the ready keys
        Set readyKeys = readSelector.selectedKeys();

        // loop through and process each key
        for(Iterator i = readyKeys.iterator(); i.hasNext();) {
            //SelectionKey key = (SelectionKey)i.next();
            //parseCommand(key);
            // this could be one line

            parseCommand((SelectionKey) i.next());
        }
    }

    /**
     * Grab the data from a socket channel that has it's "key" set
     * to "ready". Process the data to see if it adds to a command
     * or completes a command. Append non-completeing data to a stored
     * command and create command objects for completed commands.
     * Store completed commands in the command execution queue.
     */
    protected void parseCommand(SelectionKey key) throws Exception {

        // Every key has a socketchannel - get the key's channel
        SocketChannel incomingChannel = (SocketChannel) key.channel();

        String strResult = "";

        try {
            // if the socket channel activates it's key but doesn't send any data
            // it means that the connection was dropped.
            int bytesRead = incomingChannel.read(readBuffer);
            if(bytesRead < 0) {
                // DEBUG:
                System.out.println("CommandListenerThread: Lost connection");

                // close the dropped connection
                incomingChannel.close();
                return;
            }

            // set the limit to the last byte read and reset the position to the beginning
            readBuffer.flip();

            // decode the buffer
            String strRequest = asciiDecoder.decode(readBuffer).toString();

            // clear the buffer (one buffer per thread, ... we have to share)
            readBuffer.clear();

            // grab the reference to the key attachment
            StringBuffer requestString = ((Player) key.attachment()).strCurrentCommand;

            // append the request to the attachment's string buffer
            requestString.append(strRequest);

            // if the command is finished, deal with the finished command
            if(strRequest.endsWith("\n") || strRequest.endsWith("\r\n")) {

                // if the latest request string ended in [enter], pass the whole string buffer
                parseCompletedCommand(requestString.toString(), incomingChannel);

                /*
                try{
                  //System.out.println(((Player)incomingChannel.keyFor(readSelector).attachment()).getPrompt());
                  channelwriter.sendMessage(strResult + "\n\r", incomingChannel);
                  channelwriter.sendMessage(((Player)incomingChannel.keyFor(readSelector).attachment()).getPrompt(), incomingChannel);
                }catch(Exception e){
                  System.out.println("CommandListenerThread: parseCommand: failed channelwriter.sendMessage()");
                  System.out.println("error: " + e.getMessage() + " " + e.toString());
                }
                */
                // Don't send anything to the player

                //clear out the string buffer for the next active key
                requestString.delete(0, requestString.capacity() - 1);
            }
        } catch(IOException ioe) {
            if(!incomingChannel.isConnected()) {
                incomingChannel.close();
            }

            // the connection is not closed so we'll simply write an
            // error message to the player's socket channel
            channelwriter.sendError(incomingChannel);
        }
    }

    /**
     * Once a command has been fully entered, or completed, meaning that the player
     * sent a CR or CR+LF, handle the full command.
     * <p/>
     * Handling a command consists of retrieving the command's constructor from a
     * hashtable containing all commands mapped to all their aliases, creating an instance
     * of the command and adding it to the command queue of the game engine.
     *
     * @param request The command to parse
     * @param channel The SocketChannel belonging to the player that sent the command
     */
    protected void parseCompletedCommand(String request, SocketChannel channel) throws IOException {

        // get the player from the current SocketChannel's key
        Player p = (Player) channel.keyFor(readSelector).attachment();

        // get the room from the player
        Room r = p.getRoom();

        // create a new player channel for the player
        PlayerChannel pc = new PlayerChannel(p, channel);

        Command cmd;
        Constructor constr;
        Class c;

        StringTokenizer tok = new StringTokenizer(request);
        String strCommand;
        StringBuffer strbTarget = new StringBuffer("");

        /*
        * The users data has to start with a command alias but after that
        * there can be "target" data (e.g. "look Bob"). We'll tokenize the
        * string so that we can easily pick of the first whole word
        */
        if(tok.countTokens() >= 1) { //if they've sent a valid command ...
            // the command has to be the first word in the input string
            strCommand = tok.nextToken();

            // take the rest of the words and make them the target
            // (don't ask my why we take them in reverse order)
            for(int i = tok.countTokens(); i > 0; i--) {
                strbTarget.append(tok.nextToken());
                if(i > 1) {
                    strbTarget.append(" ");
                }
            }

            // DEBUG:
            //System.out.println("getting constructor for command: " + strCommand);

            /*
              since we loaded all the constructors in the commandAliases we can just pull
              out the constructor that matches this alias
            */
            constr = (Constructor) this.commandAliases.get(strCommand);

            // if we found a constructor ...
            if(constr != null) {
                try {
                    // create a new instance of the specified command class using the
                    // player, the players room and the target string (e.g. "Bob" from "look Bob")
                    cmd = (Command) constr.newInstance(pc, r, strbTarget.toString());
                    commands.add(cmd);

                    // catch all the different types of errors for debugging purposes:
                } catch(InstantiationException ie) {
                    // DEBUG:
                    System.out.println("Couldn't create instance of Class \"" + strCommand + "\"");
                } catch(IllegalAccessException iae) {
                    // DEBUG:
                    System.out.println("Illegal Access Exception\nCouldn't create instance of Class \"" + constr.getName() + "\"");
                } catch(InvocationTargetException ite) {
                    // DEBUG:
                    System.out.println("Invocation Target Exception\nCouldn't create instance of Class \"" + constr.getName() + "\"");
                } catch(IllegalArgumentException iarge) {
                    // DEBUG:
                    System.out.println("Illegal Argument Exception\nCouldn't create instance of Class \"" + constr.getName() + "\"");
                }
            } else {
                // DEBUG:
                System.out.println("Class constructor is null");
            }
        } else {
            // DEBUG:
            System.out.println("Command is empty");
        }
    }

    /**
     * Create a constructor for a class using the Class name
     *
     * @param className Name of the class (should be a command class) to create the constructor for
     */
    private Constructor getConstructor(String className) throws ClassNotFoundException {
        Class c;
        try {
            // the database doesn't include the package, so we'll append the
            // package name to the class
            // (this was for when I was trying to get packages to work)
            c = Class.forName("jmud.command." + className);
        } catch(Exception e) {
            // DEBUG: the error off of this can be pretty vague
            System.out.println("Exception getting class for "
                + className
                + ": "
                + e.getMessage()
                + "\n" + e);
            return null;
        }

        // if we got a class ...
        if(c != null) {
            try {
                /* get the constructor for the class that matches the method sig:
                * className(Player, Room, String)
                */
                return c.getConstructor(new Class[]{//Class.forName("PlayerChannel"),
                    PlayerChannel.class,
                    //Class.forName("Room"),
                    Room.class,
                    String.class});
                // check for various exceptions to aid in debugging
                /* We're not using "Class.forName(<classname>)" anymore
                }catch(ClassNotFoundException cnfe){
                  // DEBUG:
                  System.out.println("Couldn't find all Classes for Class \"" + c.getName() + "\"");
                  return null;
                */
            } catch(NoSuchMethodException nsme) {
                // DEBUG:
                System.out.println("Couldn't find constructor for Class \"" + className + "\"");
                return null;
            }
        } else {
            System.out.println("No class matches name " + className);
        }

        // shouldn't get here
        System.out.println("getConstructor: null constructor for " + className);
        return null;
    }

}