package jmud.command;

import jmud.Player;
import jmud.PlayerChannel;
import jmud.Room;

import java.nio.channels.SocketChannel;

/*
 * Created on Jun 22, 2005
 *
 * History
 *
 * Programmer:     Change:                                           Date:
 * ----------------------------------------------------------------------------------
 * Chris M         Cleaned up comments                               Feb 14, 2007
 *
 * TODO Implement this command
 */

/**
 * Executable command "Set Room Description" for setting the description of a room
 * in memory and in the database.
 *
 * @author Chris Maguire
 * @version 0.1
 */
public class SetRoomDescription extends Command {
    private PlayerChannel playerChannel;
    private Player player;
    private SocketChannel sc;
    private Room room;
    // strTarget should be the new description for a room
    private String strTarget;

    /**
     * Construct an east command
     *
     * @param pc     The PlayerChannel to send the results too
     * @param r      The current room
     * @param target target to look at, if null or empty string, looks at current room
     */
    public SetRoomDescription(PlayerChannel pc, Room r, String target) {
        // we can ignore everything but the room we're editing and the target
        // (which is the new description)
        this.room = r;
    }

    /**
     * The logic that this command encapsulates
     * <p/>
     * Set the description of the current room
     */
    public boolean exec() {

        // The code for this command has yet to be written so we'll give the
        // user some feedback to that effect.
        try {
            playerChannel.sendMessage("This command does not work yet, sorry. "
                + CRLF
                + room.getExits()
                + CRLF
                + player.getPrompt());
        } catch(Exception e) {
            System.out.println("Couldn't print \"command no workee\" message to " + player.getName());
        }

        // Returning true tells the game engine that this command is finished
        return true;
    }

}
