package com.callpipeline.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LiveKitWebhookPayload {

    @JsonAlias({"job_id", "jobId"})
    private String jobId;

    @JsonAlias({"room_id", "roomId"})
    private String roomId;

    @JsonAlias({"room", "room_name", "roomName"})
    private String roomName;

    @JsonAlias({"summary", "conversation_summary", "conversationSummary", "call_summary", "callSummary"})
    private String summary;

    @JsonAlias({"intent", "user_intent", "userIntent", "operation"})
    private String intent;

    @JsonAlias({"results", "fields", "data", "appointment", "appointment_details", "appointmentDetails", "extracted_data", "extractedData"})
    private Map<String, Object> fields = new HashMap<>();

    private Map<String, Object> metadata = new HashMap<>();

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
