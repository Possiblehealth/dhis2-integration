package com.possible.dhis2int.exception;


public class NotAvailableException extends Exception {
	
	private static final long serialVersionUID = -6373847537925482764L;

	public NotAvailableException() {
		super("This is not available");
	}
	
	public NotAvailableException(String message) {
		super(message);
	}
}
