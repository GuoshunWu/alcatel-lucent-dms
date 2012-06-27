package com.alcatel_lucent.dms.model;

import java.util.Collection;

public class Application extends BaseEntity {
	private String name;
	private ProductVersion productVersion;
	private Collection<Dictionary> dictionaries;
}
