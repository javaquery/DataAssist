package com.hibernateassist.common;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

/**
 * @author vicky.thakor
 */
public class CommonUtil {

	public enum jsPlumbArrowPosition{
		TopCenter, BottomCenter, LeftMiddle, RightMiddle, Center, TopRight, BottomRight, TopLeft, BottomLeft
	}

	private static String alphabet = "G8abHcId1J23Kefg9L0MhOPijkQlRmnSopT45UVqrWsFXtuY6EZ7vwCxByzA";
	
    /**
     * @author vicky.thakor
     * @param query
     * @return {@link String} value or null in case of null query
     * Replace ? with @P0, @P1, etc...
     */
    public static String replaceQuestionMarkWithP(String query) {
        if (query instanceof String) {
            int position = -1;
            int count = 0;
            String frontPortion = "";
            String rearPortion = "";
            while (query.contains("?")) {
                position = query.indexOf("?");
                frontPortion = query.substring(0, position + 1);
                rearPortion = query.substring(position + 1, query.length());
                frontPortion = frontPortion.replace("?", " @P" + count+" ");
                count++;
                query = frontPortion + rearPortion;
            }
            return query;
        }
        return null;
    }

    /**
     * @author vicky.thakor
     * @return {@link String}
     * To get HTML report header String
     */
    public static String getHTMLReportHeader(){
    	return "<!DOCTYPE html>\n"+
    				 "<html>\n"
    					+"<head>\n"
							+"<title>Hibernate Assist Report</title>\n"
							+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
							+ "<script src=\"jquery-1.8.2.min.js\"></script>\n"
			                + "<script src=\"jquery.jsPlumb-1.3.3-all.js\"></script>\n"
			                + "<script>"
			                + "$(document).ready(function(){"
			                + "$(\".nodeImage\").live(\"click\",function(){\n"
			                + "$(this).each(function() {\n"
			                + " var htmlContent = \"<tr><td><a style=\\\"color:blue;text-decoration: underline;cursor: pointer;\\\" id=\\\"closenodedetails\\\">close</a></td></tr>\";\n"
			                + " var count = 0;\n"
			                + "$.each(this.attributes, function() {\n"
			                + "  if(this.specified) {\n"
			                + "    if(this.name != \"style\" && this.name != \"class\" && this.name != \"src\" && this.name != \"title\" && this.name != \"nodeid\" && this.name != \"id\"){\n"
			                + "        if(count == 0){\n"
			                + "           htmlContent += \"<tr>\";\n"
			                + "        }else if(count == 1){\n"
			                + "           htmlContent += \"</tr>\";\n"
			                + "           count = 0;\n"
			                + "        }\n"
			                + "        htmlContent += \"<td>\" + this.value; + \"</td>\";\n"
			                + "        count = count + 1;\n"
			                + "    }\n"
			                + "  }\n"
			                + "});\n"
			                + "$(\"#nodeDetails\").css(\"display\",\"block\");\n"
			                + "$(\"#nodeDetails\").html(\"<table style=\\\"width:100%;font-size: 13px;\\\">\"+htmlContent+\"</table>\");\n"
			                + "});\n"
			                + "});\n"
			                + "$(\"#closenodedetails\").live(\"click\",function(){"
			                + "$(\"#nodeDetails\").css(\"display\",\"none\");"
			                + "});\n"
			                + "$(\".show_graphical_data\").click(function(){" 
			                + "$(\".statistics_data\").css(\"display\",\"none\");"
			                + "$(\".about_hibernate_assist\").css(\"display\",\"none\");"
			                + "$(\".graphical_data\").css(\"display\",\"block\");"
			                +"});"
			                + "$(\".show_statistics_data\").click(function(){" 
			                + "$(\".graphical_data\").css(\"display\",\"none\");"
			                + "$(\".about_hibernate_assist\").css(\"display\",\"none\");"
			                + "$(\".statistics_data\").css(\"display\",\"block\");"
			                +"});"
			                + "$(\".show_about_hibernate_assist\").click(function(){" 
			                + "$(\".graphical_data\").css(\"display\",\"none\");"
			                + "$(\".statistics_data\").css(\"display\",\"none\");"
			                + "$(\".about_hibernate_assist\").css(\"display\",\"block\");"
			                +"});"
			                +"});"
			                + "</script>\n"
							+"<style>\n"
								+"body{margin:0;padding:0;width: 100%;height: 100%;overflow-x: hidden;background: rgb(224, 222, 222) !important;font-family:\"Helvetica Neue\", Helvetica, \"Segoe UI\", Arial, freesans, sans-serif}\n"
	    						+".smooth_blue_background{background-color: #50b7dc !important;border-bottom: 1px solid #2693ba;-webkit-box-shadow: rgba(0,0,0,0.3) 0px 2px 2px -1px;-moz-box-shadow: rgba(0,0,0,0.3) 0px 2px 2px -1px;box-shadow: rgba(0,0,0,0.3) 0px 2px 2px -1px;}\n"
	    						+".header{z-index: 1000;position: fixed;top:0px;width: 100%;height: 40px}"
	    						+".sidebar_left{position:fixed;top: 0px; left: 0px; width:59px; height: 100%;text-align:center}"
	    						+".sidebar_icon{width:32px; height:32px;  margin: 0px auto;margin-top: 10px;cursor:pointer}"
	    						+".whiteFadedbox{-webkit-box-shadow:rgba(0, 0, 0, 0.2) 0px 2px 4px;box-shadow:rgba(0, 0, 0, 0.2) 0px 2px 4px;-webkit-transition:opacity 0.218s;background-color:white;border:1px solid rgba(0, 0, 0, 0.2);cursor:default;outline:none;padding: 10px;}"
	    						+".content{margin:5px 5px 10px 66px;}"
	    						+".operation_header{margin-bottom: 10px}"
	    						+".operation_header h3{margin-top: 3px; margin-bottom: 5px}"
	    						+".stylistTable{border-collapse: collapse;}"
	    						+".stylistTable thead tr td{border-bottom: 2px solid gray;padding: 5px;font-size: 17px}"
	    						+".stylistTable tbody tr td{border-bottom: 1px solid gray;padding: 5px;font-size: 15px}"
	    						+".statistics_data{display:none;margin-bottom:30px}"
	    						+".graphical_data{}"
	    						+".about_hibernate_assist{display:none}"
	    						+".ul_links{list-style:none}"
	    						+".ul_links li{display: inline-block; margin-right:20px}"
	    						+".externalLink{color:blue;text-decoration:none}"
	    						+".externalLink:hover {text-decoration: underline !important;}"
	    						/* MSSQL */
	    						+ "#nodeDetails{width: 250px;max-height:400px;overflow:scroll;z-index: 1;margin-top: 1px;display:none;position:absolute;top:0px;right:20px;background-color:rgb(255, 255, 161)}\n"
	    						+ ".nodeTable{width:95px !important;height: 95px !important;display: inline-block;vertical-align: top;text-align:center;font-size:12px;margin:0px auto}"
	    						+ ".nodeImage{margin: 0px auto;cursor:pointer}"
	    						/* MySQL */
	    						+ ".connectionDot{border-radius:10px;-webkit-border-radius:10px;-moz-border-radius:10px;box-shadow:0 0 8px rgba(0, 0, 0, .8);-webkit-box-shadow:0 0 8px rgba(0, 0, 0, .8);-moz-box-shadow:0 0 8px rgba(0, 0, 0, .8);margin: 0px auto;margin-bottom: 99px;width: 10px;height: 10px;}"
	    		                + ".nestedLoopDivMySQL { margin: 0px auto;margin-bottom:77px; border: 2px solid gray; width: 50px; height: 50px; text-align:center;}"
	    		                + ".tableBlockMySQL{display: inline-block; text-align:center;margin-right: 70px}"
	    		                + ".tableBlockContentMySQL{color:white; padding: 5px; border: 1px solid black; width: 140px; height: 15px; text-align:center; position: relative;}"
	    		                + ".groupByMySQL{margin:0px auto;border: 2px solid brown;padding: 7px;margin-bottom: 60px;width:50px;}"
	    		                + ".orderByMySQL{margin:0px auto;border: 2px solid red;padding: 7px;margin-bottom: 60px;width:50px;}"
	    		                + ".queryBlockMySQL{margin:0px auto;border: 1px solid black;padding: 7px;margin-bottom: 60px;width:70px;background-color: lightgray;}"
	    					+"</style>"
    					+"</head>"
    					+"<body>"
    						+"<div style=\"position: fixed; top: 2px; right: 2px;font-size: 12px;\">"+new Date().toString()+"</div>"
    						+"<div class=\"sidebar_left smooth_blue_background\">"
    						+"<div class=\"sidebar_icon show_graphical_data\" style=\"background: url('combine_icon_hibernate_assist.png') no-repeat -8px -97px\" title=\"Graphical Execution Plan\"></div>"
    						+"<div class=\"sidebar_icon show_statistics_data\" style=\"background: url('combine_icon_hibernate_assist.png') no-repeat -56px -97px\" title=\"Execution Plan Statistics\"></div>"
    						+"<div class=\"sidebar_icon show_about_hibernate_assist\" style=\"background: url('combine_icon_hibernate_assist.png') no-repeat -104px -97px\" title=\"About Hibernate Assist\"></div>"
    						+"</div>"
    						+"<div class=\"content\">";
    }

    
    /**
     * @author vicky.thakor
     * @return {@link String}
     * To get HTML report footer String
     */
    public static String getHTMLReportFooter(){
    	return "<div class=\"about_hibernate_assist\">"
	    			 +"<div class=\"operation_header\">"
	    			 +"<h3>Hibernate Assist</h3>"
	    			 +"</div>"
	    			 +"<div class=\"whiteFadedbox\">"
	    			 +"<h2 style=\"margin-top: 0px;margin-bottom: 2px;\">Hibernate Assist</h2>"
	    			 +"<hr/>"
	    			 +"<b>Hibernate Assist</b>, an Open source query analysis tool for Hibernate based application. Now don't just write Hibernate Criteria, understand the behind scene actions."
	    			 +"<br/>"
	    			 +"<br/>"
	    			 +"<h2 style=\"margin-bottom: 2px;\">Why Hibernate Assist created?</h2>"
	    			 +"<hr/>"
	    			 +"Hibernate is one of the greatest creation but now developer don't care about query that actually matters. HibernateAssist helps Developers to understand What happened at database server."
	    			 +"<br/>"
	    			 +"<br/>"
	    			 +"<h2 style=\"margin-bottom: 2px;\">Warning</h2>"
	    			 +"<hr/>"
	    			 +"Hibernate Assist is analysis tool and should be used at development phase. It'll cost a lot on Production Server. Please remove Hibernate Assist call in Final Production code."
	    			 +"<br/>"
	    			 +"<br/>"
	    			 +"<h2 style=\"margin-bottom: 2px;\">Note</h2>"
	    			 +"<hr/>"
	    			 +"<ul>"
	    			 +"<li>Third party tool's rights reserved by their respective authors.</li>"
	         		+ "<li>Files required to view this HTML report <b>1.)</b> combine_icon_hibernate_assist.png <b>2.)</b> jquery-1.8.2.min.js <b>3.)</b> jquery.jsPlumb-1.3.3-all.js</li>"
	         		+ "<li><b>Microsoft SQL Server Report:</b> Icon Not Found, it represents that this particular operation not handled in HibernateAssist. Please report it on <a href=\"mailto:vicky.thakor@javaquery.com\" target=\"_blank\" class=\"externalLink\">vicky.thakor@javaquery.com</a> with Operation displayed under icon.</li>"
	    			 +"</ul>"
	    			 +"<br/>"
	    			 +"<div style=\"text-align:center\">"
	    			 +"<a class=\"externalLink\" href=\"http://www.javaquery.com/p/hibernateassist.html\" target=\"_blank\">Download Library</a>"
	    			 +"<ul class=\"ul_links\">"
	    			 +"<li><a class=\"externalLink\" href=\"http://www.javaquery.com\" target=\"_blank\">www.javaquery.com</li>"
	    			 +"<li><a class=\"externalLink\" href=\"http://www.facebook.com/thejavaquery\" target=\"_blank\">facebook.com/thejavaquery</a></li>"
	    			 +"<li><a class=\"externalLink\" href=\"http://www.twitter.com/javaquery\" target=\"_blank\">twitter.com/javaquery</a></li>"
	    			 +"<li><a class=\"externalLink\" href=\"http://plus.google.com/+javaquery\" target=\"_blank\">plus.google.com/+javaquery</li>"
	    			 +"<li><a class=\"externalLink\" href=\"http://github.com/javaquery\" target=\"_blank\">github.com/javaquery</li>"
	    			 +"</ul>"
	    			 +"<div>"
	    			 +"</div>"
	    			 +"</div>"
	    			 +"</div>"
    				 +"</body>"
    				 +"</html>";
    }
	
