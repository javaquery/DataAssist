package com.hibernateassist.pojo;

/**
 * @author vicky.thakor
 * Bean to hold the query details.
 */
public class MSSQLQueryDetails {
	
	private String sqlText;
    private String queryPlan;
    private String lastExecutionTime;
    private int lastElapsedTime;
    private int lastLogicalReads;
    private int lastLogicalWrites;
    private int executionCount;
    
    
	public String getSqlText() {
		return sqlText;
	}
	public void setSqlText(String sqlText) {
		this.sqlText = sqlText;
	}
	public String getQueryPlan() {
		return queryPlan;
	}
	public void setQueryPlan(String queryPlan) {
		this.queryPlan = queryPlan;
	}
	public String getLastExecutionTime() {
		return lastExecutionTime;
	}
	public void setLastExecutionTime(String lastExecutionTime) {
		this.lastExecutionTime = lastExecutionTime;
	}
	public int getLastElapsedTime() {
		return lastElapsedTime;
	}
	public void setLastElapsedTime(int lastElapsedTime) {
		this.lastElapsedTime = lastElapsedTime;
	}
	public int getLastLogicalReads() {
		return lastLogicalReads;
	}
	public void setLastLogicalReads(int lastLogicalReads) {
		this.lastLogicalReads = lastLogicalReads;
	}
	public int getLastLogicalWrites() {
		return lastLogicalWrites;
	}
	public void setLastLogicalWrites(int lastLogicalWrites) {
		this.lastLogicalWrites = lastLogicalWrites;
	}
	public int getExecutionCount() {
		return executionCount;
	}
	public void setExecutionCount(int executionCount) {
		this.executionCount = executionCount;
	}
	
    
}
