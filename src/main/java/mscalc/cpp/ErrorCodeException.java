package mscalc.cpp;

public class ErrorCodeException extends RuntimeException {
    private final int errorCode;

    public ErrorCodeException(int errorCode) {
        super("Error code: " + errorCode);
        this.errorCode = errorCode;
    }

    public int errorCode() {
        return errorCode;
    }
}
