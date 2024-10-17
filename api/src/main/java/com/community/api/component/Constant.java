package com.community.api.component;

public class Constant {

    public static String COUNTRY_CODE = "+91";
    public static String PHONE_QUERY = "SELECT c FROM CustomCustomer c WHERE c.mobileNumber = :mobileNumber AND c.countryCode = :countryCode";
    public static String PHONE_QUERY_OTP = "SELECT c FROM CustomCustomer c WHERE c.mobileNumber = :mobileNumber AND c.countryCode = :countryCode AND c.otp=:otp";

    public static String FETCH_ROLE = "SELECT r.role_name FROM Role r WHERE r.role_id = :role_id";
    public static String roleUser = "CUSTOMER";
    public static final int MAX_REQUEST_SIZE=100;
    public static  final int MAX_NESTED_KEY_SIZE=100;
    public static final String GET_ALL_CUSTOMERS="Select c from CustomCustomer c";

    public static final String EMAIL_REGEXP="^[\\w-\\.]+@[\\w-]+\\.[a-zA-Z]{2,}$";


    public static final String  PLANNED= "PLANNED";
    public static final String ONGOING= "ONGOING";
    public static final String  COMPLETED ="Finished";
}
