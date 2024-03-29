package com.tcs.internal.userservice.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

import com.tcs.internal.userservice.model.ErrorResponse;
import com.tcs.internal.userservice.model.User;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Controller to manage user
 *
 * @author Neeraj Sharma
 */
@Api(value = "User service", tags = "User service")
@RestController
@RequestMapping("/v1/user")
public class UserController {
	/**
	 * Using RestTemplate to call User_Domain_service which is another micro service
	 */
	RestTemplate restTemplate = new RestTemplate();

	/**
	 * This is a http get method to get the user by their email id as email id is
	 * primary key
	 * 
	 * @param emailId the email id of the user
	 */

	@GetMapping("/{emailId}")
	public User getUserbyId(@PathVariable String emailId) {
		return restTemplate.getForObject("http://localhost:8081/user/" + emailId, User.class);

	}

	/**
	 * This method is used to add a new user in the database the request coming from
	 * any http client, with json payload like { "emailId": "abc@123.com", "name":
	 * "Mihika", "surname": "Sharma", "password":"abc1234" }
	 */
	// we can return the ResponseEntity<User> object instead of void
	@ApiOperation(notes = "Creates a new user based on user input", value = "Create User", nickname = "addUser", response = User.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully created user", response = User.class),
	@ApiResponse(code = 400, message = "Bad request", response = ErrorResponse.class),
	@ApiResponse(code = 500, message = "Internal server error", response = ErrorResponse.class) })
	@PostMapping()
	public void addUser(
			@RequestBody @ApiParam(name = "user", value = "The create user request payload", required = true) User user) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("Content-Type", "application/json");

		headers.putAll(map);

		Map<String, Object> jsonUserPayload = new HashMap<String, Object>();
		jsonUserPayload.put("emailId", user.getEmailId());
		jsonUserPayload.put("name", user.getName());
		jsonUserPayload.put("surname", user.getSurname());
		jsonUserPayload.put("password", user.getPassword());

		HttpEntity<?> request = new HttpEntity<>(jsonUserPayload, headers);
		restTemplate.postForEntity("http://localhost:8081/user", request, User.class);

	}

	/**
	 * This method is to delete a user from the database and can be called by any
	 * http client
	 * 
	 * @param emailId the email id of the user
	 */

	@DeleteMapping(value = "/{emailId}")
	public void deleteUser(@PathVariable String emailId) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("emailId", emailId);

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.delete("http://localhost:8081/user/" + emailId, params);
	}

}
