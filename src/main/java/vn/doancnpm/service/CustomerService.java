package vn.doancnpm.service;

import vn.doancnpm.dto.Request.CustomerRequest;
import vn.doancnpm.dto.Response.CustomerResponse;

public interface CustomerService {

    CustomerResponse getMyProfile(Long userId);

    CustomerResponse updateMyProfile(Long userId,CustomerRequest request);

<<<<<<< HEAD
=======
    float getMyLoyalPoint(Long userId);
>>>>>>> e61a28af7a0506996cadf03647d083e5436ace95

}