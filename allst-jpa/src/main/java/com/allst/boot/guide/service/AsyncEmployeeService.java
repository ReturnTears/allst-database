package com.allst.boot.guide.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncEmployeeService {

    private final EmployeeLookupService employeeLookupService;

    public AsyncEmployeeService(EmployeeLookupService employeeLookupService) {
        this.employeeLookupService = employeeLookupService;
    }

    @Async("jpaExecutor")
    public CompletableFuture<String> loadDepartmentName(Long employeeId) {
        return CompletableFuture.completedFuture(employeeLookupService.departmentName(employeeId));
    }
}
