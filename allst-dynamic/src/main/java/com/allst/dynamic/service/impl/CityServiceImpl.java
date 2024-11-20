package com.allst.dynamic.service.impl;

import com.allst.dynamic.mapper.CityMapper;
import com.allst.dynamic.model.City;
import com.allst.dynamic.service.CityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author Hutu
 * @since 2024-11-20 下午 10:11
 */
@Service
public class CityServiceImpl extends ServiceImpl<CityMapper, City> implements CityService {
}
