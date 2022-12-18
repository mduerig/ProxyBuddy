package michid.proxybuddy.test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import michid.proxybuddy.ProxyBuddy;
import org.junit.jupiter.api.Test;

public class InvalidProxyTest {

    @Test
    void proxyNonOpenPackage() {
        var proxyBuddy = new ProxyBuddy<>(Object.class,
            (pipe, method, arguments) -> null);

        assertThrows(IllegalAccessException.class, proxyBuddy::createProxy);
    }

    public static final class T { }

    @Test
    void proxyFinalClass() {
        var proxyBuddy = new ProxyBuddy<>(T.class, (pipe, method, arguments) -> null);

        assertThrows(IllegalArgumentException.class, proxyBuddy::createProxy);
    }

    @Test
    void proxyArray() {
        var proxyBuddy = new ProxyBuddy<>(T[].class, (pipe, method, arguments) -> null);

        assertThrows(IllegalArgumentException.class, proxyBuddy::createProxy);
    }

    @Test
    void proxyPrimitive() {
        var proxyBuddy = new ProxyBuddy<>(int.class, (pipe, method, arguments) -> null);

        assertThrows(IllegalArgumentException.class, proxyBuddy::createProxy);
    }

}
