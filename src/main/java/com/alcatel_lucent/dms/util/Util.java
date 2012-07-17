/**
 * 
 */
package com.alcatel_lucent.dms.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;

import com.alcatel_lucent.dms.SystemError;

/**
 * @author guoshunw
 * 
 */
public class Util {
	
	public static final int UTF8_BOM_LENGTH = 3;
	public static final int UTF16_BOM_LENGTH = 2;

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
	
	
	/**
	 * Detect the encoding of a File by BOM(byte order mark).
	 * 
	 * @author Guoshun.Wu Date: 2012-07-01
	 * 
	 * @param file
	 *            given File
	 * @return file encoding
	 * */
	public static String detectEncoding(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		byte[] buf=new byte[UTF8_BOM_LENGTH];
		is.read(buf);
		is.close();
		return detectEncoding(buf);
	}

	public static String detectEncoding(byte[] bom){
		byte[] utf8BOM = new byte[] { (byte) 0xef, (byte) 0xbb, (byte) 0xbf, };
		byte[] utf16LEBOM = new byte[] { (byte) 0xff, (byte) 0xfe };
		byte[] utf16BEBOM = new byte[] { (byte) 0xfe, (byte) 0xff };

		if (Arrays.equals(utf8BOM, bom)) {
			return "UTF-8";
		}
		if (Arrays.equals(utf16LEBOM, Arrays.copyOf(bom, UTF16_BOM_LENGTH))
				|| Arrays.equals(utf16BEBOM,
						Arrays.copyOf(bom, UTF16_BOM_LENGTH))) {
			return "UTF-16";
		}
		return "ISO-8859-1";
	}
}
