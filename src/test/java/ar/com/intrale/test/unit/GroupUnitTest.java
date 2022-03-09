package ar.com.intrale.test.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ar.com.intrale.messages.Group;
import io.micronaut.test.annotation.MicronautTest;

@MicronautTest
public class GroupUnitTest {

	@Test
	public void createGroup() {
		Group group = new Group();
		group.setDescription(ar.com.intrale.Test.DUMMY_VALUE);
		group.setName(ar.com.intrale.Test.DUMMY_VALUE);
		
		assertEquals(ar.com.intrale.Test.DUMMY_VALUE, group.getDescription());
		assertEquals(ar.com.intrale.Test.DUMMY_VALUE, group.getName());
		
	}

}
