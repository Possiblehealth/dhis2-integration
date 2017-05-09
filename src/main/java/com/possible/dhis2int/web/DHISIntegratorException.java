package com.possible.dhis2int.web;

public class DHISIntegratorException extends Throwable {
	
	public DHISIntegratorException(String message, Exception exception) {
		super(message, exception);
	}
	
	public DHISIntegratorException(String message) {
		super(message);
	}
}
