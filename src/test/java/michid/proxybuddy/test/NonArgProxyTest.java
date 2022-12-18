package michid.proxybuddy.test;

import static michid.proxybuddy.ProxyBuddy.isProxy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;

import michid.proxybuddy.ProxyArgumentProviderBase;
import michid.proxybuddy.ProxyArgumentProviderBase.ProxyType;
import michid.proxybuddy.ProxyBuddy;
import michid.proxybuddy.ProxyBuddy.InvocationHandler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class NonArgProxyTest {
    private static class TestException extends Exception{}

    public static class Target {
        boolean voidCalled;

        public int add(int a, int b) {
            return a + b;
        }

        public String noArgMethod() {
            return "noArg";
        }

        public void voidMethod() {
            voidCalled = true;
        }

        public void exception() throws TestException {
            throw new TestException();
        }

        @Override
        public int hashCode() {
            return 42;
        }

        @Override
        public String toString() {
            return "four two";
        }
    }

    static class ProxyArgumentProvider extends ProxyArgumentProviderBase<Target> {
        @Override
        protected Target createTarget() {
            return new Target();
        }

        @Override
        protected Target createProxy(InvocationHandler<Target> invocationHandler)
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
            return new ProxyBuddy<>(Target.class, invocationHandler).createProxy();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(ProxyArgumentProvider.class)
    void testProxyCalls(Target proxy, Target target, ProxyType type) {
        assertEquals(3, proxy.add(1, 2));
        assertEquals("noArg", proxy.noArgMethod());

        proxy.voidMethod();
        assertTrue(target.voidCalled);

        var ex = assertThrows(Exception.class, proxy::exception);
        switch (type) {
            case REFLECT -> {
                assertEquals(InvocationTargetException.class, ex.getClass());
                assertEquals(TestException.class, ex.getCause().getClass());
            }
            case DELEGATE -> assertEquals(TestException.class, ex.getClass());
        }

        assertEquals(42, proxy.hashCode());
        assertEquals("four two", proxy.toString());
        assertEquals("four two", proxy + "");
    }

    @ParameterizedTest
    @ArgumentsSource(ProxyArgumentProvider.class)
    void testIsProxy(Target proxy, Target target) {
        assertTrue(isProxy(proxy));
        assertFalse(isProxy(target));
    }

}
