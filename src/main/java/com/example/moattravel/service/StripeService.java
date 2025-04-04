package com.example.moattravel.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.moattravel.form.ReservationRegisterForm;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class StripeService {
    @Value("${stripe.api-key}")
    private String stripeApiKey;
    
    private final ReservationService reservationService;
    
    public StripeService(ReservationService reservationService) {
        this.reservationService = reservationService;
    }    
    
    // セッションを作成し、Stripeに必要な情報を返す
    public String createStripeSession(String houseName, ReservationRegisterForm reservationRegisterForm, HttpServletRequest httpServletRequest) {
        Stripe.apiKey = stripeApiKey;
        String requestUrl = new String(httpServletRequest.getRequestURL());
        SessionCreateParams params =
            SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addLineItem(SessionCreateParams.LineItem.builder()
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()   
                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName(houseName)
                .build())
                .setUnitAmount((long)reservationRegisterForm.getAmount())
                .setCurrency("jpy")                                
                .build())
                .setQuantity(1L)
                .build())
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(requestUrl.replaceAll("/houses/[0-9]+/reservations/confirm", "") + "/reservations?reserved")
                .setCancelUrl(requestUrl.replace("/reservations/confirm", ""))
                .setPaymentIntentData(SessionCreateParams.PaymentIntentData.builder()
                        .putMetadata("houseId", reservationRegisterForm.getHouseId().toString())
                        .putMetadata("userId", reservationRegisterForm.getUserId().toString())
                        .putMetadata("checkinDate", reservationRegisterForm.getCheckinDate())
                        .putMetadata("checkoutDate", reservationRegisterForm.getCheckoutDate())
                        .putMetadata("numberOfPeople", reservationRegisterForm.getNumberOfPeople().toString())
                        .putMetadata("amount", reservationRegisterForm.getAmount().toString())
                        .build())
                .build();
        try {
            Session session = Session.create(params);
            return session.getId();
        } catch (StripeException e) {
            e.printStackTrace();
            return "";
        }
    } 
    
    
	@SuppressWarnings("deprecation")
	public void processSessionCompleted(Event event) {
        System.out.println("processSessionCompleted!");
        if (event.getData() == null || event.getData().getObject() == null) {
            System.out.println("❌ event.getData() または event.getData().getObject() が null です");
            System.out.println("🔍 event.getData() の内容: " + event.getData());
            return;
        }

        Session session = (Session) event.getData().getObject();  // 直接 Session を取得

        System.out.println("✅ Session ID: " + session.getId());

        SessionRetrieveParams params = SessionRetrieveParams.builder().addExpand("payment_intent").build();
        try {
            session = Session.retrieve(session.getId(), params, null);
            Map<String, String> metadata = session.getPaymentIntentObject().getMetadata();
            reservationService.create(metadata);
            System.out.println("✅ 予約データをDBに保存しました");
        } catch (StripeException e) {
            System.out.println("❌ Webhook処理中に例外発生: " + e.getMessage());
            e.printStackTrace();
        }
    }

 
}
