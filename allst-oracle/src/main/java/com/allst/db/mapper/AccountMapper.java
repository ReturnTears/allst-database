package com.allst.db.mapper;

import com.allst.db.entity.Account;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author Hutu
 * @since 2023-07-26 下午 09:47
 */
public interface AccountMapper extends BaseMapper<Account> {

    @Select("select * from tb_account where id = #{id}")
    Account selectById(@Param("id") Long id);
}
