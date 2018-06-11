# LcMatrixPermissionAnotation
包含几个简单的注解，用来快速实现接口（方法）级的权限控制，上手简单，可读性较强。可单独使用，也可作为其他安全框架的补充。基于Spring AOP。

###如何使用：

1. 引入本项目
2. 你需要提供自己的权限判断逻辑。实现top.lcmatrix.util.permano.IPermissionChecker接口，如：
    @Service
    public class MyPermissionChecker implements IPermissionChecker{
    
    	@Override
    	public boolean check(String permission) {
    		HttpSession session = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest().getSession();
    		User loginUser = (User) session.getAttribute("LOGIN_USER");
    		if(loginUser == null){
    			return false;
    		}
    		if(loginUser.getPermissions() == null){
    			return false;
    		}
    		for(String p : loginUser.getPermissions()){
    			if(p.equals(permission)){
    				return true;
    			}
    		}
    		return false;
    	}
    
    	@Override
    	public void onAccept() {
    	}
    
    	@Override
    	public void onDeny() {
    		throw new NoPermissionException();
    	}
    
    }
3. 然后就可以用下面几个注解来保护你的接口了：

    * @RequirePermission 满足指定的所有权限才能通过，示例：

            @RequirePermission("createUser")
            public void createUser(...)    //拥有"createUser"权限才允许调用该方法

            @RequirePermission({"createUser","updateUser"})
            public void userMgr(...)    //同时拥有"createUser"和"updateUser"权限才允许调用该方法
    * @RequireAnyPermission  满足指定的任意一项权限即可通过，示例：
     
            @RequireAnyPermission({@RequirePermission("createUser"),@RequirePermission("updateUser")})
            public void userMgr(...)    //拥有"createUser"和"updateUser"其中一项权限即可调用该方法
