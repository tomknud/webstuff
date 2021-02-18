package org.tom.stream;

import java.util.*;
import java.util.function.*;
import static java.util.stream.StreamSupport.*;

public class GoldenStreams {
	private static final String IDENTITY = "";

	public static void main(String[] args) {
		List<String> peeks = new ArrayList<String>();
		List<String> reds = new ArrayList<String>();
		stream(new Spliterator<String>() {
			private static final int LIMIT = 25;
			private int integer = Integer.MAX_VALUE;
			{
				integer = 0;
			}
			@Override
			public int characteristics() {
				return Spliterator.DISTINCT;
			}
			@Override
			public long estimateSize() {
				return LIMIT-integer;
			}
			@Override
			public boolean tryAdvance(Consumer<? super String> arg0) {
				arg0.accept(IDENTITY+integer++);
				return integer < LIMIT;
			}
			@Override
			public Spliterator<String> trySplit() {
				return null;
			}}, false).peek(data->{
			peeks.add(data);
		}).filter(data-> {
			return Integer.parseInt(data)%2>0;
		}).peek(data ->{
			System.out.println("peekDeux:"+data);
		}).reduce(IDENTITY,(accumulation,input)->{
			reds.add(input);
			String concat = accumulation + ( accumulation.isEmpty() ? IDENTITY : ":") + input;
			System.out.println("reduce:"+concat);
			return concat;
		});
		System.out.println("Peeks:"+peeks.toString());
		System.out.println("Reduction:"+reds.toString());
	}
}
