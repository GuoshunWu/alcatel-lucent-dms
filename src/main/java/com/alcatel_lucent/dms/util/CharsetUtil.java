package com.alcatel_lucent.dms.util;

public class CharsetUtil {
	
	/**
	 * Get unicode block name of a character.
	 * @param c character
	 * @return unicode block name, see Character.UnicodeBlock
	 */
	public static String getUnicodeBlock(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		return ub == null ? null : ub.toString();
	}
	
	/**
	 * Check all characters in a string is valid in specified language.
	 * @param text text
	 * @param lang language name
	 * @return valid or not
	 */
	public static boolean isValid(String text, String lang) {
		for (Character c : text.toCharArray()) {
			if (!isValid(c, lang)) return false;
		}
		return true;
	}
	
	/**
	 * Check a character is valid in specified language.
	 * @param c character
	 * @param lang language name
	 * @return valid or not
	 */
	public static boolean isValid(char c, String lang) {
		String ub = getUnicodeBlock(c);
		if (ub == null) return false;
		if (ub.equals("BASIC_LATIN")) return true;
		if (lang.startsWith("Chinese") || lang.equals("Japanese")) {
			return ub.startsWith("CJK_");
		} else if (lang.equals("Korean")) {
			return ub.equals("HANGUL_SYLLABLES") || ub.startsWith("CJK_");
		} else if (lang.equals("Arabic")) {
			return ub.startsWith("ARABIC");
		} else if (lang.equals("Russian")) {
			return ub.equals("CYRILLIC");
		} else {
			return ub.equals("LATIN_1_SUPPLEMENT");
		}
	}
}
