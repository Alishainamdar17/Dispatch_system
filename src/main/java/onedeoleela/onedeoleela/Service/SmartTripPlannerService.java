package onedeoleela.onedeoleela.Service;

import lombok.RequiredArgsConstructor;
import onedeoleela.onedeoleela.Entity.VehicleRequisition;
import onedeoleela.onedeoleela.Entity.RequisitionStatus;
import onedeoleela.onedeoleela.Repository.VehicleRequisitionRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SmartTripPlannerService {

    private final VehicleRequisitionRepository requisitionRepository;

    private static final long TIME_WINDOW = 60; // minutes
    private static final double PICKUP_RADIUS = 25; // km
    private static final double MAX_DETOUR = 25; // km
    private static final double ROUTE_CORRIDOR = 20; // km

    public List<List<VehicleRequisition>> generateSmartGroups() {

        List<VehicleRequisition> pending =
                requisitionRepository.findByStatusAndTripIsNull(RequisitionStatus.PENDING);

        pending.sort(
                Comparator.comparing(VehicleRequisition::getRequisitionDate)
                        .thenComparing(VehicleRequisition::getRequisitionTime)
        );

        List<List<VehicleRequisition>> groups = new ArrayList<>();

        for (VehicleRequisition req : pending) {

            boolean added = false;

            if (!hasCoordinates(req)) {
                List<VehicleRequisition> single = new ArrayList<>();
                single.add(req);
                groups.add(single);
                continue;
            }

            for (List<VehicleRequisition> group : groups) {

                VehicleRequisition base = findGroupBase(group);

                if (hasCoordinates(base)
                        && isTimeCompatible(base, req)
                        && isPickupNearby(base, req)
                        && isSameDirection(base, req)
                        && isOnTheWay(base, req)
                        && isNearRoute(base, req)) {

                    group.add(req);
                    sortGroup(group);
                    added = true;
                    break;
                }
            }

            if (!added) {

                List<VehicleRequisition> newGroup = new ArrayList<>();
                newGroup.add(req);
                groups.add(newGroup);
            }
        }

        return groups;
    }

    // ---------------- BASE REQUISITION ----------------

    private VehicleRequisition findGroupBase(List<VehicleRequisition> group) {

        return group.stream()
                .min(Comparator.comparing(VehicleRequisition::getRequisitionTime))
                .orElse(group.get(0));
    }

    // ---------------- SORT GROUP ----------------

    private void sortGroup(List<VehicleRequisition> group) {

        group.sort(
                Comparator.comparing(VehicleRequisition::getRequisitionTime)
        );
    }

    // ---------------- COORDINATE CHECK ----------------

    private boolean hasCoordinates(VehicleRequisition r) {

        return r.getStartLat() != null &&
                r.getStartLng() != null &&
                r.getEndLat() != null &&
                r.getEndLng() != null;
    }

    // ---------------- TIME WINDOW CHECK ----------------

    private boolean isTimeCompatible(VehicleRequisition a, VehicleRequisition b) {

        LocalDateTime t1 =
                LocalDateTime.of(a.getRequisitionDate(), a.getRequisitionTime());

        LocalDateTime t2 =
                LocalDateTime.of(b.getRequisitionDate(), b.getRequisitionTime());

        long minutes =
                Math.abs(Duration.between(t1, t2).toMinutes());

        return minutes <= TIME_WINDOW;
    }

    // ---------------- PICKUP DISTANCE ----------------

    private boolean isPickupNearby(VehicleRequisition a, VehicleRequisition b) {

        double distance =
                haversine(
                        a.getStartLat(), a.getStartLng(),
                        b.getStartLat(), b.getStartLng()
                );

        return distance <= PICKUP_RADIUS;
    }

    // ---------------- SAME DIRECTION ----------------

    private boolean isSameDirection(VehicleRequisition a, VehicleRequisition b) {

        double dLat1 = a.getEndLat() - a.getStartLat();
        double dLng1 = a.getEndLng() - a.getStartLng();

        double dLat2 = b.getEndLat() - b.getStartLat();
        double dLng2 = b.getEndLng() - b.getStartLng();

        return (dLat1 * dLat2 + dLng1 * dLng2) > 0;
    }

    // ---------------- DETOUR CHECK ----------------

    private boolean isOnTheWay(VehicleRequisition a, VehicleRequisition b) {

        double startToPickup =
                haversine(
                        a.getStartLat(), a.getStartLng(),
                        b.getStartLat(), b.getStartLng()
                );

        double pickupToDrop =
                haversine(
                        b.getStartLat(), b.getStartLng(),
                        b.getEndLat(), b.getEndLng()
                );

        double dropToEnd =
                haversine(
                        b.getEndLat(), b.getEndLng(),
                        a.getEndLat(), a.getEndLng()
                );

        double fullRoute =
                haversine(
                        a.getStartLat(), a.getStartLng(),
                        a.getEndLat(), a.getEndLng()
                );

        double newRoute =
                startToPickup + pickupToDrop + dropToEnd;

        return newRoute <= (fullRoute + MAX_DETOUR);
    }

    // ---------------- ROUTE CORRIDOR CHECK ----------------

    private boolean isNearRoute(VehicleRequisition base, VehicleRequisition req) {

        double distanceFromLine = distancePointToLine(
                base.getStartLat(),
                base.getStartLng(),
                base.getEndLat(),
                base.getEndLng(),
                req.getStartLat(),
                req.getStartLng()
        );

        return distanceFromLine <= ROUTE_CORRIDOR;
    }

    // ---------------- POINT TO ROUTE DISTANCE ----------------

    private double distancePointToLine(
            double x1, double y1,
            double x2, double y2,
            double px, double py) {

        double A = px - x1;
        double B = py - y1;
        double C = x2 - x1;
        double D = y2 - y1;

        double dot = A * C + B * D;
        double len_sq = C * C + D * D;
        double param = dot / len_sq;

        double xx, yy;

        if (param < 0) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }

        return haversine(px, py, xx, yy);
    }

    // ---------------- HAVERSINE DISTANCE ----------------

    private double haversine(double lat1, double lon1, double lat2, double lon2) {

        final int R = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2)
                        * Math.sin(lonDistance / 2);

        double c =
                2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}