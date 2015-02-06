package com.hibernateassist.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.hibernateassist.bean.MSSQLQueryDetails;
import com.hibernateassist.common.CommonUtil;

/**
 * @author vicky.thakor
 */
public class MSSQLAnalyser extends AbstractDAO{
	
	private static final Logger logger = Logger.getLogger(MSSQLAnalyser.class.getName());
	
	/**
	 * @author vicky.thakor
	 * @param query
	 * @return {@link MSSQLQueryDetails}
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * 
	 * Get execution plans from database
	 */
	public List<MSSQLQueryDetails> getExecutionPlan(String query) throws ClassNotFoundException, SQLException{
		List<MSSQLQueryDetails> listMSSQLQueryDetails = new ArrayList<MSSQLQueryDetails>();
		
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
        	query = CommonUtil.replaceQuestionMarkWithP(query);
        	
        	/**
        	 * Microsoft SQL Server can process String of 2000 character only in `LIKE` clause.
        	 * We'll trim last 2000 Characters as it contains `WHERE`, `JOIN`, etc... clause and that makes query unique.
        	 */
        	if(query != null && !query.isEmpty() && query.length() > 2000){
            	int position = query.length() - 2000;
            	query = query.substring(position, query.length());
            }
        	
        	Class.forName(getDatabaseDriver());
            Connection connection = DriverManager.getConnection(getDatabaseURL(), getDatabaseUsername(), getDatabasePassword());
            PreparedStatement preparedStatement = connection.prepareStatement("WITH XMLNAMESPACES (default 'http://schemas.microsoft.com/sqlserver/2004/07/showplan') "
                    + "SELECT "
                    + "Cast('<?SQL ' + st.text + ' ?>' as xml) sql_text, "
                    + "pl.query_plan, "
                    + "ps.execution_count, "
                    + "ps.last_execution_time, "
                    + "ps.last_elapsed_time, "
                    + "ps.last_logical_reads, "
                    + "ps.last_logical_writes "
                    + "FROM sys.dm_exec_query_stats ps with (NOLOCK) "
                    + "Cross Apply sys.dm_exec_sql_text(ps.sql_handle) st "
                    + "Cross Apply sys.dm_exec_query_plan(ps.plan_handle) pl "
                    + "WHERE st.text like '%" + query + "' AND  st.text NOT LIKE '%sys.dm_exec_query_stats%'");
            
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
            	MSSQLQueryDetails objMssqlQueryDetails = new MSSQLQueryDetails();
            	objMssqlQueryDetails.setSqlText(resultSet.getString("sql_text"));
            	objMssqlQueryDetails.setQueryPlan(resultSet.getString("query_plan"));
            	objMssqlQueryDetails.setLastExecutionTime(resultSet.getString("last_execution_time"));
            	objMssqlQueryDetails.setLastElapsedTime(resultSet.getInt("last_elapsed_time"));
            	objMssqlQueryDetails.setLastLogicalReads(resultSet.getInt("last_logical_reads"));
            	objMssqlQueryDetails.setLastLogicalWrites(resultSet.getInt("last_logical_writes"));
            	objMssqlQueryDetails.setExecutionCount(resultSet.getInt("execution_count"));
            	listMSSQLQueryDetails.add(objMssqlQueryDetails);
            }
            connection.close();
        }
		
		return listMSSQLQueryDetails;
	}
	
	public static void main(String[] args) {
		
	}
}
