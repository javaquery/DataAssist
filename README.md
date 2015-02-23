# HibernateAssist
<b>Hibernate Assist</b>, an Open source query analysis tool for Hibernate based application. Now don't just write Hibernate Criteria, understand the behind scene actions. 

#Why Hibernate Assist created?
Hibernate is one of the greatest creation but now developer don't care about query that actually matters. HibernateAssist helps Developers to understand What happened at database server. 

#Features
<table>
	<tr>
		<td>✔ Analyse Hibernate Criteria</td>
		<td>✔ Analyse Microsoft SQL Plan file(.sqlplan)</td>
	</tr>
	<tr>
		<td>✔ Browser based HTML Report</td>
		<td>✔ Lightweight</td>
	</tr>
	<tr>
		<td>✔ Execution Plan Statistics</td>
		<td>✔ Query Statistics</td>
	</tr>
	<tr>
		<td>✔ Graphical Representation</td>
		<td>✔ Missing Index Details</td>
	</tr>
	<tr>
		<td>✔ Access any hibernate.cfg.xml Property</td>
		<td>✔ Get Query from Criteria</td>
	</tr>
</table>

#Live Sample Report
<a href="http://javaquery.github.io/HibernateAssist/" target="_blank">http://javaquery.github.io/HibernateAssist/</a>

#Source Code (Criteria Analysis)
<pre>
Criteria criteria = objSession.createCriteria(User.class);
criteria.add(Restrictions.eq("Username", "vicky.thakor"));
List&lt;User&gt; listUser = criteria.list();

HibernateAssist objHibernateAssist = new HibernateAssist(objSession);
objHibernateAssist.setCriteria(criteria);
objHibernateAssist.setHTMLReportFolder("C:\\Users\\javaQuery\\Desktop\\HTMLReport");
objHibernateAssist.analyseCriteria();
</pre>

#Source Code (Get Query from Criteria)
<pre>
HibernateAssist objHibernateAssist = new HibernateAssist(objSession);
objHibernateAssist.setCriteria(criteria);
objHibernateAssist.getCriteriaQuery();
</pre>

#Source Code (Access hibernate.cfg.xml property)
<pre>
HibernateAssist objHibernateAssist = new HibernateAssist(objSession);
objHibernateAssist.getHibernateProperty("hibernate.show_sql");
</pre>

#Source Code (Microsoft SQL Server .sqlplan Analysis)
<pre>
HibernateAssist objHibernateAssist = new HibernateAssist();
objHibernateAssist.setMSSQLExecutionPlanFile("C:\\Users\\javaQuery\\Desktop\\HTMLReport\\SQL Plans\\ComplexQuery.sqlplan");
objHibernateAssist.setHTMLReportFolder("C:\\Users\\javaQuery\\Desktop\\HTMLReport");
objHibernateAssist.analyseMSSQLPlan();
</pre>

