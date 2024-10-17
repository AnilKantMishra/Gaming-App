package com.community.api.endpoint.avisoft.controller.Customer;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.UserDetailDTO;
import com.community.api.endpoint.avisoft.controller.otpmodule.OtpEndpoint;
import com.community.api.endpoint.customer.AddressDTO;
import com.community.api.entity.CustomCustomer;
import com.community.api.services.*;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.services.exception.ExceptionHandlingService;

import io.micrometer.core.lang.Nullable;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.profile.core.domain.Address;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.domain.CustomerAddress;
import org.broadleafcommerce.profile.core.domain.CustomerImpl;
import org.broadleafcommerce.profile.core.service.AddressService;
import org.broadleafcommerce.profile.core.service.CustomerAddressService;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.constraints.Size;
import java.io.File;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

import static com.community.api.services.ResponseService.generateErrorResponse;

@RestController
@RequestMapping(value = "/customer",
        produces = {
                MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_XML_VALUE
        }
)

public class CustomerEndpoint {

    private PasswordEncoder passwordEncoder;
    private CustomerService customerService;  //@TODO- do this task asap
    private ExceptionHandlingImplement exceptionHandling;
    private EntityManager em;
    private CustomCustomerService customCustomerService;
    private AddressService addressService;
    private CustomerAddressService customerAddressService;
    private JwtUtil jwtUtil;


    @Autowired
    private static SharedUtilityService sharedUtilityServiceApi;

    @Autowired
    private ExceptionHandlingService exceptionHandlingService;

    @Autowired
    private JwtUtil jwtTokenUtil;


    @Autowired
    private static ResponseService responseService;

    @Autowired
    private  SanitizerService sanitizerService;

