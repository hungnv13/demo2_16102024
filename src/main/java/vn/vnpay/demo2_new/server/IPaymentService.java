package vn.vnpay.demo2_new.server;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import vn.vnpay.demo2_new.dto.request.PaymentRequest;

public interface IPaymentService {
    ResponseEntity<?> validatePayment(PaymentRequest paymentRequest, BindingResult bindingResult);
}

