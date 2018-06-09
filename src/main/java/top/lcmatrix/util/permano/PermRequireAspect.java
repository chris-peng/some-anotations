package top.lcmatrix.util.permano;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import top.lcmatrix.util.permano.anotation.RequireAnyPermission;
import top.lcmatrix.util.permano.anotation.RequirePermission;

@Aspect
@Component
public class PermRequireAspect {
	
	@Autowired(required = false)
	private IPermissionChecker permissionChcker;
	
	@Pointcut("@within(requirePermission)")
	public void typeRequirePermissionAnnotation(RequirePermission requirePermission){}
	
	@Pointcut("@annotation(requirePermission)")
	public void methodRequirePermissionAnnotation(RequirePermission requirePermission){}
	
	@Pointcut("@within(requireAnyPermission)")
	public void typeRequireAnyPermissionAnnotation(RequireAnyPermission requireAnyPermission){}
	
	@Pointcut("@annotation(requireAnyPermission)")
	public void methodRequireAnyPermissionAnnotation(RequireAnyPermission requireAnyPermission){}
	
	@Around("typeRequirePermissionAnnotation(requirePermission)")
	public Object doAroundInType(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) 
			throws Throwable{
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		if(signature.getMethod().getAnnotation(RequirePermission.class) != null
				|| signature.getMethod().getAnnotation(RequireAnyPermission.class) != null){
			//如果该切入点方法上还有RequirePermission或RequireAnyPermission注解，则以方法的注解为准。
			return joinPoint.proceed();
		}
		System.out.println(toString(requirePermission));
		if(check(new PermissionGroup[]{new PermissionGroup(requirePermission.value())})){
			return joinPoint.proceed();
		}
		return null;
	}
	
	@Around("methodRequirePermissionAnnotation(requirePermission)")
	public Object doAroundMethod(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) 
			throws Throwable{
		System.out.println(toString(requirePermission));
		if(check(new PermissionGroup[]{new PermissionGroup(requirePermission.value())})){
			return joinPoint.proceed();
		}
		return null;
	}
	
	@Around("typeRequireAnyPermissionAnnotation(requireAnyPermission)")
	public Object doAroundInType(ProceedingJoinPoint joinPoint, RequireAnyPermission requireAnyPermission) 
			throws Throwable{
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		if(signature.getMethod().getAnnotation(RequirePermission.class) != null
				|| signature.getMethod().getAnnotation(RequireAnyPermission.class) != null){
			//如果该切入点方法上还有RequirePermission或RequireAnyPermission注解，则以方法的注解为准。
			return joinPoint.proceed();
		}
		System.out.println(toString(requireAnyPermission));
		if(check(trans2PermissionGroups(requireAnyPermission.value()))){
			return joinPoint.proceed();
		}
		return null;
	}
	
	@Around("methodRequireAnyPermissionAnnotation(requireAnyPermission)")
	public Object doAroundMethod(ProceedingJoinPoint joinPoint, RequireAnyPermission requireAnyPermission) 
			throws Throwable{
		System.out.println(toString(requireAnyPermission));
		if(check(trans2PermissionGroups(requireAnyPermission.value()))){
			return joinPoint.proceed();
		}
		return null;
	}
	
	private PermissionGroup[] trans2PermissionGroups(RequirePermission[] requirePermissions){
		PermissionGroup[] permissionGroups = new PermissionGroup[requirePermissions.length];
		for(int i = 0; i < requirePermissions.length; i++){
			permissionGroups[i] = new PermissionGroup(requirePermissions[i].value());
		}
		return permissionGroups;
	}
	
	/**
	 * 判断是否拥有指定的所有权限组中的任意一组权限。权限组（或者某组的权限）为空，表示（该组）不需要任何权限
	 * @param groups
	 * @return		若拥有参数指定的权限组中的任意一组权限，就应该返回true，否则返回false
	 */
	private boolean check(PermissionGroup[] permissionGroups) {
		boolean pass = false;
		if(permissionGroups.length == 0){
			pass = true;
		}
		for(PermissionGroup pg : permissionGroups){
			boolean groupPass = true;
			String[] permissions = pg.permissions();
			if(permissions.length == 0){
				pass = true;
				break;
			}
			for(String permission : permissions){
				groupPass = groupPass && (permissionChcker != null ? permissionChcker.check(permission) : false/*如果没有实现judger，则默认不通过*/);
			}
			pass = pass || groupPass;
			if(pass){
				break;
			}
		}
		if(permissionChcker != null){
			if(pass){
				permissionChcker.onAccept();
			}else{
				permissionChcker.onDeny();
			}
		}
		return pass;
	}
	
	private String toString(RequirePermission requirePermission){
		StringBuilder sb = new StringBuilder("requirePermission:  ");
		for(String p : requirePermission.value()){
			sb.append(p).append(",");
		}
		if(sb.toString().endsWith(",")){
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}
	
	private String toString(RequireAnyPermission requireAnyPermission){
		StringBuilder sb = new StringBuilder("requireAnyPermission:  ");
		for(RequirePermission requirePermission : requireAnyPermission.value()){
			sb.append("[");
			for(String p : requirePermission.value()){
				sb.append(p).append(",");
			}
			if(sb.toString().endsWith(",")){
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.append("],");
		}
		if(sb.toString().endsWith(",")){
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}
}
