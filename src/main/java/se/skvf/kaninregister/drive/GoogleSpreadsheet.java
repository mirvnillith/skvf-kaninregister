package se.skvf.kaninregister.drive;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AddProtectedRangeRequest;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.ProtectedRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.UpdateSheetPropertiesRequest;

public class GoogleSpreadsheet {

	private static final Log LOG = LogFactory.getLog(GoogleSpreadsheet.class);

	private static final Object DEFAULT_SHEET = "Sheet1";
	
	private final GoogleDrive drive;
	final String id;
	private final String name;
	
	GoogleSpreadsheet(GoogleDrive drive, String id, String name) {
		this.id = id;
		this.name = name;
		this.drive = drive;
	}
	
	public Sheets getApi() {
		return drive.getSheets();
	}
	
	public GoogleSheet getSheet(String sheetName) throws IOException {
		Spreadsheet spreadsheet = getApi().spreadsheets().get(id)
				.setFields("sheets(properties,protectedRanges)")
				.execute();
		
		for (Sheet sheet : spreadsheet.getSheets()) {
			String sheetTitle = sheet.getProperties().getTitle();
			int sheetId = sheet.getProperties().getSheetId();
			if (sheetTitle.equals(DEFAULT_SHEET)) {
				setName(sheet, sheetName);
				protect(sheet);
				return new GoogleSheet(this, sheetName, sheetId);
			} else if (sheetTitle.equals(sheetName)) {
				protect(sheet);
				return new GoogleSheet(this, sheetName, sheetId);
			}
		}
	
		createSheet(sheetName);
		return getSheet(sheetName);
	}

	private void protect(Sheet sheet) throws IOException {
		if (sheet.getProtectedRanges() == null ||
				sheet.getProtectedRanges().isEmpty()) {
			getApi().spreadsheets().batchUpdate(id, new BatchUpdateSpreadsheetRequest().setRequests(asList(new Request()
					.setAddProtectedRange(new AddProtectedRangeRequest()
							.setProtectedRange(new ProtectedRange()
									.setRange(new GridRange().setSheetId(sheet.getProperties().getSheetId()))
									.setWarningOnly(true))))));
		}
	}

	private void createSheet(String sheetName) throws IOException {
		AddSheetRequest add = new AddSheetRequest()
				.setProperties(new SheetProperties().setTitle(sheetName));
		getApi().spreadsheets().batchUpdate(id, new BatchUpdateSpreadsheetRequest()
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
		getApi().spreadsheets().batchUpdate(id, batch).execute();
		
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
