package db;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Table {
	private String name;
	private String[] primaryKey = null;
	private Class<? extends Model<?>> modelClass;
	
	private Map<String, Class<?>> columnTypeMap = new HashMap<>();	// config.containerFactory.getAttrsMap();
	
	public Table() {
		super();
	}

	public Table(String name, String primaryKey, Class<? extends Model<?>> modelClass) {
		super();
		this.name = name;
		setPrimaryKey(primaryKey.trim());
		this.modelClass = modelClass;
	}

	public Map<String, Class<?>> getColumnTypeMap() {
		return Collections.unmodifiableMap(columnTypeMap);
	}

	public boolean hasColumnLabel(String columnLabel) {
		return columnTypeMap.containsKey(columnLabel);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getPrimaryKey() {
		return primaryKey;
	}

	void setPrimaryKey(String primaryKey) {
		String[] arr = primaryKey.split(",");
		for (int i=0; i<arr.length; i++)
			arr[i] = arr[i].trim();
		this.primaryKey = arr;
	}
	
	public Class<? extends Model<?>> getModelClass() {
		return modelClass;
	}

	public void setModelClass(Class<? extends Model<?>> modelClass) {
		this.modelClass = modelClass;
	}
	
	void setColumnType(String columnLabel, Class<?> columnType) {
		columnTypeMap.put(columnLabel, columnType);
	}
	
	public Class<?> getColumnType(String columnLabel) {
		return columnTypeMap.get(columnLabel);
	}
}
