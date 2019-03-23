package top.lcmatrix.util.cacheano;

public interface ICacher {
	public void set(String key, Object value, long ttl);
	public Object get(String key);
	public boolean isCached(String key);
}
