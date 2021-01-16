package se.skvf.kaninregister.model;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static se.skvf.kaninregister.data.Table.ID;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class EntityTest<E extends Entity> {

	private final Class<E> entityClass;
	private final Function<Map<String, String>, E> from;
	
	protected EntityTest(Class<E> entityClass, Function<Map<String, String>, E> from) {
		this.entityClass = entityClass;
		this.from = from;
	}
	
	protected void assertAttribute(String name, BiFunction<E, String, E> setter, Function<E, String> getter) throws Exception {
		
		E entity = entityClass.getDeclaredConstructor().newInstance();
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
		
		E entity = entityClass.getDeclaredConstructor().newInstance();
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
			.isEqualTo(entityClass.getSimpleName()+"#"+entity.getId()+suffix);
	}
}
