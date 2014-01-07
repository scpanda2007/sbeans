package jmud.command;

//import java.util.Iterator;

import jmud.PlayerChannel;
import jmud.Room;
//import java.net.Socket;

/*
 * Down.java
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
 * @author Chris Maguire
 * @version 0.1
 *          <p/>
 *          Executable command Down
 */

/*
 * Class: Down
 * Purpose: Extends the Command class to provide
 *          the functionality for a player to move to the
 *          room below the current room
 */
public class Down extends Direction {

    private static final String TARGET_DIRECTION = "down";
    private static final String SOURCE_DIRECTION = "above";

    private Room room;
    private PlayerChannel playerChannel;

    public Down(PlayerChannel pc, Room r, String target) {
        super(pc, r);
        this.room = r;
        this.playerChannel = pc;
    }

    public Room getTargetRoom() {
        return room.down;
    }

    public String getTargetDirection() {
        return TARGET_DIRECTION;
    }

    public String getSourceDirection() {
        return SOURCE_DIRECTION;
    }

}