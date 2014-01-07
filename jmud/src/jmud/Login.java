package jmud;

/*
 * Login.java
 *
 * Created on May 2, 2002, 8:23 PM
 *
 * History
 *
 * Programmer:     Change:                                           Date:
 * ----------------------------------------------------------------------------------
 * Chris M         Cleaned up comments                               Feb 13, 2007
 */

/**
 * Represents the state of a login process
 *
 * @author chrisma
 * @version 0.1
 */
public class Login {

    // how many times the user can screw up their login
    public static int MAX_FAILS = 3;
    public static int LOGIN = 0;
    public static int PASSWORD = 1;

    private boolean bLoggedIn = false;
    private int iState = 0;
    private int iFailedLoginAttempts = 0;
    private StringBuffer strLogin;
    private StringBuffer strPassword;

    /**
     * Creates new Login
     */
    public Login() {
        strLogin = new StringBuffer();
        strPassword = new StringBuffer();
    }

    /**
     * Report that a login attempt has failed
     */
    public void setLoginFailed() {

        // increment the number of failed login attempts
        iFailedLoginAttempts++;

        // clear the login and password they tried
        strLogin.delete(0, strLogin.length());
        strPassword.delete(0, strPassword.length());

        // set the state to "LOGIN"
        iState = LOGIN;
    }

    /**
     * Check if the user has failed their login for the "maximumth" time
     *
     * @return true if the user has failed too many times, false if not
     */
    public boolean checkMaxFailedLogins() {

        // I use >= just in case they've managed to fail more than the max number
        // of times
        return iFailedLoginAttempts >= MAX_FAILS;
    }

    /**
     * Store the current user name that the user is trying
     */
    public void saveLogin(String strLogin) {
        this.strLogin = new StringBuffer(strLogin);
    }

    /**
     * Return the current user name that the user has entered
     *
     * @return StringBuffer containing the user name entered by the user
     */
    public StringBuffer getLogin() {
        return strLogin;
    }

    /**
     * Store the current password that the user has entered
     */
    public void savePassword(String strPassword) {
        this.strPassword = new StringBuffer(strPassword);
    }

    /**
     * Return the current password that the user has entered
     *
     * @return StringBuffer containing the password entered by the user
     */
    public StringBuffer getPassword() {
        return strPassword;
    }

    /**
     * Set the state of the login process (i.e. Login or Password). That is,
     * store what step the user is at: are they entering their username or their
     * password?
     *
     * @param iState Should be Login.LOGIN for "Login" and Login.PASSWORD for "Password"
     */
    public void setState(int iState) {
        this.iState = iState;
    }

    /**
     * Return the login state number
     *
     * @return Number representing the login state
     */
    public int getState() {
        return iState;
    }

    // return the current unfinished part of whatever string we're currently
    // working on:
    // e.g. if we are currently working on login, then pass back whatever we
    //      have stored in login so far
    public StringBuffer getCurrentStateString() {
        if(iState == Login.LOGIN) {
            return strLogin;
        } else {
            return strPassword;
        }
    }

}
