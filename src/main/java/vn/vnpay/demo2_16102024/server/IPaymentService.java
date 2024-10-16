package vn.vnpay.demo2_16102024.server;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import vn.vnpay.demo2_16102024.dto.request.PaymentRequest;

public interface IPaymentService {
    ResponseEntity<?> validatePayment(PaymentRequest paymentRequest, BindingResult bindingResult);
}

