package michid.proxybuddy.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import michid.proxybuddy.ProxyBuddy;
import michid.proxybuddy.ProxyBuddy.InvocationHandler;

public class InvocationHandlerReceivesProxyTest {

    public static class Target {
        public boolean isSameProxy(Target proxy) {
            return false;
        }
    }

    @Test
    void implementInterfaces() throws Exception {
        InvocationHandler<Target> invocationHandler = (thisProxy, pipe, method, arguments) ->
            arguments.length == 1 && thisProxy == arguments[0];

        var proxy = new ProxyBuddy<>(Target.class, invocationHandler)
            .createProxy();

        assertTrue(proxy.isSameProxy(proxy));
    }

}
