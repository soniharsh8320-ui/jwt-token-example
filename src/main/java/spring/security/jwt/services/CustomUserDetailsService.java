package spring.security.jwt.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import spring.security.jwt.entity.User;
import spring.security.jwt.repository.UserRepository;

import java.util.Collections;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username={}", username);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            log.warn("User not found for username={}", username);
            throw new UsernameNotFoundException("User Not Found with username: "+ username);
        }
        log.debug("User loaded successfully for username={}", username);
        return new org.springframework.security.core.userdetails.User
                (user.getUsername(),user.getPassword(),Collections.emptyList());
    }
}
