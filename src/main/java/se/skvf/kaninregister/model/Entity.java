package se.skvf.kaninregister.model;

import java.util.HashMap;
import java.util.Map;

import se.skvf.kaninregister.data.Table;

public abstract class Entity {

	private String id;
	
	public Entity setId(String id) {
		this.id = id;
		return this;
	}
	
	public String getId() {
		return id;
	}
	
	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<>();
		if (id != null) {
			map.put(Table.ID, id);
		}
		toMap(map);
		return map;
	}
	
	protected abstract void toMap(Map<String, String> map);
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + id;
	}
}
