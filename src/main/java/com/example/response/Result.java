package com.example.response;

import lombok.Data;

/**
 * @author yyh
 */
@Data
public class Result<T> {

    private Integer code;
    private String msg;
    private T data;

    public static final Integer DEFAULT_SUCCESS_CODE = 200;
    private static final String DEFAULT_SUCCESS_MSG = "success";


    private static final Integer DEFAULT_FAIL_CODE = 500;
    private static final String DEFAULT_FAIL_MSG = "fail";


    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> fail(String msg) {
        return new Result<>(DEFAULT_FAIL_CODE, msg, null);
    }


}
