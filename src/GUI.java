/* 
 * GUI.java
 * 
 * Formal Languages and Automata Theory - Spring 2018
 * Regular Expression Engine
 * 
 * Metin Suloglu
 * Dario Pejic
 * Liana Nassanova
 * 
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

public class GUI {
	
	public static final Color highlightColor1 = Color.CYAN;
	public static final Color highlightColor2 = Color.ORANGE;
	
	private JFrame frame;
	private JPanel topPanel;
	private JPanel inputPanel;
	private JPanel bottomPanel;
	private JLabel label;
	private JLabel detailLabel;
	private JTextField textField;
	private JButton searchButton;
	private JTextArea textArea;
	private JScrollPane scrollPane;
	private JButton openButton;
	
	public void displayGUI() { // function to display the GUI components
		
		/* Set Up: JFrame */
		frame = new JFrame();
		frame.setTitle("Regular Expression Engine");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setSize(600, 600);
		frame.setLayout(new BorderLayout());
		frame.setMinimumSize(new Dimension(400,300));
		
		/* Set Up: Top Panel (Includes - inputPanel, detailLabel) */
		topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.setBackground(Color.WHITE);
		topPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		
		/* Set Up: Input Panel (Includes - 'Regular Expression:' Label, Input Text Field, 'Search Text' Button) */
		inputPanel = new JPanel();
		inputPanel.setLayout(new BorderLayout());
		inputPanel.setBackground(Color.WHITE);
		inputPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		/* Set Up: 'Regular Expression:' Label */
		label = new JLabel("Regular Expression: ");
		label.setFont(new Font("Arial", Font.PLAIN, 16));
		label.setForeground(new Color(255, 80, 0));
		
		/* Set Up: JTextField */
		textField = new JTextField();
		textField.setEditable(true);
		textField.setFont(new Font("Arial", Font.PLAIN, 20));
		textField.addActionListener(new TextFieldAction());
		
		/* Set Up: 'Search Text' Button */
		searchButton = new JButton("Search text");
		searchButton.setMargin(new Insets(5, 5, 5, 5));
		searchButton.setFont(new Font("Arial", Font.PLAIN, 15));
		searchButton.addActionListener(new TextFieldAction());
		
		/* Set Up: detailLabel (Displays number of patterns matched and errors if any) */
		detailLabel = new JLabel(" ");
		detailLabel.setBackground(new Color(249, 249, 249));
		detailLabel.setOpaque(true);
		detailLabel.setFont(new Font("Arial", Font.PLAIN, 15));
		detailLabel.setBorder(new EmptyBorder(3, 3, 3, 3));
		
		/* Add Components To Panels */
		inputPanel.add(label, BorderLayout.WEST);
		inputPanel.add(textField, BorderLayout.CENTER);
		inputPanel.add(searchButton, BorderLayout.EAST);
		topPanel.add(inputPanel, BorderLayout.NORTH);
		topPanel.add(detailLabel, BorderLayout.SOUTH);
		
		/* Set Up: JTextArea (Text input) */
		textArea = new JTextArea();
		textArea.setEditable(true);
		textArea.setFont(new Font("Arial", Font.PLAIN, 18));
		textArea.setBorder(new EmptyBorder(10,10,10,10));
		
		/* Set Up: JScrollPane (So it is possible to scroll text area) */
		scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		/* Set Up: Bottom Panel (Includes - 'Open a text file' button) */
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.setBackground(Color.WHITE);
		
		/* Set Up: 'Open a text file' Button */
		openButton = new JButton("Open a text file");
		openButton.setMargin(new Insets(5, 5, 5, 5));
		openButton.setFont(new Font("Arial", Font.PLAIN, 13));
		openButton.addActionListener(new OpenAction());
		
		/* Add Component To Panel */
		bottomPanel.add(openButton, BorderLayout.CENTER);
		
		/* Add Panels To Frame */
		frame.add(topPanel, BorderLayout.NORTH);
		frame.add(scrollPane, BorderLayout.CENTER);
		frame.add(bottomPanel, BorderLayout.SOUTH);

		frame.setVisible(true);
	}

	class TextFieldAction implements ActionListener { // An instance of this class is a listener for the text field and the 'Search text' button
		
		@Override
		public void actionPerformed(ActionEvent e) { // Search text area for matching regular expression
			boolean currentHighlight = false; // false => highlightColor1; true => highlightColor2
			Highlighter highlighter = textArea.getHighlighter(); // Get the highlighter of the text area
			HighlightPainter painter;
			
			try{
				InfixToPostfixConverter ifpf = new InfixToPostfixConverter(textField.getText()); // Convert the regular expression to postfix form
				NFA nfa = NFA.getNFAfromPostfix(ifpf.getPosfixExpression()); // Get the NFA for that postfix expression
				String text = textArea.getText(); // Set the variable 'text' to the current text in the text area
				highlighter.removeAllHighlights(); // Clear all highlights before we start highlighting again
				int count = 0; // Used to count number of pattern matches
				int skip = 0; // Used to skip over lines
				int lineCount = 0; // Used to keep track of which line we are on
				
				/* For each line of the text, start by trying to match until the end of the line (i=0, j=line.length()), if there is no match
				 * try again with one character less (i=0, j=line.length()-1) and so on. Do this for all i from 0 to line.length()-1. If we find a match,
				 * highlight it and carry on searching the line with this procedure by setting i = j */
				for(String line: text.split("\r\n|\r|\n")) {
					for(int i=0;i<line.length(); i++) {
						for(int j=line.length(); j>=i+1; j--) {
							if(nfa.accepts(line.substring(i, j))) {
								try {
									painter = new DefaultHighlighter.DefaultHighlightPainter(currentHighlight ? highlightColor1 : highlightColor2);
									highlighter.addHighlight(skip + i, skip + j, painter); // 'Highlighter' takes two indexes and a painter as input
									count++;
									i = j-1; // i = j-1 since the for loop increases i again
									currentHighlight = !currentHighlight; // Switch highlight color
									break;
								} catch (BadLocationException e2) { // 'Highlighter' may throw an error
									e2.printStackTrace();
								}
							}
						}
					}
					skip = textArea.getLineEndOffset(lineCount);
					lineCount++;
				}
				
				detailLabel.setText(count + ((count == 1) ? " match" : " matches") + " found"); // Display number of patterns matched
			} catch (Exception e1) { // 'InfixToPostfixConverter' may throw an error
				detailLabel.setText("Invalid regular expression!"); // If there is an error, display an error message
				highlighter.removeAllHighlights();
			}
		}
	}
	
	class OpenAction implements ActionListener { // An instance of this class is a listener for the 'Open a text file' button
		@Override
		public void actionPerformed(ActionEvent e) { // Open a JFileChooser and load selected text file contents into the text area
			JFileChooser chooser = new JFileChooser();
			if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				textArea.setText("");
				try {
					BufferedReader br = new BufferedReader(new FileReader(file));
					textArea.read(br, null);
					br.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			
		}
	}

}
