package com.company.logistics.utils;

import lombok.Getter;
import lombok.Setter;

/**
 * 公共返回参数类
 * @param <T>
 * @author wzj
 */
@Getter
@Setter
public class Result<T> {

    /**
     * 状态码
     */
    private int code;

    /**
     * 状态码信息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 构造方法
     */
    public Result(){

    }

    /**
     * 构造方法
     * @param code
     * @param message
     * @param data
     */
    public Result(int code,String message,T data){
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功
     * @param <T>
     * @return
     */
    public static <T> Result<T> success(){
        return new Result<>(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMessage(),null);
    }

    /**
     * 成功
     * @param data
     * @param <T>
     * @return
     */
    public static <T> Result<T> success(T data){
        return new Result<>(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMessage(),data);
    }

    /**
     * 失败
     * @param code
     * @param message
     * @param <T>
     * @return
     */
    public static <T> Result<T> fail(int code,String message){
        return new Result<>(code, message,null);
    }

    /**
     * 失败
     * @param code
     * @param message
     * @param data
     * @param <T>
     * @return
     */
    public static <T> Result<T> fail(int code,String message,T data){
        return new Result<>(code, message,data);
    }

    /**
     * 判断是否成功
     * @return
     */
    public boolean isSuccess(){
        return ResultEnum.SUCCESS.getCode() == this.code;
    }
}
