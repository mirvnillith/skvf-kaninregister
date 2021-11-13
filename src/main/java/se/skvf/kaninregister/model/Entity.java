package se.skvf.kaninregister.model;

import static se.skvf.kaninregister.data.Table.ID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public abstract class Entity<T extends Entity<?>> {

	protected static String toString(boolean flag) {
		return flag ? "Ja" : "Nej";
	}

	protected static boolean booleanFromString(String string) {
		return "Ja".equalsIgnoreCase(string);
	}

	private String id;
	
	@SuppressWarnings("unchecked")
	public T setId(String id) {
		this.id = id;
		return (T) this;
	}
	
	public String getId() {
		return id;
	}
	
	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<>();
		if (id != null) {
			map.put(ID, id);
		}
		toMap(map);
		return map;
	}
	
	protected abstract void toMap(Map<String, String> map);
	
	@SuppressWarnings("unchecked")
	protected T fromMap(Map<String, String> map) {
		id = map.get(ID);
		return (T) this;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + id;
	}
	
	public static Map<String, Predicate<String>> by(String field, String value) {
		return by(field, value::equals);
	}
	
	public static Map<String, Predicate<String>> by(String field, Predicate<String> predicate) {
		Map<String, Predicate<String>> filter = new HashMap<>();
		filter.put(field, predicate);
		return filter;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		
		if (getClass().isInstance(obj)) {
			Entity<?> that = (Entity<?>) obj;
			return this.id.equals(that.id);
		}
		
		return false;
	}

	@SuppressWarnings("unchecked")
	protected T setFromMap(Map<String, String> map, List<String> columns, List<BiConsumer<T, String>> setters) {
		if (setters.size() != columns.size()) {
			throw new IllegalStateException("Values do not match columns: "+columns);			
		}
		for (int i=0; i<columns.size(); i++) {
			setters.get(i).accept((T) this, map.get(columns.get(i)));
		}
		
		return (T) this;
	}

	protected static void addToMap(Map<String, String> map, List<String> columns, List<String> values) {
		if (values.size() != columns.size()) {
			throw new IllegalStateException("Values do not match columns: "+values+" vs "+columns);			
		}
		for (int i=0; i<columns.size(); i++) {
			map.put(columns.get(i), values.get(i));
		}
	}
}
