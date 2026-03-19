package com.test.dto;

import java.util.Set;

public class SplitReservationResult {
    private final int assignedPassengers;
    private final int remainingPassengers;
    private final Set<Integer> createdAssignationIds;

    public SplitReservationResult(int assignedPassengers, int remainingPassengers,
            Set<Integer> createdAssignationIds) {
        this.assignedPassengers = assignedPassengers;
        this.remainingPassengers = remainingPassengers;
        this.createdAssignationIds = createdAssignationIds;
    }

    public int getAssignedPassengers() {
        return assignedPassengers;
    }

    public int getRemainingPassengers() {
        return remainingPassengers;
    }

    public Set<Integer> getCreatedAssignationIds() {
        return createdAssignationIds;
    }
}
