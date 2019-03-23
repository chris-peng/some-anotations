package top.lcmatrix.util.cacheano;

public class NoneCacher implements ICacher{

	@Override
	public void set(String key, Object value, long ttl) {
	}

	@Override
	public Object get(String key) {
		return null;
	}

	@Override
	public boolean isCached(String key) {
		return false;
	}

}
