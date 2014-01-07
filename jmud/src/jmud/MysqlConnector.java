package jmud;

import jmud.item.Item;
import jmud.slot.Slot;

import java.lang.reflect.Constructor;
import java.sql.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/*
 * MysqlConnector.java
 *
 * Created on April 30, 2002, 9:49 PM
 *
 * History
 *
 * Programmer:     Change:                                           Date:
 * ----------------------------------------------------------------------------------
 * Chris M         Cleaned up comments                               Feb 13, 2007
 *
 * ToDo: finish the list of attributes in loadAttrIDs()
 * ToDo: finish the loadModIDs() function
 */

/**
 * Provides access to the database and functions to return specific data from the database
 *
 * @author Chris Maguire
 * @version 0.1
 */

public class MysqlConnector {

    protected Connection conn;
    protected ResultSet rs;
    protected static String dbUrl = "jdbc:mysql:///jmud?user=root&password=Quadra04";
    protected Statement stmt;

    private final int ATTRIBUTE_PARAM_INDEX = 1;
    private final int MODIFIER_PARAM_INDEX = 2;
    private final int ATTRIBUTE_COLUMN_INDEX = 1;
    private final int MODIFIER_COLUMN_INDEX = 2;
    private final int PLAYERID_PARAM_INDEX = 1;
    private final int LOGIN_PARAM_INDEX = 1;
    private final int PASSWORD_PARAM_INDEX = 2;

    int iStrengthAttributeID;
    int iDexterityAttributeID;
    int iDamageModifierID;

    /**
     * Constructs a new MysqlConnector for accessing the database
     */
    public MysqlConnector() {

    }

    /**
     * Create a connection to the database and a database statement
     * which is the equivalent of a ADODB Command object (I think)
     *
     * @throws Exception if something fails creating the db driver
     */
    public void setup() throws Exception {
        /* use the default constructor for the jdbc driver class
        * to create a new instance to obtain a connection object
        *
        * the driver package com directory must be in the same
        * directory as the jmud source
        */
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        conn = DriverManager.getConnection(dbUrl);
        //stmt = conn.createStatement();

        // prepare a callable statement for each of our stored procedures
        /*
         http://dev.mysql.com/doc/refman/5.0/en/connector-j-usagenotes-basic.html
         mentions that callable statements should be reused, however I couldn't find
         a way to reuse callable statements across stored procedures. I assume the author
         meant to reuse the same callable statement for that particular sproc.

         Forget that: if we're going to create the callable statements every time we
         get a connection then we might as well just create the statements we know we're going to
         use. This should save us time as we're not recreating callable statements that we're not
         going to use all the time.
        */
    }

    /**
     * Close the recordset and connection objects, in that order.
     */
    public void close() {
        if(rs != null) {
            try {
                rs.close();
            } catch(SQLException SQLE) {
            }
        }
        if(conn != null) {
            try {
                conn.close();
            } catch(SQLException SQLE) {
            }
        }
    }

    /**
     * Find all the attribute IDs in the database and assign them to local variables
     * so that attributes can be referred to by variable rather than constant
     *
     * @throws java.sql.SQLException if something goes awry with the db calls
     */
    public void loadAttrIDs() throws SQLException {
        String strAttribute = "";
        int iAttributeID = 0;

        System.out.print("loading attribute IDs: ");

        // create and execute a callable statement representing a stored procedure
        rs = conn.prepareCall("{call spGetAttributes}").executeQuery();

        // loop through all the records returned
        while(rs.next()) {

            // get each attribute name and ID
            strAttribute = rs.getString(1);
            iAttributeID = rs.getInt(2);

            // assign the attributes to their variables
            if(strAttribute.equalsIgnoreCase("strength")) {
                iStrengthAttributeID = iAttributeID;
                // DEBUG:
                System.out.print(".");
            } else if(strAttribute.equalsIgnoreCase("dexterity")) {
                iDexterityAttributeID = iAttributeID;
                // DEBUG:
                System.out.print(".");
            } else {
                // DEBUG: print a marker showing we found an attribute we weren't expecting
                System.out.print("?");
            }
        }
        System.out.print("\n");

        rs.close();
    }

