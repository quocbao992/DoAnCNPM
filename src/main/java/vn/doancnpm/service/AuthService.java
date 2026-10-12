package vn.doancnpm.service;

import vn.doancnpm.dto.Request.LoginRequest;
import vn.doancnpm.dto.Request.RegisterRequest;
import vn.doancnpm.dto.Response.LoginResponse;
import vn.doancnpm.dto.Response.RegisterResponse;


public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
    
    RegisterResponse register(RegisterRequest registerRequest);

}
