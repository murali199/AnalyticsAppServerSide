package com.example.analytics.controller;

import com.example.analytics.exception.ResourceNotFoundException;
import com.example.analytics.model.User;
import com.example.analytics.payload.*;
import com.example.analytics.repository.UserRepository;
import com.example.analytics.security.UserPrincipal;
import com.example.analytics.security.CurrentUser;

import java.util.ArrayList;
import java.util.List;

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
    	UserProfile UserProfile = new UserProfile(currentUser.getId(), currentUser.getUsername(), currentUser.getName(), currentUser.getEmail());
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
        UserProfile userProfile = new UserProfile(user.getId(), user.getUsername(), user.getName(), user.getEmail(), user.getCreatedAt());
        return userProfile;
    }

    @GetMapping("/users/allUsers")
    public List<UserProfile> getAllUser() {
        List<User> users = userRepository.findAll();
        List<UserProfile> userProfiles = new ArrayList<UserProfile>();
        for (User user : users) {
        	UserProfile userProfile = new UserProfile(user.getId(), user.getUsername(), user.getName(),  user.getEmail(), user.getCreatedAt());
        	userProfiles.add(userProfile);
		}
        return userProfiles;
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
	
	@DeleteMapping("/users/delete/{id}")
	  public String deleteUser(@PathVariable Long id) {
	    try {
	    	userRepository.deleteById(id);
	    	return "Success";
		} catch (Exception e) {
			return "Error" + e;
		}
	  }
    
}
