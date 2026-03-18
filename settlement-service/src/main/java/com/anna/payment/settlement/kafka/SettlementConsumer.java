package com.anna.payment.settlement.kafka;

import com.anna.common.event.PaymentEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;

@Service
public class SettlementConsumer {

    @KafkaListener(topics = "payment-topic", groupId = "settlement-group")
    public void consume(PaymentEvent event){

        System.out.println("Settlement Processing : " + event.getPaymentId());

        if(event.getAmount() > 50000){
            throw new RuntimeException("금액 초과 에러");
        }
    }

    @Recover
    public void recover(RuntimeException e, PaymentEvent event){

        System.out.println("Settlement Failed : " + event.getPaymentId());
    }
}