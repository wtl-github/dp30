package db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * OracleDialect.
 */
public class OracleHelp {
	
	public static String forTableBuilderDoBuild(String tableName) {
		return "select * from " + tableName + " where rownum < 1";
	}
	
	// insert into table (id,name) values(seq.nextval, ？)
	public static void forModelSave(Table table, Map<String, Object> attrs, StringBuilder sql, List<Object> paras) {
		sql.append("insert into ").append(table.getName()).append("(");
		StringBuilder temp = new StringBuilder(") values(");
		String[] pKeys = table.getPrimaryKey();
		int count = 0;
		for (Entry<String, Object> e: attrs.entrySet()) {
			String colName = e.getKey();
			if (table.hasColumnLabel(colName)) {
				if (count++ > 0) {
					sql.append(", ");
					temp.append(", ");
				}
				sql.append(colName);
				Object value = e.getValue();
				if (value instanceof String && isPrimaryKey(colName, pKeys) && ((String)value).endsWith(".nextval")) {
				    temp.append(value);
				} else {
				    temp.append("?");
				    paras.add(value);
				}
			}
		}
		sql.append(temp.toString()).append(")");
	}
	
	public static String forModelDeleteById(Table table) {
		String[] pKeys = table.getPrimaryKey();
		StringBuilder sql = new StringBuilder(45);
		sql.append("delete from ");
		sql.append(table.getName());
		sql.append(" where ");
		for (int i=0; i<pKeys.length; i++) {
			if (i > 0)
				sql.append(" and ");
			sql.append(pKeys[i]).append(" = ?");
		}
		return sql.toString();
	}
	
	public static void forModelUpdate(Table table, Map<String, Object> attrs, Set<String> modifyFlag, StringBuilder sql, List<Object> paras) {
		sql.append("update ").append(table.getName()).append(" set ");
		String[] pKeys = table.getPrimaryKey();
		for (Entry<String, Object> e : attrs.entrySet()) {
			String colName = e.getKey();
			if (modifyFlag.contains(colName) && table.hasColumnLabel(colName)) {
				boolean isKey = false;
				for (String pKey : pKeys)	// skip primaryKeys
					if (pKey.equalsIgnoreCase(colName)) {
						isKey = true;
						break ;
					}
				
				if (isKey)
					continue ;
				
				if (paras.size() > 0)
					sql.append(", ");
				sql.append(colName).append(" = ? ");
				paras.add(e.getValue());
			}
		}
		sql.append(" where ");
		for (int i=0; i<pKeys.length; i++) {
			if (i > 0)
				sql.append(" and ");
			sql.append(pKeys[i]).append(" = ?");
			paras.add(attrs.get(pKeys[i]));
		}
	}
	
	public static String forModelFindById(Table table, String columns) {
		StringBuilder sql = new StringBuilder("select ").append(columns).append(" from ");
		sql.append(table.getName());
		sql.append(" where ");
		String[] pKeys = table.getPrimaryKey();
		for (int i=0; i<pKeys.length; i++) {
			if (i > 0)
				sql.append(" and ");
			sql.append(pKeys[i]).append(" = ?");
		}
		return sql.toString();
	}
	
	public static String forDbFindById(String tableName, String[] pKeys) {
		tableName = tableName.trim();
		trimPrimaryKeys(pKeys);
		
		StringBuilder sql = new StringBuilder("select * from ").append(tableName).append(" where ");
		for (int i=0; i<pKeys.length; i++) {
			if (i > 0)
				sql.append(" and ");
			sql.append(pKeys[i]).append(" = ?");
		}
		return sql.toString();
	}
	
	public static String forDbDeleteById(String tableName, String[] pKeys) {
		tableName = tableName.trim();
		trimPrimaryKeys(pKeys);
		
		StringBuilder sql = new StringBuilder("delete from ").append(tableName).append(" where ");
		for (int i=0; i<pKeys.length; i++) {
			if (i > 0)
				sql.append(" and ");
			sql.append(pKeys[i]).append(" = ?");
		}
		return sql.toString();
	}
	
	public static void forPaginate(StringBuilder sql, int pageNumber, int pageSize, String select, String sqlExceptSelect) {
		int satrt = (pageNumber - 1) * pageSize + 1;
		int end = pageNumber * pageSize;
		sql.append("select * from ( select row_.*, rownum rownum_ from (  ");
		sql.append(select).append(" ").append(sqlExceptSelect);
		sql.append(" ) row_ where rownum <= ").append(end).append(") table_alias");
		sql.append(" where table_alias.rownum_ >= ").append(satrt);
	}
	
	public boolean isOracle() {
		return true;
	}
	
	public static void fillStatement(PreparedStatement pst, List<Object> paras) throws SQLException {
		for (int i=0, size=paras.size(); i<size; i++) {
			Object value = paras.get(i);
			if (value instanceof java.sql.Date)
				pst.setDate(i + 1, (java.sql.Date)value);
			else
				pst.setObject(i + 1, value);
		}
	}
	
	public static void fillStatement(PreparedStatement pst, Object... paras) throws SQLException {
		for (int i=0; i<paras.length; i++) {
			Object value = paras[i];
			if (value instanceof java.sql.Date)
				pst.setDate(i + 1, (java.sql.Date)value);
			else if (value instanceof java.sql.Timestamp)
				pst.setTimestamp(i + 1, (java.sql.Timestamp)value);
			else
				pst.setObject(i + 1, value);
		}
	}
	
	public static String getDefaultPrimaryKey() {
		return "ID";
	}
	
	public static boolean isPrimaryKey(String colName, String[] pKeys) {
		for (String pKey : pKeys)
			if (colName.equalsIgnoreCase(pKey))
				return true;
		return false;
	}
	
	public static void trimPrimaryKeys(String[] pKeys) {
		for (int i=0; i<pKeys.length; i++)
			pKeys[i] = pKeys[i].trim();
	}
	
}
