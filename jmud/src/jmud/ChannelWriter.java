package jmud;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/*
 * ChannelWriter.java
 *
 * Created on ?
 *
 * History
 *
 * Programmer:     Change:                                           Date:
 * ----------------------------------------------------------------------------------
 * Chris M         Cleaned up comments                               Feb 14, 2007
 */

/**
 * Writes messages to socket channels
 *
 * @author Chris Maguire
 * @version 0.1
 */
public class ChannelWriter {

    private ByteBuffer buffer;
    private CharBuffer chars;
    private Charset ascii;
    private CharsetDecoder asciiDecoder;
    private String strCommandParseErr = "Command error, could not parse command: ";
    private String strError = "Encountered an error\n\r";

    /**
     * Creates new ChannelWriter
     */
    public ChannelWriter() {
        ascii = Charset.forName("US-ASCII");
    }

    public void sendMessage(String strMessage, SocketChannel socketchannel) throws Exception {
        //System.out.println(strMessage);
        // make a message
        chars = CharBuffer.allocate(strMessage.length());
        chars.put(strMessage);
        chars.flip();
        // Translate the Unicode characters into ASCII bytes.
        buffer = ascii.newEncoder().encode(chars);
        buffer.rewind();
        socketchannel.write(buffer);
    }

    protected void sendError(String strCmd, SocketChannel socketchannel) throws Exception {
        System.out.println("Tick: run(): sending command error");

        // make a message
        CharBuffer chars = CharBuffer.allocate(strCommandParseErr.length() + strCmd.length() + 2);
        chars.put(strCommandParseErr + strCmd + " \n\r");
        chars.flip();

        // Translate the Unicode characters into ASCII bytes.
        buffer = ascii.newEncoder().encode(chars);
        buffer.rewind();
        socketchannel.write(buffer);
    }

    protected void sendError(SocketChannel socketchannel) throws Exception {
        System.out.println("CommandListenerThread: general error");

        // make a message
        chars = CharBuffer.allocate(strError.length());
        chars.put(strError);
        chars.flip();

        // Translate the Unicode characters into ASCII bytes.
        buffer = ascii.newEncoder().encode(chars);
        buffer.rewind();
        socketchannel.write(buffer);
    }

}
