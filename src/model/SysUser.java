package model;

import java.util.List;

import db.Model;

@SuppressWarnings("serial")
public class SysUser extends Model<SysUser> {
	public static final SysUser dao = new SysUser();

	public List<SysUser> findList() {
		StringBuffer sql = new StringBuffer("");
		sql.append(" select * from sys_user ");
		return super.find(sql.toString());
	}

}
