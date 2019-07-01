package info.shillem.synchronizer.util;

import java.util.Objects;

public class FieldPair {

	private final Field from;
	private Field to;
	
	public FieldPair(Field from, Field to) {
		this.from = Objects.requireNonNull(from, "From cannot be null");
		this.to = Objects.requireNonNull(to, "To cannot be null");
	}
	
	public Field getFrom() {
		return from;
	}
	
	public Field getTo() {
		return to;
	}
	
	@Override
	public String toString() {
		return from + " => " + to;
	}
	
}
