package jmud;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/*
* AcceptThread.java
*
* Created on ?
*
* History
*
* Programmer:     Change:                                           Date:
* ----------------------------------------------------------------------------------
* Chris M         Cleaned up comments                               Feb 14, 2007
*
* ToDo Find out if one selector can listen for multiple events. We might be able to have
*      one thread that listens to all events (connections, login information, commands, etc.).
*      Although, having multiple threads can be handy (e.g. for setting priority).
*/

/**
 * AcceptThread waits for incoming connections.  When a connection is made,
 * a socket channel is created, added to a list of accepted connections and
 * registered with a selector that will wait for input on that channel.
 *
 * @author Chris Maguire
 * @version 0.1
 *          <p/>
 *          ToDo No one else needs access to the accept (new connection) selector so we don't need
 *          to have it as a parameter: we can just create one in the constructor.
 */
class AcceptThread extends Thread {
    private ServerSocketChannel ssc;
    private Selector connectSelector;
    private ConnectionList acceptedConnections;

    /**
     * Open up a server socket channel, attach the server socket channel to a port,
     * attach a selector to the server socket channel that listens for connections.
     *
     * @param connectSelector A Selector that will watch for connections.
     * @param list            A list of connected socket channels that other threads will be checking
     * @param port            The port to listen for connections on
     */
    public AcceptThread(Selector connectSelector, ConnectionList list, int port) throws Exception {

        // call the thread constructor to give this thread a name
        super("Acceptor");

        this.connectSelector = connectSelector;
        this.acceptedConnections = list;

        // open a server socket channel that will be able to create socket channels for
        // incoming connections
        ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        // bind our shiny new socket to the port specified
        InetSocketAddress address = new InetSocketAddress(port);
        ssc.socket().bind(address);

        // DEBUG:
        //System.out.println("Bound to " + address);

        // tell the selector that, when we ask [.select()] we want to know
        // about attempted connections
        ssc.register(this.connectSelector, SelectionKey.OP_ACCEPT);
    }

    /**
     * Wait for connections and call a function to handle them
     */
    public void run() {
        while(true) {
            try {
                // DEBUG:
                //System.out.println("AcceptThread: Selecting");

                // this blocks until we get an attempted connection
                //  (from what I've figured out)
                connectSelector.select();

                // once we've unblocked (someone is attempting to connect) then
                // we process the connection.
                acceptPendingConnections();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Create new socket channels for incoming connections and add the
     * socket channels to the list of channels that other threads will
     * be watching (the push function also registers the new socket channel
     * with the selector associated with accepted connections).
     */
    protected void acceptPendingConnections() throws Exception {
        // we only get here when select has returned, and for select to
        // return there must be keys, so ... get the keys
        Set readyKeys = connectSelector.selectedKeys();

        // the selector comes with it's own iterator.
        // Notice the lack of the 3rd part of the for loop structure
        for(Iterator i = readyKeys.iterator(); i.hasNext();) {
            SelectionKey key = (SelectionKey) i.next();
            i.remove();

            // get the channel assoc'd with the current key
            ServerSocketChannel readyChannel = (ServerSocketChannel) key.channel();

            // create a socket channel and use the new ServerSocketChannel to accept
            // the connection and pass the SocketChannel to us
            SocketChannel incomingChannel = readyChannel.accept();

            // DEBUG:
            System.out.println("AcceptThread: Connection from " + incomingChannel.socket().getInetAddress());

            // Put the channel on the list of accepted channels and notify the selector that
            // listens for input from accepted channels for login information

            // NOTE: Here's where we could limit the number of channels per thread
            // although I don't know if we'd need to or want to unless we got to 63
            // (63 is the max channels per selector)
            acceptedConnections.push(incomingChannel);
        }
    }
}