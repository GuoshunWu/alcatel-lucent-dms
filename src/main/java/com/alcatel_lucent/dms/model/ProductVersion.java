package com.alcatel_lucent.dms.model;

import java.util.Collection;

public class ProductVersion extends BaseEntity {
	private String name;
	private Product product;
	private Collection<Application> applications;
}
