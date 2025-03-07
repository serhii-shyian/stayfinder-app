package com.example.stayfinder.service.notification;

public class NotificationTemplates {
    public static final String BOOKING_CREATED_TEMPLATE =
            "Type notification: #BOOKING_CREATED%n"
                    + "Booking detail: %n"
                    + "           Booking id: %s%n"
                    + "           Check in date: %s%n"
                    + "           Check out date: %s%n"
                    + "           Accommodation Id: %s%n"
                    + "Reservation owner: %n"
                    + "           id:  %s%n"
                    + "           name: %s";

    public static final String BOOKING_CANCELED_TEMPLATE =
            "Type notification: #BOOKING_CANCELED%n"
                    + "Booking id: %s%n"
                    + "Reservation owner: %n"
                    + "           id:  %s%n"
                    + "           name: %s";

    public static final String ACCOMMODATION_CREATED_MESSAGE =
            "Type notification: #ACCOMMODATION_CREATED%n"
                    + "Accommodation detail: %n"
                    + "           id:  %s%n"
                    + "           type: %s%n"
                    + "           size: %s%n"
                    + "           daily rate: %s";

    public static final String ACCOMMODATION_RELEASE_MESSAGE =
            "#hourly_check%n"
                    + "    Accommodations with id: %s are released";

    public static final String PAYMENT_SUCCESS_MESSAGE =
            "Type notification: #PAYMENT_CREATED%n"
                    + "The payment was successful!%n"
                    + "Payment detail: %n"
                    + "           id:  %s%n"
                    + "           bookingId:  %s%n"
                    + "           status: %s%n"
                    + "           amount: %s";
}
