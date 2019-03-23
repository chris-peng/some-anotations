package top.lcmatrix.util.logano.anotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import top.lcmatrix.util.logano.ILogger;
import top.lcmatrix.util.logano.NoneLogger;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogModule {

	Class<? extends ILogger> logger() default NoneLogger.class;
	
	/**
	 * 模块名
	 * @return
	 */
	String value();
}
