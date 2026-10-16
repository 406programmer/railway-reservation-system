package com.railway.trainservice.dto;

import com.railway.trainservice.entity.enums.ScheduleStatus;

public record UpdateScheduleRequest(
        ScheduleStatus status,
        Integer delayMinutes,
        String platformNumber
) {}
