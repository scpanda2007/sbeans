package jmud;

//import java.nio.channels.*;
//import java.util.LinkedList;

import jmud.item.Item;

import java.util.*;

/*
 * Room.java
 *
 * Created on ?
 *
 * History
 *
 * Programmer:     Change:                                           Date:
 * ----------------------------------------------------------------------------------
 * Chris M         Cleaned up comments                               Feb 13, 2007
 *
 *
 * ToDo: consider allowing the player to pick room description length by # of sentences
 *         rather than "short" or "long".
 */

/**
 * A container for <CODE>Players<CODE>, <CODE>Mobs</CODE> and <CODE>Items</CODE>.
 * Links to other <CODE>Room</CODE> objects to form a world.
 *
 * @author Chris Maguire
 * @version 0.1
 */
public class Room {

    private final int PLAYERS = 8;
    private final int MOBS = 10;
    private final int ITEMS = 20;
    private final Object lock = new Object();

    private int id;
    private int x;
    private int y;
    private int z;
    private String strLongDesc;
    private String strShortDesc;
    private String strExits;
    private int iVisibility;
    private Hashtable<Object, PlayerChannel> playerChannels = new Hashtable<Object, PlayerChannel>(PLAYERS);
    private Hashtable<String, PlayerChannel> playerChannelsByName = new Hashtable<String, PlayerChannel>(PLAYERS);
    private Hashtable<Mob, Mob> mobs = new Hashtable<Mob, Mob>(MOBS);
    private Hashtable<String, List<Mob>> mobsByAcronym = new Hashtable<String, List<Mob>>(MOBS);
    private Hashtable<String, List<Mob>> mobsByName = new Hashtable<String, List<Mob>>(MOBS);
    // key the items on Object so that we can use the item itself, or the item's name, for the key
    private Hashtable<Item, Item> items = new Hashtable<Item, Item>(ITEMS);
    private Hashtable<String, List<Item>> itemsByName = new Hashtable<String, List<Item>>(ITEMS);

    // used for writing to a players SocketChannel
    // (which is wierd because why wouldn't the PlayerChannel be used to send a message
    //  to a player)
    //private ChannelWriter cw = new ChannelWriter();

    // 10 way linked list!!
    public Room north = null;
    public Room south = null;
    public Room east = null;
    public Room west = null;
    public Room northeast = null;
    public Room northwest = null;
    public Room southeast = null;
    public Room southwest = null;
    public Room up = null;
    public Room down = null;

    /**
     * Creates a default <CODE>Room</CODE> object
     */
    public Room() {
        strShortDesc = "[no description]";
        strLongDesc = "[no description]";
    }

    /**
     * Creates a <CODE>Room</CODE> with short and long descriptions
     *
     * @param strShortDesc The short description of the <CODE>Room</CODE>
     * @param strLongDesc  The long description of the <CODE>Room</CODE>
     */
    public Room(String strShortDesc, String strLongDesc) {
        this.strShortDesc = strShortDesc;
        this.strLongDesc = strLongDesc;
    }

    /**
     * Creates a <CODE>Room</CODE> with short and long descriptions, an Id and x, y and
     * z coordinates.
     *
     * @param strShortDesc The short description of the <I>room</I>
     * @param strLongDesc  The long description of the <I>room</I>
     * @param iID          The database ID of the <I>room</I>
     * @param iX           The x coordinate of the <I>room</I>
     * @param iY           The y coordinate of the <I>room</I>
     * @param iZ           The z coordinate of the <I>room</I>
     */
    public Room(String strShortDesc, String strLongDesc, int iID, int iX, int iY, int iZ) {
        this.strShortDesc = strShortDesc;
        this.strLongDesc = strLongDesc;
        id = iID;
        x = iX;
        y = iY;
        z = iZ;
    }

