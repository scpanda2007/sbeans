package jmud;

import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.LinkedList;

/*
 * ConnectionList.java
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
 * A list of SocketChannels and their associated Selector
 *
 * @author Chris maguire
 * @version 0.1
 */

/*
 * Class: ConnectionList
 *
 * Purpose: Provides an easy way to group connections with a Selector.
 *
 * Notes: Create a ConnectionList with a Selector and then use
 *        push, remove, and remove first to add or remove connected
 *        SocketChannels and attach/detach them to/from their Selector.
 */
class ConnectionList<T> {
    private LinkedList<T> list = new LinkedList<T>();
    private Selector selectorToNotify;

    /*
    * Construct a new Connection list which will
    * attach SocketChannels to the specified Selector
    */
    public ConnectionList(Selector sel) {
        this.selectorToNotify = sel;
    }

    /*
    * Add a SocketChannel to the list and
    * wake up the selector.
    *
    * How does the SocketChannel get registered with the Selector?
    */
    public synchronized void push(T newlyConnectedChannel) {
        list.add(newlyConnectedChannel);
        selectorToNotify.wakeup();
    }

    /*
    * Remove and return the first SocketChannel in the list.
    *
    * How does the SocketChannel get unregistered from the Selector?
    */
    public synchronized T removeFirst() {
        if(list.size() == 0) {
            return null;
        }

        return list.removeFirst();
    }

    /*
    * Remove a specified SocketChannel from the list
    *
    * How does the SocketChannel get unregistered from the Selector?
    */
    public synchronized void remove(T t) {
        Iterator i = list.iterator();
        while(i.hasNext()) {
            if(i.next() == t) {
                i.remove();
            }
        }
    }
}