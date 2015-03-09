package com.hibernateassist.bean;

/**
 * @author vicky.thakor
 * Bean to hold the query details.
 */
public class MySQLQueryDetails {
	private String SelectType;
	private String Table;
	private String Type;
	private String PossibleKeys;
	private String Key;
	private long KeyLen;
	private String Ref;
	private long Rows;
	private double Filtered;
	private String Extra;
	private String QueryPlan;
	
	public String getSelectType() {
		return SelectType;
	}
	public void setSelectType(String selectType) {
		SelectType = selectType;
	}
	public String getTable() {
		return Table;
	}
	public void setTable(String table) {
		Table = table;
	}
	public String getType() {
		return Type;
	}
	public void setType(String type) {
		Type = type;
	}
	public String getPossibleKeys() {
		return PossibleKeys;
	}
	public void setPossibleKeys(String possibleKeys) {
		PossibleKeys = possibleKeys;
	}
	public String getKey() {
		return Key;
	}
	public void setKey(String key) {
		Key = key;
	}
	public long getKeyLen() {
		return KeyLen;
	}
	public void setKeyLen(long keyLen) {
		KeyLen = keyLen;
	}
	public String getRef() {
		return Ref;
	}
	public void setRef(String ref) {
		Ref = ref;
	}
	public long getRows() {
		return Rows;
	}
	public void setRows(long rows) {
		Rows = rows;
	}
	public double getFiltered() {
		return Filtered;
	}
	public void setFiltered(double filtered) {
		Filtered = filtered;
	}
	public String getExtra() {
		return Extra;
	}
	public void setExtra(String extra) {
		Extra = extra;
	}
	public String getQueryPlan() {
		return QueryPlan;
	}
	public void setQueryPlan(String queryPlan) {
		QueryPlan = queryPlan;
	}
}
