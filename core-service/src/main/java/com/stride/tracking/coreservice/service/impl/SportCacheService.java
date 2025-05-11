package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.coreservice.constant.CacheName;
import com.stride.tracking.coreservice.constant.Message;
import com.stride.tracking.coreservice.model.Sport;
import com.stride.tracking.coreservice.repository.SportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SportCacheService {
    private final SportRepository sportRepository;

    @Cacheable(value = CacheName.SPORT_BY_ID, key = "#sportId")
    @Transactional(readOnly = true)
    public Sport findSportById(String sportId) {
        return sportRepository.findById(sportId).orElseThrow(
                () -> new StrideException(HttpStatus.BAD_REQUEST, Message.SPORT_NOT_FOUND)
        );
    }
}
