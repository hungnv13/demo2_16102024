package vn.vnpay.demo2_16102024.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

@Data
public class PaymentRequest implements Serializable {

    @NotBlank(message = "tokenKey must not be empty")
    private String tokenKey;

    @NotBlank(message = "apiID must not be empty")
    private String apiID;

    @Pattern(regexp = "^[0-9]{10}$", message = "mobile must be a 10-digit number")
    private String mobile;

    private String bankCode = "970445"; // Mã ngân hàng mặc định

    @NotBlank(message = "accountNo must not be empty")
    private String accountNo;

    @Pattern(regexp = "\\d{14}", message = "payDate must be in format yyyyMMddHHmmss")
    private String payDate;

    private String additionalData;

    @NotNull(message = "debitAmount must not be null")
    private Integer debitAmount;

    @NotBlank(message = "respCode must not be empty")
    private String respCode;

    @NotBlank(message = "respDesc must not be empty")
    private String respDesc;

    @NotBlank(message = "traceTransfer must not be empty")
    private String traceTransfer;

    private String messageType = "1";

    @NotBlank(message = "orderCode must not be empty")
    private String orderCode;

    @NotNull(message = "realAmount must not be null")
    private Integer realAmount;

    private String promotionCode;

    private String addValue = "{\"payMethod\":\"01\",\"payMethodMMS\":1}";

    @NotBlank(message = "checkSum must not be empty")
    private String checkSum;

    @NotBlank(message = "userName must not be empty")
    private String userName;
}
