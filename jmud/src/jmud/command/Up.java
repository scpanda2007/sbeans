package jmud.command;

import jmud.PlayerChannel;
import jmud.Room;
//import java.net.Socket;
//import java.util.Iterator;

/*
 * Up.java
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
 * Executable command Up: moves a player from their previous room to the room above, if possible
 *
 * @author Chris maguire
 * @version 0.1
 */
public class Up extends Direction {

    private static final String TARGET_DIRECTION = "up";
    private static final String SOURCE_DIRECTION = "down";

    private Room room;
    private PlayerChannel playerChannel;

    public Up(PlayerChannel pc, Room r, String target) {
        super(pc, r);
        this.room = r;
        this.playerChannel = pc;
    }

    public Room getTargetRoom() {
        return room.up;
    }

    public String getTargetDirection() {
        return TARGET_DIRECTION;
    }

    public String getSourceDirection() {
        return SOURCE_DIRECTION;
    }

}