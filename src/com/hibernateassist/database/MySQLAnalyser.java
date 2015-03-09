package com.hibernateassist.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.hibernateassist.bean.MySQLQueryDetails;


/**
 * @author vicky.thakor
 */
public class MySQLAnalyser extends AbstractDAO{
	private static final Logger logger = Logger.getLogger(MySQLAnalyser.class.getName());
	
	public void generateQueryReport(String query, String reportFolderPath) throws ClassNotFoundException, SQLException {
		List<MySQLQueryDetails> listMySQLQueryDetails = getExecutionPlan(query);
		if(listMySQLQueryDetails != null && !listMySQLQueryDetails.isEmpty()){
			for (MySQLQueryDetails mySQLQueryDetails : listMySQLQueryDetails) {
				// TO-DO Impossible WHERE noticed after reading const tables
				System.out.println(mySQLQueryDetails.getTable());
			}
		}else{
			logger.info("Execution plan not found on database.");
		}
	}
	
	private List<MySQLQueryDetails> getExecutionPlan(String query) throws ClassNotFoundException, SQLException{
		List<MySQLQueryDetails> listMySQLQueryDetails = new ArrayList<MySQLQueryDetails>();
		if(query == null || query.isEmpty()){
			logger.info("Please provide valid query");
		}else if (getDatabaseDriver() == null || getDatabaseDriver().isEmpty()) {
            logger.info("Provide database driver string");
        } else if (getDatabaseURL() == null || getDatabaseURL().isEmpty()) {
            logger.info("Provide database url");
        } else if (getDatabaseUsername() == null || getDatabaseUsername().isEmpty()) {
            logger.info("Provide database username");
        } else if (getDatabasePassword() == null || getDatabasePassword().isEmpty()) {
            logger.info("Provide database password");
        } else {
        	Class.forName(getDatabaseDriver());
            Connection connection = DriverManager.getConnection(getDatabaseURL(), getDatabaseUsername(), getDatabasePassword());
            if(getDatabaseVersion() != null && !getDatabaseVersion().isEmpty() && getDatabaseVersion().indexOf(".") > 0){
            	String strDatabaseVersion = getDatabaseVersion();
            	strDatabaseVersion = strDatabaseVersion.substring(0, strDatabaseVersion.indexOf(".") + 2);
            	double doubleVersion = Double.valueOf(strDatabaseVersion);
            	/**
            	 * MySQL 5.6 above supports EXPLAIN with JSON format.
            	 */
            	if(doubleVersion >= 5.6){
            		//EXPLAIN FORMAT = JSON
            	}else{
            		PreparedStatement preparedStatement = connection.prepareStatement("EXPLAIN EXTENDED "+query);
            		ResultSet resultSet = preparedStatement.executeQuery();
            		if(resultSet.next()){
            			MySQLQueryDetails objMySQLQueryDetails = new MySQLQueryDetails();
            			objMySQLQueryDetails.setSelectType(resultSet.getString("select_type"));
            			objMySQLQueryDetails.setTable(resultSet.getString("table"));
            			objMySQLQueryDetails.setType(resultSet.getString("type"));
            			objMySQLQueryDetails.setPossibleKeys(resultSet.getString("possible_keys"));
            			objMySQLQueryDetails.setKey(resultSet.getString("key"));
            			objMySQLQueryDetails.setKeyLen(resultSet.getLong("key_len"));
            			objMySQLQueryDetails.setRef(resultSet.getString("ref"));
            			objMySQLQueryDetails.setRows(resultSet.getLong("rows"));
            			objMySQLQueryDetails.setFiltered(resultSet.getDouble("filtered"));
            			objMySQLQueryDetails.setExtra(resultSet.getString("Extra"));
            			objMySQLQueryDetails.setQueryPlan("");
            			listMySQLQueryDetails.add(objMySQLQueryDetails);
            		}
            		resultSet.close();
            	}
            }
            connection.close();
        }
		return listMySQLQueryDetails;
	}
}
