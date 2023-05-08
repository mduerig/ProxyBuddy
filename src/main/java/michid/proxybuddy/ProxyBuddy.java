package michid.proxybuddy;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.privateLookupIn;
import static net.bytebuddy.description.modifier.Visibility.PRIVATE;
import static net.bytebuddy.description.modifier.Visibility.PUBLIC;
import static net.bytebuddy.dynamic.loading.ClassLoadingStrategy.UsingLookup;
import static net.bytebuddy.implementation.MethodCall.invoke;
import static net.bytebuddy.implementation.MethodDelegation.withDefaultConfiguration;
import static net.bytebuddy.implementation.bind.annotation.Pipe.Binder.install;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.Pipe;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * {@code ProxyBuddy} is a simple factory for creating dynamic proxies for arbitrary
 * Java classes and interfaces with ByteBuddy.
 * <pre>
 * var proxy = new ProxyBuddy<>(MyClass.class, (pipe, method, arguments) ->
 *     switch (method.getName()) {
 *         case "method1" -> 1;
 *         case "method2" -> 2;
 *         case "method3" -> 3;
 *         default -> 0;
 *     })
 *     .withInterface(Interface1.class)
 *     .withInterface(Interface2.class)
 *     .withInterface(Interface3.class)
 *     .createProxy();
 * </pre>
 */
public class ProxyBuddy<T> {
    private final Class<T> superClass;
    private final Cons<Class<?>> interfaces;
    private final Constructor<T> constructor;
    private final Object[] arguments;
    private final InvocationHandler<T> invocationHandler;

    private ProxyBuddy(
            Class<T> superClass,
            Cons<Class<?>> interfaces,
            Constructor<T> constructor,
            Object[] arguments,
            InvocationHandler<T> invocationHandler) {
        this.superClass = superClass;
        this.interfaces = interfaces;
        this.constructor = constructor;
        this.arguments = arguments;
        this.invocationHandler = invocationHandler;
    }

    /**
     * Implementations of {@code InvocationHandler} are responsible for dispatching calls
     * to the proxy instance.
     *
     * @param <T>
     */
    public interface InvocationHandler<T> {

        /**
         * The {@code invoke} method is called for each call to a method of a proxy.
         * @param proxy  the instance of the proxy this handler was invoked for.
         * @param pipe  a function for forwarding a proxy call to a real instance of the same type {@code T}.
         * @param method  the method that was called on the proxy
         * @param arguments  the arguments that were passed to the proxy method
         * @return  result of the method call
         * @throws Exception
         */
        Object invoke(T proxy, Function<T, Object> pipe, Method method, Object... arguments) throws Exception;
    }

    /**
     * Create a proxy of type {@code T} for the given {@code superClass}. The returned instance
     * is a subclass of {@code superClass}.
     * @param superClass  class to proxy
     * @param invocationHandler  handler receiving all calls to the returned proxy
     */
    public ProxyBuddy(Class<T> superClass, InvocationHandler<T> invocationHandler) {
        this(superClass, cons(ProxyBuddyProxy.class, null), null, new Object[]{}, invocationHandler);
    }

    /**
     * Create a proxy implementing the given interface.
     * @param interfaze interface to implement
     * @return  a new {@code ProxyBuddy} instance
     */
    public ProxyBuddy<T> withInterface(Class<?> interfaze) {
        return new ProxyBuddy<>(superClass, cons(interfaze, interfaces), constructor, arguments, invocationHandler);
    }

    /**
     * Create a proxy with correct implementations for {@code equals} and {@code hashCode}.
     * With this implementation a proxy will never equal an instance of the proxied class.
     * @param witness  a object for witnessing the equality between a proxy and proxied instance.
     *                 The only requirement for the witness is to implement {@code equals} and
     *                 {@code hashCode} consistently with the proxied class.
     * @return  a new {@code ProxyBuddy} instance
     */
    public ProxyBuddy<T> withProxyNeverEqualsTarget(Object witness) {
        return new ProxyBuddy<>(superClass, interfaces, constructor, arguments, (proxy, pipe, method, arguments) -> {
            if ("equals".equals(method.getName())) {
                interface Witness {
                    Object get();
                }

                if (isProxy(arguments[0])) {
                    return arguments[0].equals((Witness) () -> witness);
                } else {
                    if (arguments[0] instanceof Witness) {
                        return witness.equals(((Witness) arguments[0]).get());
                    } else {
                        return false;
                    }
                }
            } else if ("hashCode".equals(method.getName())) {
                return 31 * witness.hashCode();
            } else {
                return invocationHandler.invoke(proxy, pipe, method, arguments);
            }
        });
    }

