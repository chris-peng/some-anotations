package top.lcmatrix.util.permano;

public class PermissionGroup {
	
	PermissionGroup(String[] permissions){
		this.permissions = permissions;
	}
	
	private String[] permissions;

	public String[] permissions() {
		return permissions;
	}
}
