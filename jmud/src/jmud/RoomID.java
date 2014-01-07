package jmud;

import jmud.command.Command;

import java.nio.channels.SocketChannel;

/*
 * RoomID.java
 *
 * Created on July 29, 2006
 *
 * History
 *
 * Programmer:     Change:                                           Date:
 * ----------------------------------------------------------------------------------
 * Chris M         Cleaned up comments                               Feb 14, 2007
 */

/**
 * Executable command RoomID: This command returns the ID of the room that the current player is in.
 *
 * @author Chris maguire
 * @version 0.1
 */
public class RoomID extends Command {
    private PlayerChannel playerChannel;
    private Player player;
    private SocketChannel sc;
    private Room room;

    /**
     * Construct a RoomID command
     *
     * @param pc     The PlayerChannel to get the RoomID
     * @param r      The current room
     * @param target Not used, but all command constructors must have all three parameters
     */
    public RoomID(PlayerChannel pc, Room r, String target) {
        this.playerChannel = pc;
        this.player = pc.getPlayer();
        this.sc = pc.getSocketChannel();
        this.room = r;
        // we don't need target
    }

    /**
     * The logic that this command encapsulates
     * <p/>
     * Return the Room's ID (for dev purposes)
     *
     * @return whether of not this command is finished
     */
    public boolean exec() {
        try {
            // tell the player what room they're in
            playerChannel.sendMessage(room.getID()
                + CRLF
                + player.getPrompt());
        } catch(Exception e) {
            System.out.println("Send room ID message failed:\n\r"
                + e.getMessage());
        }

        // this command does not need to be repeated so return true for "Yes, I'm done"
        return true;
    }

}
