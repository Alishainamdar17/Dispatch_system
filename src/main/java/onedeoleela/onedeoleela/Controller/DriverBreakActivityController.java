package onedeoleela.onedeoleela.Controller;

import lombok.RequiredArgsConstructor;
import onedeoleela.onedeoleela.Entity.DriverBreakActivity;
import onedeoleela.onedeoleela.Service.DriverBreakActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/driver-break")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://103.6.120.246:3030")
public class DriverBreakActivityController {

    private final DriverBreakActivityService breakService;

    // ✅ Start Break
    @PostMapping("/start")
    public ResponseEntity<?> startBreak(
            @RequestParam Long tripId,
            @RequestParam(required = false) String reason) {

        try {
            DriverBreakActivity result = breakService.startBreak(tripId, reason);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to start break");
        }
    }

    // ✅ End Break
    @PostMapping("/end")
    public ResponseEntity<?> endBreak(@RequestParam Long tripId) {

        try {
            DriverBreakActivity result = breakService.endBreak(tripId);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to end break");
        }
    }

    // ✅ Get Total Break Time
    @GetMapping("/total")
    public ResponseEntity<?> getTotalBreak(@RequestParam Long tripId) {

        try {
            long totalMinutes = breakService.getTotalBreakMinutes(tripId);
            return ResponseEntity.ok(totalMinutes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch break time");
        }
    }

    // ✅ Get Break History
    @GetMapping("/history/{tripId}")
    public ResponseEntity<?> getBreakHistory(@PathVariable Long tripId) {

        try {
            List<DriverBreakActivity> history = breakService.getBreaksByTrip(tripId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch break history");
        }
    }
}