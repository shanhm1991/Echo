package io.github.shanhm1991.echo.leetcode.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class CglibProxy implements MethodInterceptor {

	private void before(Object object, Method method, Object[] objects) { 
		System.out.println("before " + method.getName());
	}

	private void after(Object object, Method method, Object[] objects) {
		System.out.println("after " + method.getName());
	}

	@Override
	public Object intercept(Object object, 
			Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
		before(object, method, args);
		// 注意这里是调用 invokeSuper 而不是 invoke，否则死循环
		//invokesuper执行目标类方法，invoke执行的是子类方法
		Object result = methodProxy.invokeSuper(object, args);   
		after(object, method, args);
		return result;
	}

	public static Handler createHandlerProxy(Object obj){
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(obj.getClass());
		enhancer.setCallback(new CglibProxy());
		return (Handler)enhancer.create();
	}
}
