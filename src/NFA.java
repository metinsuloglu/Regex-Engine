/* 
 * NFA.java
 * 
 * Formal Languages and Automata Theory - Spring 2018
 * Regular Expression Engine
 * 
 * Metin Suloglu
 * Dario Pejic
 * Liana Nassanova
 * 
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

public class NFA {
	
	private static int stateIDsource = 0;

	private int initialState;
	private HashSet<Integer> allStates;
	private HashSet<Integer> acceptStates;
	private HashSet<Edge> transitions;
	
	private HashSet<Integer> currentStates;
	
	private NFA(int initialState, HashSet<Integer> allStates, HashSet<Integer> acceptStates, HashSet<Edge> transitions) {
		this.initialState = initialState;
		this.allStates = new HashSet<Integer>(allStates);
		this.acceptStates = new HashSet<Integer>(acceptStates);
		this.transitions = new HashSet<Edge>(transitions);
	}
	
	public String toString() {
		String stringRepresentation = "Starting state : " + initialState;
		stringRepresentation += "\nAccept states : " + acceptStates;
		stringRepresentation += "\nAll states : " + allStates + "\nTransitions : \n";
		for (Edge edge : transitions) {
			stringRepresentation += edge + "\n";
		}
		return stringRepresentation;
	}
	
	public HashSet<Integer> epsilonClosure(int state) {
		/* Finds the set of states that are in epsilon closure of the given states within
		 * this automaton. (set of states that can be reached from all states given using
		 * epsilon transitions) */
    
		HashSet<Integer> epsilonClosure = new HashSet<Integer>(); // Initialise epsilonClosure as empty
		Queue<Integer> q = new LinkedList<Integer>(); // Queue for breadth-first search on the NFA
		q.add(state);
		
        /* Breadth-First Search on the NFA */
		while(!q.isEmpty()) {
			int curr = q.remove();
			epsilonClosure.add(curr); // Include the current node in the epsilon closure
			
			/* Add the neighbours of the current node reachable by epsilon transitions to the queue */
			for(Edge transition: transitions) {
				if(transition.getSourceState() == curr && transition.isEpsilonTransition() && !epsilonClosure.contains(transition.getDestinationState())) {
					q.add(transition.getDestinationState());
				}
			}
		}
		
		return epsilonClosure;
	}
	
	public boolean accepts(String s) {
		// Checks whether this automaton accepts the string 's'.
		
		this.currentStates = epsilonClosure(this.initialState); // Start out with the epsilon closure of the starting node
		HashSet<Integer> nextStates = new HashSet<Integer>();
		
		for(int i=0; i<s.length(); i++) { // Loop through each character of the string
			for(int state: this.currentStates) { // For each of the states we are in
                /* Calculate the next states */
				for(Edge transition: this.transitions) {
					if(transition.getSourceState() == state && transition.getSymbol() == s.charAt(i) && !nextStates.contains(transition.getDestinationState())) {
						nextStates.addAll(epsilonClosure(transition.getDestinationState()));
					}
				}
			}
			currentStates = new HashSet<Integer>(nextStates);
			nextStates.clear();
		}
		
		/* At the end, if any of the states the NFA is currently in is accepting accept the string 's'. Otherwise don't */
		for(int state: currentStates) {
			if(this.acceptStates.contains(state)) return true;
		}
		
		return false;
		
	}

	public boolean acceptsLine(String line) {
		/* Checks whether this automaton accepts any part of line.
		 * Implemented using Algorithm 2 in the project document */
		
		this.currentStates = new HashSet<Integer>();
		HashSet<Integer> nextStates = new HashSet<Integer>();
		
		int i = -1; // Index of current character in the line
		
		while(true) {
			currentStates.add(initialState);
			
			nextStates = new HashSet<Integer>(currentStates); // nextStates = currentStates
			
			/* Add the epsilon closure of all states in nextStates to currentStates */ 
			for(int state: nextStates)
				currentStates.addAll(epsilonClosure(state));
			
			/* Return true if any of the currentStates are accepting */
			for(int acceptState: acceptStates)
				if(currentStates.contains(acceptState)) return true;
			
			if(i == line.length()-1) return false; // If end of line return false
			i++; // Next character
			
			nextStates.clear();
			for(int state: this.currentStates) { // For each of the states we are in
                /* Calculate the next states */
				for(Edge transition: this.transitions) {
					if(transition.getSourceState() == state && transition.getSymbol() == line.charAt(i) && !nextStates.contains(transition.getDestinationState())) {
						nextStates.add(transition.getDestinationState());
					}
				}
			}
			
			currentStates = new HashSet<Integer>(nextStates); // currentStates = nextStates
				
		}
		
	}
	
	private static int newState() {
		/* Every new state must have distinct id, so increment the id generator
		 * each time you need a new one */
		return stateIDsource++;
	}
	
	public static NFA epsilonNFA() { // Returns an NFA which only accepts epsilon
		
		// Single initial state
		int initialState = newState();
		
		// Single state
		HashSet<Integer> statesSet = new HashSet<Integer>();
		statesSet.add(initialState);

		// Initial state is the accept state
		HashSet<Integer> acceptStates = new HashSet<Integer>();
		acceptStates.add(initialState);
		
		// Create an NFA with such properties
		return new NFA(initialState, statesSet, acceptStates, new HashSet<Edge>());
		
	}
	
	public static NFA singleSymbol(char symbol) {
		
		int initialState = newState();
		int finalState = newState();
		
		// Include the two states
		HashSet<Integer> statesSet = new HashSet<Integer>();
		statesSet.add(initialState);
		statesSet.add(finalState);

		// Single accepting state (finalState)
		HashSet<Integer> acceptStates = new HashSet<Integer>();
		acceptStates.add(finalState);
		
		/* There is a single transition in this automaton, which takes the initial state
		 * to the final state if the input is the same as the 'symbol' variable */
		Edge edge = new Edge(initialState, finalState, symbol);
		HashSet<Edge> transitions = new HashSet<Edge>();
		transitions.add(edge);
		
		// Create an NFA with such properties
		return new NFA(initialState, statesSet, acceptStates, transitions);
	}
	
	public static NFA union(NFA nfa1, NFA nfa2) {
        
		int initialState = newState();
		
		// The new states are all states from both NFAs and a new initial state
		HashSet<Integer> statesSet = new HashSet<Integer>();
		statesSet.addAll(nfa1.allStates);
		statesSet.addAll(nfa2.allStates);
		statesSet.add(initialState);

		// No new accepting states, same as both the NFAs
		HashSet<Integer> acceptStates = new HashSet<Integer>();
		acceptStates.addAll(nfa1.acceptStates);
		acceptStates.addAll(nfa2.acceptStates);
		
		/* All transitions from the two NFAs and two additional epsilon transitions from
		 * the new initial node to the initial states of the two NFAs */
		HashSet<Edge> transitions = new HashSet<Edge>();
		transitions.add(Edge.epsilonTransition(initialState, nfa1.initialState));
		transitions.add(Edge.epsilonTransition(initialState, nfa2.initialState));
		transitions.addAll(nfa1.transitions);
		transitions.addAll(nfa2.transitions);
		
		// Create an NFA with such properties
		return new NFA(initialState, statesSet, acceptStates, transitions);
	}
	
	public static NFA concatenate(NFA nfa1, NFA nfa2) {
		
		// No new states
		HashSet<Integer> statesSet = new HashSet<Integer>();
		statesSet.addAll(nfa1.allStates);
		statesSet.addAll(nfa2.allStates);
		
		/* Include all transitions from both NFAs and additional epsilon transitions
		 * from the accepting states of nfa1 to the initial state of nfa2 */
		HashSet<Edge> transitions = new HashSet<Edge>();
		transitions.addAll(nfa1.transitions);
		transitions.addAll(nfa2.transitions);
		for(int acceptState: nfa1.acceptStates) {
			transitions.add(Edge.epsilonTransition(acceptState, nfa2.initialState));
		}
		
		// Create an NFA with such properties
		return new NFA(nfa1.initialState, statesSet, nfa2.acceptStates, transitions);
	}
	
	public static NFA star(NFA nfa) {
		
		int initialState = newState();
		
		// The new states are all states from the NFA and a new initial state
		HashSet<Integer> statesSet = new HashSet<Integer>();
		statesSet.addAll(nfa.allStates);
		statesSet.add(initialState);

		// The accepting states are all accepting states from the NFA and the initial state
		HashSet<Integer> acceptStates = new HashSet<Integer>();
		acceptStates.addAll(nfa.acceptStates);
		acceptStates.add(initialState);
		
		/* Include all transitions from the NFA and additional epsilon transitions
		 * from the accepting states of the NFA to the initial state of the same NFA 
		 * and the newly created initial state to the original initial state of the NFA */
		HashSet<Edge> transitions = new HashSet<Edge>();
		transitions.addAll(nfa.transitions);
		for(int acceptState: nfa.acceptStates) {
			transitions.add(Edge.epsilonTransition(acceptState, nfa.initialState));
		}
		transitions.add(Edge.epsilonTransition(initialState,nfa.initialState));
		
		// Create an NFA with such properties
		return new NFA(initialState, statesSet, acceptStates, transitions);
	}
	
	// Takes the file and a postfix expression as input and prints the accepted lines
	public void acceptedLines(String fileName) {
		
		File file = new File(fileName + ".txt");
		int lineNumber = 0; // Counts the number of lines read from the file
		boolean found = false; // If any line is accepted this becomes true
		
		try {
			Scanner input = new Scanner(file);
			while(input.hasNextLine()) { // Loop until the end of the file
				String line = input.nextLine(); // Get the current line from the file
				
				if(this.acceptsLine(line)) { // Check whether our NFA accepts the current line
					System.out.println("ACCEPTED LINE (" + lineNumber + ") : " + line);
					found = true;
				}

				lineNumber++;
			}
			
			if(!found) {
				System.out.println("No matches found.");
			}
			input.close();
		} catch (FileNotFoundException e) { // Catch an exception if the file is not found and inform the user
			System.out.println(fileName + ".txt could not be found."); 
		}
	}
	
	public static NFA getNFAfromPostfix(String postfixExpression) { // Returns an NFA built from a postfix expression
		
		Stack<NFA> stack = new Stack<NFA>(); // Initialize a new stack
		NFA nfa1, nfa2;
		
		// Loop through each character of postfix expression
		for(int i=0; i<postfixExpression.length(); i++) {
			char c = postfixExpression.charAt(i); // Get current character
			
			if(c == '&') { 
				nfa2 = stack.pop(); // Get the last NFA object and remove it from stack
				nfa1 = stack.pop(); // Get the last NFA object and remove it from stack
				stack.push(concatenate(nfa1, nfa2)); // Build a new NFA by concatenating the previous two NFAs and push it onto the stack
			}
			else if(c == '|') {
				nfa2 = stack.pop(); // Get last NFA object and remove it from stack
				nfa1 = stack.pop();	// Get last NFA object and remove it from stack
				stack.push(union(nfa1, nfa2)); // Build a new NFA by making a union of the previous two NFAs and push it onto the stack
			}
			else if(c == '*') {
				nfa1 = stack.pop(); // Get last NFA object and remove it from stack
				stack.push(star(nfa1)); // Build a new NFA by taking the * (star) of the previous NFA and push it onto the stack
			}
			else if(c == '\u03B5') {
				stack.push(epsilonNFA()); // Push an NFA which only accepts the epsilon transition to the stack
			}
			else
				stack.push(singleSymbol(c)); // Build a new NFA that accepts a single symbol and push it onto the stack
		}
		
		return stack.peek(); // Return the built NFA
	}
	
	public static void test() { // Method to test the NFA class
		System.out.println("Testing...");
		System.out.println();
		System.out.println("Trying to construct automaton that recognizes: ");
		System.out.println("(a|b)*abb");
		
		NFA a = NFA.singleSymbol('a');
		NFA b = NFA.singleSymbol('b');
		NFA union = NFA.union(a, b);
		NFA star = NFA.star(union);
		
		NFA anotherA = NFA.singleSymbol('a');
		NFA anotherB = NFA.singleSymbol('b');
		NFA lastB = NFA.singleSymbol('b');
		
		NFA ab = NFA.concatenate(anotherA, anotherB);
		NFA abb = NFA.concatenate(ab, lastB);
		
		NFA result = NFA.concatenate(star, abb);
		
		String testString = "aababb";
		
		if (result.accepts(testString)) {
			System.out.println("NFA accepts " + testString);
		} else {
			throw new RuntimeException("NFA did not accept " + testString);
		}
		
		System.out.println("All tests passed!");
		System.out.println("Here is the resulting NFA: ");
		System.out.println(result);
	}

}