    /**
     * Use jsPlumb to connect two nodes with an Arrow.
     * <br/><br/>
     * @author 0Signals
     * @param SourceNode
     * @param TargetNode
     * @return
     */
    public static String getjsPlumbScript(String SourceNode, String TargetNode, jsPlumbArrowPosition SourceArrowPosition, jsPlumbArrowPosition TargetArrowPosition){
    	StringBuilder jsPlumb = new StringBuilder("");
    	jsPlumb.append("jsPlumb.bind(\"ready\", function() {");
    	jsPlumb.append("jsPlumb.connect({");
    	jsPlumb.append("source: \"").append(SourceNode).append("\",");
    	jsPlumb.append("target: \"").append(TargetNode).append("\",");
    	jsPlumb.append("anchors: [\"").append(SourceArrowPosition).append("\",\"").append(TargetArrowPosition).append("\"],");
    	jsPlumb.append("endpoint: [\"Dot\", {radius: 1}],");
    	jsPlumb.append("endpointStyle: {fillStyle: \"#5b9ada\"},");
    	jsPlumb.append("setDragAllowedWhenFull: true,");
    	jsPlumb.append("paintStyle: {strokeStyle: \"#5b9ada\",lineWidth: 3},");
    	jsPlumb.append("connector: [\"Straight\"],");
    	jsPlumb.append("connectorStyle: {lineWidth: 3,strokeStyle: \"#5b9ada\"},");
    	jsPlumb.append("overlays: [[\"Arrow\", {width: 10,length: 10,foldback: 1,location: 1,id: \"arrow\"}]]");
    	jsPlumb.append("});");
    	jsPlumb.append("});");
    	return jsPlumb.toString();
    }
    
