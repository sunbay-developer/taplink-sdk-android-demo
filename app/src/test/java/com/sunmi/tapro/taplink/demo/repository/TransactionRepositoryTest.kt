package com.sunmi.tapro.taplink.demo.repository

import com.sunmi.tapro.taplink.demo.model.Transaction
import com.sunmi.tapro.taplink.demo.model.TransactionStatus
import com.sunmi.tapro.taplink.demo.model.TransactionType
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * TransactionRepository Unit Tests
 * 
 * Tests transaction repository functionality including amount updates
 */
class TransactionRepositoryTest {
    
    @Before
    fun setUp() {
        // Clear all transactions before each test
        TransactionRepository.clearAllTransactions()
    }
    
    /**
     * Test updating transaction with amounts from query result
     */
    @Test
    fun updateTransactionWithAmounts_updatesAllAmountFields_whenQueryReturnsAmounts() {
        // Create initial transaction
        val initialTransaction = Transaction(
            transactionRequestId = "REQ_001",
            transactionId = null,
            referenceOrderId = "ORDER_001",
            type = TransactionType.SALE,
            amount = 10.0,
            totalAmount = null,
            currency = "USD",
            status = TransactionStatus.PROCESSING,
            timestamp = System.currentTimeMillis(),
            surchargeAmount = null,
            tipAmount = null,
            cashbackAmount = null,
            serviceFee = null
        )
        
        // Add transaction to repository
        assertTrue(TransactionRepository.addTransaction(initialTransaction))
        
        // Simulate query result with updated amounts
        val updateResult = TransactionRepository.updateTransactionWithAmounts(
            transactionRequestId = "REQ_001",
            status = TransactionStatus.SUCCESS,
            transactionId = "TXN_12345",
            authCode = "AUTH_123",
            orderAmount = 10.01, // Updated order amount
            totalAmount = 16.01, // Total amount including all fees
            surchargeAmount = 1.0,
            tipAmount = 2.0,
            cashbackAmount = 1.0,
            serviceFee = 2.0
        )
        
        assertTrue("Update should succeed", updateResult)
        
        // Verify the transaction was updated with all amounts
        val updatedTransaction = TransactionRepository.getTransactionByRequestId("REQ_001")
        assertNotNull("Transaction should exist", updatedTransaction)
        
        updatedTransaction?.let { txn ->
            assertEquals("Status should be updated", TransactionStatus.SUCCESS, txn.status)
            assertEquals("Transaction ID should be updated", "TXN_12345", txn.transactionId)
            assertEquals("Auth code should be updated", "AUTH_123", txn.authCode)
            assertEquals("Order amount should be updated", 10.01, txn.amount, 0.001)
            assertEquals("Total amount should be updated", 16.01, txn.totalAmount!!, 0.001)
            assertEquals("Surcharge amount should be updated", 1.0, txn.surchargeAmount!!, 0.001)
            assertEquals("Tip amount should be updated", 2.0, txn.tipAmount!!, 0.001)
            assertEquals("Cashback amount should be updated", 1.0, txn.cashbackAmount!!, 0.001)
            assertEquals("Service fee should be updated", 2.0, txn.serviceFee!!, 0.001)
        }
    }
    
