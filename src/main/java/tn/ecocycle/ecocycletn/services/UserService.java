package tn.ecocycle.ecocycletn.services;

import tn.ecocycle.ecocycletn.dto.ProfileResponse;
import tn.ecocycle.ecocycletn.dto.ProfileUpdateRequest;

public interface UserService {

    ProfileResponse getCurrentProfile(String email);

    ProfileResponse updateCurrentProfile(String email, ProfileUpdateRequest request);
}
