package com.example.user_service;

import com.example.user_service.models.RoleType;
import com.example.user_service.models.UserEntity;
import com.example.user_service.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Bean
	public CommandLineRunner initData(UserRepository userRepository) {
		return args -> {

			UserEntity user = new UserEntity("tomas@gmail.com", "Tomas", passwordEncoder.encode("Tomas123."), RoleType.USER);
			user.setVerified(true);
			userRepository.save(user);

			UserEntity user1 = new UserEntity("tom@gmail.com", "Tomi", passwordEncoder.encode("Tomito123."), RoleType.USER);
			//user1.setVerified(true);
			userRepository.save(user1);

			UserEntity user2 = new UserEntity("manuel@gmail.com", "Manuel", passwordEncoder.encode("Manuel123."), RoleType.ADMIN);
			user2.setVerified(true);
			userRepository.save(user2);

			UserEntity user3 = new UserEntity("manu@gmail.com", "Manu", passwordEncoder.encode("Manuelito123."), RoleType.USER);
			user3.setVerified(true);
			userRepository.save(user3);

			UserEntity user4 = new UserEntity("manubal@gmail.com", "Manuca", passwordEncoder.encode("Manuca123."), RoleType.USER);
			userRepository.save(user4);

			System.out.println("User Server Running!");
		};
	}
}
