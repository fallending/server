package org.nasdanika.cdo.web.routes.app;

/**
 * Helper class for sorting values.
 * @author Pavel Vlasov
 *
 * @param <V>
 */
public class ValueEntry<V> implements Comparable<ValueEntry<V>> {
	
	public ValueEntry(V value, int position, Object sortValue) throws Exception {
		this.value = value;
		this.position = position;
		this.sortValue = sortValue;
	}
	
	public final V value;
	public final Object sortValue;
	public final int position;
	
	@SuppressWarnings("unchecked")
	@Override
	public int compareTo(ValueEntry<V> o) {
		Object sv = sortValue;					
		Object osv = o.sortValue;
		
		if (sv instanceof Comparable) {
			if (osv == null) {
				return -1;
			}
			int result = ((Comparable<Object>) sv).compareTo(osv);
			if (result != 0) {
				return result;
			}
		} else if (osv instanceof Comparable) {
			if (sv == null) {
				return 1;
			}
			int result = ((Comparable<Object>) osv).compareTo(sv);
			if (result != 0) {
				return -result;
			}
		}
		return position-o.position;
	}
	
}
