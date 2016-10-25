package general;

/**
 * Classifier for geo-points to regions.
 *
 * @author MM
 *
 */
public class GeoDivision {
	
	/**
	 * A region is a square.
	 */
	class Region {
		final double top;
		final double bottom;
		final double left;
		final double right;
		
		public Region(double top, double bottom, double left, double right) {
			this.top = top;
			this.bottom = bottom;
			this.left = left;
			this.right = right;
			// TODO: überprüfe auf gültige Werte
		}
	}
	

}
