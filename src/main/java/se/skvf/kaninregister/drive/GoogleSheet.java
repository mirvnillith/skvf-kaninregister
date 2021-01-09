package se.skvf.kaninregister.drive;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.defaultString;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.CellFormat;
import com.google.api.services.sheets.v4.model.DeleteRangeRequest;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.GridProperties;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.RepeatCellRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.TextFormat;
import com.google.api.services.sheets.v4.model.UpdateCellsRequest;
import com.google.api.services.sheets.v4.model.UpdateSheetPropertiesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;

public class GoogleSheet {

	private static final SheetProperties FROZEN_TOP_ROW = new SheetProperties()
			.setGridProperties(new GridProperties().setFrozenRowCount(1));
	private static final CellData BOLD_CELL = new CellData()
			.setUserEnteredFormat(new CellFormat().setTextFormat(new TextFormat().setBold(true)));
	private static final String TOP_ROW = "!1:1";

	private static final Log LOG = LogFactory.getLog(GoogleSheet.class);
	
	private final GoogleSpreadsheet spreadsheet;
	private final String name;
	private final int id;
	
	public GoogleSheet(GoogleSpreadsheet sheet, String name, int id) {
		this.spreadsheet = sheet;
		this.name = name;
		this.id = id;
	}

	public String getColumn(String columnName) throws IOException {

		ValueRange data = spreadsheet.api.spreadsheets().values()
				.get(spreadsheet.id, name + TOP_ROW)
				.execute();
		if (data.getValues() == null || data.getValues().isEmpty()) {
			formatHeaders();
			return createColumn(columnName, 0);
		} else {
			
			List<Object> row = data.getValues().get(0);
			for (int i = 0; i < row.size(); i++) {
				if (columnName.equals(row.get(i).toString())) {
					return column(i);
				}
			}

			return createColumn(columnName, row.size());
		}
	}

	private void formatHeaders() throws IOException {
		
		RepeatCellRequest bold = new RepeatCellRequest()
				.setRange(topRowRange(id))
				.setCell(BOLD_CELL)
				.setFields("userEnteredFormat(textFormat)");
		
		UpdateSheetPropertiesRequest locked = new UpdateSheetPropertiesRequest()
				.setProperties(FROZEN_TOP_ROW.setSheetId(id))
				.setFields("gridProperties.frozenRowCount");
		
		update(new Request().setRepeatCell(bold),
				new Request().setUpdateSheetProperties(locked));
		
		LOG.info("Formatted headers for " + this);
	}

	private static GridRange topRowRange(int sheetId) {
		return new GridRange().setSheetId(sheetId).setStartRowIndex(0).setEndRowIndex(1);
	}

	private String createColumn(String columnName, int columnIndex) throws IOException {
		
		ValueRange title = new ValueRange()
				.setValues(singletonList(singletonList(columnName)));
		spreadsheet.api.spreadsheets().values()
				.update(spreadsheet.id, name + headerCell(columnIndex), title)
				.setValueInputOption("RAW")
				.execute();
		
		LOG.info("Created column " + columnName + " as " + column(columnIndex) + " in " + this);
		return getColumn(columnName);
	}

	private static String headerCell(int columnIndex) {
		return "!" + column(columnIndex) + "1:" + column(columnIndex) + "1";
	}

	private static String column(int columnIndex) {
		return Character.toString('A' + columnIndex);
	}
	
	private static int columnIndex(String column) {
		return column.charAt(0) - 'A';
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return name + " (" + id + ") in " + spreadsheet;
	}

	public int removeRows(String column, Predicate<String> filter) throws IOException {
		
		ValueRange data = spreadsheet.api.spreadsheets().values()
				.get(spreadsheet.id, name + "!" + column + ":" + column)
				.execute();
		
		for (int i=0;i<data.getValues().size();i++) {
			if (filter.test(data.getValues().get(i).get(0).toString())) {
				removeRow(i);
				return removeRows(column, filter) + 1;
			}
		}
		
		return 0;
	}

	private void removeRow(int rowIndex) throws IOException {
		
		GridRange row = new GridRange()
				.setStartRowIndex(rowIndex)
				.setEndRowIndex(rowIndex + 1);
		update(new Request().setDeleteRange(new DeleteRangeRequest()
				.setRange(row)
				.setShiftDimension("ROWS")));
		LOG.info("Removed row " + (rowIndex + 1) + " from " + this);
	}

	public void addRow(String idColumn, Map<String, String> data) throws IOException {
		
		int firstColumn = -1;
		int lastColumn = -1;
		for (String c : data.keySet()) {
			int ci = columnIndex(c);
			if (firstColumn == -1 || firstColumn > ci) {
				firstColumn = ci;
			}
			if (lastColumn == -1 || lastColumn < ci) {
				lastColumn = ci;
			}
		}

		CellData[] cells = new CellData[lastColumn - firstColumn + 1];
		final int offset = firstColumn;
		data.forEach((c, v) -> {
			cells[columnIndex(c) - offset] = new CellData()
					.setUserEnteredValue(new ExtendedValue().setStringValue(defaultString(v)));
		});
		
		int rowIndex = findFirstEmptyRow(idColumn);
		GridRange rowColumns = new GridRange()
				.setStartRowIndex(rowIndex)
				.setEndRowIndex(rowIndex + 1)
				.setStartColumnIndex(firstColumn)
				.setEndColumnIndex(lastColumn + 1);
		
		UpdateCellsRequest update = new UpdateCellsRequest()
				.setRange(rowColumns)
				.setRows(singletonList(new RowData().setValues(asList(cells))))
				.setFields("userEnteredValue.stringValue");
		
		update(new Request().setUpdateCells(update));
	}

	private void update(Request... requests) throws IOException {
		BatchUpdateSpreadsheetRequest batch = new BatchUpdateSpreadsheetRequest()
				.setRequests(asList(requests));
		spreadsheet.api.spreadsheets().batchUpdate(spreadsheet.id, batch).execute();
	}

	private int findFirstEmptyRow(String idColumn) throws IOException {
		
		ValueRange data = spreadsheet.api.spreadsheets().values()
				.get(spreadsheet.id, name + "!" + idColumn + ":" + idColumn)
				.execute();
		return data.getValues().size();
	}
}