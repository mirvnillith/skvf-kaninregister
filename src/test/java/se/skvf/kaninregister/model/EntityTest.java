package se.skvf.kaninregister.model;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static se.skvf.kaninregister.data.Table.ID;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class EntityTest<E extends Entity<?>> {

	private final Function<Map<String, String>, E> from;
	
	protected EntityTest(Function<Map<String, String>, E> from) {
		this.from = from;
	}
	
	protected abstract E create();
	
	protected void assertMandatoryAttribute(String attribute) {
		
		Map<String, String> map = create().toMap();
		map.remove(attribute);
		E entity = from.apply(map);
		
		assertThrows(IllegalStateException.class, () -> entity.toMap());
	}
	
	protected void assertAttribute(String name, BiFunction<E, String, E> setter, Function<E, String> getter) throws Exception {
		
		E entity = create();
		String id = randomUUID().toString();
		assertThat(entity.setId(id)).isSameAs(entity);
		assertThat(entity.getId()).isSameAs(id);
		
		String value = randomUUID().toString();
		
		assertThat(setter.apply(entity, value)).isSameAs(entity);
		assertThat(getter.apply(entity)).isSameAs(value);
		
		Map<String, String> map = entity.toMap();
		assertThat(map)
			.containsEntry(name, value)
			.containsEntry(ID, id);
		
		entity = from.apply(map);
		assertThat(getter.apply(entity)).isSameAs(value);
		assertThat(entity.getId()).isSameAs(id);
	}
	
	protected void assertBooleanAttribute(String name, BiFunction<E, Boolean, E> setter, Function<E, Boolean> getter) throws Exception {
		
		E entity = create();
		String id = randomUUID().toString();
		assertThat(entity.setId(id)).isSameAs(entity);
		assertThat(entity.getId()).isSameAs(id);
		
		boolean value = true;
		
		assertThat(setter.apply(entity, value)).isSameAs(entity);
		assertThat(getter.apply(entity)).isSameAs(value);
		
		Map<String, String> map = entity.toMap();
		assertThat(map)
			.containsEntry(name, Entity.toString(value))
			.containsEntry(ID, id);
		
		entity = from.apply(map);
		assertThat(getter.apply(entity)).isSameAs(value);
		assertThat(entity.getId()).isSameAs(id);
	}
	
	protected void assertToString(E entity, String suffix) {
		assertThat(entity.toString())
			.isEqualTo(entity.getClass().getSimpleName()+"#"+entity.getId()+suffix);
	}
}
