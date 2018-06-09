package top.lcmatrix.util.permano.anotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要其中一项权限（a或b）。一般来说，空数组表示不需要任何权限
 * @author chris
 *
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAnyPermission {

	RequirePermission[] value();
}
