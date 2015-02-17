package com.hibernateassist.common;

import java.util.Date;

/**
 * @author vicky.thakor
 */
public class CommonUtil {

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
    public static String getHTMLReportHeader() {
        return "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<head>\n"
                + "<title>Hibernate Assist report by javaQuery</title>\n"
                + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
                + "<script src=\"jquery-1.8.2.min.js\"></script>\n"
                + "<script src=\"jquery.jsPlumb-1.3.3-all.js\"></script>\n"
                + "<script>"
                + "$(\".nodeImage\").live(\"click\",function(){\n"
                + "$(this).each(function() {\n"
                + " var htmlContent = \"<tr><td style=\\\"text-align:right\\\"><a style=\\\"color:blue;text-decoration: underline;cursor: pointer;\\\" id=\\\"closenodedetails\\\">close</a></td></tr>\";\n"
                + " var count = 0;\n"
                + "$.each(this.attributes, function() {\n"
                + "  if(this.specified) {\n"
                + "    if(this.name != \"style\" && this.name != \"class\" && this.name != \"src\" && this.name != \"title\" && this.name != \"nodeid\"){\n"
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
                + "});"
                + "</script>\n"
                + "<style>\n"
                + ".content-footer{text-align: center}\n"
                + ".nodeTable{width:95px !important;height: 95px !important;display: inline-block;vertical-align: top;text-align:center;font-size:12px;overflow:hidden;margin:0px auto}"
                + ".smooth_blue_background{background-color: #50b7dc !important;border-bottom: 1px solid #2693ba;-webkit-box-shadow: rgba(0,0,0,0.3) 0px 2px 2px -1px;-moz-box-shadow: rgba(0,0,0,0.3) 0px 2px 2px -1px;box-shadow: rgba(0,0,0,0.3) 0px 2px 2px -1px;}"
                + ".menuBarLocation{z-index: 1000;position: fixed;top:0px;width: 100%;height: 40px}"
                + "body{margin:0;padding:0;width: 100%;height: 100%;overflow-x: hidden}"
                + ".stylistTable thead{border: 0; background-color: lightgray;color: black;white-space: nowrap;border-color: #e1e1e1;border-width: 0 1px 1px 0;border-style: solid;text-transform: uppercase;}"
                + ".stylistTable tr{border: 0;border-color: #e1e1e1;border-style: solid;border-width: 1px 0 0 1px;}"
                + ".stylistTable td{border: 0;padding: 10px;border-color: #e1e1e1;border-style: solid;border-spacing: 2px;border-width: 1px 0 0 1px;}"
                + ".stylistTable{border-collapse: separate;border-spacing: 0;border-color: #e1e1e1;border-width: 1px 0 0 1px;border-style: solid;}"
                + ".stylistTable tbody{background-color: skyblue; color: white;}"
                + ".nodeImage{margin: 0px auto;cursor:pointer}"
                + "</style>\n"
                + "</head>\n"
                + "<body>\n"
                + "<div class=\"smooth_blue_background menuBarLocation\">"
                + "<div style=\"color: white;font-size: 20px;padding: 6px;display: inline-block\">"
                + "Hibernate Assist Report"
                + "<div style=\"position:absolute; right: 10px; top: 3px;font-size: 13px\">Report date: " + new Date().toString() + "<br/>Generated by: " + System.getProperty("user.name") + "</div>"
                + "</div>"
                + "</div>"
                + "<div style=\"margin-top: 35px;padding: 10px;\">"
                + "</div>"
                + "<div style=\"padding: 10px;\">";
    }

    
    /**
     * @author vicky.thakor
     * @return {@link String}
     * To get HTML report footer String
     */
    public static String getHTMLReportFooter() {
        return "<div class=\"content-footer\"><a href=\"www.javaquery.com\" target=\"_blank\">www.javaquery.com</a></div>"
                + "</div></body>"
                + "</html>";
    }
	
    /**
     * Use jsPlumb to connect two nodes with an Arrow.
     * <br/><br/>
     * @author 0Signals
     * @param SourceNode
     * @param TargetNode
     * @return
     */
    public static String getjsPlumbScript(String SourceNode, String TargetNode){
    	StringBuilder jsPlumb = new StringBuilder("");
    	jsPlumb.append("jsPlumb.bind(\"ready\", function() {");
    	jsPlumb.append("jsPlumb.connect({");
    	jsPlumb.append("source: \""+SourceNode+"\",");
    	jsPlumb.append("target: \""+TargetNode+"\",");
    	jsPlumb.append("anchors: [\"LeftMiddle\",\"RightMiddle\"],");
    	jsPlumb.append("endpoint: [\"Dot\", {radius: 1}],");
    	jsPlumb.append("endpointStyle: {fillStyle: \"white\"},");
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
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
