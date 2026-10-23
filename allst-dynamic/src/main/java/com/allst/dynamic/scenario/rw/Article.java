package com.allst.dynamic.scenario.rw;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_article")
public class Article {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String title;
    private String author;
    private LocalDateTime createdAt;
}
