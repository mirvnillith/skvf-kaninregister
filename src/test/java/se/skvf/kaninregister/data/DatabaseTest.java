package se.skvf.kaninregister.data;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static se.skvf.kaninregister.data.Database.NAME;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import se.skvf.kaninregister.BunnyTest;
import se.skvf.kaninregister.drive.GoogleDrive;
import se.skvf.kaninregister.drive.GoogleSheet;
import se.skvf.kaninregister.drive.GoogleSpreadsheet;

public class DatabaseTest extends BunnyTest {

	@InjectMocks
	private Database database;
	@Mock
	private GoogleDrive drive;
	@Mock
	private GoogleSheet sheet;
	
	private GoogleSpreadsheet spreadsheet;
	
	@BeforeEach
	public void mockSpreadsheet() throws IOException {
		spreadsheet = mock(GoogleSpreadsheet.class);
		when(drive.getSpreadsheet(NAME)).thenReturn(spreadsheet);
	}
	
	@Test
	public void getTable() throws IOException {
		
		String name = randomUUID().toString();
		String column1 = randomUUID().toString();
		String column2 = randomUUID().toString();
		
		when(spreadsheet.getSheet(name)).thenReturn(sheet);
		when(sheet.getColumns(any())).thenAnswer(i -> {
			Collection<String> names = i.getArgument(0);
			return names.stream().collect(toMap(identity(), "Column"::concat));
		});
		String sheetName = randomUUID().toString();
		when(sheet.getName()).thenReturn(sheetName);
		
		Table table = database.getTable(name, asList(column1, column2));
		verifyNoInteractions(drive);
		assertThat(table.toString()).isEqualTo(name);
		
		table.add(new HashMap<>());
		verify(drive).getSpreadsheet(NAME);
		verify(spreadsheet).getSheet(name);
		verify(sheet).addRow(eq("ColumnID"), anyMap());
		
		assertThat(table.toString()).isEqualTo(sheetName);
	}
}
