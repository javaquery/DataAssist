package com.hibernateassist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Settings;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.impl.SessionImpl;
import org.hibernate.loader.OuterJoinLoader;
import org.hibernate.loader.criteria.CriteriaLoader;
import org.hibernate.persister.entity.OuterJoinLoadable;

import com.hibernateassist.basecode.CriteriaQueryValueTranslator;
import com.hibernateassist.database.MSSQLAnalyser;
import com.hibernateassist.database.MySQLAnalyser;
import com.hibernateassist.database.OracleAnalyser;
import com.hibernateassist.database.PostgreSQLAnalyser;


/**
 * Hibernate Assist
 * 
 * Title: HibernateAssist v1.1
 *
 * Copyright (c) 2010 - 2011 Vicky Thakor (vicky.thakor@javaquery.com)
 * 
 * http://www.javaquery.com
 * http://github.com/javaquery/HibernateAssist
 * 
 * @author vicky.thakor
 */
public class HibernateAssist {
	private Session HibernateLocalSession;
    private Criteria criteria;
    private static final Logger logger = Logger.getLogger(HibernateAssist.class.getName());
    private String HTMLReportFolder;
    private String MSSQLExecutionPlanFile;
    private SessionFactory objSessionFactory;
    private String strFilenamePrefix;

    public HibernateAssist(){
    	
    }
    
    public HibernateAssist(Session HibernateSession) {
        this.HibernateLocalSession = HibernateSession;
        this.objSessionFactory = this.HibernateLocalSession.getSessionFactory();
    }

    /**
     * Retrieves Driver used for database {@link Connection}.
     * @author vicky.thakor
     * @return {@link String} value or null(in case of connection error)
     * @throws SQLException in case of connection error
     */
    public String getDatabaseDriver() throws SQLException {
        if (HibernateLocalSession instanceof Session) {
            Settings settings = ((SessionFactoryImplementor) this.objSessionFactory).getSettings();
            ConnectionProvider connectionProvider = settings.getConnectionProvider();
            Connection connection = connectionProvider.getConnection();
            return connection.getClass().getName();
        }
        return null;
    }

    /**
     * Retrieves database connection URL with parameters<br/>
     * i.e jdbc:jtds:sqlserver://127.0.0.1:1433/javaQuery;sendStringParametersAsUnicode=false
     * @author vicky.thakor
     * @return {@link String} value or null(in case of connection error)
     * @throws SQLException in case of connection error
     */
    public String getDatabaseURL() throws SQLException {
        if (HibernateLocalSession instanceof Session) {
            Settings settings = ((SessionFactoryImplementor) this.objSessionFactory).getSettings();
            ConnectionProvider connectionProvider = settings.getConnectionProvider();
            Connection connection = connectionProvider.getConnection();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            return databaseMetaData.getURL();
        }
        return null;
    }

    /**
     * To get Hibernate dialect from current {@link Session}
     * @author vicky.thakor
     * @return {@link String} value or null(in case of connection error)
     */
    public String getDialect() {
        if (HibernateLocalSession instanceof Session) {
            Dialect dialect = ((SessionFactoryImplementor) this.objSessionFactory).getDialect();
            return dialect.toString();
        }
        return null;
    }

    /**
     * Retrieves currently connected database name from {@link Session}
     * @author vicky.thakor
     * @return {@link String} value or null(in case of connection error)
     * @throws SQLException in case of connection error
     */
    public String getDatabaseName() throws SQLException {
        if (HibernateLocalSession instanceof Session) {
            Settings settings = ((SessionFactoryImplementor) this.objSessionFactory).getSettings();
            ConnectionProvider connectionProvider = settings.getConnectionProvider();
            Connection connection = connectionProvider.getConnection();
            return connection.getCatalog();
        }
        return null;
    }

    /**
     * Retrieves Username used for database {@link Connection}.
     * @author vicky.thakor
     * @return {@link String} value or null(in case of connection error)
     * @throws {@link SQLException}
     */
    public String getDatabaseUsername() throws SQLException {
    	String dialect = getDialect();
    	String strUsername = null;
        if (HibernateLocalSession instanceof Session) {
            Settings settings = ((SessionFactoryImplementor) this.objSessionFactory).getSettings();
            ConnectionProvider connectionProvider = settings.getConnectionProvider();
            Connection connection = connectionProvider.getConnection();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            strUsername = databaseMetaData.getUserName();
            
            if("org.hibernate.dialect.MySQLDialect".equalsIgnoreCase(dialect)
            		|| "org.hibernate.dialect.MySQLInnoDBDialect".equalsIgnoreCase(dialect)
            		|| "org.hibernate.dialect.MySQLMyISAMDialect".equalsIgnoreCase(dialect)){
            	strUsername = strUsername.substring(0, strUsername.indexOf("@"));
            }
        }
        return strUsername;
    }

