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
import java.util.Date;
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

import com.hibernateassist.database.MSSQLAnalyser;


/**
 * @author vicky.thakor
 */
public class HibernateAssist {
	private Session HibernateLocalSession;
    private Criteria criteria;
    private static final Logger logger = Logger.getLogger(HibernateAssist.class.getName());
    private String HTMLReportFolder;
    private String MSSQLExecutionPlanFile;

    public HibernateAssist(){
    	
    }
    
    public HibernateAssist(Session HibernateSession) {
        this.HibernateLocalSession = HibernateSession;
    }

    /**
     * @author vicky.thakor
     * @return {@link String} value or null(in case of connection error)
     * @throws SQLException in case of connection error
     * Retrieves Driver used for database {@link Connection}.
     */
    public String getDatabaseDriver() throws SQLException {
        if (HibernateLocalSession instanceof Session) {
            SessionFactory sessionFactory = HibernateLocalSession.getSessionFactory();
            Settings settings = ((SessionFactoryImplementor) sessionFactory).getSettings();
            ConnectionProvider connectionProvider = settings.getConnectionProvider();
            Connection connection = connectionProvider.getConnection();
            return connection.getClass().getName();
        }
        return null;
    }

    /**
     * @author vicky.thakor
     * @return {@link String} value or null(in case of connection error)
     * @throws SQLException in case of connection error
     * 
     * Retrieves database connection URL with parameters
     * i.e jdbc:jtds:sqlserver://127.0.0.1:1433/javaQuery;sendStringParametersAsUnicode=false
     */
    public String getDatabaseURL() throws SQLException {
        if (HibernateLocalSession instanceof Session) {
            SessionFactory sessionFactory = HibernateLocalSession.getSessionFactory();
            Settings settings = ((SessionFactoryImplementor) sessionFactory).getSettings();
            ConnectionProvider connectionProvider = settings.getConnectionProvider();
            Connection connection = connectionProvider.getConnection();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            return databaseMetaData.getURL();
        }
        return null;
    }

    /**
     * @author vicky.thakor
     * @return {@link String} value or null(in case of connection error)
     * To get Hibernate dialect from current {@link Session}
     */
    public String getDialect() {
        if (HibernateLocalSession instanceof Session) {
            SessionFactory sessionFactory = HibernateLocalSession.getSessionFactory();
            Dialect dialect = ((SessionFactoryImplementor) sessionFactory).getDialect();
            return dialect.toString();
        }
        return null;
    }

    /**
     * @author vicky.thakor
     * @return {@link String} value or null(in case of connection error)
     * @throws SQLException in case of connection error
     * Retrieves currently connected database name from {@link Session}
     */
    public String getDatabaseName() throws SQLException {
        if (HibernateLocalSession instanceof Session) {
            SessionFactory sessionFactory = HibernateLocalSession.getSessionFactory();
            Settings settings = ((SessionFactoryImplementor) sessionFactory).getSettings();
            ConnectionProvider connectionProvider = settings.getConnectionProvider();
            Connection connection = connectionProvider.getConnection();
            return connection.getCatalog();
        }
        return null;
    }

    /**
     * @author vicky.thakor
     * @return {@link String} value or null(in case of connection error)
     * @throws SQLException in case of connection error
     * Retrieves Username used for database {@link Connection}.
     */
    public String getDatabaseUsername() throws SQLException {
        if (HibernateLocalSession instanceof Session) {
            SessionFactory sessionFactory = HibernateLocalSession.getSessionFactory();
            Settings settings = ((SessionFactoryImplementor) sessionFactory).getSettings();
            ConnectionProvider connectionProvider = settings.getConnectionProvider();
            Connection connection = connectionProvider.getConnection();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            return databaseMetaData.getUserName();
        }
        return null;
    }

    /**
     * @author vicky.thakor
     * @return {@link String} value or null(in case of connection error or property not found in hibernate.cfg.xml)
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException 
     * Retrieves Password from hibernate.cfg.xml only if its configured
     */
    public String getDatabasePassword() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        return getHibernateProperty("hibernate.connection.password");
    }

    /**
     * @author vicky.thakor
     * @param {@link String} HibernateProperty
     * @return {@link String} value or null(in case of connection error or property not found in hibernate.cfg.xml)
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException 
     * in case of connection error
     * 
     * Retrieve Hibernate Properties from configuration file(i.e hibernate.cfg.xml) by its property name like...
     * <ul>
     *  <li>hibernate.show_sql</li>
     *  <li>hibernate.connection.autocommit</li>
     *  <li>connection.isolation</li>
     *  <li>hibernate.generate_statistics</li>
     *  <li>etc...</li>
     * </ul>
     * 
     */
    public String getHibernateProperty(String HibernateProperty) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        if (HibernateLocalSession instanceof Session) {
            Field field = SessionFactoryImpl.class.getDeclaredField("properties");
            field.setAccessible(true);
            SessionFactory sessionFactory = HibernateLocalSession.getSessionFactory();
            Properties properties = (Properties) field.get(sessionFactory);
            return properties.getProperty(HibernateProperty);
        }
        return null;
    }

    /**
     * @author vicky.thakor
     * @return {@link String} value or null(in case of connection error or Criteria error)
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException 
     * in case of connection error
     * Retrieve SQL Query from {@link Criteria}.
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
                    objMSSQLAnalyser.generateQueryReport(getCriteriaQuery(), getHTMLReportFolder());
                }else if("org.hibernate.dialect.MySQLDialect".equalsIgnoreCase(dialect)
                		|| "org.hibernate.dialect.MySQLInnoDBDialect".equalsIgnoreCase(dialect)
                		|| "org.hibernate.dialect.MySQLMyISAMDialect".equalsIgnoreCase(dialect)){
                	System.out.println(getCriteriaQuery());
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
    	        StringBuilder stringBuffer = new StringBuilder();
    	        String readLine = objBufferedReader.readLine();
    	        while (readLine != null) {
    	        	if(readLine.contains("utf-16")){
    	        		readLine = readLine.replace("utf-16", "utf-8");
    	        	}
    	            stringBuffer.append(readLine);
    	            readLine = objBufferedReader.readLine();
    	        }
    	        String fileContent = stringBuffer.toString();
    	        
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
}
