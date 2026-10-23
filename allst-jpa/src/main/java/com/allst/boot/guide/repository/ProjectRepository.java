package com.allst.boot.guide.repository;

import com.allst.boot.guide.entity.Project;
import com.allst.boot.guide.repository.support.GuideBaseRepository;

import java.util.Optional;

public interface ProjectRepository extends GuideBaseRepository<Project, Long> {

    Optional<Project> findByCode(String code);
}
