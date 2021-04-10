package se.skvf.kaninregister.migration;

import static org.mockito.Mockito.when;
import static se.skvf.kaninregister.migration.OldRegistry.BREEDERS_SHEET;
import static se.skvf.kaninregister.migration.OldRegistry.OWNERS_SHEET;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import se.skvf.kaninregister.BunnyTest;
import se.skvf.kaninregister.drive.GoogleDrive;
import se.skvf.kaninregister.drive.GoogleSheet;
import se.skvf.kaninregister.drive.GoogleSpreadsheet;
import se.skvf.kaninregister.model.Registry;

public class OldRegistryTest extends BunnyTest {

	@InjectMocks
	private OldRegistry old;
	
	@Mock
	private GoogleDrive drive;
	@Mock
	private GoogleSpreadsheet spreadsheet;
	@Mock
	private GoogleSheet ownerSheet;
	@Mock
	private GoogleSheet breederSheet;
	
	
	@Mock
	private Registry registry;
	
	@BeforeEach
	public void setup() throws IOException {
		old.setOldRegistry("Old Registry");
		when(drive.getSpreadsheet("Old Registry")).thenReturn(spreadsheet);
		when(spreadsheet.getSheet(OWNERS_SHEET)).thenReturn(ownerSheet);
		when(spreadsheet.getSheet(BREEDERS_SHEET)).thenReturn(breederSheet);
	}
	
	@Test
	public void empty() throws IOException {
		old.setup();
	}
}
