package se.skvf.kaninregister.data;

import java.io.IOException;
import java.util.Collection;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.skvf.kaninregister.drive.GoogleDrive;
import se.skvf.kaninregister.drive.GoogleSpreadsheet;

@Component
public class Database {

	private static final Log LOG = LogFactory.getLog(Database.class);

	private static final String NAME = "Kaninregister";

	@Autowired
	private GoogleDrive drive;
	
	private GoogleSpreadsheet spreadsheet;
	
	@PostConstruct
	public void setup() throws IOException {
		spreadsheet = drive.getSpreadsheet(NAME);
		LOG.info(spreadsheet);
	}
	
	public Table getTable(String name, Collection<String> columns) throws IOException {
		return new Table(spreadsheet.getSheet(name), columns);
	}
}
