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

    public HibernateAssist(){
    	
    }
    
    public HibernateAssist(Session HibernateSession) {
        this.HibernateLocalSession = HibernateSession;
        this.objSessionFactory = this.HibernateLocalSession.getSessionFactory();
    }

    /**
     * Retrieves Driver used for database {@link Connection}.
     * <br/><br/>
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
     * Retrieves database connection URL with parameters
     * i.e jdbc:jtds:sqlserver://127.0.0.1:1433/javaQuery;sendStringParametersAsUnicode=false
     * <br/><br/>
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
     * <br/><br/>
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
     * <br/><br/>
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
     * <br/><br/>
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
     * <br/><br/>
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
     * <br/><br/>
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
     * <br/><br/>
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
     * <br/><br/>
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
            return (String) field.get(criteriaLoader);
        }
        return null;
    }

    /**
     * This method is cost a lot on database. Use this method to analyse your Criteria at developing environment.
     * Remove this method call at production server.
     * <br/><br/>
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
                    logger.info("Hibernate Assist: If report is not generating please execute same Criteria 2-3 times.");
                    objMSSQLAnalyser.generateQueryReport(getCriteriaQuery(), getHTMLReportFolder());
                }else if("org.hibernate.dialect.MySQLDialect".equalsIgnoreCase(dialect)
                		|| "org.hibernate.dialect.MySQLInnoDBDialect".equalsIgnoreCase(dialect)
                		|| "org.hibernate.dialect.MySQLMyISAMDialect".equalsIgnoreCase(dialect)){
                	MySQLAnalyser objMySQLAnalyser = new MySQLAnalyser();
                	objMySQLAnalyser.setDatabaseDriver(getDatabaseDriver());
                	objMySQLAnalyser.setDatabaseURL(getDatabaseURL());
                	objMySQLAnalyser.setDatabaseUsername(getDatabaseUsername());
                	objMySQLAnalyser.setDatabasePassword(getDatabasePassword());
                	objMySQLAnalyser.setDatabaseVersion(getDatabaseVersion());
                	String valuedQuery = getValuedCriteriaQuery();
        			if(valuedQuery != null && !valuedQuery.isEmpty()){
        				objMySQLAnalyser.generateQueryReport(getCriteriaQuery(), valuedQuery, getHTMLReportFolder());
        			}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create HTML Report from Microsoft SQLPlan.
     * <br/><br/>
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
    		objMSSQLAnalyser.generateQueryReportFromFile(temporaryXMLFile.getAbsolutePath(), getHTMLReportFolder());
    	}
    }
    
    /**
     * Get {@link Criteria} query with values.
     * <br/><br/>
     * @author 0Signals
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
    public Criteria getCriteria() {
        return criteria;
    }

    /**
     * Set criteria to profile.
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
     * Set HTML Report Folder
     * @param HTMLReportFolder
     */
    public void setHTMLReportFolder(String HTMLReportFolder) {
        this.HTMLReportFolder = HTMLReportFolder;
    }

    /**
     * Get .sqlplan file path
     * @return
     */
	public String getMSSQLExecutionPlanFile() {
		return MSSQLExecutionPlanFile;
	}

	/**
	 * Set .sqlplan file path
	 * @param mSSQLExecutionPlanFile
	 */
	public void setMSSQLExecutionPlanFile(String mSSQLExecutionPlanFile) {
		MSSQLExecutionPlanFile = mSSQLExecutionPlanFile;
	}

	public SessionFactory getObjSessionFactory() {
		return objSessionFactory;
	}

	public void setObjSessionFactory(SessionFactory objSessionFactory) {
		this.objSessionFactory = objSessionFactory;
	}
}
