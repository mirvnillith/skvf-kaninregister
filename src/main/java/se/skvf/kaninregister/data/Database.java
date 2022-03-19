package se.skvf.kaninregister.data;

import java.io.IOException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.skvf.kaninregister.drive.GoogleDrive;
import se.skvf.kaninregister.drive.GoogleSpreadsheet;

@Component
public class Database {

	private static final Log LOG = LogFactory.getLog(Database.class);

	static final String NAME = "Kaninregister";

	@Autowired
	private GoogleDrive drive;
	
	private GoogleSpreadsheet spreadsheet;
	
	synchronized GoogleSpreadsheet getSpreadsheet() throws IOException {
		if (spreadsheet == null) {
			spreadsheet = drive.getSpreadsheet(NAME);
			LOG.info(spreadsheet);
		}
		return spreadsheet;
	}
	
	public Table getTable(String name, Collection<String> columns) {
		return new Table(this, name, columns);
	}
}
