import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DrawingApplication extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	private PaintPanel paintPanel;           // panel on which to draw shapes
	private Container container;             // contains all buttons and text fields
	private TopContainer topContainer;       // the top half of the container
	private BottomContainer bottomContainer; // the bottom half of the container
	private JLabel mouseLocation;            // location of the mouse
	private JLabel shape;                    // labels pickShape
	private JLabel filled;                   // labels isFilled
	private JLabel gradient;                 // labels useGradient
	private JLabel width;                    // labels strokeWidth
	private JLabel length;                   // labels lengthWidth
	private JLabel dashed;                   // labels lineType
	private JButton undoButton;              // undo the last shape drawn
	private JButton clearButton;             // clear all shapes from the drawing
	private JButton colorOneButton;          // choose the first color in the gradient
	private JButton colorTwoButton;          // choose the second color in the gradient
	private JTextField strokeWidthField;     // specifies the stroke width
	private JTextField dashLengthField;      // specifies the stroke dash length
	private JCheckBox isFilledBox;           // specifies if shape is filled or unfilled
	private JCheckBox useGradientBox;        // specifies whether to paint using a gradient
	private JCheckBox isDashedBox;           // specifies whether to draw dashed or solid line
	private JComboBox<String> pickShape;     // specifies the shape to draw
	private Color colorOne;                  // the first color
	private Color colorTwo;                  // the second color
	private Point pointA;                    // the first point of the shape
	private Point pointB;                    // the second point of the shape
	private boolean isFilled;                // true if the shape is filled
	private boolean isDashed;                // true if the line is dashed
	private boolean useGradient;             // true if using a gradient
	private boolean isDragged;               // true if the shape is being dragged
	private int strokeWidth;                 // width of the stroke
	private int dashLength;                  // length of the stroke
	private int shapeChoice;                 // index of shapeNameArray
	private String[] shapeNameArray;         // array of shape names
	private ArrayList<Shape> shapeArray;     // array of shapes to be drawn
	private Shape shapeDrawn;                // the current shape being drawn
	
	
	
	public DrawingApplication()
	{	
		// set the title bar
		super("Java 2D Drawing Application");
		
		// set frame layout
		setLayout( new BorderLayout());
		
		// create shapeArray to hold shapes to be drawn
		shapeArray = new ArrayList<Shape>();
		
		// create components to be placed in DrawingApplication
		container = new Container();
		paintPanel = new PaintPanel();
		mouseLocation = new JLabel();
		
		// set location of the components in DrawingApplication
		add(container,BorderLayout.NORTH);
		add(paintPanel,BorderLayout.CENTER);
		add(mouseLocation,BorderLayout.SOUTH);
	} // end DrawingApplication constructor
	
