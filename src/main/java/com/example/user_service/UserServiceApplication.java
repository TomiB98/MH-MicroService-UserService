package com.example.user_service;

import com.example.user_service.models.RoleType;
import com.example.user_service.models.UserEntity;
import com.example.user_service.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(UserRepository userRepository) {
		return args -> {

			UserEntity user = new UserEntity("tomas@gmail.com", "Tomas", "Tomas123.", RoleType.USER);
			userRepository.save(user);
			UserEntity user1 = new UserEntity("tom@gmail.com", "Tomi", "Tomito123.", RoleType.ADMIN);
			userRepository.save(user1);
			UserEntity user2 = new UserEntity("manuel@gmail.com", "Manuel", "Manuel123.", RoleType.ADMIN);
			userRepository.save(user2);
			UserEntity user3 = new UserEntity("manu@gmail.com", "Manu", "Manuelito123.", RoleType.USER);
			userRepository.save(user3);

			System.out.println("User Server Running!");
		};
	}
}
