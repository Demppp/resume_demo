package com.classmanagement.dto;

import lombok.Data;

@Data
public class AiSearchResponse {
    private Boolean success;
    private String route;
    private String description;
    private String message;
    
    public static AiSearchResponse success(String route, String description) {
        AiSearchResponse response = new AiSearchResponse();
        response.setSuccess(true);
        response.setRoute(route);
        response.setDescription(description);
        return response;
    }
    
    public static AiSearchResponse error(String message) {
        AiSearchResponse response = new AiSearchResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}