/******************************************************************************************
***********************************PAINTPANEL**********************************************
******************************************************************************************/	
	public class PaintPanel extends JPanel 
	{		
		private static final long serialVersionUID = 1L;

		public PaintPanel()
		{	
			// initialize shape attributes
			pointA = new Point(0,0);
			pointB = new Point(0,0);
			colorOne = Color.BLACK;
			colorTwo = Color.GRAY;
			useGradient = false;
			isFilled = false;
			isDashed = false;
			strokeWidth = 0;
			dashLength = 0;
			shapeChoice = 0;
			isDragged = false;
			
			// create event handlers for PaintPanel
			MouseHandler mouseHandler = new MouseHandler();
			ComboBoxHandler comboBoxHandler = new ComboBoxHandler();
			CheckBoxHandler checkBoxHandler = new CheckBoxHandler();
			ColorOneButtonHandler colorOneButtonHandler = new ColorOneButtonHandler();
			ColorTwoButtonHandler colorTwoButtonHandler = new ColorTwoButtonHandler();
			UndoButtonHandler undoButtonHandler = new UndoButtonHandler();
			ClearButtonHandler clearButtonHandler = new ClearButtonHandler();
			
			// add event handlers to corresponding JComponents
			addMouseListener(mouseHandler);
			addMouseMotionListener(mouseHandler);
			pickShape.addItemListener(comboBoxHandler);
			isFilledBox.addItemListener(checkBoxHandler);
			isDashedBox.addItemListener(checkBoxHandler);
			useGradientBox.addItemListener(checkBoxHandler);
			colorOneButton.addActionListener(colorOneButtonHandler);
			colorTwoButton.addActionListener(colorTwoButtonHandler);
			undoButton.addActionListener(undoButtonHandler);
			clearButton.addActionListener(clearButtonHandler);	
		} // end PaintPanel constructor
		
		/*MouseHandler handles mouse events on the PaintPanel*/
		private class MouseHandler implements MouseListener, MouseMotionListener
		{
			private boolean mouseEntered; // true if mouse is in the PaintPanel
			
			public void mousePressed(MouseEvent event)
			{	
				pointA = event.getPoint();
				
				if (strokeWidthField.getText().equals(""))
					strokeWidth = 0;
				else
					strokeWidth = Integer.parseInt(strokeWidthField.getText());
				
				if (isDashedBox.isSelected() && !dashLengthField.getText().equals(""))
				{	
					isDashed = true;
					dashLength = Integer.parseInt(dashLengthField.getText());
				}
				else
					isDashed = false;
					
			} // end MousePressed
			
			public void mouseClicked(MouseEvent event)
			{
				
			} // end MouseClicked
			
			@Override
			public void mouseReleased(MouseEvent event)
			{
				pointB = event.getPoint();
				isDragged = false;
				createShape();
				repaint();
			} // end MouseReleased
			
			public void mouseEntered(MouseEvent event)
			{
				mouseEntered = true;
			} // end MouseEntered
			
			public void mouseExited(MouseEvent event)
			{
				mouseEntered = false;
			} // end MouseExited
			
			public void mouseMoved( MouseEvent event)
			{
				if (mouseEntered)
					mouseLocation.setText( String.format("[%d,%d]", event.getX(), event.getY()));		
			} // end MouseMoved
			
			public void mouseDragged( MouseEvent event )
			{	
				pointB = event.getPoint();
				isDragged = true;
				createShape();
				repaint();
				
				if (mouseEntered)
					mouseLocation.setText( String.format("[%d,%d]", event.getX(), event.getY()));
			} // end MouseDragged
		} // end MouseHandler
		
		@Override
		public void paintComponent (Graphics g)
		{
			super.paintComponent(g);
			
			Graphics2D g2d = (Graphics2D) g;
			
			this.setBackground(Color.WHITE);
			
			for (int i = 0; i < shapeArray.size(); i++)
			{
				shapeArray.get(i).draw(g2d);
			}
			if (isDragged)
				shapeDrawn.draw(g2d);
		} // end paintComponent
	} // end PaintPanel
	
/******************************************************************************************
*************************************CONTAINERS********************************************
******************************************************************************************/
	
	/*Container JPanel holds all buttons and text fields that edit the shape*/
	public class Container extends JPanel 
	{	
		private static final long serialVersionUID = 1L;

		public Container()
		{
			// set panel layout
			setLayout(new BorderLayout());
			
			// create top and bottom half of the container
			topContainer = new TopContainer();
			bottomContainer = new BottomContainer();
			
			// split the container into two separate JPanels
			add(topContainer,BorderLayout.NORTH);
			add(bottomContainer,BorderLayout.SOUTH);
		} // end Container constructor
	} // end Container
	
	public class TopContainer extends JPanel
	{
		private static final long serialVersionUID = 1L;
		
		public TopContainer()
		{
			// set panel layout
			setLayout( new FlowLayout());
			
			// create shape name array for JComboBox
			shapeNameArray = new String[] {"Line","Rectangle","Oval"};
			
			// create JComponents to be placed in top half of container panel
			shape = new JLabel("Shape:");
			filled = new JLabel("Filled");
			undoButton = new JButton("Undo");
			clearButton = new JButton("Clear");
			isFilledBox = new JCheckBox();
			pickShape = new JComboBox<String>(shapeNameArray);
			
			// add JComponents to topContainer
			add(undoButton);
			add(clearButton);
			add(shape);
			add(pickShape);
			add(isFilledBox);
			add(filled);
		} // end TopContainer constructor
	} // end TopContainer
	
	public class BottomContainer extends JPanel 
	{
		private static final long serialVersionUID = 1L;

		public BottomContainer()
		{
			// set panel layout
			setLayout( new FlowLayout());
			
			// create JComponents to be placed in bottom half of container panel
			gradient = new JLabel("Use Gradient");
			width = new JLabel("Line Width");
			length = new JLabel("Dash Length");
			dashed = new JLabel("Dashed");
			colorOneButton = new JButton("1st Color");
			colorTwoButton = new JButton("2nd Color");
			strokeWidthField = new JTextField(5);
			dashLengthField = new JTextField(5);
			useGradientBox = new JCheckBox();
			isDashedBox = new JCheckBox();
			
			// add JComponenets to bottomContainer
			add(useGradientBox);
			add(gradient);
			add(colorOneButton);
			add(colorTwoButton);
			add(width);
			add(strokeWidthField);
			add(length);
			add(dashLengthField);
			add(isDashedBox);
			add(dashed);
		} // end BottomContainer constructor
	} // end BottomContainer
	
