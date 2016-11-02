package general;

import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import representation.Region;
import representation.Vertex;

/**
 * Tests for {@link GeoDivision}.
 * @author MM
 *
 */
public class GeoDivisionTest {

	/**
	 * for {@link GeoDivision#addVertices(java.util.Set)}.
	 */
	@Test
	public void test_addVertices() {
		GeoDivision div = new GeoDivision(GeoDivision.initDefaultRegions());
		Set<Vertex> set = new HashSet<Vertex>();
		
		Vertex paris = new Vertex();
		paris.lat = 48.9; paris.lon = 2.3; paris.label = "paris";
		set.add(paris);
		Vertex athens = new Vertex();
		athens.lat = 38.1; athens.lon = 23.9; athens.label = "athens";
		System.out.println(set.add(athens));
		Vertex dublin = new Vertex();
		dublin.lat = 53.3; dublin.lon = -6.4; dublin.label = "dublin";
		set.add(dublin);
		Vertex tunis = new Vertex();
		tunis.lat = 36.7; tunis.lon = 10.1; tunis.label = "tunis";
		set.add(tunis);
		Vertex mauritius = new Vertex();
		mauritius.lat = -20.3; mauritius.lon = 57.5; mauritius.label = "mauritius";
		set.add(mauritius);
		Vertex dakar = new Vertex();
		dakar.lat = 14.6; dakar.lon = -17.6; dakar.label = "dakar";
		set.add(dakar);
		Vertex arabicSea = new Vertex();
		arabicSea.lat = 15.0; arabicSea.lon = 15.0; arabicSea.label = "arabicSea";
		set.add(arabicSea);
		Vertex kabul = new Vertex();
		kabul.lat = 34.5; kabul.lon = 69.2; kabul.label = "kabul";
		set.add(kabul);
		Vertex sydney = new Vertex();
		sydney.lat = -33.9; sydney.lon = 151.2; sydney.label = "sydney";
		set.add(sydney);
		Vertex davao = new Vertex();
		davao.lat = 7.0; davao.lon = 125.6; davao.label = "davao";
		set.add(davao);
		Vertex toronto = new Vertex();
		toronto.lat = 43.5; toronto.lon = -79.4; toronto.label = "toronto";
		set.add(toronto);
		Vertex panama = new Vertex();
		panama.lat = 8.6; panama.lon = -80.4; panama.label = "panama";
		set.add(panama);
		
		System.out.println(set.size());
		System.out.println("------");
		
		div.addVertices(set);
		for (Region r : div.getRegions()) {
			System.out.println(r.label);
			for (Vertex v : div.getVertices(r)) {
				System.out.println("\t"+v.label);
			}
		}
		
		
		fail("Not yet implemented"); // TODO
	}

}
