package jmud;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;

/*
 * LoginThread.java
 *
 * Created on March 28 2003 12:26 PM
 *
 * History
 *
 * Programmer:     Change:                                           Date:
 * ----------------------------------------------------------------------------------
 * Chris M         Cleaned up comments                               Feb 13, 2007
 *
 * ToDo: combine all the functions that create and return ByteBuffers
 */

/**
 * LoginThread handles:
 * - Listening for login activity
 * - Validating logins
 * - Creating "PlayerChannels" from login information
 * - Register player channels with the command thread's selector
 * - Putting player channels in the list of authenticated players
 *
 * @author Chris maguire
 * @version 0.1
 */
class LoginThread extends Thread {
    private static final int READ_BUFFER_SIZE = 20;

    private Selector commandSelector;
    private ConnectionList<SocketChannel> acceptedConnections;
    //private ConnectionList<SocketChannel> authConnections;
    private LinkedList<PlayerChannel> playerChannelList;
    //private LinkedList <Player>playerList;
    private Selector loginSelector;
    private ByteBuffer readBuffer;
    private Charset ascii;
    private CharsetDecoder asciiDecoder;
    //private StringBuffer strBuffLoginInfo;
    private MysqlConnector mSqlConn;

    /*
       We pass in:
         - the selector for login connections
         - the selector to pass channels to when they've logged in
         - a list of accepted connections where the acceptor thread dumps it's recent
           connections
         - a list of player channels that the Command Listener watches for logged in channels
         - a list of players that the game engine watches for
    */
    public LoginThread(Selector loginSelector,
                       Selector commandSelector,
                       ConnectionList<SocketChannel> acceptedConnections,
                       LinkedList<PlayerChannel> playerchannels) throws Exception {

        // call the thread constructor to name the thread
        super("Login");

        this.loginSelector = loginSelector;
        this.commandSelector = commandSelector;
        this.acceptedConnections = acceptedConnections;
        playerChannelList = playerchannels;
        //playerList = new LinkedList<Player>();
        this.readBuffer = ByteBuffer.allocateDirect(READ_BUFFER_SIZE);

        ascii = Charset.forName("US-ASCII");
        asciiDecoder = ascii.newDecoder();

        // DEBUG:
        //System.out.println("Created new login thread");

        // set up a connection to the database
        mSqlConn = new MysqlConnector();
        try {
            mSqlConn.setup();
        } catch(Exception e) {
            System.out.println("LoginThread() --> No players loaded, could not connect to database.");
            System.out.println(e);
            //return;
        }

        // retrieve the list of players from the databse
        /*
        try{
           mSqlConn.getPlayers(playerList);
           mSqlConn.close();
        }catch(SQLException se){
           System.out.println("LoginThread() --> " + se);
        }finally{
          mSqlConn = null;
        }
        */
        // Scratch that: we'll grab the players from the database (along with slots, items, etc.) when they log in

        // DEBUG: go through the list now that it's filled and print out the names
        //        to be sure it actually GOT filled
        //for(Iterator i = playerList.iterator(); i.hasNext();){
        //  System.out.println(((Player)i.next()).getPassword() + "has been verified.");
        //}
    }

