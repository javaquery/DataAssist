package com.hibernateassist.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import com.hibernateassist.bean.MySQLQueryDetails;
import com.hibernateassist.common.CommonUtil;
import com.hibernateassist.common.CommonUtil.jsPlumbArrowPosition;


/**
 * @author vicky.thakor
 */
public class MySQLAnalyser extends AbstractDAO{
	
	enum MySQLOperation{
		ordering_operation, grouping_operation, nested_loop, table;
	}
	
	enum MySQLTableProperties{
		table_name, access_type, possible_keys, key, used_key_parts, 
		key_length, ref, rows, filtered, index_condition, 
		attached_condition, using_join_buffer, message; 
	}
	
	private static final Logger logger = Logger.getLogger(MySQLAnalyser.class.getName());
	
	public void generateQueryReport(String query, String reportFolderPath) throws ClassNotFoundException, SQLException {
		List<MySQLQueryDetails> listMySQLQueryDetails = getExecutionPlan(query);
		if(listMySQLQueryDetails != null && !listMySQLQueryDetails.isEmpty()){
			for (MySQLQueryDetails mySQLQueryDetails : listMySQLQueryDetails) {
				// TO-DO Impossible WHERE noticed after reading const tables
				/*System.out.println(mySQLQueryDetails.getTable());*/
				try {
					System.out.println(mySQLQueryDetails.getQueryPlan());
					JSONObject QueryPlan = new JSONObject(mySQLQueryDetails.getQueryPlan());
					
					StringBuilder HTMLReport = new StringBuilder("");
					
					HTMLReport.append(CommonUtil.getHTMLReportHeader());
					parseJSON(QueryPlan.getJSONObject("query_block"), 0,HTMLReport);
					HTMLReport.append(CommonUtil.getHTMLReportFooter());
					
					try {
		                PrintWriter writer = new PrintWriter(reportFolderPath + File.separatorChar + "HibernateAssist_MySQL.html", "UTF-8");
		                writer.write(HTMLReport.toString());
		                writer.close();
		                String informationMessage = "Hibernate Assist Report Generated";
		                logger.info(informationMessage);
		            } catch (FileNotFoundException ex) {
		            	Logger.getLogger(MSSQLAnalyser.class.getName()).log(Level.SEVERE, null, ex);
		            } catch (UnsupportedEncodingException ex) {
		            	Logger.getLogger(MSSQLAnalyser.class.getName()).log(Level.SEVERE, null, ex);
		            }
					
				} catch (ParseException e) {
					e.printStackTrace();
				}
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
            	query = "SELECT * FROM actor a left join film_actor fc on a.actor_id = fc.actor_id left join film f on fc.film_id = f.film_id where f.film_id = 106";
            	query = "SELECT * FROM actor a left join film_actor fc on a.actor_id = fc.actor_id left join film f on fc.film_id = f.film_id where f.film_id = 106 group by a.actor_id order by a.first_name;";
            	if(doubleVersion >= 5.6){
            		PreparedStatement preparedStatement = connection.prepareStatement("EXPLAIN format = JSON "+query);
            		ResultSet resultSet = preparedStatement.executeQuery();
            		while(resultSet.next()){
            			MySQLQueryDetails objMySQLQueryDetails = new MySQLQueryDetails();
            			objMySQLQueryDetails.setQueryPlan(resultSet.getString(1));
            			listMySQLQueryDetails.add(objMySQLQueryDetails);
            		}
            		resultSet.close();
            	}else{
            		PreparedStatement preparedStatement = connection.prepareStatement("EXPLAIN EXTENDED "+query);
            		ResultSet resultSet = preparedStatement.executeQuery();
            		while(resultSet.next()){
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
	
	private String parseJSON(JSONObject obj, int parentNode, StringBuilder stringBuilderHTMLReport){
		
		if(obj != null){
			if(obj.has(MySQLOperation.table.toString())){
				parseTableNode(obj.getJSONObject("table"), parentNode, stringBuilderHTMLReport);
			}else if(obj.has(MySQLOperation.nested_loop.toString())){
				JSONArray jsonArrayTable = obj.getJSONArray(MySQLOperation.nested_loop.toString());
				for(int i = 0; i < jsonArrayTable.length(); i++){
					parentNode = i;
					parseJSON(jsonArrayTable.getJSONObject(i), parentNode, stringBuilderHTMLReport);
				}
			}else if(obj.has(MySQLOperation.grouping_operation.toString())){
				JSONObject jsonObjectGroupingOperation = obj.getJSONObject(MySQLOperation.grouping_operation.toString());
				parseJSON(jsonObjectGroupingOperation, parentNode, stringBuilderHTMLReport);
			}else if(obj.has(MySQLOperation.ordering_operation.toString())){
				JSONObject jsonObjectOrderingOperation = obj.getJSONObject(MySQLOperation.ordering_operation.toString());
				parseJSON(jsonObjectOrderingOperation, parentNode, stringBuilderHTMLReport);
			}
		}
		return stringBuilderHTMLReport.toString();
	}
	
	private void parseTableNode(JSONObject objTableNode, int parentNode, StringBuilder stringBuilder){
		if(objTableNode != null){
			stringBuilder.append("<div class=\"tableBlockMySQL\">");
			if(parentNode <= 0){
				stringBuilder.append("<div id=\"parent").append(parentNode).append("\" class=\"connectionDot\"></div>");
			}else{
				/*stringBuilder.append("<div class=\"nestedLoopDivMySQL\"><div id=\"parent").append(parentNode).append("\"><div style=\"height:32px\"></div>nested loop</div></div>");*/
				stringBuilder.append("<div class=\"nestedLoopDivMySQL\" id=\"parent").append(parentNode).append("\"><div style=\"height:7px\"></div>nested loop</div>");
			}
			
			
			/* Attach runtime style to node */
			String nodeStyle = "";
			StringBuilder nodeAttribute = new StringBuilder("");
			String nodeContent = "";
			
			/* Loop through all properties of JSONObject("table") */
			Iterator<String> iteratorProperties = objTableNode.keys();
			while(iteratorProperties.hasNext()){
				/* Get key from JSONObject */
				String property = iteratorProperties.next();
				/* Get value of key */
				String propertyValue = objTableNode.getString(property);
				
				nodeAttribute.append(property).append(" = ").append(" \"<b>").append(property).append("</b>: ").append(propertyValue.replace("\"","quote;")).append("\"").append(" ");
				
				System.out.println("key: "+ property + "; value: " + propertyValue);
				
				if(MySQLTableProperties.access_type.toString().equalsIgnoreCase(property)){
					if("ALL".equalsIgnoreCase(propertyValue)){
						nodeContent += "Full Table Scan";
						/* rgb(245, 110, 110): nearly `red` */
						nodeStyle = "background-color: rgb(245, 110, 110);";
					}else if("range".equalsIgnoreCase(propertyValue)){
						nodeContent += "Index Range Scan";
						/* rgb(215, 169, 26): nearly `brown` */
						nodeStyle = "background-color: rgb(215, 169, 26);";
					}else if("const".equalsIgnoreCase(propertyValue)){
						nodeContent += "Single Row(Constant)";
						/* rgb(26, 107, 215): nearly `blue` */
						nodeStyle = "background-color: rgb(26, 107, 215);";
					}else if("ref".equalsIgnoreCase(propertyValue)){
						nodeContent += "Non-Unique Key Lookup";
						nodeStyle = "background-color: green;";
					}else if("eq_ref".equalsIgnoreCase(propertyValue)){
						nodeContent += "Unique Key Lookup";
						nodeStyle = "background-color: green;";
					}
				}
			}
			
			String prepareTableBlock = "<div id=\"child"+parentNode+"\" class=\"tableBlockContentMySQL\" style=\""+nodeStyle+"\" "+nodeAttribute+">";
			prepareTableBlock += nodeContent;
			prepareTableBlock += "</div>";
			
			/* Append TableBlock to stringBuilder */
			stringBuilder.append(prepareTableBlock);
			stringBuilder.append("<script>");
			stringBuilder.append(CommonUtil.getjsPlumbScript("child"+parentNode, "parent"+parentNode, jsPlumbArrowPosition.TopCenter, jsPlumbArrowPosition.BottomCenter));
			stringBuilder.append(CommonUtil.getjsPlumbScript("parent"+parentNode, "parent"+(parentNode+1), jsPlumbArrowPosition.RightMiddle, jsPlumbArrowPosition.LeftMiddle));
			stringBuilder.append("</script>");
			
			
			if(objTableNode.has(MySQLTableProperties.table_name.toString())){
				stringBuilder.append(objTableNode.get(MySQLTableProperties.table_name.toString()));
			}
			
			if(objTableNode.has(MySQLTableProperties.key.toString())){
				stringBuilder.append("<div style=\"font-size:11px;font-weight:bold\">").append(objTableNode.get(MySQLTableProperties.key.toString())).append("</div>");
			}
			
			stringBuilder.append("</div>");
		}
	}
}
