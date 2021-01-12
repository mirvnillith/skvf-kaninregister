package se.skvf.kaninregister.data;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.skvf.kaninregister.data.Database.NAME;

import java.io.IOException;

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
	private GoogleSpreadsheet spreadsheet;
	@Mock
	private GoogleSheet sheet;
	
	@Test
	public void setup() throws IOException {
		database.setup();
		verify(drive).getSpreadsheet(NAME);
	}
	
	@Test
	public void getTable() throws IOException {
		
		String name = randomUUID().toString();
		String column1 = randomUUID().toString();
		String column2 = randomUUID().toString();
		
		when(drive.getSpreadsheet(NAME)).thenReturn(spreadsheet);
		when(spreadsheet.getSheet(name)).thenReturn(sheet);
		when(sheet.getColumn(anyString())).thenAnswer(i -> "Column" + i.getArgument(0));
		String sheetName = randomUUID().toString();
		when(sheet.getName()).thenReturn(sheetName);
		
		assertThat(database.getTable(name, asList(column1, column2)).toString()).isEqualTo(sheetName);
	}
}
