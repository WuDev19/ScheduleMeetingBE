package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.response.UploadSignatureResponse;

import java.io.IOException;
import java.util.Map;


public interface ICloudinaryService {

    UploadSignatureResponse generateUploadSignature(String publicId);
    Map delete(String publicId) throws IOException;
}
