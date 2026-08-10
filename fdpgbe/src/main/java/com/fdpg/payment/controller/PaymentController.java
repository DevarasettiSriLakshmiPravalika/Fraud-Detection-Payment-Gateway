package com.fdpg.payment.controller;

import com.fdpg.common.response.ApiResponse;
import com.fdpg.payment.dto.PaymentRequest;
import com.fdpg.payment.dto.PaymentResponse;
import com.fdpg.payment.dto.TransactionDetail;
import com.fdpg.payment.dto.TransactionSummary;
import com.fdpg.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody PaymentRequest request,
            HttpServletRequest httpRequest) {
        String username  = getAuthenticatedUsername();
        String ipAddress = extractClientIp(httpRequest);
        ApiResponse<PaymentResponse> response = paymentService.createPayment(username, request, ipAddress);
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionSummary>>> getUserPayments() {
        String username = getAuthenticatedUsername();
        ApiResponse<List<TransactionSummary>> response = paymentService.getUserPayments(username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionDetail>> getPaymentDetail(
            @PathVariable String transactionId) {
        String username = getAuthenticatedUsername();
        ApiResponse<TransactionDetail> response = paymentService.getPaymentDetail(username, transactionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        if (response.getErrors() != null &&
                response.getErrors().stream().anyMatch(e -> e.contains("permission"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // ---- Private helpers ----

    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    /**
     * Extracts the real client IP from the request.
     * Checks X-Forwarded-For (proxy/load balancer) first, then X-Real-IP, then the direct socket address.
     * The frontend must never supply the IP; it is always derived server-side.
     */
    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}
