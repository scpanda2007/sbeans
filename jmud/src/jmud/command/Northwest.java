package jmud.command;

import jmud.PlayerChannel;
import jmud.Room;
//import java.net.Socket;
//import java.util.Iterator;

/*
 * Northwest.java
 *
 * Created on March 28 2003 12:26 PM
 *
 * History
 *
 * Programmer:     Change:                                           Date:
 * ----------------------------------------------------------------------------------
 * Chris M         Cleaned up comments                               Feb 13, 2007

/**
 * Executable command Northwest
 *
 * @author  Chris maguire
 * @version 0.1
 *
 */

public class Northwest extends Direction {

    private static final String TARGET_DIRECTION = "northwest";
    private static final String SOURCE_DIRECTION = "southeast";

    private Room room;
    private PlayerChannel playerChannel;

    public Northwest(PlayerChannel pc, Room r, String target) {
        super(pc, r);
        this.room = r;
        this.playerChannel = pc;
    }

    public Room getTargetRoom() {
        return room.northwest;
    }

    public String getTargetDirection() {
        return TARGET_DIRECTION;
    }

    public String getSourceDirection() {
        return SOURCE_DIRECTION;
    }

}