package io.github.shanhm1991.echo.leetcode.proxy;

//Proxy
public class StaticProxy implements Handler {
	
	private Handler target;
	
	public StaticProxy(Handler target){
		this.target = target;
	}

	@Override
	public void query(String id) {
		System.out.println("before query");
		target.query(id);
		System.out.println("after query");
	}

	@Override
	public void delete(String id) {
		System.out.println("before delete");
		target.delete(id);
		System.out.println("after delete");
	}
}
