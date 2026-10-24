package com.civicpulse.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ActuatorSecurityIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("/actuator/health is publicly accessible without authentication (OPS-2)")
    void testHealthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Non-health actuator endpoints require authentication/admin privileges")
    void testNonHealthActuatorRequiresAuth() throws Exception {
        mockMvc.perform(get("/actuator/beans"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CITIZEN")
    @DisplayName("Citizen cannot access sensitive actuator endpoints (403 Forbidden)")
    void testCitizenForbiddenFromActuator() throws Exception {
        mockMvc.perform(get("/actuator/beans"))
                .andExpect(status().isForbidden());
    }
}
