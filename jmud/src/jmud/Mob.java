package jmud;

/*
 * Mob.java
 *
 * Created on April 28, 2002, 8:56 AM
 */

/**
 * Represents a mobile entity in the mud
 *
 * @author Chris Maguire
 */
public class Mob implements Target {

    private int iID;
    private MobType mobType;
    private int iHP = 1;
    private int iMaxHP = 1;
    private int iAC;
    private int iStrength = 1;
    private int iDexterity = 1;
    private boolean bAlive = true;

    /**
     * Creates new Mob
     */
    public Mob() {
    }

    /**
     * Creates new Mob with hitpoints, armour class, strength and dexterity
     */
    public Mob(int iID,
               MobType mobType,
               int iHP,
               int iMaxHP,
               int iAC,
               int iStrength,
               int iDexterity) {
        this.iID = iID;
        this.mobType = mobType;
        this.iHP = iHP;
        this.iMaxHP = iMaxHP;
        this.iAC = iAC;
        this.iStrength = iStrength;
        this.iDexterity = iDexterity;
    }

    /**
     * Compares two Mobs for equality
     */
    public boolean equals(Object o) {
        return o.getClass() == this.getClass() && ((Mob) o).iID == this.iID;
    }

    /**
     * returns a unique number for every mob
     */
    public int hashCode() {
        return iID;
    }

    /**
     * Get the <code>MobType</code> for this Mob
     *
     * @return This <code>Mob</code>'s <code>MobType</code> even if null
     */
    public MobType getMobType() {
        return mobType;
    }

    /**
     * Get the name of this <code>Mob</code>'s <code>MobType</code>
     * <p/>
     * Required by Target interface
     *
     * @return the <code>MobType</code> name for this <code>Mob</code>
     */
    public String getName() {
        return mobType.getType();
    }

    /**
     * Returns true if the Mob is alive
     */
    public boolean isAlive() {
        return bAlive;
    }

    /**
     * Subtracts hitpoints from the mob, returns true if hitpoints <= 0
     */
    public boolean hurt(int iDmg) {
        iHP -= iDmg;

        // if the Mob is dead then set the alive flag
        //bAlive = !(iHP > 0);
        //Scratch that: we'll complicate this and set it in the return statement :) (yeah, yeah, so shoot me)

        //DEBUG:
        System.out.println(mobType.getType() + " has " + iHP + " of " + iMaxHP + " hitpoints.");

        // set and return the alive flag based on whether the Mob was killed
        return bAlive = !(iHP > 0);
    }

    /**
     * returns the Mob's ID
     */
    public int getID() {
        return iID;
    }

    /**
     * returns the Mob's remaing hit points
     */
    public int getHP() {
        return iHP;
    }

    /**
     * Returns the mob's Armour Class
     */
    public int getAC() {
        return iAC;
    }

    /**
     * Returns the mob's Dexterity
     */
    public int getDex() {
        return iDexterity;
    }

    /**
     * Returns the mob's Strength
     */
    public int getStr() {
        return iStrength;
    }

}
