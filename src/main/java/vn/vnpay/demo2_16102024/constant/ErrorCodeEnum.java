package vn.vnpay.demo2_16102024.constant;

public enum ErrorCodeEnum {
    SUCCESS("00", "Thành công"),
    VALIDATION_ERROR("01", "Lỗi validate"),
    TOKEN_EXISTS_ERROR("02", "TokenKey đã tồn tại"),
    JSON_PROCESSING_ERROR("03", "Lỗi xử lý JSON"),
    SYSTEM_ERROR("04", "Lỗi hệ thống");

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
