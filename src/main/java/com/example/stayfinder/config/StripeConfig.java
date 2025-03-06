package com.example.stayfinder.config;

import com.stripe.Stripe;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
public class StripeConfig {
    @Value("${stripe.secretKey}")
    private String stripeSecretKey;
    @Value("${stripe.success.url}")
    private String successUrl;
    @Value("${stripe.cancel.url}")
    private String cancelUrl;
    @Value("${stripe.session.placeholder}")
    private String sessionPlaceholder;
    @Value("${stripe.session.request.param}")
    private String sessionRequestParam;
    @Value("${stripe.default.quantity}")
    private long defaultQuantity;
    @Value("${stripe.default.currency}")
    private String defaultCurrency;
    @Value("${stripe.cents.amount}")
    private BigDecimal centsAmount;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public SessionCreateParams createSessionParams(BigDecimal totalAmount) {
        return SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCancelUrl(buildUrlWithSessionId(cancelUrl))
                .setSuccessUrl(buildUrlWithSessionId(successUrl))
                .addLineItem(createLineItem(totalAmount))
                .build();
    }

    private SessionCreateParams.LineItem.PriceData createPriceData(BigDecimal totalAmount) {
        return SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency(defaultCurrency)
                .setUnitAmount(totalAmount.multiply(centsAmount).longValue())
                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName("Booking Payment")
                        .build())
                .build();
    }

    private SessionCreateParams.LineItem createLineItem(BigDecimal totalAmount) {
        return SessionCreateParams.LineItem.builder()
                .setPriceData(createPriceData(totalAmount))
                .setQuantity(defaultQuantity)
                .build();
    }

    private String buildUrlWithSessionId(String baseUrl) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam(sessionRequestParam, sessionPlaceholder)
                .build(false)
                .toUriString();
    }
}
