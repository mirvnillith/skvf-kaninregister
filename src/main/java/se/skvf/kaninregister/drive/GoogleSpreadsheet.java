package se.skvf.kaninregister.drive;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.UpdateSheetPropertiesRequest;

public class GoogleSpreadsheet {

	private static final Log LOG = LogFactory.getLog(GoogleSpreadsheet.class);

	private static final Object DEFAULT_SHEET = "Sheet1";
	
	final String id;
	private final String name;
	final Sheets api;
	
	GoogleSpreadsheet(String id, String name, Sheets api) {
		this.id = id;
		this.name = name;
		this.api = api;
	}

	public GoogleSheet getSheet(String sheetName) throws IOException {
		Spreadsheet spreadsheet = api.spreadsheets().get(id)
				.setFields("sheets(properties)")
				.execute();
		
		for (Sheet sheet : spreadsheet.getSheets()) {
			String sheetTitle = sheet.getProperties().getTitle();
			int sheetId = sheet.getProperties().getSheetId();
			LOG.info(sheetTitle + " (" + sheetId + ")");
			if (sheetTitle.equals(DEFAULT_SHEET)) {
				setName(sheet, sheetName);
				return new GoogleSheet(this, sheetName, sheetId);
			} else if (sheetTitle.equals(sheetName)) {
				return new GoogleSheet(this, sheetName, sheetId);
			}
		}
	
		createSheet(sheetName);
		return getSheet(sheetName);
	}

	private void createSheet(String sheetName) throws IOException {
		AddSheetRequest add = new AddSheetRequest()
				.setProperties(new SheetProperties().setTitle(sheetName));
		api.spreadsheets().batchUpdate(id, new BatchUpdateSpreadsheetRequest()
				.setRequests(asList(new Request().setAddSheet(add))))
				.execute();
	}

	private void setName(Sheet sheet, String name) throws IOException {
		
		String originalName = sheet.getProperties().getTitle();
		
		UpdateSheetPropertiesRequest renameSheet = new UpdateSheetPropertiesRequest();
		renameSheet.setProperties(sheet.getProperties());
		renameSheet.getProperties().setTitle(name);
		renameSheet.setFields("title");
		
		Request request = new Request();
		request.setUpdateSheetProperties(renameSheet);
		BatchUpdateSpreadsheetRequest batch = new BatchUpdateSpreadsheetRequest();
		batch.setRequests(singletonList(request));
		api.spreadsheets().batchUpdate(id, batch).execute();
		
		LOG.info(originalName + " renamed to " + name);
		
	}

	public String getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return name + " (" + id + ")";
	}
}
