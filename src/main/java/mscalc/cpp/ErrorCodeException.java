package mscalc.cpp;

public class ErrorCodeException extends RuntimeException {
    private final int errorCode;

    public ErrorCodeException(int errorCode) {
        this.errorCode = errorCode;
    }
}
