package leo;

import java.util.ArrayList;
import java.util.List;

import db.Table;
import db.TableBuilder;
import model.SysUser;

public class Main {

	static{
		List<Table> tableList = new ArrayList<>();
		tableList.add(new Table("sys_user", "id", SysUser.class));
		TableBuilder.build(tableList);
	}
	
	public static void main(String[] args) {
		
		while (true) {
			try {
				List<SysUser> users = SysUser.dao.findList();
				System.out.println(users.size());
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
