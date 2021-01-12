package se.skvf.kaninregister.data;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.skvf.kaninregister.data.Table.ID;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import se.skvf.kaninregister.BunnyTest;
import se.skvf.kaninregister.drive.GoogleSheet;

public class TableTest extends BunnyTest {

	private Table table;
	
	@Mock
	private GoogleSheet sheet;
	@Captor
	private ArgumentCaptor<Map<String, String>> data;
	@Captor
	private ArgumentCaptor<Map<String, Predicate<String>>> filters;
	
	private String attribute1;
	private String attribute2;
	private Map<String, String> entity;
	
	@BeforeEach
	public void setup() throws IOException {
		
		attribute1 = randomUUID().toString();
		attribute2 = randomUUID().toString();
		when(sheet.getColumn(anyString())).thenAnswer(i -> "Column" + i.getArgument(0));
		
		table = new Table(sheet, asList(attribute1, attribute2));
		
		entity = new HashMap<>();
		entity.put(attribute1, randomUUID().toString());
		entity.put(attribute2, randomUUID().toString());
	}
	
	@Test
	public void add() throws IOException {
		
		String id = table.add(entity);
		
		assertThat(entity).containsEntry(ID, id);
		verify(sheet).addRow(eq("Column"+ID), data.capture());
		assertThat(data.getValue())
			.containsEntry("Column"+ID, id)
			.containsEntry("Column"+attribute1, entity.get(attribute1))
			.containsEntry("Column"+attribute2, entity.get(attribute2));
	}
	
	@Test
	public void update() throws IOException {
		
		String id = randomUUID().toString();
		entity.put(ID, id);
		
		table.update(entity);
		
		verify(sheet).updateRow(eq("Column"+ID), eq(id), data.capture());
		assertThat(data.getValue())
			.doesNotContainEntry("Column"+ID, id)
			.containsEntry("Column"+attribute1, entity.get(attribute1))
			.containsEntry("Column"+attribute2, entity.get(attribute2));
		assertThat(entity)
			.containsEntry(ID, id)
			.containsEntry(attribute1, entity.get(attribute1))
			.containsEntry(attribute2, entity.get(attribute2));
	}
	
	@Test
	public void findIDs() throws IOException {
		
		Collection<String> ids = singleton(randomUUID().toString());
		
		Map<String, String> map = new HashMap<String, String>();
		String value1 = randomUUID().toString();
		map.put("Column"+attribute1, value1);
		String value2 = randomUUID().toString();
		map.put("Column"+attribute2, value2);
		when(sheet.findRows("Column"+ID, ids)).thenReturn(singleton(map ));
		
		Collection<Map<String, String>> found = table.find(ids);
		
		assertThat(found).hasSize(1);
		assertThat(found.iterator().next())
			.containsEntry(attribute1, value1)
			.containsEntry(attribute2, value2);
	}
	
	@Test
	public void findFilters() throws IOException {
		
		Map<String, Predicate<String>> find = new HashMap<>();
		Predicate<String> filter1 = attribute1::equals;
		find.put(attribute1, filter1);
		Predicate<String> filter2 = attribute2::equals;
		find.put(attribute2, filter2);
		
		Map<String, String> map = new HashMap<String, String>();
		String value1 = randomUUID().toString();
		map.put("Column"+attribute1, value1);
		String value2 = randomUUID().toString();
		map.put("Column"+attribute2, value2);
		when(sheet.findRows(filters.capture())).thenReturn(singleton(map ));
		
		Collection<Map<String, String>> found = table.find(find);
		
		assertThat(filters.getValue())
			.containsEntry("Column"+attribute1, filter1)
			.containsEntry("Column"+attribute2, filter2);
		assertThat(found).hasSize(1);
		assertThat(found.iterator().next())
			.containsEntry(attribute1, value1)
			.containsEntry(attribute2, value2);
	}
	
	@Test
	public void remove() throws IOException {
		
		String id = randomUUID().toString();
		table.remove(id);
		verify(sheet).removeRow("Column"+ID, id);
	}
	
}
