package jmud;

/*
 * MobType.java
 *
 * Created on April 28, 2002, 8:56 AM
 */

/**
 * A type of Mob (Mobile Object) that specifies the type, acronym and description of the mob.
 *
 * @author chrisma
 * @version 0.0.1
 */
public class MobType {

    //private int iID;
    private String strType;
    private String strAcronym;
    private String strDesc;

    /**
     * Creates new Mob
     */
    public MobType() {
    }

    /**
     * Creates new Mob with type, acronym and description
     *
     * @param type    The name of this Mob type
     * @param acronym The "nickname" of this Mob type
     * @param desc    The description of this Mob type
     */
    public MobType(String type,
                   String acronym,
                   String desc) {
        this.strType = type;
        this.strAcronym = acronym;
        this.strDesc = desc;
    }

    /**
     * Gets the type name of this Mob type
     *
     * @return The actual name of this Mob Type (or group, class, species, etc)
     */
    public String getType() {
        return strType;
    }

    /**
     * Sets the type name of this Mob type
     *
     * @param strType The new type name for this type
     */
    public void setType(String strType) {
        this.strType = strType;
    }

    /**
     * Gets the acronym for this <code>MobType</code>
     *
     * @return The acronym for this Mob Type (or group, class, species, etc)
     */
    public String getAcronym() {
        return strAcronym;
    }

    /**
     * Sets the type name of this Mob type
     *
     * @param strAcronym The new acronym for this <code>MobType</code>
     */
    public void setAcronym(String strAcronym) {
        this.strAcronym = strAcronym;
    }

    /**
     * Gets the description for this type of Mob
     *
     * @return The Mob type description
     */
    public String getDesc() {
        return strDesc;
    }

    /**
     * Sets the description for this type of Mob
     *
     * @param strDesc The new description (sensory perception) of this Mob type
     */
    public void setDesc(String strDesc) {
        this.strDesc = strDesc;
    }
}
