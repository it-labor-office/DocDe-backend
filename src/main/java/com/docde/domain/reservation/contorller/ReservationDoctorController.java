package com.docde.domain.reservation.contorller;

import com.docde.common.Apiresponse.ApiResponse;
import com.docde.domain.reservation.dto.request.ReservationRequestDto;
import com.docde.domain.reservation.dto.response.ReservationResponseDto;
import com.docde.domain.reservation.service.ReservationDoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/doctors/{doctorId}/patients/{patientId}")
public class ReservationDoctorController {

    private final ReservationDoctorService reservationDoctorService;


    @PutMapping("/reservations/{reservationId}/approval")
    public ApiResponse<ReservationResponseDto> approvalReservation(@PathVariable Long doctorId,
                                                                                   @PathVariable Long patientId,
                                                                                   @PathVariable Long reservationId){
        ReservationResponseDto reservationResponseDto = reservationDoctorService.approvalReservation(doctorId, patientId, reservationId);
        return ApiResponse.onCreated(reservationResponseDto);
    }

    @PutMapping("/reservations/{reservationId}/refusal")
    public ApiResponse<ReservationResponseDto> refusalReservation(@PathVariable Long doctorId,
                                                                                  @PathVariable Long patientId,
                                                                                  @PathVariable Long reservationId,
                                                                                  @RequestBody ReservationRequestDto reservationRequestDto){
        ReservationResponseDto reservationResponseDto = reservationDoctorService.refusalReservation(doctorId, patientId, reservationId, reservationRequestDto);
        return ApiResponse.onCreated(reservationResponseDto);
    }
}
