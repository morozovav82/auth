package ru.morozov.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.morozov.auth.dto.NewUserDto;
import ru.morozov.auth.dto.UserDto;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
@Slf4j
public class AuthController {

    private final static String USER_ID_HEADER = "X-UserId";

    @Value("${users.url}")
    private String usersUrl;

    @PostMapping("/reg")
    public ResponseEntity registration(@RequestBody NewUserDto user) {
        if (!StringUtils.hasText(user.getUsername())) {
            throw new RuntimeException("Empty username");
        }

        RestTemplate restTemplate = new RestTemplate();
        try {
            log.debug("Sent message to " + usersUrl + "/public/reg");
            ResponseEntity<UserDto> result = restTemplate.postForEntity(usersUrl + "/public/reg", user, UserDto.class);
            log.info("User created with id={}", result.getBody().getId());
            return new ResponseEntity(result.getBody(), HttpStatus.CREATED);
        } catch (Throwable e) {
            log.error("Failed to create user: " + user.getUsername(), e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/signin")
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String signin() {
        return "You must be authenticated";
    }

    @GetMapping("/auth")
    public ResponseEntity auth(HttpSession session, HttpServletResponse response) {
        log.debug("Start auth");
        if (session != null) {
            log.debug("session is not null");
            if (session.isNew()) {
                log.debug("session is new");
                session.invalidate();
            } else {
                try {
                    Long userId = (Long) session.getAttribute(USER_ID_HEADER);
                    log.info("User authenticated: {}", userId);
                    response.setHeader(USER_ID_HEADER, userId.toString());
                    return new ResponseEntity(HttpStatus.OK);
                } catch (Throwable e) {
                    //nothing
                    log.debug("Error: {}", e.getMessage());
                }
            }
        } else {
            log.debug("session is null");
        }

        log.warn("User not authenticated");
        return new ResponseEntity(HttpStatus.UNAUTHORIZED);
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestParam String login, @RequestParam String password, HttpServletRequest request, HttpSession session) {
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("userName", login);
        params.add("userPassword", password);
        try {
            ResponseEntity<UserDto> result = restTemplate.postForEntity(usersUrl + "/public/find", params, UserDto.class);
            log.info("User found with id={}", result.getBody().getId());
            session.invalidate();
            HttpSession newSession = request.getSession();
            newSession.setAttribute(USER_ID_HEADER, result.getBody().getId());
            return new ResponseEntity(HttpStatus.OK);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("User not found by login: " + login);
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        } catch (Throwable e) {
            log.error("Failed to find user by login: " + login, e);
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/logout")
    public void logout(HttpSession session) {
        log.warn("Logout userId: {}", session.getAttribute(USER_ID_HEADER));
        session.invalidate();
    }
}
