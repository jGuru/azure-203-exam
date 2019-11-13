package com.tcs.internal.userdomainservice.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tcs.internal.userdomainservice.model.User;
import com.tcs.internal.userdomainservice.repository.UserRepository;

@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;

	public void deleteUser(String emailId) {
		userRepository.deleteById(emailId);
	}

	public void addUser(User user) {
		userRepository.save(user);
	}

	public void updateUser(User user) {
		userRepository.save(user);
	}

	public User getUser(String emailId) {
		
		Optional<User>user= userRepository.findById(emailId);
		return user.get();
	}

}
