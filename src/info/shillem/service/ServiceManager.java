package info.shillem.service;

public interface ServiceManager {
	
	<T extends Service> T get(Class<T> cls);

}
