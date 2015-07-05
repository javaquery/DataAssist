# HibernateAssist
<b>Hibernate Assist</b>, an Open source query analysis tool for Hibernate based application. Now don't just write Hibernate Criteria, understand the behind scene actions. 

#Why Hibernate Assist created?
Hibernate is one of the greatest creation but now developer don't care about query that actually matters. HibernateAssist helps Developers to understand What happened at database server. 

#Features
<table >
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
	<tr>
		<td>✔ Get valued query from Criteria</td>
		<td></td>
	</tr>
</table>

#Database Support
<table style='width:100%'>
	<tr>
		<td>✔ Microsoft SQL Server</td>
		<td>✔ MySQL (5.6 or above)</td>
	</tr>
	<tr>
		<td>✔ PostgreSQL</td>
		<td>✔ Oracle</td>
	</tr>
</table>

#Configuration
No configuration required to use Hibernate Assist

#Online Sample Report
<a href="http://javaquery.github.io/HibernateAssist/" target="_blank">Hibernate Assist</a><br/>
Microsoft SQL Server: <a href="http://javaquery.github.io/HibernateAssist/mssql" target="_blank">http://javaquery.github.io/HibernateAssist/mssql</a><br/>
MySQL: <a href="http://javaquery.github.io/HibernateAssist/mysql" target="_blank">http://javaquery.github.io/HibernateAssist/mysql</a><br/>
Oracle: <a href="http://javaquery.github.io/HibernateAssist/oracle" target="_blank">http://javaquery.github.io/HibernateAssist/oracle</a><br/>
PostgreSQL: <a href="http://javaquery.github.io/HibernateAssist/postgresql" target="_blank">http://javaquery.github.io/HibernateAssist/postgresql</a><br/>

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

#Source Code (Get valued query from Criteria)
<pre>
Criteria criteria = objSession.createCriteria(User.class);
criteria.add(Restrictions.eq("Username", "vicky.thakor"));
List&lt;User&gt; listUser = criteria.list();

HibernateAssist objHibernateAssist = new HibernateAssist(objSession);
objHibernateAssist.setCriteria(criteria);
String strQuery = objHibernateAssist.getValuedCriteriaQuery();
System.out.println(strQuery);
</pre>

#Minimum Requirement
Hibernate 3.5 and above

#Warning
Hibernate Assist is analysis tool and should be used at development phase. It'll cost a lot on Production Server. Please remove Hibernate Assist call in Final Production code.
