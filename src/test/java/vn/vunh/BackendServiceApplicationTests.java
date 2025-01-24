package vn.vunh;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.vunh.controller.AuthenticationController;
import vn.vunh.controller.UserController;

@SpringBootTest
class BackendServiceApplicationTests {

    //	@Autowired
    @InjectMocks
    private AuthenticationController authenticationController;

    //	@Autowired
    @InjectMocks
    private UserController userController;

    // Testing if application loads correctly
    @Test
    void contextLoads() {
        Assertions.assertThat(authenticationController).isNotNull();
        Assertions.assertThat(userController).isNotNull();
    }

}
