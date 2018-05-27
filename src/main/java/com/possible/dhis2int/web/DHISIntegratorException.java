package com.possible.dhis2int.web;

public class DHISIntegratorException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DHISIntegratorException(String message, Exception exception) {
		super(message, exception);
	}
	
	public DHISIntegratorException(String message) {
		super(message);
	}
}
