package se.skvf.kaninregister.drive;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.singleton;
import static org.apache.commons.io.IOUtils.readLines;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Joiner;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;

@SuppressWarnings("deprecation")
@Component
public class GoogleDrive {

	private static final Log LOG = LogFactory.getLog(GoogleDrive.class);
	
	private static final String SPREADSHEET_MIME = "application/vnd.google-apps.spreadsheet";
	private static final String APPLICATION_NAME = "SKVF Kaninregister";
	private static final String ACCESS_SCOPE = "https://www.googleapis.com/auth/drive.file";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	private String jsonCredentials;
	private String folder;
	private Drive drive;
	private Sheets sheets;
	private long lastSheets;

	@Value("${skvf.google.credentials}")
	public void setJsonCredentials(String jsonCredentials) {
		this.jsonCredentials = jsonCredentials;
	}

	@Value("${skvf.google.folder}")
	public void setFolder(String folder) {
		this.folder = folder;
	}

	@PostConstruct
	public void setup() throws Exception {

		jsonCredentials = decode(jsonCredentials);

		final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

		GoogleCredential credential = GoogleCredential
				.fromStream(new ByteArrayInputStream(jsonCredentials.getBytes()), httpTransport, JSON_FACTORY)
				.createScoped(singleton(ACCESS_SCOPE));

		drive = new Drive.Builder(httpTransport, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME)
				.build();

		sheets = new Sheets.Builder(httpTransport, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME)
				.build();
		lastSheets = System.currentTimeMillis();
	}

	private static ThreadLocal<Boolean> skipDelay = new ThreadLocal<Boolean>();
	
	public interface Skipper {
		void skip() throws IOException;
	}
	
	public static void skipDelay(Skipper skipper) throws IOException {
		skipDelay.set(true);
		try {
			skipper.skip();
		} finally {
			skipDelay.remove();
		}
	}
	
	public synchronized Sheets getSheets() {
		Boolean skip = skipDelay.get();
		if (skip == null || !skip) {
			long sinceLast = currentTimeMillis() - lastSheets;
			if (sinceLast < 1000) {
				try {
					Thread.sleep(1000 - sinceLast);
				} catch (InterruptedException ignored) {
				}
			}
		}
		lastSheets = currentTimeMillis();
		
		return sheets;
	}

	public GoogleSpreadsheet getSpreadsheet(String name) throws IOException {
		String id = findFile(name, SPREADSHEET_MIME);
		if (id == null) {
			id = createSpreadheet(name);
		}
		return new GoogleSpreadsheet(this, id, name);
	}

	private String createSpreadheet(String name) throws IOException {

		Spreadsheet newSheet = new Spreadsheet();
		newSheet.setProperties(new SpreadsheetProperties().setTitle(name));

		newSheet = sheets.spreadsheets().create(newSheet)
				.setFields("spreadsheetId")
				.execute();
		
		LOG.info("createSheet(" + name + "): " + newSheet.getSpreadsheetId());

		return moveToFolder(newSheet.getSpreadsheetId());
	}

	private String moveToFolder(String fileId) throws IOException {

		File file = drive.files().get(fileId).setFields("parents").execute();

		drive.files().update(fileId, null).setAddParents(folder)
				.setRemoveParents(Joiner.on(',').join(file.getParents())).execute();

		LOG.info("Moved " + fileId + " to " + folder);
		
		return fileId;
	}

	private String findFile(String name, String mime) throws IOException {

		FileList result = drive.files().list()
				.setPageSize(10)
				.setFields("files(id, name, mimeType, parents)")
				.execute();
		List<File> files = result.getFiles();
		if (files != null) {
			for (File file : files) {
				if (!file.getParents().contains(folder)) {
					LOG.info("delete(" + file.getId() + "): " + file.getName() + ":" + file.getMimeType());
					drive.files().delete(file.getId()).execute();
				} else {
					LOG.info(file.getName() + ":" + file.getMimeType() + " (" + file.getId() + ")");
					if (file.getName().equals(name) && file.getMimeType().equals(mime)) {
						return file.getId();
					}
				}
			}
		}
		return null;
	}

	public static void main(String[] args) throws IOException {
		System.out.println(readLines(new FileInputStream("credentials.json"), Charset.forName("UTF-8")).stream()
				.map(line -> line.replace('"', '§'))
				.map(line -> line.replace('\\', '¤'))
				.collect(Collectors.joining("")));
	}

	private static String decode(String credentials) {
		credentials = credentials.replace('§', '"');
		credentials = credentials.replace('¤', '\\');
		return credentials;
	}
}
