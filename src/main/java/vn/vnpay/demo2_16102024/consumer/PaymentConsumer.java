package vn.vnpay.demo2_16102024.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import vn.vnpay.demo2_16102024.config.RabbitConfig;
import vn.vnpay.demo2_16102024.constant.ErrorCodeEnum;
import vn.vnpay.demo2_16102024.dto.request.PaymentRequest;
import vn.vnpay.demo2_16102024.dto.response.PaymentResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
            logger.info("Received message at {}: {}", LocalDateTime.now(), message);
            PaymentRequest paymentRequest = objectMapper.readValue(message, PaymentRequest.class);
            String tokenKey = paymentRequest.getTokenKey();

            String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            logger.info("Checking for existing tokenKey: {} on date: {}", tokenKey, today);

            String sqlCheck = "SELECT COUNT(*) FROM payments WHERE tokenKey = ? AND DATE(payDate) = ?";
            Integer count = jdbcTemplate.queryForObject(sqlCheck, new Object[]{tokenKey, today}, Integer.class);

            if (null != count && 0 < count) {
                logger.warn("TokenKey already exists for today: {}", tokenKey);
                PaymentResponse response = receivePaymentResponse(paymentRequest, ErrorCodeEnum.TOKEN_EXISTS_ERROR);
                sendResponse(response);
                return;
            }

            String sqlInsert = "INSERT INTO payments (tokenKey, apiID, mobile, bankCode, accountNo, payDate, additionalData, debitAmount, respCode, respDesc, traceTransfer, messageType, checkSum, orderCode, userName, realAmount, promotionCode, addValue) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            logger.info("Inserting payment request for tokenKey: {} on date: {}", paymentRequest.getTokenKey(), paymentRequest.getPayDate());

            jdbcTemplate.update(sqlInsert,
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
        } catch (DataAccessException e) {
            logger.error("Database error while processing tokenKey: {}. Error: {}", e.getMessage(), e);
            PaymentResponse errorResponse = receivePaymentResponse(null, ErrorCodeEnum.SYSTEM_ERROR);
            sendResponse(errorResponse);
        } catch (Exception e) {
            logger.error("Error processing payment request for tokenKey: {}. Error: {}", e.getMessage(), e);
            PaymentResponse errorResponse = receivePaymentResponse(null, ErrorCodeEnum.SYSTEM_ERROR);
            sendResponse(errorResponse);
        }
    }

    private PaymentResponse receivePaymentResponse(PaymentRequest paymentRequest, ErrorCodeEnum errorCode) {
        PaymentResponse response = new PaymentResponse();
        response.setTokenKey(null != paymentRequest ? paymentRequest.getTokenKey() : null);
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
