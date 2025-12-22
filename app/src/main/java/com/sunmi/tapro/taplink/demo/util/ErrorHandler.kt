package com.sunmi.tapro.taplink.demo.util

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson

/**
 * Enhanced error handling utility class for cross-device connection modes
 * 
 * Provides comprehensive error handling using SDK provided error information:
 * - Uses SDK provided error codes and messages directly
 * - Shows user-friendly error messages without additional mapping
 * - Provides retry buttons based on error type and connection mode
 */
object ErrorHandler {

    private const val TAG = "ErrorHandler"

    /**
     * Handle payment error with simple retry logic
     */
    fun handlePaymentError(
        context: Context,
        errorCode: String,
        errorMessage: String,
        onRetryWithSameId: (() -> Unit)? = null,
        onRetryWithNewId: (() -> Unit)? = null,
        onQuery: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null
    ) {
        Log.e(TAG, "Payment error - Code: $errorCode, Message: $errorMessage")

        // Check if this error code can be retried
        val canRetryWithSameId = RetryManager.isTemporaryError(errorCode)
        val canRetryWithNewId = RetryManager.isDefiniteFailure(errorCode)
        val isTimeout = errorCode == "E10"

        // Show simple error dialog with original error message
        showSimpleErrorDialog(
            context = context,
            errorCode = errorCode,
            errorMessage = errorMessage,
            isTimeout = isTimeout,
            onQuery = onQuery,
            onCancel = onCancel
        )
    }

    data class ErrorResponse(
        val traceId: String,
        val eventCode: String,
        val errorCode: String,
        val eventMsg: String
    )

    /**
     * Show simple error dialog with original error message and retry options
     */
    private fun showSimpleErrorDialog(
        context: Context,
        errorCode: String,
        errorMessage: String,
        isTimeout: Boolean,
        onQuery: (() -> Unit)?,
        onCancel: (() -> Unit)?
    ) {
        // Display original error message from SDK
        val message = "$errorMessage\n\nError Code: $errorCode"

        val builder = AlertDialog.Builder(context)
            .setTitle("Payment Error")
            .setMessage(message)
            .setCancelable(false)

        // Add appropriate buttons based on error type and available actions
        when {
            isTimeout && onQuery != null -> {
                // Timeout error - offer query first
                builder.setPositiveButton("Query Status") { _, _ ->
                    onQuery.invoke()
                }
                builder.setNeutralButton("Cancel") { _, _ ->
                    onCancel?.invoke()
                }
            }
            
            else -> {
                // No retry available - just show OK
                builder.setPositiveButton("OK") { _, _ ->
                    onCancel?.invoke()
                }
            }
        }

        builder.show()
    }

    /**
     * Handle connection errors using SDK provided error information
     * Shows original SDK error message without additional mapping
     */
    fun handleConnectionError(
        context: Context,
        errorCode: String,
        errorMessage: String,
        onRetry: (() -> Unit)? = null
    ) {
        Log.e(TAG, "Connection error - Code: $errorCode, Message: $errorMessage")
        
        // Use SDK provided error message directly
        val fullMessage = "$errorMessage\n\nError Code: $errorCode"
        
        val builder = AlertDialog.Builder(context)
            .setTitle("Connection Error")
            .setMessage(fullMessage)
            .setCancelable(false)
            
        if (onRetry != null) {
            builder.setPositiveButton("Retry") { _, _ ->
                onRetry.invoke()
            }
            builder.setNegativeButton("Cancel", null)
        } else {
            builder.setPositiveButton("OK", null)
        }

        builder.show()
    }

    /**
     * Show simple toast message
     */
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}