    /**
     * ERROR! This function was copied from loadAttriIDs() but never completed
     * <p/>
     * Find all the modifier IDs in the database and assign them to local variables
     * so that modifiers can be referred to by variable rather than constant
     */
    public void loadModIDs() throws SQLException {
        String strModifier = "";
        int iModifierID = 0;

        // DEBUG:
        System.out.print("loading modifier IDs: ");

        // run the attribute query
        //rs = stmt.executeQuery(GET_ATTRIBUTE_IDS);
        //rs = cstmtGetMobs.executeQuery();

        // loop through all the attributes we found
        while(rs.next()) {
            strModifier = rs.getString(1);
            iModifierID = rs.getInt(2);

            if("damage".equalsIgnoreCase(strModifier)) {
                iDamageModifierID = iModifierID;
                System.out.print(".");
            } else {
                // print a marker showing we found a modifier we weren't expecting
                System.out.print("?");
            }
        }
        System.out.print("\n");

        rs.close();
    }

    /**
     * Load the Strength->Damage modifier array
     * <br>
     * Note: Call loadAttrIDs and loadModIDs FIRST!!
     *
     * @see MysqlConnector#loadAttrIDs
     * @see MysqlConnector#loadModIDs
     */
    public void loadModsStrDmg(int[] iStrDmgMods) throws SQLException {
        int iMaxAttrLvl = 0;

        // DEBUG:
        System.out.println("loading Strengh-Damage modifiers");

        // create and execute a callable statement representing a stored procedure
        CallableStatement cstmtGetModifiers = conn.prepareCall("{call spGetModifiers(?,?)}");
        cstmtGetModifiers.setInt(ATTRIBUTE_PARAM_INDEX, iStrengthAttributeID);
        cstmtGetModifiers.setInt(MODIFIER_PARAM_INDEX, iDamageModifierID);

        // run the statement
        //rs = pstmt.executeQuery();
        // run the sproc
        rs = cstmtGetModifiers.executeQuery();

        // find out how big to make the array by getting the
        // maximum attribute level (because each attribute level
        // needs to have a spot in the modifier array, even it only
        // occupies that spot with a zero
        while(rs.next()) {
            if(rs.getInt(1) > iMaxAttrLvl) {
                iMaxAttrLvl = rs.getInt(2);
            }
        }

        // instantiate a big enough array
        iStrDmgMods = new int[iMaxAttrLvl];

        // hopefully this sets the pointer before the first element
        rs.beforeFirst();

        // loop through all the damage modifiers and add them to the array
        while(rs.next()) {
            // add a modifier to the array at the index of the attribute level that it applies to
            iStrDmgMods[rs.getInt(ATTRIBUTE_COLUMN_INDEX)] = rs.getInt(MODIFIER_COLUMN_INDEX);
        }

        rs.close();
    }

    /**
     * Fill an Room array with all the room information stored in the database.
     * Need to know beforehand how many rooms there are. Also adds items to rooms.
     * <p/>
     * The reason an array is used and not a LinkedList or some other collection
     * is that the rooms need to be indexed by other objects by the room ID. So adding
     * each <code>Room</code> object to the <code>Room</code> Array by its ID allows
     * this to happen.
     *
     * @param rooms The Room array to fill with persisted Room objects.
     * @throws java.sql.SQLException if something comes off the rails with the database calls
     * @see MysqlConnector#getRoomCount()
     */
    public void getRooms(Room rooms[]) throws SQLException {
        // DEBUG: loading rooms
        System.out.print("loading rooms: ");

        // create and execute a callable statement representing a stored procedure
        CallableStatement cstmtGetRoomItems = conn.prepareCall("{call spGetRoomItems()}");

        // create and execute a callable statement for a stored procedure
        rs = conn.prepareCall("{call spGetRooms}").executeQuery();

        // loop through all the rooms
        while(rs.next()) {

            // DEBUG: print an indicator for each Room loaded
            System.out.print(".");

            // DEBUG:
            //System.out.print(rs.getInt(1) + " ");

            // fill the room array with rooms pulled from the database and indexed
            // by room id (Some array slots will be empty)
            rooms[rs.getInt(1)] = new Room(rs.getString(2), rs.getString(3), rs.getInt(1), rs.getInt(4), rs.getInt(5), rs.getInt(6));
        }
        System.out.print("\n");
        rs.close();

        // DEBUG: loading room items
        System.out.print("loading room items: ");

        rs = cstmtGetRoomItems.executeQuery();

        while(rs.next()) {
            // DEBUG: print an indicator for each room item we find
            System.out.print(".");

            rooms[rs.getInt(1)].add(new Item(rs.getInt(1), rs.getString(8), rs.getInt(5), rs.getInt(6), rs.getInt(4), rs.getInt(7), new LinkedList<Slot>()));
        }
        System.out.print("\n");
        rs.close();
    }


