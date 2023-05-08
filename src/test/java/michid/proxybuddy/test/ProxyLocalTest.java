package michid.proxybuddy.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import michid.proxybuddy.ProxyBuddy;

public class ProxyLocalTest {

    @SuppressWarnings("JavaReflectionMemberAccess")
    @Test
    void proxyLocalClass() throws Exception {
        class Local {
            public Local() {}
            public int get() { return 42; }
        }

        var localProxy = new ProxyBuddy<>(Local.class,
                (proxy, pipe, method, arguments) -> pipe.apply(new Local()))
            .withConstructor(Local.class.getConstructor(ProxyLocalTest.class), new Object[]{this})
            .createProxy();

        assertEquals(42, localProxy.get());
    }

}
