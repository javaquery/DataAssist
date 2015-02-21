package com.hibernateassist.test;

import java.io.IOException;

import com.hibernateassist.HibernateAssist;

public class ReportFromFile {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		 HibernateAssist objHibernateAssist = new HibernateAssist();
	        objHibernateAssist.setMSSQLExecutionPlanFile("C:\\Users\\0Signals\\Desktop\\HTMLReport\\SQL Plans\\meeting modified.sqlplan");
	        objHibernateAssist.setHTMLReportFolder("C:\\Users\\0Signals\\Desktop\\HTMLReport");
	        objHibernateAssist.analyseMSSQLPlan();

	}

}
