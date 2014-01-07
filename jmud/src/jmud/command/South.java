package jmud.command;

import jmud.PlayerChannel;
import jmud.Room;

/*
 * South.java
 *
 * Created on March 14 2003 10:17 PM
 *
 * History
 *
 * Programmer:     Change:                                           Date:
 * ----------------------------------------------------------------------------------
 * Chris M         Cleaned up comments                               Feb 14, 2007
 */

/**
 * Executable command south: moves the player one room to the south if a room exists there
 *
 * @author Chris Maguire
 * @version 0.1
 */
public class South extends Direction {

    private static final String TARGET_DIRECTION = "south";
    private static final String SOURCE_DIRECTION = "north";

    private Room room;
    private PlayerChannel playerChannel;

    public South(PlayerChannel pc, Room r, String target) {
        super(pc, r);
        this.room = r;
        this.playerChannel = pc;
    }

    public Room getTargetRoom() {
        return room.south;
    }

    public String getTargetDirection() {
        return TARGET_DIRECTION;
    }

    public String getSourceDirection() {
        return SOURCE_DIRECTION;
    }
}