package com.hibernateassist.test;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;

import com.hibernateassist.basecode.CriteriaQueryValueTranslator;
import com.hibernateassist.bean.User;

/**
 * Example of getting query with its value from Criteria.
 * <br/><br/>
 * @author vicky.thakor
 */
public class CriteriaQueryWithValuesExample {
	public static void main(String[] args) {
		try {
			/* Create hibernate configuration. */
			Configuration objConfiguration = new Configuration();
			objConfiguration.configure("com\\hibernateassist\\hbm\\hibernate.cfg.xml");
			
			/* Open session and begin database transaction for database operation. */
			SessionFactory objSessionFactory = objConfiguration.buildSessionFactory();
	        Session objSession = objSessionFactory.openSession();
	        
	        /* Create object of Conjunction */
	        Conjunction objConjunction = Restrictions.conjunction();
	        /* Add multiple condition separated by AND clause. */
	        objConjunction.add(Restrictions.eq("Username", "conjunction_value1"));
	        objConjunction.add(Restrictions.eq("Username", "conjunction_value2"));
	        
	        /* Create object of Disjunction */
	        Disjunction objDisjunction = Restrictions.disjunction();
	        /* Add multiple condition separated by OR clause within brackets. */
	        objDisjunction.add(Restrictions.eq("Username", "disjunction_value1"));
	        objDisjunction.add(Restrictions.eq("Username", "disjunction_value2"));
	        
	        List<String> listStr = new ArrayList<String>();
	        listStr.add("vicky.thakor@javaquery.com");
	        listStr.add("chirag.thakor@gmail.com");
	        
	        Criteria criteria = objSession.createCriteria(User.class);
            criteria.add(Restrictions.eq("Username", "vicky.thakor"));
            criteria.add(Restrictions.eq("Username", "karsh.thakor"));
            criteria.add(Restrictions.ne("Username", "chirag.thakor"));
            criteria.add(Restrictions.like("Username", "vicky.thakor"));
            
            criteria.add(Restrictions.in("Email", listStr));
            
            criteria.createAlias("Messages", "Messages");
            criteria.add(Restrictions.eq("Messages.id", 0));
            
            /* Attach Disjunction in Criteria */
            criteria.add(objDisjunction);
            
            /* Attach Conjunction in Criteria */
	        criteria.add(objConjunction);
	        
	        /* Create object of CriteriaQueryValueResolver */
	        CriteriaQueryValueTranslator objCriteriaQueryValueTranslator = new CriteriaQueryValueTranslator(objSessionFactory);
	        String queryWithValue = objCriteriaQueryValueTranslator.getValuedQuery(criteria);
	        /* Print query */
	        System.out.println(queryWithValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
