package com.railway.reservationservice.client;

import com.railway.reservationservice.client.dto.UserProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for user-service.
 * Used to resolve the authenticated user's email to their numeric userId.
 *
 * PRIVACY: Returns id and email only (from UserResponse). Auth required: JWT. PII fields: email, name.
 * GDPR: Legal basis: contract. DPA: N/A (internal service).
 */
@FeignClient(name = "user-service", configuration = UserClientConfig.class)
public interface UserClient {

    /**
     * Fetch a user's profile by their email address.
     * The email comes from the validated JWT subject — not from client-supplied input.
     */
    @GetMapping("/api/users/email/{email}")
    UserProfileDto getUserByEmail(@PathVariable("email") String email);
}
