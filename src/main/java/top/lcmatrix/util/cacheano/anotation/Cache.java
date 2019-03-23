package top.lcmatrix.util.cacheano.anotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import top.lcmatrix.util.cacheano.ICacher;
import top.lcmatrix.util.cacheano.NoneCacher;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {

	/**
	 * 生存时间，单位毫秒，默认为10分钟
	 * @return
	 */
	long ttl() default 600000;
	Class<? extends ICacher> cacher() default NoneCacher.class;
	/**
	 * 构造key的格式化字符串，用%n$s代替被注解方法的第n（从1开始）个参数（json序列化后的值，若是字符串将带有双引号），为空时将自动生成key（方法签名+参数json序列化后的值）
	 * @return
	 */
	String keyFormatter() default "";
}