    /**
     * Create a proxy with correct implementations for {@code equals} and {@code hashCode}.
     * With this implementation a may equal an instance of the proxied class.
     * @param witness  a object for witnessing the equality between a proxy and proxied instance.
     *                 The only requirement for the witness is to implement {@code equals} and
     *                 {@code hashCode} consistently with the proxied class.
     * @return  a new {@code ProxyBuddy} instance
     */
    public ProxyBuddy<T> withProxyCanEqualTarget(Object witness) {
        return new ProxyBuddy<>(superClass, interfaces, constructor, arguments, (proxy,pipe, method, arguments) -> {
            if ("equals".equals(method.getName())) {
                return witness.equals(arguments[0]);
            } else if ("hashCode".equals(method.getName())) {
                return witness.hashCode();
            } else {
                return invocationHandler.invoke(proxy, pipe, method, arguments);
            }
        });
    }

    /**
     * Create a proxy that uses the supplied constructor and arguments for creating a super class
     * instance.
     * @param constructor  constructor for creating the super class instance
     * @param arguments  arguments passed to the constructor for creating the super class instance
     * @return
     */
    public ProxyBuddy<T> withConstructor(Constructor<T> constructor, Object... arguments) {
        return new ProxyBuddy<>(superClass, interfaces, constructor, arguments, invocationHandler);
    }

    /**
     * Create a proxy for a class of type {@code T}
     * @return  a new proxy instance of type {@code T}
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    public T createProxy()
    throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        return new ByteBuddy()
            .subclass(superClass)
            .implement(toList(interfaces))
            .method(isPublic())
            .intercept(withDefaultConfiguration()
                .withBinders(install(Function.class))
                .toField("INVOKER"))
            .defineField("INVOKER", Invoker.class, PRIVATE)
            .defineConstructor(PUBLIC)
                .withParameters(Invoker.class)
                .intercept((constructor == null
                    ? invoke(superClass.getConstructor())
                    : invoke(constructor).with(arguments))
                .andThen(FieldAccessor.ofField("INVOKER").setsArgumentAt(0)))
            .make()
            .load(superClass.getClassLoader(), UsingLookup.of(privateLookupIn(superClass, lookup())))
            .getLoaded()
        .getConstructor(Invoker.class)
            .newInstance(new Invoker<>(invocationHandler));
    }

    /**
     * Helper method to determine whether an instance is a proxy created through {@code ProxyBuddy}.
     * @param object  an instance to check
     * @return  {@code true} if and only if {@code object} is a proxy created by {@code ProxyBuddy}.
     */
    public static boolean isProxy(Object object) {
        return object instanceof ProxyBuddyProxy;
    }

    /**
     * Marker interface for {@code ProxyBuddy} proxies.
     */
    public interface ProxyBuddyProxy {}

    /**
     * Internal helper class for delegating method calls from a proxy instance to
     * the {@link InvocationHandler}.
     * @param <T>
     */
    public static final class Invoker<T> {

        private final InvocationHandler<T> invocationHandler;

        private Invoker(InvocationHandler<T> invocationHandler) {
            this.invocationHandler = invocationHandler;
        }

        @RuntimeType
        public Object delegate(@This T proxy, @Pipe Function<T, Object> pipe, @Origin Method method, @AllArguments Object[] args)
        throws Exception {
            return invocationHandler.invoke(proxy, pipe, method, args);
        }
    }

    private record Cons<T>(T value, Cons<T> values) {}

    private static <T> Cons<T> cons(T value, Cons<T> values) {
        return new Cons<>(value, values);
    }

    private static <T> List<T> toList(Cons<T> values) {
        var result = new ArrayList<T>();

        while (values != null) {
            result.add(values.value);
            values = values.values;
        }
        return result;
    }

}
