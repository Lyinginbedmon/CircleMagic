package com.lying.misc19.client;

@FunctionalInterface
public interface TriConsumer<T, U, M>
{
	void accept(T t, U u, M m);
}
