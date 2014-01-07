package jmud.command;

import jmud.Flag;
import jmud.Player;
import jmud.PlayerChannel;
import jmud.Room;

import java.lang.reflect.Field;

/**
 * Executable command Set
 *
 * @author Chris maguire
 */

/*
 * Class: Set
 * Purpose: Sets a player property
 */
public class Set extends Command {

    private PlayerChannel playerChannel;
    private Player player;
    private String target;

    /**
     * Construct a Set command
     *
     * @param pc     The PlayerChannel to send the results too
     * @param r      The current room
     * @param target property to set and the value
     */
    public Set(PlayerChannel pc, Room r, String target) {
        this.playerChannel = pc;
        this.player = pc.getPlayer();
        this.target = target;
    }

    /**
     * The logic that this command encapsulates
     */
    public boolean exec() {
        StringBuilder strbMessage = new StringBuilder();
        Field field = null;

        // DEBUG: are we getting here?
        //System.out.println("Set command called for plaery " + player.getName() + " with target " + target);

        // if they didn't specify a flag then print the values of all flags
        if(target.length() == 0) {

            // DEBUG: target.length() == 0
            //System.out.println("Set target length was 0");

            for(Field f : Player.getFlags()) {

                // DEBUG: do we have any fields?
                //System.out.println("Set: found field - " + f.getName());

                try {
                    // print the f's annotated name and value (on or off)
                    strbMessage.append(f.getAnnotation(Flag.class).name())
                        .append(":\t")
                        .append(f.getBoolean(player) ? "on" : "off")
                        .append(CRLF);

                    // DEBUG: check that we're actually appending stuff
                    //System.out.println("Set results: " + strbMessage.toString());

                } catch(Exception e) {
                    strbMessage.append("[error retrieving ")
                        .append(f.getName())
                        .append("]")
                        .append("\n");
                    System.out.println("Couldn't retrieve setting for flag [" + f.getName() + "] for player [" + player.getName() + "]");
                }
            }
            // else they've specified a flag in particular
        } else {

            // split the target string into
            // words
            String args[] = target.split(" ");

            // DEBUG: what target string did we get?
            //System.out.println("Set target: " + target);

            // DEUBG: what did target.split give us?
//            for(String s : args){
//                System.out.println("Set arg: " + s);
//            }

            // get the field by the alias that the player supplied
            field = Player.getFlagsByAlias().get(args[0]);

            // if they've only supplied the flag name then simply print the name and value
            if(args.length == 1) {

                // display the annotated name and value ("on" or "off") of the field
                try {
                    strbMessage.append(field.getAnnotation(Flag.class).name())
                        .append(":\t")
                        .append(field.getBoolean(player) ? "on" : "off")
                        .append(CRLF);
                } catch(Exception e) {
                    strbMessage.append("[error retrieving ")
                        .append(args[0])
                        .append("]");
                    System.out.println("Couldn't retrieve setting for flag [" + field.getName() + "] for player [" + player.getName() + "]");
                }

                // else they've supplied the flag name _and_ a value
            } else {
                try {
                    field.setBoolean(player, "on".equals(args[1]));

                    strbMessage.append(field.getAnnotation(Flag.class).name())
                        .append(" is now:\t")
                        .append(field.getBoolean(player) ? "on" : "off")
                        .append(CRLF);
                } catch(IllegalAccessException e) {
                    strbMessage.append("[error retrieving ")
                        .append(args[0])
                        .append("]");
                    System.out.println("Couldn't retrieve setting for flag [" + field.getName() + "] for player [" + player.getName() + "]");
                }
            }

            // DEBUG: has the command been called
            //System.out.println("Set.exec() called");


        }

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
