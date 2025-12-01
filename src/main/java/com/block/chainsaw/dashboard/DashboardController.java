package com.block.chainsaw.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // 대시보드 데이터 조회 API
    @GetMapping
    public ResponseEntity<DashboardDTO> getDashboard(@AuthenticationPrincipal String email){
        System.out.println("대시보드 요청 수신: " + email);
        DashboardDTO data = dashboardService.getDashboardData(email);
        return ResponseEntity.ok(data);
    }
}
