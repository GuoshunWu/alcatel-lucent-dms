/**
 * 
 */
package com.alcatel_lucent.dms.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;

import com.alcatel_lucent.dms.SystemError;

/**
 * @author guoshunw
 * 
 */
public class Util {

	/**
	 * <p>
	 * Return the value collection of the specified property of the specified
	 * bean in collection, no matter which property reference format is used,
	 * with no type conversions.
	 * </p>
	 * 
	 * @param collection
	 *            the specified collection which contain beans
	 * @param propertyName
	 *            the property name need to be added in.
	 * @return the property name collection
	 * */
	public static List getObjectProperiesList(Collection collection,
			String propertyName) {
		List propertiesList = new ArrayList<Object>();
		for (Object obj : collection) {
			Object value = null;
			try {
				value = PropertyUtils.getProperty(obj, propertyName);
			} catch (IllegalAccessException e) {
				throw new SystemError(e.getMessage());
			} catch (InvocationTargetException e) {
				throw new SystemError(e.getMessage());
			} catch (NoSuchMethodException e) {
				throw new SystemError(e.getMessage());
			}
			propertiesList.add(value);
		}
		return propertiesList;
	}

	/**
	 * Generate the specified number of space as a String.
	 * @param count the number of spaces.
	 * @return String of the concatenated space
	 * */
	public static String generateSpace(int count) {
		if (count < 0) {
			throw new IllegalArgumentException(
					"count must be greater than or equal 0.");
		}
		char[] chs = new char[count];
		for (int i = 0; i < count; i++) {
			chs[i] = ' ';
		}
		return new String(chs);
	}
}
