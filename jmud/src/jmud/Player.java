package jmud;

/*
 * Player.java
 *
 * Created on April 21, 2002, 4:24 PM
 *
 * History
 *
 * Programmer:     Change:                                           Date:
 * ----------------------------------------------------------------------------------
 * Chris M         Cleaned up comments                               Feb 13, 2007

/**
 * A role playing personna to be controlled and used to interact with the game
 *
 * @author  Chris Maguire
 * @version 0.02
 */

import jmud.slot.Slot;
import jmud.item.Item;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.annotation.Annotation;
import java.util.*;

public class Player implements Serializable, Target {

    final static int MAP_START_ROOM = 1;

    private static Map<String, Field> flagsByAlias = new HashMap<String, Field>();
    private static Set<Field> flags = new HashSet<Field>();

    private int iID;

    // settings
    @Flag(name = "debug", aliases = {"debug"})
    public boolean debug = false;

    @Flag(name = "auto look", aliases = {"auto look", "autolook"})
    public boolean autoLook = true; // should the player automatically look when entering a new room?

    // validation
    private String login;
    private String password;

    // info
    private String name;
    private String description;

    // health
    private int maxHP = 1;
    private int currHP = 0;

    // armor
    private int iAC = 0;

    // stats
    private int strength = 1;
    private int constitution = 1;
    private int dexterity = 1;
    private int intelligence = 1;

    // modifiers
    private static int[] poisonResistance;
    private static int[] hitModifier;
    private static int[] damageModifier;
    private static int[] armorClassModifier;

    // location
    private Room room;

    // unfinished input
    public StringBuffer strCurrentCommand;

    private Map<String, Slot> slots;

    /*
     Store references to all "settable" methods both by aliases and
     by names. Getting methods by alias allows us to pick a method and
     getting methods (from a set) by name allows us to get a list of
     all the methods and display their results with a human readable name.
     */
    static {
        Flag flag;

        System.out.print("Loading Player flags: ");

        // DEBUG: are any of the annotations registering?
        //for(Annotation a : Player.class.getDeclaredAnnotations()){
        //    System.out.println(a.getClass().getName());
        //}

        try {
            for(Field field : Player.class.getDeclaredFields()) {

                // DEBUG: are we getting any fields?
                //System.out.println(field.getName());

                for(Annotation a : field.getDeclaredAnnotations()) {
                    // DEBUG: field has annotation?
                    //System.out.println("[Field " + field.getName() + " has annotation " + a.toString() + "]");
                }

                if((flag = field.getAnnotation(Flag.class)) != null) {

                    // DEBUG: field has Flag annotation
                    //System.out.print("[" + field.getName() + " has " + flag.name() + "] ");

                    for(String alias : flag.aliases()) {

                        // DEBUG: field has alias
                        //System.out.print("[" + field.getName() + " has " + alias + "] ");

                        flagsByAlias.put(alias, field);
                        flags.add(field);
                    }

                    // DEBUG: show that we've loaded a flag
                    System.out.print(".");
                }
            }
        } catch(Exception e) {
            System.out.println("Player static{} (load flag methods) --> " + e);
        }

        System.out.print("\n");
    }

    /**
     * Creates new Player
     * <p/>
     * Also creates a StringBuffer for holding an unfinnished command in progress
     */
    public Player() {
        strCurrentCommand = new StringBuffer();
    }

    /**
     * Creates new Player and sets the players name (NOT their login)
     * <p/>
     * Also creates a StringBuffer for holding an unfinnished command in progress
     *
     * @param name The name (not login) to assign to this player. This is what other
     *             players in the game will see and refer to this player as.
     */
    public Player(String name) {
        strCurrentCommand = new StringBuffer();
        this.name = name;
    }

