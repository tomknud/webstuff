package org.tom.practice;

import java.util.*;
import java.util.function.*;

public class Practice implements Comparable<Practice> {
	private String thing;
	@Override
	public int compareTo(Practice thingy) {
		return thing.compareTo(thingy.getThing());
	}
	public String getThing() {
		return thing;
	}
	public void setThing(String thing) {
		this.thing = thing;
	}
	public static void main(String ... things) {
		SortedMap<String,String> sortMap = new TreeMap<>();
		Map<String,String> hashMap = new HashMap<>();
		SortedSet<String> treeSet = new TreeSet<>();
		String[] strings = {"", null, "supercalifragilistic"};
		BiConsumer<String,String> consumer = hashMap::put;
		Consumer<String> mConsumer = treeSet::add;
		for(String string: strings) {
			tryMapString(consumer.andThen(sortMap::put), string);
			trySetString(mConsumer, string);
		}
		hashMap.keySet().stream().forEach(arg0->System.out.println("hashMap.k:"+(arg0==null?"GOT A NULL":arg0)+" v:"+hashMap.get(arg0)));
		sortMap.keySet().stream().forEach(arg0->System.out.println("sortMap.k:"+(arg0==null?"GOT A NULL":arg0)+" v:"+sortMap.get(arg0)));
		treeSet.stream().forEach(arg0->System.out.println("sortSet.k:"+(arg0==null?"GOT A NULL":arg0)+" v:"+arg0));
		MyPractice aa = new MyPractice();
		aa.setThing("amajig");
		aa.setThingy("thingy");
		MyPractice bb = new MyPractice();
		bb.setThing("amajig");
		bb.setThingy("thingy");
		System.out.println("a to b:"+ aa.compareTo(bb));
		System.out.println("a to b:"+ aa.equals(bb));
	}
	private static void tryMapString(BiConsumer<String,String> consumer, String string) {
		try {
			consumer.accept(string, string+"thing");
		} catch(Exception e) {
			System.err.println("Error:"+string+" message:"+e.getLocalizedMessage());
		}
	}
	private static void trySetString(Consumer<String> consumer, String string) {
		try {
			consumer.accept(string);
		} catch(Exception e) {
			System.err.println("Error:"+string+" message:"+e.getLocalizedMessage());
		}
	}
}
class MyPractice extends Practice {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((thing == null) ? 0 : thing.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object input) {
		if (this == input)
			return true;
		if (input == null)
			return false;
		if (getClass() != input.getClass())
			return false;
		MyPractice castInput = (MyPractice) input;
		if (thing == null) {
			if (castInput.thing != null)
				return false;
		} else if (!thing.equals(castInput.thing)) {
			return false;
		} else if(!getThingy().equals(castInput.getThingy())) {
			return false;
		}
		return true;
	}
	private String thing;
	@Override
	public int compareTo(Practice amajiggy) {
		return super.compareTo(amajiggy);
	}
	@Override
	public String getThing() {
		return thing;
	}
	@Override
	public void setThing(String thing) {
		this.thing = thing;
	}
	public String getThingy() {
		return super.getThing();
	}
	public void setThingy(String thing) {
		super.setThing(thing);
	}

}