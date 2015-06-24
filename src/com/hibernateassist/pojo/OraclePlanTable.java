package com.hibernateassist.pojo;

import java.util.Date;

/**
 * @author vicky.thakor
 * @date 21st June, 2015
 * @since 1.3
 */
public class OraclePlanTable {
	private String STATEMENT_ID;
	private Number PLAN_ID;
	private Date TIMESTAMP;
	private String REMARKS;
	private String OPERATION;
	private String OPTIONS;
	private String OBJECT_NODE;
	private String OBJECT_OWNER;
	private String OBJECT_NAME;
	private String OBJECT_ALIAS;
	private Number OBJECT_INSTANCE;
	private String OBJECT_TYPE;
	private String OPTIMIZER;
	private Number SEARCH_COLUMNS;
	private Number ID;
	private Number PARENT_ID;
	private Number DEPTH;
	private Number POSITION;
	private Number COST;
	private Number CARDINALITY;
	private Number BYTES;
	private String OTHER_TAG;
	private String PARTITION_START;
	private String PARTITION_STOP;
	private Integer PARTITION_ID;
	private Long OTHER;
	private String OTHER_XML;
	private String DISTRIBUTION;
	private Number CPU_COST;
	private Number IO_COST;
	private Integer TEMP_SPACE;
	private String ACCESS_PREDICATES;
	private String FILTER_PREDICATES;
	private String PROJECTION;
	private Number TIME;
	private String QBLOCK_NAME;
	public String getSTATEMENT_ID() {
		return STATEMENT_ID;
	}
	public void setSTATEMENT_ID(String sTATEMENT_ID) {
		STATEMENT_ID = sTATEMENT_ID;
	}
	public Number getPLAN_ID() {
		return PLAN_ID;
	}
	public void setPLAN_ID(Number pLAN_ID) {
		PLAN_ID = pLAN_ID;
	}
	public Date getTIMESTAMP() {
		return TIMESTAMP;
	}
	public void setTIMESTAMP(Date tIMESTAMP) {
		TIMESTAMP = tIMESTAMP;
	}
	public String getREMARKS() {
		return REMARKS;
	}
	public void setREMARKS(String rEMARKS) {
		REMARKS = rEMARKS;
	}
	public String getOPERATION() {
		return OPERATION;
	}
	public void setOPERATION(String oPERATION) {
		OPERATION = oPERATION;
	}
	public String getOPTIONS() {
		return OPTIONS;
	}
	public void setOPTIONS(String oPTIONS) {
		OPTIONS = oPTIONS;
	}
	public String getOBJECT_NODE() {
		return OBJECT_NODE;
	}
	public void setOBJECT_NODE(String oBJECT_NODE) {
		OBJECT_NODE = oBJECT_NODE;
	}
	public String getOBJECT_OWNER() {
		return OBJECT_OWNER;
	}
	public void setOBJECT_OWNER(String oBJECT_OWNER) {
		OBJECT_OWNER = oBJECT_OWNER;
	}
	public String getOBJECT_NAME() {
		return OBJECT_NAME;
	}
	public void setOBJECT_NAME(String oBJECT_NAME) {
		OBJECT_NAME = oBJECT_NAME;
	}
	public String getOBJECT_ALIAS() {
		return OBJECT_ALIAS;
	}
	public void setOBJECT_ALIAS(String oBJECT_ALIAS) {
		OBJECT_ALIAS = oBJECT_ALIAS;
	}
	public Number getOBJECT_INSTANCE() {
		return OBJECT_INSTANCE;
	}
	public void setOBJECT_INSTANCE(Number oBJECT_INSTANCE) {
		OBJECT_INSTANCE = oBJECT_INSTANCE;
	}
	public String getOBJECT_TYPE() {
		return OBJECT_TYPE;
	}
	public void setOBJECT_TYPE(String oBJECT_TYPE) {
		OBJECT_TYPE = oBJECT_TYPE;
	}
	public String getOPTIMIZER() {
		return OPTIMIZER;
	}
	public void setOPTIMIZER(String oPTIMIZER) {
		OPTIMIZER = oPTIMIZER;
	}
	public Number getSEARCH_COLUMNS() {
		return SEARCH_COLUMNS;
	}
	public void setSEARCH_COLUMNS(Number sEARCH_COLUMNS) {
		SEARCH_COLUMNS = sEARCH_COLUMNS;
	}
	public Number getID() {
		return ID;
	}
	public void setID(Number iD) {
		ID = iD;
	}
	public Number getPARENT_ID() {
		return PARENT_ID;
	}
	public void setPARENT_ID(Number pARENT_ID) {
		PARENT_ID = pARENT_ID;
	}
	public Number getDEPTH() {
		return DEPTH;
	}
	public void setDEPTH(Number dEPTH) {
		DEPTH = dEPTH;
	}
	public Number getPOSITION() {
		return POSITION;
	}
	public void setPOSITION(Number pOSITION) {
		POSITION = pOSITION;
	}
	public Number getCOST() {
		return COST;
	}
	public void setCOST(Number cOST) {
		COST = cOST;
	}
	public Number getCARDINALITY() {
		return CARDINALITY;
	}
	public void setCARDINALITY(Number cARDINALITY) {
		CARDINALITY = cARDINALITY;
	}
	public Number getBYTES() {
		return BYTES;
	}
	public void setBYTES(Number bYTES) {
		BYTES = bYTES;
	}
	public String getOTHER_TAG() {
		return OTHER_TAG;
	}
	public void setOTHER_TAG(String oTHER_TAG) {
		OTHER_TAG = oTHER_TAG;
	}
	public String getPARTITION_START() {
		return PARTITION_START;
	}
	public void setPARTITION_START(String pARTITION_START) {
		PARTITION_START = pARTITION_START;
	}
	public String getPARTITION_STOP() {
		return PARTITION_STOP;
	}
	public void setPARTITION_STOP(String pARTITION_STOP) {
		PARTITION_STOP = pARTITION_STOP;
	}
	public Integer getPARTITION_ID() {
		return PARTITION_ID;
	}
	public void setPARTITION_ID(Integer pARTITION_ID) {
		PARTITION_ID = pARTITION_ID;
	}
	public Long getOTHER() {
		return OTHER;
	}
	public void setOTHER(Long oTHER) {
		OTHER = oTHER;
	}
	public String getOTHER_XML() {
		return OTHER_XML;
	}
	public void setOTHER_XML(String oTHER_XML) {
		OTHER_XML = oTHER_XML;
	}
	public String getDISTRIBUTION() {
		return DISTRIBUTION;
	}
	public void setDISTRIBUTION(String dISTRIBUTION) {
		DISTRIBUTION = dISTRIBUTION;
	}
	public Number getCPU_COST() {
		return CPU_COST;
	}
	public void setCPU_COST(Number cPU_COST) {
		CPU_COST = cPU_COST;
	}
	public Number getIO_COST() {
		return IO_COST;
	}
	public void setIO_COST(Number iO_COST) {
		IO_COST = iO_COST;
	}
	public Integer getTEMP_SPACE() {
		return TEMP_SPACE;
	}
	public void setTEMP_SPACE(Integer tEMP_SPACE) {
		TEMP_SPACE = tEMP_SPACE;
	}
	public String getACCESS_PREDICATES() {
		return ACCESS_PREDICATES;
	}
	public void setACCESS_PREDICATES(String aCCESS_PREDICATES) {
		ACCESS_PREDICATES = aCCESS_PREDICATES;
	}
	public String getFILTER_PREDICATES() {
		return FILTER_PREDICATES;
	}
	public void setFILTER_PREDICATES(String fILTER_PREDICATES) {
		FILTER_PREDICATES = fILTER_PREDICATES;
	}
	public String getPROJECTION() {
		return PROJECTION;
	}
	public void setPROJECTION(String pROJECTION) {
		PROJECTION = pROJECTION;
	}
	public Number getTIME() {
		return TIME;
	}
	public void setTIME(Number tIME) {
		TIME = tIME;
	}
	public String getQBLOCK_NAME() {
		return QBLOCK_NAME;
	}
	public void setQBLOCK_NAME(String qBLOCK_NAME) {
		QBLOCK_NAME = qBLOCK_NAME;
	}
}
