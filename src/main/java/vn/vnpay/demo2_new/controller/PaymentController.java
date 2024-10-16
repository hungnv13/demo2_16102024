package vn.vnpay.demo2_new.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vnpay.demo2_new.dto.request.PaymentRequest;
import vn.vnpay.demo2_new.server.IPaymentService;


@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private IPaymentService iPaymentService;

    public PaymentController(IPaymentService iPaymentService) {
        this.iPaymentService = iPaymentService;
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validatePayment(@Valid @RequestBody PaymentRequest paymentRequest, BindingResult bindingResult) {
        return iPaymentService.validatePayment(paymentRequest, bindingResult);
    }
}
