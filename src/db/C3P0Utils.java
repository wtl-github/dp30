package db;

import java.beans.PropertyVetoException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class C3P0Utils {
	
	private static ComboPooledDataSource dataSource = null;
	
    private static Logger logger = Logger.getLogger(C3P0Utils.class);

	private static String DRIVER = null;
	private static String URL = null;
	private static String USER = null;
	private static String PASSWD = null;
	private static String INITIALPOOLSIZE = null;
	private static String ACQUIREINCREMENT = null;
	private static String MAXPOOLSIZE = null;
	private static String SQL = "select * from ";// 数据库操作

	/**
	 * 静态代码块中（只加载一次）
	 */
	static {
		try {
			Properties props = new Properties();
			InputStream in = C3P0Utils.class.getResourceAsStream("/db.properties");
			props.load(in);
			URL = props.getProperty("url");
			USER = props.getProperty("username");
			PASSWD = props.getProperty("password");
			DRIVER = props.getProperty("driverClass");
			INITIALPOOLSIZE = props.getProperty("initialPoolSize");
			ACQUIREINCREMENT = props.getProperty("acquireIncrement");
			MAXPOOLSIZE = props.getProperty("maxPoolSize");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("驱程程序注册出错");
		}
	}
	
	/**
     * 获取数据源
     * @return 连接池
     */
    public static DataSource getDataSource(){
    	try {
    		if(dataSource == null){
    			dataSource = new ComboPooledDataSource();  
    			dataSource.setDriverClass(DRIVER);
    			dataSource.setJdbcUrl(URL);
    	    	dataSource.setUser(USER);
    	    	dataSource.setPassword(PASSWD);
    	    	dataSource.setMinPoolSize(Integer.parseInt(INITIALPOOLSIZE.trim()));
    	    	dataSource.setAcquireIncrement(Integer.parseInt(ACQUIREINCREMENT.trim()));
    	    	dataSource.setMaxPoolSize(Integer.parseInt(MAXPOOLSIZE.trim()));
    		}
	    	return dataSource ;
		} catch (PropertyVetoException e) {
			logger.error("DataSource Exception", e);
			logger.error("Server stop!");
			System.exit(-1);
			return null ;
		}
    }
    
	/**
	 * 使用连接池返回一个连接对象
	 * @return
	 * @throws SQLException
	 */
	public static Connection getConnection() {
		try {
			return C3P0Utils.getDataSource().getConnection();
		} catch (SQLException e) {
			logger.error("Connection Exception", e);
			System.out.println("Connection Exception");
			return null;
		}
	}
	
	/**
	 * 关闭数据库连接
	 */
	public static void close(ResultSet rs) {
		try {
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
			logger.error("close connection failure", e);
		}
	}
	
	public static void close(ResultSet rs, Connection con) {
		try {
			if (rs != null)
				rs.close();
			if (con != null)
				con.close();
		} catch (SQLException e) {
			logger.error("close connection failure", e);
		}
	}
	
	public static void close(PreparedStatement ps, Connection con) {
		try {
			if (ps != null)
				ps.close();
			if (con != null)
				con.close();
		} catch (SQLException e) {
			logger.error("close connection failure", e);
		}
	}
	
	public static void close(ResultSet rs, PreparedStatement ps, Connection con) {
		try {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
			if (con != null)
				con.close();
		} catch (SQLException e) {
			logger.error("close connection failure", e);
		}
	}
	
	public static void rollback(Connection con) {
		try {
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// 数据插入或更新
	public static boolean save(String sql, Object[] paras) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean b = true;
		try {
			con = getConnection();
			ps = con.prepareStatement(sql);
			for (int i = 0; i < paras.length; i++) {
				ps.setObject(i + 1, paras[i]);
			}
			if (ps.executeUpdate() == -1) {
				b = false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, ps, con);
		}
		return b;
	}
	/**
	 * 批量插入  ???
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	/*public static void batchInsert() {
        long start = System.currentTimeMillis();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
        	con = getConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement("insert into test1 values(?,?)");
			for (int i = 0; i < 1000000; i++) {//100万条数据
	        	ps.setInt(1, i);
	        	ps.setString(2, "test");
	        	ps.addBatch();
	            if(i%1000==0){
	            	ps.executeBatch();
	            }
	        }
	        ps.executeBatch();
	        con.commit();
		} catch (SQLException e) {
			rollback(con);
			e.printStackTrace();
		} finally {
			close(rs, ps, con);
		}
        long end = System.currentTimeMillis();
        System.out.println("批量插入需要时间:"+(end - start)); //批量插入需要时间:24675
    }*/
	
	// 查询
	public static ResultSet find(Connection con, PreparedStatement ps, Object[] paras) {
		ResultSet rs = null;
		try {
			for (int i = 0; i < paras.length; i++) {
				ps.setObject(i + 1, paras[i]);
			}
			rs = ps.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return rs;
	}
	
	/*public static ResultSet find(String sql, Object[] paras) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			ps = con.prepareStatement(sql);
			for (int i = 0; i < paras.length; i++) {
				ps.setObject(i + 1, paras[i]);
			}
			rs = ps.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return rs;
	}*/

	/**
	 * 获取数据库下的所有表名
	 * @throws SQLException 
	 */
	public static List<String> getTableNames() {
		List<String> tableNames = new ArrayList<>();
		Connection con = getConnection();
		ResultSet rs = null;
		try {
			// 获取数据库的元数据
			DatabaseMetaData db = con.getMetaData();
			// 从元数据中获取到所有的表名
			rs = db.getTables(null, null, null, new String[] { "TABLE" });
			while (rs.next()) {
				tableNames.add(rs.getString(3));
			}
		} catch (SQLException e) {
			logger.error("getTableNames failure", e);
		} finally {
			close(rs, con);
		}
		return tableNames;
	}

	/**
	 * 获取表中所有字段名称
	 * 
	 * @param tableName
	 *            表名
	 * @return
	 * @throws SQLException 
	 */
	public static List<String> getColumnNames(String tableName) {
		List<String> columnNames = new ArrayList<>();
		// 与数据库的连接
		Connection con = getConnection();
		PreparedStatement ps = null;
		String tableSql = SQL + tableName;
		try {
			ps = con.prepareStatement(tableSql);
			// 结果集元数据
			ResultSetMetaData rsmd = ps.getMetaData();
			// 表列数
			int size = rsmd.getColumnCount();
			for (int i = 0; i < size; i++) {
				columnNames.add(rsmd.getColumnName(i + 1));
			}
		} catch (SQLException e) {
			logger.error("getColumnNames failure", e);
		} finally {
			close(ps, con);
		}
		return columnNames;
	}

	/**
	 * 获取表中所有字段类型
	 * 
	 * @param tableName
	 * @return
	 * @throws SQLException 
	 */
	public static List<String> getColumnTypes(String tableName) {
		List<String> columnTypes = new ArrayList<>();
		// 与数据库的连接
		Connection con = getConnection();
		PreparedStatement ps = null;
		String tableSql = SQL + tableName;
		try {
			ps = con.prepareStatement(tableSql);
			// 结果集元数据
			ResultSetMetaData rsmd = ps.getMetaData();
			// 表列数
			int size = rsmd.getColumnCount();
			for (int i = 0; i < size; i++) {
				columnTypes.add(rsmd.getColumnTypeName(i + 1));
			}
		} catch (SQLException e) {
			logger.error("getColumnTypes failure", e);
		} finally {
			close(ps, con);
		}
		return columnTypes;
	}

	/**
	 * 获取表中字段的所有注释
	 * 
	 * @param tableName
	 * @return
	 */
	public static List<String> getColumnComments(String tableName) {
		List<String> columnComments = new ArrayList<>();// 列名注释集合
		// 与数据库的连接
		Connection con = getConnection();
		PreparedStatement ps = null;
		String tableSql = SQL + tableName;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(tableSql);
			rs = ps.executeQuery("show full columns from " + tableName);
			while (rs.next()) {
				columnComments.add(rs.getString("Comment"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, ps, con);
		}
		return columnComments;
	}

	public static void main(String[] args) {
		List<String> tableNames = getTableNames();
		System.out.println("tableNames:" + tableNames);
		for (String tableName : tableNames) {
			System.out.println("ColumnNames:" + getColumnNames(tableName));
			System.out.println("ColumnTypes:" + getColumnTypes(tableName));
			System.out.println("ColumnComments:" + getColumnComments(tableName));
		}
	}
	
}
