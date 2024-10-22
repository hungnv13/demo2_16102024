package vn.vnpay.demo2_16102024.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import vn.vnpay.demo2_16102024.constant.PaymentConstant;

public class PaymentUtils {

    public static String formatPayDate(LocalDateTime payDate) {
        if (payDate == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PaymentConstant.TIMESTAMP_FORMATTER);
        return payDate.format(formatter);
    }
}
