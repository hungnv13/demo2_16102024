package vn.vnpay.demo2_16102024.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.vnpay.demo2_16102024.config.RabbitConfig;
import vn.vnpay.demo2_16102024.constant.ErrorCodeEnum;
import vn.vnpay.demo2_16102024.dto.request.PaymentRequest;
import vn.vnpay.demo2_16102024.dto.response.PaymentResponse;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class PaymentService implements IPaymentService {

    private static final Logger logger = LogManager.getLogger(PaymentService.class);

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public PaymentService(RabbitTemplate rabbitTemplate, RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public ResponseEntity<?> validatePayment(@Valid PaymentRequest paymentRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return handleValidationErrors(bindingResult);
        }

        if (isRealAmountGreaterThanDebitAmount(paymentRequest)) {
            return createErrorResponse(paymentRequest.getTokenKey(), ErrorCodeEnum.VALIDATION_ERROR, "The real amount must be less than or equal to the debit amount.");
        }

        if (isPromotionCodeRequired(paymentRequest)) {
            return createErrorResponse(paymentRequest.getTokenKey(), ErrorCodeEnum.VALIDATION_ERROR, "Invalid promotion code.");
        }

        String tokenKey = paymentRequest.getTokenKey();
        String todayKey = String.format("token:%s:%s", tokenKey, LocalDate.now().format(DateTimeFormatter.ISO_DATE));

        if (redisTemplate.hasKey(todayKey)) {
            logger.warn("TokenKey {} already exists for today", tokenKey);
            return createErrorResponse(tokenKey, ErrorCodeEnum.TOKEN_EXISTS_ERROR, "TokenKey already exists.");
        }

        return sendMessageToQueue(paymentRequest, todayKey);
    }

    private ResponseEntity<?> handleValidationErrors(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        for (FieldError error : fieldErrors) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        logger.warn("Validation Error: {}", errors);
        return createErrorResponse(null, ErrorCodeEnum.VALIDATION_ERROR, "Validation error occurred.");
    }

    private boolean isRealAmountGreaterThanDebitAmount(PaymentRequest paymentRequest) {
        return paymentRequest.getRealAmount() != null && paymentRequest.getDebitAmount() != null
                && paymentRequest.getRealAmount() > paymentRequest.getDebitAmount();
    }

    private boolean isPromotionCodeRequired(PaymentRequest paymentRequest) {
        return paymentRequest.getPromotionCode() != null
                && !paymentRequest.getPromotionCode().trim().isEmpty()
                && paymentRequest.getDebitAmount().equals(paymentRequest.getRealAmount());
    }

    private ResponseEntity<?> sendMessageToQueue(PaymentRequest paymentRequest, String todayKey) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(paymentRequest);
            rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_NAME, jsonMessage);
            logger.info("Sent PaymentRequest to RabbitMQ: {}", jsonMessage);
            redisTemplate.opsForValue().set(todayKey, "exists", 1, TimeUnit.DAYS);
            logger.info("Saved tokenKey {} in Redis for today", paymentRequest.getTokenKey());
            return new ResponseEntity<>(new PaymentResponse(paymentRequest.getTokenKey(), ErrorCodeEnum.SUCCESS.getCode(), ErrorCodeEnum.SUCCESS.getMessage(), LocalDateTime.now()), HttpStatus.OK);
        } catch (JsonProcessingException e) {
            logger.error("Error sending message to RabbitMQ: {}", e.getMessage(), e);
            return createErrorResponse(null, ErrorCodeEnum.SYSTEM_ERROR, "System error occurred while processing JSON.");
        }
    }

    private ResponseEntity<PaymentResponse> createErrorResponse(String tokenKey, ErrorCodeEnum errorCode, String customMessage) {
        PaymentResponse errorResponse = new PaymentResponse(tokenKey, errorCode.getCode(), customMessage, LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