    /**
     * Links a <CODE>Room</CODE> to this <I>room</I> in a specified direction
     *
     * @param room The <CODE>Room</CODE> object to add
     * @param dir  The acronym of the direction to add the <CODE>Room</CODE>
     */
    public void addRoom(Room room, String dir) {
        // DEBUG:
        //System.out.println(" conn " + dir + "; ");

        try {
            switch(dir.charAt(0)) {
                case'N': // n
                    // make sure "N" doesn't come first, because N,NE,NW all START with "N"
                    if(dir.startsWith("NE")) {
                        northeast = room;

                        // DEBUG:
                        //System.out.println("connecting north east");
                    } else if(dir.startsWith("NW")) {
                        northwest = room;

                        // DEBUG:
                        //System.out.println("connecting north west");
                    } else if(dir.startsWith("N")) {
                        north = room;

                        // DEBUG:
                        //System.out.println("connecting: " + this.getShortDescription() + " -> " + north.getShortDescription());
                    }
                    break;
                case'E': // e
                    east = room;
                    break;
                case'S': // s
                    if(dir.startsWith("SE")) {
                        southeast = room;
                    } else if(dir.startsWith("SW")) {
                        southwest = room;
                    } else if(dir.startsWith("S")) {
                        south = room;
                    }
                    break;
                case'W': // w
                    west = room;
                    break;
                case'U':
                    up = room;
                    break;
                case'D':
                    down = room;
                    break;
            }
            // why are we catching these if we're not using them?
        } catch(IndexOutOfBoundsException e) {

        } catch(NullPointerException e) {

        }
    }

    /**
     * Returns the ID of this <I>room</I>
     *
     * @return The ID of this <I>room</I>
     */
    public int getID() {
        return id;
    }

    /**
     * Get the X, Y and Z coordinates for this room
     *
     * @return An int array containing the x, y and z coordinates for this room
     */
    public int[] getCoords() {
        return new int[]{x, y, z};
    }

    /**
     * Create the exit string by manually checking for exits in each direction
     */
    public void setExits() {
        StringBuffer strbExits = new StringBuffer();
        if(north != null) {
            strbExits.append("n ");
        }
        if(south != null) {
            strbExits.append("s ");
        }
        if(east != null) {
            strbExits.append("e ");
        }
        if(west != null) {
            strbExits.append("w ");
        }
        if(northeast != null) {
            strbExits.append("ne ");
        }
        if(northwest != null) {
            strbExits.append("nw ");
        }
        if(southeast != null) {
            strbExits.append("se ");
        }
        if(southwest != null) {
            strbExits.append("sw ");
        }
        if(up != null) {
            strbExits.append("u ");
        }
        if(down != null) {
            strbExits.append("d");
        }
        strExits = strbExits.toString();
    }

    /**
     * Get the list of exits for this room
     *
     * @return A string that lists the exit accronyms (e.g. e, w, etc.) for this room
     */
    public String getExits() {
        return "[ " + strExits + "]";
    }

    /**
     * Get the short description for this room. Each room has a short description
     * and a long description so that users can choose the amount of detail that they want.
     *
     * @return The short description for this room
     */
    public String getShortDescription() {
        return strShortDesc;
    }

    /**
     * Set the short description for this room. Each room has a short description
     * and a long description so that users can choose the amount of detail that they want.
     *
     * @param strShortDescription The short description for this room
     */
    public void setShortDescription(String strShortDescription) {
        strShortDesc = strShortDescription;
    }

    /**
     * Get the long description for this room. Each room has a short description
     * and a long description so that users can choose the amount of detail that they want.
     *
     * @return The short description for this room
     */
    public String getLongDescription() {
        return strLongDesc;
    }


    /**
     * Set the long description for this room. Each room has a short description
     * and a long description so that users can choose the amount of detail that they want.
     *
     * @param strLongDescription The short description for this room
     */
    public void setLongDescription(String strLongDescription) {
        strLongDesc = strLongDescription;
    }

    /**
     * Get the visibility "index" for this room. Visibility refers to how this room affects
     * players' abilities to see through it. This comes into play when a player wants to
     * see or map adjacent rooms without having been in them (in which case they would be
     * in their memory - which is stored in the database)
     *
     * @return The short description for this room
     */
    public int getVisibility() {
        return iVisibility;
    }