/********************************************************************************************
***********************************EVENT HANDLERS********************************************
********************************************************************************************/
	private class ComboBoxHandler implements ItemListener
	{
		public void itemStateChanged( ItemEvent event)
		{
			if (event.getStateChange() == ItemEvent.SELECTED)
				switch (pickShape.getSelectedIndex())
				{
				case 0: shapeChoice = 0;;
						break;
				case 1: shapeChoice = 1;
						break;
				case 2: shapeChoice = 2;
						break;
				}
		} // end itemStateChanged
	} // end ComboBoxHandler
	
	private class CheckBoxHandler implements ItemListener
	{
		public void itemStateChanged( ItemEvent event)
		{
			if (isFilledBox.isSelected())
				isFilled = true;
			else
				isFilled = false;
			
			if (useGradientBox.isSelected())
				useGradient = true;
			else
				useGradient = false;
		} // end itemStateChanged
	} // end CheckBoxHandler
	
	private class ColorOneButtonHandler implements ActionListener
	{
		// display JColorChooser when user clicks button
		public void actionPerformed (ActionEvent event)
		{
			colorOne = JColorChooser.showDialog(DrawingApplication.this, "Choose a color",
													colorOne);
		} // end actionPerformed
	} // end ColorOneButtonHandler
	
	private class ColorTwoButtonHandler implements ActionListener
	{
		// display JColorChooser when user clicks button
		public void actionPerformed(ActionEvent event)
		{
			colorTwo = JColorChooser.showDialog(DrawingApplication.this, "Choose a color",
					colorTwo);
		} // end actionPerformed
	} // end ColorTwoButtonHandler
	
	private class UndoButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			if (shapeArray.size() > 0)
				shapeArray.remove(shapeArray.size() - 1);
			
			repaint();
		} // end actionPerformed
	} // end UndoButtonHandler
	
	private class ClearButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			shapeArray.clear();
			
			repaint();
		} // end actionPerformed
	} // end ClearButtonHandler
/******************************************************************************************
*******************************************************************************************
******************************************************************************************/
	
	public void createShape()
	{
		switch(shapeChoice)
		{
			case 0: shapeDrawn = new Line(pointA,pointB,colorOne,colorTwo,useGradient,
											isDashed,strokeWidth,dashLength);
					break;
			case 1: shapeDrawn = new Rectangle(pointA,pointB,colorOne,colorTwo,
												useGradient,isDashed,isFilled,
													strokeWidth,dashLength);
					break;
			case 2: shapeDrawn = new Oval(pointA,pointB,colorOne,colorTwo,
												useGradient,isDashed,isFilled,
													strokeWidth,dashLength);
					break;
		}
		
		// if the mouse is being dragged, does not add shape to shapeArray
		if (!isDragged)
			shapeArray.add(shapeDrawn);
	} // end createShape
	
	public static void main( String args[])
	{
		DrawingApplication drawApp = new DrawingApplication();
		
		drawApp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		drawApp.setSize(800,500);
		drawApp.setVisible(true);
	} // end main
} // end DrawingApplication
