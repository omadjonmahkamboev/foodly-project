package com.example.fooddeliveryapp

import com.example.fooddeliveryapp.ui.data.PaymentMethod
import com.example.fooddeliveryapp.ui.data.detectPaymentMethod
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun cardPrefixes_areDetectedByPaymentSystem() {
        assertEquals(PaymentMethod.Visa, detectPaymentMethod("4111 1111 1111 1111"))
        assertEquals(PaymentMethod.MasterCard, detectPaymentMethod("5555 5555 5555 4444"))
        assertEquals(PaymentMethod.MasterCard, detectPaymentMethod("2221 0000 0000 0000"))
        assertEquals(PaymentMethod.Uzcard, detectPaymentMethod("8600 3129 2957 7175"))
        assertEquals(PaymentMethod.Uzcard, detectPaymentMethod("5614 0000 0000 0000"))
        assertEquals(PaymentMethod.Uzcard, detectPaymentMethod("6262 0000 0000 0000"))
        assertEquals(PaymentMethod.Uzcard, detectPaymentMethod("5440 0000 0000 0000"))
        assertEquals(PaymentMethod.HumoCard, detectPaymentMethod("9860 0901 0121 9724"))
    }
}
