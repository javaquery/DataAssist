package com.hibernateassist.test;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.hibernateassist.HibernateAssist;
import com.hibernateassist.bean.User;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			/* Create hibernate configuration. */
			Configuration objConfiguration = new Configuration();
			objConfiguration.configure("com\\hibernateassist\\hbm\\hibernate.cfg.xml");
			
			/* Open session and begin database transaction for database operation. */
			SessionFactory objSessionFactory = objConfiguration.buildSessionFactory();
	        Session objSession = objSessionFactory.openSession();
	        
	        Criteria criteria = objSession.createCriteria(User.class);
	        /*criteria.setProjection(Projections.projectionList().add(Projections.max("id")));*/
	        criteria.createAlias("Messages", "Messages");
	        criteria.createAlias("CreditCard", "CreditCard");
	        
	        
	        /* Create object of Conjunction */
	        Conjunction objConjunction = Restrictions.conjunction();
	        /* Add multiple condition separated by AND clause. */
	        /*objConjunction.add(Restrictions.eq("Messages.userID", 1));
	        objConjunction.add(Restrictions.eq("Messages.userID", 2));*/
	        
	        /* Attach Conjunction in Criteria */
	        /*criteria.add(objConjunction);*/
	        
            /*criteria.add(Restrictions.eq("Email", "vicky.thakor@javaquery.com"));
            criteria.addOrder(Order.asc("Username"));
            criteria.setMaxResults(10);*/
            criteria.add(Restrictions.between("id", 1, 2));
	        List<User> listUser = criteria.list();
	        
	        HibernateAssist objHibernateAssist = new HibernateAssist(objSession);
	        objHibernateAssist.setCriteria(criteria);
	        String strQuery = objHibernateAssist.getValuedCriteriaQuery();
	        System.out.println(strQuery);
	        objHibernateAssist.setHTMLReportFolder("C:\\Users\\0Signals\\Desktop\\Hibernate Assist\\PostgreSQL");
	        objHibernateAssist.analyseCriteria();
	        
	        /*for (User user : listUser) {
				System.out.println(user.getUsername());
			}*/
	        
	        objSession.close();
	        objSessionFactory.close();
	        
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
