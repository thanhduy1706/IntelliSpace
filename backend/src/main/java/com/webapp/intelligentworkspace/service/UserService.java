package com.webapp.intelligentworkspace.service;

import com.webapp.intelligentworkspace.model.entity.Storage;
import com.webapp.intelligentworkspace.model.entity.Token;
import com.webapp.intelligentworkspace.model.request.OTPRequest;
import com.webapp.intelligentworkspace.model.response.AuthResponse;
import com.webapp.intelligentworkspace.model.entity.User;
import com.webapp.intelligentworkspace.model.response.OtpResponse;
import com.webapp.intelligentworkspace.repository.StorageRepository;
import com.webapp.intelligentworkspace.repository.TokenRepository;
import com.webapp.intelligentworkspace.repository.UserRepository;
import lombok.Data;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.Objects;
import java.util.Random;

@Service
@Data
public class UserService {

    private final PasswordEncoder passwordEncoder;

    UserRepository userRepository;

    JwtService jwtService;

    TokenRepository tokenRepository;

    StorageService storageService;
    StorageRepository storageRepository;
    private final AuthenticationManager authenticationManager;
    EmailSenderService emailSenderService;

    public UserService(StorageRepository storageRepository,UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager,TokenRepository tokenRepository, StorageService storageService, EmailSenderService emailSenderService) {
        this.tokenRepository=tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.storageService= storageService;
        this.storageRepository=storageRepository;
        this.emailSenderService= emailSenderService;
    }

    public AuthResponse createUser(User user){
        System.out.println("Register");
        if(userRepository.findUserByUsername(user.getUsername()).orElse(null)!= null){
            return  new AuthResponse("User has been registered");
        }else {
            if(userRepository.findUserByEmail(user.getEmail()).orElse(null)!= null){
                return new AuthResponse("Email has been registered");
            }
            User newUser = new User();
            Random random = new Random();
            newUser.setId(random.nextInt(0,100000000));
            newUser.setUsername(user.getUsername());
            newUser.setPassword(passwordEncoder.encode(user.getPassword()));
            newUser.setEmail(user.getEmail());
            newUser.setNumberPhone(user.getNumberPhone());
            Storage storage= new Storage();
            storage.setCurrentStorage(0);
            storageRepository.save(storage);
            newUser.setStorage(storage);
            userRepository.save(newUser);
            String token= jwtService.generateRefreshToken(newUser);
            saveToken(token,newUser);


            return new AuthResponse("Created successfully with the username "+ newUser.getUsername());
        }
    }

    public AuthResponse login(User user){
        System.out.println("Login phase started");
        if(userRepository.findUserByUsername(user.getUsername()).isEmpty()){
            return new AuthResponse("Do not register with this user name");
        }else {
            try {
                System.out.println("running");
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                user.getUsername(),
                                user.getPassword()
                        )
                );
                User user1= userRepository.findUserByUsername(user.getUsername()).orElse(null);
                System.out.println(user1);
                if(user1 != null) {
                    String accessToken = jwtService.generateAccessToken(user1);
                    String refreshToken = jwtService.generateRefreshToken(user1);
                    saveToken(refreshToken, user1);
                    return new AuthResponse("Login successfully hehe", accessToken, refreshToken, user1,user1.getStorage().getId());
                }
                else {
                    return  new AuthResponse("Login failed");
                }
            }catch (Exception e) {
                System.out.println("error");
                System.out.println(e);
            }

