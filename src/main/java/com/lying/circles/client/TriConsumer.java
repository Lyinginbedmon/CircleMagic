package com.lying.circles.client;

@FunctionalInterface
public interface TriConsumer<T, U, M>
{
	void accept(T t, U u, M m);
}
