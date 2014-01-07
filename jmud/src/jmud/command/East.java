package jmud.command;

//import java.util.Iterator;

import jmud.PlayerChannel;
import jmud.Room;
//import java.net.Socket;

/*
 * East.java
 *
 * Created on March 28 2003 12:26 PM
 *
 * History
 *
 * Programmer:     Change:                                           Date:
 * ----------------------------------------------------------------------------------
 * Chris M         Cleaned up comments                               Feb 13, 2007
 */

/**
 * @author Chris maguire
 * @version 0.1
 *          <p/>
 *          Executable command East
 */

/*
 * Class: East
 * Purpose: Extends the Command class to provide
 *          the functionality for a player to go to the
 *          room east of their current room
 */
public class East extends Direction {

    private static final String TARGET_DIRECTION = "east";
    private static final String SOURCE_DIRECTION = "west";

    private Room room;
    private PlayerChannel playerChannel;

    public East(PlayerChannel pc, Room r, String target) {
        super(pc, r);
        this.room = r;
        this.playerChannel = pc;
    }

    public Room getTargetRoom() {
        return room.east;
    }

    public String getTargetDirection() {
        return TARGET_DIRECTION;
    }

    public String getSourceDirection() {
        return SOURCE_DIRECTION;
    }

}