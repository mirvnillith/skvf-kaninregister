package se.skvf.kaninregister.drive;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singleton;
import static org.apache.commons.io.IOUtils.readLines;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

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

	@Value("${skvf.google.credentials}")
	public void setJsonCredentials(String jsonCredentials) {
		this.jsonCredentials = jsonCredentials;
	}

	@Value("${skvf.google.folder}")
	public void setFolder(String folder) {
		this.folder = folder;
	}

	@PostConstruct
	public void setup() throws IOException, GeneralSecurityException {

		jsonCredentials = decode(jsonCredentials);

		final HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

		GoogleCredentials credentials = GoogleCredentials
				.fromStream(new ByteArrayInputStream(jsonCredentials.getBytes()))
				.createScoped(singleton(ACCESS_SCOPE));

		drive = new Drive.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
				.setApplicationName(APPLICATION_NAME)
				.build();

		sheets = new Sheets.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
				.setApplicationName(APPLICATION_NAME)
				.build();
	}

	public synchronized Sheets getSheets() {
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

		File file = new File()
				.setName(name)
				.setMimeType(SPREADSHEET_MIME)
				.setParents(Collections.singletonList(folder));
		
		file = drive.files().create(file)
				.setFields("id")
				.execute();
		
		LOG.info("createSheet(" + name + "): " + file.getId());
		
		return file.getId();
	}

	private String findFile(String name, String mime) throws IOException {

		FileList result = drive.files().list()
				.setPageSize(10)
				.setQ(String.format("name = '%s' AND mimeType = '%s' AND '%s' IN parents", name, mime, folder))
				.setFields("files(id, name, mimeType, parents)")
				.execute();
		
		if (result.getFiles().isEmpty()) {
			return null;
		} else {
			File file = result.getFiles().get(0);
			LOG.info(file.getName() + ":" + file.getMimeType() + " (" + file.getId() + ")");
			return file.getId();
		}
	}

	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			try (FileInputStream input = new FileInputStream("credentials.json")) {
			System.out.println(readLines(input, UTF_8).stream()
				.map(line -> line.replace('"', '§'))
				.map(line -> line.replace('\\', '¤'))
				.collect(Collectors.joining("")));
			}
		} else {
			System.out.println(decode(args[0]));
		}
	}

	private static String decode(String credentials) {
		credentials = credentials.replace('§', '"');
		credentials = credentials.replace('¤', '\\');
		return credentials;
	}
}
