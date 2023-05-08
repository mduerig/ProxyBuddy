package michid.proxybuddy.test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import michid.proxybuddy.ProxyBuddy;

public class InvalidProxyTest {

    @Test
    void proxyNonOpenPackage() {
        var proxyBuddy = new ProxyBuddy<>(Object.class,
            (proxy, pipe, method, arguments) -> null);

        assertThrows(IllegalAccessException.class, proxyBuddy::createProxy);
    }

    public static final class T { }

    @Test
    void proxyFinalClass() {
        var proxyBuddy = new ProxyBuddy<>(T.class, (proxy, pipe, method, arguments) -> null);

        assertThrows(IllegalArgumentException.class, proxyBuddy::createProxy);
    }

    @Test
    void proxyArray() {
        var proxyBuddy = new ProxyBuddy<>(T[].class, (proxy, pipe, method, arguments) -> null);

        assertThrows(IllegalArgumentException.class, proxyBuddy::createProxy);
    }

    @Test
    void proxyPrimitive() {
        var proxyBuddy = new ProxyBuddy<>(int.class, (proxy, pipe, method, arguments) -> null);

        assertThrows(IllegalArgumentException.class, proxyBuddy::createProxy);
    }

}
