package tn.recommendation;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;

public class SparseVector {
	private Hashtable<Integer, Double> map;
	private int length;
	private double MIN = 0.0;

	public Set<Integer> getIndexSet() {
		if (map != null) {

			Set<Integer> indexes = new HashSet<Integer>();
			Set<Entry<Integer, Double>> set = map.entrySet();
			for (Entry<Integer, Double> entry : set) {
				indexes.add(entry.getKey());
			}
			return indexes;
		} else {
			return null;
		}
	}

	public SparseVector(int len) {
		if (len > 0) {
			map = new Hashtable<Integer, Double>();
			length = len;
		}
	}

	public SparseVector(SparseVector old) {
		this(old.length);
		Set<Entry<Integer, Double>> set = old.map.entrySet();
		for (Entry<Integer, Double> entry : set) {
			map.put(entry.getKey(), entry.getValue());
		}
	}

	public void setMIN(int MIN) {
		this.MIN = MIN;
	}

	/**
   * 
   */
	public void clear() {
		map.clear();
	}

	/**
	 * get max value
	 * 
	 * @return
	 */
	public double getMaxValue() {
		double maxValue = 0;
		if (map != null) {
			Set<Entry<Integer, Double>> set = map.entrySet();
			for (Entry<Integer, Double> entry : set) {
				maxValue = Math.max(maxValue, entry.getValue());
			}
		}
		return maxValue;
	}

	public double get(int index) {
		if (map.containsKey(index))
			return map.get(index);
		else
			return 0;
	}

	/**
	 * increment by value
	 * 
	 * @param index
	 */
	public void increment(int index, double value) {
		if (map.containsKey(index)) {
			set(index, map.get(index) + value);
		} else {
			set(index, value);
		}

	}

	public void set(int index, double value) {
		if (map != null) {
			if (value < MIN) {
				map.remove(index);
			} else {
				map.put(index, value);
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public Hashtable<Integer, Double> getMap() {
		return this.map;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");

		for (int i = 0; i < length - 1; i++) {
			sb.append(get(i)).append(", ");
		}
		if (length > 0) {
			sb.append(get(length - 1));
		}
		sb.append("]");
		return sb.toString();
	}
}
