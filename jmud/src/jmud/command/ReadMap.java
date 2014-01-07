package jmud.command;

import jmud.Player;
import jmud.PlayerChannel;
import jmud.Room;

import java.nio.channels.SocketChannel;

/*
 * ReadMap.java
 *
 * Created on Jun 29, 2005
 *
 * History
 *
 * Programmer:     Change:                                           Date:
 * ----------------------------------------------------------------------------------
 * Chris M         Cleaned up comments                               Feb 13, 2007
 *
 */

/**
 * @author chrism
 * @version 0.1
 *          <p/>
 *          TODO Maybe tell the room that the player is looking at their map
 */
public class ReadMap extends Command {

    private PlayerChannel playerChannel;
    private Player player;
    private SocketChannel sc;
    private Room room;

    /**
     * Construct an east command
     *
     * @param pc     The PlayerChannel to move east
     * @param r      The current room
     * @param target Not used, but all command constructors must have all three parameters
     */
    public ReadMap(PlayerChannel pc, Room r, String target) {
        this.playerChannel = pc;
        this.player = pc.getPlayer();
        this.room = r;
        // we don't need target
    }

    /**
     * Display a map of the rooms that a player has visited
     *
     * @return True if the command is finished, false if it needs to be put back in the queue to be repeated
     */
    public boolean exec() {
        Map map = new Map();
        map.loadRooms(player.getID());
        map.fillMap();

        try {
            // print out the map
            playerChannel.sendMessage(CRLF
                + map.toString()
                + CRLF
                + player.getPrompt());
        } catch(Exception e) {
            System.out.println("Send map and prompt failed:\n\r"
                + e.getMessage());
        }

        return true;
    }

}
