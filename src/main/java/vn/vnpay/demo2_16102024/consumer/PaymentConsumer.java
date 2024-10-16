package vn.vnpay.demo2_16102024.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import vn.vnpay.demo2_16102024.config.RabbitConfig;
import vn.vnpay.demo2_16102024.constant.ErrorCodeEnum;
import vn.vnpay.demo2_16102024.dto.request.PaymentRequest;
import vn.vnpay.demo2_16102024.dto.response.PaymentResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Component
public class PaymentConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentConsumer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    public PaymentConsumer(JdbcTemplate jdbcTemplate,
                           RedisTemplate<String, String> redisTemplate,
                           RabbitTemplate rabbitTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = new ObjectMapper();
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void receiveMessage(String message) {
        try {
            logger.info("Received message: {}", message);
            PaymentRequest paymentRequest = objectMapper.readValue(message, PaymentRequest.class);
            String tokenKey = paymentRequest.getTokenKey();
            String todayKey = String.format("token:%s:%s", tokenKey, LocalDate.now().format(DateTimeFormatter.ISO_DATE));

            if (redisTemplate.hasKey(todayKey)) {
                logger.warn("TokenKey already exists for today: {}", tokenKey);
                PaymentResponse response = receivePaymentResponse(paymentRequest, ErrorCodeEnum.TOKEN_EXISTS_ERROR);
                sendResponse(response);
                return;
            }

            redisTemplate.opsForValue().set(todayKey, "exists", 1, TimeUnit.DAYS);
            String sql = "INSERT INTO payments (tokenKey, apiID, mobile, bankCode, accountNo, payDate, additionalData, debitAmount, respCode, respDesc, traceTransfer, messageType, checkSum, orderCode, userName, realAmount, promotionCode, addValue) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            jdbcTemplate.update(sql,
                    paymentRequest.getTokenKey(),
                    paymentRequest.getApiID(),
                    paymentRequest.getMobile(),
                    paymentRequest.getBankCode(),
                    paymentRequest.getAccountNo(),
                    paymentRequest.getPayDate(),
                    paymentRequest.getAdditionalData(),
                    paymentRequest.getDebitAmount(),
                    paymentRequest.getRespCode(),
                    paymentRequest.getRespDesc(),
                    paymentRequest.getTraceTransfer(),
                    paymentRequest.getMessageType(),
                    paymentRequest.getCheckSum(),
                    paymentRequest.getOrderCode(),
                    paymentRequest.getUserName(),
                    paymentRequest.getRealAmount(),
                    paymentRequest.getPromotionCode(),
                    paymentRequest.getAddValue()
            );

            logger.info("Inserted payment request: {}", paymentRequest);
            PaymentResponse paymentResponse = receivePaymentResponse(paymentRequest, ErrorCodeEnum.SUCCESS);
            sendResponse(paymentResponse);
        } catch (Exception e) {
            logger.error("Error processing payment request: {}", e.getMessage(), e);
            PaymentResponse errorResponse = receivePaymentResponse(null, ErrorCodeEnum.SYSTEM_ERROR);
            sendResponse(errorResponse);
        }
    }

    private PaymentResponse receivePaymentResponse(PaymentRequest paymentRequest, ErrorCodeEnum errorCode) {
        PaymentResponse response = new PaymentResponse();
        response.setTokenKey(paymentRequest != null ? paymentRequest.getTokenKey() : null);
        response.setRespCode(errorCode.getCode());
        response.setStatus(errorCode.getMessage());
        logger.info("Created payment response: {}", response);
        return response;
    }

    private void sendResponse(PaymentResponse response) {
        try {
            String responseJson = objectMapper.writeValueAsString(response);
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, RabbitConfig.ROUTING_KEY, responseJson);
            logger.info("Sent payment response to queue: {}", responseJson);
        } catch (Exception ex) {
            logger.error("Error sending response: {}", ex.getMessage(), ex);
        }
    }
}
