/*
 * Hand.java
 *
 * Created on December 9, 2007, 5:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jmud.slot;

/**
 * @author root
 */
public class Foot extends SingleSlot {
    private static final int MAX_BULK = 20;

    /**
     * Creates a new instance of Foot
     */
    public Foot(String name) {
        super(name);
    }

    public int maxBulk() {
        return MAX_BULK;
    }

}
