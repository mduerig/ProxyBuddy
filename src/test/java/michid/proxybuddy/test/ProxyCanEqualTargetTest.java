package michid.proxybuddy.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

import michid.proxybuddy.ProxyBuddy;

public class ProxyCanEqualTargetTest {

    @SuppressWarnings("ClassCanBeRecord")
    public static class Target {
        private final int value;

        public Target(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof Target) {
                return value == ((Target) obj).getValue();
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return getValue();
        }
    }

    private static Target createProxy(Target target)
    throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        return new ProxyBuddy<>(Target.class, (proxy,pipe, method, arguments) -> pipe.apply(target))
            .withConstructor(Target.class.getConstructor(int.class), 0)
            .withProxyCanEqualTarget(target)
            .createProxy();
    }

    @SuppressWarnings({"EqualsWithItself", "SimplifiableAssertion"})
    @Test
    public void testEquality()
    throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Target target1 = new Target(1);
        Target target2 = new Target(1);

        Target proxy1 = createProxy(target1);
        Target proxy2 = createProxy(target2);

        assertTrue(target1.equals(proxy1));
        assertTrue(target1.equals(proxy2));

        assertTrue(target2.equals(proxy1));
        assertTrue(target2.equals(proxy2));

        assertTrue(proxy1.equals(target1));
        assertTrue(proxy1.equals(target2));

        assertTrue(proxy1.equals(proxy1));
        assertTrue(proxy1.equals(proxy2));

        assertTrue(proxy2.equals(target1));
        assertTrue(proxy2.equals(target2));

        assertTrue(proxy2.equals(proxy1));
        assertTrue(proxy2.equals(proxy2));

        assertEquals(target1.hashCode(), proxy1.hashCode());
        assertEquals(target1.hashCode(), proxy2.hashCode());

        assertEquals(target2.hashCode(), proxy1.hashCode());
        assertEquals(target2.hashCode(), proxy2.hashCode());

        assertEquals(proxy1.hashCode(), target1.hashCode());
        assertEquals(proxy1.hashCode(), target2.hashCode());

        assertEquals(proxy1.hashCode(), proxy1.hashCode());
        assertEquals(proxy1.hashCode(), proxy2.hashCode());

        assertEquals(proxy2.hashCode(), target1.hashCode());
        assertEquals(proxy2.hashCode(), target2.hashCode());

        assertEquals(proxy2.hashCode(), proxy1.hashCode());
        assertEquals(proxy2.hashCode(), proxy2.hashCode());
    }

    @SuppressWarnings({"SimplifiableAssertion", "EqualsWithItself"})
    @Test
    public void testInequality()
    throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Target target1 = new Target(1);
        Target target2 = new Target(2);

        Target proxy1 = createProxy(target1);
        Target proxy2 = createProxy(target2);

        assertTrue(target1.equals(proxy1));
        assertFalse(target1.equals(proxy2));

        assertFalse(target2.equals(proxy1));
        assertTrue(target2.equals(proxy2));

        assertTrue(proxy1.equals(target1));
        assertFalse(proxy1.equals(target2));

        assertTrue(proxy1.equals(proxy1));
        assertFalse(proxy1.equals(proxy2));

        assertFalse(proxy2.equals(target1));
        assertTrue(proxy2.equals(target2));

        assertFalse(proxy2.equals(proxy1));
        assertTrue(proxy2.equals(proxy2));

        assertEquals(target1.hashCode(), proxy1.hashCode());
        assertNotEquals(target1.hashCode(), proxy2.hashCode());

        assertNotEquals(target2.hashCode(), proxy1.hashCode());
        assertEquals(target2.hashCode(), proxy2.hashCode());

        assertEquals(proxy1.hashCode(), target1.hashCode());
        assertNotEquals(proxy1.hashCode(), target2.hashCode());

        assertEquals(proxy1.hashCode(), proxy1.hashCode());
        assertNotEquals(proxy1.hashCode(), proxy2.hashCode());

        assertNotEquals(proxy2.hashCode(), target1.hashCode());
        assertEquals(proxy2.hashCode(), target2.hashCode());

        assertNotEquals(proxy2.hashCode(), proxy1.hashCode());
        assertEquals(proxy2.hashCode(), proxy2.hashCode());
    }

}
