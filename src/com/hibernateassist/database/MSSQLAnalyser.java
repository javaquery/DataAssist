package com.hibernateassist.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.hibernate.SQLQuery;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.hibernateassist.common.CommonUtil;
import com.hibernateassist.common.CommonUtil.jsPlumbArrowPosition;
import com.hibernateassist.pojo.MSSQLQueryDetails;

/**
 * @author vicky.thakor
 */
public class MSSQLAnalyser extends AbstractDAO implements Analyser{
	
	enum ExecutionPlanEnum {
		AvgRowSize, EstimateCPU, EstimateIO, EstimateOperatorCost, EstimateRebinds,
		EstimateRewinds, EstimateRows, LogicalOp, NodeId, Parallel,
		PhysicalOp, EstimatedTotalSubtreeCost
	}
	
	private static final Logger logger = Logger.getLogger(MSSQLAnalyser.class.getName());
	private String QueryType;
	private String QueryHash;
    private String StatementSubTreeCost;
    
    /**
     * Generate Query report from Hibernate Criteria.
     * @param hibernateQuery
     * @param reportFolderPath
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    @Override
	public void generateQueryReport(String hibernateQuery, String actualQuery, String reportFolderPath, String strFilenamePrefix) throws Exception{
		List<MSSQLQueryDetails> listMssqlQueryDetails = getExecutionPlan(hibernateQuery);
		if(listMssqlQueryDetails != null && !listMssqlQueryDetails.isEmpty()){
			for (MSSQLQueryDetails mssqlQueryDetails : listMssqlQueryDetails) {
				if(mssqlQueryDetails.getQueryPlan() != null && !mssqlQueryDetails.getQueryPlan().isEmpty()){

					StringBuilder stringBuilderHTMLReport = new StringBuilder("");
					stringBuilderHTMLReport.append(CommonUtil.getHTMLReportHeader());
					stringBuilderHTMLReport.append(getExecutionPlanStatistics(mssqlQueryDetails.getExecutionCount(), mssqlQueryDetails.getLastExecutionTime(), mssqlQueryDetails.getLastElapsedTime(), mssqlQueryDetails.getLastLogicalReads(), mssqlQueryDetails.getLastLogicalWrites()));
					stringBuilderHTMLReport.append(parseXML(getXMLDocument(mssqlQueryDetails.getQueryPlan(), null)));
					stringBuilderHTMLReport.append(CommonUtil.getHTMLReportFooter());
					
					reportFolderPath = reportFolderPath == null ? "" : reportFolderPath;
					new CommonUtil().createHTMLReportFile(stringBuilderHTMLReport.toString(), strFilenamePrefix, reportFolderPath);
				}
			}
		}else{
			logger.info("Execution plan not found on database. Please execute same Criteria multiple times to generate execution plan.");
		}
	}
	
	/**
	 * Generate HTML Report from Microsoft SQL file.
	 * @author vicky.thakor
	 * @param sqlPlanXMLFile
	 * @param reportFolderPath
	 */
	public void generateQueryReportFromFile(String sqlPlanXMLFile, String reportFolderPath, String strFilenamePrefix){
		StringBuilder stringBuilderHTMLReport = new StringBuilder("");
		stringBuilderHTMLReport.append(CommonUtil.getHTMLReportHeader());
		stringBuilderHTMLReport.append(parseXML(getXMLDocument(null, sqlPlanXMLFile)));
		stringBuilderHTMLReport.append(CommonUtil.getHTMLReportFooter());
		
		reportFolderPath = reportFolderPath == null ? "" : reportFolderPath;
		new CommonUtil().createHTMLReportFile(stringBuilderHTMLReport.toString(), strFilenamePrefix + "_" + CommonUtil.getRandomString(5), reportFolderPath);
	}
	
	/**
	 * Get execution plans from database
	 * @author vicky.thakor
	 * @param query
	 * @return {@link MSSQLQueryDetails}
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private List<MSSQLQueryDetails> getExecutionPlan(String query) throws ClassNotFoundException, SQLException{
		List<MSSQLQueryDetails> listMSSQLQueryDetails = new ArrayList<MSSQLQueryDetails>();
		
		if(query == null || query.isEmpty()){
			logger.info("Please provide valid query");
		}else if(getHibernateSession() != null){
			BufferedReader objBufferedReader = null;
			
			query = CommonUtil.replaceQuestionMarkWithP(query);
			/**
        	 * Microsoft SQL Server can process String of 2000 character only in `LIKE` clause.
        	 * We'll trim last 2000 Characters as it contains `WHERE`, `JOIN`, etc... clause and that makes query unique.
        	 */
			if(query != null && !query.isEmpty() && query.length() > 2000){
            	int position = query.length() - 2000;
            	query = query.substring(position, query.length());
            }
			
