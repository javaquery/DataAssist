package com.hibernateassist.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.hibernateassist.bean.MSSQLQueryDetails;
import com.hibernateassist.common.CommonUtil;

/**
 * @author vicky.thakor
 */
public class MSSQLAnalyser extends AbstractDAO{
	
	private static final Logger logger = Logger.getLogger(MSSQLAnalyser.class.getName());
	private String QueryType;
	private String QueryHash;
    private String StatementSubTreeCost;
    private Set<String> setIconFilename;
	
	public void generateQueryReport(String query, String reportFolderPath) throws ClassNotFoundException, SQLException, FileNotFoundException, UnsupportedEncodingException{
		List<MSSQLQueryDetails> listMssqlQueryDetails = getExecutionPlan(query);
		if(listMssqlQueryDetails != null && !listMssqlQueryDetails.isEmpty()){
			for (MSSQLQueryDetails mssqlQueryDetails : listMssqlQueryDetails) {
				if(mssqlQueryDetails.getQueryPlan() != null && !mssqlQueryDetails.getQueryPlan().isEmpty()){
					
					setIconFilename = new HashSet<String>();
					StringBuilder HTMLReport = new StringBuilder("");
					HTMLReport.append(CommonUtil.getHTMLReportHeader());
					HTMLReport.append(getExecutionPlanStatistics(mssqlQueryDetails.getExecutionCount(), mssqlQueryDetails.getLastExecutionTime(), mssqlQueryDetails.getLastElapsedTime(), mssqlQueryDetails.getLastLogicalReads(), mssqlQueryDetails.getLastLogicalWrites()));
					HTMLReport.append(parseXML(getXMLDocument(mssqlQueryDetails.getQueryPlan(), null)));
					HTMLReport.append(CommonUtil.getHTMLReportFooter());
					
					File reportFolder = new File(reportFolderPath);
					if(reportFolder.exists()){
						File createHTMLReport = new File(reportFolderPath + File.separatorChar + "HibernateAssist_" + getQueryHash() + ".html");
                        if (createHTMLReport.exists()) {
                            createHTMLReport.delete();
                        }
                        try {
                            PrintWriter writer = new PrintWriter(reportFolderPath + File.separatorChar + "HibernateAssist_" + getQueryHash() + ".html", "UTF-8");
                            writer.write(HTMLReport.toString());
                            writer.close();
                            String informationMessage = "Hibernate Assist Report: \"" + createHTMLReport.getAbsolutePath() + "\"";
                            logger.info(informationMessage);
                        } catch (FileNotFoundException ex) {
                        	Logger.getLogger(MSSQLAnalyser.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (UnsupportedEncodingException ex) {
                        	Logger.getLogger(MSSQLAnalyser.class.getName()).log(Level.SEVERE, null, ex);
                        }
					}else{
						logger.info("Can't find your custom report folder");
                        reportFolderPath = System.getProperty("user.home");
                        File createHTMLReport = new File(reportFolderPath + File.separatorChar + "MSSQL_HibernateAssist_" + getQueryHash() + ".html");
                        if (createHTMLReport.exists()) {
                            createHTMLReport.delete();
                        }
                        try {
                            PrintWriter writer = new PrintWriter(reportFolderPath + File.separatorChar + "MSSQL_HibernateAssist_" + getQueryHash() + ".html", "UTF-8");
                            writer.write(HTMLReport.toString());
                            writer.close();
                            String informationMessage = "Hibernate Assist Report: \"" + createHTMLReport.getAbsolutePath() + "\"";
                            logger.info(informationMessage);
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(MSSQLAnalyser.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (UnsupportedEncodingException ex) {
                            Logger.getLogger(MSSQLAnalyser.class.getName()).log(Level.SEVERE, null, ex);
                        }
					}
				}
			}
		}else{
			logger.info("Execution plan not found on database. Please execute same Criteria multiple times to generate execution plan.");
		}
	}
	
	/**
	 * Get execution plans from database
	 * <br/><br/>
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
	
	/**
	 * Get XML Document instance from String or File
	 * <br/><br/>
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
	 * <br/><br/>
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
            	 	stringBuilderHTMLReport.append("<div>");
                    stringBuilderHTMLReport.append("<h2>Execution Plan</h2>");
                    stringBuilderHTMLReport.append("<div id=\"parent-1\" style=\"width:100%;max-height:500px;overflow:scroll;border:1px double;white-space: nowrap;position:relative\">");
                    stringBuilderHTMLReport.append("<div id=\"nodeDetails\" style=\"display:none;position:absolute;top:0px;right:0px;width:250px;background-color:rgb(255, 255, 161)\"></div>");
                    stringBuilderHTMLReport.append(getRootNodeImage());
            		stringBuilderHTMLReport.append("</div><script>");
                    recursiveXMLParse(listRootNode, -1, stringBuilderHTMLReport);
                    stringBuilderHTMLReport.append("</script>");
	            }
	            
	            /*if (listMissingIndexNode != null) {
                    stringBuilderHTMLReport.append("<div>");
                    stringBuilderHTMLReport.append("<h2>Missing Index Details</h2>");
                    ScanMissingIndexes(listMissingIndexNode, stringBuilderHTMLReport);
                    stringBuilderHTMLReport.append("</div>");
                }*/
	            
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
	 * <br/><br/>
	 * @author vicky.thakor
	 * @param listQueryStatement
	 * @param stringBuilder
	 */
	private void getQueryProperties(NodeList listQueryStatement, StringBuilder stringBuilder) {
        if (listQueryStatement != null) {
            if (listQueryStatement.item(0) != null) {
                if (listQueryStatement.item(0).getNodeType() == Node.ELEMENT_NODE) {
                    Element elementQueryStatement = (Element) listQueryStatement.item(0);
                    NamedNodeMap queryAttributes = elementQueryStatement.getAttributes();

                    if (queryAttributes.getNamedItem("StatementText") != null) {
                        stringBuilder.append("<div><h2 style=\"margin-top:0px\">Query Statistics</h2>");
                        stringBuilder.append("<div style=\"border: 1px dashed;padding:5px\">");
                        stringBuilder.append("<table class=\"stylistTable\" style=\"width:100%\"><thead><tr>");
                        stringBuilder.append("<td>Query Type</td>");
                        stringBuilder.append("<td>Estimated Rows</td>");
                        stringBuilder.append("<td>Sub Tree Cost</td>");
                        stringBuilder.append("<td>Optimization Level</td>");
                        stringBuilder.append("<td>Early Abort Reason</td>");
                        stringBuilder.append("<td>QueryHash</td>");
                        stringBuilder.append("<td>QueryPlanHash</td>");
                        stringBuilder.append("</tr></thead>");
                        stringBuilder.append("<tbody><tr>");
                        if (queryAttributes.getNamedItem("StatementType") != null) {
                            stringBuilder.append("<td>");
                            stringBuilder.append(queryAttributes.getNamedItem("StatementType").getNodeValue());
                            stringBuilder.append("</td>");
                            setQueryType(queryAttributes.getNamedItem("StatementType").getNodeValue());
                        } else {
                            stringBuilder.append("<td>NA</td>");
                        }

                        if (queryAttributes.getNamedItem("StatementEstRows") != null) {
                            stringBuilder.append("<td>");
                            stringBuilder.append(queryAttributes.getNamedItem("StatementEstRows").getNodeValue());
                            stringBuilder.append("</td>");
                        } else {
                            stringBuilder.append("<td>NA</td>");
                        }
                        if (queryAttributes.getNamedItem("StatementSubTreeCost") != null) {
                            stringBuilder.append("<td>");
                            stringBuilder.append(queryAttributes.getNamedItem("StatementSubTreeCost").getNodeValue());
                            stringBuilder.append("</td>");
                            setStatementSubTreeCost(queryAttributes.getNamedItem("StatementSubTreeCost").getNodeValue());
                        } else {
                            stringBuilder.append("<td>NA</td>");
                        }

                        if (queryAttributes.getNamedItem("StatementOptmLevel") != null) {
                            stringBuilder.append("<td>");
                            stringBuilder.append(queryAttributes.getNamedItem("StatementOptmLevel").getNodeValue());
                            stringBuilder.append("</td>");
                        } else {
                            stringBuilder.append("<td>NA</td>");
                        }

                        if (queryAttributes.getNamedItem("StatementOptmEarlyAbortReason") != null) {
                            stringBuilder.append("<td>");
                            stringBuilder.append(queryAttributes.getNamedItem("StatementOptmEarlyAbortReason").getNodeValue());
                            stringBuilder.append("</td>");
                        } else {
                            stringBuilder.append("<td>NA</td>");
                        }

                        if (queryAttributes.getNamedItem("QueryHash") != null) {
                            stringBuilder.append("<td>");
                            stringBuilder.append(queryAttributes.getNamedItem("QueryHash").getNodeValue());
                            stringBuilder.append("</td>");
                            setQueryHash(queryAttributes.getNamedItem("QueryHash").getNodeValue());
                        } else {
                            stringBuilder.append("<td>NA</td>");
                        }

                        if (queryAttributes.getNamedItem("QueryPlanHash") != null) {
                            stringBuilder.append("<td>");
                            stringBuilder.append(queryAttributes.getNamedItem("QueryPlanHash").getNodeValue());
                            stringBuilder.append("</td>");
                        } else {
                            stringBuilder.append("<td>NA</td>");
                        }

                        stringBuilder.append("</tr></tbody></table><br/>");
                        stringBuilder.append("<div style=\"width:100%;overflow:scroll;height:100px\">");
                        stringBuilder.append(queryAttributes.getNamedItem("StatementText").getNodeValue());
                        stringBuilder.append("</div>");
                        stringBuilder.append("</div>");
                        stringBuilder.append("</div>");
                    }
                }
            }
        }
    }
	
	/**
	 * Get root node operation image (i.e: SELECT, UPDATE, DELETE)
	 * <br/><br/>
	 * @author vicky.thakor
	 * @return
	 */
	private String getRootNodeImage(){
		String HTMLContent = "<div style=\"display: inline-block;vertical-align:top;text-align:center\">";
		if("SELECT".equalsIgnoreCase(getQueryType())){
			HTMLContent += getHTMLNodeImage(SQLServerOperationImageEnum.Select.getImagePosition(), SQLServerOperationImageEnum.Select.getTitle(), "", "Select", 0, -2).replace("\\", "");
		}else if("UPDATE".equalsIgnoreCase(getQueryType())){
			HTMLContent += getHTMLNodeImage(SQLServerOperationImageEnum.Update.getImagePosition(), SQLServerOperationImageEnum.Update.getTitle(), "", "Update", 0, -2).replace("\\", "");
		}
		HTMLContent += "</div>";
		return HTMLContent;
	}
	
	/**
	 * Parse all XML node by recursive method call
	 * <br/><br/>
	 * @author vicky.thakor 
	 * @param nodeList
	 * @param parentNode
	 * @param stringBuilder
	 */
    private void recursiveXMLParse(NodeList nodeList, int parentNode, StringBuilder stringBuilder) {
        int nodeLength = nodeList.getLength();
        for (int i = 0; i < nodeLength; i++) {
            if (nodeList.item(i).hasChildNodes()) {
                Element element = (Element) nodeList.item(i);
                if ("RelOp".equals(element.getTagName())) {
                    parentNode = generateChildDetails(nodeList.item(i), nodeList, parentNode, stringBuilder);
                }
                recursiveXMLParse(nodeList.item(i).getChildNodes(), parentNode, stringBuilder);
            }
        }
    }
	
    /**
     * Get child node details
     * <br/><br/>
     * @author vicky.thakor
     * @param parentNode
     * @param childNodes
     * @param parentNodeID
     * @param sb
     * @return
     */
    private int generateChildDetails(Node parentNode, NodeList childNodes, int parentNodeID, StringBuilder sb) {
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
                                        // mapNodeProperty.putAll(mapOperationProperty);
                                    }
                                }

                                if (!operationType.isEmpty()) {
                                    sb.append("$(\"#parent").append(oldParentID).append("\")");
                                    sb.append(".append(\"<div id=\\\"parent").append(childNodeID).append("\\\"");
                                    sb.append(" style=\\\"padding-left:95px;padding-bottom:20px;display: inline-block;\\\">");
                                    sb.append(getElementImage(operationType, mapNodeProperty, childNodeID));
                                    sb.append("</div><br/>\");\n");
                                    
                                    /* Set Source and Target node for jsPlumb */
                                    String sourceNode = "parentTable"+childNodeID;
                                    String targetNode = oldParentID == -1 ? "rootnode" : "parentTable"+oldParentID; 
                                    sb.append(CommonUtil.getjsPlumbScript(sourceNode, targetNode));
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
     * <br/><br/>
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
     * <br/><br/> 
     * @author vicky.thakor
     * @param operationType
     * @param elementRelOp
     * @return
     */
    private Map<String, String> getOperationProperty(String operationType, Element elementRelOp) {
        Map<String, String> mapOperationProperty = new LinkedHashMap<String, String>();
        if ("Sort".equalsIgnoreCase(operationType)) {
            if (elementRelOp.getElementsByTagName("Sort") != null) {
                Node sortNode = elementRelOp.getElementsByTagName("Sort").item(0);
                if (sortNode.getChildNodes().item(1) != null && "OrderBy".equalsIgnoreCase(sortNode.getChildNodes().item(1).getNodeName())) {
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
                            if (namedNodeMap != null && namedNodeMap.getLength() > 0) {
                                int attributeLength = namedNodeMap.getLength();
                                for (int i = 0; i < attributeLength; i++) {
                                    Node getAttribute = namedNodeMap.item(i);
                                    mapOperationProperty.put(getAttribute.getNodeName(), getAttribute.getNodeValue());
                                }
                            }
                        }
                    }
                }

            }
        }
        return mapOperationProperty;
    }
    
    /**
     * Get HTML content for node image.
     * <br/><br/>
     * @param tagName
     * @param mapNodeProperty
     * @return
     */
    private String getElementImage(String tagName, Map<String, String> mapNodeProperty, int parentID) {
        String returnNode = tagName;
        double EstimateOperatorCost = 0.0;

        StringBuilder nodeAttribute = new StringBuilder("");
        if (mapNodeProperty != null && !mapNodeProperty.isEmpty()) {
            for (Map.Entry<String, String> nodeAttr : mapNodeProperty.entrySet()) {
                nodeAttribute.append(nodeAttr.getKey()).append(" = ").append("\\\"<b>").append(nodeAttr.getKey()).append("</b>: ").append(nodeAttr.getValue()).append("\\\" ");

                if (ExecutionPlanEnum.EstimateOperatorCost.toString().equals(nodeAttr.getKey())) {
                    EstimateOperatorCost = Math.round((Double.valueOf(nodeAttr.getValue()) / Double.valueOf(getStatementSubTreeCost())) * 100);
                }
            }
        }

        if ("Table Scan:Table Scan".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.TableScan.getImagePosition(), SQLServerOperationImageEnum.TableScan.getTitle(), nodeAttribute.toString(), "Table Scan", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.TableScan.toString());
        } else if ("Sort".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.Sort.getImagePosition(), SQLServerOperationImageEnum.Sort.getTitle(), nodeAttribute.toString(), "Sort", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.Sort.toString());
        } else if ("Sort:Sort".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.Sort.getImagePosition(), SQLServerOperationImageEnum.Sort.getTitle(), nodeAttribute.toString(), "Sort", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.Sort.toString());
        } else if ("Distinct Sort:Sort".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.Sort.getImagePosition(), SQLServerOperationImageEnum.Sort.getTitle(), nodeAttribute.toString(), "Sort<br/>(Distinct Sort)", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.Sort.toString());
        } else if ("Left Outer Join:Nested Loops".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.NestedLoops.getImagePosition(), SQLServerOperationImageEnum.NestedLoops.getTitle(), nodeAttribute.toString(), "Nested Loops<br/>(Left Outer Join)", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.NestedLoops.toString());
        } else if ("Right Outer Join:Nested Loops".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.NestedLoops.getImagePosition(), SQLServerOperationImageEnum.NestedLoops.getTitle(), nodeAttribute.toString(), "Nested Loops<br/>(Right Outer Join)", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.NestedLoops.toString());
        } else if ("Inner Join:Nested Loops".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.NestedLoops.getImagePosition(), SQLServerOperationImageEnum.NestedLoops.getTitle(), nodeAttribute.toString(), "Nested Loops<br/>(Inner Join)", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.NestedLoops.toString());
        } else if ("Left Anti Semi Join:Nested Loops".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.NestedLoops.getImagePosition(), SQLServerOperationImageEnum.NestedLoops.getTitle(), nodeAttribute.toString(), "Nested Loops<br/>(Left Anti Semi Join)", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.NestedLoops.toString());
        } else if ("Left Outer Join:Merge Join".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.Merge.getImagePosition(), SQLServerOperationImageEnum.Merge.getTitle(), nodeAttribute.toString(), "Merge<br/>(Left Outer Join)", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.Merge.toString());
        } else if ("Right Outer Join:Merge Join".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.Merge.getImagePosition(), SQLServerOperationImageEnum.Merge.getTitle(), nodeAttribute.toString(), "Merge<br/>(Right Outer Join)", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.Merge.toString());
        } else if ("Left Outer Join:Hash Match".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.HashMatch.getImagePosition(), SQLServerOperationImageEnum.HashMatch.getTitle(), nodeAttribute.toString(), "Hash Match<br/>(Left Outer Join)", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.HashMatch.toString());
        } else if ("Right Outer Join:Hash Match".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.HashMatch.getImagePosition(), SQLServerOperationImageEnum.HashMatch.getTitle(), nodeAttribute.toString(), "Hash Match<br/>(Right Outer Join)", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.HashMatch.toString());
        } else if ("Clustered Index Scan:Clustered Index Scan".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.ClusteredIndexScan.getImagePosition(), SQLServerOperationImageEnum.ClusteredIndexScan.getTitle(), nodeAttribute.toString(), "Clustered Index Scan", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.ClusteredIndexScan.toString());
        } else if ("Clustered Index Seek:Clustered Index Seek".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.ClusteredIndexSeek.getImagePosition(), SQLServerOperationImageEnum.ClusteredIndexSeek.getTitle(), nodeAttribute.toString(), "Clustered Index Seek", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.ClusteredIndexSeek.toString());
        } else if ("Index Seek:Index Seek".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.IndexSeek.getImagePosition(), SQLServerOperationImageEnum.IndexSeek.getTitle(), nodeAttribute.toString(), "Index Seek", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.IndexSeek.toString());
        } else if ("Compute Scalar:Compute Scalar".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.ComputeScalar.getImagePosition(), SQLServerOperationImageEnum.ComputeScalar.getTitle(), nodeAttribute.toString(), "Compute Scalar", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.ComputeScalar.toString());
        } else if ("Lazy Spool:Table Spool".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.TableSpool.getImagePosition(), SQLServerOperationImageEnum.TableSpool.getTitle(), nodeAttribute.toString(), "Table Spool<br/>(Lazy Spool)", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.TableSpool.toString());
        } else if ("Distribute Streams:Parallelism".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.DistributeStreams.getImagePosition(), SQLServerOperationImageEnum.DistributeStreams.getTitle(), nodeAttribute.toString(), "Parallelism<br/>(Distribute Streams)", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.DistributeStreams.toString());
        } else if ("Repartition Streams:Parallelism".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.RepartitionStreams.getImagePosition(), SQLServerOperationImageEnum.RepartitionStreams.getTitle(), nodeAttribute.toString(), "Parallelism<br/>(Repartition Streams)", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.RepartitionStreams.toString());
        } else if ("Gather Streams:Parallelism".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.GatherStreams.getImagePosition(), SQLServerOperationImageEnum.GatherStreams.getTitle(), nodeAttribute.toString(), "Parallelism<br/>(Gather Streams)", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.GatherStreams.toString());
        } else if ("Sequence:Sequence".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.Sequence.getImagePosition(), SQLServerOperationImageEnum.Sequence.getTitle(), nodeAttribute.toString(), "Sequence", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.Sequence.toString());
        } else if ("Table-valued function:Table-valued function".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.TableValuedFunction.getImagePosition(), SQLServerOperationImageEnum.TableValuedFunction.getTitle(), nodeAttribute.toString(), "Table Valued Function", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.TableValuedFunction.toString());
        } else if ("Insert:Table Insert".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.TableInsert.getImagePosition(), SQLServerOperationImageEnum.TableInsert.getTitle(), nodeAttribute.toString(), "Table Insert", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.TableInsert.toString());
        } else if ("Top:Top".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.Top.getImagePosition(), SQLServerOperationImageEnum.Top.getTitle(), nodeAttribute.toString(), "Top", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.Top.toString());
        } else if ("Filter:Filter".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.Filter.getImagePosition(), SQLServerOperationImageEnum.Filter.getTitle(), nodeAttribute.toString(), "Filter", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.Filter.toString());
        } else if ("Compute Scalar:Sequence Project".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.Segment.getImagePosition(), SQLServerOperationImageEnum.Segment.getTitle(), nodeAttribute.toString(), "Segment", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.Segment.toString());
        } else if ("Segment:Segment".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.Segment.getImagePosition(), SQLServerOperationImageEnum.Segment.getTitle(), nodeAttribute.toString(), "Segment", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.Segment.toString());
        } else if ("RID Lookup:RID Lookup".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.RIDLookup.getImagePosition(), SQLServerOperationImageEnum.RIDLookup.getTitle(), nodeAttribute.toString(), "RID Lookup", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.RIDLookup.toString());
        } else if ("Concatenation:Concatenation".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.Concatenation.getImagePosition(), SQLServerOperationImageEnum.Concatenation.getTitle(), nodeAttribute.toString(), "Concatenation", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.Concatenation.toString());
        } else if ("Delete:Table Delete".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.TableDelete.getImagePosition(), SQLServerOperationImageEnum.TableDelete.getTitle(), nodeAttribute.toString(), "Table Delete", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.TableDelete.toString());
        } else if ("Update:Table Update".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.TableUpdate.getImagePosition(), SQLServerOperationImageEnum.TableUpdate.getTitle(), nodeAttribute.toString(), "Table Update", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.TableUpdate.toString());
        } else if ("Aggregate:Stream Aggregate".equalsIgnoreCase(tagName)) {
            returnNode = getHTMLNodeImage(SQLServerOperationImageEnum.StreamAggregate.getImagePosition(), SQLServerOperationImageEnum.StreamAggregate.getTitle(), nodeAttribute.toString(), "Stream Aggregate<br/>(Aggregate)", EstimateOperatorCost, parentID);
            getSetIconFilename().add(SQLServerOperationImageEnum.StreamAggregate.toString());
        }
        return returnNode;
    }
    
    /**
     * Create HTML content for node image.
     * <br/><br/>
     * @author vicky.thakor
     * @param imageSrc
     * @param imageTitle
     * @param imageAttribute
     * @param nodeName
     * @param EstimateOperatorCost
     * @return {@link String}
     */
    private String getHTMLNodeImage(String imageSrc, String imageTitle, String imageAttribute, String nodeName, double EstimateOperatorCost, int parentID) {
    	String nodeID = parentID == -2 ? "rootnode" : "parentTable"+parentID;
    	
        return "<table class=\\\"nodeTable\\\" id=\\\""+nodeID+"\\\">"
                + "<tr>"
                + "<td style=\\\"width:95px\\\">"
                + "<div class=\\\"nodeImage\\\" style=\\\"margin:0px auto;width:32px;height:32px;background: url('combine_icon_hibernate_assist.png') no-repeat "+imageSrc+"\\\" title=\\\"" + imageTitle + "\\\" " + imageAttribute + "></div>"
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td style=\\\"width:95px\\\">" + nodeName + "<br/>Cost:" + EstimateOperatorCost + " %"
                + "</td>"
                + "</tr>"
                + "</table>";
    }
    
    /**
     * Get Execution Plan Statistics.
     * <br/><br/>
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
        stringBuilder.append("<div><h2 style=\"margin-top:0px\">Execution Plan Statistics</h2>");
        stringBuilder.append("<div style=\"border: 1px dashed;padding:5px\">");
        stringBuilder.append("<table class=\"stylistTable\" style=\"width:100%\"><thead><tr>");
        stringBuilder.append("<td>Execution Count</td>");
        stringBuilder.append("<td>Last Execution Time</td>");
        stringBuilder.append("<td>Last Elapsed Time</td>");
        stringBuilder.append("<td>Last Logical Reads</td>");
        stringBuilder.append("<td>Last Logical Writes</td>");
        stringBuilder.append("</tr></thead>");
        stringBuilder.append("<tbody><tr>");
        stringBuilder.append("<td>");
        stringBuilder.append(executionCount);
        stringBuilder.append("</td>");
        stringBuilder.append("<td>");
        stringBuilder.append(lastExecutionTime);
        stringBuilder.append("</td>");
        stringBuilder.append("<td>");
        stringBuilder.append(lastElapsedTime);
        stringBuilder.append("</td>");
        stringBuilder.append("<td>");
        stringBuilder.append(lastLogicalReads);
        stringBuilder.append("</td>");
        stringBuilder.append("<td>");
        stringBuilder.append(lastLogicalWrites);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr></tbody></table>");
        stringBuilder.append("</div>");
        stringBuilder.append("</div>");
        stringBuilder.append("<br/>");
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
        String databaseName = "", schemaName = "", tableName = "", baseColumn = "", includeColumn = "", impact = "";

        if (nodeListMissingIndex != null) {
            int nodeLength = nodeListMissingIndex.getLength();
            if (nodeLength > 0) {
                for (int i = 0; i < nodeLength; i++) {
                    if (nodeListMissingIndex.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Element MissingIndexGroupNode = (Element) nodeListMissingIndex.item(i);
                        if (MissingIndexGroupNode != null && "MissingIndexGroup".equalsIgnoreCase(MissingIndexGroupNode.getNodeName())) {
                            if (MissingIndexGroupNode.hasAttribute("Impact")) {
                                impact = MissingIndexGroupNode.getAttribute("Impact");
                            }

                            if (MissingIndexGroupNode.hasChildNodes() && "MissingIndex".equals(MissingIndexGroupNode.getChildNodes().item(1).getNodeName())) {
                                Element MissingIndexNode = (Element) MissingIndexGroupNode.getChildNodes().item(1);
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

                    if (databaseName != null && !databaseName.isEmpty()
                            && schemaName != null && !schemaName.isEmpty()
                            && tableName != null && !tableName.isEmpty()
                            && baseColumn != null && !baseColumn.isEmpty()) {
                        stringBuilder.append("Missing Index Script");
                        if (impact != null && !impact.isEmpty()) {
                            stringBuilder.append(" (Impact : ").append(impact).append(")");
                        }
                        stringBuilder.append(":");
                        stringBuilder.append("CREATE NONCLUSTERED INDEX IndxNc_").append(tableName.replace("[", "").replace("]", ""));
                        String[] baseColumns = baseColumn.split(",");
                        if (baseColumns.length > 1) {
                            stringBuilder.append("_MultiCol").append(i);
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
                        stringBuilder.append(";");
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

	public Set<String> getSetIconFilename() {
		return setIconFilename;
	}

	public void setSetIconFilename(Set<String> setIconFilename) {
		this.setIconFilename = setIconFilename;
	}
}
