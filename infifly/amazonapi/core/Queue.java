package infifly.amazonapi.core;


public interface Queue {
	public void push(Request request);
	public Request poll();
}
