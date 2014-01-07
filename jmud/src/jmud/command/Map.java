package jmud.command;

import jmud.MysqlConnector;
import jmud.Room;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/*
 * Created on Jun 24, 2005
 *
 * History
 *
 * Programmer:     Change:                                           Date:
 * ----------------------------------------------------------------------------------
 * Chris M         Cleaned up comments                               Feb 13, 2007
 *
 * TODO Determine how many rooms in a straight line a player can see
 *      (i.e. can they see 2 rooms to the south?)
 *      OR, should the landscape determine if they can see?
 *      (i.e. is there a door in the way? or are they in a field and can
 *       see for 3 "rooms" in any direction?)
 * TODO fill in the rooms[] array from the database better (i.e. don't grab ALL rooms)
 *      We could do this by tracking each players last n visited rooms
 * TODO Handle situations where a southeast crosses a northeast
 * TODO Make ProcessRoom recursive
 * TODO Handle rooms that are seen as well as rooms that are visited
 * TODO Fix bottom row, it's getting cut off
 *
 * example:
 *
 *      0-0   0
 *        |  /|
 *        0-0 0-
 *       /   X  |   // imagine a bridge going southeast and an underpass going southwest, the paths don't *really* cross
 *          0-0-0
 *              |
 *  Notice:
 *   1) Only one connector per two rooms (wouldn't handle 1 way doors well, unless we
 *      had colour)
 *   2) Not great UP and DOWN handling (unless we had characters or colours)
 */

/**
 * Represents a player's map of the world
 *
 * @author chrism
 */
public class Map {
    final int X = 0; // The X coord is always the first item in the coords array
    final int Y = 1; // and Y is the 2nd

    private Room[] rooms; // filled in from the database
    int iMinX = 0; // these will tells us how big the 2D array needs to be
    int iMinY = 0; // We'll take Max - Min to get the real width
    int iMaxX = 0; // and then translate each room into the array coords
    int iMaxY = 0;
    // rooms to draw
    LinkedList listRooms = new LinkedList();
    // rooms connected to already-being-drawn rooms to check for further connected rooms
    LinkedList listRoomsToProcess = new LinkedList();
    // room IDs of processed rooms
    LinkedList listAddedRoomIDs = new LinkedList();
    char cMap[][]; // actual 2D map array
    int iMapWidth = 0;  // map size
    int iMapHeight = 0; // map size

    public Map() {
    }

    /**
     * Load all of the player's rooms from the database
     */
    public void loadRooms(int iPlayerID) {
        Hashtable hashRooms;

        // open a connection to the database
        MysqlConnector mSqlConn = new MysqlConnector();
        try {
            mSqlConn.setup();
        } catch(Exception e) {
            System.out.println("loadRooms() --> No rooms loaded, could not connect to database.");
            System.out.println(e);
            return;
        }

        // get all the rooms from the database for this player
        try {
            // create an array big enough to hold all the rooms
            rooms = new Room[mSqlConn.getPlayerRoomCount(iPlayerID)];

            // fill the room array and store what rooms are where in the hash table
            hashRooms = mSqlConn.getPlayerRooms(iPlayerID, rooms);

            // get all connections and attach to rooms
            mSqlConn.getPlayerRoomConnections(iPlayerID, rooms, hashRooms);
            mSqlConn.close();
        } catch(SQLException se) {
            System.out.println("loadRooms() --> " + se);
        }

        // close the connection (?)
        mSqlConn = null;

        // DEBUG:
        System.out.print("Setting Exits: ");

        // go through and specifically set the exits string for each room
        for(int i = 0; i < rooms.length; i++) {
            if(rooms[i] != null) {
                rooms[i].setExits();
            }

            // DEBUG:
            System.out.print(".");
        }

        // DEBUG:
        System.out.print("\n");
    }

