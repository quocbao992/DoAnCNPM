package vn.doancnpm.service;

import java.util.List;

import vn.doancnpm.dto.Request.PaymentRequest;
import vn.doancnpm.dto.Response.PaymentResponse;

public interface PaymentService {

    PaymentResponse createPayment(PaymentRequest rq);
    PaymentResponse updateStatus(Long paymentId, String newStatus, String transactionRef);
    PaymentResponse getPayment(Long paymentId);
    List<PaymentResponse> getPaymentsByOrder(Long orderId);
}
