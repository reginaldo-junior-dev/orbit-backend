package reginaldo.orbit.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reginaldo.orbit.api.doc.DashboardApi;
import reginaldo.orbit.api.dto.dashboard.DashboardResponse;
import reginaldo.orbit.api.service.DashboardService;

@RestController
@RequestMapping("dashboard")
@RequiredArgsConstructor
public class DashboardController implements DashboardApi {
    private final DashboardService dashboardService;

    @GetMapping
    @Override
    public ResponseEntity<DashboardResponse> getDashboard(Authentication authentication) {
        return ResponseEntity.ok(dashboardService.getDashboard(authentication.getName()));
    }
}
