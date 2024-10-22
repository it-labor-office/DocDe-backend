package com.docde.domain.reservation.contorller;

import com.docde.domain.reservation.dto.request.ReservationRequestDto;
import com.docde.domain.reservation.dto.response.ReservationResponseDto;
import com.docde.domain.reservation.dto.ResponseDto;
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
    public ResponseEntity<ResponseDto<ReservationResponseDto>> approvalReservation(@PathVariable Long doctorId,
                                                                                   @PathVariable Long patientId,
                                                                                   @PathVariable Long reservationId){
        ReservationResponseDto reservationResponseDto = reservationDoctorService.approvalReservation(doctorId, patientId, reservationId);
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.of(HttpStatus.OK.value(), HttpStatus.OK.toString(), reservationResponseDto));
    }

    @PutMapping("/reservations/{reservationId}/refusal")
    public ResponseEntity<ResponseDto<ReservationResponseDto>> refusalReservation(@PathVariable Long doctorId,
                                                                                  @PathVariable Long patientId,
                                                                                  @PathVariable Long reservationId,
                                                                                  @RequestBody ReservationRequestDto reservationRequestDto){
        ReservationResponseDto reservationResponseDto = reservationDoctorService.refusalReservation(doctorId, patientId, reservationId, reservationRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.of(HttpStatus.OK.value(), HttpStatus.OK.toString(), reservationResponseDto));
    }
}
