package com.alcatel_lucent.dms.service;

public class ImportSettings {
	
	private boolean autoCreateLang = true;
	
	private boolean removeOldLabels = false;
	
	public boolean isAutoCreateLang() {
		return autoCreateLang;
	}
	
	/**
	 * Indicate whether to create languages if the dictionary has no language,
     * if true, add languages from any other dictionary in the application. Default is true.
	 * @param autoCreateLang
	 */
	public void setAutoCreateLang(boolean autoCreateLang) {
		this.autoCreateLang = autoCreateLang;
	}

	public boolean isRemoveOldLabels() {
		return removeOldLabels;
	}

	/**
	 * Indicate whether to remove old label which exists in existing dictionary but doesn't exist in the imported dictionary
	 * Default value is false.
	 */
	public void setRemoveOldLabels(boolean removeOldLabels) {
		this.removeOldLabels = removeOldLabels;
	}
	
	
	
}
