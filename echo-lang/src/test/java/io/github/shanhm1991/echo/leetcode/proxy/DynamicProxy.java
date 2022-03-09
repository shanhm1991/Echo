package io.github.shanhm1991.echo.leetcode.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class DynamicProxy implements InvocationHandler {

	Object target;  

	public DynamicProxy(Object target){
		this.target = target;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		before(method, args);
		Object result = method.invoke(target, args);  
		after(method, args);
		return result;  
	}

	private void before(Method method, Object[] args) {
		System.out.println("before " + method.getName());
	}

	private void after(Method method, Object[] args) {
		System.out.println("after " + method.getName());
	}
	
	public static Handler createHandlerProxy(Handler handler){
		ClassLoader classLoader = handler.getClass().getClassLoader();
		Class<?>[] interfaces = handler.getClass().getInterfaces();
		
		DynamicProxy handlerProxy = new DynamicProxy(handler);
		return (Handler) Proxy.newProxyInstance(classLoader, interfaces, handlerProxy);
	}
}
