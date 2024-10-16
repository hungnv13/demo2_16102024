package vn.vnpay.demo2_16102024.dto.response;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class PaymentResponse implements Serializable {
    private String tokenKey;
    private String respCode;
    private String status;
    private String payDate;

    public PaymentResponse() {
    }

    public PaymentResponse(String tokenKey, String respCode, String status, LocalDateTime payDate) {
        this.tokenKey = tokenKey;
        this.respCode = respCode;
        this.status = status;
        this.payDate = (payDate != null) ? formatPayDate(payDate) : null;
    }

    private String formatPayDate(LocalDateTime payDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return payDate.format(formatter);
    }
}
