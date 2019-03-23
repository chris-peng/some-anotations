package top.lcmatrix.util.cacheano;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import top.lcmatrix.util.cacheano.anotation.Cache;

@Aspect
@Component
public class CacheAnoAspect implements ApplicationContextAware{
	
	public static final String AUTO_KEY_PREFIX = "LcMatrixCache:";
	
	@Autowired(required = false)
	private ICacher cacher;
	
	private ApplicationContext applicationContext;
	
	public static int KEY_JSON_SERIALIZER_FEATURE;
    static {
        int features = 0;
        features &= ~SerializerFeature.QuoteFieldNames.getMask();
        features |= SerializerFeature.SkipTransientField.getMask();
        features |= SerializerFeature.WriteEnumUsingName.getMask();
        features |= SerializerFeature.SortField.getMask();
        features |= SerializerFeature.MapSortField.getMask();
        features &= ~SerializerFeature.PrettyFormat.getMask();

        KEY_JSON_SERIALIZER_FEATURE = features;
    }
    
	@Pointcut("@annotation(cache)")
	public void methodCacheAnnotation(Cache cache){}

	@Around("methodCacheAnnotation(cache)")
	public Object doAroundMethod(ProceedingJoinPoint joinPoint, Cache cache) 
			throws Throwable{
		String keyFormatter = cache.keyFormatter();
		String key;
		if("".equals(keyFormatter)) {
			key = getAutoKey(joinPoint, cache);
		}else {
			key = getFormatKey(joinPoint, cache);
		}
		ICacher cacher = getCacher(cache);
		Object object = cacher.get(key);
		if(!cacher.isCached(key)) {
			object = joinPoint.proceed();
			cacher.set(key, object, cache.ttl());
		}
		return object;
	}
	
	private String getAutoKey(ProceedingJoinPoint joinPoint, Cache cache) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		String invokeKey = signature.toString() + ":" + JSON.toJSONString(joinPoint.getArgs(), KEY_JSON_SERIALIZER_FEATURE);
		return AUTO_KEY_PREFIX + invokeKey;
	}
	
	private String getFormatKey(ProceedingJoinPoint joinPoint, Cache cache) {
		Object[] args = joinPoint.getArgs();
		if(args.length > 0) {
			Object[] argJsonStrs = new Object[args.length];
			for(int i = 0; i < args.length; i++) {
				argJsonStrs[i] = JSON.toJSONString(args[i], KEY_JSON_SERIALIZER_FEATURE);
			}
			return String.format(cache.keyFormatter(), argJsonStrs);
		}
		return cache.keyFormatter();
	}
	
	private ICacher getCacher(Cache cache) {
		ICacher rcacher = null;
		if(NoneCacher.class.equals(cache.cacher())) {
			rcacher = cacher;
		}else {
			rcacher = applicationContext.getBean(cache.cacher());
		}
		if(rcacher == null) {
			throw new RuntimeException("指定或默认的cacher为null，请指定cacher或提供一个默认的cacher");
		}
		return rcacher;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
