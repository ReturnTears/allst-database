package com.allst.es.service.impl;

import cn.hutool.core.util.StrUtil;
import com.allst.es.entity.EsUser;
import com.allst.es.entity.User;
import com.allst.es.mapper.UserMapper;
import com.allst.es.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Hutu
 * @since 2025-01-02 下午 10:09
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public List<User> selectUserList() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        return userMapper.selectList(wrapper);
    }

    @Override
    public List<EsUser> selectEsUserList() {
        SearchHits<EsUser> search = elasticsearchRestTemplate.search(Query.findAll(), EsUser.class);
        List<SearchHit<EsUser>> searchHits = search.getSearchHits();
        // 获得searchHits,进行遍历得到content
        List<EsUser> esUserList = Lists.newArrayList();
        searchHits.forEach(hit -> esUserList.add(hit.getContent()));
        System.out.println(esUserList);
        return esUserList;
    }

    @Override
    public String saveEsUser(String param) {
        if (StrUtil.isEmpty(param)) {
            return "save fail";
        }
        //String[] split = param.split("[;|,|:]");
        String[] split = param.split(":");
        EsUser user = new EsUser();
        user.setId(Long.parseLong(split[0]));
        user.setName(split[1]);
        user.setAge(Integer.valueOf(split[2]));
        user.setEmail(split[3]);
        elasticsearchRestTemplate.save(user);
        return "save success";
    }
}
