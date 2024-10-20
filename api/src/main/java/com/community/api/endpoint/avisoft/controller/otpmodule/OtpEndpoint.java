package com.community.api.endpoint.avisoft.controller.otpmodule;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;

import com.community.api.entity.CustomCustomer;
import com.community.api.services.*;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import io.github.bucket4j.Bucket;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/otp")
public class OtpEndpoint {

    private static final Logger log = LoggerFactory.getLogger(OtpEndpoint.class);

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private SharedUtilityService sharedUtilityService;

    @Autowired
    private TwilioService twilioService;

    @Autowired
    private CustomCustomerService customCustomerService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private EntityManager em;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SanitizerService sanitizerService;

    @Autowired
    private ResponseService responseService;

    @Value("${twilio.authToken}")
    private String authToken;

    @Value("${twilio.accountSid}")
    private String accountSid;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody CustomCustomer customerDetails, HttpSession session) throws UnsupportedEncodingException {
        try {
            if (customerDetails.getMobileNumber() == null || customerDetails.getMobileNumber().isEmpty()) {
                return responseService.generateErrorResponse(ApiConstants.MOBILE_NUMBER_NULL_OR_EMPTY, HttpStatus.NOT_ACCEPTABLE);
            }

            String mobileNumber = customerDetails.getMobileNumber().startsWith("0")
                    ? customerDetails.getMobileNumber().substring(1)
                    : customerDetails.getMobileNumber();

            String countryCode = customerDetails.getCountryCode() == null || customerDetails.getCountryCode().isEmpty()
                    ? Constant.COUNTRY_CODE
                    : customerDetails.getCountryCode();

            CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhoneWithOtp(customerDetails.getMobileNumber(), countryCode);
            if (existingCustomer != null) {
                return responseService.generateErrorResponse(ApiConstants.CUSTOMER_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
            }

            Bucket bucket = rateLimiterService.resolveBucket(customerDetails.getMobileNumber(), "/otp/send-otp");
            if (bucket.tryConsume(1)) {
                if (!customCustomerService.isValidMobileNumber(mobileNumber)) {
                    return responseService.generateErrorResponse(ApiConstants.INVALID_MOBILE_NUMBER, HttpStatus.BAD_REQUEST);

                }

                ResponseEntity<Map<String, Object>> otpResponse = twilioService.sendOtpToMobile(mobileNumber, countryCode);
                Map<String, Object> responseBody = otpResponse.getBody();

                if (responseBody.get("otp")!=null) {
                    return responseService.generateSuccessResponse((String) responseBody.get("message"), responseBody.get("otp"), HttpStatus.OK);
                } else {
                    return responseService.generateErrorResponse((String) responseBody.get("message"), HttpStatus.BAD_REQUEST);
                }
            } else {
                return responseService.generateErrorResponse(ApiConstants.RATE_LIMIT_EXCEEDED, HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error occurred" + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody Map<String, Object> loginDetails, HttpSession session, HttpServletRequest request) {
        try {
            loginDetails=sanitizerService.sanitizeInputMap(loginDetails);

            if (loginDetails == null) {
                return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

            String otpEntered = (String) loginDetails.get("otpEntered");
            Integer role = 4;
            String countryCode = (String) loginDetails.get("countryCode");
            String username = (String) loginDetails.get("username");
            String mobileNumber = (String) loginDetails.get("mobileNumber");



            if (username != null) {
                if (customerService == null) {
                    return responseService.generateErrorResponse(ApiConstants.CUSTOMER_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                Customer customer = customerService.readCustomerByUsername(username);

                if (customer == null) {
                    return responseService.generateErrorResponse(ApiConstants.NO_RECORDS_FOUND, HttpStatus.INTERNAL_SERVER_ERROR);
                }

                CustomCustomer customCustomer = em.find(CustomCustomer.class, customer.getId());
                if (customCustomer != null) {
                    mobileNumber = customCustomer.getMobileNumber();
                } else {
                    return responseService.generateErrorResponse(ApiConstants.NO_RECORDS_FOUND, HttpStatus.NOT_FOUND);
                }
            } else if (mobileNumber == null) {
                return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (otpEntered == null || otpEntered.trim().isEmpty()) {
                return responseService.generateErrorResponse("otp is null ", HttpStatus.BAD_REQUEST);
            }

            CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhone(mobileNumber, countryCode);

            if (existingCustomer == null) {
                return responseService.generateErrorResponse(ApiConstants.NO_RECORDS_FOUND, HttpStatus.NOT_FOUND);
            }

            String storedOtp = existingCustomer.getOtp();
            String ipAddress = customCustomerService.getClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            String tokenKey = "authToken_" + mobileNumber;
            Customer customer = customerService.readCustomerById(existingCustomer.getId());

            if (otpEntered.equals(storedOtp)) {
                existingCustomer.setOtp(null);
                em.persist(existingCustomer);


                String existingToken = existingCustomer.getToken();

                if (existingToken!= null && jwtUtil.validateToken(existingToken, ipAddress, userAgent)) {


                    ApiResponse response = new ApiResponse(existingToken,sharedUtilityService.breakReferenceForCustomer(customer), HttpStatus.OK.value(), HttpStatus.OK.name(),"User has been logged in");
                    return ResponseEntity.ok(response);

                } else {
                    String newToken = jwtUtil.generateToken(existingCustomer.getId(), role, ipAddress, userAgent);
                    session.setAttribute(tokenKey, newToken);
                    existingCustomer.setToken(newToken);
                    em.persist(existingCustomer);

                    ApiResponse response = new ApiResponse(newToken,sharedUtilityService.breakReferenceForCustomer(customer), HttpStatus.OK.value(), HttpStatus.OK.name(),"User has been logged in");
                    return ResponseEntity.ok(response);

                }
            } else {
                return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Otp verification error" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public static class ApiResponse {
        private Data data;
        private int status_code;
        private String status;
        private String message;
        private String token;


        public ApiResponse(String token, Map<String,Object>customerDetails, int statusCodeValue, String statusCode, String message) {
            this.data = new Data(customerDetails);
            this.status_code = statusCodeValue;
            this.status = statusCode;
            this.message = message;
            this.token = token;
        }

        public Data getData() {
            return data;
        }

        public int getStatus_code() {
            return status_code;
        }

        public String getStatus() {
            return status;
        }

        public String getToken() {
            return token;
        }

        public String getMessage() {
            return message;
        }

        public  class Data {
            private Map<String,Object> userDetails;

            public Data(Map<String,Object>customerDetails) {
                this.userDetails = customerDetails;
            }

            public Map<String,Object> getUserDetails() {
                return userDetails;
            }
        }
    }


}
