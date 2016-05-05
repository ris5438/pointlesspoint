import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

public abstract class Shape 
{
	private Point pointA;        // the first point of the shape
	private Point pointB;        // the second point of the shape
	private Color colorOne;      // the first color of the shape
	private Color colorTwo;      // the second color of the shape
	private int strokeWidth;     // width of the stroke
	private int dashLength;      // length of the dash
	private boolean useGradient; // true if gradient is used
	private boolean isDashed;    // true if the line is dashed
	
	public Shape()
	{
		// set default values
		pointA = new Point(0,0);
		pointB = new Point(0,0);
		colorOne = Color.BLACK;
		colorTwo = Color.BLACK;
		useGradient = false;
		isDashed = false;
		strokeWidth = 0;
		dashLength = 0;
	} // end default Shape constructor
	
	public Shape(Point pointA, Point pointB, Color colorOne, Color colorTwo, 
			boolean useGradient, boolean isDashed, int strokeWidth, int dashLength )
	{
		this.pointA = pointA;
		this.pointB = pointB;
		this.colorOne = colorOne;
		this.colorTwo = colorTwo;
		this.useGradient = useGradient;
		this.isDashed = isDashed;
		this.strokeWidth = strokeWidth;
		this.dashLength = dashLength;
	} // end Shape constructor

/**************************************************************************************	
*****************************GETTER AND SETTER FUNCTIONS*******************************
**************************************************************************************/
	public Point getPointA() 
	{
		return pointA;
	}

	public void setPointA(Point pointA ) 
	{
		this.pointA = pointA;
	}

	public Point getPointB() 
	{
		return pointB;
	}

	public void setPointB(Point pointB) 
	{
		this.pointB = pointB;
	}

	public Color getColorOne() 
	{
		return colorOne;
	}

	public void setColorOne(Color colorOne) 
	{
		this.colorOne = colorOne;
	}
	
	public Color getColorTwo() 
	{
		return colorTwo;
	}

	public void setColorTwo(Color colorTwo) 
	{
		this.colorTwo = colorTwo;
	}

	public boolean useGradient() 
	{
		return useGradient;
	}

	public void setUseGradient(boolean useGradient) 
	{
		this.useGradient = useGradient;
	}

	public boolean isDashed() 
	{
		return isDashed;
	}

	public void setDashed(boolean isDashed) 
	{
		this.isDashed = isDashed;
	}

	public int getstrokeWidth() 
	{
		return strokeWidth;
	}

	public void setstrokeWidth(int strokeWidth) 
	{
		this.strokeWidth = strokeWidth;
	}

	public int getDashLength() 
	{
		return dashLength;
	}

	public void setDashLength(int dashLength) 
	{
		this.dashLength = dashLength;
	}
/***************************************************************************************
 ***************************************************************************************
 **************************************************************************************/	
	
	// abstract method draw
	public abstract void draw(Graphics2D g2d);
} // end Shape


