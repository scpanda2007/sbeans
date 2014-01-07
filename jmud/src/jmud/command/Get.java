package jmud.command;

import jmud.Player;
import jmud.PlayerChannel;
import jmud.Room;
import jmud.item.Item;

/**
 * Executable command Look
 *
 * @author Chris maguire
 * @version 0.1
 */

/*
 * Class: Look
 * Purpose: Extends the Command class to provide
 *          the functionality for a player to look
 *          at something
 */
public class Get extends Command {

    private PlayerChannel playerChannel;
    private Player player;
    private Room room;
    private String strTarget;

    /**
     * Construct a Get command
     *
     * @param pc     The PlayerChannel to send the results too
     * @param r      The current room
     * @param target target to look at, if null or empty string, looks at current room
     */
    public Get(PlayerChannel pc, Room r, String target) {
        this.playerChannel = pc;
        this.player = pc.getPlayer();
        this.room = r;
        this.strTarget = target.trim();
    }

    /**
     * The logic that this command encapsulates
     * <p/>
     * If the item exists, get it.
     */
    public boolean exec() {
        StringBuilder strbMessage = new StringBuilder();
        Item item;

        // DEBUG: has the command been called
        //System.out.println("Get.exec() called");

        // check if there is an item or not
        if(strTarget == null || strTarget.length() == 0) {
            strbMessage.append("You must specify an item to get")
                .append(CRLF);
            // check if the player has a free hand
        } else if(!player.hasFreeGrabber()) {
            strbMessage.append("You need a free hand to grab that.")
                .append(CRLF);
            // make sure the item is in the room
        } else if(room.getItem(strTarget) == null || (item = room.remove(room.getItem(strTarget))) == null) {
            strbMessage.append("You don't see a ")
                .append(strTarget)
                .append(CRLF);
            // nothing failed, we got the item
        } else {
            player.grab(item);
            strbMessage.append("You get the ")
                .append(item.getName())
                .append(CRLF);
        }

        // tack on the player's prompt even though the engine is supposed to handle the
        // prompt.
        strbMessage.append(player.getPrompt());

        // now send back what we found to the player
        try {
            playerChannel.sendMessage(strbMessage.toString());
        } catch(Exception e) {
            System.out.println("Couldn't print room desc and exits to " + player.getName());
        }

        return true;
    }

}
