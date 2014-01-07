/*
 * Direction.java
 *
 * Created on December 9, 2007, 2:46 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jmud.command;

import jmud.MysqlConnector;
import jmud.Player;
import jmud.PlayerChannel;
import jmud.Room;

/**
 * Handles all the direction agnostic gear for moving around
 *
 * @author Chris Maguire
 */
public abstract class Direction extends Command {

    private static final String ALSO_HERE_PROMPT = "Also here: ";
    private static final String YOU_SEE_PROMPT = "You see: ";
    private final Object lock = new Object();
    private PlayerChannel playerChannel;
    private Player player;
    private Room room;

    public abstract Room getTargetRoom();

    public abstract String getTargetDirection();

    public abstract String getSourceDirection();

    /**
     * Construct a direction command
     *
     * @param pc The PlayerChannel to move south
     * @param r  The current room
     */
    public Direction(PlayerChannel pc, Room r) {
        this.playerChannel = pc;
        this.player = pc.getPlayer();
        this.room = r;
        // we don't need target
    }

    /**
     * The logic that this command encapsulates
     * <p/>
     * If the room to the south isn't null, remove the PlayerChannel from the
     * current room and add it to the room that is south of the current room. Also
     * set the player's room reference to the room that is south of the current room.
     *
     * @return whether of not this command is finished
     */
    public boolean exec() {

        //String names;
        //String items;
        // Scratch that: these were used when we were handling the 'Look'ing here

        Room targetRoom;

        // Check if there is a room to the south of the player's current room
        if((targetRoom = getTargetRoom()) != null) {

            synchronized(lock) {
                room.remove(playerChannel);
                player.setRoom(targetRoom);
                targetRoom.add(playerChannel);
            }

            // set up a connection to the database
            MysqlConnector mSqlConn = new MysqlConnector();
            try {
                mSqlConn.setup();
            } catch(Exception e) {
                System.out.println(getClass().getName() + ".exec() --> player-room not insered: could not connect to database.");
                System.out.println(e);
                // we'll keep going even though the mapping is broken
            }

            try {
                //record this room in the players list of visited rooms for mapping
                mSqlConn.insertPlayerRoom(player.getID(), targetRoom.getID());
            } catch(Exception e) {
                System.out.println("Insert of player-room into DB failed:\n\r"
                    + e.getMessage());
            }

            //I assume this closes the connection
            mSqlConn.close();

            try {
                //names = targetRoom.getMobAndPlayerNames(player);
                //items = targetRoom.getItemNames();

                // tell the player whose in the new rom
                /*
                playerChannel.sendMessage(targetRoom.getShortDescription()
                    + CRLF
                    + (names.length() != 0 ? ALSO_HERE_PROMPT + names + CRLF : "")
                    + (items.length() != 0 ? YOU_SEE_PROMPT + items + CRLF : "")
                    + targetRoom.getExits()
                    + CRLF
                    + player.getPrompt());
                */
                // Scratch that: Look is going to handle this for us

                // send the message that the player entered to everyone *else* in the room
                targetRoom.sendMessageToAllButOne(player.getName()
                    + " entered from the " + getSourceDirection()
                    + CRLF, playerChannel);

                // send the message that the player left to everyone in the room they just left
                room.sendMessageToAll(CRLF
                    + player.getName()
                    + " went " + getTargetDirection()
                    + CRLF);
            } catch(Exception e) {
                System.out.println("Send room desc, mob names, exits and prompt, OR player went [direction] message failed:\n\r"
                    + e.getMessage());
            }

            // there was no room to the south of the player's current room
        } else {
            try {
                playerChannel.sendMessage("Can't go " + getTargetDirection() + CRLF);
            } catch(Exception e) {
                System.out.println("Send \"Can't go [direction]\\n\\r\" failed:" + CRLF
                    + e.getMessage());
            }
        }

        // if the player has autoLook set then try and execute the "Look" command
        try {
            // DEBUG: player has autoLook set?
            //System.out.println("Direction.exec() -> Checking player.autoLook == true");

            if(player.isAutoLook()) {

                // DEBUG: player has autoLook set
                //System.out.println("Direction.exec() -> player.autoLook == true: calling look command");

                Look.class.getConstructor(PlayerChannel.class, Room.class, String.class).newInstance(playerChannel, targetRoom, "").exec();
            }
        } catch(Exception e) {
            System.out.println("Look.exec() failed: \n\r"
                + e.getMessage());
        }

        // returning true tells the game engine that this command is complete
        // (some commands like "Attack" will get repeated until some condition is met)
        return true;
    }

}
