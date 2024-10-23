package com.docde.domain.reservation.dto.request;


import lombok.Data;

@Data
public class ReservationRequestDto {
    //예약 사유
    private String reservationReason;

    //예약 거부 사유
    private String rejectionReason;
}
