package com.company.logistics.utils;

import lombok.Getter;

/**
 * 状态码枚举
 * @author wzj
 * @date 2020/12/723:16
 */
@Getter
public enum ResultEnum {

    SUCCESS(200,"成功");
    /**
     * 状态码
     */
    private int code;

    /**
     * 状态码信息
     */
    private String message;

    ResultEnum(int code,String message){
        this.code = code;
        this.message = message;
    }
}
