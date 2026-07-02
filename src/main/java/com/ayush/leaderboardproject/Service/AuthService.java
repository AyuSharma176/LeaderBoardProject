package com.ayush.leaderboardproject.Service;

import com.ayush.leaderboardproject.Model.User;
import com.ayush.leaderboardproject.Repository.UserRepository;
import com.ayush.leaderboardproject.Security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final LeaderBoardService leaderBoardService;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, LeaderBoardService leaderBoardService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.leaderBoardService = leaderBoardService;
    }

    public String register(String username,String email,String password) throws Exception{
        if(userRepository.existsByUsername(username)){
            throw new Exception("Username already exists");
        }
        if(userRepository.existsByEmail(email)){
            throw new Exception("Email already exists");
        }
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role("ROLE_USER")
                .build();

        User saved=userRepository.save(user);
        leaderBoardService.addEntryForUser(saved,username);
        return jwtUtil.generateToken(username);
    }

    public String login(String username,String password)throws Exception{
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username,password)
            );
        } catch (Exception e) {
            throw new Exception("Invalid username or password");
        }
        return jwtUtil.generateToken(username);
    }
    public String oauthRegister(String leetcodeUsername,String email) throws Exception{
        if(userRepository.existsByUsername(leetcodeUsername)){
            throw new Exception("This leetcode Username is already exist");
        }
        if(userRepository.existsByEmail(email)){
            throw new Exception("This email is already exist");
        }
        User user = User.builder()
                .username(leetcodeUsername)
                .email(email)
                .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                .role("ROLE_USER")
                .build();

        User saved = userRepository.save(user);
        leaderBoardService.addEntryForUser(saved,leetcodeUsername);
        return jwtUtil.generateToken(leetcodeUsername);
    }
}
