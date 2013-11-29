package com.alcatel_lucent.dms.rest;

import java.util.Collection;

import com.alcatel_lucent.dms.model.ProductBase;

public class Products {
	private Long id;
	private String data;
	private Collection<ProductBase> products;
	
	public Products(Collection<ProductBase> products) {
		this.products = products;
	}
	
	public Long getId() {
		return -1l;
	}
	
	public Collection<ProductBase> getProducts() {
		return products;
	}
	
	public String getData() {
		return "Products";
	}

}
