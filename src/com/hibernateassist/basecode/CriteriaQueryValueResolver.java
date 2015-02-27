package com.hibernateassist.basecode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.impl.CriteriaImpl.CriterionEntry;
import org.hibernate.persister.entity.AbstractEntityPersister;

import com.hibernateassist.HibernateAssist;

/**
 * @author javaQuery
 */
public class CriteriaQueryValueResolver {
	private StringBuilder stringBuilderQuery;
	private Map<String, String> AliasToColumNameMap;
	private SessionFactory objSessionFactory;
	private HibernateAssist objHibernateAssist;
	
	
	public CriteriaQueryValueResolver(SessionFactory sessionFactory) {
		this.objSessionFactory = sessionFactory;
		objHibernateAssist = new HibernateAssist(objSessionFactory.openSession()); 
	}
	
	public String getValuedQuery(Criteria criteria){
		/* Pass Criteria in HibernateAssist to get query */
		objHibernateAssist.setCriteria(criteria);
		
		Map<String, Map<String, String>> TableColumnNamesMap = new HashMap<String, Map<String,String>>();
		
		String expression = "";
		try {
			/* Get query from given Criteria */
			String query = objHibernateAssist.getCriteriaQuery();
			CriteriaImpl objCriteriaImpl = (CriteriaImpl) criteria;
			
			Iterator iterator = objCriteriaImpl.iterateExpressionEntries();		
			while(iterator.hasNext()){
				CriterionEntry c = (CriterionEntry) iterator.next();
				Criterion criterion = c.getCriterion();
				if(expression != null){
					/* Expression for current table */
					if(!expression.contains(".")){
						/* EntityName/Bean */
						String EntityName = objCriteriaImpl.getEntityOrClassName();
						
						if(!TableColumnNamesMap.containsKey(EntityName)){
							TableColumnNamesMap.put(EntityName, getAliasToRealColumnNameMap(EntityName));
						}
						String ColumnBeanName = expression.substring(0, expression.indexOf(" "));
						String RealColumnName = TableColumnNamesMap.get(EntityName).get(ColumnBeanName);
					}else{
						/* Expression for other table */
					}
				}
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
		
		stringBuilderQuery.append("");
		return stringBuilderQuery.toString();
	}
	
	/**
	 * Get Map<String, String> of <ColumnNameInBean, RealColumnName>.
	 * <br/><br/>
	 * @author javaQuery
	 * @param ClassName
	 * @return {@link Map<String, String>}
	 */
	public Map<String, String> getAliasToRealColumnNameMap(String ClassName){
		AliasToColumNameMap = new HashMap<String, String>();
		try {
			/* Get ClassMetadata(Table/Bean) from SessionFactory */
			AbstractEntityPersister objAbstractEntityPersister =  (AbstractEntityPersister) this.objSessionFactory.getClassMetadata(Class.forName(ClassName));
			/* Get column name given in Bean */
			String[] columnAliasNames = objAbstractEntityPersister.getPropertyNames();
			
			for(String columnAlias : columnAliasNames){
				/* Get real column name */
				if(objAbstractEntityPersister.getSubclassPropertyColumnNames(columnAlias) != null
						&& objAbstractEntityPersister.getSubclassPropertyColumnNames(columnAlias).length > 0){
					AliasToColumNameMap.put(columnAlias, objAbstractEntityPersister.getSubclassPropertyColumnNames(columnAlias)[0]);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return this.AliasToColumNameMap;
	}
	
}
