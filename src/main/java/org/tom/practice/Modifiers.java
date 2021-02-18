package org.tom.practice;

import java.sql.*;
import java.util.*;

public class Modifiers {
	public static void main(String ...strings) {
		try {
			new TreeMap<String,String>().put(null,"thing");
			new TreeSet<String>().add(null);
		} catch(NullPointerException npe) {
			
		} finally {
			
		}
	}
	public Modifiers() {
		
	}
	public class Inner<T extends Map<String,String>> implements Comparable<T> {
		private String name;
		private T thing;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public T getThing() {
			return thing;
		}
		public void setThing(T thing) {
			this.thing = thing;
		}
		@Override
		public int compareTo(T arg0) {
			// TODO Auto-generated method stub
			return 0;
		}
	}
	public static class Static {
		private String name;
		private static final String thing;
		static {
			thing = "";
			Connection connection = null;
			try {
				connection = java.sql.DriverManager.getConnection("asdf", "fdsa", "dbcooper");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		public String getName() {
			if(name == null) {
				return thing;
			} else {
				return name;
				
			}
		}

		public void setName(String name) {
			this.name = name;
		}
		
	}
}
