package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.model.Translation;
import com.google.common.base.Objects;

public class TranslationPair {
	private Translation a;
	private Translation b;

    private String take;

    public static final String A = "A";
    public static final String B = "B";


    public TranslationPair() {
    }

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
    public String getTake() {
        return take;
    }
    public void setTake(String take) {
        this.take = take;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("a", a.getId())
                .add("b", b.getId())
                .add("take", take)
                .toString();
    }
}
