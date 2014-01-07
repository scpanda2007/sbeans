package jmud;

import java.nio.channels.SocketChannel;

/*
 * PlayerChannel.java
 *
 * Created on June 12, 2002, 9:20 PM
 *
 * History
 *
 * Programmer:     Change:                                           Date:
 * ----------------------------------------------------------------------------------
 * Chris M         Cleaned up comments                               Feb 13, 2007

/**
 * A SocketChannel and associated Player
 *
 * @author  Chris Maguire
 * @version 0.01
 */
public class PlayerChannel {

    private Player player;
    private SocketChannel socketchannel;

    // used for writing to a players SocketChannel
    // (which is wierd because why wouldn't the PlayerChannel be used to send a message
    //  to a player)
    private ChannelWriter cw = new ChannelWriter();

    /**
     * Creates new PlayerChannel
     *
     * @param player        The player
     * @param socketchannel The socketchannel
     */
    public PlayerChannel(Player player, SocketChannel socketchannel) {
        this.player = player;
        this.socketchannel = socketchannel;
    }

    /**
     * Compare this PlayerChannel with another PlayerChannel to see if they are equal
     *
     * @param obj Object to compare with this PlayerChannel
     *            (equals must be usable from any object)
     * @return true if the players in both objects are equal, false otherwise
     */
    public boolean equals(Object obj) {
        // if the classes match (i.e. both are player channels)
        // and the players match (cast o to PlayerChannel and get player)
        if(obj.getClass() == this.getClass() &&
            ((PlayerChannel) obj).player.equals(this.player)) {
            return true;
        }
        return false;
    }

    /*
      This seems a little dangerous because we can't really *guarantee* that the
      channel will be unique, but we can guarantee that the player is unique based
      on the player's hashcode.
      The only time we'll hit a problem with this (I think) is when one player
      has multiple channels, ... but then, that should be ok.
    */
    /**
     * Get the hashcode for this PlayerChannel
     *
     * @return Hashcode for this player
     */
    public int hashCode() {
        return player.hashCode();
    }

    /**
     * Get the player object from this PlayerChannel
     *
     * @return The PlayerChannel's Player object
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the SocketChannel from this PlayerChannel
     *
     * @return This PlayerChannel's SocketChannel object
     */
    public SocketChannel getSocketChannel() {
        return socketchannel;
    }

    /**
     * Sends a message to a player via their socket chanenl
     *
     * @param strMessage
     * @throws Exception
     */
    public void sendMessage(String strMessage) throws Exception {
        cw.sendMessage(strMessage, socketchannel);
    }

}
