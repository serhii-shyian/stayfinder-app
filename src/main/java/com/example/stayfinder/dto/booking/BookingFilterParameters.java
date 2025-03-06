package com.example.stayfinder.dto.booking;

public record BookingFilterParameters(
        String[] userIdArray,
        String[] statusArray) {
    public BookingFilterParameters {
        userIdArray = userIdArray != null ? userIdArray : new String[0];
        statusArray = statusArray != null ? statusArray : new String[0];
    }
}
