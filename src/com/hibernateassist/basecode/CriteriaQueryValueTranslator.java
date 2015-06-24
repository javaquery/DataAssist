package com.hibernateassist.basecode;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.BetweenExpression;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.InExpression;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.TypedValue;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.impl.CriteriaImpl.CriterionEntry;
import org.hibernate.loader.criteria.CriteriaQueryTranslator;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.type.BooleanType;
import org.hibernate.type.CustomType;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;

import com.hibernateassist.HibernateAssist;

/**
 * Get Query with its value from Criteria.
 * @author vicky.thakor
 * @since 1.2
 */
public class CriteriaQueryValueTranslator {
	private Map<String, String> AliasToColumNameMap;
	private SessionFactory objSessionFactory;
	private HibernateAssist objHibernateAssist;
	
	public CriteriaQueryValueTranslator(SessionFactory sessionFactory) {
		this.objSessionFactory = sessionFactory;
		objHibernateAssist = new HibernateAssist(objSessionFactory.openSession()); 
	}
	
	public String getValuedQuery(Criteria criteria){
		/* Pass Criteria in HibernateAssist to get query */
		objHibernateAssist.setCriteria(criteria);
		
		String query = "";
		try {
			/* Get query from given Criteria */
			query = objHibernateAssist.getCriteriaQuery();
			CriteriaImpl objCriteriaImpl = (CriteriaImpl) criteria;
			String EntityName = objCriteriaImpl.getEntityOrClassName();
			CriteriaQuery criteriaQuery = new CriteriaQueryTranslator((SessionFactoryImplementor) objSessionFactory, objCriteriaImpl, EntityName, "this_");

			if(objCriteriaImpl.getMaxResults() != null){
				String dialect = objHibernateAssist.getDialect();
				if("org.hibernate.dialect.SQLServerDialect".equalsIgnoreCase(dialect))
					query = query.replace("select", "select top " + objCriteriaImpl.getMaxResults());
				else if("org.hibernate.dialect.MySQLDialect".equalsIgnoreCase(dialect)
                		|| "org.hibernate.dialect.MySQLInnoDBDialect".equalsIgnoreCase(dialect)
                		|| "org.hibernate.dialect.MySQLMyISAMDialect".equalsIgnoreCase(dialect)
                		|| "org.hibernate.dialect.PostgreSQLDialect".equalsIgnoreCase(dialect))
					query = query + " limit " + objCriteriaImpl.getMaxResults();
				else if("org.hibernate.dialect.Oracle9Dialect".equalsIgnoreCase(dialect))
					query = query.replace("rownum <= ?", "rownum <= " + objCriteriaImpl.getMaxResults());
			}
			
			/* Get all expression of Query(i.e `where` condition) */
			Iterator iterator = objCriteriaImpl.iterateExpressionEntries();		
			while(iterator.hasNext()){
				CriterionEntry criterionEntry = (CriterionEntry) iterator.next();
				Criterion criterion = criterionEntry.getCriterion();
				
				if(criterion instanceof SimpleExpression){
					SimpleExpression simpleExpression = (SimpleExpression) criterion;
					String strSimpleExpression = simpleExpression.toSqlString(criteria, criteriaQuery);					
					TypedValue[] typedValues = simpleExpression.getTypedValues(criteria, criteriaQuery);
					String strSimpleExpressionImpl = "";
					strSimpleExpressionImpl = replaceParameterValues(typedValues[0], strSimpleExpression);
					strSimpleExpression = replaceSpecialChar(strSimpleExpression);
					query = query.replaceFirst(strSimpleExpression, strSimpleExpressionImpl);
				}else if(criterion instanceof InExpression){
					InExpression inExpression = (InExpression) criterion;
					String strInExpression = inExpression.toSqlString(criteria, criteriaQuery);
					TypedValue[] typedValues = inExpression.getTypedValues(criteria, criteriaQuery);
					
					String strInExpressionImpl = strInExpression;
					for (int i = 0; i < typedValues.length; i++) {
						strInExpressionImpl = replaceParameterValues(typedValues[0], strInExpressionImpl);
					}
					strInExpression = replaceSpecialChar(strInExpression);
					query = query.replaceFirst(strInExpression, strInExpressionImpl);
				}else if(criterion instanceof Conjunction){
					Conjunction conjunction = (Conjunction) criterion;
					String strConjunction = conjunction.toSqlString(criteria, criteriaQuery);
					TypedValue[] typedValues = conjunction.getTypedValues(criteria, criteriaQuery);

					String strConjunctionImpl = strConjunction;
					for (int i = 0; i < typedValues.length; i++) {
						strConjunctionImpl = replaceParameterValues(typedValues[i], strConjunctionImpl);
					}
					strConjunction = replaceSpecialChar(strConjunction);
					query = query.replaceFirst(strConjunction, strConjunctionImpl);
				}else if(criterion instanceof Disjunction){
					Disjunction disjunction = (Disjunction) criterion;
					String strDisjunction = disjunction.toSqlString(criteria, criteriaQuery);
					TypedValue[] typedValues = disjunction.getTypedValues(criteria, criteriaQuery);

					String strDisjunctionImpl = strDisjunction;
					for (int i = 0; i < typedValues.length; i++) {
						strDisjunctionImpl = replaceParameterValues(typedValues[i], strDisjunctionImpl);
					}
					strDisjunction = replaceSpecialChar(strDisjunction);
					query = query.replaceFirst(strDisjunction, strDisjunctionImpl);
				}else if(criterion instanceof BetweenExpression){
					BetweenExpression betweenExpression = (BetweenExpression) criterion;
					String strBetweenExpression = betweenExpression.toSqlString(criteria, criteriaQuery);
					TypedValue[] typedValues = betweenExpression.getTypedValues(criteria, criteriaQuery);

					String strBetweenImpl = strBetweenExpression;
					for (int i = 0; i < typedValues.length; i++) {
						strBetweenImpl = replaceParameterValues(typedValues[i], strBetweenImpl);
					}
					strBetweenExpression = replaceSpecialChar(strBetweenExpression);
					query = query.replaceFirst(strBetweenExpression, strBetweenImpl);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
		return query;
	}
	
	/**
	 * Get String replaced with special character to use in String.replaceFirst() as regular expression.
	 * <br/><br/>
	 * @author vicky.thakor
	 * @param str
	 * @return
	 */
	private String replaceSpecialChar(String str){
		str = str.replace("(", "\\(");
		str = str.replace(")", "\\)");
		str = str.replace("?", "\\?");
		str = str.replace(",", "\\,");	
		return str;
	}
	
	/**
	 * Replace <code>?</code> with original values;
	 * @author vicky.thakor
	 * @date 23rd June, 2015
	 * @since 1.3
	 * @param typedValue
	 * @param strQueryImpl
	 * @return
	 */
	private String replaceParameterValues(TypedValue typedValue, String strQueryImpl){
		if(typedValue != null){
			if(typedValue.getType() instanceof StringType
					|| typedValue.getType() instanceof CustomType){
				String strValue = typedValue.getValue().toString();
				strValue = strValue.replace("'", "''");
				strQueryImpl = strQueryImpl.replace("?", "\'"+ strValue +"\'");
			}else if(typedValue.getType() instanceof TimestampType){
				java.util.Date javaDateFormat = (Date) typedValue.getValue(); 
				java.sql.Timestamp sqlTimeStamp = new java.sql.Timestamp(javaDateFormat.getTime());
				strQueryImpl = strQueryImpl.replace("?", "\'"+sqlTimeStamp+"\'");
			}else if(typedValue.getType() instanceof BooleanType){
				int convertedBoolean = (Boolean) typedValue.getValue() ? 1 : 0;
				strQueryImpl = strQueryImpl.replace("?", String.valueOf(convertedBoolean));
			}else{
				strQueryImpl = strQueryImpl.replaceFirst("\\?", typedValue.getValue().toString());
			}
		}
		return strQueryImpl;
	}
	
	/**
	 * Get Map<String, String> of <ColumnNameInBean, RealColumnName>.
	 * <br/><br/>
	 * @author javaQuery
	 * @param ClassName
	 * @return {@link Map<String, String>}
	 */
	private Map<String, String> getAliasToRealColumnNameMap(String ClassName){
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
