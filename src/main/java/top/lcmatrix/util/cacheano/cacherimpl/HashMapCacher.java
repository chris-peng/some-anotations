package top.lcmatrix.util.cacheano.cacherimpl;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import top.lcmatrix.util.cacheano.ICacher;

@Component
public class HashMapCacher implements ICacher{
	
	private static final Map<String, CacheObj> CACHE = new ConcurrentHashMap<>();
	
	@PostConstruct
	private void clean() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					Iterator<Entry<String, CacheObj>> iterator = CACHE.entrySet().iterator();
					while(iterator.hasNext()) {
						Entry<String, CacheObj> ec = iterator.next();
						if(ec.getValue().deadTime <= System.currentTimeMillis()) {
							iterator.remove();
						}
					}
					try {
						Thread.sleep(1800 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	@Override
	public void set(String key, Object value, long ttl) {
		CACHE.put(key, new CacheObj(value, System.currentTimeMillis() + ttl));
	}

	@Override
	public Object get(String key) {
		CacheObj cacheObj = CACHE.get(key);
		if(cacheObj != null) {
			if(cacheObj.deadTime > System.currentTimeMillis()) {
				return cacheObj.o;
			}else {
				CACHE.remove(key);
			}
		}
		return null;
	}
	
	@Override
	public boolean isCached(String key) {
		return CACHE.containsKey(key);
	}

	private class CacheObj {
		private Object o;
		private long deadTime;
		CacheObj(Object o, long deadTime){
			this.o = o;
			this.deadTime = deadTime;
		}
	}
}
