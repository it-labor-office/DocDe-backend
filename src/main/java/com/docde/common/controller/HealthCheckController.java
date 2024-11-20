package com.docde.common.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
public class HealthCheckController {

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() throws UnknownHostException {
        // 서버 상태가 정상일 때 "OK"
        String ipAddress = InetAddress.getLocalHost().getHostAddress();
        return ResponseEntity.ok("OK ip-" + ipAddress);
    }
}