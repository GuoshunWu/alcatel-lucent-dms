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
			if (!isValid(c, lang)) {
				//System.out.println("###Invalid char: '" + c + "', ub:" + getUnicodeBlock(c) + " for language " + lang);
				return false;
			}
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
		if (ub.equals("BASIC_LATIN") || ub.equals("GENERAL_PUNCTUATION")) return true;
		if (lang.startsWith("Chinese")) {
			return ub.startsWith("CJK_") || ub.equals("HALFWIDTH_AND_FULLWIDTH_FORMS");
		} else if (lang.equals("Korean")) {
			return ub.equals("HANGUL_SYLLABLES") || ub.equals("HALFWIDTH_AND_FULLWIDTH_FORMS") || ub.startsWith("CJK_");
		} else if (lang.equals("Japanese")) {
			return ub.equals("HIRAGANA") || ub.equals("KATAKANA") || ub.equals("HALFWIDTH_AND_FULLWIDTH_FORMS") || ub.startsWith("CJK_");
		} else if (lang.equals("Arabic")) {
			return ub.startsWith("ARABIC");
		} else if (lang.equals("Russian")) {
			return ub.equals("CYRILLIC");
		} else if (lang.equals("Greek")) {
			return ub.equals("GREEK");
		} else {
			return ub.startsWith("LATIN_");
		}
	}
}
