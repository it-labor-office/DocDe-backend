package com.docde.domain.reservation.contorller;

import com.docde.common.Apiresponse.ApiResponse;
import com.docde.domain.reservation.dto.request.ReservationRequestDto;
import com.docde.domain.reservation.dto.response.ReservationResponseDto;
import com.docde.domain.reservation.service.ReservationDoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/doctors/{doctorId}/patients/{patientId}")
public class ReservationDoctorController {

    private final ReservationDoctorService reservationDoctorService;


    /**
     * 예약 승인
     * @param doctorId
     * @param patientId
     * @param reservationId
     * @return 예약 아이디, 예약 상태
     */
    @PutMapping("/reservations/{reservationId}/approval")
    public ApiResponse<ReservationResponseDto> approvalReservation(@PathVariable Long doctorId,
                                                                   @PathVariable Long patientId,
                                                                   @PathVariable Long reservationId,
                                                                   @AuthenticationPrincipal UserDetails userDetails){
        ReservationResponseDto reservationResponseDto = reservationDoctorService.approvalReservation(doctorId, patientId, reservationId, userDetails);
        return ApiResponse.onCreated(reservationResponseDto);
    }

    /**
     * 예약 거부
     * @param doctorId
     * @param patientId
     * @param reservationId
     * @param reservationRequestDto
     * @return 예약 아이디, 예약 거부 사유, 예약 상태
     */
    @PutMapping("/reservations/{reservationId}/refusal")
    public ApiResponse<ReservationResponseDto> refusalReservation(@PathVariable Long doctorId,
                                                                  @PathVariable Long patientId,
                                                                  @PathVariable Long reservationId,
                                                                  @RequestBody ReservationRequestDto reservationRequestDto,
                                                                  @AuthenticationPrincipal UserDetails userDetails){
        ReservationResponseDto reservationResponseDto = reservationDoctorService.refusalReservation(doctorId, patientId, reservationId, reservationRequestDto, userDetails);
        return ApiResponse.onCreated(reservationResponseDto);
    }


    /**
     * 진료 완료 후 예약 상태 변경
     * @param doctorId
     * @param patientId
     * @param reservationId
     * @return 예약 아이디, 예약 상태 변경
     */
    @PutMapping("/reservations/{reservationId}/done")
    public ApiResponse<ReservationResponseDto> doneReservation(@PathVariable Long doctorId,
                                                               @PathVariable Long patientId,
                                                               @PathVariable Long reservationId,
                                                               @AuthenticationPrincipal UserDetails userDetails){
        ReservationResponseDto reservationResponseDto = reservationDoctorService.doneReservation(doctorId,patientId,reservationId,userDetails);
        return ApiResponse.onCreated(reservationResponseDto);
    }
}
