package se.skvf.kaninregister.data;

import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;

import se.skvf.kaninregister.drive.GoogleSheet;

public class Table {

	private static final Log LOG = LogFactory.getLog(Table.class);

	public static final String ID = "ID";
	
	private final Database database;
	private final String name;
	private final List<String> tableColumns;
	private GoogleSheet sheet;
	private Map<String, String> attributeColumns;
	private Map<String, String> columnAttributes;

	public static final Map<String, Predicate<String>> ALL = Maps.toMap(singleton(ID), k -> ((Predicate<String>)Objects::nonNull));
	
	Table(Database database, String name, Collection<String> columns) {
		this.database = database;
		this.name = name;
		tableColumns = new ArrayList<>(columns);
		tableColumns.add(ID);
		tableColumns.addAll(columns);
	}
	
	private synchronized GoogleSheet getSheet() throws IOException {
		if (sheet == null) {
			sheet = database.getSpreadsheet().getSheet(name);
			attributeColumns = sheet.getColumns(tableColumns);
			columnAttributes = attributeColumns.entrySet().stream().collect(toMap(Map.Entry::getValue, Map.Entry::getKey));
			LOG.info(sheet);
		}
		return sheet;
	}

	public void remove(String id) throws IOException {
		getSheet().removeRow(attributeColumns.get(ID), id);
		
		LOG.info("Removed " + id + " from " + this);
		
	}

	@Override
	public synchronized String toString() {
		if (sheet == null) {
			return name;
		} else {
			return sheet.getName();
		}
	}

	public String add(Map<String, String> entity) throws IOException {
		
		String id = randomUUID().toString();
		
		getSheet().addRow(attributeColumns.get(ID), mapEntity(entity, id));
		
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
		return getSheet().findRows(attributeColumns.get(ID), ids).stream().map(this::mapRow).collect(toList());
	}
	
	public Collection<Map<String, String>> find(Map<String, Predicate<String>> filters) throws IOException {
		return getSheet().findRows(mapFilters(filters)).stream().map(this::mapRow).collect(toList());
	}
	
	private Map<String, Predicate<String>> mapFilters(Map<String, Predicate<String>> filters) {
		return filters.entrySet().stream().collect(toMap(e -> attributeColumns.get(e.getKey()), Map.Entry::getValue));
	}
	
	private Map<String, String> mapRow(Map<String, String> row) {
		return row.entrySet().stream().collect(toMap(e -> columnAttributes.get(e.getKey()), Map.Entry::getValue));
	}

	public void update(Map<String, String> entity) throws IOException {
		
		String id = entity.get(ID);
		
		getSheet().updateRow(attributeColumns.get(ID), id, mapEntity(entity, null));
		
		LOG.info("Updated " + id + " in " + this);
	}
}
