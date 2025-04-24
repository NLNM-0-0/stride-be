package com.stride.tracking.coreservice.repository;

import com.stride.tracking.coreservice.model.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SportRepository extends JpaRepository<Sport, String>, JpaSpecificationExecutor<Sport> {
}
