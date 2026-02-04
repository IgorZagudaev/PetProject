package ru.samara.pet.portfolio_service.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessException extends RuntimeException {

    private String errorCode;
    private int statusCode;
    private Map<String, String> details;

    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.statusCode = HttpStatus.BAD_REQUEST.value();
    }

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = HttpStatus.BAD_REQUEST.value();
    }

    public BusinessException(String message, String errorCode, int statusCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public BusinessException withDetail(String key, String value) {
        if (details == null) {
            details = new HashMap<>();
        }
        details.put(key, value);
        return this;
    }
}
