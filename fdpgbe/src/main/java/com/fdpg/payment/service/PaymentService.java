package com.fdpg.payment.service;

import com.fdpg.payment.dto.PaymentRequest;
import com.fdpg.payment.dto.PaymentResponse;
import com.fdpg.payment.dto.TransactionDetail;
import com.fdpg.payment.dto.TransactionSummary;
import com.fdpg.common.response.ApiResponse;

import java.util.List;

public interface PaymentService {

    /**
     * @param ipAddress server-extracted client IP — never from frontend payload
     */
    ApiResponse<PaymentResponse> createPayment(String username, PaymentRequest request, String ipAddress);

    ApiResponse<List<TransactionSummary>> getUserPayments(String username);

    ApiResponse<TransactionDetail> getPaymentDetail(String username, String transactionId);
}
