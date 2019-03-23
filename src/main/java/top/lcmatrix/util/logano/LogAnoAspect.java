package top.lcmatrix.util.logano;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import top.lcmatrix.util.logano.anotation.Log;
import top.lcmatrix.util.logano.anotation.LogIgnore;
import top.lcmatrix.util.logano.anotation.LogModule;

@Aspect
@Component
public class LogAnoAspect implements ApplicationContextAware{
	
	@Autowired(required = false)
	private ILogger logger;
	
	private ApplicationContext applicationContext;
	
	private static Logger sysLogger = LoggerFactory.getLogger(LogAnoAspect.class);
	
	public static int DETAIL_JSON_SERIALIZER_FEATURE;
    static {
        int features = 0;
        features &= ~SerializerFeature.QuoteFieldNames.getMask();
        features |= SerializerFeature.SkipTransientField.getMask();
        features |= SerializerFeature.WriteEnumUsingName.getMask();
        features &= ~SerializerFeature.PrettyFormat.getMask();

        DETAIL_JSON_SERIALIZER_FEATURE = features;
    }
    
	@Pointcut("@annotation(log)")
	public void methodLogAnnotation(Log log){}

	@Around("methodLogAnnotation(log)")
	public Object doAroundMethod(ProceedingJoinPoint joinPoint, Log log) 
			throws Throwable{
		Object object = null;
		try {
			object = joinPoint.proceed();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			log(joinPoint, log);
		} catch (Exception e) {
			sysLogger.warn("记录应用日志出错！", e);
		}
		return object;
	}
	
	private void log(ProceedingJoinPoint joinPoint, Log log) {
		String action = log.action();
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		if(action == null || "".equals(action)) {
			action = methodSignature.getName();
		}
		String detail = log.detail();
		if(detail == null || "".equals(detail)) {
			Map<String, Object> detailMap = new HashMap<>();
			String[] parameterNames = methodSignature.getParameterNames();
			Object[] args = joinPoint.getArgs();
			Annotation[][] parameterAnnotations = methodSignature.getMethod().getParameterAnnotations();
			for(int i = 0; i < parameterNames.length; i++) {
				Annotation[] annotations = parameterAnnotations[i];
				boolean isIgnored = false;
				for(Annotation a : annotations) {
					if(a.annotationType() == LogIgnore.class) {
						isIgnored = true;
						break;
					}
				}
				if(!isIgnored) {
					detailMap.put(parameterNames[i], args[i]);
				}
			}
			detail = JSON.toJSONString(detailMap, DETAIL_JSON_SERIALIZER_FEATURE);
		}else {
			Object[] args = joinPoint.getArgs();
			if(args.length > 0) {
				Object[] argJsonStrs = new Object[args.length];
				for(int i = 0; i < args.length; i++) {
					argJsonStrs[i] = JSON.toJSONString(args[i], DETAIL_JSON_SERIALIZER_FEATURE);
				}
				detail = String.format(detail, argJsonStrs);
			}
		}
		getLogger(joinPoint, log).log(getModule(joinPoint, log), action, detail);
	}
	
	private ILogger getLogger(ProceedingJoinPoint joinPoint, Log log) {
		ILogger rlogger = null;
		if(NoneLogger.class.equals(log.logger())) {
			rlogger = getLogModuleLogger(joinPoint);
		}else {
			rlogger = applicationContext.getBean(log.logger());
		}
		if(rlogger == null) {
			throw new RuntimeException("指定或默认的logger为null，请指定logger或提供一个默认的logger");
		}
		return rlogger;
	}
	
	private ILogger getLogModuleLogger(ProceedingJoinPoint joinPoint) {
		ILogger rlogger = null;
		LogModule logModule = joinPoint.getTarget().getClass().getAnnotation(LogModule.class);
		if(logModule != null) {
			if(NoneLogger.class.equals(logModule.logger())) {
				rlogger = logger;
			}else {
				rlogger = applicationContext.getBean(logModule.logger());
			}
		}else {
			rlogger = logger;
		}
		return rlogger;
	}
	
	private String getModule(ProceedingJoinPoint joinPoint, Log log) {
		String module = log.module();
		if(module == null || "".equals(module)) {
			LogModule logModule = joinPoint.getTarget().getClass().getAnnotation(LogModule.class);
			if(logModule != null) {
				module = logModule.value();
			}
		}
		return module;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
