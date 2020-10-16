/* 
 * Main.java
 * 
 * Formal Languages and Automata Theory - Spring 2018
 * Regular Expression Engine
 * 
 * Metin Suloglu
 * Dario Pejic
 * Liana Nassanova
 * 
 */

import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		
		final boolean GUI = true; // Set to true to display the GUI.
		
		if(GUI) new GUI().displayGUI();
		
		String file;
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter the file to search (without the '.txt' extension): ");
		file = scanner.nextLine();
		
		String regex;
		System.out.println("Enter the regular expression: ");
		regex = scanner.nextLine();
			
		InfixToPostfixConverter ifpf = new InfixToPostfixConverter(regex); // convert regular expression to postfix form
		System.out.println("Regex in postfix form:\n---------------------\n" + ifpf.getPosfixExpression() + '\n');
		
		NFA nfa = NFA.getNFAfromPostfix(ifpf.getPosfixExpression()); // build NFA from postfix expression and assign it to NFA object
		System.out.println("The following NFA was built:\n---------------------------\n" + nfa);
		
		System.out.println("NFA simulation results: \n-----------------------");
		long startTime = System.nanoTime();
		nfa.acceptedLines(file); // This function adds the '.txt' extension
		long endTime = System.nanoTime();

		System.out.println("\nNFA simulation took: " + (endTime - startTime)/10e6 + " ms");
		
		scanner.close();

	}
	
}
