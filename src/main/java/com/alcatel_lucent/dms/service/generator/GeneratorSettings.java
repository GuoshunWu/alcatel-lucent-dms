package com.alcatel_lucent.dms.service.generator;

public class GeneratorSettings {
	
	// whether escape Apostrophe character (0x2032) into sequence in XML properties
	private boolean escapeApostrophe = false;
    // whether convert single quote char (&#39) to apostrophe (&#8242) in XML properties
    private boolean convertApostrophe = false;

    public GeneratorSettings() {
    }

    public boolean isConvertApostrophe() {
        return convertApostrophe;
    }

    public void setConvertApostrophe(boolean convertApostrophe) {
        this.convertApostrophe = convertApostrophe;
    }

    public GeneratorSettings(boolean escapeApostrophe) {
        this.escapeApostrophe = escapeApostrophe;
    }

    public boolean isEscapeApostrophe() {
		return escapeApostrophe;
	}

	public void setEscapeApostrophe(boolean escapeApostrophe) {
		this.escapeApostrophe = escapeApostrophe;
	}

    public GeneratorSettings(boolean escapeApostrophe, boolean convertApostrophe) {
        this.escapeApostrophe = escapeApostrophe;
        this.convertApostrophe = convertApostrophe;
    }
}
