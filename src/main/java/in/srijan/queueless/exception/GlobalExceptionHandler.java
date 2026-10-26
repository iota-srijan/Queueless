package in.srijan.queueless.exception;

import in.srijan.queueless.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException
            (BusinessException ex, HttpServletRequest request){


        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getCode(),
                ex.getMessage(),
                request.getRequestURI(),
                UUID.randomUUID().toString(),
                null


        );

        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidationException
            (MethodArgumentNotValidException ex,
             HttpServletRequest request){

        Map<String,String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(
                fieldError -> fieldErrors.put(fieldError.getField()
                        ,fieldError.getDefaultMessage()));


        ErrorResponse errorResponse =
                new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),

                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        //here we dont write ex.getmessage() as
                        // want the error to be  the exact HTTP message
                        //(like in caps, eg 'BAD_REQUEST'//
                        "VALIDATION_FAILED",
                        "Validation Failed",
                        request.getRequestURI(),
                        UUID.randomUUID().toString(),
                        fieldErrors
                );


        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException
            (Exception ex,
             HttpServletRequest request){
        ErrorResponse errorResponse =
                new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        "INTERNAL_ERROR",
                        "Something went wrong",
                        request.getRequestURI(),
                        UUID.randomUUID().toString(),
                        null);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    };


}
