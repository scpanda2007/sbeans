package jmud.command;

import jmud.*;

import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
//import java.net.Socket;

/*
 * Attack.java
 *
 * Created on March 14 2003 10:17 PM
 *
 * History
 *
 * Programmer:     Change:                                           Date:
 * ----------------------------------------------------------------------------------
 * Chris M         Cleaned up comments                               Feb 14, 2007
 *
 * ToDo Calculate the damage done to the target based on the player's stats and
 *      the targets armor class
 * ToDo Either use a random attack verb (e.g. "smashes", "impales") when a player
 *      hits a target or based the verb on the amount of damage.
 * ToDo Choose an attack verb that fits the players weapon (e.g. swords don't whip or crush)
 */

/**
 * Executable command attack: handles a player attacking something
 *
 * @author Chris maguire
 * @version 0.1
 */
public class Attack extends Command {

    private static final String[] names = {"a", "at", "att", "atta", "attac", "attack"};
    private ChannelWriter chanWriter = new ChannelWriter();
    private PlayerChannel playerChannel;
    private Room room;
    private String targetName;


    /**
     * Constructs an Attack command with attacker being the Player in pc, the
     * room where the attack takes place as r and the target or "atackee" as target.
     *
     * @param pc     The object containing the attacking player and its channel
     * @param r      The room where the attack takes place
     * @param target The name of the target object to attack (does not need to be unique)
     */
    public Attack(PlayerChannel pc, Room r, String target) {
        this.playerChannel = pc;
        this.room = r;
        this.targetName = target;
    }

