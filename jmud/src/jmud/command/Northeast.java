package jmud.command;

//import java.util.Iterator;

import jmud.PlayerChannel;
import jmud.Room;
//import java.net.Socket;

/*
 * Northeast.java
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
 * Executable command "Northeast"
 *
 * @author Chris maguire
 * @version 0.1
 */

public class Northeast extends Direction {

    private static final String TARGET_DIRECTION = "northeast";
    private static final String SOURCE_DIRECTION = "southwest";

    private Room room;
    private PlayerChannel playerChannel;

    public Northeast(PlayerChannel pc, Room r, String target) {
        super(pc, r);
        this.room = r;
        this.playerChannel = pc;
    }

    public Room getTargetRoom() {
        return room.northeast;
    }

    public String getTargetDirection() {
        return TARGET_DIRECTION;
    }

    public String getSourceDirection() {
        return SOURCE_DIRECTION;
    }

}