			SQLQuery objSQLQuery = getHibernateSession().createSQLQuery("WITH XMLNAMESPACES (default 'http://schemas.microsoft.com/sqlserver/2004/07/showplan') "
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
			List<Object[]> executionPlanDetails = objSQLQuery.list();
			for(Object[] obj : executionPlanDetails){
				MSSQLQueryDetails objMssqlQueryDetails = new MSSQLQueryDetails();
				Clob clobXML = (Clob) obj[1];
				StringBuilder objStringBuilder = new StringBuilder("");
				String line;
				try {
					objBufferedReader = new BufferedReader(clobXML.getCharacterStream());
					while ((line = objBufferedReader.readLine())!=null) {
						objStringBuilder.append(line);
						objStringBuilder.append(System.getProperty("line.separator"));
				    }
				} catch (Exception e) {
					e.printStackTrace();
				}finally{
					if(objBufferedReader != null){
						try {
							objBufferedReader.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				
            	objMssqlQueryDetails.setSqlText(String.valueOf(obj[0]));
            	objMssqlQueryDetails.setQueryPlan(objStringBuilder.toString());
            	objMssqlQueryDetails.setExecutionCount(Integer.valueOf(String.valueOf(obj[2])));
            	objMssqlQueryDetails.setLastExecutionTime(String.valueOf(obj[3]));
            	objMssqlQueryDetails.setLastElapsedTime(Integer.valueOf(String.valueOf(obj[4])));
            	objMssqlQueryDetails.setLastLogicalReads(Integer.valueOf(String.valueOf(obj[5])));
            	objMssqlQueryDetails.setLastLogicalWrites(Integer.valueOf(String.valueOf(obj[6])));
            	
            	listMSSQLQueryDetails.add(objMssqlQueryDetails);
			}
		}
		
		return listMSSQLQueryDetails;
	}
	
	/**
	 * Get XML Document instance from String or File
	 * @author vicky.thakor
	 * @param fromString
	 * @param fromFile
	 * @return {@link Document}
	 */
	private Document getXMLDocument(String fromString, String fromFile){
		Document objDocument = null;
		try {
			DocumentBuilderFactory objDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder objDocumentBuilder = objDocumentBuilderFactory.newDocumentBuilder();
			if(fromString != null && !fromString.isEmpty()){
				InputSource inputSource = new InputSource(new StringReader(fromString));
	            objDocument = objDocumentBuilder.parse(inputSource);
			}else if(fromFile != null && !fromFile.isEmpty()){
				File xmlFile = new File(fromFile);
				/* Check if file is exists or not */
	            if (xmlFile.exists()) {
	                /* Check you have permission to read file or not */
	                if (xmlFile.canRead()) {
	                	objDocument = objDocumentBuilder.parse(xmlFile);
	                }
	            }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return objDocument;
	}
	
	/**
	 * Parse Execution Plan XML and get HTML Report.
	 * @author vicky.thakor
	 * @param objDocument
	 */
	private String parseXML(Document objDocument){
		StringBuilder stringBuilderHTMLReport = new StringBuilder("");
		try {
			if(objDocument != null){
	            NodeList listQueryStatement = objDocument.getElementsByTagName("StmtSimple");
	            NodeList listRootNode = objDocument.getElementsByTagName("QueryPlan");
	            NodeList listMissingIndexNode = objDocument.getElementsByTagName("MissingIndexGroup");
	            
	            if (listRootNode.getLength() == 1 && listQueryStatement.getLength() > 0) {
            		/* Get query properties and create HTML report */
            		getQueryProperties(listQueryStatement, stringBuilderHTMLReport);
            		stringBuilderHTMLReport.append("<div class=\"graphical_data\">");
            	 	stringBuilderHTMLReport.append("<div class=\"operation_header\">");
                    stringBuilderHTMLReport.append("<h3>Execution Plan - Microsoft SQL Server</h3>");
                    stringBuilderHTMLReport.append("It is the graphical representation of query executed on database. Each node in it plays significant role in query. Click on node for more information.");
                    stringBuilderHTMLReport.append("</div>");
                    stringBuilderHTMLReport.append("<div style=\"position:relative\">");
                    stringBuilderHTMLReport.append("<div id=\"nodeDetails\"></div>");
                    stringBuilderHTMLReport.append("<div id=\"parent-1\" style=\"height:500px;max-height:500px;overflow:scroll;white-space: nowrap;position:relative\" class=\"whiteFadedbox\">");
                    stringBuilderHTMLReport.append(getRootNodeImage());
            		stringBuilderHTMLReport.append("</div><script>");
                    recursiveXMLParse(listRootNode, -1, stringBuilderHTMLReport);
                    stringBuilderHTMLReport.append("</script>");
                    stringBuilderHTMLReport.append("</div>");
                    stringBuilderHTMLReport.append("</div>");
	            }
	            
	            if (listMissingIndexNode != null && listMissingIndexNode.getLength() > 0) {
	            	stringBuilderHTMLReport.append("<div class=\"statistics_data\">");
	            	stringBuilderHTMLReport.append("<div class=\"operation_header\"><h3>Missing Index Details</h3>");
	            	stringBuilderHTMLReport.append("Following script is Index suggested by database. Contact your database administrator with query and following script.");
	            	stringBuilderHTMLReport.append("</div>");
	            	stringBuilderHTMLReport.append("<div class=\"whiteFadedbox\">");
	            	ScanMissingIndexes(listMissingIndexNode, stringBuilderHTMLReport);
	            	stringBuilderHTMLReport.append("</div>");
	            	stringBuilderHTMLReport.append("</div>");
                }
			}else{
				logger.info("Not a valid XML document.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringBuilderHTMLReport.toString();
	}
	
	/**
	 * Get basic details of query like Estimated Rows, QueryHash, etc...
	 * @author vicky.thakor
	 * @param listQueryStatement
	 * @param stringBuilderHTMLReport
	 */
	private void getQueryProperties(NodeList listQueryStatement, StringBuilder stringBuilderHTMLReport) {
        if (listQueryStatement != null) {
            if (listQueryStatement.item(0) != null) {
                if (listQueryStatement.item(0).getNodeType() == Node.ELEMENT_NODE) {
                    Element elementQueryStatement = (Element) listQueryStatement.item(0);
                    NamedNodeMap queryAttributes = elementQueryStatement.getAttributes();

                    if (queryAttributes.getNamedItem("StatementText") != null) {
                    	String strStatementType = queryAttributes.getNamedItem("StatementType") != null ? queryAttributes.getNamedItem("StatementType").getNodeValue() : "NA";
                        String strStatementEstRows = queryAttributes.getNamedItem("StatementEstRows") != null ? queryAttributes.getNamedItem("StatementEstRows").getNodeValue() : "NA";
                        String strStatementSubTreeCost = queryAttributes.getNamedItem("StatementSubTreeCost") != null ? queryAttributes.getNamedItem("StatementSubTreeCost").getNodeValue() : "NA";
                        String strStatementOptmLevel = queryAttributes.getNamedItem("StatementOptmLevel") != null ? queryAttributes.getNamedItem("StatementOptmLevel").getNodeValue() : "NA";
                        String strStatementOptmEarlyAbortReason = queryAttributes.getNamedItem("StatementOptmEarlyAbortReason") != null ? queryAttributes.getNamedItem("StatementOptmEarlyAbortReason").getNodeValue() : "NA";
                        String strQueryHash = queryAttributes.getNamedItem("QueryHash") != null ? queryAttributes.getNamedItem("QueryHash").getNodeValue() : "NA";
                        String strQueryPlanHash = queryAttributes.getNamedItem("QueryPlanHash") != null ? queryAttributes.getNamedItem("QueryPlanHash").getNodeValue() : "NA";
                    	
                    	stringBuilderHTMLReport.append("<div class=\"statistics_data\">");
                        stringBuilderHTMLReport.append("<div class=\"operation_header\"><h3>Query Statistics</h3>");
                        stringBuilderHTMLReport.append("Data shows actual query generated by Hibernate, Estimated Rows and other query properties. In case of report generated from .sqlplan file then all data taken from .sqlplan file.");
                		stringBuilderHTMLReport.append("</div>");
                        stringBuilderHTMLReport.append("<div class=\"whiteFadedbox\">");
                        stringBuilderHTMLReport.append("<table class=\"stylistTable\" style=\"width:100%\"><thead>");
                        stringBuilderHTMLReport.append(CommonUtil.prepareTableRow("Query Type", "Estimated Rows", "Sub Tree Cost", "Optimization Level", "Early Abort Reason", "QueryHash", "QueryPlanHash"));                        
                        stringBuilderHTMLReport.append("</thead>");
                        stringBuilderHTMLReport.append("<tbody>");
                        stringBuilderHTMLReport.append(CommonUtil.prepareTableRow(strStatementType, strStatementEstRows, strStatementSubTreeCost, strStatementOptmLevel, strStatementOptmEarlyAbortReason, strQueryHash, strQueryPlanHash));
                        stringBuilderHTMLReport.append("</tbody></table><br/>");
                        stringBuilderHTMLReport.append("<div style=\"overflow:scroll;height:100px;font-size:15px\">");
                        stringBuilderHTMLReport.append(queryAttributes.getNamedItem("StatementText").getNodeValue());
                        stringBuilderHTMLReport.append("</div>");
                        stringBuilderHTMLReport.append("</div>");
                        stringBuilderHTMLReport.append("</div>");
                        
                        setQueryType(strStatementType);
                        setStatementSubTreeCost(strStatementSubTreeCost);
                        setQueryHash(strQueryHash);
                    }
                }
            }
        }
    }
	
	/**
	 * Get root node operation image (i.e: SELECT, UPDATE, DELETE)
	 * @author vicky.thakor
	 * @return
	 */
	private String getRootNodeImage(){
		String HTMLContent = "<div style=\"display: inline-block;vertical-align:top;text-align:center\">";
		if("SELECT".equalsIgnoreCase(getQueryType())){
			HTMLContent += getHTMLNodeImage(SQLServerOperationImageEnum.Select.getImagePosition(), SQLServerOperationImageEnum.Select.getTitle(), "", "Select", 0, -2,"").replace("\\", "");
		}else if("UPDATE".equalsIgnoreCase(getQueryType())){
			HTMLContent += getHTMLNodeImage(SQLServerOperationImageEnum.TSQLIcon.getImagePosition(), SQLServerOperationImageEnum.Update.getTitle(), "", "Update", 0, -2,"").replace("\\", "");
		}else if("DELETE".equalsIgnoreCase(getQueryType())){
			HTMLContent += getHTMLNodeImage(SQLServerOperationImageEnum.TSQLIcon.getImagePosition(), SQLServerOperationImageEnum.Delete.getTitle(), "", "Delete", 0, -2,"").replace("\\", "");
		}else{
			HTMLContent += getHTMLNodeImage(SQLServerOperationImageEnum.IconNotFound.getImagePosition(), getQueryType(), "", getQueryType(), 0, -2,"").replace("\\", "");
		}
		HTMLContent += "</div>";
		return HTMLContent;
	}
	
	/**
	 * Parse all XML node by recursive method call
	 * @author vicky.thakor 
	 * @param nodeList
	 * @param parentNode
	 * @param stringBuilderHTMLReport
	 */
    private void recursiveXMLParse(NodeList nodeList, int parentNode, StringBuilder stringBuilderHTMLReport) {
        int nodeLength = nodeList.getLength();
        for (int i = 0; i < nodeLength; i++) {
            if (nodeList.item(i).hasChildNodes()) {
                Element element = (Element) nodeList.item(i);
                if ("RelOp".equals(element.getTagName())) {
                    parentNode = generateChildDetails(nodeList.item(i), nodeList, parentNode, stringBuilderHTMLReport);
                }
                recursiveXMLParse(nodeList.item(i).getChildNodes(), parentNode, stringBuilderHTMLReport);
            }
        }
    }
	
    /**
     * Get child node details.
     * @author vicky.thakor
     * @param parentNode
     * @param childNodes
     * @param parentNodeID
     * @param stringBuilderHTMLReport
     * @return
     */
    private int generateChildDetails(Node parentNode, NodeList childNodes, int parentNodeID, StringBuilder stringBuilderHTMLReport) {
    	try {
    		int oldParentID = parentNodeID;
            Element elementParent = (Element) parentNode;
            if ("RelOp".equals(elementParent.getTagName())) {
                NamedNodeMap nodeAttributesParent = elementParent.getAttributes();
                if (nodeAttributesParent != null) {
                    if (nodeAttributesParent.getNamedItem("NodeId") != null && nodeAttributesParent.getNamedItem("NodeId").getNodeValue() != null) {
                        parentNodeID = Integer.parseInt(nodeAttributesParent.getNamedItem("NodeId").getNodeValue());
                    }
                }
            }
            
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element elementChild = (Element) childNodes.item(i);
                    if ("RelOp".equals(elementChild.getTagName())) {
                        String operationType = "";
                        NamedNodeMap nodeAttributesChild = elementChild.getAttributes();
                        int childNodeID = 0;
                        if (nodeAttributesChild != null) {
                            Map<String, String> mapNodeProperty = getNodeProperty(nodeAttributesChild);

                            if (mapNodeProperty != null && !mapNodeProperty.isEmpty()) {
                                if (mapNodeProperty.get(ExecutionPlanEnum.NodeId.toString()) != null) {
                                    childNodeID = Integer.parseInt(mapNodeProperty.get(ExecutionPlanEnum.NodeId.toString()));
                                    /* oldParentID != 0 First element in the list */
                                    if (oldParentID != 0 && oldParentID == childNodeID) {
                                        break;
                                    }
                                }

                                if (mapNodeProperty.get(ExecutionPlanEnum.LogicalOp.toString()) != null) {
                                    /* Get attribute value */
                                    operationType = mapNodeProperty.get(ExecutionPlanEnum.LogicalOp.toString());
                                }

                                if (mapNodeProperty.get(ExecutionPlanEnum.PhysicalOp.toString()) != null) {
                                    /* Get attribute value */
                                    String physicalOperation = mapNodeProperty.get(ExecutionPlanEnum.PhysicalOp.toString());
                                    operationType = operationType + ":" + physicalOperation;

                                    Map<String, String> mapOperationProperty = getOperationProperty(physicalOperation, elementChild);
                                    if (mapOperationProperty != null && !mapNodeProperty.isEmpty()) {
                                         mapNodeProperty.putAll(mapOperationProperty);
                                    }
                                }

                                if (!operationType.isEmpty()) {
                                    stringBuilderHTMLReport.append("$(\"#parent").append(oldParentID).append("\")");
                                    stringBuilderHTMLReport.append(".append(\"<div id=\\\"parent").append(childNodeID).append("\\\"");
                                    stringBuilderHTMLReport.append(" style=\\\"padding-left:95px;padding-bottom:20px;display: inline-block;\\\">");
                                    stringBuilderHTMLReport.append(getElementImage(operationType, mapNodeProperty, childNodeID));
                                    stringBuilderHTMLReport.append("</div><br/>\");\n");
                                    
                                    /* Set Source and Target node for jsPlumb */
                                    String sourceNode = "parentTable"+childNodeID;
                                    String targetNode = oldParentID == -1 ? "rootnode" : "parentTable"+oldParentID; 
                                    stringBuilderHTMLReport.append(CommonUtil.getjsPlumbScript(sourceNode, targetNode, jsPlumbArrowPosition.LeftMiddle, jsPlumbArrowPosition.RightMiddle));
                                }
                            }
                        }
                    }
                }
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return parentNodeID;
    }
    
    /**
     * Return attributes of current node. i.e: PhysicalOp, LogicalOp, EstimateCPU, etc...
     * @author vicky.thakor
     * @param nodeAttributes
     * @return {@link Map<String, String>}
     */
    private Map<String, String> getNodeProperty(NamedNodeMap nodeAttributes) {
        Map<String, String> mapNodeProperty = new LinkedHashMap<String, String>();
        if (nodeAttributes != null) {
            if (nodeAttributes.getNamedItem(ExecutionPlanEnum.PhysicalOp.toString()) != null
                    && nodeAttributes.getNamedItem(ExecutionPlanEnum.PhysicalOp.toString()).getNodeValue() != null
                    && !nodeAttributes.getNamedItem(ExecutionPlanEnum.PhysicalOp.toString()).getNodeValue().isEmpty()) {
                mapNodeProperty.put(ExecutionPlanEnum.PhysicalOp.toString(), nodeAttributes.getNamedItem(ExecutionPlanEnum.PhysicalOp.toString()).getNodeValue());
            }

            if (nodeAttributes.getNamedItem(ExecutionPlanEnum.LogicalOp.toString()) != null
                    && nodeAttributes.getNamedItem(ExecutionPlanEnum.LogicalOp.toString()).getNodeValue() != null
                    && !nodeAttributes.getNamedItem(ExecutionPlanEnum.LogicalOp.toString()).getNodeValue().isEmpty()) {
                mapNodeProperty.put(ExecutionPlanEnum.LogicalOp.toString(), nodeAttributes.getNamedItem(ExecutionPlanEnum.LogicalOp.toString()).getNodeValue());
            }

            if (nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimateIO.toString()) != null
                    && nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimateIO.toString()).getNodeValue() != null
                    && !nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimateIO.toString()).getNodeValue().isEmpty()) {
                mapNodeProperty.put(ExecutionPlanEnum.EstimateIO.toString(), nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimateIO.toString()).getNodeValue());
            }

            if (nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimateCPU.toString()) != null
                    && nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimateCPU.toString()).getNodeValue() != null
                    && !nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimateCPU.toString()).getNodeValue().isEmpty()) {
                mapNodeProperty.put(ExecutionPlanEnum.EstimateCPU.toString(), nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimateCPU.toString()).getNodeValue());
            }

            if (mapNodeProperty.get(ExecutionPlanEnum.EstimateIO.toString()) != null
                    && mapNodeProperty.get(ExecutionPlanEnum.EstimateCPU.toString()) != null) {
                double EstimateIO = Double.valueOf(mapNodeProperty.get(ExecutionPlanEnum.EstimateIO.toString()));
                double EstimateCPU = Double.valueOf(mapNodeProperty.get(ExecutionPlanEnum.EstimateCPU.toString()));
                mapNodeProperty.put(ExecutionPlanEnum.EstimateOperatorCost.toString(), String.valueOf(EstimateIO + EstimateCPU));
            }

            if (nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimatedTotalSubtreeCost.toString()) != null
                    && nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimatedTotalSubtreeCost.toString()).getNodeValue() != null
                    && !nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimatedTotalSubtreeCost.toString()).getNodeValue().isEmpty()) {
                mapNodeProperty.put(ExecutionPlanEnum.EstimatedTotalSubtreeCost.toString(), nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimatedTotalSubtreeCost.toString()).getNodeValue());
            }

            if (nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimateRows.toString()) != null
                    && nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimateRows.toString()).getNodeValue() != null
                    && !nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimateRows.toString()).getNodeValue().isEmpty()) {
                mapNodeProperty.put(ExecutionPlanEnum.EstimateRows.toString(), nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimateRows.toString()).getNodeValue());
            }

            if (nodeAttributes.getNamedItem(ExecutionPlanEnum.AvgRowSize.toString()) != null
                    && nodeAttributes.getNamedItem(ExecutionPlanEnum.AvgRowSize.toString()).getNodeValue() != null
                    && !nodeAttributes.getNamedItem(ExecutionPlanEnum.AvgRowSize.toString()).getNodeValue().isEmpty()) {
                double convertSizeInKB = Long.valueOf(nodeAttributes.getNamedItem(ExecutionPlanEnum.AvgRowSize.toString()).getNodeValue());
                convertSizeInKB = Math.round(convertSizeInKB / 1024);
                mapNodeProperty.put(ExecutionPlanEnum.AvgRowSize.toString(), convertSizeInKB + "KB");
            }

            if (nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimateRebinds.toString()) != null
                    && nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimateRebinds.toString()).getNodeValue() != null
                    && !nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimateRebinds.toString()).getNodeValue().isEmpty()) {
                mapNodeProperty.put(ExecutionPlanEnum.EstimateRebinds.toString(), nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimateRebinds.toString()).getNodeValue());
            }
            if (nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimateRewinds.toString()) != null
                    && nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimateRewinds.toString()).getNodeValue() != null
                    && !nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimateRewinds.toString()).getNodeValue().isEmpty()) {
                mapNodeProperty.put(ExecutionPlanEnum.EstimateRewinds.toString(), nodeAttributes.getNamedItem(ExecutionPlanEnum.EstimateRewinds.toString()).getNodeValue());
            }

            if (nodeAttributes.getNamedItem(ExecutionPlanEnum.NodeId.toString()) != null
                    && nodeAttributes.getNamedItem(ExecutionPlanEnum.NodeId.toString()).getNodeValue() != null
                    && !nodeAttributes.getNamedItem(ExecutionPlanEnum.NodeId.toString()).getNodeValue().isEmpty()) {
                mapNodeProperty.put(ExecutionPlanEnum.NodeId.toString(), nodeAttributes.getNamedItem(ExecutionPlanEnum.NodeId.toString()).getNodeValue());
            }
            if (nodeAttributes.getNamedItem(ExecutionPlanEnum.Parallel.toString()) != null
                    && nodeAttributes.getNamedItem(ExecutionPlanEnum.Parallel.toString()).getNodeValue() != null
                    && !nodeAttributes.getNamedItem(ExecutionPlanEnum.Parallel.toString()).getNodeValue().isEmpty()) {
                mapNodeProperty.put(ExecutionPlanEnum.Parallel.toString(), nodeAttributes.getNamedItem(ExecutionPlanEnum.Parallel.toString()).getNodeValue());
            }

        }
        return mapNodeProperty;
    }
    
    /**
     * Get column specific operation details.
     * @author vicky.thakor
     * @param operationType
     * @param elementRelOp
     * @return
     */
    private Map<String, String> getOperationProperty(String operationType, Element elementRelOp) {
        Map<String, String> mapOperationProperty = new LinkedHashMap<String, String>();
        
        if(elementRelOp.hasChildNodes()){
        	for(int i = 0; i < elementRelOp.getChildNodes().getLength(); i++){
        		if(elementRelOp.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE){
        			Element elementWarnings = (Element) elementRelOp.getChildNodes().item(i);
                	if("Warnings".equalsIgnoreCase(elementWarnings.getNodeName())){
                		getAttributeMap(elementWarnings.getAttributes(), mapOperationProperty);
                	}
        		}
        	}
        }
        
        if ("Sort".equalsIgnoreCase(operationType)) {
            if (elementRelOp.getElementsByTagName("Sort") != null) {
                Node sortNode = elementRelOp.getElementsByTagName("Sort").item(0);
                if (sortNode.hasChildNodes() && sortNode.getChildNodes().item(1) != null && "OrderBy".equalsIgnoreCase(sortNode.getChildNodes().item(1).getNodeName())) {
                    Node OrderByNode = sortNode.getChildNodes().item(1);
                    if (OrderByNode.getChildNodes().item(1) != null && "OrderByColumn".equalsIgnoreCase(OrderByNode.getChildNodes().item(1).getNodeName())) {
                        Node OrderByColumnNode = OrderByNode.getChildNodes().item(1);
                        if (OrderByColumnNode.getAttributes() != null
                                && OrderByColumnNode.getAttributes().getNamedItem("Ascending") != null
                                && OrderByColumnNode.getAttributes().getNamedItem("Ascending").getNodeValue() != null
                                && !OrderByColumnNode.getAttributes().getNamedItem("Ascending").getNodeValue().isEmpty()) {
                            mapOperationProperty.put("Ascending", OrderByColumnNode.getAttributes().getNamedItem("Ascending").getNodeValue());
                        }

                        if ("ColumnReference".equalsIgnoreCase(OrderByColumnNode.getChildNodes().item(1).getNodeName())) {
                            Node ColumnReferenceNode = OrderByColumnNode.getChildNodes().item(1);
                            NamedNodeMap namedNodeMap = ColumnReferenceNode.getAttributes();
                            getAttributeMap(namedNodeMap, mapOperationProperty);
                        }
                    }
                }

            }
        }else if("Table Scan".equalsIgnoreCase(operationType)){
        	if(elementRelOp.getElementsByTagName("TableScan") != null){
        		Element elementTableScan = (Element) elementRelOp.getElementsByTagName("TableScan").item(0);
        		
        		/* Attributes of IndexScan node */
    			NamedNodeMap namedNodeMapTableScan = elementTableScan.getAttributes();
    			getAttributeMap(namedNodeMapTableScan, mapOperationProperty);
        		
        		if(elementTableScan.hasChildNodes()){
        			for(int i = 0; i < elementTableScan.getChildNodes().getLength(); i++){
        				if(elementTableScan.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE){
        					Element elementChildNode = (Element) elementTableScan.getChildNodes().item(i);
        					if("Object".equalsIgnoreCase(elementChildNode.getNodeName())){
        						NamedNodeMap namedNodeMap = elementChildNode.getAttributes();
        						getAttributeMap(namedNodeMap, mapOperationProperty);
        					}else if("Predicate".equalsIgnoreCase(elementChildNode.getNodeName())){
        						Element elementScalarOperator = (Element) elementChildNode.getElementsByTagName("ScalarOperator").item(0);
                				NamedNodeMap namedNodeMap = elementScalarOperator.getAttributes();
                				getAttributeMap(namedNodeMap, mapOperationProperty);
        					}
        				}
        			}        			
        		}
        	}
        }else if("Nested Loops".equalsIgnoreCase(operationType)){
        	if(elementRelOp.getElementsByTagName("NestedLoops") != null){
        		Node NestedLoopNode = elementRelOp.getElementsByTagName("NestedLoops").item(0);
        		if(NestedLoopNode.hasChildNodes()){
        			for(int k = 0; k < NestedLoopNode.getChildNodes().getLength(); k++){
        				if(NestedLoopNode.getChildNodes().item(k).getNodeType() == Node.ELEMENT_NODE){
        					Element elementChildNode = (Element) NestedLoopNode.getChildNodes().item(k);
                			if("OuterReferences".equalsIgnoreCase(elementChildNode.getNodeName())){
                				if(elementChildNode.hasChildNodes()){
                					for(int i = 0; i< elementChildNode.getChildNodes().getLength(); i++){
                						if(elementChildNode.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE){
                							Element ColumnReferenceElement = (Element) elementChildNode.getChildNodes().item(i);
                							if("ColumnReference".equalsIgnoreCase(ColumnReferenceElement.getNodeName())){
                								NamedNodeMap namedNodeMap = ColumnReferenceElement.getAttributes();
                								getAttributeMap(namedNodeMap, mapOperationProperty);
                							}
                						}
                					}
                				}
                			}else if("Predicate".equalsIgnoreCase(elementChildNode.getNodeName())){
                				if(elementChildNode.hasChildNodes()){
                    				Element elementScalarOperator = (Element) elementChildNode.getElementsByTagName("ScalarOperator").item(0);
                    				NamedNodeMap namedNodeMap = elementScalarOperator.getAttributes();
                    				getAttributeMap(namedNodeMap, mapOperationProperty);
                    			}
                			}
        				}
        			}
        		}
        	}
        }else if("Merge Join".equalsIgnoreCase(operationType)){
        	if(elementRelOp.getElementsByTagName("Merge") != null){
        		Element MergeElement = (Element) elementRelOp.getElementsByTagName("Merge").item(0);
        		
        		/* Attributes of Merge node */
    			NamedNodeMap namedNodeMapMerge = MergeElement.getAttributes();
    			getAttributeMap(namedNodeMapMerge, mapOperationProperty);
        		
        		if(MergeElement.hasChildNodes()){
        			Element ResidualElement = (Element) MergeElement.getElementsByTagName("Residual").item(0);
        			if(ResidualElement.hasChildNodes()){
        				Element elementScalarOperator = (Element) ResidualElement.getElementsByTagName("ScalarOperator").item(0);
        				NamedNodeMap namedNodeMap = elementScalarOperator.getAttributes();
        				getAttributeMap(namedNodeMap, mapOperationProperty);
        			}
        		}
        	}
        }else if("Clustered Index Scan".equalsIgnoreCase(operationType)
        		|| "RID Lookup".equalsIgnoreCase(operationType)
        		|| "Index Scan".equalsIgnoreCase(operationType)){
        	if(elementRelOp.getElementsByTagName("IndexScan") != null){
        		Element elementIndexScan = (Element) elementRelOp.getElementsByTagName("IndexScan").item(0);
        		
        		/* Attributes of IndexScan node */
    			NamedNodeMap namedNodeMapIndex = elementIndexScan.getAttributes();
    			getAttributeMap(namedNodeMapIndex, mapOperationProperty);
        		
        		if(elementIndexScan.hasChildNodes()){
        			for(int i = 0; i < elementIndexScan.getChildNodes().getLength(); i++){
        				if(elementIndexScan.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE){
        					Element elementChildNode = (Element) elementIndexScan.getChildNodes().item(i);
        					if("Object".equalsIgnoreCase(elementChildNode.getNodeName())){
        						NamedNodeMap namedNodeMap = elementChildNode.getAttributes();
        						getAttributeMap(namedNodeMap, mapOperationProperty);
        					}else if("Predicate".equalsIgnoreCase(elementChildNode.getNodeName())){
        						Element elementScalarOperator = (Element) elementChildNode.getElementsByTagName("ScalarOperator").item(0);
                				NamedNodeMap namedNodeMap = elementScalarOperator.getAttributes();
                				getAttributeMap(namedNodeMap, mapOperationProperty);
        					}
        				}
        			}    
        		}
        	}
        }else if("Clustered Index Seek".equalsIgnoreCase(operationType) || "Index Seek".equalsIgnoreCase(operationType)){
        	if(elementRelOp.getElementsByTagName("IndexScan") != null){
        		Element elementIndexScan = (Element) elementRelOp.getElementsByTagName("IndexScan").item(0);
        		
        		/* Attributes of IndexScan node */
    			NamedNodeMap namedNodeMapIndex = elementIndexScan.getAttributes();
    			getAttributeMap(namedNodeMapIndex, mapOperationProperty);
        		
        		if(elementIndexScan.hasChildNodes()){
        			Element elementObject = (Element) elementIndexScan.getElementsByTagName("Object").item(0);
        			NamedNodeMap namedNodeMap = elementObject.getAttributes();
        			getAttributeMap(namedNodeMap, mapOperationProperty);
        		}
        	}
        }else if("Hash Match".equalsIgnoreCase(operationType)){
        	if(elementRelOp.getElementsByTagName("Hash") != null){
        		Element elementHash = (Element) elementRelOp.getElementsByTagName("Hash").item(0);
        		
        		if(elementHash.hasChildNodes()){
        			for(int i = 0; i < elementHash.getChildNodes().getLength(); i++){
        				if(elementHash.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE){
        					Element elementChildNode = (Element) elementHash.getChildNodes().item(i);
        					if("ProbeResidual".equalsIgnoreCase(elementChildNode.getNodeName())){
        						Element elementScalarOperator = (Element) elementChildNode.getElementsByTagName("ScalarOperator").item(0);
                				NamedNodeMap namedNodeMap = elementScalarOperator.getAttributes();
                				getAttributeMap(namedNodeMap, mapOperationProperty);
        					}else if("HashKeysBuild".equalsIgnoreCase(elementChildNode.getNodeName())){
        						Element elementColumnReference = (Element) elementChildNode.getElementsByTagName("ColumnReference").item(0);
        						NamedNodeMap namedNodeMap = elementColumnReference.getAttributes();
        						mapOperationProperty.put("HashKeysBuild,HashKeysProbe", "");
                				getAttributeMap(namedNodeMap, mapOperationProperty);
        					}else if("HashKeysProbe".equalsIgnoreCase(elementChildNode.getNodeName())){
        						Element elementColumnReference = (Element) elementChildNode.getElementsByTagName("ColumnReference").item(0);
        						NamedNodeMap namedNodeMap = elementColumnReference.getAttributes();
        						mapOperationProperty.put("HashKeysBuild,HashKeysProbe", "");
                				getAttributeMap(namedNodeMap, mapOperationProperty);
        					}
        				}
        			}
        		}
        	}
        }else if("Table Delete".equalsIgnoreCase(operationType)
        		|| "Table Update".equalsIgnoreCase(operationType)){
        	if(elementRelOp.getElementsByTagName("Update") != null){
        		Element elementTableDelete = (Element) elementRelOp.getElementsByTagName("Update").item(0);
        		
        		if(elementTableDelete.hasChildNodes()){
        			for(int i = 0; i < elementTableDelete.getChildNodes().getLength(); i++){
        				if(elementTableDelete.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE){
        					Element elementChildNode = (Element) elementTableDelete.getChildNodes().item(i);
        					if("Object".equalsIgnoreCase(elementChildNode.getNodeName())){
        						NamedNodeMap namedNodeMap = elementChildNode.getAttributes();
        						getAttributeMap(namedNodeMap, mapOperationProperty);
        					}else if("Predicate".equalsIgnoreCase(elementChildNode.getNodeName())){
        						Element elementScalarOperator = (Element) elementChildNode.getElementsByTagName("ScalarOperator").item(0);
                				NamedNodeMap namedNodeMap = elementScalarOperator.getAttributes();
                				getAttributeMap(namedNodeMap, mapOperationProperty);
        					}
        				}
        			}        			
        		}
        	}
        }else if("Filter".equalsIgnoreCase(operationType)){
        	if(elementRelOp.getElementsByTagName("Filter") != null){
        		Element elementFilter = (Element) elementRelOp.getElementsByTagName("Filter").item(0);
        		
        		if(elementFilter.hasChildNodes()){
        			for(int i = 0; i < elementFilter.getChildNodes().getLength(); i++){
        				if(elementFilter.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE){
        					Element elementChildNode = (Element) elementFilter.getChildNodes().item(i);
        					if("Predicate".equalsIgnoreCase(elementChildNode.getNodeName())){
        						Element elementScalarOperator = (Element) elementChildNode.getElementsByTagName("ScalarOperator").item(0);
                				NamedNodeMap namedNodeMap = elementScalarOperator.getAttributes();
                				getAttributeMap(namedNodeMap, mapOperationProperty);
        					}
        				}
        			}        			
        		}
        	}
        }else if("Top".equalsIgnoreCase(operationType)){
        	if(elementRelOp.getElementsByTagName("Top") != null){
        		Element elementTop = (Element) elementRelOp.getElementsByTagName("Top").item(0);
        		
        		if(elementTop.hasChildNodes()){
        			for(int i = 0; i < elementTop.getChildNodes().getLength(); i++){
        				if(elementTop.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE){
        					Element elementChildNode = (Element) elementTop.getChildNodes().item(i);
        					if("TopExpression".equalsIgnoreCase(elementChildNode.getNodeName())){
        						Element elementScalarOperator = (Element) elementChildNode.getElementsByTagName("ScalarOperator").item(0);
                				NamedNodeMap namedNodeMap = elementScalarOperator.getAttributes();
                				getAttributeMap(namedNodeMap, mapOperationProperty);
        					}
        				}
        			}        			
        		}
        	}
        }
        return mapOperationProperty;
    }
    
    /**
     * Get all attributes of node.
     * @author vicky.thakor
     * @param namedNodeMap
     * @param mapOperationProperty
     * @return {@link Map<String, String>}
     */
    private Map<String, String> getAttributeMap(NamedNodeMap namedNodeMap, Map<String, String> mapOperationProperty){
        if (namedNodeMap != null && namedNodeMap.getLength() > 0) {
            int attributeLength = namedNodeMap.getLength();
            for (int j = 0; j < attributeLength; j++) {
                Node getAttribute = namedNodeMap.item(j);
                String attributeName = getAttribute.getNodeName();
                attributeName = "ScalarString".equalsIgnoreCase(attributeName) ? "Predicate/ProbeResidual" : attributeName;
                if(mapOperationProperty.get(attributeName) != null){
                	mapOperationProperty.put(attributeName, mapOperationProperty.get(getAttribute.getNodeName()) + ", " + getAttribute.getNodeValue());
                }else{
                	mapOperationProperty.put(attributeName, getAttribute.getNodeValue());
                }
            }
        }
    	return mapOperationProperty;
    }
    
    /**
     * Get HTML content for node image.
     * @param tagName
     * @param mapNodeProperty
     * @return
     */
    private String getElementImage(String tagName, Map<String, String> mapNodeProperty, int parentID) {
        String returnNode = tagName;
        double EstimateOperatorCost = 0.0;
        String nodeName = "";
        String newTagName = "";
        String strNotifyIcon = "";
        
        StringBuilder nodeAttribute = new StringBuilder("");
        if (mapNodeProperty != null && !mapNodeProperty.isEmpty()) {
            for (Map.Entry<String, String> nodeAttr : mapNodeProperty.entrySet()) {
            	String strLabel = "";
            	if("Clustered Index Scan:Clustered Index Scan".equalsIgnoreCase(tagName)
            			|| "Table Scan:Table Scan".equalsIgnoreCase(tagName)){
            		if("Table".equalsIgnoreCase(nodeAttr.getKey())){
            			nodeName = nodeAttr.getValue() +"<br/>";
            			nodeName = nodeName.replace("[", "");
            			nodeName = nodeName.replace("]", "");
            		}
            	}else if("Clustered Index Seek:Clustered Index Seek".equalsIgnoreCase(tagName)
            				|| "Index Seek:Index Seek".equalsIgnoreCase(tagName)){
            		/* Key Lookup */
            		if("Clustered Index Seek:Clustered Index Seek".equalsIgnoreCase(tagName) && "Lookup".equalsIgnoreCase(nodeAttr.getKey()) && "true".equalsIgnoreCase(nodeAttr.getValue())){
            			newTagName = "Key Lookup:Clustered";
            		}
            		
            		if("Table".equalsIgnoreCase(nodeAttr.getKey())){
            			nodeName = nodeAttr.getValue() +"<br/>";
            			nodeName = nodeName.replace("[", "");
            			nodeName = nodeName.replace("]", "");
            		}
            	}else if("Delete:Table Delete".equalsIgnoreCase(tagName)
        					|| "Update:Table Update".equalsIgnoreCase(tagName)){
            		if("Table".equalsIgnoreCase(nodeAttr.getKey())){
            			nodeName = nodeAttr.getValue() +"<br/>";
            			nodeName = nodeName.replace("[", "");
            			nodeName = nodeName.replace("]", "");
            		}
            	}
            	
                if (ExecutionPlanEnum.EstimateOperatorCost.toString().equals(nodeAttr.getKey())) {
                    EstimateOperatorCost = Math.round((Double.valueOf(nodeAttr.getValue()) / Double.valueOf(getStatementSubTreeCost())) * 100);
                }
                
                if("NoJoinPredicate".equalsIgnoreCase(nodeAttr.getKey())){
                	nodeName = "<font color=\\\"red\\\">Warning</font><br/>";
                	strLabel = "Warning:<br/>";
                	strNotifyIcon = SQLServerOperationImageEnum.WarningIcon.toString();
                }
                
                if("Parallel".equalsIgnoreCase(nodeAttr.getKey()) && "true".equalsIgnoreCase(nodeAttr.getValue())){
                	strNotifyIcon = SQLServerOperationImageEnum.ParallelIcon.toString();
                }
                
                nodeAttribute.append(nodeAttr.getKey()).append(" = ").append("\\\"<b>").append(strLabel).append(nodeAttr.getKey()).append("</b>: ").append(nodeAttr.getValue()).append("\\\" ");
            }
        }

        /* Check for newTagname(case: Clustered Index Seek -> Key Lookup) */
        tagName = newTagName != null && !newTagName.isEmpty() ? newTagName : tagName;
        
        if ("Table Scan:Table Scan".equalsIgnoreCase(tagName)) {
        	nodeName += "Table Scan"; 
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.TableScan.getImagePosition(), SQLServerOperationImageEnum.TableScan.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Sort".equalsIgnoreCase(tagName)) {
        	nodeName += "Sort";
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.Sort.getImagePosition(), SQLServerOperationImageEnum.Sort.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Sort:Sort".equalsIgnoreCase(tagName)) {
        	nodeName += "Sort";
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.Sort.getImagePosition(), SQLServerOperationImageEnum.Sort.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Distinct Sort:Sort".equalsIgnoreCase(tagName)) {
        	nodeName += "Sort<br/>(Distinct Sort)";
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.Sort.getImagePosition(), SQLServerOperationImageEnum.Sort.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Left Outer Join:Nested Loops".equalsIgnoreCase(tagName)) {
        	nodeName += "Nested Loops<br/>(Left Outer Join)";
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.NestedLoops.getImagePosition(), SQLServerOperationImageEnum.NestedLoops.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Right Outer Join:Nested Loops".equalsIgnoreCase(tagName)) {
        	nodeName += "Nested Loops<br/>(Right Outer Join)";
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.NestedLoops.getImagePosition(), SQLServerOperationImageEnum.NestedLoops.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Inner Join:Nested Loops".equalsIgnoreCase(tagName)) {
        	nodeName += "Nested Loops<br/>(Inner Join)";
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.NestedLoops.getImagePosition(), SQLServerOperationImageEnum.NestedLoops.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Left Anti Semi Join:Nested Loops".equalsIgnoreCase(tagName)) {
        	nodeName += "Nested Loops<br/>(Left Anti Semi Join)";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.NestedLoops.getImagePosition(), SQLServerOperationImageEnum.NestedLoops.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Left Outer Join:Merge Join".equalsIgnoreCase(tagName)) {
        	nodeName += "Merge<br/>(Left Outer Join)";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.Merge.getImagePosition(), SQLServerOperationImageEnum.Merge.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Right Outer Join:Merge Join".equalsIgnoreCase(tagName)) {
        	nodeName += "Merge<br/>(Right Outer Join)";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.Merge.getImagePosition(), SQLServerOperationImageEnum.Merge.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Left Outer Join:Hash Match".equalsIgnoreCase(tagName)) {
        	nodeName += "Hash Match<br/>(Left Outer Join)";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.HashMatch.getImagePosition(), SQLServerOperationImageEnum.HashMatch.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Right Outer Join:Hash Match".equalsIgnoreCase(tagName)) {
        	nodeName += "Hash Match<br/>(Right Outer Join)";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.HashMatch.getImagePosition(), SQLServerOperationImageEnum.HashMatch.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Clustered Index Scan:Clustered Index Scan".equalsIgnoreCase(tagName)) {
        	nodeName += "Clustered Index Scan";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.ClusteredIndexScan.getImagePosition(), SQLServerOperationImageEnum.ClusteredIndexScan.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Clustered Index Seek:Clustered Index Seek".equalsIgnoreCase(tagName)) {
        	nodeName += "Clustered Index Seek";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.ClusteredIndexSeek.getImagePosition(), SQLServerOperationImageEnum.ClusteredIndexSeek.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Index Seek:Index Seek".equalsIgnoreCase(tagName)) {
        	nodeName += "Index Seek";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.IndexSeek.getImagePosition(), SQLServerOperationImageEnum.IndexSeek.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Compute Scalar:Compute Scalar".equalsIgnoreCase(tagName)) {
        	nodeName += "Compute Scalar";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.ComputeScalar.getImagePosition(), SQLServerOperationImageEnum.ComputeScalar.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Lazy Spool:Table Spool".equalsIgnoreCase(tagName)) {
        	nodeName += "Table Spool<br/>(Lazy Spool)";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.TableSpool.getImagePosition(), SQLServerOperationImageEnum.TableSpool.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Distribute Streams:Parallelism".equalsIgnoreCase(tagName)) {
        	nodeName += "Parallelism<br/>(Distribute Streams)";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.DistributeStreams.getImagePosition(), SQLServerOperationImageEnum.DistributeStreams.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Repartition Streams:Parallelism".equalsIgnoreCase(tagName)) {
        	nodeName += "Parallelism<br/>(Repartition Streams)";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.RepartitionStreams.getImagePosition(), SQLServerOperationImageEnum.RepartitionStreams.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Gather Streams:Parallelism".equalsIgnoreCase(tagName)) {
        	nodeName += "Parallelism<br/>(Gather Streams)";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.GatherStreams.getImagePosition(), SQLServerOperationImageEnum.GatherStreams.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Sequence:Sequence".equalsIgnoreCase(tagName)) {
        	nodeName += "Sequence";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.Sequence.getImagePosition(), SQLServerOperationImageEnum.Sequence.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Table-valued function:Table-valued function".equalsIgnoreCase(tagName)) {
        	nodeName += "Table Valued Function";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.TableValuedFunction.getImagePosition(), SQLServerOperationImageEnum.TableValuedFunction.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Insert:Table Insert".equalsIgnoreCase(tagName)) {
        	nodeName += "Table Insert";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.TableInsert.getImagePosition(), SQLServerOperationImageEnum.TableInsert.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Top:Top".equalsIgnoreCase(tagName)) {
        	nodeName += "Top";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.Top.getImagePosition(), SQLServerOperationImageEnum.Top.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Filter:Filter".equalsIgnoreCase(tagName)) {
        	nodeName += "Filter";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.Filter.getImagePosition(), SQLServerOperationImageEnum.Filter.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Compute Scalar:Sequence Project".equalsIgnoreCase(tagName)) {
        	nodeName += "Sequence Project<br/>(Compute Scalar)";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.SequenceProject.getImagePosition(), SQLServerOperationImageEnum.SequenceProject.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Segment:Segment".equalsIgnoreCase(tagName)) {
        	nodeName += "Segment";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.Segment.getImagePosition(), SQLServerOperationImageEnum.Segment.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("RID Lookup:RID Lookup".equalsIgnoreCase(tagName)) {
        	nodeName += "RID Lookup";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.RIDLookup.getImagePosition(), SQLServerOperationImageEnum.RIDLookup.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Concatenation:Concatenation".equalsIgnoreCase(tagName)) {
        	nodeName += "Concatenation";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.Concatenation.getImagePosition(), SQLServerOperationImageEnum.Concatenation.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Delete:Table Delete".equalsIgnoreCase(tagName)) {
        	nodeName += "Table Delete";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.TableDelete.getImagePosition(), SQLServerOperationImageEnum.TableDelete.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Update:Table Update".equalsIgnoreCase(tagName)) {
        	nodeName += "Table Update";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.TableUpdate.getImagePosition(), SQLServerOperationImageEnum.TableUpdate.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        } else if ("Aggregate:Stream Aggregate".equalsIgnoreCase(tagName)) {
        	nodeName += "Stream Aggregate<br/>(Aggregate)";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.StreamAggregate.getImagePosition(), SQLServerOperationImageEnum.StreamAggregate.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        }else if("Inner Join:Hash Match".equalsIgnoreCase(tagName)){
        	nodeName += "Hash Match<br/>(Inner Join)";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.HashMatch.getImagePosition(), SQLServerOperationImageEnum.HashMatch.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        }else if("Key Lookup:Clustered".equalsIgnoreCase(tagName)){
        	nodeName += "Key Lookup (Clustered)";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.KeyLookup.getImagePosition(), SQLServerOperationImageEnum.KeyLookup.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        }else if("Index Scan:Index Scan".equalsIgnoreCase(tagName)){
        	nodeName += "Index Scan (NonClustered)";
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.NonclusteredIndexScan.getImagePosition(), SQLServerOperationImageEnum.NonclusteredIndexScan.getTitle(), nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);        	
        }else{
        	nodeName += tagName; 
        	returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.IconNotFound.getImagePosition(), tagName, nodeAttribute.toString(), nodeName, EstimateOperatorCost, parentID, strNotifyIcon);
        }
        return returnNode;
    }
    
    /**
     * Create HTML content for node image.
     * @author vicky.thakor
     * @param imageSrc
     * @param imageTitle
     * @param imageAttribute
     * @param nodeName
     * @param EstimateOperatorCost
     * @return {@link String}
     */
    private String getHTMLNodeImage(String imageSrc, String imageTitle, String imageAttribute, String nodeName, double EstimateOperatorCost, int parentID, String strNotifyIcon) {
    	String nodeID = parentID == -2 ? "rootnode" : "parentTable"+parentID;
    	
    	String addNotificationIcon = "";
    	
    	if(SQLServerOperationImageEnum.WarningIcon.toString().equalsIgnoreCase(strNotifyIcon)){
    		addNotificationIcon = "<div style=\\\"position:absolute;bottom:0px;right:0px;width:16px;height:16px;background: url('combine_icon_hibernate_assist.png') no-repeat "+SQLServerOperationImageEnum.WarningIcon.getImagePosition()+"\\\"></div>";
    	}else if(SQLServerOperationImageEnum.ParallelIcon.toString().equalsIgnoreCase(strNotifyIcon)){
    		addNotificationIcon = "<div style=\\\"position:absolute;bottom:0px;right:0px;width:16px;height:16px;background: url('combine_icon_hibernate_assist.png') no-repeat "+SQLServerOperationImageEnum.ParallelIcon.getImagePosition()+"\\\"></div>";
    	}
    	
        return "<table class=\\\"nodeTable\\\" id=\\\""+nodeID+"\\\">"
                + "<tr>"
                + "<td style=\\\"width:95px\\\">"
                + "<div class=\\\"nodeImage\\\" style=\\\"position:relative;margin:0px auto;width:32px;height:32px;background: url('combine_icon_hibernate_assist.png') no-repeat "+imageSrc+"\\\" title=\\\"" + imageTitle + "\\\" " + imageAttribute + ">"
                + addNotificationIcon
                + "</div>"
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td style=\\\"width:95px;white-space: pre-wrap;\\\">" + nodeName + "<br/>Cost:" + EstimateOperatorCost + " %"
                + "</td>"
                + "</tr>"
                + "</table>";
    }
    
    /**
     * Get Execution Plan Statistics.
     * @author vicky.thakor
     * @param executionCount
     * @param lastExecutionTime
     * @param lastElapsedTime
     * @param lastLogicalReads
     * @param lastLogicalWrites
     * @return {@link String}
     */
    public String getExecutionPlanStatistics(int executionCount, String lastExecutionTime, int lastElapsedTime, int lastLogicalReads, int lastLogicalWrites) {
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append("<div class=\"statistics_data\">");
        stringBuilder.append("<div class=\"operation_header\"><h3>Execution Plan Statistics</h3>");
        stringBuilder.append("Data shows total hit of same query on database by different or same user. It also shows time and type of operation performed by query.");
        stringBuilder.append("</div>");
        stringBuilder.append("<div class=\"whiteFadedbox\">");
        stringBuilder.append("<table class=\"stylistTable\" style=\"width:100%\">");
        stringBuilder.append("<thead>");
        stringBuilder.append(CommonUtil.prepareTableRow("Execution Count", "Last Execution Time", "Last Elapsed Time", "Last Logical Reads", "Last Logical Writes"));
        stringBuilder.append("</thead>");
        stringBuilder.append("<tbody>");
        stringBuilder.append(CommonUtil.prepareTableRow(String.valueOf(executionCount), String.valueOf(lastExecutionTime), String.valueOf(lastElapsedTime),
        																						String.valueOf(lastLogicalReads), String.valueOf(lastLogicalWrites)));
        stringBuilder.append("</tbody>");
        stringBuilder.append("</table>");
        stringBuilder.append("</div>");
        stringBuilder.append("</div>");
        return stringBuilder.toString();
    }
    
    /**
     * Get MSSQL proposed indices.
     * <br/><br/>
     * @author vicky.thakor
     * @param nodeListMissingIndex
     * @param stringBuilder
     */
    private void ScanMissingIndexes(NodeList nodeListMissingIndex, StringBuilder stringBuilder) {
        if (nodeListMissingIndex != null) {
            int nodeLength = nodeListMissingIndex.getLength();
            if (nodeLength > 0) {
                for (int i = 0; i < nodeLength; i++) {
                	String databaseName = "", schemaName = "", tableName = "", baseColumn = "", includeColumn = "", impact = "";
                    if (nodeListMissingIndex.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Element MissingIndexGroupNode = (Element) nodeListMissingIndex.item(i);
                        if (MissingIndexGroupNode != null && "MissingIndexGroup".equalsIgnoreCase(MissingIndexGroupNode.getNodeName())) {
                            if (MissingIndexGroupNode.hasAttribute("Impact")) {
                                impact = MissingIndexGroupNode.getAttribute("Impact");
                            }
                            
                            if (MissingIndexGroupNode.hasChildNodes()) {
                            	Element MissingIndexNode = null;
                            	for(int j = 0; j < MissingIndexGroupNode.getChildNodes().getLength(); j++){
                            		if(MissingIndexGroupNode.getChildNodes().item(j).getNodeType() == Node.ELEMENT_NODE){
                            			MissingIndexNode = (Element) MissingIndexGroupNode.getChildNodes().item(j); 
                            		}
                            	}
                            	
                            	if(MissingIndexNode != null && "MissingIndex".equalsIgnoreCase(MissingIndexNode.getNodeName())){
                                     NamedNodeMap namedNodeMap = MissingIndexNode.getAttributes();
                                     if (namedNodeMap != null) {
                                         if (namedNodeMap.getNamedItem("Database") != null) {
                                             databaseName = namedNodeMap.getNamedItem("Database").getNodeValue();
                                         }
                                         if (namedNodeMap.getNamedItem("Schema") != null) {
                                             schemaName = namedNodeMap.getNamedItem("Schema").getNodeValue();
                                         }
                                         if (namedNodeMap.getNamedItem("Table") != null) {
                                             tableName = namedNodeMap.getNamedItem("Table").getNodeValue();
                                         }

                                         NodeList nodeListChild = MissingIndexNode.getElementsByTagName("ColumnGroup");
                                         for (int j = 0; j < nodeListChild.getLength(); j++) {
                                             Element elementColumnGroup = (Element) nodeListChild.item(j);

                                             if (elementColumnGroup.hasAttribute("Usage") && "EQUALITY".equals(elementColumnGroup.getAttribute("Usage"))) {
                                                 NodeList nodeListColumn = elementColumnGroup.getElementsByTagName("Column");
                                                 for (int k = 0; k < nodeListColumn.getLength(); k++) {
                                                     if (k != 0) {
                                                         baseColumn += ",";
                                                     }
                                                     baseColumn += ((Element) nodeListColumn.item(k)).getAttribute("Name");
                                                 }
                                             } else if (elementColumnGroup.hasAttribute("Usage") && "INCLUDE".equals(elementColumnGroup.getAttribute("Usage"))) {
                                                 NodeList nodeListColumn = elementColumnGroup.getElementsByTagName("Column");
                                                 for (int k = 0; k < nodeListColumn.getLength(); k++) {
                                                     if (k != 0) {
                                                         includeColumn += ",";
                                                     }
                                                     includeColumn += ((Element) nodeListColumn.item(k)).getAttribute("Name");
                                                 }
                                             }
                                         }
                                     }
                            	}
                            }
                        }
                    }

                    if (databaseName != null && !databaseName.isEmpty()
                            && schemaName != null && !schemaName.isEmpty()
                            && tableName != null && !tableName.isEmpty()
                            && baseColumn != null && !baseColumn.isEmpty()) {
                        stringBuilder.append("<b>Missing Index Script");
                        if (impact != null && !impact.isEmpty()) {
                            stringBuilder.append(" (Impact : ").append(impact).append(")");
                        }
                        stringBuilder.append(":</b>");
                        stringBuilder.append("CREATE NONCLUSTERED INDEX IndxNc_").append(tableName.replace("[", "").replace("]", ""));
                        String[] baseColumns = baseColumn.split(",");
                        if (baseColumns.length > 1) {
                            stringBuilder.append("_MultiCol").append((i+1));
                        } else {
                            baseColumn = baseColumn.replace("[", "");
                            baseColumn = baseColumn.replace("]", "");
                            stringBuilder.append("_");
                            stringBuilder.append(baseColumn);
                        }
                        stringBuilder.append(" ON ").append(databaseName).append(".").append(schemaName).append(".").append(tableName);
                        stringBuilder.append(" (").append(baseColumn).append(")");

                        if (includeColumn != null && !includeColumn.isEmpty()) {
                            stringBuilder.append(" INCLUDE (").append(includeColumn).append(")");
                        }
                        stringBuilder.append(";<br/>");
                    }
                }
            }
        }
    }
    
	public static void main(String[] args) {
		
	}

	public String getQueryType() {
		return QueryType;
	}

	public void setQueryType(String queryType) {
		QueryType = queryType;
	}

	public String getQueryHash() {
		return QueryHash;
	}

	public void setQueryHash(String queryHash) {
		QueryHash = queryHash;
	}

	public String getStatementSubTreeCost() {
		return StatementSubTreeCost;
	}

	public void setStatementSubTreeCost(String statementSubTreeCost) {
		StatementSubTreeCost = statementSubTreeCost;
	}
}
