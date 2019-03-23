package top.lcmatrix.util.logano.anotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import top.lcmatrix.util.logano.ILogger;
import top.lcmatrix.util.logano.NoneLogger;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

	Class<? extends ILogger> logger() default NoneLogger.class;
	/**
	 * 模块名
	 * @return
	 */
	String module() default "";
	/**
	 * 日志动作，为空时自动填充为方法名 
	 * @return
	 */
	String action() default "";
	/**
	 * 日志详情，用%n$s代替被注解方法的第n（从1开始）个参数（json序列化后的值，若是字符串将带有双引号），为空时将自动根据方法参数生成
	 * @return
	 */
	String detail() default "";
}
