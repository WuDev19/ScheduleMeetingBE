package com.example.schedulemeetingbe.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.schedulemeetingbe.dto.response.UploadSignatureResponse;
import com.example.schedulemeetingbe.service.base.ICloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements ICloudinaryService {

    @Value("${CLOUDINARY_NAME}")
    private String cloudName;

    @Value("${CLOUDINARY_API_KEY}")
    private String apiKey;

    @Value("${CLOUDINARY_SECRET_KEY}")
    private String secret_key;

    private final Cloudinary cloudinary;

    // tạo chữ ký cho client, client sẽ gửi theo đúng cấu trúc này thì mới có thể upload được ảnh lên cloudinary
    @Override
    public UploadSignatureResponse generateUploadSignature(String publicId) {
        long timestamp = System.currentTimeMillis() / 1000L;
        Map<String, Object> params = new HashMap<>();
        params.put("timestamp", timestamp);
        params.put("public_id", publicId);
        params.put("overwrite", true);
        String signature = cloudinary.apiSignRequest(params, secret_key);
        return new UploadSignatureResponse(
                signature,
                cloudName,
                apiKey,
                timestamp,
                publicId,
                true
        );
    }

    //xóa ảnh trên cloudinary dựa trên publicId
    @Override
    public void delete(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

}