    /**
     * Creates new Player and sets most of the player's attributes
     * <p/>
     * Also creates a StringBuffer for holding an unfinnished command in progress
     *
     * @param name          The name (not login) to assign to this player. This is what other
     *                      players in the game will see and refer to this player as.
     * @param login         The login name that the user will used to login to the game and
     *                      control this player
     * @param password      The password that will be used to ensure only the allowed users
     *                      will be able to use this player (it isn't really security, but oh well)
     * @param currHitPoints The current amount of hit points this player should have, if
     *                      more than Max number of hit points than the max number of hit points
     *                      will be assigned to the player's current hit points instead.
     * @param maxHitPoints  The maximum number of hit points this player should have. If
     *                      the max hit points is less than one then max hit points will be
     *                      set to one.
     * @param strength      The quantifiable strength of the player
     * @param constitution  The quantifiable constitution (overall healthfullness) of the
     *                      player.
     * @param dexterity     The quantifiable dexterity (overal agility, reflexes and speed) of the
     *                      player.
     * @param inteligence   The quantifiable intelligence of the player.
     * @param iID           database ID of the player
     * @param desc          description of the player
     */
    public Player(int iID,
                  String name,
                  String desc,
                  String login,
                  String password,
                  int currHitPoints,
                  int maxHitPoints,
                  int strength,
                  int constitution,
                  int dexterity,
                  int inteligence) {
        strCurrentCommand = new StringBuffer();
        this.iID = iID;
        this.name = name;
        this.description = desc;
        this.login = login;
        this.password = password;
        this.maxHP = maxHitPoints;
        if(maxHitPoints < 1) {
            maxHitPoints = 1;
        }
        this.currHP = currHitPoints;
        if(currHitPoints > maxHitPoints) {
            currHitPoints = maxHitPoints;
        }
        this.strength = strength;
        this.constitution = constitution;
        this.dexterity = dexterity;
        this.intelligence = inteligence;

        slots = new HashMap<String, Slot>();
    }

    public static Map<String, Field> getFlagsByAlias() {
        return flagsByAlias;
    }

    public static Set<Field> getFlags() {
        return flags;
    }


    /**
     * Determines whether an instance of this class is equal to another instance of this class.
     *
     * @param o The object to compare this instance to (might not be of the same class)
     * @return true if the login of this instance matches the login of instance o or false if
     *         the class of o does not match the class of this instance or the login of class
     *         o does not match the login of this instance.
     */
    public boolean equals(Object o) {
        return o.getClass() == this.getClass() && ((Player) o).login.equals(this.login);
    }

    /**
     * Get the hash code for this Player instance which is simply the hashcode of the
     * Player's login.
     *
     * @return The int hash code for this player
     */
    public int hashCode() {
        return login.hashCode();
    }

    /**
     * Get the player's DB IDessage
     *
     * @return Player's DB ID
     */
    public int getID() {
        return iID;
    }

    /**
     * Get the login name used to access this player
     *
     * @return The login name String associated with this player
     */
    public String getLogin() {
        return login;
    }

    /**
     * Set the login name to be associated with this Player instance.
     *
     * @param login The String containing the new login for this Player instance.
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Get the password associated with this player instance
     *
     * @return The users password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the password associated with this player instance
     *
     * @param password The users password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Check if a login object has a username and password that match this user
     *
     * @param login A login object (username and password) to match against this player
     * @return return true if the submitted login and password match this player's login and password, else false.
     */
    public boolean checkLogin(Login login) {

        // getLogin and getPassword return string buffers
        return login.getLogin().toString().equals(this.login) && login.getPassword().toString().equals(password);
    }

    /**
     * Get the text prompt associated with this player instance
     *
     * @return The users prompt
     */
    public String getPrompt() {
        return "[" + getCurrHitPoints() + "/" + getMaxHitPoints() + "]";
    }

    /**
     * Check if this player is alive
     *
     * @return True if the player is alive, otherwise false
     */
    public boolean isAlive() {
        return currHP > 0;
    }

    /**
     * Set the player's in-game name (not their login)
     *
     * @param name The new name to assign to the player
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the player's in-game name (not their login)
     *
     * @return The player's name (may contain spaces)
     */
    public String getName() {
        return name;
    }

    public void addSlot(Slot slot) {
        slots.put(slot.getName(), slot);
    }

    public Slot getSlot(String name) {
        return slots.get(name);
    }

    public List<Slot> getSlots() {
        return new LinkedList<Slot>(slots.values());
    }

    /**
     * Assign the player to a room, essentially moves the player.
     * <p/>
     * Caution: this does not cancel any reference to the player that the
     * room may have.
     *
     * @param rm The room to connect the player to, or put the player "in"
     */
    public void setRoom(Room rm) {
        room = rm;
    }

    /**
     * Return the room that the player is "in"
     * <p/>
     * Note: The room or rooms that have a reference to this player may not be the same
     * as the room the player has a reference to. (Although they should be, or something is
     * wrong)
     *
     * @return The room that the player has a reference to.
     */
    public Room getRoom() {
        return room;
    }

    /**
     * Return the player's Armour Class, the quantifiable amount of protection that the
     * player has from wearing armour.
     *
     * @return An int equal to or greater than zero.
     */
    public int getAC() {
        return iAC;
    }

