package test.util;

public abstract class Id {
	Object id;
	
	public Object getId() { return id; }
	
	@Override
	public boolean equals(Object other) {
		return other instanceof Id && id.equals( ((Id)other).getId() ) ? true : false;
	}
}
