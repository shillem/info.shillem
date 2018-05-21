package info.shillem.util;

public class StandardBuilder<T> implements Builder<T> {

	private Class<? extends T> cls;

	public StandardBuilder(Class<? extends T> cls) {
		this.cls = cls;
	}

	@Override
	public T build() {
		try {
			return cls.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
