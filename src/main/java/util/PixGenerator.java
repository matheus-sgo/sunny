package util;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.resources.payment.Payment;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class PixGenerator {

    private static String accessToken() {
        String token = System.getenv("MP_ACCESS_TOKEN");
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Defina a variável de ambiente MP_ACCESS_TOKEN com o access token do Mercado Pago.");
        }
        return token;
    }

    public static Map<String, String> gerarPix(BigDecimal valor, String email) throws Exception {
        MercadoPagoConfig.setAccessToken(accessToken());

        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put("x-idempotency-key", String.valueOf(System.currentTimeMillis()));

        MPRequestOptions requestOptions = MPRequestOptions.builder()
                .customHeaders(customHeaders)
                .build();

        PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.builder()
                .transactionAmount(valor)
                .paymentMethodId("pix")
                .payer(PaymentPayerRequest.builder()
                        .email(email)
                        .build())
                .build();

        PaymentClient client = new PaymentClient();
        Payment payment = client.create(paymentCreateRequest, requestOptions);

        String copiaCola = payment.getPointOfInteraction().getTransactionData().getQrCode();
        String base64 = payment.getPointOfInteraction().getTransactionData().getQrCodeBase64();
        String paymentId = payment.getId().toString();
        String status = payment.getStatus();

        return Map.of(
                "copia_cola", copiaCola,
                "imagem_base64", base64,
                "payment_id", paymentId,
                "status", status
        );
    }
    
    public static String consultarStatusPagamento(String paymentId) throws Exception {
        MercadoPagoConfig.setAccessToken(accessToken());

        PaymentClient client = new PaymentClient();
        Payment payment = client.get(Long.valueOf(paymentId));

        return payment.getStatus();
    }
    
}