    /**
     * Fill a 2D char array with characters representing rooms
     * and connections between rooms
     */
    public void fillMap() {
        int iRoomX = 0;
        int iRoomY = 0;
        int iRoomCoords[];
        int iRooms;  // How many rooms should we draw?

        /* for the number of rooms, we may just use this number (iRooms)
        * to determine how many rooms from memory, not from line of sight
        */
        Room currRoom;
        Room tempRoom;
        ListIterator iter;
        int iRoomID = 0; // used for checking if we've added a room already

        // get our first room to draw
        // (why did I want the last room added?!)
        //listRooms.add(rooms[rooms.length - 1]);
        // until I figure out why I used the last room I'll just go with the first room
        listRooms.add(rooms[0]);

        //specify that we've added the first rooom
        //(so that we don't add it again)
        //This gets done later, no sense douplicating the effort
        //(we have to do it in the loop down below anyways, so we'll not do it here)
        //listAddedRoomIDs.add(new Integer(((Room)listRooms.getFirst()).getID()));

        // add all the rooms that we grabbed from the database to the
        // "rooms to be drawn" list and the
        // "rooms to be processed" list
        // except the first one which is already the room currently being processed
        for(int i = 1; i < rooms.length; i++) {
            // DEBUG:
            System.out.println("adding room [" + rooms[i].getID() + "] to the to-be-processed list");

            listRoomsToProcess.add(rooms[i]);
            listRooms.add(rooms[i]);
        }

        // put the first room in the "currently being processed variable"
        currRoom = (Room) listRooms.getFirst();

        // DEBUG:
        System.out.println("room [" + currRoom.getID() + "] is first to be processed");

        // use the first room to set the max and min X and Y coords
        iRoomCoords = currRoom.getCoords();
        iMinX = iMaxX = iRoomCoords[X];
        iMinY = iMaxY = iRoomCoords[Y];

        // we're picking an arbitrary number of rooms here for now
        // and not really handling them properly, this is just to
        // test going through and adding rooms and then drawing them
        //iRooms = 15;

        int i = 0;

        // process all the rooms we have available because the query
        // should have returned as many rooms as the player can see
        while(currRoom != null) {
            if(processRoom(currRoom.northwest)) {
                i++;
            }
            if(processRoom(currRoom.north)) {
                i++;
            }
            if(processRoom(currRoom.northeast)) {
                i++;
            }
            if(processRoom(currRoom.west)) {
                i++;
            }
            if(processRoom(currRoom.east)) {
                i++;
            }
            if(processRoom(currRoom.southwest)) {
                i++;
            }
            if(processRoom(currRoom.south)) {
                i++;
            }
            if(processRoom(currRoom.southeast)) {
                i++;
            }

            // mark that we've processed this room (we already did this above)
            listAddedRoomIDs.add(new Integer(currRoom.getID()));

            // null the current room so we can test to see if we get the next one
            currRoom = null;

            // get an iterator to look at each ID in the list of added room IDs
            iter = listAddedRoomIDs.listIterator();

            // keep getting the next room until we find one that hasn't been processed
            while(!listRoomsToProcess.isEmpty() && currRoom == null) {
                // remove a room to process it
                currRoom = (Room) listRoomsToProcess.removeFirst();

                // DEBUG:
                System.out.print("fillMap() -> Looking for room " + currRoom.getID() + " ...");

                // get the room's ID to make sure we haven't ALREADY processed it (not that it would hurt)
                while(iter.hasNext()) {
                    // check the next processed ID in the list against the current room ID
                    if(((Integer) iter.next()).intValue() == currRoom.getID()) {

                        // we've found the ID has already been processed
                        // so set the current room to null so the outer loop will grab the next room
                        currRoom = null;

                        // we found the room had been processed so we don't need to keep looping
                        // through the processed rooms
                        break;
                    }
                }

                if(currRoom != null) {
                    // DEBUG: add to the previous print
                    System.out.println(" not found: processing.");
                } else {
                    //DEBUG: Add to the previous print
                    System.out.println(" already processed.");
                }
            }
        }

        //clear the rooms to add list
        listRoomsToProcess = null;

        //now we need to find what our map width and height are
        //so we can create a 2D array big enough to hold all the
        //rooms and connections
        //                           + 1 so that we don't translate TOO far left or down
        iMapHeight = ((iMaxY - iMinY + 1) * 2) + 1;
        iMapWidth = ((iMaxX - iMinX + 1) * 2) + 1; // see notes below

        // make sure there is a minimum of 3 columns and three rows
        //iMapHeight = iMapHeight < 3 ? 3 : iMapHeight;
        //iMapWidth = iMapWidth < 3 ? 3 : iMapWidth;

        // in order to hold 1 room and 1 possible connection in any direction we'll
        // need a 3 by 3 array for each room HOWEVER two neighbouring rooms will
        // share one connection so we only need a 2x2 array for each room plus
        // one full row and one full column to account for the connections that aren't shared
        /* Example (Zeros are rooms, dashes/pipes are connections between them)

         |         2 rooms high, but 2 * 2 + 1 rows high
        -0-0-
           |
           0-0-
           |

        ++           Columns for first column of rooms
          ++         Columns for second column of rooms
            ++       Columns for third column of rooms
              +      Column for left over, "unshared" connection column

        3 rooms wide, but 2 * 3 + 1 columns wide
        */

        // create 2D array big enough to hold one character per room and
        // one character per room connection
        cMap = new char[iMapWidth][iMapHeight];

        // initialize all cells to ' '
        for(int iCols = 0; iCols < iMapWidth; iCols++) {
            for(int iRows = 0; iRows < iMapHeight; iRows++) {
                cMap[iCols][iRows] = ' '; // remember: x,y coords
            }
        }

        // get an iterator on the room list
        // (I think when we did it before we got an error when we change the list afterwards)
        // (Yup, works now)
        iter = listRooms.listIterator();

        // add the rooms individually to the array
        while(iter.hasNext()) {
            // re-use currRoom
            currRoom = (Room) iter.next();
            // get room coords
            iRoomCoords = currRoom.getCoords();

            // find room center coords
            //                                 |<-- add space for conns  -->|
            //                                                                - 1 accounts for 0-based arrays
            iRoomX = ((iRoomCoords[X] - iMinX) * 2) + 1;
            iRoomY = ((iRoomCoords[Y] - iMinY) * 2) + 1;

            // correct edge rooms that get messed up by the above formula (i.e. where actual room X = min X, work it out to see what I mean)
            iRoomX = iRoomX < 1 ? 1 : iRoomX;
            iRoomY = iRoomY < 1 ? 1 : iRoomY;

            // add room to map
            cMap[iRoomX][iRoomY] = '0';

            // add connections to map or space if no connection
            // NOTE: This will overwrite previous connections if they exist in the
            //       resulting array cells
            /* We'll add the connections in this order (if they exist):
            *
            *   NW 1    N 2    NE 3
            *
            *    W 4           E 5    // Note: no connection for room itself
            *
            *   SW 6    S 7    SE 8
            *
            */
            // NW
            cMap[iRoomX - 1][iRoomY + 1] = currRoom.northwest != null ? '\\' : ' ';
            // N
            cMap[iRoomX][iRoomY + 1] = currRoom.north != null ? '|' : ' ';
            // NE
            cMap[iRoomX + 1][iRoomY + 1] = currRoom.northeast != null ? '/' : ' ';

            // W
            cMap[iRoomX - 1][iRoomY] = currRoom.west != null ? '-' : ' ';

            // NO CENTER CONNECTION - It's the room itself!

            // E
            cMap[iRoomX + 1][iRoomY] = currRoom.east != null ? '-' : ' ';

            // SW
            cMap[iRoomX - 1][iRoomY - 1] = currRoom.southwest != null ? '/' : ' ';
            // S
            cMap[iRoomX][iRoomY - 1] = currRoom.south != null ? '|' : ' ';
            // SE
            cMap[iRoomX + 1][iRoomY - 1] = currRoom.southeast != null ? '\\' : ' ';
        }
    }

