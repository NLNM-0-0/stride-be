package com.stride.tracking.coreservice.repository;

import com.stride.tracking.coreservice.model.Route;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, String>, JpaSpecificationExecutor<Route> {
    @Query("SELECT r " +
            "FROM routes r " +
            "WHERE r.userId IS NULL " +
            "AND function('ST_DWithin', r.geometry, :geometry, 1) = true " +
            "AND function('ST_HausdorffDistance', r.geometry, :geometry) <= 0.0001 " +
            "ORDER BY function('ST_HausdorffDistance', r.geometry, :geometry) ASC")
    Optional<Route> findMostSimilarRoute(@Param("geometry") Geometry geometry);
}