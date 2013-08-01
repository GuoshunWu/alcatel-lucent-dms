package com.alcatel_lucent.dms.rest;


@SuppressWarnings("serial")
public class RESTException extends RuntimeException {
	public RESTException(Exception cause) {
		super(cause);
	}
	
	public RESTException(String message) {
		super(message);
	}
}
