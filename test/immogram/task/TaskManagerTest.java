package immogram.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskManagerTest {

	private TaskManager sut;

	@BeforeEach
	void setUp() {
		sut = new TaskManager();
	}

	@Test
	void listFactories_shouldReturnManagedTaskFactoryWithMatchingParamType() {
		// arrange
		var boolFactory = new TaskFactory<Boolean, Void, Void>() {
			public @Override Task<Void, Void> create(Boolean param) {
				return null;
			}
		};

		var intsFactory = new TaskFactory<Integer, Void, Void>() {
			public @Override Task<Void, Void> create(Integer param) {
				return null;
			}
		};

		var managed1 = sut.register("int1", intsFactory);
		var managed2 = sut.register("bool", boolFactory);
		var managed3 = sut.register("int2", intsFactory);

		// act
		var factories = sut.listFactories(Integer.class);

		// assert
		assertEquals(2, factories.size());
		assertFalse(factories.contains(managed2));
		assertSame(managed1, factories.get(0));
		assertSame(managed3, factories.get(1));
	}
}
