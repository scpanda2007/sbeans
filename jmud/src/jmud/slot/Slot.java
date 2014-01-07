/*
 * Slot.java
 *
 * Created on December 9, 2007, 4:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jmud.slot;

import jmud.item.Item;

import java.util.LinkedList;
import java.util.List;

/**
 * A slot is a spot on a player (or mob?) that can hold something
 *
 * @author Chris Maguire
 */
public abstract class Slot {
    public static final int MAX_BULK = 100;
    public static final int MIN_BULK = 0;
    private String name;
    private List<String> aliases;

    /**
     * Creates a new instance of Slot
     *
     * @param name the name for the new slot
     */
    public Slot(String name) {
        this(name, new LinkedList<String>());
    }

    public Slot(String name, List<String> aliases) {
        this.name = name;
        this.aliases = aliases;
    }

    public String getName() {
        return name;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public abstract int itemCount();

    public abstract boolean isFull();

    public abstract boolean isEmpty();

    public abstract int maxBulk();

    public abstract int maxItems();

    public abstract boolean hasItem(Item item);

    //public abstract List<Slot> getSlots();
    // why would slots need to "house" other slots? 
    // For instance, a hand will semantically "house" fingers, but does it need to logically? 
    // Left hand doesn't need to have 'left index finger', the user will just assume it.

    public abstract boolean addItem(Item item);

    public abstract Item removeItem(String name);

    public abstract List<Item> getItems();

    public abstract boolean isGrabber();
}