    /**
     * Copy .js and .png file from jar to report folder.
     * <br/><br/>
     * @author vicky.thakor
     * @param reportFolderPath
     */
    public void copyJavaScriptAndImageFile(String reportFolderPath){
    	List<String> files = new ArrayList<String>();
    	files.add("combine_icon_hibernate_assist.png");
    	files.add("jquery-1.8.2.min.js");
    	files.add("jquery.jsPlumb-1.3.3-all.js");
    	
    	try {
	    	for(String filename : files){
	    		File copyFile = new File(reportFolderPath + File.separatorChar + filename);
	    		if(filename.endsWith(".js")){
	    			/* Copy .js file if not exists */
	    			if(!copyFile.exists()){
	    				InputStream objInputStream = getClass().getResourceAsStream("/com/hibernateassist/files/"+filename);
	    				BufferedReader objBufferedReader = new BufferedReader(new InputStreamReader(objInputStream));
	    				StringBuilder objStringBuilder = new StringBuilder();
	    				String line;
						while ((line = objBufferedReader.readLine()) != null) {
							objStringBuilder.append(line);
							objStringBuilder.append(System.getProperty("line.separator"));
						}
						String fileContent = objStringBuilder.toString();
						/* Write to destination file. */
						copyFile.createNewFile();
		    	        BufferedWriter objBufferedWriter = new BufferedWriter(new FileWriter(copyFile));
		    	        objBufferedWriter.write(fileContent);
		    	        objBufferedWriter.close();
		    	        
		    	        objBufferedReader.close();
		    	        objInputStream.close();
	    			}
	    		}else{
	    			BufferedImage objBufferedImage = ImageIO.read(getClass().getResourceAsStream("/com/hibernateassist/files/"+filename));
	    			ImageIO.write(objBufferedImage, "PNG", copyFile);
	    		}
	    	}
    	} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Generate random String by providing length.<br/><br/>
     * @author vicky.thakor
     * @param length
     * @return
     */
    public static final String getRandomString(int length) {
        Random rand = new Random();
        String token = "";
        char tokenChar = '\0';
        for (int i = 0; i < length; i++) {
            tokenChar = alphabet.charAt(rand.nextInt(alphabet.length()));
            token += tokenChar;
        }
        return token;
    }
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
