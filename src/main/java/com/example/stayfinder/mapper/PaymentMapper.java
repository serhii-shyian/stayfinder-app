package com.example.stayfinder.mapper;

import com.example.stayfinder.config.MapperConfig;
import com.example.stayfinder.dto.payment.PaymentDto;
import com.example.stayfinder.dto.payment.PaymentLowInfoDto;
import com.example.stayfinder.dto.payment.PaymentWithoutSessionDto;
import com.example.stayfinder.model.Payment;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {
    List<PaymentLowInfoDto> toDtoList(List<Payment> payments);

    @Mapping(source = "booking.id", target = "bookingId")
    PaymentDto toDto(Payment payment);

    @Mapping(source = "booking.id", target = "bookingId")
    PaymentWithoutSessionDto toWithoutSessionDto(Payment payment);
}
