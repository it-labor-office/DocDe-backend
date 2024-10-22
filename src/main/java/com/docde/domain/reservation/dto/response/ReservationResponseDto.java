package com.docde.domain.reservation.dto.response;

import com.docde.domain.reservation.entity.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
public class ReservationResponseDto {
    private Long reservationId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String reservationReason;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String rejectionReason;

    private ReservationStatus reservationStatus;


    @Builder
    private ReservationResponseDto(Long reservationId, String reservationReason, String rejectionReason, ReservationStatus reservationStatus){
        this.reservationId = reservationId;
        this.reservationReason = reservationReason;
        this.rejectionReason = rejectionReason;
        this.reservationStatus = reservationStatus;
    }

    public static ReservationResponseDto of(Long reservationId, ReservationStatus reservationStatus){
        return ReservationResponseDto.builder()
                .reservationId(reservationId)
                .reservationStatus(reservationStatus)
                .build();
    }
}
