package db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableMapping {
	private final Map<Class<? extends Model<?>>, Table> modelToTableMap = new HashMap<Class<? extends Model<?>>, Table>();
	
	private static TableMapping me = new TableMapping(); 
	
	private TableMapping() {}
	
	public static TableMapping me() {
		return me;
	}
	
	public void putTable(Table table) {
		modelToTableMap.put(table.getModelClass(), table);
	}
	
	public void putTables(List<Table> tables) {
		for (Table t : tables) {
			modelToTableMap.put(t.getModelClass(), t);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public Table getTable(Class<? extends Model> modelClass) {
		Table table = modelToTableMap.get(modelClass);
		if (table == null)
			throw new RuntimeException("The Table mapping of model: " + modelClass.getName() + " not exists. Please add mapping to ActiveRecordPlugin: activeRecordPlugin.addMapping(tableName, YourModel.class).");
		
		return table;
	}
}
