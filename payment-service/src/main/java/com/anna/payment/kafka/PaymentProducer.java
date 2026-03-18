package com.anna.payment.kafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.anna.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import com.anna.common.event.PaymentEvent;

@Service
@RequiredArgsConstructor
public class PaymentProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentEvent(Payment payment){
    	PaymentEvent event = PaymentEvent.builder()
                .paymentId(payment.getId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .build();
    	
        kafkaTemplate.send("payment-topic", event);
    }

}