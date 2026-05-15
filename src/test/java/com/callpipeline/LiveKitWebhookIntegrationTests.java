package com.callpipeline;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LiveKitWebhookIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsAppointmentFromLiveKitPayload() throws Exception {
        String payload = """
                {
                  "job_id": "job_123",
                  "room_id": "room_123",
                  "room": "browser-demo",
                  "intent": "CREATE_APPOINTMENT",
                  "summary": "The caller requested a dental cleaning appointment.",
                  "results": {
                    "name": { "value": "Ananya Rao" },
                    "phone": { "value": "+91 9876543210" },
                    "email": { "value": "ananya@example.com" },
                    "reason_for_visit": { "value": "Dental cleaning" },
                    "preferred_date_time": { "value": "Friday afternoon" }
                  }
                }
                """;

        mockMvc.perform(post("/api/webhooks/livekit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("CREATE_APPOINTMENT"))
                .andExpect(jsonPath("$.appointment.callerName").value("Ananya Rao"))
                .andExpect(jsonPath("$.appointment.callerPhone").value("9876543210"))
                .andExpect(jsonPath("$.appointment.reason").value("Dental cleaning"));
    }
}
