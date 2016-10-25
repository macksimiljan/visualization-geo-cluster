package representation;

/**
 * A region is a square.
 * 
 * @author MM
 */
public  class Region {
	final double top;
	final double bottom;
	final double left;
	final double right;
	
	public Region(double top, double bottom, double left, double right) {
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
		
		if (top > 90 || top < -90 || bottom > 90 || bottom < -90 ||
				left > 180 || left < -180 || right > 180 || right < -180)
			throw new IllegalArgumentException("Your region specification is not allowed: "+this.toString());
	}
	
	/**
	 * Checks whether a location is within this region.
	 * @param longitude Longitude of the location.
	 * @param latitude Latitude of the location.
	 * @return 'true' iff location is within this region.
	 */
	public boolean contains(double longitude, double latitude) {
		return ( (longitude <= right) && (longitude >= left) ) && (latitude <= top && latitude >= bottom);
	}
	
	@Override
	public String toString() {
		return "top: "+top+", bottom: "+bottom+", left: "+left+", right: "+right;
	}
}