package com.hibernateassist.test;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

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
	        List<User> listUser = criteria.list();
	        
	        for (User user : listUser) {
				System.out.println(user.getUsername());
			}
	        
	        objSession.close();
	        objSessionFactory.close();
	        
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
