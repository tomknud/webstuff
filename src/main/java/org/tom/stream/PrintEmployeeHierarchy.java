package org.tom.stream;

import java.util.*;
import java.util.concurrent.*;
import static org.tom.stream.Letters.*;

public class PrintEmployeeHierarchy {
	private Map<Integer,List<EmployeeNode>> employeesByManager = new HashMap<>();
	private Map<String,Employee> organizationHierarchy = new TreeMap<>();
	public static void main(String[] args) {
		PrintEmployeeHierarchy run = new PrintEmployeeHierarchy();
		run.setUpExample();;
		run.run();
	}
	private void run() {
		organizationHierarchy.put(letters[0], employeesByManager.get(null).get(0).setManagerIdSS(letters[0]));
		processNode(employeesByManager.get(null).get(0));
		employeesByManager.get(null).get(0).invoke();
		System.out.println(organizationHierarchy.toString());
	}
	private void processNode(EmployeeNode manager) {
		manager.printInfo();
		List<EmployeeNode> employees = employeesByManager.get(manager.getId());
		if(employees == null) {
			System.out.println(" Reports : NONE");
		} else {
			System.out.println(" Reports : ");
			Letters count = new Letters();
			employees.stream().peek(employee->{employee.setIndent(manager.getIndent()+1); employee.setManagerIdSS(manager.getManagerIdSS()+count.nextLetter()); organizationHierarchy.put(employee.getManagerIdSS(), employee);} ).forEach(employee->processNode(employee));
		}
	}
	private void setUpExample() {
		employeesByManager.put(null, new ArrayList<EmployeeNode>() {
		private static final long serialVersionUID = 1L;
		{add(new EmployeeNode(employeesByManager,1,null,"The Big Cheese"));}});
		List<EmployeeNode> ones = new ArrayList<>();
		ones.add(new EmployeeNode(employeesByManager,2,1,"The Dude"));
		ones.add(new EmployeeNode(employeesByManager,3,1,"Over Achiever"));
		employeesByManager.put(1, ones);
		List<EmployeeNode> twos = new ArrayList<>();
		twos.add(new EmployeeNode(employeesByManager,4,2,"Under Dude I"));
		List<EmployeeNode> twosaaas = new ArrayList<>();
		twosaaas.add(new EmployeeNode(employeesByManager,8,4,"Below Dude I a"));
		employeesByManager.put(4, twosaaas);
		twos.add(new EmployeeNode(employeesByManager,5,2,"Under Dude II"));
		
		List<EmployeeNode> twosbbbs = new ArrayList<>();
		twosbbbs.add(new EmployeeNode(employeesByManager,9,5,"Below Dude II b"));
		employeesByManager.put(5, twosbbbs);
		
		employeesByManager.put(2, twos);
		List<EmployeeNode> three = new ArrayList<>();
		three.add(new EmployeeNode(employeesByManager,6,3,"Under Achiever I"));
		three.add(new EmployeeNode(employeesByManager,7,3,"Under Achiever II"));
		employeesByManager.put(3, three);
	}
}
abstract class Employee extends RecursiveAction {
	private static final long serialVersionUID = 1L;
	private Integer id;
	private Integer managerId;
	private String name;
	public Employee(Integer id, Integer managerId, String name) {
		super();
		this.id = id;
		this.managerId = managerId;
		this.name = name;
	}
	public Integer getId() {
		return id;
	}
	public Integer getManagerId() {
		return managerId;
	}
	public String getName() {
		return name;
	}
}
class EmployeeNode extends Employee {
	private static final long serialVersionUID = 1L;
	private Map<Integer,List<EmployeeNode>> map = new HashMap<>();
	private Integer indent = 0;
	private String managerIdSS;// Unique String ID naturally ordering this employee in the company hierarchy
	public EmployeeNode(Map<Integer, List<EmployeeNode>> map, Integer id, Integer managerId, String name) {
		super(id,managerId,name);
		this.map = map;
	}
	public String getManagerIdSS() {
		return managerIdSS;
	}
	public EmployeeNode setManagerIdSS(String managerIdSS) {
		this.managerIdSS = managerIdSS;
		return this;
	}
	public Integer getIndent() {
		return indent;
	}
	public void setIndent(Integer indent) {
		this.indent = indent;
	}
	public void printInfo() {
		for(int i = 0;i<getIndent();i++) {
			System.out.print("  ");
		}
		System.out.print("Id:"+getId() + " Name: " + getName());
	}
	@Override
	protected void compute() {
		printInfo();
		List<EmployeeNode> employees = map.get(getId());
		if(employees == null) {
			System.out.println(" Reports : NONE");
		} else {
			System.out.println(" Reports : ");
			// Can't use invokeAll as it will execute out of order
			employees.forEach(employee->employee.invoke());
		}
	}
}
class Letters {
	public static String letters[] = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z",
	                                  "a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
	private int count = 0;
	private int incCount() {
		return this.count++;
	}
	public String nextLetter() {
		return letters[incCount()];
	}
}