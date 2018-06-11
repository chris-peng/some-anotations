package top.lcmatrix.util.permano;

public interface IPermissionChecker {

	/**
	 * 判断是否拥有单一权限
	 * @param permission
	 * @return
	 */
	public boolean check(String permission);
	
	/**
	 * 最终通过时将执行该方法，然后执行原方法
	 */
	public void onAccept();
	
	/**
	 * 最终不通过时将执行该方法，然后原方法将不被执行，而直接返回null。可以在该方法中抛出异常让外层捕获
	 * @return
	 */
	public void onDeny();
}
