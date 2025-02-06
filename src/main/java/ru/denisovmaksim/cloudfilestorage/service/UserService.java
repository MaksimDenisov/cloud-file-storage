package ru.denisovmaksim.cloudfilestorage.service;


import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.denisovmaksim.cloudfilestorage.model.User;
import ru.denisovmaksim.cloudfilestorage.repository.UserRepository;
import ru.denisovmaksim.cloudfilestorage.exception.UserAlreadyExistException;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        return userRepository.findByName(username)
                .map(this::buildSpringUser)
                .orElseThrow(() -> new UsernameNotFoundException("Not found user with 'username': " + username));
    }

    public User getUserByName(String userName) {
        return userRepository.findByName(userName)
                .orElseThrow();
    }

    private UserDetails buildSpringUser(final User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getName(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("USER"))
        );
    }

    @Transactional
    public User signUp(String username, String password) {
        if (userRepository.findByName(username).isPresent()) {
            throw new UserAlreadyExistException(String.format("User with name %s already exist", username));
        }
        return userRepository.save(User.builder()
                .name(username)
                .password(new BCryptPasswordEncoder().encode(password))
                .build());
    }
}
