package jmud;

/*
 * Target.java
 *
 * Created on March 14 2003 10:17 PM
 *
 * History
 *
 * Programmer:     Change:                                           Date:
 * ----------------------------------------------------------------------------------
 * Chris M         Cleaned up comments                               Feb 14, 2007
 */

/**
 * Target is the abstract definition of something that can be attacked.
 *
 * @author Chris Maguire
 * @version 0.1
 */
// this has to be an interface so that Mobs can implement other things as well, like Attacker
// (haven't made an Attacker interface yet, but probably will, ... maybe)
// ...
// Thing is, if I have a separate attack command for Mobs then I don't need an attacker interface.
public interface Target {

    // get the armor class of the target
    public abstract int getAC();

    // damage the target and return if it's dead
    public abstract boolean hurt(int dmg);

    // get the name of the target
    public abstract String getName();

}