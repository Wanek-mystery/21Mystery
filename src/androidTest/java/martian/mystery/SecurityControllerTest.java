package martian.mystery;

import org.junit.Before;
import org.junit.Test;

import martian.mystery.controller.SecurityController;

import static org.junit.Assert.*;

public class SecurityControllerTest {

    SecurityController securityController;
    @Before
    public void setUp() throws Exception {
        securityController = new SecurityController();
    }
    @Test
    public void getAnswer() {
        assertEquals("время",securityController.getAnswer(7)[0]);
    }
}