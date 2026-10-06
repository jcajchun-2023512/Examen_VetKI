package com.jcajchun_2023512.VetKI.service;

import com.jcajchun_2023512.VetKI.dto.auth.AuthResponse;
import com.jcajchun_2023512.VetKI.dto.auth.LoginRequest;
import com.jcajchun_2023512.VetKI.dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