            return new AuthResponse("Wrong password");
        }
    }


    public void saveToken(String token,User user){
        Token existingToken= tokenRepository.findByUser(user).orElse(null);
        if(existingToken!= null){
            existingToken.setToken(token);
            existingToken.setLogout(false);
            tokenRepository.save(existingToken);
            return;
        }else {
            Token newToken=Token.builder()
                    .isLogout(false)
                    .user(user)
                    .token(token)
                    .build();

            tokenRepository.save(newToken);
        }
    }


    public AuthResponse loginWithOauth(String email, String name){
        User user= userRepository.findUserByUsername(name).orElse(null);
        if( user == null ){
            return registerWithOauth(email, name);
        }else{
            System.out.println("Login with OAuth");
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            name,
                            name
                    )
            );
            System.out.println(user);
            User user1= userRepository.findUserByUsername(user.getUsername()).orElse(null);
            System.out.println(user1);
            if(user1 != null) {
                String accessToken = jwtService.generateAccessToken(user1);
                String refreshToken = jwtService.generateRefreshToken(user1);
                saveToken(refreshToken, user1);
                return new AuthResponse("Login successfully hehe", accessToken, refreshToken, user1,user1.getStorage().getId());
            }
            else {
                return  new AuthResponse("Login failed");
            }
        }
    }

    private AuthResponse registerWithOauth(String email, String name){
        User newUser = new User();
        Random random = new Random();
        newUser.setId(random.nextInt(0,100000000));
        newUser.setUsername(name);
        newUser.setPassword(passwordEncoder.encode(name));
        newUser.setEmail(email);
        Storage storage= new Storage();
        storage.setCurrentStorage(0);
        storageRepository.save(storage);
        newUser.setStorage(storage);
        userRepository.save(newUser);
        String token= jwtService.generateRefreshToken(newUser);
        saveToken(token,newUser);
        String refreshToken= jwtService.generateAccessToken(newUser);
        return  new AuthResponse("Login Success",token,refreshToken,newUser, newUser.getStorage().getId());
    }

    public AuthResponse refreshAccessToken(String refreshToken, Integer userId) {
        User user= userRepository.findUserById(userId).orElse(null);
        if(user==null){
            return new AuthResponse("Not found user", null, null);
        }
        Token userRefreshToken= tokenRepository.findByToken(refreshToken).orElse(null);
        if(jwtService.isExpired(refreshToken)){
            logOut(userId);
            return new AuthResponse("Log out", null, null );
        }
        String token=jwtService.refreshAccessToken(userRefreshToken.getToken(), user);
        return new AuthResponse("This is new accessToken",token,refreshToken );
    }


    public void resetPassword(){

    }

    public void logOut(Integer userId){
        User user= userRepository.findUserById(userId).orElse(null);
        if(user==null){
            System.out.println("Do not have user");
            return;
        }else {
            Token token = tokenRepository.findByUser(user).orElse(null);
            if(token!=null){
                token.setLogout(true);
                tokenRepository.save(token);
            }else {
                System.out.println("Can not have the token");
                return;
            }
        }
    }

    public void sendEmail(String email){
        User user= userRepository.findUserByEmail(email).orElse(null);
        if( user == null){
            System.out.println("Can not find user by user Email");
            return;
        }
        Random random= new Random();
        user.setResetCode(random.nextLong(10000,99999));
        userRepository.save(user);
        Long resetCode= user.getResetCode();
        String subject = "Reset Password OTP Code ";
        emailSenderService.sendEmail(email,subject,resetCode.toString());

    }

    public OtpResponse checkOtp(OTPRequest otpRequest){
        User user= userRepository.findUserByEmail(otpRequest.getEmail()).orElse(null);
        if(user== null){
            return  new OtpResponse("Do not have user", false);
        }

        if(Objects.equals(otpRequest.getOtp(), user.getResetCode())){
            return new OtpResponse("Valid code", true);
        }else {
            return new OtpResponse("Invalid Code", false);
        }
    }

    public AuthResponse resetPassword(User request){
        User user = userRepository.findUserByEmail(request.getEmail()).orElse(null);
        if(user==null){
            return new AuthResponse("Not valid user");
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return  new AuthResponse("Change Success");
    }

    public String getUsernameById(Integer userId) {
        User user = userRepository.findUserById(userId).orElse(null);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with id: " + userId);
        }
        return user.getUsername();
    }

}