    /**
     * Retrieves Password from hibernate.cfg.xml only if its configured
     * @author vicky.thakor
     * @return {@link String} value or null(in case of connection error or property not found in hibernate.cfg.xml)
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException 
     */
    public String getDatabasePassword() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        return getHibernateProperty("hibernate.connection.password");
    }

    /**
     * Retrieves Database version.
     * @author vicky.thakor
     * @return {@link String} value or null(in case of connection error)
     * @throws {@link SQLException}
     */
    public String getDatabaseVersion() throws SQLException{
    	if (HibernateLocalSession instanceof Session) {
            Settings settings = ((SessionFactoryImplementor) this.objSessionFactory).getSettings();
            ConnectionProvider connectionProvider = settings.getConnectionProvider();
            Connection connection = connectionProvider.getConnection();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            return databaseMetaData.getDatabaseProductVersion();
        }
        return null;
    }
    
    /**
     * Retrieves Database Product Name.
     * @author vicky.thakor
     * @return {@link String} value or null(in case of connection error)
     * @throws {@link SQLException}
     */
    public String getDatabaseProductName() throws SQLException{
    	if (HibernateLocalSession instanceof Session) {
            Settings settings = ((SessionFactoryImplementor) this.objSessionFactory).getSettings();
            ConnectionProvider connectionProvider = settings.getConnectionProvider();
            Connection connection = connectionProvider.getConnection();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            return databaseMetaData.getDatabaseProductName();
        }
        return null;
    }
    
    /**
     * Retrieve Hibernate Properties from configuration file(i.e hibernate.cfg.xml) by its property name like...
     * <ul>
     *  <li>hibernate.show_sql</li>
     *  <li>hibernate.connection.autocommit</li>
     *  <li>connection.isolation</li>
     *  <li>hibernate.generate_statistics</li>
     *  <li>etc...</li>
     * </ul>
     * <br/>
     * @author vicky.thakor
     * @param {@link String} HibernateProperty
     * @return {@link String} value or null(in case of connection error or property not found in hibernate.cfg.xml)
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException 
     * in case of connection error
     */
    public String getHibernateProperty(String HibernateProperty) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        if (HibernateLocalSession instanceof Session) {
            Field field = SessionFactoryImpl.class.getDeclaredField("properties");
            field.setAccessible(true);
            Properties properties = (Properties) field.get(this.objSessionFactory);
            return properties.getProperty(HibernateProperty);
        }
        return null;
    }

    /**
     * Retrieve SQL Query from {@link Criteria}.
     * @author vicky.thakor
     * @return {@link String} value or null(in case of connection error or Criteria error)
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException 
     * in case of connection error
     */
    public String getCriteriaQuery() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        if (getCriteria() instanceof Criteria) {
            CriteriaImpl criteriaImpl = (CriteriaImpl) getCriteria();
            SessionImpl sessionImpl = (SessionImpl) criteriaImpl.getSession();
            SessionFactoryImplementor sessionFactoryImplementor = (SessionFactoryImplementor) sessionImpl.getSessionFactory();
            String[] implementors = sessionFactoryImplementor.getImplementors(criteriaImpl.getEntityOrClassName());
            
            CriteriaLoader criteriaLoader = new CriteriaLoader(
                    (OuterJoinLoadable) sessionFactoryImplementor.getEntityPersister(implementors[0]),
                    sessionFactoryImplementor,
                    criteriaImpl,
                    implementors[0],
                    sessionImpl.getLoadQueryInfluencers());
            Field field = OuterJoinLoader.class.getDeclaredField("sql");
            field.setAccessible(true);
            String query = (String) field.get(criteriaLoader);
            if("org.hibernate.dialect.OracleDialect".equalsIgnoreCase(getDialect())
            		|| "org.hibernate.dialect.Oracle9Dialect".equalsIgnoreCase(getDialect())){
            	if(criteriaImpl.getMaxResults() != null)
            		query = "select * from (" + query + ") where rownum <= ?";
            }
            return query;
        }
        return null;
    }

    /**
     * This method is cost a lot on database. Use this method to analyse your Criteria at developing environment.
     * Remove this method call at production server.
     * @author vicky.thakor
     */
    public void analyseCriteria() {
        try {
            if (!(HibernateLocalSession instanceof Session)) {
                logger.info("Provide object of session");
            } else if (!(getCriteria() instanceof Criteria)) {
                logger.info("Provide criteria");
            } else if (!(getCriteriaQuery() instanceof String)) {
                logger.info("Provide valid criteria");
            } else {
                String dialect = getDialect();
                if ("org.hibernate.dialect.SQLServerDialect".equalsIgnoreCase(dialect)) {
                    MSSQLAnalyser objMSSQLAnalyser = new MSSQLAnalyser();
                    objMSSQLAnalyser.setDatabaseDriver(getDatabaseDriver());
                    objMSSQLAnalyser.setDatabaseURL(getDatabaseURL());
                    objMSSQLAnalyser.setDatabaseUsername(getDatabaseUsername());
                    objMSSQLAnalyser.setDatabasePassword(getDatabasePassword());
                    objMSSQLAnalyser.setHibernateSession(HibernateLocalSession);
                    /* strFilenamePrefix allows users specify report name for identification. (i.e: UserLogin, Password change report, etc...) */
                    strFilenamePrefix = strFilenamePrefix == null || strFilenamePrefix.isEmpty() ? "HibernateAssist_MSSQL" : strFilenamePrefix + "_HibernateAssist";
                    objMSSQLAnalyser.generateQueryReport(getCriteriaQuery(), "", getHTMLReportFolder(), strFilenamePrefix);
                }else if("org.hibernate.dialect.MySQLDialect".equalsIgnoreCase(dialect)
                		|| "org.hibernate.dialect.MySQLInnoDBDialect".equalsIgnoreCase(dialect)
                		|| "org.hibernate.dialect.MySQLMyISAMDialect".equalsIgnoreCase(dialect)){
                	MySQLAnalyser objMySQLAnalyser = new MySQLAnalyser();
                	objMySQLAnalyser.setDatabaseDriver(getDatabaseDriver());
                	objMySQLAnalyser.setDatabaseURL(getDatabaseURL());
                	objMySQLAnalyser.setDatabaseUsername(getDatabaseUsername());
                	objMySQLAnalyser.setDatabasePassword(getDatabasePassword());
                	objMySQLAnalyser.setDatabaseVersion(getDatabaseVersion());
                	objMySQLAnalyser.setHibernateSession(HibernateLocalSession);
                	
                	String valuedQuery = getValuedCriteriaQuery();
        			if(valuedQuery != null && !valuedQuery.isEmpty()){
        				/* strFilenamePrefix allows users specify report name for identification. (i.e: UserLogin, Password change report, etc...) */
        				strFilenamePrefix = strFilenamePrefix == null || strFilenamePrefix.isEmpty() ? "HibernateAssist_MySQL" : strFilenamePrefix + "_HibernateAssist";
        				objMySQLAnalyser.generateQueryReport(getCriteriaQuery(), valuedQuery, getHTMLReportFolder(), strFilenamePrefix);
        			}
                }else if("org.hibernate.dialect.PostgreSQLDialect".equalsIgnoreCase(dialect)){
                	PostgreSQLAnalyser objPostgreSQLAnalyser = new PostgreSQLAnalyser();
                	objPostgreSQLAnalyser.setHibernateSession(HibernateLocalSession);
                	
                	String valuedQuery = getValuedCriteriaQuery();
        			if(valuedQuery != null && !valuedQuery.isEmpty()){
        				/* strFilenamePrefix allows users specify report name for identification. (i.e: UserLogin, Password change report, etc...) */
        				strFilenamePrefix = strFilenamePrefix == null || strFilenamePrefix.isEmpty() ? "HibernateAssist_PostgreSQL" : strFilenamePrefix+ "_HibernateAssist";
        				objPostgreSQLAnalyser.generateQueryReport(getCriteriaQuery(), valuedQuery, getHTMLReportFolder(), strFilenamePrefix);
        			}
                }else if("org.hibernate.dialect.OracleDialect".equalsIgnoreCase(dialect)
            			|| "org.hibernate.dialect.Oracle9Dialect".equalsIgnoreCase(dialect)){
                	OracleAnalyser objOracleAnalyser = new OracleAnalyser();
                	objOracleAnalyser.setHibernateSession(HibernateLocalSession);
                	
                	String valuedQuery = getValuedCriteriaQuery();
        			if(valuedQuery != null && !valuedQuery.isEmpty()){
        				/* strFilenamePrefix allows users specify report name for identification. (i.e: UserLogin, Password change report, etc...) */
        				strFilenamePrefix = strFilenamePrefix == null || strFilenamePrefix.isEmpty() ? "HibernateAssist_Oracle" : strFilenamePrefix + "_HibernateAssist";
        				objOracleAnalyser.generateQueryReport(getCriteriaQuery(), valuedQuery, getHTMLReportFolder(), strFilenamePrefix);
        			}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create HTML Report from Microsoft SQLPlan.
     * @author vicky.thakor
     * @throws IOException 
     */
    public void analyseMSSQLPlan() throws IOException{
    	if(getMSSQLExecutionPlanFile() == null || getMSSQLExecutionPlanFile().isEmpty()){
    		logger.info("Provide Microsoft SQLPlan file path.");
    	}else if(!getMSSQLExecutionPlanFile().endsWith(".sqlplan")){
    		logger.info("Provide valid Microsoft SQLPlan file (extension: .sqlplan).");
    	}else{
    		/* Create temporary XML file. */
	        File temporaryXMLFile = File.createTempFile("HibernateAssistTemporaryFile", ".xml");
    		BufferedReader objBufferedReader = new BufferedReader(new FileReader(getMSSQLExecutionPlanFile()));
    	    try {
    	        StringBuilder objStringBuilder = new StringBuilder();
    	        String readLine = objBufferedReader.readLine();
    	        while (readLine != null) {
    	        	if(readLine.contains("utf-16")){
    	        		readLine = readLine.replace("utf-16", "utf-8");
    	        	}
    	            objStringBuilder.append(readLine);
    	            readLine = objBufferedReader.readLine();
    	        }
    	        String fileContent = objStringBuilder.toString();
    	        
	        	/* Write .sqlplan content to temporary .xml file. */
    	        BufferedWriter objBufferedWriter = new BufferedWriter(new FileWriter(temporaryXMLFile));
    	        objBufferedWriter.write(fileContent);
    	        objBufferedWriter.close();
    	    } finally {
    	        objBufferedReader.close();
    	    }
    		
    		MSSQLAnalyser objMSSQLAnalyser = new MSSQLAnalyser();
    		/* strFilenamePrefix allows users specify report name for identification. (i.e: UserLogin, Password change report, etc...) */
            strFilenamePrefix = strFilenamePrefix == null || strFilenamePrefix.isEmpty() ? "HibernateAssist_MSSQL" : strFilenamePrefix + "_HibernateAssist";
    		objMSSQLAnalyser.generateQueryReportFromFile(temporaryXMLFile.getAbsolutePath(), getHTMLReportFolder(), strFilenamePrefix);
    	}
    }
    
    /**
     * Get {@link Criteria} query with values.
     * @author vicky.thakor
     * @return {@link String}
     */
    public String getValuedCriteriaQuery(){
    	if (getCriteria() instanceof Criteria) {
    		CriteriaQueryValueTranslator objCriteriaQueryValueTranslator = new CriteriaQueryValueTranslator(objSessionFactory);
    		return objCriteriaQueryValueTranslator.getValuedQuery(getCriteria());
    	}
    	return null;
    }
    
    /**
     * Get criteria
     * @return {@link Criteria}
     */
    private Criteria getCriteria() {
        return criteria;
    }

    /**
     * Set criteria to profile.
     * @author vicky.thakor
     * @since 1.0
     * @param criteria 
     */
    public void setCriteria(Criteria criteria) {
        this.criteria = criteria;
    }

    /**
     * Get HTML Report Folder
     * @return
     */
    public String getHTMLReportFolder() {
        return HTMLReportFolder;
    }

    /**
     * Set report folder and filename will be auto generated.
     * @author vicky.thakor
     * @since 1.0
     * @see HibernateAssist#setHTMLReportFolder(String HTMLReportFolder, String strFilenamePrefix)
     * @param HTMLReportFolder
     */
    public void setHTMLReportFolder(String HTMLReportFolder) {
        this.HTMLReportFolder = HTMLReportFolder;
    }

    /**
     * Set report folder path and filename prefix to identify the report.
     * @author vicky.thakor
     * @date 23rd June, 2015
     * @since 1.3
     * @param HTMLReportFolder
     * @param strFilenamePrefix
     */
    public void setHTMLReportFolder(String HTMLReportFolder, String strFilenamePrefix) {
        this.HTMLReportFolder = HTMLReportFolder;
        this.strFilenamePrefix = strFilenamePrefix;
    }
    
    /**
     * Get .sqlplan file path
     * @return
     */
	private String getMSSQLExecutionPlanFile() {
		return MSSQLExecutionPlanFile;
	}

	/**
	 * Set .sqlplan file path to analyse.
	 * @author vicky.thakor
	 * @since 1.0
	 * @param mSSQLExecutionPlanFile
	 */
	public void setMSSQLExecutionPlanFile(String mSSQLExecutionPlanFile) {
		MSSQLExecutionPlanFile = mSSQLExecutionPlanFile;
	}

	public void setObjSessionFactory(SessionFactory objSessionFactory) {
		this.objSessionFactory = objSessionFactory;
	}
}
