package representation;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests {@link Region}.
 * @author MM
 *
 */
public class RegionTest {

	/**
	 * for {@link Region#contains(double, double)}.
	 */
	@Test
	public void test_contains() {
		Region r = new Region(90, 0, 0, 90);
		
		assertTrue(r.contains(45, 45));
		assertFalse(r.contains(-45, -45));
	}

}