    public String toString() {
        StringBuffer strbMap = new StringBuffer();
        // print the array
        // (this is hard to explain why we draw from the end of the row array
        //  to the beginning of the row array, but it has to do with the difference
        //  between how my map grid looks and how the rooms will be added to the array)
        //  I have to draw the TOP row of the map FIRST (i.e. the GREATEST Y coord first)
        //  in order to have the map printed right-side up.
        for(int iRows = iMapHeight; iRows > 0; iRows--) {
            for(int iCols = 0; iCols < iMapWidth; iCols++) {
                strbMap.append(cMap[iCols][iRows - 1]); // X,Y coords, X is the column, Y is the row
            }
            // after each row add a carriage return
            strbMap.append("\n");
        }
        System.out.println(strbMap.toString());
        return strbMap.toString();
    }

    private boolean processRoom(Room room) {
        int iCoords[];
        int iRoomID = 0;
        Iterator iter;

        // The room could very well be null because we're basically passing
        // the direction pointer from an adjacent room that exists whether or
        // not a room in that direction exists
        // If it is null then we don't need to process it
        if(room == null) {
            return false;
        }

        // speed things up by getting the coords locally
        iCoords = room.getCoords();

        // get the room ID so we can check to see if it's been processed and
        // also store the ID if we do process it
        iRoomID = room.getID();

        // DEBUG:
        System.out.print("ProcessRoom() -> Looking for room " + iRoomID + " ...");

        // run through the list of processed room IDs and check if this one is there
        iter = listAddedRoomIDs.iterator();
        while(iter.hasNext()) {
            // if the room ID matches a processed room ID then null the room
            // so that we pick the next one
            if(((Integer) iter.next()).intValue() == iRoomID) {
                // cancel the room we picked

                //DEBUG: Add to the previous print
                System.out.println(" already processed.");
                return false;
            }
        }

        // DEBUG: add to the previous print
        System.out.println(" not found: processing.");

        // we've not processed this room yet so we'll add it to the following lists
        // - rooms to draw
        // - rooms to check for adjacent rooms
        // - room IDs that we've already processed
        //   (without too much thinking I decided checking the IDs instead of
        //    the objects would be faster, don't know if I'm right)
        listRooms.add(room);
        listRoomsToProcess.add(room);
        listAddedRoomIDs.add(new Integer(iRoomID));

        // DEBUG:
        System.out.println("Added room " + room.getID() +
            " at " +
            room.getCoords()[X] +
            "," +
            room.getCoords()[Y]);

        if(iCoords[X] > iMaxX) {
            iMaxX = iCoords[X];
            // DEBUG:
            System.out.println("Expanding max x to " + iMaxX);
        }
        if(iCoords[X] < iMinX) {
            iMinX = iCoords[X];
            // DEBUG:
            System.out.println("Expanding min x to " + iMinX);
        }
        if(iCoords[Y] > iMaxY) {
            iMaxY = iCoords[Y];
            // DEBUG:
            System.out.println("Expanding max y to " + iMaxY);
        }
        if(iCoords[Y] < iMinY) {
            iMinY = iCoords[Y];
            // DEBUG:
            System.out.println("Expanding min y to " + iMinY);
        }

        return true;
    }
}
