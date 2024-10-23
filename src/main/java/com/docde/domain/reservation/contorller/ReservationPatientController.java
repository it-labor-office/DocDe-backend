package com.docde.domain.reservation.contorller;


import com.docde.common.Apiresponse.ApiResponse;
import com.docde.domain.reservation.dto.response.ReservationResponseDto;
import com.docde.domain.reservation.dto.request.ReservationRequestDto;
import com.docde.domain.reservation.service.ReservationPatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/doctors/{doctorId}/patients/{patientId}")
public class ReservationPatientController {

    private final ReservationPatientService reservationPatientService;


    @PostMapping("/reservations")
    public ApiResponse<ReservationResponseDto> createReservation(@PathVariable Long doctorId,
                                                                                 @PathVariable Long patientId,
                                                                                 @RequestBody ReservationRequestDto reservationRequestDto){
        ReservationResponseDto reservationResponseDto = reservationPatientService.createReservation(doctorId, patientId, reservationRequestDto);
        return ApiResponse.onSuccess(reservationResponseDto);
    }

    @PutMapping("/reservations/{reservationId}/cancel")
    public ApiResponse<ReservationResponseDto> cancelReservation(@PathVariable Long doctorId,
                                                                                 @PathVariable Long patientId,
                                                                                 @PathVariable Long reservationId){
        ReservationResponseDto reservationResponseDto = reservationPatientService.cancelReservation(doctorId,patientId,reservationId);
        return ApiResponse.onSuccess(reservationResponseDto);
    }


    @GetMapping("/reservations/{reservationId}")
    public ApiResponse<ReservationResponseDto> getReservation(@PathVariable Long doctorId,
                                                                              @PathVariable Long patientId,
                                                                              @PathVariable Long reservationId){
        ReservationResponseDto reservationResponseDto = reservationPatientService.getReservation(doctorId, patientId,reservationId);
        return ApiResponse.onSuccess(reservationResponseDto);
    }


}