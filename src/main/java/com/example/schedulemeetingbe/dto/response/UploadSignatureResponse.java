package com.example.schedulemeetingbe.dto.response;

public record UploadSignatureResponse (
        String signature,
        String cloud_name,
        String api_key,
        long timestamp,
        String public_id,
        boolean override
) {
}
