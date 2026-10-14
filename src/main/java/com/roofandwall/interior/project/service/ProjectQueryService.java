package com.roofandwall.interior.project.service;

import com.roofandwall.interior.project.dto.ProjectListItem;
import com.roofandwall.interior.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProjectQueryService {

    private final ProjectRepository projectRepository;

    public ProjectQueryService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<ProjectListItem> findAll(long companyId) {
        return projectRepository.findAll(companyId);
    }
}
