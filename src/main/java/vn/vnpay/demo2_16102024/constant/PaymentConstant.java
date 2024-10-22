package vn.vnpay.demo2_16102024.constant;

import vn.vnpay.demo2_16102024.config.RabbitConfig;

public class PaymentConstant {
    public static final String TIMESTAMP_FORMATTER = "yyyyMMddHHmmss";
    public static final String ERROR_REAL_AMOUNT_GREATER_THAN_DEBIT = "The real amount must be less than or equal to the debit amount.";
    public static final String ERROR_PROMOTION_CODE = "Invalid promotion code.";
    public static final String ERROR_VALIDATION = "Validation error occurred.";
    public static final String ERROR_TOKEN_EXISTS = "TokenKey already exists.";
    public static final String ERROR_SYSTEM = "System error occurred while processing JSON.";
    public static final String QUEUE_NAME = "paymentQueue";
}
