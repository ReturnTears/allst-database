package com.allst.boot.guide.web;

import com.allst.boot.guide.entity.EmployeeStatus;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

/**
 * 把扁平的查询参数组装成搜索对象。复杂条件不适合继续堆在控制器方法签名上。
 */
public class EmployeeSearchArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(BindEmployeeSearch.class)
                && EmployeeSearchCriteria.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        EmployeeSearchCriteria criteria = new EmployeeSearchCriteria();
        criteria.setName(text(webRequest, "name"));
        criteria.setDepartmentName(text(webRequest, "departmentName"));
        criteria.setMinAge(integer(webRequest, "minAge"));
        criteria.setMaxAge(integer(webRequest, "maxAge"));
        criteria.setMinSalary(decimal(webRequest, "minSalary"));
        String status = text(webRequest, "status");
        if (status != null) {
            try {
                criteria.setStatus(EmployeeStatus.valueOf(status));
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无法识别的员工状态: " + status);
            }
        }
        return criteria;
    }

    private String text(NativeWebRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Integer integer(NativeWebRequest request, String name) {
        String value = text(request, name);
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, name + " 必须是整数");
        }
    }

    private BigDecimal decimal(NativeWebRequest request, String name) {
        String value = text(request, name);
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, name + " 必须是数字");
        }
    }
}
