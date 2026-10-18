package com.railway.paymentservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "razorpay.key.id=rzp_test_dummy",
    "razorpay.key.secret=dummy_secret_key_1234567890",
    "jwt.secret=your-256-bit-secret-key-here-must-be-at-least-32-chars-long",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver"
})
class PaymentServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
