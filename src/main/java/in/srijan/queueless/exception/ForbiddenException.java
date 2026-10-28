package in.srijan.queueless.exception;

import org.springframework.http.HttpStatus;

/**
 * 403s: NOT_YOUR_RESOURCE (P2, A6, A8) for now. Blueprint's separate
 * ROLE_NOT_ALLOWED is an interceptor-level concern that belongs to M9's
 * real auth layer, not this service-level ownership stand-in.
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String code, String message) {
        super(HttpStatus.FORBIDDEN, code, message);
    }
}
