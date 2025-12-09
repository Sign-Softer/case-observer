package ro.signsofter.caseobserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ro.signsofter.caseobserver.exception.portal.PortalQueryException;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("message", errorMessage != null ? errorMessage : "Validation failed"));
    }
    
    @ExceptionHandler(PortalQueryException.class)
    public ResponseEntity<Map<String, String>> handlePortalQueryException(PortalQueryException ex) {
        String errorMessage = ex.getMessage();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("message", errorMessage != null ? errorMessage : "Portal query failed"));
    }
    
    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<Map<String, String>> handleIncorrectPasswordException(IncorrectPasswordException ex) {
        String errorMessage = ex.getMessage();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("message", errorMessage != null ? errorMessage : "Current password is incorrect"));
    }
}