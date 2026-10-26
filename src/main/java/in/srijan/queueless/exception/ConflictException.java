package in.srijan.queueless.exception;

import org.springframework.http.HttpStatus;

/**
 * 409s cover several different rule ids (D2, A1, S2, A5, D4, ...), each with its own
 * `code`, so the code is passed in rather than fixed like ResourceNotFoundException's.
 */
public class ConflictException extends BusinessException {

    public ConflictException(String code, String message) {
        super(HttpStatus.CONFLICT, code, message);
    }
}
