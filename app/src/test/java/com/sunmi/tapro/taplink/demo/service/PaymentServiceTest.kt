package com.sunmi.tapro.taplink.demo.service

import org.junit.Test
import org.junit.Assert.*

/**
 * PaymentService Unit Tests
 * 
 * Tests basic functionality of the payment service
 */
class PaymentServiceTest {
    
    /**
     * Test PaymentResult's isSuccess method
     */
    @Test
    fun paymentResult_isSuccess_returnsTrue_whenCodeIsZeroAndStatusIsSuccess() {
        val result = PaymentResult(
            code = "0",
            message = "Success",
            transactionId = "TXN001",
            referenceOrderId = "ORDER001",
            transactionRequestId = "REQ001",
            transactionStatus = "SUCCESS",
            transactionType = "SALE",
            amount = 100.0,
            currency = "USD",
            authCode = "AUTH123",
            batchNo = 1,
            voucherNo = "V001",
            stan = "STAN001",
            rrn = "RRN001",
            transactionResultCode = "00",
            transactionResultMsg = "Approved",
            createTime = "2024-01-01T10:00:00",
            completeTime = "2024-01-01T10:00:05",
            description = "Test payment",
            batchCloseInfo = null
        )
        
        assertTrue(result.isSuccess())
        assertFalse(result.isProcessing())
        assertFalse(result.isFailed())
    }
    
    /**
     * Test PaymentResult's isProcessing method
     */
    @Test
    fun paymentResult_isProcessing_returnsTrue_whenStatusIsProcessing() {
        val result = PaymentResult(
            code = "0",
            message = "Processing",
            transactionId = null,
            referenceOrderId = "ORDER001",
            transactionRequestId = "REQ001",
            transactionStatus = "PROCESSING",
            transactionType = "SALE",
            amount = 100.0,
            currency = "USD",
            authCode = null,
            batchNo = null,
            voucherNo = null,
            stan = null,
            rrn = null,
            transactionResultCode = null,
            transactionResultMsg = null,
            createTime = "2024-01-01T10:00:00",
            completeTime = null,
            description = "Test payment",
            batchCloseInfo = null
        )
        
        assertFalse(result.isSuccess())
        assertTrue(result.isProcessing())
        assertFalse(result.isFailed())
    }
    
    /**
     * Test PaymentResult's isFailed method
     */
    @Test
    fun paymentResult_isFailed_returnsTrue_whenStatusIsFailed() {
        val result = PaymentResult(
            code = "1",
            message = "Failed",
            transactionId = "TXN001",
            referenceOrderId = "ORDER001",
            transactionRequestId = "REQ001",
            transactionStatus = "FAILED",
            transactionType = "SALE",
            amount = 100.0,
            currency = "USD",
            authCode = null,
            batchNo = null,
            voucherNo = null,
            stan = null,
            rrn = null,
            transactionResultCode = "05",
            transactionResultMsg = "Declined",
            createTime = "2024-01-01T10:00:00",
            completeTime = "2024-01-01T10:00:05",
            description = "Test payment",
            batchCloseInfo = null
        )
        
        assertFalse(result.isSuccess())
        assertFalse(result.isProcessing())
        assertTrue(result.isFailed())
    }
    
    /**
     * Test BatchCloseInfo data class
     */
    @Test
    fun batchCloseInfo_createsCorrectly() {
        val batchCloseInfo = BatchCloseInfo(
            totalCount = 10,
            totalAmount = 1000.0,
            totalTip = 50.0,
            totalTax = 80.0,
            totalSurchargeAmount = 20.0,
            totalServiceFee = 10.0,
            cashDiscount = 5.0,
            closeTime = "2024-01-01T23:59:59"
        )
        
        assertEquals(10, batchCloseInfo.totalCount)
        assertEquals(1000.0, batchCloseInfo.totalAmount, 0.01)
        assertEquals(50.0, batchCloseInfo.totalTip, 0.01)
        assertEquals(80.0, batchCloseInfo.totalTax, 0.01)
        assertEquals(20.0, batchCloseInfo.totalSurchargeAmount, 0.01)
        assertEquals(10.0, batchCloseInfo.totalServiceFee, 0.01)
        assertEquals(5.0, batchCloseInfo.cashDiscount, 0.01)
        assertEquals("2024-01-01T23:59:59", batchCloseInfo.closeTime)
    }
}
