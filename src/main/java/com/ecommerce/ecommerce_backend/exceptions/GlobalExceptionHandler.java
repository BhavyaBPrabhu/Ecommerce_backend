package com.ecommerce.ecommerce_backend.exceptions;

import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(Exception.class)
	public final ResponseEntity<ErrorDetails> handleAllException
	(Exception ex, WebRequest req) throws Exception{
		
		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),ex.getMessage(),req.getDescription(false));
		return new ResponseEntity<ErrorDetails> (errorDetails,HttpStatus.INTERNAL_SERVER_ERROR);
		
	}
	@ExceptionHandler(ResourceNotFoundException.class)
	public final ResponseEntity<ErrorDetails> handleResourceNotFoundException
	(ResourceNotFoundException ex, WebRequest req) throws Exception{
		
		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),ex.getMessage(),req.getDescription(false));
		return new ResponseEntity<ErrorDetails> (errorDetails,HttpStatus.NOT_FOUND);
		
	}
	
	@ExceptionHandler(ResourceAlreadyExistsException.class)
	public final ResponseEntity<ErrorDetails> handleResourceAlreadyExistsException
	(ResourceAlreadyExistsException ex, WebRequest req) throws Exception{
		
		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),ex.getMessage(),req.getDescription(false));
		return new ResponseEntity<ErrorDetails> (errorDetails,HttpStatus.CONFLICT);
		
	}
	
	@ExceptionHandler(EmptyListException.class)
	public final ResponseEntity<ErrorDetails> handleEmptyListException
	(EmptyListException ex, WebRequest req) throws Exception{
		
		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),ex.getMessage(),req.getDescription(false));
		return new ResponseEntity<ErrorDetails> (errorDetails,HttpStatus.NOT_FOUND);
		
	}
	
	@Override // Overriding the same function in  ResponseEntityExceptionHandler
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {


	    StringBuilder errors = new StringBuilder();
	    ex.getBindingResult().getFieldErrors().forEach(error -> {
	    	errors.append(error.getField())
	    			.append(": ")
	    			.append(error.getDefaultMessage())
	    			.append(";");
	    });
		ErrorDetails errorDetails = new 
				ErrorDetails(LocalDateTime.now(), 
						"Validation Failed : "+errors.toString(),request.getDescription(false));
		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
	}

	
	
}
