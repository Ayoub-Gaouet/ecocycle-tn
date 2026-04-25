package tn.ecocycle.ecocycletn.services;

import tn.ecocycle.ecocycletn.dto.AuthResponse;
import tn.ecocycle.ecocycletn.dto.LoginRequest;
import tn.ecocycle.ecocycletn.dto.RegisterRequest;
import tn.ecocycle.ecocycletn.dto.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
