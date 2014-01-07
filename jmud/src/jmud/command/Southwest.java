package jmud.command;

import jmud.PlayerChannel;
import jmud.Room;
//import java.net.Socket;
//import java.util.Iterator;

/*
 * Southwest.java
 *
 * Created on March 28 2003 12:26 PM
 */

/**
 * Executable command Southwest
 *
 * @author Chris maguire
 * @version 0.1
 */

public class Southwest extends Direction {

    private static final String TARGET_DIRECTION = "southwest";
    private static final String SOURCE_DIRECTION = "northeast";

    private Room room;
    private PlayerChannel playerChannel;

    public Southwest(PlayerChannel pc, Room r, String target) {
        super(pc, r);
        this.room = r;
        this.playerChannel = pc;
    }

    public Room getTargetRoom() {
        return room.southwest;
    }

    public String getTargetDirection() {
        return TARGET_DIRECTION;
    }

    public String getSourceDirection() {
        return SOURCE_DIRECTION;
    }

}