    /**
     * Set the visibility "index" for this room. Visibility refers to how this room affects
     * players' abilities to see through it. This comes into play when a player wants to
     * see or map adjacent rooms without having been in them (in which case they would be
     * in their memory - which is stored in the database)
     *
     * @param iVis The short description for this room
     */
    public void setVisibility(int iVis) {
        iVisibility = iVis;
    }

    /**
     * Get an Iterator to iterate through all the PlayerChannels in the room's
     * PlayerChannel hashtable.
     *
     * @return An Iterator for all the PlayerChannels in this room.
     */
    public Iterator getPlayerChannels() {
        return playerChannels.values().iterator();
    }

    /**
     * Checks the hashtable for a PlayerChannel with a matching
     * player
     *
     * @param p player to look for in this room
     * @return true if the player is found, false if the player is not found
     */
    public boolean hasPlayer(Player p) {
        /*
          Now this is risky because we're assuming that the hashcode of a player
          channel will always be the same as the hashcode for a player.
          That's silly. (But I'm leaving like this for now)
        */
        return (playerChannels.get(p) != null);
    }

    /**
     * Checks the hashtable for a PlayerChannel with a partialName starting with
     * <code>partialName</code> (that is, the parameter partialName).
     *
     * @param partialName The start of the player's partialName to look for in this room
     * @return The <code>Player</code>'s partialName if a <code>Player</code> is found
     *         else null.
     */
    public List<PlayerChannel> getPlayers(String partialName) {
        List<PlayerChannel> playerChannels = new ArrayList<PlayerChannel>();

        partialName = partialName.toLowerCase();

        // get all the keys in the player partialName hash
        // check the start of each String (i.e. not Player hashcodes) key against partialName
        for(String fullName : playerChannelsByName.keySet()) {
            // Check if any the current player partialName starts with the partialName provided
            if((fullName).toLowerCase().startsWith(partialName)) {
                // since we found a match, add that partialName to the set of matching player names
                playerChannels.add(playerChannelsByName.get(fullName));
            }
        }
        return playerChannels;
    }

    /* Checks the mob hashtable for a matching Mob
    *
    * @param m The mob to check the room for
    *
    * @return <code>true</code> if the <code>Mob</code> is in the room, or <code>false</code> if
    *         it isn't.
    */
    public boolean hasMob(Mob m) {
        return (mobs.get(m) != null);
    }

    /**
     * Checks the <code>Mob</code> hashtable for matching <code>Mob</code> names. If there
     * is a matching acronym, we'll just return the one partialName. If there are more than one
     * full names that match (i.e. they just typed the beginning of the partialName) then we'll
     * return a hashset of them all (no dups in a set).
     *
     * @param partialName The partialName or acronym of the <code>MobType</code> of the <code>Mob</code> to match
     * @return a hashset containing either one Mob partialName that matched the acronym or all the <code>Mob</code>
     *         names whose beginnings match <code>partialName</code>
     */
    public List<Mob> getMobs(String partialName) {
        List<Mob> mobs = new ArrayList<Mob>();
        List<Mob> list;

        partialName = partialName.toLowerCase();

        // First we'll check the acronym hash
        // to find out if the player used the acronym
        list = mobsByAcronym.get(partialName);
        if(list != null && !list.isEmpty()) {
            return mobs;
        }

        // get all the keys in the Mob partialName hash
        // check the start of each String (i.e. not Mob hashcodes) key against partialName
        for(String name : mobsByName.keySet()) {
            if(name.toLowerCase().startsWith(partialName)) {
                mobs.addAll(mobsByName.get(name));
            }
        }
        return mobs;
    }

    public List<Item> getItems(String partialName) {
        List<Item> items = new ArrayList<Item>();
        List<Item> list;

        partialName = partialName.toLowerCase();

        // First we'll check the acronym hash
        // to find out if the player used the acronym
        list = itemsByName.get(partialName);
        if(list != null && !list.isEmpty()) {
            return list;
        }

        // get all the keys in the Mob partialName hash
        // check the start of each String (i.e. not Mob hashcodes) key against partialName
        for(String name : itemsByName.keySet()) {
            if(name.toLowerCase().startsWith(partialName)) {
                items.addAll(itemsByName.get(name));
            }
        }
        return items;
    }

