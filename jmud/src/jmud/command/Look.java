package jmud.command;

import jmud.Mob;
import jmud.Player;
import jmud.PlayerChannel;
import jmud.Room;
import jmud.item.Item;

import java.util.List;
//import java.net.Socket;

/*
 * Look.java
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
public class Look extends Command {
    private static final String ALSO_HERE_PROMPT = "Also here: ";
    private static final String YOU_SEE_PROMPT = "You see: ";

    private PlayerChannel playerChannel;
    private Player player;
    private Room room;
    private String strTarget;

    /**
     * Construct a look command
     *
     * @param pc     The PlayerChannel to send the results too
     * @param r      The current room
     * @param target target to look at, if null or empty string, looks at current room
     */
    public Look(PlayerChannel pc, Room r, String target) {
        this.playerChannel = pc;
        this.player = pc.getPlayer();
        this.room = r;
        this.strTarget = target;
    }

    /**
     * The logic that this command encapsulates
     * <p/>
     * If the target is null or blank look at the room, else if the target can be
     * mapped to an item, player or mob, look at the item, player or mob
     */
    public boolean exec() {
        List<PlayerChannel> playerChannels;
        List<Mob> mobs;
        List<Item> items;
        StringBuilder message = new StringBuilder();
        String mobAndPlayerNames = room.getMobAndPlayerNames(player);
        String itemNames = room.getItemNames(player.isDebug());
        int matches = 0;
        boolean needComma = false;

        // DEBUG: has the command been called
        //System.out.println("Look.exec() called");

        // check if there is a target (to look at) or not
        if(strTarget == null || strTarget.length() == 0) {
            message.append(room.getShortDescription())
                .append(player.isDebug() ? " [" + room.getID() + "]" : "")
                .append(CRLF)
                .append(mobAndPlayerNames.length() != 0 ? ALSO_HERE_PROMPT + mobAndPlayerNames + CRLF : "")
                .append(itemNames.length() != 0 ? YOU_SEE_PROMPT + itemNames + CRLF : "");

            sendMessage(message);
            return true;

        }

        // try to parse the target to a mob, player or item

        /* for each of the hashtables holding players, mobs and items,
        * if the hashtable holds a key (always a String: the name) that
        * starts with strTarget, get the description of the object whose
        * key started with strTarget
        */
        playerChannels = room.getPlayers(strTarget);
        if(playerChannels != null && !playerChannels.isEmpty()) {
            matches += playerChannels.size();
        }


        mobs = room.getMobs(strTarget);
        if(mobs != null && !mobs.isEmpty()) {
            matches += mobs.size();
        }

        items = room.getItems(strTarget);
        if(items != null && !items.isEmpty()) {
            matches += items.size();
        }

        // room.getXXX can return null, so check for it
        if(matches == 0) {
            message.append("You don't see a ")
                .append(strTarget)
                .append(".");
            sendMessage(message);
            return true;
        }

        // if we found more than one match then prompt the player for which thing they meant
        if(matches > 1) {
            message.append("multiple matches: ");
            if(playerChannels != null) {
                for(PlayerChannel pc : playerChannels) {
                    if(needComma) {
                        message.append(", ");
                    } else {
                        needComma = true;
                    }
                    message.append(pc.getPlayer().getName());
                }
            }
            if(mobs != null) {
                for(Mob mob : mobs) {
                    if(needComma) {
                        message.append(", ");
                    } else {
                        needComma = true;
                    }
                    message.append(mob.getName());
                }
            }
            if(items != null) {
                for(Item item : items) {
                    if(needComma) {
                        message.append(", ");
                    } else {
                        needComma = true;
                    }
                    message.append(item.getName());
                }
            }
            sendMessage(message);
            return true;
        }

        if(playerChannels != null && !playerChannels.isEmpty()) {
            message.append(playerChannels.get(0).getPlayer().getDescription());
        } else if(mobs != null && !mobs.isEmpty()) {
            message.append(mobs.get(0).getMobType().getDesc());
        } else {
            message.append(items.get(0).getName());
        }

        return true;
    }

    private void sendMessage(StringBuilder strbMessage) {
        // tack on the exits and the prompt even though the engine is supposed to handle the
        // prompt.
        strbMessage.append(room.getExits())
            .append(CRLF)
            .append(player.getPrompt());

        // now send back what we found to the player
        try {
            playerChannel.sendMessage(strbMessage.toString());
        } catch(Exception e) {
            System.out.println("Couldn't print room desc and exits to " + player.getName());
        }
    }

}