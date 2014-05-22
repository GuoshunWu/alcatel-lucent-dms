package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.model.Translation;

public class TranslationPair {
	private Translation a;
	private Translation b;
	
	public TranslationPair(Translation a, Translation b) {
		this.a = a;
		this.b = b;
	}
	
	public Long getId() {
		return a.getId();
	}
	
	public Translation getA() {
		return a;
	}
	public void setA(Translation a) {
		this.a = a;
	}
	public Translation getB() {
		return b;
	}
	public void setB(Translation b) {
		this.b = b;
	}
}