    /**
     * Get a comma separated list of all monsters and players in the room
     *
     * @param player Player requesting the names - they don't need to be told that they're in the room
     * @return String containing a comma-separated list of all monsters and players in this room except the
     *         name of the player who's requesting the list
     */
    public String getMobAndPlayerNames(Player player) {
        StringBuffer names = new StringBuffer();
        boolean bMultipleNames = false;

        // this will happen O(n) where n is number of mobs
        // should consider hash if mobs gets large (> 10) which is unlikely
        //for(Enumeration e = mobsByName.keys(); e.hasMoreElements();){
        //Scratch that: we need to be able to display "Mob" or "Dead mob", so we'll
        //need mobs by object, not by name
        for(Object o : mobs.values()) {

            /* now, here I could just tack on however many ", " + Mob names as I needed and then
               chop off the first ", " with StringBuffer.delete(0,2)
               Or, I can keep a boolean of whether or not we need the ", " and set it to
               true after the first element. I don't know which is faster.
               I'm assuming the branch is faster than the StringBuffer alteration
            */
            if(bMultipleNames) {
                //names.append(", ").append((String) o);
                //Scratch that: we now have the mob, we need the mob's name
                names.append(", ");
            }

            names.append(((Mob) o).getName())
                .append(player.isDebug() ? " [" + ((Mob) o).getID() + "]" : "")
                .append(((Mob) o).isAlive() ? "" : "Dead");

            bMultipleNames = true;

            // Add the mob name to the list and a comma if there are more mobs
            // carefull with the parenthesis on this one, the plus sign can mess things up
            //strbMobNames.append(((Mob)i.next()).getName() + (i.hasNext()?", ":""));
            //Scratch that: we'll just set a flag after we've added the first name to say that we need
            // a comma separator before any more names are added
        }

        return names.toString();
    }


    /**
     * Get a comma separated list of all items in the room
     *
     * @param isDebug should debugging information be included
     * @return String containing a comma-separated list of all monsters and players in this room except the
     *         name of the player who's requesting the list
     */
    public String getItemNames(boolean isDebug) {
        StringBuffer items = new StringBuffer();
        boolean bMultipleItems = false;

        // get the list of items from the hash
        for(Item item : this.items.values()) {
            if(bMultipleItems) {
                items.append(", ");
            }

            items.append(item.getName())
                .append(isDebug ? " [" + item.getId() + "]" : "");
            bMultipleItems = true;
        }

        return items.toString();
    }

    public String getItemNames() {
        return getItemNames(false);
    }

    /**
     * Stores the players in a hashmap so that they can be returned quickly
     * by name
     *
     * @param pc PlayerChannel to add to the room
     * @see #add(Mob)
     * @see #add(Item)
     */
    public void add(PlayerChannel pc) {
        /* we need to add the PlayerChannel based on the players hashcode
           *AND* name. Why, you ask?
           Because, the whole point of having a hashtable is so that we
           can pull out objects based on their name!

           (But why do we need them indexed by hash code?)
        */

        // map the PlayerChannel with the builtin hashcode
        playerChannels.put(pc, pc);

        // map the PlayerChannel with the player's name
        playerChannelsByName.put(pc.getPlayer().getName(), pc);
    }

