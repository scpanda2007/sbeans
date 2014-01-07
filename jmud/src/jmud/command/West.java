package jmud.command;

import jmud.PlayerChannel;
import jmud.Room;

//import java.net.Socket;
//import java.util.Iterator;

/*
 * West.java
 *
 * Created on March 28 2003 12:26 PM
 *
 * History
 *
 * Programmer:     Change:                                           Date:
 * ----------------------------------------------------------------------------------
 * Chris M         Cleaned up comments                               Feb 14, 2007
 */

/**
 * Executable command West: handles moving a player from the room they were in to the
 * room to the west, if such a room exists.
 *
 * @author Chris maguire
 * @version 0.1
 */

public class West extends Direction {

    private static final String TARGET_DIRECTION = "west";
    private static final String SOURCE_DIRECTION = "east";

    private Room room;
    private PlayerChannel playerChannel;

    public West(PlayerChannel pc, Room r, String target) {
        super(pc, r);
        this.room = r;
        this.playerChannel = pc;
    }

    public Room getTargetRoom() {
        return room.west;
    }

    public String getTargetDirection() {
        return TARGET_DIRECTION;
    }

    public String getSourceDirection() {
        return SOURCE_DIRECTION;
    }

}