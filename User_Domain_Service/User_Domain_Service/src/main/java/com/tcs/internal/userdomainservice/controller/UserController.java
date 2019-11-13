package com.tcs.internal.userdomainservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tcs.internal.userdomainservice.model.User;
import com.tcs.internal.userdomainservice.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;

	@GetMapping("/{id}")
	public User getUser(@PathVariable Integer id) {
		return userService.getUser(id);
	}

	@PostMapping
	public void addUser(@RequestBody User user) {
		userService.addUser(user);
	}

	@PutMapping
	public void updateUser(@RequestBody User user) {
		userService.updateUser(user);
	}

	@DeleteMapping(value = "/{id}")
	public void deleteUser(@PathVariable Integer id) {
		userService.deleteUser(id);
	}

}
