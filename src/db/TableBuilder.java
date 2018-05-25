package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TableBuilder build the mapping of model between class and table.
 */
public class TableBuilder {
	
	private static final Map<String, Class<?>> strToType = new HashMap<String, Class<?>>() {
		private static final long serialVersionUID = -8651755311062618532L; {
		
		// varchar, char, enum, set, text, tinytext, mediumtext, longtext
		put("java.lang.String", java.lang.String.class);
		
		// int, integer, tinyint, smallint, mediumint
		put("java.lang.Integer", java.lang.Integer.class);
		
		// bigint
		put("java.lang.Long", java.lang.Long.class);
		
		// java.util.Data can not be returned
		// java.sql.Date, java.sql.Time, java.sql.Timestamp all extends java.util.Data so getDate can return the three types data
		// put("java.util.Date", java.util.Date.class);
		
		// date, year
		put("java.sql.Date", java.sql.Date.class);
		
		// real, double
		put("java.lang.Double", java.lang.Double.class);
		
		// float
		put("java.lang.Float", java.lang.Float.class);
		
		// bit
		put("java.lang.Boolean", java.lang.Boolean.class);
		
		// time
		put("java.sql.Time", java.sql.Time.class);
		
		// timestamp, datetime
		put("java.sql.Timestamp", java.sql.Timestamp.class);
		
		// decimal, numeric
		put("java.math.BigDecimal", java.math.BigDecimal.class);
		
		// binary, varbinary, tinyblob, blob, mediumblob, longblob
		// qjd project: print_info.content varbinary(61800);
		put("[B", byte[].class);
	}};
	
	public static void build(List<Table> tableList) {
		Table temp = null;
		try {
			//TableMapping tableMapping = TableMapping.me();
			for (Table table : tableList) {
				temp = table;
				doBuild(table);
				//tableMapping.putTable(table);
				TableMapping.me().putTable(table);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (temp != null)
				System.err.println("Can not create Table object, maybe the table " + temp.getName() + " is not exists.");
			throw new RuntimeException(e);
		}
	}
	
	private static void doBuild(Table table) throws SQLException {
		if (table.getPrimaryKey() == null)
			table.setPrimaryKey("id");
		
		String sql = MySqlHelp.forTableBuilderDoBuild(table.getName());
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = C3P0Utils.getConnection();
			ps = con.prepareStatement(sql);
			rs = C3P0Utils.find(con, ps, new Object[0]);
		
			ResultSetMetaData rsmd = rs.getMetaData();
			
			for (int i=1; i<=rsmd.getColumnCount(); i++) {
				String colName = rsmd.getColumnName(i);
				String colClassName = rsmd.getColumnClassName(i);
				
				Class<?> clazz = strToType.get(colClassName);
				if (clazz != null) {
					table.setColumnType(colName, clazz);
				}
				else {
					int type = rsmd.getColumnType(i);
					if (type == Types.BLOB) {
						table.setColumnType(colName, byte[].class);
					}
					else if (type == Types.CLOB || type == Types.NCLOB) {
						table.setColumnType(colName, String.class);
					}
					else {
						table.setColumnType(colName, String.class);
					}
					// core.TypeConverter
					// throw new RuntimeException("You've got new type to mapping. Please add code in " + TableBuilder.class.getName() + ". The ColumnClassName can't be mapped: " + colClassName);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			C3P0Utils.close(rs, ps, con);
		}
		
	}
}

