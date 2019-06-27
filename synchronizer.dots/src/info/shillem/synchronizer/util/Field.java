package info.shillem.synchronizer.util;

public class Field {

	public enum Type {
		STRING, INTEGER, DOUBLE, DATE		
	}

	private final String name;
	private final Type type;
	
	public Field(String name, Type type) {
		this.name = name;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	
	public Type getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return name + ":" + type;
	}
	
}