    /**
     * Put all the rooms that a player has visited into an array and map the ID of each room to the
     * index of that room in the array in a hashtable.
     *
     * @param iPlayerID ID of Player to retrieve rooms for
     * @param rooms     Array to fill with rooms
     * @return A Hash Table of the indexes of the visited rooms in the array keyed on the rooms' IDs.
     */
    public Hashtable getPlayerRooms(int iPlayerID, Room[] rooms) throws SQLException {
        int iRoomIndex = 0;
        Hashtable hashRooms = new Hashtable();

        // DEBUG:
        System.out.print("loading rooms: ");

        // create and execute a callable statement for a stored procedure
        CallableStatement cstmtGetPlayerRooms = conn.prepareCall("{call spGetPlayerRooms}");
        cstmtGetPlayerRooms.setInt(PLAYERID_PARAM_INDEX, iPlayerID);
        rs = cstmtGetPlayerRooms.executeQuery();

        // loop through all the player rooms ...
        while(rs.next()) {

            // DEBUG: print an indicator for each Room loaded
            System.out.print(".");

            // fill the room array with rooms pulled from the database
            rooms[iRoomIndex] = new Room(rs.getString(2), rs.getString(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getInt(7));

            // put the array index of this room into a hash keyed on the rooms ID
            hashRooms.put(new Integer(rooms[iRoomIndex].getID()), new Integer(iRoomIndex));

            // increment the array index
            iRoomIndex++;
        }
        System.out.print("\n");

        rs.close();

        return hashRooms;
    }

    /**
     * Return the number of <code>Room</code> records stored/persisted in the database.
     * <p/>
     * I say stored or persisted because at this point the only way to get room
     * information into the database is to enter it manually or via a script file,
     * but the plan is to have in game editing. The in game editing is close, but
     * not quite there.
     *
     * @return The number of rooms stored in the database.
     * @see MysqlConnector#getRooms(Room[])
     */
    public int getRoomCount() throws SQLException {
        int iRooms;

        // create and execute callable statment for stored procedure
        rs = conn.prepareCall("{call spGetRoomCount}").executeQuery();

        // check for one returned record
        if(rs.next()) {
            // get the max room ID, and then add 1 to account for Array length being greater
            // than Array max index
            iRooms = rs.getInt(1) + 1;
        } else {
            iRooms = 0;
        }

        rs.close();

        return iRooms;
    }

    /**
     * Return the number of <code>Room</code> records that a player has visited
     * and can remember
     *
     * @return The number of rooms this player can remember
     * @throws SQLException
     */
    public int getPlayerRoomCount(int iPlayerID) throws SQLException {
        int iRooms;

        // create and execute a callable statement for a stored procedure
        CallableStatement cstmtGetPlayerRoomCount = conn.prepareCall("{call spGetPlayerRoomCount(?)}");
        cstmtGetPlayerRoomCount.setInt(PLAYERID_PARAM_INDEX, iPlayerID);
        rs = cstmtGetPlayerRoomCount.executeQuery();

        // loop through all of the returned records
        if(rs.next()) {
            // unlike getRoomCount we're going to actually get the number of
            // player rooms, not the max room ID for that player's Player-rooms
            iRooms = rs.getInt(1);
        } else {
            iRooms = 0;
        }

        rs.close();

        // make sure we don't return less than zero
        return iRooms < 0 ? 0 : iRooms;
    }

    /**
     * Retrieve all the room connections out of the database and go through
     * the Room array adding all the connecting rooms to each room in the array.
     *
     * @param rooms The previously created <code>Room</code> array with all the rooms in it
     * @see MysqlConnector#getRooms(Room[])
     */
    public void getRoomConnections(Room rooms[]) throws SQLException {
        System.out.print("loading room connections: ");

        // create and execute the a stored procedure through a callable sql statement
        rs = conn.prepareCall("{call spGetRoomConnections}").executeQuery();

        while(rs.next()) {
            //display an indicator for each connecting room we add
            System.out.print(".");
            /*
            * The query will tell us the ID of the originating room, the ID of the
            * destination room and the direction of the conenction (for example "south").
            *
            * The Room array holds rooms indexed on their id, so we can get a reference
            * to the originating room with the Room array indexed on the first field
            * in the recordset which is the originating room's ID. Then we can call addRoom
            * and pass the destination room by using the second field in the recordset as
            * an index into the Room array because the second field is the destination room's
            * ID. We'll also pass the third field in the recordset to addRoom because this says
            * what reference in the originating room to add the destination room to.
            * (There are ten references: eight compass directions as well as up and down)
            */
            rooms[rs.getInt(1)].addRoom(rooms[rs.getInt(2)], rs.getString(3));
        }
        System.out.print("\n");

        rs.close();
    }

    /**
     * Creates connections between rooms that the player has visited so that
     * when the user requests to see what rooms they remember we can print
     * out a map of connected rooms
     *
     * @param iPlayerID ID of player to get room connections for
     * @param rooms     Rooms that the player has visited
     * @param hashRooms table of room indexes mapped to their IDs
     */
    public void getPlayerRoomConnections(int iPlayerID, Room[] rooms, Hashtable hashRooms) throws SQLException {
        int iRoomArrayIndex = 0;
        Room roomOrig;
        Room roomDest;

        // DEBUG:
        System.out.print("loading player room connections: ");

        // use a CallableStatement instead (so we can call a sproc)
        CallableStatement cstmtGetPlayerRoomConnections = conn.prepareCall("{call spGetPlayerRoomConnections(?)}");
        cstmtGetPlayerRoomConnections.setInt(PLAYERID_PARAM_INDEX, iPlayerID);
        rs = cstmtGetPlayerRoomConnections.executeQuery();

        // loop through all the room connections we found
        while(rs.next()) {

            // DEBUG: display an indicator for each connecting room we add
            System.out.print(".");

            // somehow link the rooms together based on ID, maybe store the array ordinal in a hash with the room ID?
            // This way we could say "Give me the array ordinal for the room with ID = x" and then
            // retrieve the room from the array

            // get the orig room based on it's array index based on it's ID
            roomOrig = rooms[((Integer) hashRooms.get(new Integer(rs.getInt(1))))];

            // get the dest room based on it's array index based on it's ID
            roomDest = rooms[((Integer) hashRooms.get(new Integer(rs.getInt(2))))];

            // add a connection from the orig room to the dest room for a certain direction reference
            roomOrig.addRoom(roomDest, rs.getString(3));
        }

        rs.close();
    }

    /**
     * This is the first attempt at updating the game world from in the game
     *
     * @param iRoomID The Room object to update
     * @param strDesc The new short description for the Room object
     * @throws java.sql.SQLException on statment creation or update execution
     */
    public void setRoomShortDesc(int iRoomID, String strDesc) throws SQLException {
        int iResult;
        iResult = conn.createStatement().executeUpdate("update tblRooms set vchDescShort = '" + strDesc + "' where iRoomID = " + iRoomID);
        System.out.println(iResult);

        rs.close();
    }

    /**
     * Fill a supplied LinkedList with player object data queried from the
     * database.
     *
     * @param playerList The <code>LinkedList</code> to append new <code>Player</code>
     *                   objects to.
     * @throws java.sql.SQLException on sproc prepare, sproc execute or recordset close
     */
    public void getPlayers(LinkedList<Player> playerList) throws SQLException {

        // create and execute the a stored procedure through a callable sql statement
        rs = conn.prepareCall("{call spGetPlayers}").executeQuery();

        // loop through the returned records and create Players from the records
        // to add to the passed in list of players
        while(rs.next()) {
            playerList.add(new Player(rs.getInt(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getInt(6),
                rs.getInt(7),
                rs.getInt(8),
                rs.getInt(9),
                rs.getInt(10),
                rs.getInt(11)));

            // DEBUG:
            System.out.println(((Player) playerList.getLast()).getName() + " loaded \n");
        }

        rs.close();
    }

    /**
     * Get a player from the database if one exists that matches a login and password
     *
     * @param login    player login to use (i.e. player name)
     * @param password player password
     * @return the player from the database
     * @throws java.sql.SQLException on sproc prepares, sproc executes, recordset close, sproc param assignments
     */
    public Player getPlayer(String login, String password) throws SQLException {
        int playerID;
        Player player = null;
        List<Object[]> slots = new LinkedList<Object[]>();
        List<String> aliases = new LinkedList<String>();
        List<Slot> itemSlots;

        // create a callable statement to get the player's slots once we have the players ID
        CallableStatement cstmtGetPlayerSlots = conn.prepareCall("{call spGetPlayerSlots(?)}");
        CallableStatement cstmtGetSlotAliases = conn.prepareCall("{call spGetSlotAliases(?)}");
        CallableStatement cstmtGetPlayerSlotItems = conn.prepareCall("{call spGetPlayerSlotItems(?, ?)}");

        // create and execute the a stored procedure through a callable sql statement
        CallableStatement cstmtGetPlayer = conn.prepareCall("{call spGetPlayer(?,?)}");
        cstmtGetPlayer.setString(1, login);
        cstmtGetPlayer.setString(2, password);
        rs = cstmtGetPlayer.executeQuery();

        // loop through the returned records and create Players from the records
        // to add to the passed in list of players
        if(rs.next()) {
            playerID = rs.getInt(1);

            player = new Player(playerID,
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                // not using class ID
                rs.getInt(7),
                rs.getInt(8),
                rs.getInt(9),
                rs.getInt(10),
                rs.getInt(11),
                rs.getInt(12));

            // DEBUG:
            System.out.println(player.getName() + " loaded \n");
            rs.close();
        } else {
            rs.close();
            return null;
        }

        // now that we have the player ID we can get the slots
        cstmtGetPlayerSlotItems.setInt(1, playerID);
        cstmtGetPlayerSlots.setInt(1, playerID);
        rs = cstmtGetPlayerSlots.executeQuery();

        // DEBUG:
        System.out.print("Getting player slots: ");

        while(rs.next()) {
            try {
                Class clazz = Class.forName(rs.getString(2));
                Constructor<Slot> constructor = clazz.getConstructor(new Class[]{String.class});
                Slot slot = constructor.newInstance(rs.getString(1));
                //player.addSlot(slot);
                slots.add(new Object[]{slot, rs.getInt(3)});

                // DEBUG:
                System.out.print(".");
            } catch(Exception e) {
                System.out.println("Failed to load player slot \n");
            }
        }

        rs.close();

        System.out.println("\n");

        for(Object[] o : slots) {
            // DEBUG:
            //System.out.print("Getting player slots aliases and items: ");

            aliases.clear();
            cstmtGetSlotAliases.setInt(1, (Integer) o[1]);
            rs = cstmtGetSlotAliases.executeQuery();
            while(rs.next()) {
                try {
                    aliases.add(rs.getString(1));

                    // DEBUG:
                    //System.out.print("a");
                } catch(Exception e) {
                    System.out.println("Failed to load player slot \n");
                }
            }
            rs.close();
            ((Slot) o[0]).setAliases(aliases);
            player.addSlot((Slot) o[0]);

            // set the slot ID param and get the items for that player and slot
            cstmtGetPlayerSlotItems.setInt(2, ((Integer) o[1]));

            //System.out.println("getting player slot items for player " + playerID + " and slot " + ((Integer)o[1]).toString());

            rs = cstmtGetPlayerSlotItems.executeQuery();
            while(rs.next()) {
                // add the current slot to the list of slots that this item can go in
                itemSlots = new LinkedList<Slot>();
                itemSlots.add(((Slot) o[0]));

                ((Slot) o[0]).addItem(new Item(rs.getInt(1), rs.getString(8), rs.getInt(4), rs.getInt(5), rs.getInt(3), rs.getInt(6), itemSlots));

                // DEBUG:
                //System.out.println("Item name: " + ((Slot)o[0]).getItems().get(0).getName());
                //System.out.println("Name is " + rs.getString(5));

                // DEBUG:
                //System.out.print("i");
            }
            rs.close();

            //System.out.println("\n");
        }

        rs.close();

        return player;
    }

    /**
     * Create <code>MobType</code> objects in an Array indexed by their ID so
     * that <code>Mob</code> objects can be created and given a reference to their
     * <code>MobType</code>s.
     *
     * @param mobTypes The previously instantiated (but not neccessarily initialized)
     *                 array of type <code>MobType</code> that has be instantiated to
     *                 hold at least as many <code>MobTypes</code> as the maximum
     *                 <code>MobType</code> ID. (There will be empty spaces in the array
     *                 but I'm not overly worried about that now as the <code>MobType</code>
     *                 Array is only a temporary object (NOT a temporary solution! <grin>).
     * @throws java.sql.SQLException on sproc prepare, sproc exexcute or recordset close
     */
    public void getMobTypes(MobType mobTypes[]) throws SQLException {
        // DEBUG:
        System.out.print("loading mob types: ");

        // create and execute a callable statement for a stored procedure
        rs = conn.prepareCall("{call spGetMobTypes}").executeQuery();

        // loop through the resultant records
        while(rs.next()) {

            // DEBUG:
            System.out.print(".");

            // construct a new MobType and add it to the Array
            // according to the MobTypes ID.
            mobTypes[rs.getInt(1)] = new MobType(rs.getString(2), rs.getString(3), rs.getString(4));
        }
        System.out.print("\n");

        rs.close();
    }

    /**
     * Return the number of <code>MobType</code> records stored/persisted in the database.
     * <p/>
     * I say stored or persisted because at this point the only way to get MobType
     * information into the database is to enter it manually or via a script file,
     * but the plan is to have in game editing. The in game editing is close, but
     * not quite there.
     *
     * @return The number of <code>MobType</code>s stored in the database.
     * @throws java.sql.SQLException on sproc prepare, sproc execute or rs close
     * @see MysqlConnector#getMobTypes(MobType[])
     */
    public int getMobTypeCount() throws SQLException {
        int iMobTypes;

        // create and execute a callable statement representing a stored procedure
        rs = conn.prepareCall("{call spGetMobTypeCount}").executeQuery();

        // look at the first record returned, if any
        if(rs.next()) {

            // get the max MobType ID, and then add 1 to account for Array length being greater
            // than Array max index
            iMobTypes = rs.getInt(1) + 1;

            // else we didn't find *any* mob types, so the max is zero
        } else {
            iMobTypes = 0;
        }

        rs.close();

        return iMobTypes;
    }

    /**
     * Create <code>Mob</code> objects and add them to their designated <code>Room</code>
     * objects in the supplied <code>Room</code> array.
     * <p/>
     * The <code>Room</code> array must already contain ALL <code>Room</code> objects that
     * Mobs will be assigned too.
     * <p/>
     * An ArrayIndexOutOfBounds exception will be thrown if the first field of any record
     * returned from the database is not a valid index into the <code>Room</code> array.
     *
     * @param rooms    The previously populated <code>Room</code> array containing all the rooms
     *                 for the Mud.
     * @param mobTypes The previously populated <code>MobType</code> Array containing
     *                 all the <code>MobType</code>s for the Mud.
     * @throws java.sql.SQLException on sproc prepare or execute and recordset close
     */
    public void getMobs(Room rooms[], MobType mobTypes[]) throws SQLException {

        // DEBUG:
        System.out.print("loading room mobs: ");

        // create and execute a callable statement representing a stored procedure
        rs = conn.prepareCall("{call spGetMobs}").executeQuery();

        // loop through the resultant records
        while(rs.next()) {

            // DEBUG:
            System.out.print(".");

            // Create each mob (monster) and add it to it's room
            rooms[rs.getInt(1)].add(new Mob(rs.getInt(1),
                mobTypes[rs.getInt(2)],
                rs.getInt(3),
                rs.getInt(3),
                rs.getInt(4),
                rs.getInt(5),
                rs.getInt(6)));
        }

        rs.close();

        // DEBUG:
        System.out.print("\n");
    }

    /**
     * Populate a <code>LinkedList</code> with Command + Alias pairs.
     *
     * @param commands list of commands to get aliases for
     * @throws java.sql.SQLException if the stored procedure prepare call screws up
     */
    public void getCommandAliases(LinkedList<String[]> commands) throws SQLException {

        // execute a hard coded query on the database
        //rs = stmt.executeQuery(GET_COMMAND_ALIAS_PAIRS_SQL);
        //cstmtGetCommandAliasPairs = conn.prepareCall("{call spGetCommandAliasPairs}");
        //rs = cstmtGetCommandAliasPairs.executeQuery();
        rs = conn.prepareCall("{call spGetCommandAliasPairs}").executeQuery();

        // loop through the resultant records
        while(rs.next()) {
            /*
            Create a new array with a Command + Alias pair and add it
            to the <code>LinkedList</code>
            */
            commands.add(new String[]{rs.getString(1), rs.getString(2)});
        }

        rs.close();
    }

    /**
     * update a player's room in the database
     *
     * @param iPlayerID player to insert into room
     * @param iRoomID   room to insert player into
     * @throws java.sql.SQLException creating the sproc statement, setting the values, calling the sproc
     */
    public void insertPlayerRoom(int iPlayerID, int iRoomID) throws SQLException {
        CallableStatement cstmtInsertPlayerRoom = conn.prepareCall("{call spInsertPlayerRoom(?,?)}");
        cstmtInsertPlayerRoom.setInt(1, iPlayerID);
        cstmtInsertPlayerRoom.setInt(2, iRoomID);
        cstmtInsertPlayerRoom.executeUpdate();
    }

    public void getSlotAliases(HashMap<String, String> slotAliases) throws SQLException {

        // DEBUG:
        System.out.print("Getting slot aliases: ");

        // create and execute the a stored procedure through a callable sql statement
        rs = conn.prepareCall("{call spGetSlotAliases}").executeQuery();

        // loop through the returned records and create Players from the records
        // to add to the passed in list of players
        while(rs.next()) {
            slotAliases.put(rs.getString(1), rs.getString(2));

            // DEBUG:
            System.out.print(".");
        }

        // DEBUG:
        System.out.println("");

        rs.close();
    }

    /**
     * Used for stand-alone testing of database queries
     *
     * @param args the command line arguments
     */
    /* keeping this for testing
    public static void main (String args[]) {
      Room rooms[];
      MysqlConnector mSqlConn = new MysqlConnector();
      mSqlConn.setup();
      try{
         rooms = new Room[mSqlConn.getRoomCount()];
         mSqlConn.getRooms(rooms);
         mSqlConn.getRoomConnections(rooms);
         mSqlConn.close();
      }catch(SQLException se){
         System.out.println(se);
      }
    }
    **/
}
