package com.anna.payment.controller;
import com.anna.payment.entity.Payment;
import com.anna.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public Payment createPayment(
            @RequestHeader("Idempotency-Key") String key,
            @RequestBody Payment payment){
        return paymentService.createPayment(key,payment);
    }
    
    @PostMapping("/{id}/success")
    public Payment success(@PathVariable Long id){
        return paymentService.completePayment(id);
    }

    @PostMapping("/{id}/fail")
    public Payment fail(@PathVariable Long id){
        return paymentService.failPayment(id);
    }
}