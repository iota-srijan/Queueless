package in.srijan.queueless.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String httpmessage;
    private final String message;
    private final String path;
    private final String requestId;
    private final Map<String, String> fieldErrors;

    public ErrorResponse(LocalDateTime timestamp, int status, String error, String httpmessage,
                          String message, String path, String requestId,
                          Map<String, String> fieldErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.httpmessage = httpmessage;
        this.message = message;
        this.path = path;
        this.requestId = requestId;
        this.fieldErrors = fieldErrors;
    }



    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getHttpMessage() {
        return httpmessage;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public String getRequestId() {
        return requestId;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
