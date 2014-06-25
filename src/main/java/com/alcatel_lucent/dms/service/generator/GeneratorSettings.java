package com.alcatel_lucent.dms.service.generator;

public class GeneratorSettings {
	
	// whether escape Apostrophe character (0x2032) into sequence in XML properties
	private boolean escapeApostrophe = false;

	public boolean isEscapeApostrophe() {
		return escapeApostrophe;
	}

	public void setEscapeApostrophe(boolean escapeApostrophe) {
		this.escapeApostrophe = escapeApostrophe;
	}
}
