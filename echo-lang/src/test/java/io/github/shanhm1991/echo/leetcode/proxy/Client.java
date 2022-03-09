package io.github.shanhm1991.echo.leetcode.proxy;

import net.sf.cglib.core.DebuggingClassWriter;


public class Client {
	public static void main(String[] args) {
		//保存动态生成的代理类的class
		System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "D:\\class");

		Handler handler = new HandlerImpl();
		Handler proxy = CglibProxy.createHandlerProxy(handler);

		proxy.query("id1");
		proxy.delete("id2");
	}
}





//	Handler handler = new HandlerImpl();
//	
//	Enhancer enhancer = new Enhancer();
//	enhancer.setSuperclass(handler.getClass());
//	enhancer.setCallback(new InvocationHandler() {
//		@Override
//		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//			System.out.println("before");
//			Object resut = method.invoke(handler, args);
//			System.out.println("after");
//			return resut;
//		}
//	});
//	
//	Handler proxy = (Handler)enhancer.create();
//	proxy.query("id1");
//	proxy.delete("id2");
//}

//Handler handler = new HandlerImpl();
//StaticProxy staticProxy = new StaticProxy(handler);
//staticProxy.query("id1");
//staticProxy.delete("id2");
