package se.skvf.kaninregister.model;

import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.skvf.kaninregister.model.Registry.BUNNIES_TABLE;
import static se.skvf.kaninregister.model.Registry.OWNERS_TABLE;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import se.skvf.kaninregister.BunnyTest;
import se.skvf.kaninregister.data.Database;
import se.skvf.kaninregister.data.Table;

public class RegistryTest extends BunnyTest {

	@InjectMocks
	private Registry registry;
	
	@Mock
	private Database database;
	@Mock
	private Table bunnies;
	@Mock
	private Table owners;
	@Captor
	private ArgumentCaptor<Map<String, String>> entity;
	
	@BeforeEach
	public void setup() throws Exception {
		when(database.getTable(BUNNIES_TABLE, Bunny.COLUMNS)).thenReturn(bunnies);
		when(database.getTable(OWNERS_TABLE, Owner.COLUMNS)).thenReturn(owners);
		registry.setup();
	}
	
	@SafeVarargs
	private <E extends Entity<?>> void assertAdd(Function<E, String> adder, Table table, Class<E> entityClass, BiFunction<E, String, E>... setters) throws Exception {
		
		E add = entityClass.getDeclaredConstructor().newInstance();
		setValues(add, setters);
		
		String id = randomUUID().toString();
		when(table.add(add.toMap())).thenReturn(id);
		
		assertThat(adder.apply(add)).isSameAs(id);
		verify(table).add(anyMap());
		assertThat(add.getId()).isSameAs(id);
		
		assertThrows(IllegalStateException.class, () -> adder.apply(add));
		verifyNoMoreInteractions(table);
	}
	
	@SafeVarargs
	private <E extends Entity<?>> void assertUpdate(Consumer<E> updater, Table table, Class<E> entityClass, BiFunction<E, String, E>... setters) throws Exception {
		
		E update = entityClass.getDeclaredConstructor().newInstance();
		setValues(update, setters);
		
		assertThrows(IllegalStateException.class, () -> updater.accept(update));
		verifyNoMoreInteractions(table);
		
		update.setId(randomUUID().toString());
		
		updater.accept(update);
		verify(table).update(update.toMap());
	}
	
	@SafeVarargs
	private <E extends Entity<?>> void assertRemove(Consumer<E> remover, Table table, Class<E> entityClass, BiFunction<E, String, E>... setters) throws Exception {
		
		E remove = entityClass.getDeclaredConstructor().newInstance();
		setValues(remove, setters);
		
		assertThrows(IllegalStateException.class, () -> remover.accept(remove));
		verifyNoMoreInteractions(table);
		
		remove.setId(randomUUID().toString());
		
		remover.accept(remove);
		verify(table).remove(remove.getId());
	}
	
	@SafeVarargs
	private <E extends Entity<?>> void assertFind(Function<Collection<String>, Collection<E>> ids, Function<Map<String, Predicate<String>>, Collection<E>> filters, Table table, Class<E> entityClass, BiFunction<E, String, E>... setters) throws Exception {
		
		E find = entityClass.getDeclaredConstructor().newInstance();
		setValues(find, setters);
		find.setId(randomUUID().toString());
		
		when(table.find(singleton(find.getId()))).thenReturn(singleton(find.toMap()));
		assertThat(ids.apply(singleton(find.getId())))
			.isNotEmpty()
			.allMatch(f -> f.toMap().equals(find.toMap()));

		Map<String, Predicate<String>> filter = new HashMap<String, Predicate<String>>();
		filter.put(find.getId(), find.getId()::equals);
		
		when(table.find(filter)).thenReturn(singleton(find.toMap()));
		assertThat(filters.apply(filter))
			.isNotEmpty()
			.allMatch(f -> f.toMap().equals(find.toMap()));
		
	}
	
	@SuppressWarnings("unchecked")
	private <E extends Entity<?>> void setValues(E entity, BiFunction<E, String, E>... setters) {
		for (BiFunction<E, String, E> setter : setters) {
			setter.apply(entity, randomUUID().toString());
		}
	}

	interface AddOperation<E extends Entity<?>> {
		String add(E entity) throws IOException;
	}
	
	private static <E extends Entity<?>> Function<E, String> wrap(AddOperation<E> adder) {
		return e -> {
			try {
				return adder.add(e);
			} catch (IOException never) {
				throw new RuntimeException(never);
			}
		};
	}
	
	interface UpdateOperation<E extends Entity<?>> {
		void update(E entity) throws IOException;
	}
	
	private static <E extends Entity<?>> Consumer<E> wrap(UpdateOperation<E> updater) {
		return e -> {
			try {
				updater.update(e);
			} catch (IOException never) {
				throw new RuntimeException(never);
			}
		};
	}
	
	interface IdsFinder<E extends Entity<?>> {
		Collection<E> find(Collection<String> ids) throws IOException;
	}
	
	private static <E extends Entity<?>> Function<Collection<String>, Collection<E>> wrap(IdsFinder<E> finder) {
		return ids -> {
			try {
				return finder.find(ids);
			} catch (IOException never) {
				throw new RuntimeException(never);
			}
		};
	}
	
	interface FiltersFinder<E extends Entity<?>> {
		Collection<E> find(Map<String, Predicate<String>> filters) throws IOException;
	}
	
	private static <E extends Entity<?>> Function<Map<String, Predicate<String>>, Collection<E>> wrap(FiltersFinder<E> finder) {
		return filters -> {
			try {
				return finder.find(filters);
			} catch (IOException never) {
				throw new RuntimeException(never);
			}
		};
	}
	
	@Test
	public void addOwner() throws Exception {
		
		assertAdd(wrap((Owner o) -> registry.add(o)), 
				owners, Owner.class, Owner::setName);
	}
	
	@Test
	public void updateOwner() throws Exception {
		
		assertUpdate(wrap((Owner o) -> registry.update(o)), 
				owners, Owner.class, Owner::setName);
	}
	
	@Test
	public void removeOwner() throws Exception {
		
		assertRemove(wrap((Owner o) -> registry.remove(o)), 
				owners, Owner.class, Owner::setName);
	}
	
	@Test
	public void findOwners() throws Exception {
		
		assertFind(wrap((Collection<String> ids) -> registry.findOwners(ids)), 
				wrap((Map<String, Predicate<String>> filters) -> registry.findOwners(filters)), 
				owners, Owner.class, Owner::setName);
	}
	
	@Test
	public void addBunny() throws Exception {
		
		assertAdd(wrap((Bunny b) -> registry.add(b)), 
				bunnies, Bunny.class, Bunny::setName, Bunny::setOwner, Bunny::setChip);
	}
	
	@Test
	public void updateBunny() throws Exception {
		
		assertUpdate(wrap((Bunny b) -> registry.update(b)), 
				bunnies, Bunny.class, Bunny::setName, Bunny::setOwner, Bunny::setChip);
	}
	
	@Test
	public void removeBunny() throws Exception {
		
		assertRemove(wrap((Bunny o) -> registry.remove(o)), 
				bunnies, Bunny.class, Bunny::setName, Bunny::setOwner, Bunny::setChip);
	}
	
	@Test
	public void findBunnies() throws Exception {
		
		assertFind(wrap((Collection<String> ids) -> registry.findBunnies(ids)), 
				wrap((Map<String, Predicate<String>> filters) -> registry.findBunnies(filters)), 
				bunnies, Bunny.class, Bunny::setName, Bunny::setOwner, Bunny::setChip);
	}
}
