package se.skvf.kaninregister.data;

import static java.util.UUID.randomUUID;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import se.skvf.kaninregister.drive.GoogleSheet;

public class Table {

	private static final Log LOG = LogFactory.getLog(Table.class);

	public static final String ID = "ID";
	
	private final GoogleSheet sheet;
	private final Map<String, String> columns;
	
	public Table(GoogleSheet sheet, Collection<String> columns) throws IOException {
		this.sheet = sheet;
		this.columns = new HashMap<String, String>();
		this.columns.put(ID, sheet.getColumn(ID));
		for (String column : columns) {
			this.columns.put(column, sheet.getColumn(column));
		}
		LOG.info(sheet);
	}

	public void remove(String id) throws IOException {
		sheet.removeRows(columns.get(ID), id::equals);
		
		LOG.info("Removed "+id+" from " + this);
		
	}

	@Override
	public String toString() {
		return sheet.getName();
	}

	public String add(Map<String, String> entity) throws IOException {
		
		String id = randomUUID().toString();
		
		Map<String, String> data = new HashMap<String, String>();
		data.put(columns.get(ID), id);
		entity.forEach((k, v) -> data.put(columns.get(k), v));
		
		sheet.addRow(columns.get(ID), data);
		
		LOG.info("Added " + id + " to " + this);
		entity.put(ID, id);
		return id;
	}
}
