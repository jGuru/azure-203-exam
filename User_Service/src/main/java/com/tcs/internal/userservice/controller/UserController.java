package com.tcs.internal.userservice.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.tcs.internal.userservice.model.User;

@RestController
@RequestMapping("/user")
public class UserController {
	// We can also use WebClient

	RestTemplate restTemplate = new RestTemplate();

	@GetMapping("/{id}")
	public User getUserbyId(@PathVariable Integer id) {
		return restTemplate.getForObject("http://localhost:8081/user/" + id, User.class);

	}

	@PostMapping("/user")
	public void addUser(@RequestBody User user) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("Content-Type", "application/json");

		headers.putAll(map);

		Map<String, Object> jsonUserPayload = new HashMap<String, Object>();
		jsonUserPayload.put("id", user.getId());
		jsonUserPayload.put("name", user.getName());
		jsonUserPayload.put("surname", user.getSurname());

		HttpEntity<?> request = new HttpEntity<>(jsonUserPayload, headers);

		ResponseEntity<User> response = restTemplate.postForEntity("http://localhost:8081/user", request, User.class);

	}
	
	@DeleteMapping(value="/{id}")
	public void deleteUser(Integer id)
	{
		
	}

}
