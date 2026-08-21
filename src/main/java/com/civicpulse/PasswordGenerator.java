package com.civicpulse;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        String password = "admin@123";

        String hash = encoder.encode(password);

        System.out.println(hash);
    }
}
