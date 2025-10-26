package ro.signsofter.caseobserver.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ro.signsofter.caseobserver.exception.portal.PortalQueryException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(
                ex.getBindingResult().getAllErrors().get(0).getDefaultMessage()
        );
    }
    
    @ExceptionHandler(PortalQueryException.class)
    public ResponseEntity<?> handlePortalQueryException(PortalQueryException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}