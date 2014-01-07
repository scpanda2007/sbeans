/*
 * Copyright 2007 Entero Corporation. All Rights Reserved.
 * www.entero.com
 */
package jmud.command;

import jmud.Player;
import jmud.PlayerChannel;
import jmud.Room;

import java.nio.channels.SocketChannel;

public class RoomID extends Command {
    private PlayerChannel playerChannel;
    private Player player;
    private SocketChannel sc;
    private Room room;

    /**
     * Construct a down command
     *
     * @param pc       The PlayerChannel to move down
     * @param r        The current room
     * @param whatever Not used, but all command constructors must have all three parameters
     */
    public RoomID(PlayerChannel pc, Room r, String whatever) {
        this.playerChannel = pc;
        this.player = pc.getPlayer();
        this.room = r;
    }

    /**
     * Each subclass of Command must implement its own exec() method because
     * that's what commands are for, executing specific MUD commands.
     */
    public boolean exec() {
        // Tell the player what else is in the room they just entered
        try {
            playerChannel.sendMessage(new StringBuilder().append(room.getID())
                .append(CRLF)
                .append(player.getPrompt()).toString());

            // send the message that the player left to the room they just left
            room.up.sendMessageToAll(CRLF
                + player.getName()
                + " went down"
                + CRLF);
        } catch(Exception e) {
            System.out.println("sending room ID message failed:\n\r"
                + e.getMessage());
        }

        // Return true for "Yes, this command is finished"
        return true;
    }
}
