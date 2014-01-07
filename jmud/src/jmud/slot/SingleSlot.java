/*
 * Hand.java
 *
 * Created on December 9, 2007, 5:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jmud.slot;

import jmud.item.Item;

import java.util.LinkedList;
import java.util.List;

/**
 * @author root
 */
public abstract class SingleSlot extends Slot {
    private static final int MAX_ITEMS = 1;
    private Item item;
    private List<Slot> slots;
    private String name;

    /**
     * Creates a new instance of Foot
     *
     * @param name name of the new slot
     */
    public SingleSlot(String name) {
        super(name);
    }

    public int itemCount() {
        return (item == null ? 0 : 1);
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public boolean hasItem(Item item) {
        return this.item == item;
    }

    public boolean isFull() {
        return itemCount() == MAX_ITEMS;
    }

    public boolean isEmpty() {
        return !isFull();
    }

    public Item removeItem(Item item) {
        return item;
    }

    public final List<Item> getItems() {
        List<Item> items = new LinkedList<Item>();
        if(item != null) {
            items.add(item);
        }
        /*
        else{
            System.out.println("SingleSlot.getItems() - Empty item list being returned");
        }
        */
        return items;
    }

    public int maxItems() {
        return MAX_ITEMS;
    }

    public boolean addItem(Item item) {
        if(this.item == null) {
            this.item = item;
            return true;
        }
        return false;
    }

    public Item removeItem(String name) {
        if(item.getName().equals(name)) {
            return item;
        }
        return null;
    }

    /**
     * Default override
     *
     * @return false
     */
    public boolean isGrabber() {
        return false;
    }

}
