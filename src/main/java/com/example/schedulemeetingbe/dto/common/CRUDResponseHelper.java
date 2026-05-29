package com.example.schedulemeetingbe.dto.common;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.utils.TimeUtils;

import java.util.Map;

public class CRUDResponseHelper {
    private CRUDResponseHelper() {
    }

    public static Map<String, Object> createSuccess() {
        return Map.of(StringCommon.CREATED_AT, TimeUtils.dateTimeFormat());
    }

    public static Map<String, Object> updateSuccess() {
        return Map.of(StringCommon.UPDATED_AT, TimeUtils.dateTimeFormat());
    }

    public static Map<String, Object> deleteSuccess() {
        return Map.of(StringCommon.DELETED_AT, TimeUtils.dateTimeFormat());
    }

    public static Map<String, Object> modifySuccess() {
        return Map.of(StringCommon.MODIFY_AT, TimeUtils.dateTimeFormat());
    }

}
