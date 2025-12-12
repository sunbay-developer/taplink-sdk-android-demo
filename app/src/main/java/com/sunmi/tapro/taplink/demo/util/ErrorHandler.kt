package com.sunmi.tapro.taplink.demo.util

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.widget.Toast

/**
 * Simple error handling utility class
 * 
 * Provides basic error handling with retry options:
 * - Shows original SDK error messages
 * - Provides retry buttons based on error type
 * - Keeps UI simple and user-friendly
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
            canRetryWithSameId = canRetryWithSameId,
            canRetryWithNewId = canRetryWithNewId,
            isTimeout = isTimeout,
            onRetryWithSameId = onRetryWithSameId,
            onRetryWithNewId = onRetryWithNewId,
            onQuery = onQuery,
            onCancel = onCancel
        )
    }

    /**
     * Show simple error dialog with original error message and retry options
     */
    private fun showSimpleErrorDialog(
        context: Context,
        errorCode: String,
        errorMessage: String,
        canRetryWithSameId: Boolean,
        canRetryWithNewId: Boolean,
        isTimeout: Boolean,
        onRetryWithSameId: (() -> Unit)?,
        onRetryWithNewId: (() -> Unit)?,
        onQuery: (() -> Unit)?,
        onCancel: (() -> Unit)?
    ) {
        // Display original error message from SDK
        val message = buildString {
            append(errorMessage)
            append("\n\nError Code: $errorCode")
        }

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
                if (onRetryWithSameId != null) {
                    builder.setNegativeButton("Retry") { _, _ ->
                        onRetryWithSameId.invoke()
                    }
                }
                builder.setNeutralButton("Cancel") { _, _ ->
                    onCancel?.invoke()
                }
            }
            
            canRetryWithSameId && onRetryWithSameId != null -> {
                // Temporary error - can retry with same ID
                builder.setPositiveButton("Retry") { _, _ ->
                    onRetryWithSameId.invoke()
                }
                builder.setNegativeButton("Cancel") { _, _ ->
                    onCancel?.invoke()
                }
            }
            
            canRetryWithNewId && onRetryWithNewId != null -> {
                // Definite failure - can retry with new ID
                builder.setPositiveButton("Try Again") { _, _ ->
                    onRetryWithNewId.invoke()
                }
                builder.setNegativeButton("Cancel") { _, _ ->
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
     * Handle connection-related errors
     */
    fun handleConnectionError(
        context: Context,
        errorCode: String,
        errorMessage: String,
        onRetry: (() -> Unit)? = null
    ) {
        Log.e(TAG, "Connection error - Code: $errorCode, Message: $errorMessage")
        
        // Show simple connection error dialog 
        val builder = AlertDialog.Builder(context)
            .setTitle("Connection Error")
            .setMessage("$errorMessage\n\nError Code: $errorCode")
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