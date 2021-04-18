package com.company.logistics.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author wzj
 * @date 2021/3/24 21:51
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageParam implements Serializable {

    @NotNull(message = "页码不允许为空")
    @Min(value = 1L, message = "页码长度最小为1")
    public Integer page;

    @NotNull(message = "每页显示数量不允许为空")
    @Min(value = 1L, message = "每页显示数量长度最小为1")
    public Integer size;
}
