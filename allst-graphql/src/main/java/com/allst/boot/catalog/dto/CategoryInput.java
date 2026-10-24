package com.allst.boot.catalog.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class CategoryInput {

    @NotBlank(message = "不能为空")
    @Size(max = 40, message = "最长 40")
    private String name;

    private Long parentId;
}
