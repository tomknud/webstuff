package org.tom.interfaces;

import static org.tom.interfaces.MyInterface.*;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public interface MyInterface {
	double CONVERSION = 125;
	static double getSpeedMPH() {
		return CONVERSION;
	}
	default double getSpeedKMH() {
		return (getSpeedMPH()/CONVERSION);
	}
	static boolean tryLock(Lock lock,int time) {
		boolean result = false;
		try {
			result = lock.tryLock(time, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return result;
	}
}
class NotherThing {
	private static double conversion;
	Lock lock = new ReentrantLock();
	static
	{
		conversion = MyInterface.CONVERSION;
	}
	public <T extends NotherThing> T wholeNother(T input) {
		tryLock(lock,5);
		return input;
	}
	public double getConversion() {
		return conversion;
	}
	public void setConversion(int conversion) {
		NotherThing.conversion = conversion;
	}
}