    @Autowired
    private CatalogService catalogService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Autowired
    public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }

    @Autowired
    public void setEm(EntityManager em) {
        this.em = em;
    }

    @Autowired
    public void setCustomCustomerService(CustomCustomerService customCustomerService) {
        this.customCustomerService = customCustomerService;
    }

    @Autowired
    private SharedUtilityService sharedUtilityService;

    @Autowired
    public void setAddressService(AddressService addressService) {
        this.addressService = addressService;
    }

    @Autowired
    public void setCustomerAddressService(CustomerAddressService customerAddressService) {
        this.customerAddressService = customerAddressService;
        this.jwtUtil = jwtUtil;
    }

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }



    @Transactional
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomer(@RequestBody Map<String, Object> details, @RequestParam Long customerId) {
        try {
            List<String> errorMessages = new ArrayList<>();

            details=sanitizerService.sanitizeInputMap(details);

            if (!errorMessages.isEmpty()) {
                return generateErrorResponse("List of Failed validations: " + errorMessages.toString(), HttpStatus.BAD_REQUEST);
            }
            if (customerService == null) {
                return generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            CustomCustomer customCustomer = em.find(CustomCustomer.class, customerId);
            if (customCustomer == null) {
                return generateErrorResponse("No data found for this customerId", HttpStatus.NOT_FOUND);
            }

            // Validating mobile number
            String mobileNumber = (String) details.get("mobileNumber");
            if (mobileNumber != null && !customCustomerService.isValidMobileNumber(mobileNumber)) {
                return generateErrorResponse("Cannot update phoneNumber", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            String username = (String) details.get("username");
            String emailAddress = (String) details.get("emailAddress");
            Customer existingCustomerByUsername = (username != null) ? customerService.readCustomerByUsername(username) : null;
            Customer existingCustomerByEmail = (emailAddress != null) ? customerService.readCustomerByEmail(emailAddress) : null;

            if ((existingCustomerByUsername != null && !existingCustomerByUsername.getId().equals(customerId)) ||
                    (existingCustomerByEmail != null && !existingCustomerByEmail.getId().equals(customerId))) {
                return generateErrorResponse("Email or Username already in use", HttpStatus.BAD_REQUEST);
            }

            customCustomer.setId(customerId);
            customCustomer.setMobileNumber(customCustomer.getMobileNumber());
            customCustomer.setCountryCode(customCustomer.getCountryCode());


            if (details.containsKey("firstName")&&!details.get("firstName").toString().isEmpty()) {
                customCustomer.setFirstName((String) details.get("firstName"));
            } else if (details.containsKey("firstName")&&details.get("firstName").toString().isEmpty())
            {
                errorMessages.add("First name cannot be null");
            }
            if (details.containsKey("lastName")&&!details.get("lastName").toString().isEmpty())
                customCustomer.setLastName((String) details.get("lastName"));
            else if (details.containsKey("lastName")&&details.get("lastName").toString().isEmpty())
            {
                errorMessages.add("Last name cannot be null");
            }



            if (details.containsKey("emailAddress") && ((String) details.get("emailAddress")).isEmpty())
                errorMessages.add("email Address cannot be null");
            if (details.containsKey("emailAddress") && !((String) details.get("emailAddress")).isEmpty())
                customCustomer.setEmailAddress(emailAddress);
            details.remove("firstName");
            details.remove("lastName");
            details.remove("emailAddress");
            String state = (String) details.get("currentState");
            String district = (String) details.get("currentDistrict");
            String pincode = (String) details.get("currentPincode");
            if (state != null && district != null && pincode != null) {
                boolean updated=false;
                for (CustomerAddress customerAddress : customCustomer.getCustomerAddresses()) {
                    if (customerAddress.getAddressName().equals("CURRENT_ADDRESS")) {
                        customerAddress.getAddress().setAddressLine1((String) details.get("currentAddress"));
                        customerAddress.getAddress().setPostalCode(pincode);
                        customerAddress.getAddress().setCity((String) details.get("currentCity"));
                        updated = true;
                        break;
                    }
                }
                if(!updated) {
                    Map<String, Object> addressMap = new HashMap<>();
                    addressMap.put("address", details.get("currentAddress"));
                    addressMap.put("city", details.get("currentCity"));
                    addressMap.put("pinCode", pincode);
                    addressMap.put("addressName", "CURRENT_ADDRESS");

                }
            }
            details.remove("currentState");
            details.remove("currentDistrict");
            details.remove("currentAddress");
            details.remove("currentPincode");
            details.remove("currentCity");
            state = (String) details.get("permanentState");
            district = (String) details.get("permanentDistrict");
            pincode = (String) details.get("permanentPincode");
            if (state != null && district != null && pincode != null) {
                boolean updated = false;
                for (CustomerAddress customerAddress : customCustomer.getCustomerAddresses()) {

                    if (customerAddress.getAddressName().equals("PERMANENT_ADDRESS")) {
                        System.out.println("1");
                        customerAddress.getAddress().setAddressLine1((String) details.get("permanentAddress"));

                        customerAddress.getAddress().setPostalCode(pincode);
                        customerAddress.getAddress().setCity((String) details.get("permanentCity"));
                        updated = true;
                        break;
                    }
                }
                if (!updated) {
                    Map<String, Object> addressMap = new HashMap<>();
                    addressMap.put("address", details.get("permanentAddress"));
                    addressMap.put("city", details.get("permanentCity"));
                    addressMap.put("pinCode", pincode);
                    addressMap.put("addressName", "PERMANENT_ADDRESS");
                }
            }
            details.remove("permanentState");
            details.remove("permanentDistrict");
            details.remove("permanentAddress");
            details.remove("permanentPincode");
            details.remove("permanentCity");

            for (Map.Entry<String, Object> entry : details.entrySet()) {
                String fieldName = entry.getKey();
                Object newValue = entry.getValue();
                Field field = CustomCustomer.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                Column columnAnnotation = field.getAnnotation(Column.class);
                boolean isColumnNotNull = (columnAnnotation != null && !columnAnnotation.nullable());
                boolean isNullable = field.isAnnotationPresent(Nullable.class);
                field.setAccessible(true);
                if (newValue.toString().isEmpty() && !isNullable) {
                    errorMessages.add(fieldName + " cannot be null");
                    continue;
                }

                if (field.isAnnotationPresent(Size.class)) {
                    Size sizeAnnotation = field.getAnnotation(Size.class);
                    int min = sizeAnnotation.min();
                    int max = sizeAnnotation.max();
                    if (newValue.toString().length() > max || newValue.toString().length() < min) {
                        errorMessages.add(fieldName + " size should be between " + min + " and " + max);
                        continue;
                    }
                }

                if (newValue != null && field.getType().isAssignableFrom(newValue.getClass())) {
                    field.set(customCustomer, newValue);
                }
            }


            if (!errorMessages.isEmpty()) {
                return generateErrorResponse("List of Failed validations: " + errorMessages.toString(), HttpStatus.BAD_REQUEST);
            }

            em.merge(customCustomer);
            return ResponseService.generateSuccessResponse("User details updated successfully", sharedUtilityService.breakReferenceForCustomer(customCustomer), HttpStatus.OK);

        }catch(NoSuchFieldException e)
        {
            return generateErrorResponse("No such field present :" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch(Exception e){
            exceptionHandling.handleException(e);
            return generateErrorResponse("Error updating " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

/*    @Transactional
    @RequestMapping(value = "/get-customer-details/{customerId}", method = RequestMethod.GET)
    public ResponseEntity<?> getUserDetails(@PathVariable Long customerId) {
        try {
            CustomCustomer customCustomer = em.find(CustomCustomer.class, customerId);
            if (customCustomer == null) {
                return generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
            }
            CustomerImpl customer = em.find(CustomerImpl.class, customerId);  // Assuming you retrieve the base Customer entity
            Map<String, Object> customerDetails = sharedUtilityService.breakReferenceForCustomer(customer);

            return responseService.generateSuccessResponse("User details retrieved successfully", customerDetails, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return generateErrorResponse("Error retrieving user details", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/

    @Transactional
    @GetMapping("/get-customer-details/{customerId}")
    public ResponseEntity<?> getUserDetails(@PathVariable Long customerId) {
        try {
            UserDetailDTO userDetail = customCustomerService.getUserDetails(customerId);
            return ResponseEntity.ok(userDetail);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return generateErrorResponse("Error fetching user details", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public boolean isFieldPresent (Class < ? > clazz, String fieldName){
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return field != null;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
    @Transactional
    @RequestMapping(value = "update-username", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomerUsername(@RequestBody Map<String, Object> updates, @RequestParam Long customerId) {
        try {

            updates=sanitizerService.sanitizeInputMap(updates);

            if (customerService == null) {
                return generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);

            }
            String username = (String) updates.get("username");
            if(username!=null)
                username=username.trim();

            if (username.isEmpty()||username.contains(" ")) {
                return generateErrorResponse("Invalid username", HttpStatus.NOT_FOUND);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return generateErrorResponse("No data found for this customerId", HttpStatus.NOT_FOUND);

            }
            Customer existingCustomerByUsername = null;
            existingCustomerByUsername = customerService.readCustomerByUsername(username);

            if ((existingCustomerByUsername != null) && !existingCustomerByUsername.getId().equals(customerId)) {
                return generateErrorResponse("Username is not available", HttpStatus.BAD_REQUEST);

            } else {
                if(customer.getUsername()!=null && customer.getUsername().equals(username))
                    return generateErrorResponse("Old and new username cannot be same", HttpStatus.BAD_REQUEST);
                customer.setUsername(username);
                em.merge(customer);
                return ResponseService.generateSuccessResponse("User name  updated successfully : ", sharedUtilityService.breakReferenceForCustomer(customer), HttpStatus.OK);

            }
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return generateErrorResponse("Error updating username", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @Transactional
    @RequestMapping(value = "create-or-update-password", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomerPassword(@RequestBody Map<String, Object> details, @RequestParam Long customerId) {
        try {
            if (customerService == null) {
                return generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);

            }

            String password = (String) details.get("password");
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return generateErrorResponse("No data found for this customerId", HttpStatus.NOT_FOUND);
            }
            if (password != null) {
                if (customer.getPassword() == null || customer.getPassword().isEmpty()) {
                    customer.setPassword(passwordEncoder.encode(password));
                    em.merge(customer);
                    return ResponseService.generateSuccessResponse("Password Created", sharedUtilityService.breakReferenceForCustomer(customer), HttpStatus.OK);
                }
                if (!passwordEncoder.matches(password, customer.getPassword())) {

                    customer.setPassword(passwordEncoder.encode(password));
                    em.merge(customer);
                    return ResponseService.generateSuccessResponse("Password Updated", sharedUtilityService.breakReferenceForCustomer(customer), HttpStatus.OK);

                }else{
                    return generateErrorResponse("Old Password and new Password cannot be same", HttpStatus.BAD_REQUEST);
                }

            } else {
                return generateErrorResponse("Empty Password", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return generateErrorResponse("Error updating password", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "delete", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteCustomer(@RequestParam String customerId) {
        try {
            Long id = Long.valueOf(customerId);
            if (customerService == null) {
                return generateErrorResponse(ApiConstants.CUSTOMER_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Customer customer = customerService.readCustomerById(id);
            if (customer != null) {
                customerService.deleteCustomer(customer);
                return ResponseService.generateSuccessResponse("Record Deleted Successfully", "", HttpStatus.OK);
            } else {
                return generateErrorResponse("No Records found for this ID " + id, HttpStatus.NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            return generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return generateErrorResponse("Some issue in deleting customer: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    public AddressDTO makeAddressDTO(CustomerAddress customerAddress) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressId(customerAddress.getAddress().getId());
        addressDTO.setAddress(customerAddress.getAddress().getAddressLine1());
        addressDTO.setPinCode(customerAddress.getAddress().getPostalCode());
        addressDTO.setState(customerAddress.getAddress().getStateProvinceRegion());
        addressDTO.setCity(customerAddress.getAddress().getCity());
        addressDTO.setCustomerId(customerAddress.getCustomer().getId());
        addressDTO.setAddressName(customerAddress.getAddressName());
        CustomCustomer customCustomer = em.find(CustomCustomer.class, customerAddress.getCustomer().getId());
        addressDTO.setPhoneNumber(customCustomer.getMobileNumber());
        return addressDTO;
    }

    public ResponseEntity<?> createAuthResponse(String token, Customer customer) {
        OtpEndpoint.ApiResponse authResponse = new OtpEndpoint.ApiResponse(token, sharedUtilityService.breakReferenceForCustomer(customer), HttpStatus.OK.value(), HttpStatus.OK.name(), "User has been logged in");
        return ResponseService.generateSuccessResponse("Token details : ", authResponse, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body("Token is required");
        }
        try {
            jwtUtil.logoutUser(token);

            return ResponseEntity.ok("Logged out successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during logout");
        }
    }

    @GetMapping("/get-all-customers")
    public ResponseEntity<?> getAllCustomers(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        try {

            int startPosition = offset * limit;
            TypedQuery<CustomCustomer> query = entityManager.createQuery(Constant.GET_ALL_CUSTOMERS, CustomCustomer.class);
            query.setFirstResult(startPosition);
            query.setMaxResults(limit);
            List<Map> results = new ArrayList<>();
            for (CustomCustomer customer : query.getResultList()) {
                Customer customerToadd = customerService.readCustomerById(customer.getId());
                results.add(sharedUtilityService.breakReferenceForCustomer(customerToadd));
            }
            return ResponseService.generateSuccessResponse("List of customers : ", results, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }  catch (Exception e) {
            exceptionHandling.handleException(e);
            return generateErrorResponse("Some issue in customers: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


}