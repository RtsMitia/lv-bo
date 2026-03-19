package com.test.dto;

import java.util.List;
import java.util.Set;

import com.test.model.Reservation;

public class DepartureAssignmentResult {
    private final List<Reservation> deferredReservations;
    private final Set<Integer> createdAssignationIds;
    private final boolean assigned;

    public DepartureAssignmentResult(List<Reservation> deferredReservations,
            Set<Integer> createdAssignationIds,
            boolean assigned) {
        this.deferredReservations = deferredReservations;
        this.createdAssignationIds = createdAssignationIds;
        this.assigned = assigned;
    }

    public List<Reservation> getDeferredReservations() {
        return deferredReservations;
    }

    public Set<Integer> getCreatedAssignationIds() {
        return createdAssignationIds;
    }

    public boolean hasAssigned() {
        return assigned;
    }
}
