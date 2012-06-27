package com.alcatel_lucent.dms.model;

import java.util.Collection;

public class Language extends BaseEntity {
	private String name;
	private String isoCode;
	private Collection<Charset> charsets;
}
