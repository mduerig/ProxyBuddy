package michid.proxybuddy;

import static michid.proxybuddy.ProxyArgumentProviderBase.ProxyType.DELEGATE;
import static michid.proxybuddy.ProxyArgumentProviderBase.ProxyType.REFLECT;

import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import michid.proxybuddy.ProxyBuddy.InvocationHandler;

public abstract class ProxyArgumentProviderBase<T> implements ArgumentsProvider {

    public enum ProxyType { REFLECT, DELEGATE }

    protected abstract T createTarget();

    protected abstract T createProxy(InvocationHandler<T> invocationHandler) throws Exception;

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        T reflectionTarget = createTarget();
        T delegationTarget = createTarget();
        return Stream.of(
            Arguments.of(Named.of(("reflecting proxy"), createProxy(newReflectionHandler(reflectionTarget))), reflectionTarget, REFLECT),
            Arguments.of(Named.of(("delegating proxy"), createProxy(newDelegationHandler(delegationTarget))), delegationTarget, DELEGATE));
    }

    private static <T> InvocationHandler<T> newReflectionHandler(T target) {
        return  (proxy, pipe, method, arguments) -> method.invoke(target, arguments);
    }

    private static <T> InvocationHandler<T> newDelegationHandler(T target) {
        return  (proxy, pipe, method, arguments) -> pipe.apply(target);
    }
}
