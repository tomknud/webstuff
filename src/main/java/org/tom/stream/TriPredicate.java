package org.tom.stream;

@FunctionalInterface
public interface TriPredicate<T,U,V> {
	boolean test(T t,U u,V v);
}
interface TestEm extends TriPredicate<String,Boolean,Integer> {
	boolean test(String t,Boolean u,Integer v);
}
