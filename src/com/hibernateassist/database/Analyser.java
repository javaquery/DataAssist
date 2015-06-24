package com.hibernateassist.database;

/**
 * Interface each Analyser should implement.
 * @author vicky.thakor
 * @date 23rd June, 2015
 * @since 1.3
 */
public interface Analyser {
	public void generateQueryReport(String hibernateQuery, String actualQuery, String reportFolderPath, String strFilenamePrefix) throws Exception;
}
