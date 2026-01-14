package com.ecommerce.ecommerce_backend.exceptions;

public class BadRequestException extends RuntimeException {

	public BadRequestException(String message) {
		super(message);
	}
}
