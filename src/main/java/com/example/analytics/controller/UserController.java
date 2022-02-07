package com.example.analytics.controller;

import com.example.analytics.exception.ResourceNotFoundException;
import com.example.analytics.model.User;
import com.example.analytics.payload.*;
import com.example.analytics.repository.UserRepository;
import com.example.analytics.security.UserPrincipal;
import com.example.analytics.security.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    public UserProfile getCurrentUser(@CurrentUser UserPrincipal currentUser) {
    	UserProfile UserProfile = new UserProfile(currentUser.getId(), currentUser.getUsername(), currentUser.getName());
        return UserProfile;
    }

    @GetMapping("/user/checkUsernameAvailability")
    public UserIdentityAvailability checkUsernameAvailability(@RequestParam(value = "username") String username) {
        Boolean isAvailable = !userRepository.existsByUsername(username);
        return new UserIdentityAvailability(isAvailable);
    }

    @GetMapping("/user/checkEmailAvailability")
    public UserIdentityAvailability checkEmailAvailability(@RequestParam(value = "email") String email) {
        Boolean isAvailable = !userRepository.existsByEmail(email);
        return new UserIdentityAvailability(isAvailable);
    }

    @GetMapping("/users/{username}")
    public UserProfile getUserProfile(@PathVariable(value = "username") String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        UserProfile userProfile = new UserProfile(user.getId(), user.getUsername(), user.getName(), user.getCreatedAt());

        return userProfile;
    }

	@PutMapping("/users/update/{id}")
	public String updateUser(@PathVariable Long id, @RequestBody UserProfile userProfile) {
		try {
			User user = userRepository.findById(id)
					.orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
			user.setName(userProfile.getName());
			user.setUsername(userProfile.getUsername());
			user.setEmail(userProfile.getEmail());
			userRepository.save(user);
			return "Success";
		} catch (Exception e) {
			return "Error" + e;
		}
	}
    
}
