package com.tcs.internal.userservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for the application health check
 *
 * @author Neeraj Sharma
 */
@RestController
public class HealthCheckController {

  /**
   * Method used to return the health check (a html page with http status 200) which indicates that the application is
   * up.
   *
   * @return the health check
   */
  @GetMapping(value = "/healthcheck.html", produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> getHealthCheck()  {
    StringBuilder strBuilder = new StringBuilder();
    strBuilder.append("<html><body>");
    addElement(strBuilder, "Health check", HttpStatus.OK.getReasonPhrase());
    strBuilder.append("</body></html>");
    return new ResponseEntity<>(strBuilder.toString(), HttpStatus.OK);
  }

  /**
   * Adds the element.
   *
   * @param strBuilder The str builder
   * @param key The key
   * @param value The value
   */
  private void addElement(StringBuilder strBuilder, String key, String value) {
    strBuilder.append("<p>");
    strBuilder.append(key);
    strBuilder.append(": <b>");
    strBuilder.append(value);
    strBuilder.append("</b></p>");
  }
}
