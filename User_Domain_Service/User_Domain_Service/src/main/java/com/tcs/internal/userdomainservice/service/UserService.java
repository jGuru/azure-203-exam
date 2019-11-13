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

	public void deleteUser(Integer id) {
		userRepository.deleteById(id);
	}

	public void addUser(User user) {
		userRepository.save(user);
	}

	public void updateUser(User user) {
		userRepository.save(user);
	}

	public User getUser(Integer id) {
		
		Optional<User>user= userRepository.findById(id);
		return user.get();
	}

}
