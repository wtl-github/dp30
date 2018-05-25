package db;

import java.util.PropertyResourceBundle;

public class Config {

	private static final String PROPNAME = "config";

	private static PropertyResourceBundle resource = null;

	static {
		String prop = System.getProperty("config", PROPNAME);
		resource = (PropertyResourceBundle) PropertyResourceBundle.getBundle(prop);
	}

	public static String getProperty(String name) {
		return resource.getString(name);
	}

}
