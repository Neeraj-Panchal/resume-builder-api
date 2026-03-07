package com.neeraj.resumebuilderapi.service;

import com.neeraj.resumebuilderapi.document.Payment;
import com.neeraj.resumebuilderapi.document.User;
import com.neeraj.resumebuilderapi.dto.AuthResponse;
import com.neeraj.resumebuilderapi.repository.PaymentRepository;
import com.neeraj.resumebuilderapi.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.neeraj.resumebuilderapi.util.AppConstants.PREMIUM;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final AuthService authService;

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public Payment createOrder(Object principal, String planType) throws RazorpayException {

        //Initial step
        AuthResponse authResponse = authService.getProfile(principal);

        //step 1 : Initialize the razorpay client
        RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        //step 2 : prepare the json object to pass the rezorpay
        int amount = 500;      //AMOUNT IN PAISE
        String currency = "INR";
        String receipt = PREMIUM+"_"+ UUID.randomUUID().toString().substring(0, 8);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount);
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", receipt);

        //step 3 : call the razorpay API to create order
        Order razorpayOrder = razorpayClient.orders.create(orderRequest);

        //step 4 : save the order details into the database
        Payment newPayment = Payment.builder()
                .userId(authResponse.getId())
                .razorpayOrderId(razorpayOrder.get("id"))
                .amount(amount)
                .currency(currency)
                .planType(planType)
                .status("created")
                .receipt(receipt)
                .build();

        //step 5 : return the result
        return paymentRepository.save(newPayment);
    }

    public boolean verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) throws RazorpayException {
        try{
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", razorpayOrderId);
            attributes.put("razorpay_payment_id", razorpayPaymentId);
            attributes.put("razorpay_signature", razorpaySignature);

            boolean isValidSignature = Utils.verifyPaymentSignature(attributes, razorpayKeySecret);

            if(isValidSignature){
                //Update the payment status
                Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                        .orElseThrow(()-> new RuntimeException("Payment not found"));
                payment.setRazorpayPaymentId(razorpayPaymentId);
                payment.setRazorpaySignature(razorpaySignature);
                payment.setStatus("paid");
                paymentRepository.save(payment);

                //Upgrade the user subscription
                upgradeUserSubscription(payment.getUserId(), payment.getPlanType());
                return true;
            }
            return false;
        }catch (Exception e){
            log.error("Error verifying the payment: ",e);
            return false;
        }
    }

    private void upgradeUserSubscription(String userId, String planType) {
        User existingUser = userRepository.findById(userId)
                        .orElseThrow(()-> new UsernameNotFoundException("User not found"));
        existingUser.setSubscriptionPlan(planType);
        userRepository.save(existingUser);
        log.info("User {} upgraded to {} plan",userId,planType);
    }

    public List<Payment> getUserPayments(Object principal) {
        //step 1 : get the current profile
        AuthResponse authResponse = authService.getProfile(principal);

        //step 2 : Call the repository finder method
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(authResponse.getId());
    }

    public Payment getPaymentDetails(String orderId) {
        //step 1 : call the repository finder method
        return paymentRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(()-> new RuntimeException("Payment not found"));
    }
}
