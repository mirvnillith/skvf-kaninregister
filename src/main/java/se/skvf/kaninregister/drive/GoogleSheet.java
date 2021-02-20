package se.skvf.kaninregister.drive;

import static java.lang.Math.max;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.defaultString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.api.services.sheets.v4.model.AppendCellsRequest;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
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
	private static final String TOP_ROW = "1:1";

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

		ValueRange data = spreadsheet.getApi().spreadsheets().values()
				.get(spreadsheet.id, range(TOP_ROW))
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

	private String range(String a1Range) {
		return "'" + name + "'!" + a1Range;
	}

	private void formatHeaders() throws IOException {
		
		RepeatCellRequest bold = new RepeatCellRequest()
				.setRange(topRowGridRange())
				.setCell(BOLD_CELL)
				.setFields("userEnteredFormat(textFormat)");
		
		UpdateSheetPropertiesRequest locked = new UpdateSheetPropertiesRequest()
				.setProperties(FROZEN_TOP_ROW.setSheetId(id))
				.setFields("gridProperties.frozenRowCount");
		
		update(new Request().setRepeatCell(bold),
				new Request().setUpdateSheetProperties(locked));
		
		LOG.info("Formatted headers for " + this);
	}

	private GridRange topRowGridRange() {
		return new GridRange().setSheetId(id).setStartRowIndex(0).setEndRowIndex(1);
	}
	
	private String createColumn(String columnName, int columnIndex) throws IOException {
		
		ValueRange title = new ValueRange()
				.setValues(singletonList(singletonList(columnName)));
		spreadsheet.getApi().spreadsheets().values()
				.update(spreadsheet.id, range(headerCell(columnIndex)), title)
				.setValueInputOption("RAW")
				.execute();
		
		LOG.info("Created column " + columnName + " as " + column(columnIndex) + " in " + this);
		return getColumn(columnName);
	}

	private static String headerCell(int columnIndex) {
		return column(columnIndex) + "1:" + column(columnIndex) + "1";
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

	public Collection<Map<String, String>> findRows(String idColumn, Collection<String> ids) throws IOException {
		
		Map<String, Predicate<String>> filter = new HashMap<>();
		filter.put(idColumn, ids::contains);
		return findRows(filter);
	}
	
	public Collection<Map<String, String>> findRows(Map<String, Predicate<String>> filters) throws IOException {
		
		return getRows(findIndex(filters));
	}

	private List<Integer> findIndex(Map<String, Predicate<String>> filters) throws IOException {
		
		List<String> ranges = filters.keySet().stream()
				.map(this::columnRange)
				.collect(toList());
		
		BatchGetValuesResponse data = spreadsheet.getApi().spreadsheets().values()
				.batchGet(spreadsheet.id)
				.setMajorDimension("COLUMNS")
				.setRanges(ranges)
				.execute();

		Map<String, List<Object>> columnData = new HashMap<>();
		int index = 0;
		int rowCount = 0;
		for (String column : filters.keySet()) {
			List<Object> values = data.getValueRanges().get(index).getValues().get(0);
			values.remove(0);
			columnData.put(column, values);
			index++;
			rowCount = max(rowCount, values.size());
		}
		
		List<Integer> rowIndex = new ArrayList<>();
		for (int i=0;i<rowCount;i++) {
			if (match(columnData, i, filters)) {
				rowIndex.add(i);
			}
		}
		return rowIndex;
	}
	
	private boolean match(Map<String, List<Object>> columnData, int index, Map<String, Predicate<String>> filters) {
		
		for (Map.Entry<String, Predicate<String>> filter : filters.entrySet()) {
			List<Object> values = columnData.get(filter.getKey());
			String value = "";
			if (index < values.size() && values.get(index) != null) {
				value = values.get(index).toString();
			}
			if (!filter.getValue().test(value)) {
				return false;
			}
		}
		return true;
	}

	private List<Map<String, String>> getRows(List<Integer> rowIndex) throws IOException {
		
		if (rowIndex.isEmpty()) {
			return emptyList();
		}
		
		List<String> ranges = rowIndex.stream()
				.map(this::rowRange)
				.collect(toList());
		
		BatchGetValuesResponse data = spreadsheet.getApi().spreadsheets().values()
				.batchGet(spreadsheet.id)
				.setMajorDimension("ROWS")
				.setRanges(ranges)
				.execute();
		return data.getValueRanges().stream()
				.map(vr -> vr.getValues().get(0))
				.map(GoogleSheet::map)
				.collect(toList());
	}

	private static Map<String, String> map(List<Object> row) {
		Map<String, String> map = new HashMap<>();
		char column = 'A';
		for (Object value : row) {
			if (value != null) {
				map.put(Character.toString(column), value.toString());
			}
			column++;
		}
		return map;
	}
	
	private String rowRange(int idx) {
		return range((idx + 2) + ":" + (idx + 2));
	}

	public boolean removeRow(String idColumn, String id) throws IOException {
		
		ValueRange data = spreadsheet.getApi().spreadsheets().values()
				.get(spreadsheet.id, columnRange(idColumn))
				.setMajorDimension("ROWS")
				.execute();
		
		List<Object> ids = data.getValues().stream()
				.map(l -> l.get(0))
				.collect(toList());
		ids.remove(0);
		for (int i = 0; i < ids.size(); i++) {
			if (id.equals(ids.get(i).toString())) {
				removeRow(i);
				return true;
			}
		}
		
		return false;
	}

	private String columnRange(String column) {
		return range(column + ":" + column);
	}

	private void removeRow(int rowIndex) throws IOException {
		
		GridRange row = new GridRange()
				.setSheetId(id)
				.setStartRowIndex(rowIndex + 1)
				.setEndRowIndex(rowIndex + 2);
		update(new Request().setDeleteRange(new DeleteRangeRequest()
				.setRange(row)
				.setShiftDimension("ROWS")));
		LOG.info("Removed row " + (rowIndex + 2) + " from " + this);
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
		
		AppendCellsRequest append = new AppendCellsRequest()
				.setSheetId(id)
				.setRows(singletonList(new RowData().setValues(asList(cells))))
				.setFields("userEnteredValue.stringValue");
		
		update(new Request().setAppendCells(append));
	}

	private void update(Request... requests) throws IOException {
		BatchUpdateSpreadsheetRequest batch = new BatchUpdateSpreadsheetRequest()
				.setRequests(asList(requests));
		spreadsheet.getApi().spreadsheets().batchUpdate(spreadsheet.id, batch).execute();
	}

	public void updateRow(String idColumn, String id, Map<String, String> data) throws IOException {
		
		Map<String, Predicate<String>> filter = new HashMap<>();
		filter.put(idColumn, id::equals);
		List<Integer> index = findIndex(filter);
		if (index.isEmpty()) {
			throw new NullPointerException(id + " not found in " + this);
		}
		
		List<Request> updates = new ArrayList<>(data.size());
		for (Map.Entry<String, String> value : data.entrySet()) {
			CellData cell = new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(defaultString(value.getValue())));
			RowData row = new RowData().setValues(asList(cell));
			updates.add(new Request().setUpdateCells(new UpdateCellsRequest()
				.setRange(gridRange(value.getKey(), index.get(0)))
				.setRows(asList(row))
				.setFields("userEnteredValue.stringValue")));
		}
		
		update(updates.toArray(new Request[0]));
	}

	private GridRange gridRange(String column, int rowIndex) {
		return new GridRange()
				.setSheetId(id)
				.setStartRowIndex(rowIndex + 1)
				.setEndRowIndex(rowIndex + 2)
				.setStartColumnIndex(columnIndex(column))
				.setEndColumnIndex(columnIndex(column) + 1);
	}
}