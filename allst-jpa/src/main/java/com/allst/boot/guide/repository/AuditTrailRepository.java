package com.allst.boot.guide.repository;

import com.allst.boot.guide.entity.AuditTrail;
import com.allst.boot.guide.repository.support.GuideBaseRepository;

import java.util.List;

public interface AuditTrailRepository extends GuideBaseRepository<AuditTrail, Long> {

    List<AuditTrail> findByEntityNameAndAction(String entityName, String action);
}