    /**
     * Executes an attack by the attacker on the attackee. Attack may not succeed if the
     * target specified in the constructor does not match any attackable targets
     * in the attacker's current room.
     * <br>
     * The attack will be repeated until:
     * <br>
     * - The specific target that is being attacked dies
     * <br>
     * <b>or</b>
     * <br>
     * - The specific target that is being attacked fleas
     * <br>
     * <b>or</b>
     * <br>
     * - The attacker leaves the room
     * <br>
     * <b>or</b>
     * <br>
     * - The attacker dies
     *
     * @return true if the attack completed or false if the attack needs to be run again.
     */
    public boolean exec() {
        Target target;
        SocketChannel sc = playerChannel.getSocketChannel();
        Iterator i;
        int iDamage = 1;
        int iThac0 = 10;
        int iHitRoll = 0;
        boolean bHit;
        List<Mob> mobs;
        List<PlayerChannel> players;
        HashSet<String> mobNames = new HashSet<String>();
        StringBuffer strbAllNames = new StringBuffer("Multiple matches: ");
        boolean needComma = false;

        // DEBUG:
        /*
        System.out.println("Attack.exec(): calling room.hasMob("
                           + targetName
                           + ");");
        */

        // check to see if there is a mob or mobs in the room with a matching name or acronymn
        mobs = room.getMobs(targetName);

        // if there were none
        if(mobs == null || mobs.isEmpty()) {

            // check to see if there are any players that match and
            // tell them they can't attack players.
            players = room.getPlayers(targetName);
            if(players != null && !players.isEmpty()) {
                try {
                    chanWriter.sendMessage("You can't attack " + targetName + ", they are a player."
                        + playerChannel.getPlayer().getPrompt(), sc);

                    // tell the game engine that we're done
                    return true;
                } catch(Exception e) {
                    System.out.println("Attack.exec(): ChannelWriter.sendMessage(\"Can't Attack\") failed for "
                        + playerChannel.getPlayer().getName());
                }
            }

            try {
                // we didn't find any target that matched what the user entered so
                // we'll let them know it
                chanWriter.sendMessage("There is no \"" + targetName + "\" here to attack.\n\r"
                    + playerChannel.getPlayer().getPrompt(), sc);
            } catch(Exception e) {
                System.out.println("Attack.exec(): ChannelWriter.sendMessage(\"No Enemy\") failed for "
                    + playerChannel.getPlayer().getName()
                    + "\n"
                    + e.getMessage()
                    + "\n"
                    + e.toString());
                e.printStackTrace();

                // DEBUG:
                //System.out.println(playerChannel.getSocketChannel().toString());
            }

            // tell the game engine that we're done
            return true;
        }

        // check if we have multiple different mob names
        // (if we have multiple of the same mob then just attack any of the identical mobs)
        for(Mob mob : mobs) {
            mobNames.add(mob.getName());
        }

        // if we found MORE than one mob
        if(mobNames.size() > 1) {

            // create a string listing all the matching mobs
            for(String str : mobNames) {
                if(needComma) {
                    strbAllNames.append(", ");
                } else {
                    needComma = true;
                }
                strbAllNames.append(str);
            }

            // show the user all the potential matches so they can pick one
            try {
                playerChannel.sendMessage(strbAllNames.append(".\n\r")
                    .append(playerChannel.getPlayer().getPrompt())
                    .toString());
            } catch(Exception e) {
                System.out.println("Attack.exec(): ChannelWriter.sendMessage(\"Multiple matches\") failed for "
                    + playerChannel.getPlayer().getName()
                    + "\n"
                    + e.getMessage()
                    + "\n"
                    + e.toString());
                e.printStackTrace();

                // DEBUG:
                //System.out.println(playerChannel.getSocketChannel().toString());
            }

            // tell the game engine that we're done
            return true;
        }

        // get an array from the set, get element 1, cast to string, get mob with it, put mob in target, check for null
        if((target = room.getMob(mobs.get(0))) == null) {
            try {
                playerChannel.sendMessage("Found the mob name, but no mob! Weird.\n\r"
                    + playerChannel.getPlayer().getPrompt());
            } catch(Exception e) {
                System.out.println("Attack.exec(): ChannelWriter.sendMessage(\"Name, but no Mob\") failed for "
                    + playerChannel.getPlayer().getName()
                    + "\n"
                    + e.getMessage()
                    + "\n"
                    + e.toString());
                e.printStackTrace();

                // DEBUG:
                //System.out.println(playerChannel.getSocketChannel().toString());
            }

            // tell the game engine that we're done
            return true;
        }

        /*****************************************************************************/
        /* AT THIS POINT WE HAVE A MOB */

        // generate a roll (as in rolling the dice) and decide if they hit the target
        iHitRoll = (int) (Math.random() * 20);
        /* ToDo: We need to implement a strategy here to handle the attack:
            - what appendages are we attacking with?
            - what non-appendage attacks are we using?
            - If we have multiple attacks avaiable (e.g. right-hand sword, left-foot boot), what ration
              of each attack should we use (e.g. for every two swings of the sword, throw in a kick):
            - We could even add some logic:
               - if I miss with the sword, kick the target
                 or
               - if my sword is blocked, use a kick
                 or (and this gets really crazy)
               - if *I think* my sword will be blocked (e.g. they have a shield), use a kick

           All this together would combine into a potentially complex "attack profile"
           which may include:
             - mob type specific attack parameters (e.g. when attacking snakes, DO NOT KICK!)
        */

        // DEBUG:
        /*
        System.out.println("iHitRoll = " + iHitRoll
                          + ", iThac0 = " + iThac0
                          + ", target AC = " + target.getAC());
        */

        // The "hit" flag (as in "you hit the target") is true if the hit roll
        // is greater than the players THAC0 ("To Hit Armor Class 0) plus the
        // targets armor class
        bHit = iHitRoll >= (iThac0 + target.getAC());

        // did the player miss the target?
        if(!bHit) {

            // broadcast a "MISSED!" message
            try {
                room.sendMessageToAll(""
                    + playerChannel.getPlayer().getName()
                    + " swings at "
                    + target.getName()
                    + " and misses!\n\r");
                /* ToDo: We need a catalogue of miss verbs (for variety)
                    However, we may want to consider macros, this could screw up a macro, which might be good
                */
            } catch(Exception e) {
                System.out.println("Attack.exec(): room.sendMessage(\"Miss\") failed: \n\r"
                    + e.getMessage());
            }

            // else the player hit the target
        } else {

            // get the amount of damage
            // iDamage = pc.getPlayer().getDamage();
            // we need to calculate this using the players stats, stat modifiers and the
            // the targets armor class

            // broadcast a "HIT!" message
            try {
                room.sendMessageToAll(""
                    + playerChannel.getPlayer().getName()
                    + " smashes "
                    + target.getName()
                    + " for "
                    + iDamage
                    + " damage!\n\r");
            } catch(Exception e) {
                System.out.println("Attack.exec(): ChannelWriter.sendMessage(\"Hit\") failed \n\r"
                    + e.getMessage());
            }
        }

        // do damage and check for killed IF they scored a hit
        if(bHit && target.hurt(iDamage)) {
            //mark the target as dead
            //Scratch that: the target should "kill" itself (i.e. target.hurt() should set the isAlive flag)

            // broadcast a "KILLED!" message
            // now this is retarded, but you can't rewind an iterator, have to get a new one
            try {
                room.sendMessageToAll(""
                    + playerChannel.getPlayer().getName()
                    + " killed "
                    + target.getName()
                    + "!\n\r");
            } catch(Exception e) {
                System.out.println("Attack.exec(): ChannelWriter.sendMessage(\"Killed\") failed \n\r"
                    + e.getMessage());
            }

            // The enemy is dead, so tell the game engine that we're done
            return true;
        }

        // The target isn't dead yet so tell the game engine to run this command again
        return false;
    }
}