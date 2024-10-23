package com.docde.domain.reservation.dto.response;

import com.docde.domain.reservation.entity.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReservationResponseDto {
    private Long reservationId;

    private String reservationReason;

    private String rejectionReason;

    private ReservationStatus reservationStatus;


    public ReservationResponseDto(Long reservationId, String reservationReason, String rejectionReason, ReservationStatus reservationStatus) {
        this.reservationId = reservationId;
        if(reservationReason != null){
            this.reservationReason = reservationReason;
        }else if(rejectionReason != null){
            this.rejectionReason = rejectionReason;
        }
        this.reservationStatus = reservationStatus;
    }

    public ReservationResponseDto(Long reservationId, ReservationStatus reservationStatus) {
        this.reservationId = reservationId;
        this.reservationStatus = reservationStatus;
    }


    public static ReservationResponseDto reservationReason(Long reservationId, ReservationStatus reservationStatus){
        return new ReservationResponseDto(reservationId, reservationStatus);
    }

    public static ReservationResponseDto reservationReason(Long reservationId, ReservationStatus reservationStatus, String reservationReason){
        return new ReservationResponseDto(reservationId, reservationReason,null, reservationStatus);
    }

    public static ReservationResponseDto rejectReservation(Long reservationId, ReservationStatus reservationStatus, String rejectionReason){
        return new ReservationResponseDto(reservationId, null, rejectionReason, reservationStatus);
    }
}
