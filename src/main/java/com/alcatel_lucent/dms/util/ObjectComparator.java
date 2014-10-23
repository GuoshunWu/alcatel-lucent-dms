package com.alcatel_lucent.dms.util;

import java.util.Comparator;

import org.apache.commons.beanutils.PropertyUtils;

import com.alcatel_lucent.dms.SystemError;

/**
 * Common object comparator by property value
 * @author allany
 *
 * @param <T>
 */
public class ObjectComparator<T> implements Comparator<T> {
	
	private String prop;
	private int asc = 1;
	
	public ObjectComparator(String prop, String ord) {
		this.prop = prop;
		if (ord != null && ord.equalsIgnoreCase("desc")) {
			asc = -1;
		}
	}

	@Override
	public int compare(T o1, T o2) {
		try {
			Object value1 = PropertyUtils.getProperty(o1, prop);
			Object value2 = PropertyUtils.getProperty(o2, prop);
			if (value1 == null && value2 == null) {
				return 0;
			} else if (value1 == null && value2 != null) {
				return -1 * asc;
			} else if (value1 != null && value2 == null) {
				return 1 * asc;
			} else {
				if (value1 instanceof Comparable && value2 instanceof Comparable) {
					return ((Comparable) value1).compareTo(value2) * asc;
				} else {
					return value1.toString().compareTo(value2.toString()) * asc;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new SystemError(e);
		}
	}
}
