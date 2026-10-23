package com.allst.boot.guide.repository.support;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * 项目内统一的仓库基类。业务仓库继承它，而不是各自再声明一套 save / findById。
 */
@NoRepositoryBean
public interface GuideBaseRepository<T, ID> extends JpaRepository<T, ID> {

    /**
     * 用数据库里的最新行覆盖当前持久化上下文中的实体，丢弃尚未刷新的内存修改。
     */
    void refresh(T entity);
}
