package jmud.command;

import jmud.PlayerChannel;
import jmud.Room;
//import java.net.Socket;
//import java.util.Iterator;

/*
 * South.java
 *
 * Created on March 14 2003 10:17 PM
 *
 * History
 *
 * Programmer:     Change:                                           Date:
 * ----------------------------------------------------------------------------------
 * Chris M         Cleaned up comments                               Feb 13, 2007

/**
 * Executable command "North"
 *
 * @author  Chris Maguire
 * @version 0.1
 *
 */
public class North extends Direction {

    private static final String TARGET_DIRECTION = "north";
    private static final String SOURCE_DIRECTION = "south";

    private Room room;
    private PlayerChannel playerChannel;

    public North(PlayerChannel pc, Room r, String target) {
        super(pc, r);
        this.room = r;
        this.playerChannel = pc;
    }

    public Room getTargetRoom() {
        return room.north;
    }

    public String getTargetDirection() {
        return TARGET_DIRECTION;
    }

    public String getSourceDirection() {
        return SOURCE_DIRECTION;
    }

}