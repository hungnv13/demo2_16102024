package vn.vnpay.demo2_16102024.constant;

public enum ErrorCodeEnum {
    SUCCESS("00", "Success"),
    VALIDATION_ERROR("01", "Validation Error"),
    TOKEN_EXISTS_ERROR("02", "TokenKey already exists"),
    SYSTEM_ERROR("99", "System Error");

    private final String code;
    private final String message;

    ErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
