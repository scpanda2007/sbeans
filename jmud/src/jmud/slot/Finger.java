/*
 * Finger.java
 *
 * Created on December 9, 2007, 5:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jmud.slot;

import jmud.item.Item;

import java.util.List;

/**
 * @author root
 */
public class Finger extends MultiSlot {
    public static final int MAX_BULK = 10;
    public static final int MAX_ITEMS = 2;
    private List<Item> items;
    private String name;

    /**
     * Creates a new instance of Finger
     */
    public Finger(String name) {
        super(name);
    }

    public int maxBulk() {
        return MAX_BULK;
    }

    public int maxItems() {
        return MAX_ITEMS;
    }

}
