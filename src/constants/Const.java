package constants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Const {
	
	public static final Map<String, String> MAP = new ConcurrentHashMap<>();  
	
	public static class ThreadStatus {
        public static final String CLOSE = "0";
        public static final String OPEN = "1";
    }
	
    public static class ResultCode {
        public static final String SUCCESS = "200";
        public static final String FAILURE = "201";
        public static final String NOPOWER = "401";
    }

    public static class ResultDesc {
        public static final String SUCCESS = "success";
        public static final String FAILURE = "error";
        public static final String NOPOWER = "No permission!";
    }
}
