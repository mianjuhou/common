package com.fda.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class Result<E> {
    private Integer code = 1;
    private String message = "";
    private Long total;
    private List<E> data;
    public static final Integer CODE_SUCCESS = 1;
    public static final Integer CODE_FAILED = 2;
    public static final Integer CODE_PARTIAL_SUCCESS = 3;
    public static final Integer CODE_LOCK = 4;
    public static final Integer CODE_DIFFERENT_PLACE_LOGIN = 5;
    public static final Integer CODE_SERVER_DOWN = 6;
    public static final Integer CODE_PASSWORD_OVERDUE = 7;
    public static final Integer CODE_TOKEN_ERROR = 8;

    public static <E> Result<E> success() {
        return Result.<E>builder().code(CODE_SUCCESS).message("操作成功").build();
    }

    public static <E> Result<E> success(String message) {
        return Result.<E>builder().code(CODE_SUCCESS).message(message).build();
    }

    public static <E> Result<E> success(String message, E data) {
        return Result.<E>builder().code(CODE_SUCCESS).message(message).total(1L).data(Arrays.asList(data)).build();
    }

    public static <E> Result<E> success(String message, List<E> data) {
        return Result.<E>builder().code(CODE_SUCCESS).message(message).total(Long.valueOf(data.size())).data(data).build();
    }

    public static <E> Result<E> success(String message, List<E> data, Long total) {
        return Result.<E>builder().code(CODE_SUCCESS).message(message).total(total).data(data).build();
    }

    public static <E> Result<E> partialSuccess(String msg, E data) {
        return error(3, msg, data);
    }

    public static <E> Result<E> partialSuccess(String msg, List<E> data) {
        return error(3, msg, data);
    }

    public static <E> Result<E> error() {
        return Result.<E>builder().code(CODE_FAILED).message("操作失败").build();
    }

    public static <E> Result<E> error(Integer code) {
        return Result.<E>builder().code(code).message("操作失败").build();
    }

    public static <E> Result<E> error(String message) {
        return Result.<E>builder().code(CODE_FAILED).message(message).total(1L).build();
    }

    public static <E> Result<E> error(Integer code, String message) {
        return Result.<E>builder().code(code).message(message).total(1L).build();
    }

    public static <E> Result<E> error(Integer code, String message, E data) {
        return Result.<E>builder().code(code).message(message).total(1L).data(Arrays.asList(data)).build();
    }

    public static <E> Result error(String message, List data) {
        return Result.<E>builder().code(CODE_FAILED).message(message).total(Long.valueOf(data.size())).data(data).build();
    }

    public static <E> Result<E> error(Integer code, String message, List<E> data) {
        return Result.<E>builder().code(code).message(message).total(Long.valueOf(data.size())).data(data).build();
    }

    public static <E> Result<E> error(Integer code, String message, List<E> data, Long total) {
        return Result.<E>builder().code(code).message(message).total(total).data(data).build();
    }

}
