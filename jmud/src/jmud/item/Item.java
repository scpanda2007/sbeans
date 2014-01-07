package jmud.item;

import jmud.slot.Slot;

import java.util.LinkedList;
import java.util.List;

/*
 * Item.java
 *
 * Created on April 28, 2002, 8:53 AM
 *
 * History
 *
 * Programmer:     Change:                                           Date:
 * ----------------------------------------------------------------------------------
 * Chris M         Cleaned up comments                               Feb 13, 2007
 */

/**
 * Represents a virtual item in the game, e.g. a weapon, a piece of money, food, etc.
 *
 * @author Chris Maguire
 * @version 0.1
 */
public class Item {

    private static final int DEFAULT_BULK = 1;
    private static final int DEFAULT_WEIGHT = 1;
    private static final int DEFAULT_USES = 1000;
    private static final int DEFAULT_CONDITION = 100;

    private int id;
    private String name;
    private int bulk;
    private int weight;
    private int uses;
    private int condition; // percentage of "mint" condition
    private List<Slot> slots;

    /**
     * Creates new Item
     *
     * @param id   ID of the item
     * @param name name of the item
     */
    public Item(int id, String name) {
        this(id, name, DEFAULT_BULK, DEFAULT_WEIGHT, DEFAULT_USES, DEFAULT_CONDITION, new LinkedList<Slot>());
    }

    public Item(int id, String name, int bulk, int weight, int uses, int condition, List<Slot> slots) {
        this.id = id;
        this.name = name;
        this.slots = slots;
        this.bulk = bulk;
        this.weight = weight;
        this.uses = uses;
        this.condition = condition;

        // DEBUG:
        //System.out.println("Item - name is " + name);
    }

    /**
     * Check if another item equals this item
     *
     * @param o The other item to check this item against
     */
    public boolean equals(Object o) {
        // March 20, 2003
        /** I left this open and used hashCode() to compare the two
         *  objects because I didn't want to have to change BOTH methods
         *  (equals and hashCode) if I ever wanted to change how hashCode()
         *  (and thus uniqueness) is implemented.
         */
        return o.getClass() == this.getClass() && (o).hashCode() == this.hashCode();
    }

    /**
     * returns the unique hashcode for this object
     */
    public int hashCode() {
        return id;
    }

    /**
     * Get the name of this item
     *
     * @return the name of the item
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of this item
     *
     * @param name new name for the item
     */
    public void setName(String name) {
        this.name = name;
        // DEBUG: 
        //System.out.println("Name is " + name);
    }

    public int getBulk() {
        return bulk;
    }

    public void setBulk(int bulk) {
        this.bulk = bulk;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getUses() {
        return uses;
    }

    public void setUses(int uses) {
        this.uses = uses;
    }

    public int use() {
        return use(1);
    }

    /**
     * in case there are uses that use more than one "use"
     *
     * @param uses how many usages to apply to the item
     * @return how many uses left after we use the item
     */
    public int use(int uses) {
        return this.uses -= uses;
    }

    public void addSlot(Slot slot) {
        slots.add(slot);
    }

    public boolean removeSlot(Slot slot) {
        return slots.remove(slot);
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public int getCondition() {
        return condition;
    }

    public void setCondition(int condition) {
        this.condition = condition;
    }

    public int getId() {
        return id;
    }
}
