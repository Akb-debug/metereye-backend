// BaseResponse.java
package com.metereye.backend.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    private int status;
    private String message;
    private T data;

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(200, "Succès", data);
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(200, message, data);
    }

    public static <T> BaseResponse<T> created(T data) {
        return new BaseResponse<>(201, "Créé avec succès", data);
    }

    public static <T> BaseResponse<T> error(int status, String message) {
        return new BaseResponse<>(status, message, null);
    }

    public static <T> BaseResponse<T> notFound(String message) {
        return new BaseResponse<>(404, message, null);
    }

    public static <T> BaseResponse<T> badRequest(String message) {
        return new BaseResponse<>(400, message, null);
    }
}