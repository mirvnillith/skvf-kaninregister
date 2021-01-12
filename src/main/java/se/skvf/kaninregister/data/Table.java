package se.skvf.kaninregister.data;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import se.skvf.kaninregister.drive.GoogleSheet;

public class Table {

	private static final Log LOG = LogFactory.getLog(Table.class);

	public static final String ID = "ID";
	
	private final GoogleSheet sheet;
	private final Map<String, String> attributeColumns;
	private final Map<String, String> columnAttributes;
	
	public Table(GoogleSheet sheet, Collection<String> columns) throws IOException {
		this.sheet = sheet;
		this.attributeColumns = new HashMap<String, String>();
		this.attributeColumns.put(ID, sheet.getColumn(ID));
		for (String column : columns) {
			this.attributeColumns.put(column, sheet.getColumn(column));
		}
		columnAttributes = attributeColumns.entrySet().stream().collect(toMap(Map.Entry::getValue, Map.Entry::getKey));
		LOG.info(sheet);
	}

	public void remove(String id) throws IOException {
		sheet.removeRow(attributeColumns.get(ID), id);
		
		LOG.info("Removed " + id + " from " + this);
		
	}

	@Override
	public String toString() {
		return sheet.getName();
	}

	public String add(Map<String, String> entity) throws IOException {
		
		String id = randomUUID().toString();
		
		sheet.addRow(attributeColumns.get(ID), mapEntity(entity, id));
		
		LOG.info("Added " + id + " to " + this);
		entity.put(ID, id);
		return id;
	}

	private Map<String, String> mapEntity(Map<String, String> entity, String id) {
		Map<String, String> data = new HashMap<String, String>();
		entity.forEach((k, v) -> data.put(attributeColumns.get(k), v));
		if (id != null) {
			data.put(attributeColumns.get(ID), id);
		} else {
			data.remove(attributeColumns.get(ID));
		}
		return data;
	}

	public Collection<Map<String, String>> find(Collection<String> ids) throws IOException {
		return sheet.findRows(attributeColumns.get(ID), ids).stream().map(this::mapRow).collect(toList());
	}
	
	public Collection<Map<String, String>> find(Map<String, Predicate<String>> filters) throws IOException {
		return sheet.findRows(mapFilters(filters)).stream().map(this::mapRow).collect(toList());
	}
	
	private Map<String, Predicate<String>> mapFilters(Map<String, Predicate<String>> filters) {
		return filters.entrySet().stream().collect(toMap(e -> attributeColumns.get(e.getKey()), Map.Entry::getValue));
	}
	
	private Map<String, String> mapRow(Map<String, String> row) {
		return row.entrySet().stream().collect(toMap(e -> columnAttributes.get(e.getKey()), Map.Entry::getValue));
	}

	public void update(Map<String, String> entity) throws IOException {
		
		String id = entity.get(ID);
		
		sheet.updateRow(attributeColumns.get(ID), id, mapEntity(entity, null));
		
		LOG.info("Updated " + id + " in " + this);
		
	}
}
