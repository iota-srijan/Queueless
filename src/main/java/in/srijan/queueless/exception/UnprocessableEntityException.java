package in.srijan.queueless.exception;

import org.springframework.http.HttpStatus;

/**
 * 422s cover several rule ids (S2, A1, A2, A3, A6), each with its own `code`.
 */
public class UnprocessableEntityException extends BusinessException {

    public UnprocessableEntityException(String code, String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, code, message);
    }
}
