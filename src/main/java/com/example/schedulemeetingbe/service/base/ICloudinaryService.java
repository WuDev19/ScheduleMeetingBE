package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.response.UploadSignatureResponse;

import java.io.IOException;


public interface ICloudinaryService {

    UploadSignatureResponse generateUploadSignature(String publicId);
    void delete(String publicId) throws IOException;
}
