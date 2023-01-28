# ProxyBuddy
ProxyBuddy is a simple factory for creating dynamic proxies for arbitrary Java classes and interfaces with ByteBuddy.

    var proxy = new ProxyBuddy<>(MyClass.class, (pipe, method, arguments) ->
        switch (method.getName()) {
            case "method1" -> 1;
            case "method2" -> 2;
            case "method3" -> 3;
            default -> 0;
        })
        .withInterface(Interface1.class)
        .withInterface(Interface2.class)
        .withInterface(Interface3.class)
        .createProxy();