    /**
     * Deduct hit points from the player and determine if the player has been killed.
     *
     * @param iDmg The amount of hit points to deduct from the players current hit points.
     * @return true if the player has died, false if the player is still alive.
     */
    public boolean hurt(int iDmg) {
        // what if player is already dead?
        if(iDmg <= currHP) {
            currHP -= iDmg;
        } else {
            currHP = 0;
            return true;
        }
        return false;
    }

    /**
     * Add hit points to the player and determine how many hit points the player now has.
     * <p/>
     * Trying to add more hit points to the players current hit points greater than the
     * difference between the player's max hit points and current hit points will NOT cause
     * the player to have more current than max hit points. In other words, extra hit points
     * are, in effect, wasted.
     *
     * @param iHitPoints The number of hit points to add to the player's current hit points.
     * @return An int specifying the resulting current hit points for the player
     */
    public int heal(int iHitPoints) {
        // if there are more hit points to add than the player can have,
        // set their current hit points to the max
        if((iHitPoints + currHP) > maxHP) {
            currHP = maxHP;
        } else {
            // else add the new hit points
            currHP += iHitPoints;
        }
        return currHP;
    }

    /**
     * Determine the number of hit points a player currently has
     *
     * @return An int specifying the current hit points for the player
     */
    public int getCurrHitPoints() {
        return currHP;
    }

    /**
     * Determine the maximum number of hit points a player can have
     *
     * @return An int specifying the maximum hit points for the player
     */
    public int getMaxHitPoints() {
        return maxHP;
    }

    /**
     * Increase the maximum number of hit points the player can have
     *
     * @param iHitPoints The number of hit points to raise the player's
     *                   maximum hit points by
     * @return An int specifying what the resulting maximum number of hit points
     *         this player can have is
     */
    public int addHitPoints(int iHitPoints) {
        return maxHP += iHitPoints;
    }

    /**
     * Decrease the maximum number of hit points the player can have
     *
     * @param iHitPoints The number of hit points to lower the player's
     *                   maximum hit points by
     * @return An int specifying what the resulting maximum number of hit points
     *         this player can have is
     */
    public int removeHitPoints(int iHitPoints) {
        if(maxHP > iHitPoints) {
            maxHP -= iHitPoints;
        } else {
            maxHP = 1;
        }
        if(currHP > maxHP) {
            currHP = maxHP;
        }
        return maxHP;
    }

    /**
     * Determine if the player is currently poisoned
     * <br>
     * This must be rewritten to reflect the new poison resistance array
     *
     * @return true if the player is poisoned, false if the player is not poisoned
     */
    public boolean getPoisoned() {
        /*
        if(iPoisonStrength > 0){
          return true;
        }else{
          return false;
        }
        */
        return false;
    }

    /**
     * Poison the player
     *
     * @param iPoisonStrength The strength of the poison to poison the player with
     * @return An int representing the total combined strength of all poisons this
     *         player has been poisoned with
     */
    public int poison(int iPoisonStrength) {
        //return this.iPoisonStrength += iPoisonStrength;
        return 1;
    }

    /**
     * Cure the player of poison
     * <p/>
     * If the healing strength of the cure is greater than the strength of the total
     * combined strength of the poison that the player has been poisoned with the player
     * does NOT maintain the "leftover" strength of the cure.
     *
     * @param iHealingStrength The strength of the cure to counteract the poison
     *                         that the player has been poisoned with.
     * @return An int representing the total combined strength of all poisons this
     *         player is still poisoned with
     */
    public int curePoison(int iHealingStrength) {
        /*
        if(iPoisonStrength > iHealingStrength){
          iPoisonStrength -= iHealingStrength;
        }else{
          iPoisonStrength = 0;
        }
        */
        return 1;
    }

    /**
     * Set this player's description
     *
     * @param desc The new description for the player
     */
    public void setDescription(String desc) {
        this.description = desc;
    }

    /**
     * Get this player's description
     *
     * @return The description of this player
     */
    public String getDescription() {
        return this.description;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isAutoLook() {
        return autoLook;
    }

    public void setAutoLook(boolean autoLook) {
        this.autoLook = autoLook;
    }

    public boolean grab(Item item) {
        // if we can find a slot that is a grabber and isn't full then we're good to go
        for(Slot slot : slots.values()) {
            if(slot.isGrabber() && !slot.isFull()) {
                slot.addItem(item);
                return true;
            }
        }
        return false;
    }

    public boolean hasFreeGrabber() {
        // if we can find a slot that is a grabber and isn't full then we're good to go
        for(Slot slot : slots.values()) {
            if(slot.isGrabber() && !slot.isFull()) {
                return true;
            }
        }
        return false;
    }
}