    /**
     * Listen for any connections, prompt players for usernames and passwords, verify
     * usernames and passwords.
     */
    public void run() {

        // run indefinitely
        while(true) {
            try {
                // when a channel registers, all we do is:
                //  1) send login prompt
                //  2) record response up to /n
                //  3) send pwd prompt
                //  4) record response up to /n
                //  5) check for player in [player collection] with matching login/password
                //  NOTE: we don't want to check the login before they've sent the password,
                //        because we don't want them to know if they even got the login right

                // DEBUG:
                //System.out.println("LoginThread: Selecting");
                //System.out.println("Never ending loop iteration #" + ++iIterations);

                // register any new SocketChannels encountered by the Acceptor thread
                // and send them a login prompt
                registerNewChannels();

                // DEBUG:
                //System.out.println("LoginThread: registered new channels");

                // block and wait for input from any of the registered socket channels
                int keysReady = loginSelector.select();

                // handle any incoming data
                if(keysReady > 0) {

                    // DEBUG:
                    //System.out.println(keysReady + " Keys ready in never ending loop iteration #" + iIterations + "\n\n\n");

                    // process the incoming login data
                    acceptPendingRequests();

                    loginSelector.selectedKeys().clear();
                } else {
                    if(loginSelector.selectedKeys().size() > 0) {
                        // DEBUG:
                        System.out.println("Removing leftover ready keys with: loginSelector.selectedKeys().clear()");
                        // How do "ready" keys get left over?

                        loginSelector.selectedKeys().clear();
                    }
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * rip through the list of connections and register them all
     * with the commandSelector, which I THINK tells us that there is
     * data (e.g. a command, in this program) waiting for us
     *
     * @throws Exception no idea what exception this throws
     */
    protected void registerNewChannels() throws Exception {
        SocketChannel channel;

        // work through all the socket channels put into the acceptedCOnnections linked list
        // by the acceptor thread
        while(null != (channel = acceptedConnections.removeFirst())) {

            // why do we do this?
            channel.configureBlocking(false);

            // at this point, I think we lose the player object (gets replaced with a login?)
            // nope, at this point there is not object attached to the socket channels key

            // register the socket channel with the login selector and attach a new login
            // object to the socket channels selector key
            channel.register(loginSelector, SelectionKey.OP_READ, new Login());

            //channel.write(getWelcomeMessage());

            // DEBUG:
            //System.out.println("Writing login prompt\n");

            // Send the player a login prompt
            channel.write(getLoginRequest());

            // - get the key from the selector we just registered this channel with
            // - get the attachment attached to that key
            // - cast the attachment to a Login
            // - set the state for that login
            ((Login) channel.keyFor(loginSelector).attachment()).setState(Login.LOGIN);
            //so now we've started the whole login process
        }
    }

    /**
     * for each channel that has received a command, rip through and process
     * the command
     *
     * @throws Exception no idea what this throws
     */
    protected void acceptPendingRequests() throws Exception {
        Set readyKeys = loginSelector.selectedKeys();

        // go through each key that is in the "ready" state
        for(Object readyKey : readyKeys) {

            // DEBUG:
            //System.out.println("LoginThread: getting key ");

            // get the next key
            SelectionKey key = (SelectionKey) readyKey;

            // DEBUG:
            //System.out.println("LoginThread: got key ");

            // process the incoming data from the socket channel associated with that key
            handleInput(key);

            // DEUBG: not getting here for some reason? (Dec 7, 2002)
            //System.out.println("Done Handling Input: " + ((Login)key.attachment()).getCurrentStateString());
            // I'm pretty sure this is fixed (Feb 13, 2007)
        }

        // DEBUG:
        //System.out.println("Done with ready keys");
    }

    /**
     * Process the incoming data from a socket channel whose selector key
     * is in the "ready" state
     *
     * @param key Key associated with the socket channel that has recieved data
     * @throws Exception no idea what exception this throws
     */
    protected void handleInput(SelectionKey key) throws Exception {
        // DEBUG:
        //System.out.println("LoginThread: handleInput: getting channel ");

        // get the channel assoc'd with the "someone has sent a command" key
        // (really it's just a "we can read from this channel" key)
        SocketChannel incomingChannel = (SocketChannel) key.channel();

        //get the actuall socket associated with the channel
        Socket incomingSocket = incomingChannel.socket();

        try {
            // get the data from the socket channel
            int bytesRead = incomingChannel.read(readBuffer);

            // if we've gotten a key, with no attached bytes, then the user's connection has dropped,
            // so kill the connection from this end
            if(bytesRead < 0) {
                // DEBUG:
                System.out.println("No bytes to read, killing connection.");

                incomingChannel.close();
                return;
            }

            // set the limit to the last byte read and reset the position to the beginning
            readBuffer.flip();

            // decode the buffer
            String strRequest = asciiDecoder.decode(readBuffer).toString();

            // clear the buffer (one buffer per thread, ... we have to share)
            readBuffer.clear();

            //NOTE: tricky
            // grab the reference to the key attachment
            StringBuffer requestString = ((Login) key.attachment()).getCurrentStateString();

            // append the request to the attachment's string buffer
            requestString.append(strRequest);

            // DEBUG:
            //System.out.println(requestString.toString());

            // if the login/pwd is finished, deal with the finished login info
            // (by calling handleCompletedLoginState)
            if(strRequest.endsWith("\r\n")) {

                // DEBUG:
                //System.out.println("Ends with \\r\\n");

                // remove the \\r\\n from the end of the latest login string
                requestString.delete(requestString.length() - 2, requestString.length());

                // Since the latest login string ended in [enter], pass the whole string buffer
                handleCompletedLoginState(requestString.toString(), incomingChannel);

            } else if(strRequest.endsWith("\n")) {
                // DEBUG:
                //System.out.println("Ends with \\n");

                // remove the \\n from the end of the login string
                requestString.deleteCharAt(requestString.length() - 1);

                // Since the latest login string ended in [Enter], pass the whole string buffer
                // to the function that will check the login or password
                handleCompletedLoginState(requestString.toString(), incomingChannel);
            }
        } catch(IOException ioe) {
            if(!incomingChannel.isConnected()) {

                // DEBUG:
                System.out.println("LoginThread: Closing disconnected socket channel");

                // the incoming channel is not connected so disconnect our end
                incomingChannel.close();
            } else {

                // DEBUG:
                System.out.println("LoginThread: Cancelling the socket key registration for socket: "
                    + incomingChannel.socket().getInetAddress().toString());

                // There was an error so cancel the "ready" key and disconnect the socket channel
                // from our end
                incomingChannel.keyFor(loginSelector).cancel();

                // DEBUG:
                System.out.println("LoginThread: Closing connection");

                incomingChannel.close();
            }
            System.out.println("ioe exception: " + ioe.getMessage());
        }
    }

    /**
     * Store the latest login information. If the latest login information is a password then
     * attempt to validate the user.
     *
     * @param loginString Complete login information (either a login or a password)
     * @param channel     Socket Channel that the user is on
     * @throws java.io.IOException if there io problems
     */
    protected void handleCompletedLoginState(String loginString, SocketChannel channel) throws IOException {
        Player player;

        // get the login object from the "ready" login selectors "ready" key associated with this Socket Channel
        Login login = (Login) channel.keyFor(loginSelector).attachment();

        // DEBUG:
        //System.out.println("Login is: (" + login.getLogin() + ", " + login.getPassword() + ")");

        // tokenize the string to make sure they've entered a "word"
        StringTokenizer tok = new StringTokenizer(loginString);

        // DEBUG:
        //System.out.println("LoginThread: handleCompletedLoginState: handling state");

        //if they've sent a valid login or password (not just \n) ...
        if(tok.hasMoreTokens()) {

            // grab the WHOLE thing (spaces and all)
            //strBuffLoginInfo = new StringBuffer(loginString);
            // I guess I never ended up using this

            // DEBUG:
            //System.out.println("LoginThread: handleCompletedLoginState: login/pwd = " + loginString);

            // DEBUG:
            //System.out.println("LoginThread: handleCompletedLoginState: state is " + login.getState() + " (" + login.PASSWORD + ")");

            //If the player has entered their password then attempt to validate the user
            if(login.getState() == Login.PASSWORD) {

                // attempt to validate the users login and password
                if(null != (player = (checkLogin(login)))) {

                    // DEBUG:
                    //System.out.println(player.getName() + " logged on successfully");

                    try {
                        // the player is logged in so send them a welcome message
                        channel.write(getWelcomeMessage(player.getName()));
                    } catch(Exception e) {
                        System.out.println("LoginThread: handleCompletedLoginState: getWelcomeMessage() failed");
                    }

                    // remove this connection from the acceptedConnections list
                    // because it should not go on the command thread's list
                    acceptedConnections.remove(channel);

                    // cancel this channel's key for the login selector
                    channel.keyFor(loginSelector).cancel();

                    // create playerChannel and push onto playerChannel linked list
                    playerChannelList.add(new PlayerChannel(player, channel));

                    commandSelector.wakeup();
                    return;
                }

                // to get here the login/password must not have been valid
                // (the login and password didn't match any players)

                login.setLoginFailed();

                //print login failed message;
                if(login.checkMaxFailedLogins()) {
                    channel.close();
                    return;
                }

                // if they haven't exhausted their login attempts, print the login again.
                try {
                    channel.write(getLoginRequest());
                } catch(Exception e) {
                    System.out.println("LoginThread: handleCompletedRequest: channel.write(login request) error");
                    e.printStackTrace();
                }

                // else the login state is LOGIN, so move on to the password prompt
            } else {
                // DEBUG:
                //System.out.println("LoginThread: handleCompletedRequest: setting login state to pwd");

                // update the login state
                login.setState(Login.PASSWORD);

                // give the user the password prompt
                try {
                    channel.write(getPasswordRequest());
                } catch(Exception e) {
                    System.out.println("LoginThread: handleCompletedRequest: channel.write(pwd request) error");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Create a ByteBuffer with the "Login:" text to display to the player
     *
     * @return A byte buffer containing "Login:"
     * @throws Exception java.nio.charset.CharacterCodingException
     */
    protected ByteBuffer getLoginRequest() throws Exception {
        ByteBuffer buffer = null;

        // make a message
        CharBuffer chars = CharBuffer.allocate(6);
        chars.put("Login:");
        chars.flip();

        // Translate the Unicode characters into ASCII bytes.
        buffer = ascii.newEncoder().encode(chars);

        buffer.rewind();

        return buffer;
    }

    /**
     * Create a ByteBuffer with the "Password:" text to display to the player
     *
     * @return A byte buffer containing "Password:"
     * @throws Exception no idea what this would throw
     */
    protected ByteBuffer getPasswordRequest() throws Exception {
        ByteBuffer buffer = null;

        // make a message
        CharBuffer chars = CharBuffer.allocate(9);
        chars.put("Password:");
        chars.flip();

        // Translate the Unicode characters into ASCII bytes.
        buffer = ascii.newEncoder().encode(chars);

        buffer.rewind();

        return buffer;
    }

    /**
     * Check a login object's username and password against all the users in the database
     * to see if we get a match
     *
     * @param login Login object containing username and password to use to match against all users
     * @return A player that matches the username and password, if any
     */
    private Player checkLogin(Login login) {
        // DEBUG:
        //System.out.println("Checking login (" + login.getLogin() + ", " + login.getPassword() + ")");

        Player player = null;

        try {
            player = mSqlConn.getPlayer(login.getLogin().toString(), login.getPassword().toString());
            mSqlConn.close();
        } catch(SQLException se) {
            System.out.println("LoginThread.checkLogin() --> " + se);
        } finally {
            mSqlConn = null;
        }

        // The login object's username and password didn't match any players
        return player;
    }

    /**
     * Create a ByteBuffer with the welcome text to display to the player
     *
     * @param name name of player
     * @return A byte buffer containing the welcome message
     * @throws Exception java.nio.charset.CharacterCodingException
     */
    protected ByteBuffer getWelcomeMessage(String name) throws Exception {
        ByteBuffer buffer = null;

        // make a message
        CharBuffer chars = CharBuffer.allocate(500);
        chars.put("Welcome to JMUD " + name + "! \n\rPlay nice or get the boot, ... \n\ryou have no rights here. :)\n\r");
        chars.flip();

        // Translate the Unicode characters into ASCII bytes.
        buffer = ascii.newEncoder().encode(chars);
        buffer.rewind();
        return buffer;
    }

/*
  // KEEP THIS: It's example of how to construct a buffer
  protected ByteBuffer sendError(StringBuffer strBuf) throws Exception {

    CharBuffer chars = CharBuffer.allocate(100);
    chars.put("You sent: [" + strBuf.toString() + "]\r\n");
    chars.put("Here's the response: \r\n");
    chars.put("(none yet)\r\n");
    chars.flip();

    // Translate the Unicode characters into ASCII bytes.
    ByteBuffer buffer = ascii.newEncoder().encode(chars);
    buffer.rewind();
    return buffer;
  }
*/
}