    /**
     * Add a <code>Mob</code> to this <code>Room</code> and index it
     * by its name, acronym and hashcode. (Why?)
     *
     * @param m The <code>Mob</code> to add to this <code>Room</code>
     * @see #add(Item)
     * @see #add(PlayerChannel)
     */
    public void add(Mob m) {
        try {
            String acronym = m.getMobType().getAcronym();

            // if there are any mobs by that acronym already there
            // will be a list in the hash by that acronym, so retrieve it
            // and add the new mob, else create a list, add it to the list,
            // and add the list to the hash
            List<Mob> mobs = mobsByAcronym.get(acronym);

            if(mobs != null) {
                mobs.add(m);
            } else {
                mobs = new ArrayList<Mob>();
                mobs.add(m);
                mobsByAcronym.put(acronym, mobs);
            }

            // same deal as the acronyms above
            mobs = mobsByName.get(m.getName());

            if(mobs != null) {
                mobs.add(m);
            } else {
                mobs = new ArrayList<Mob>();
                mobs.add(m);
                mobsByName.put(acronym, mobs);
            }

            // map the mob hashcodes to themselves
            // (Mob hascode is the database ID of the mob)
            this.mobs.put(m, m);

        } catch(Exception e) {
            System.out.println("Room (" + x + "," + y + "," + z + "): add(Mob m): Failed. " + e.toString());
        }

    }

    /**
     * Add an <code>Item</code> to this <code>Room</code> and index it
     * by its name and hashcode. (Why by hashcode?)
     *
     * @param i The <code>Item</code> to add to this <code>Room</code>
     * @see #add(Mob)
     * @see #add(PlayerChannel)
     */
    public void add(Item i) {
        List<Item> items;
        String name = i.getName();

        // index the item by its hashcode
        this.items.put(i, i);

        // if there are any items by that name already there
        // will be a list in the hash by that name, so retrieve it
        // and add the new item, else create a list, add it to the list,
        // and add the list to the hash
        items = itemsByName.get(name);

        if(items != null) {
            items.add(i);
        } else {
            items = new ArrayList<Item>();
            items.add(i);
            itemsByName.put(name, items);
        }
    }

    /**
     * Return any <code>PlayerChannel</code> with a name that matches name
     * <p/>
     * The full name should be retrieved from the room before calling getPlayerChannel(String name)
     *
     * @param name The full name to match against all players in the <code>Room</code>
     * @return The first matching <code>PlayerChannel</code> if any, else null
     * @see #getPlayers(String)
     */
    public PlayerChannel getPlayerChannel(String name) {
        try {
            return playerChannelsByName.get(name);
        } catch(Exception e) {
            System.out.println("Room (" + x + "," + y + "," + z + "): getPlayer(String name): Failed.");
            return null;
        }
    }

    /**
     * Return any player in this <code>Room</code> with a name that matches name
     * <p/>
     * The full name should be retrieved from the room before calling getPlayerChannel(String name)
     *
     * @param name The full name to match against all players in the room
     * @return The first matching <code>Player</code> if any, else null
     * @see #getPlayers(String)
     */
    public Player getPlayer(String name) {
        try {
            return playerChannels.get(name).getPlayer();
        } catch(Exception e) {
            System.out.println("Room (" + x + "," + y + "," + z + "): getPlayer(String name): Failed.");
            return null;
        }
    }

    /**
     * Return any <code>Mob</code> in this <code>Room</code> with a name that matches <code>name</code>
     * <p/>
     * The full name should be retrieved from the room before calling getMob(String name)
     *
     * @param name The full name to match against all <code>Mob</code>s in the <code>Room</code>
     * @return The first matching <code>Mob</code> if any, else null
     */
    public Mob getMob(String name) {
        List<Mob> mobs = mobsByName.get(name);
        if(mobs != null && !mobs.isEmpty()) {
            return mobs.get(0);
        }
        return null;
    }

    public Mob getMob(Mob m) {
        return mobs.get(m);
    }

    /**
     * Return any <code>Item</code> in this <code>Room</code> with a name that matches <code>name</code>
     *
     * @param name The name to match against all <code>Item</code>s in the <code>Room</code>
     * @return The first matching <code>Mob</code> if any, else null
     */
    public Item getItem(String name) {
        for(String str : itemsByName.keySet()) {
            if(str.toLowerCase().startsWith(name.toLowerCase()) && !itemsByName.get(str).isEmpty()) {
                return itemsByName.get(str).get(0);
            }
        }
        return null;
    }

