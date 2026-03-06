package com.malgn.controller;

import com.malgn.dto.AuthenticationRequest;
import com.malgn.dto.AuthenticationResponse;
import com.malgn.dto.UserResponse;
import com.malgn.security.JwtUtil;
import com.malgn.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    private AuthenticationManager authenticationManager;
    private JwtUtil jwtTokenUtil;
    private UserDetailsService userDetailsService;
    private UserService userService;

    @Autowired

    public AuthenticationController(
            AuthenticationManager authenticationManager,
            JwtUtil jwtTokenUtil,
            UserDetailsService userDetailsService,
            UserService userService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(
            @RequestBody AuthenticationRequest authRequest
    ) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authRequest.username(), authRequest.password()
            ));

            UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.username());

            UserResponse userResponse = userService.getUserByUsername(userDetails.getUsername());

            return ResponseEntity.ok(
                    AuthenticationResponse.builder()
                            .jwt(jwtTokenUtil.generateToken(userDetails, userResponse.id()))
                            .build()
            );
        } catch (BadCredentialsException e) {
            logger.error("인증 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("인증 중 에러가 발생했습니다.");
        }
    }
}