    /**
     * Test updating transaction preserves existing amounts when query returns null amounts
     */
    @Test
    fun updateTransactionWithAmounts_preservesExistingAmounts_whenQueryReturnsNullAmounts() {
        // Create initial transaction with existing amounts
        val initialTransaction = Transaction(
            transactionRequestId = "REQ_002",
            transactionId = null,
            referenceOrderId = "ORDER_002",
            type = TransactionType.SALE,
            amount = 15.0,
            totalAmount = 20.0,
            currency = "USD",
            status = TransactionStatus.PROCESSING,
            timestamp = System.currentTimeMillis(),
            surchargeAmount = 2.0,
            tipAmount = 3.0,
            cashbackAmount = null,
            serviceFee = null
        )
        
        // Add transaction to repository
        assertTrue(TransactionRepository.addTransaction(initialTransaction))
        
        // Simulate query result with null amounts (should preserve existing)
        val updateResult = TransactionRepository.updateTransactionWithAmounts(
            transactionRequestId = "REQ_002",
            status = TransactionStatus.SUCCESS,
            transactionId = "TXN_67890",
            authCode = "AUTH_456",
            orderAmount = null, // Should preserve existing
            totalAmount = null, // Should preserve existing
            surchargeAmount = null, // Should preserve existing
            tipAmount = null, // Should preserve existing
            cashbackAmount = null, // Should preserve existing (was null)
            serviceFee = null // Should preserve existing (was null)
        )
        
        assertTrue("Update should succeed", updateResult)
        
        // Verify the transaction preserved existing amounts
        val updatedTransaction = TransactionRepository.getTransactionByRequestId("REQ_002")
        assertNotNull("Transaction should exist", updatedTransaction)
        
        updatedTransaction?.let { txn ->
            assertEquals("Status should be updated", TransactionStatus.SUCCESS, txn.status)
            assertEquals("Transaction ID should be updated", "TXN_67890", txn.transactionId)
            assertEquals("Auth code should be updated", "AUTH_456", txn.authCode)
            assertEquals("Order amount should be preserved", 15.0, txn.amount, 0.001)
            assertEquals("Total amount should be preserved", 20.0, txn.totalAmount!!, 0.001)
            assertEquals("Surcharge amount should be preserved", 2.0, txn.surchargeAmount!!, 0.001)
            assertEquals("Tip amount should be preserved", 3.0, txn.tipAmount!!, 0.001)
            assertNull("Cashback amount should remain null", txn.cashbackAmount)
            assertNull("Service fee should remain null", txn.serviceFee)
        }
    }
    
    /**
     * Test updating transaction with partial amount updates
     */
    @Test
    fun updateTransactionWithAmounts_updatesPartialAmounts_whenQueryReturnsPartialAmounts() {
        // Create initial transaction
        val initialTransaction = Transaction(
            transactionRequestId = "REQ_003",
            transactionId = null,
            referenceOrderId = "ORDER_003",
            type = TransactionType.SALE,
            amount = 25.0,
            totalAmount = 30.0,
            currency = "USD",
            status = TransactionStatus.PROCESSING,
            timestamp = System.currentTimeMillis(),
            surchargeAmount = 2.0,
            tipAmount = 3.0,
            cashbackAmount = null,
            serviceFee = null
        )
        
        // Add transaction to repository
        assertTrue(TransactionRepository.addTransaction(initialTransaction))
        
        // Simulate query result with partial amount updates
        val updateResult = TransactionRepository.updateTransactionWithAmounts(
            transactionRequestId = "REQ_003",
            status = TransactionStatus.SUCCESS,
            transactionId = "TXN_11111",
            authCode = "AUTH_789",
            orderAmount = null, // Should preserve existing (25.0)
            totalAmount = 35.0, // Should update
            surchargeAmount = null, // Should preserve existing (2.0)
            tipAmount = 5.0, // Should update
            cashbackAmount = 1.5, // Should update (was null)
            serviceFee = null // Should preserve existing (was null)
        )
        
        assertTrue("Update should succeed", updateResult)
        
        // Verify the transaction has mixed preserved and updated amounts
        val updatedTransaction = TransactionRepository.getTransactionByRequestId("REQ_003")
        assertNotNull("Transaction should exist", updatedTransaction)
        
        updatedTransaction?.let { txn ->
            assertEquals("Status should be updated", TransactionStatus.SUCCESS, txn.status)
            assertEquals("Transaction ID should be updated", "TXN_11111", txn.transactionId)
            assertEquals("Auth code should be updated", "AUTH_789", txn.authCode)
            assertEquals("Order amount should be preserved", 25.0, txn.amount, 0.001)
            assertEquals("Total amount should be updated", 35.0, txn.totalAmount!!, 0.001)
            assertEquals("Surcharge amount should be preserved", 2.0, txn.surchargeAmount!!, 0.001)
            assertEquals("Tip amount should be updated", 5.0, txn.tipAmount!!, 0.001)
            assertEquals("Cashback amount should be updated", 1.5, txn.cashbackAmount!!, 0.001)
            assertNull("Service fee should remain null", txn.serviceFee)
        }
    }
}