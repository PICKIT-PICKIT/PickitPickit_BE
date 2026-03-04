package PickitPickit.global.exception;


import PickitPickit.global.response.ErrorStatus;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final ErrorStatus errorStatus;

    public ApiException(ErrorStatus errorStatus) {
        super(errorStatus.getMessage());
        this.errorStatus = errorStatus;
    }

    public ApiException(ErrorStatus errorStatus, String overrideMessage) {
        super(overrideMessage);
        this.errorStatus = errorStatus;
    }
}
