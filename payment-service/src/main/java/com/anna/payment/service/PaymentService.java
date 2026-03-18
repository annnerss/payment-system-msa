package com.anna.payment.service;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.anna.payment.entity.Payment;
import com.anna.payment.entity.PaymentStatus;
import com.anna.payment.kafka.PaymentProducer;
import com.anna.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentProducer paymentProducer;
    private final RedisTemplate<String,Object> redisTemplate;    
    private final RedissonClient redissonClient;

    public Payment createPayment(String key, Payment payment){
        String redisKey = "payment:" + key;

        // 1️. Idempotency 체크
        if(redisTemplate.hasKey(redisKey)){
            throw new RuntimeException("Duplicate Request");
        }

        // 2️. 분산락
        String lockKey = "lock:payment:" + payment.getUserId();
        RLock lock = redissonClient.getLock(lockKey);

        try{
            boolean available = lock.tryLock(5,10,TimeUnit.SECONDS);

            if(!available){
                throw new RuntimeException("결제 진행 중");
            }

            redisTemplate.opsForValue().set(redisKey,"done",10,TimeUnit.MINUTES);
            payment.setStatus(PaymentStatus.PENDING);
            Payment saved = paymentRepository.save(payment);
            paymentProducer.sendPaymentEvent(saved);
            return saved;
        } catch (InterruptedException e){
            throw new RuntimeException(e);
        } finally {
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }

    public Payment completePayment(Long paymentId){
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow();
        payment.setStatus(PaymentStatus.SUCCESS);
        return paymentRepository.save(payment);
    }

    public Payment failPayment(Long paymentId){
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow();
        payment.setStatus(PaymentStatus.FAILED);
        return paymentRepository.save(payment);
    }
}