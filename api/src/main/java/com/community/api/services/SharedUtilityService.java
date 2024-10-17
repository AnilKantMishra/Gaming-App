package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.*;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.service.OrderService;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.domain.CustomerAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SharedUtilityService {
    private EntityManager entityManager;


    @Autowired
    HttpServletRequest request;
    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public OrderService orderService;

    public long findCount(String queryString) {
        TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
        return query.getSingleResult();
    }
    public static String getCurrentTimestamp() {
        // Get the current date and time with timezone
        ZonedDateTime zonedDateTime = ZonedDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSXXX");
        return zonedDateTime.format(formatter);
    }

    public enum ValidationResult {
        SUCCESS,
        EXCEEDS_MAX_SIZE,
        EXCEEDS_NESTED_SIZE,
        INVALID_TYPE
    }

    @Transactional
    public Map<String,Object> breakReferenceForCustomer(Customer customer)
    {
        Map<String,Object>customerDetails=new HashMap<>();
        customerDetails.put("id", customer.getId());
        customerDetails.put("dateCreated", customer.getAuditable().getDateCreated());
        customerDetails.put("createdBy", customer.getAuditable().getCreatedBy());
        customerDetails.put("dateUpdated", customer.getAuditable().getDateUpdated());
        customerDetails.put("updatedBy", customer.getAuditable().getUpdatedBy());
        customerDetails.put("username", customer.getUsername());
        customerDetails.put("password", customer.getPassword());
        customerDetails.put("emailAddress", customer.getEmailAddress());
        customerDetails.put("firstName", customer.getFirstName());
        customerDetails.put("lastName", customer.getLastName());
        customerDetails.put("fullName",customer.getFirstName()+" "+customer.getLastName());
        customerDetails.put("externalId", customer.getExternalId());
        customerDetails.put("challengeQuestion", customer.getChallengeQuestion());
        customerDetails.put("challengeAnswer", customer.getChallengeAnswer());
        customerDetails.put("passwordChangeRequired", customer.isPasswordChangeRequired());
        customerDetails.put("receiveEmail", customer.isReceiveEmail());
        customerDetails.put("registered", customer.isRegistered());
        customerDetails.put("deactivated", customer.isDeactivated());
        customerDetails.put("customerPayments", customer.getCustomerPayments());
        customerDetails.put("taxExemptionCode", customer.getTaxExemptionCode());
        customerDetails.put("unencodedPassword", customer.getUnencodedPassword());
        customerDetails.put("unencodedChallengeAnswer", customer.getUnencodedChallengeAnswer());
        customerDetails.put("anonymous", customer.isAnonymous());
        customerDetails.put("cookied", customer.isCookied());
        customerDetails.put("loggedIn", customer.isLoggedIn());
        customerDetails.put("transientProperties", customer.getTransientProperties());
        CustomCustomer customCustomer=entityManager.find(CustomCustomer.class,customer.getId());
        Order cart=orderService.findCartForCustomer(customer);
        if(cart!=null)
        customerDetails.put("orderId",cart.getId());
        else
            customerDetails.put("orderId",null);
        customerDetails.put("mobileNumber", customCustomer.getMobileNumber());
        customerDetails.put("secondaryMobileNumber", customCustomer.getSecondaryMobileNumber());
        customerDetails.put("whatsappNumber", customCustomer.getWhatsappNumber());

        customerDetails.put("countryCode", customCustomer.getCountryCode());
        customerDetails.put("otp", customCustomer.getOtp());
        customerDetails.put("fathersName", customCustomer.getFathersName());
        customerDetails.put("mothersName", customCustomer.getMothersName());
        customerDetails.put("panNumber",customCustomer.getPanNumber());
        customerDetails.put("nationality",customCustomer.getNationality());
        customerDetails.put("dob", customCustomer.getDob());
        customerDetails.put("gender", customCustomer.getGender());
        customerDetails.put("adharNumber", customCustomer.getAdharNumber());

        customerDetails.put("secondaryEmail", customCustomer.getSecondaryEmail());
        customerDetails.put("mothers_name", customCustomer.getMothersName());
        customerDetails.put("date_of_birth", customCustomer.getDob());


        customerDetails.put("secondary_mobile_number", customCustomer.getSecondaryMobileNumber());
        customerDetails.put("whatsapp_number", customCustomer.getWhatsappNumber());
        customerDetails.put("secondary_email", customCustomer.getSecondaryEmail());


        Map<String,String>currentAddress=new HashMap<>();
        Map<String,String>permanentAddress=new HashMap<>();
        for(CustomerAddress customerAddress:customer.getCustomerAddresses())
        {
            if(customerAddress.getAddressName().equals("CURRENT_ADDRESS"))
            {
                currentAddress.put("state", customerAddress.getAddress().getStateProvinceRegion());
                currentAddress.put("city", customerAddress.getAddress().getCity());
                currentAddress.put("district", customerAddress.getAddress().getCounty());
                currentAddress.put("pincode", customerAddress.getAddress().getPostalCode());
                currentAddress.put("Address line",customerAddress.getAddress().getAddressLine1());
            }
            if(customerAddress.getAddressName().equals("PERMANENT_ADDRESS"))
            {
                permanentAddress.put("state", customerAddress.getAddress().getStateProvinceRegion());
                permanentAddress.put("city", customerAddress.getAddress().getCity());
                permanentAddress.put("district", customerAddress.getAddress().getCounty());
                permanentAddress.put("pincode", customerAddress.getAddress().getPostalCode());
                permanentAddress.put("Address line",customerAddress.getAddress().getAddressLine1());
            }

        }
        customerDetails.put("currentAddress",currentAddress);
        customerDetails.put("permanentAddress",permanentAddress);



      /*  customerDetails.put("qualificationDetails",customCustomer.getQualificationDetailsList());
        customerDetails.put("documentList",customCustomer.getDocumentList());
        List<Map<String,Object>>listOfSavedProducts=new ArrayList<>();*/
    /*    if(!customCustomer.getSavedForms().isEmpty()) {
            for (Product product : customCustomer.getSavedForms()) {
                listOfSavedProducts.add(createProductResponseMap(product, null,customCustomer));
            }
        }

        customerDetails.put("savedForms",listOfSavedProducts);*/

        List<Map<String, Object>> filteredDocuments = new ArrayList<>();



        if (!filteredDocuments.isEmpty()) {
            customerDetails.put("documents", filteredDocuments);
        }

        return customerDetails;
    }

    public ValidationResult validateInputMap(Map<String,Object>inputMap)
    {
            if(inputMap.keySet().size()>Constant.MAX_REQUEST_SIZE)
                return ValidationResult.EXCEEDS_MAX_SIZE;

            // Iterate through the map entries to check for nested maps
            for (Map.Entry<String, Object> entry : inputMap.entrySet()) {
                Object value = entry.getValue();

                // Check if the value is a nested map
                if (value instanceof Map) {
                    Map<?, ?> nestedMap = (Map<?, ?>) value;

                    // Check the size of the nested map's key set
                    if (nestedMap.keySet().size() > Constant.MAX_NESTED_KEY_SIZE) {
                        return ValidationResult.EXCEEDS_NESTED_SIZE;
                    }
                }
            }
            return ValidationResult.SUCCESS;

        }

    public Map<String,Object> trimStringValues(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof String) {
                // Trim the string and update the map
                String trimmedValue = ((String) entry.getValue()).trim();
                entry.setValue(trimmedValue);
            }
        }
        return map;
    }

    public  boolean isValidEmail(String email) {
        return email != null && email.matches(Constant.EMAIL_REGEXP);
    }

}

