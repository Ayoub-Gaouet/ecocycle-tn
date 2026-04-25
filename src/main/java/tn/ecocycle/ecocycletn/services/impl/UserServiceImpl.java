package tn.ecocycle.ecocycletn.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.ecocycle.ecocycletn.dto.ProfileResponse;
import tn.ecocycle.ecocycletn.dto.ProfileUpdateRequest;
import tn.ecocycle.ecocycletn.entities.User;
import tn.ecocycle.ecocycletn.exceptions.ResourceNotFoundException;
import tn.ecocycle.ecocycletn.repositories.UserRepository;
import tn.ecocycle.ecocycletn.services.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getCurrentProfile(String email) {
        return ProfileResponse.from(findUserByEmail(email));
    }

    @Override
    @Transactional
    public ProfileResponse updateCurrentProfile(String email, ProfileUpdateRequest request) {
        User user = findUserByEmail(email);
        user.updateProfile(
                request.fullName().trim(),
                request.phone().trim(),
                request.governorate().trim()
        );

        return ProfileResponse.from(userRepository.save(user));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
