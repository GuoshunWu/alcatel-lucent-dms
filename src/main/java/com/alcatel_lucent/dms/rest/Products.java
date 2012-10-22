package com.alcatel_lucent.dms.rest;

import java.util.Collection;

import com.alcatel_lucent.dms.model.Product;

public class Products {
	private Long id;
	private String data;
	private Collection<Product> products;
	
	public Products(Collection<Product> products) {
		this.products = products;
	}
	
	public Long getId() {
		return -1l;
	}
	
	public Collection<Product> getProducts() {
		return products;
	}
	
	public String getData() {
		return "Products";
	}

}
