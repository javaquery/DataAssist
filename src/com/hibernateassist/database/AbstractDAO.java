package com.hibernateassist.database;

/**
 * @author vicky.thakor
 */
public abstract class AbstractDAO {
	protected String DatabaseDriver;
	protected String DatabaseURL;
	protected String DatabaseUsername;
	protected String DatabasePassword;
    
    /**
     * Get database driver
     * @return {@link String}
     */
    public String getDatabaseDriver() {
        return DatabaseDriver;
    }

    /**
     * Set database driver
     * @param DatabaseDriver 
     */
    public void setDatabaseDriver(String DatabaseDriver) {
        this.DatabaseDriver = DatabaseDriver;
    }

    /**
     * Get database url
     * @return {@link String}
     */
    public String getDatabaseURL() {
        return DatabaseURL;
    }

    /**
     * Set database url
     * @param DatabaseURL 
     */
    public void setDatabaseURL(String DatabaseURL) {
        this.DatabaseURL = DatabaseURL;
    }

    /**
     * Get database username
     * @return {@link String}
     */
    public String getDatabaseUsername() {
        return DatabaseUsername;
    }

    /**
     * Set database username
     * @param DatabaseUsername 
     */
    public void setDatabaseUsername(String DatabaseUsername) {
        this.DatabaseUsername = DatabaseUsername;
    }

    /**
     * Get database password
     * @return {@link String}
     */
    public String getDatabasePassword() {
        return DatabasePassword;
    }

    /**
     * Set database password
     * @param DatabasePassword 
     */
    public void setDatabasePassword(String DatabasePassword) {
        this.DatabasePassword = DatabasePassword;
    }
}
