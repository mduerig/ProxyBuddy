package michid.proxybuddy.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import michid.proxybuddy.ProxyBuddy;

public class ImplementInterfacesTest {

    public static class C { }

    interface I1 {
        int m1();
    }

    interface I2 {
        int m2();
    }

    interface I3 {
        int m3();
    }

    @Test
    void implementInterfaces() throws Exception {
        var proxy = new ProxyBuddy<>(C.class, (thisProxy, pipe, method, arguments) ->
            switch (method.getName()) {
                case "m1" -> 1;
                case "m2" -> 2;
                case "m3" -> 3;
                default -> 0;
            })
            .withInterface(I1.class)
            .withInterface(I2.class)
            .withInterface(I3.class)
            .createProxy();

        assertTrue(proxy instanceof I1);
        assertEquals(1, ((I1)proxy).m1());

        assertTrue(proxy instanceof I2);
        assertEquals(2, ((I2)proxy).m2());

        assertTrue(proxy instanceof I3);
        assertEquals(3, ((I3)proxy).m3());
    }

}
