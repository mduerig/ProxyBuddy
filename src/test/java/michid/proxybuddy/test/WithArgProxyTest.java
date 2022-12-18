package michid.proxybuddy.test;

import static michid.proxybuddy.ProxyBuddy.isProxy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;

import michid.proxybuddy.ProxyArgumentProviderBase;
import michid.proxybuddy.ProxyBuddy;
import michid.proxybuddy.ProxyBuddy.InvocationHandler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class WithArgProxyTest {

    public static class Target {

        private final int a;

        public Target(int a) {
            this.a = a;
        }
        public int add(int b) {
            return a + b;
        }

        public String noArgMethod() {
            return "noArg";
        }
    }

    static class ProxyArgumentProvider extends ProxyArgumentProviderBase<Target> {
        @Override
        protected Target createTarget() {
            return new Target(1);
        }

        @Override
        protected Target createProxy(InvocationHandler<Target> invocationHandler)
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
            return new ProxyBuddy<>(Target.class, invocationHandler)
                .withConstructor(Target.class.getConstructor(int.class), 3)
                .createProxy();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(NonArgProxyTest.ProxyArgumentProvider.class)
    void testIsProxy(NonArgProxyTest.Target proxy, NonArgProxyTest.Target target) {
        assertTrue(isProxy(proxy));
        assertFalse(isProxy(target));
    }

    @ParameterizedTest
    @ArgumentsSource(ProxyArgumentProvider.class)
    void testProxyCalls(Target proxy) {
        assertEquals(3, proxy.add(2));
        assertEquals("noArg", proxy.noArgMethod());
    }

}