    /**
     * Removes a <code>PlayerChannel</code> (A <code>Player</code> and their <code>SocketChannel</code>)
     * from this <code>Room</code>
     *
     * @param pc The <code>PlayerChannel</code> that contains the player to remove
     * @see #remove(Mob)
     * @see #remove(Item)
     */
    public void remove(PlayerChannel pc) {
        Player p = pc.getPlayer();
        try {
            // hashtable elements must be removed with the key objects, not with things that
            // hash out to the same value as the key object (I'm pretty sure anyway)
            playerChannels.remove(pc);
            playerChannelsByName.remove(p.getName());
        } catch(Exception e) {
            System.out.println("Room (" + x + "," + y + "," + z + "): remove(Player p): Failed.");
        }
    }

    /**
     * Removes a <code>Player</code> from this <code>Room</code>
     *
     * @param m The <code>Mob</code> that is to be removed
     * @see #remove(PlayerChannel)
     * @see #remove(Item)
     */
    public void remove(Mob m) {
        try {
            mobs.remove(m);
            mobsByName.remove(m.getName());
            mobsByAcronym.remove(m.getMobType().getAcronym());
        } catch(Exception e) {
            System.out.println("Room (" + x + "," + y + "," + z + "): remove(Mob m): Failed.");
        }
    }

    /**
     * Removes an <code>Item</code> from this <code>Room</code>
     *
     * @param i The <code>Item</code> that is to be removed
     * @return The item that was removed or null if the item wasn't in the room
     * @see Room#remove(PlayerChannel)
     * @see #remove(Mob)
     */
    public synchronized Item remove(Item i) {
        List<Item> items;
        Item item = this.items.remove(i);

        items = itemsByName.get(i.getName());
        if(items != null && !items.isEmpty()) {
            items.remove(0);
            if(items.isEmpty()) {
                // remove the list from the hash (using the list's key)
                itemsByName.remove(i.getName());
            }
        }
        return item;
    }

    /**
     * Send a message to a Player via their SocketChannel
     *
     * @param strMessage String message to send
     * @param socketchannel where to send the message
     * @throws Exception Throws any exception caused by ChannelWriter.sendMessage
     */
//  public void sendMessage(String strMessage, SocketChannel socketchannel) throws Exception{
//    cw.sendMessage(strMessage, socketchannel);
//  }
// Use PlayerChannel.SendMessage(String) instead


    /**
     * Send a message to all players in a room via their SocketChannels
     *
     * @param strMessage String message to send
     * @throws Exception Throws any exception caused by ChannelWriter.sendMessage
     */
    public void sendMessageToAll(String strMessage) throws Exception {
        PlayerChannel playerChannel;

        for(Enumeration e = playerChannels.elements(); e.hasMoreElements();) {
            playerChannel = (PlayerChannel) e.nextElement();
            playerChannel.sendMessage(strMessage + playerChannel.getPlayer().getPrompt());

            /*
            sendMessage(strMessage
                        + playerChannel.getPlayer().getPrompt(),
                        playerChannel.getSocketChannel());
            */
        }
    }

    /**
     * Send a message to all <code>Players</code> in a <code>Room</code> except one. This is to facilitate sending a
     * message to all <code>Players</code> but the one who is the initiator of the message.
     * E.g. A <code>Player</code> who chats, attacks, etc..
     *
     * @param strMessage            The message to send to all other players
     * @param playerChannelExcluded The playerChannel of the player NOT to send the message to
     * @throws Exception Throws any exception caused by ChannelWriter.sendMessage
     */
    public void sendMessageToAllButOne(String strMessage, PlayerChannel playerChannelExcluded) throws Exception {
        PlayerChannel playerChannel;

        // loop through all the PlayerChannels in the room
        for(Enumeration e = playerChannels.elements(); e.hasMoreElements();) {
            playerChannel = (PlayerChannel) e.nextElement();

            // if the current PlayerChannel isn't the PlayerChannel we're excluding then
            // send them the message
            if(!playerChannel.equals(playerChannelExcluded)) {
                //sendMessage(strMessage + playerChannel.getPlayer().getPrompt(), playerChannel.getSocketChannel());
                playerChannel.sendMessage(strMessage + playerChannel.getPlayer().getPrompt());
            }
        }
    }

}
