package com.alcatel_lucent.dms.model;

import java.util.Collection;

public class Product extends BaseEntity {
	private String name;
	private Collection<ProductVersion> versions;
}
