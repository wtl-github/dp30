package db;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Model<M extends Model> implements Serializable{

	private static final long serialVersionUID = 4787259288747308523L;
	
	private Map<String, Object> attrs = getAttrsMap();
	private Set<String> modifyFlag;
	
	private Map<String, Object> getAttrsMap() {
		return new HashMap<String, Object>();
	}
	
	public M set(String attr, Object value) {
		if (getTable().hasColumnLabel(attr)) {
			attrs.put(attr, value);
			getModifyFlag().add(attr);
			return (M)this;
		}
		throw new RuntimeException("The attribute name does not exist: " + attr);
	}
	
	public M put(String key, Object value) {
		attrs.put(key, value);
		return (M)this;
	}
	
	private Set<String> getModifyFlag() {
		if (modifyFlag == null) {
			modifyFlag = new HashSet<String>();
		}
		return modifyFlag;
	}
	
	private Table getTable() {
		return TableMapping.me().getTable(getClass());
	}
	
	public String getStr(String attr) {
		return (String)attrs.get(attr);
	}
	
	public Integer getInt(String attr) {
		return (Integer)attrs.get(attr);
	}
	
	public Long getLong(String attr) {
		return (Long)attrs.get(attr);
	}
	
	public BigInteger getBigInteger(String attr) {
		return (BigInteger)attrs.get(attr);
	}
	
	public Date getDate(String attr) {
		return (Date)attrs.get(attr);
	}
	
	public Time getTime(String attr) {
		return (Time)attrs.get(attr);
	}
	
	public Timestamp getTimestamp(String attr) {
		return (Timestamp)attrs.get(attr);
	}
	
	public Double getDouble(String attr) {
		return (Double)attrs.get(attr);
	}
	
	public Float getFloat(String attr) {
		return (Float)attrs.get(attr);
	}
	
	public Boolean getBoolean(String attr) {
		return (Boolean)attrs.get(attr);
	}
	
	public java.math.BigDecimal getBigDecimal(String attr) {
		return (java.math.BigDecimal)attrs.get(attr);
	}
	
	public byte[] getBytes(String attr) {
		return (byte[])attrs.get(attr);
	}
	
	public Number getNumber(String attr) {
		return (Number)attrs.get(attr);
	}
	
	protected Map<String, Object> getAttrs() {
		return attrs;
	}
	
	public M clear() {
		attrs.clear();
		getModifyFlag().clear();
		return (M)this;
	}
	
	public List<M> find(String sql) {
		return find(sql, new Object[0]);
	}
	
	/**
	 * Find model.
	 */
	private List<M> find(String sql, Object... paras) {
		Class<? extends Model> modelClass = getClass();
		checkTableName(modelClass, sql);
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = C3P0Utils.getConnection();
			ps = con.prepareStatement(sql);
			rs = C3P0Utils.find(con, ps, paras);
			
			List<M> result = ModelBuilder.build(rs, modelClass);
			return result;
		} catch (Exception e) {
			//e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			C3P0Utils.close(rs, ps, con);
		}
	}
	
	public M findFirst(String sql, Object... paras) {
		List<M> result = find(sql, paras);
		return result.size() > 0 ? result.get(0) : null;
	}
	
	public M findFirst(String sql) {
		List<M> result = find(sql, new Object[0]);
		return result.size() > 0 ? result.get(0) : null;
	}
	
	public M findById(Object idValue) {
		return findByIdLoadColumns(new Object[]{idValue}, "*");
	}
	
	public M findById(Object... idValues) {
		return findByIdLoadColumns(idValues, "*");
	}
	
	public M findByIdLoadColumns(Object idValue, String columns) {
		return findByIdLoadColumns(new Object[]{idValue}, columns);
	}
	
	public M findByIdLoadColumns(Object[] idValues, String columns) {
		Table table = getTable();
		if (table.getPrimaryKey().length != idValues.length)
			throw new IllegalArgumentException("id values error, need " + table.getPrimaryKey().length + " id value");
		
		String sql = MySqlHelp.forModelFindById(table, columns);
		List<M> result = find(sql, idValues);
		return result.size() > 0 ? result.get(0) : null;
	}
	/**
	 * Save model.
	 */
	public boolean save() {
		Table table = getTable();
		StringBuilder sql = new StringBuilder();
		List<Object> paras = new ArrayList<Object>();
		//判断是那个数据库，现在默认是MYSQL
		MySqlHelp.forModelSave(table, attrs, sql, paras);
		// if (paras.size() == 0)	return false;	// The sql "insert into tableName() values()" works fine, so delete this line
		// --------
		boolean flag = C3P0Utils.save(sql.toString(), paras.toArray());
		getModifyFlag().clear();
		return flag;
	}
	/**
	 * Update model.
	 */
	public boolean update() {
		if (getModifyFlag().isEmpty())
			return false;
		
		Table table = getTable();
		String[] pKeys = table.getPrimaryKey();
		for (String pKey : pKeys) {
			Object id = attrs.get(pKey);
			if (id == null)
				throw new RuntimeException("You can't update model without Primary Key, " + pKey + " can not be null.");
		}
		
		StringBuilder sql = new StringBuilder();
		List<Object> paras = new ArrayList<Object>();
		
		//判断是那个数据库，现在默认是MYSQL
		MySqlHelp.forModelUpdate(table, attrs, getModifyFlag(), sql, paras);
		
		if (paras.size() <= 1) {	// Needn't update
			return false;
		}
		// --------
		boolean flag = C3P0Utils.save(sql.toString(), paras.toArray());
		if (!flag) {
			getModifyFlag().clear();
			return true;
		}
		return false;
	}
	
	/**
	 * Delete model.
	 */
	public boolean delete() {
		Table table = getTable();
		String[] pKeys = table.getPrimaryKey();
		Object[] ids = new Object[pKeys.length];
		for (int i=0; i<pKeys.length; i++) {
			ids[i] = attrs.get(pKeys[i]);
			if (ids[i] == null)
				throw new RuntimeException("You can't delete model without primary key value, " + pKeys[i] + " is null");
		}
		return deleteById(table, ids);
	}
	
	private boolean deleteById(Table table, Object... idValues) {
		String sql = MySqlHelp.forModelDeleteById(table);
		return C3P0Utils.save(sql, idValues);
	}
	
	public boolean deleteById(Object idValue) {
		if (idValue == null)
			throw new IllegalArgumentException("idValue can not be null");
		return deleteById(getTable(), idValue);
	}
	
	private void checkTableName(Class<? extends Model> modelClass, String sql) {
		Table table = TableMapping.me().getTable(modelClass);
		if (! sql.toLowerCase().contains(table.getName().toLowerCase()))
			throw new RuntimeException("The table name: " + table.getName() + " not in your sql.");
	}
}
