package ru.denisovmaksim.cloudfilestorage;


import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.denisovmaksim.cloudfilestorage.service.UserService;


@Profile("dev")
@Component
@AllArgsConstructor
public class SampleDataLoader implements ApplicationRunner {

    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        userService.signUp("user", "password");
    }
}
