import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

public abstract class BoundedShape extends Shape 
{
	private boolean isFilled;           // true if the shape is filled
	
	public BoundedShape()
	{
		// call superclass default constructor
		super();
		
		// shape initially unfilled
		this.isFilled = false;
	} // end default BoundedShape constructor
	
	public BoundedShape( Point pointA, Point pointB, Color colorOne, Color colorTwo,
							boolean useGradient, boolean isDashed, boolean isFilled, 
								int strokeWidth, int dashLength)
	{
		// call superclass constructor
		super(pointA,pointB,colorOne,colorTwo,useGradient,isDashed,strokeWidth,dashLength);
		
		// shape initially unfilled
		this.isFilled = isFilled;
	} // end BoundedShape constructor
	
	// getter function for isFilled
	public boolean isFilled()
	{
		return isFilled;
	} // end isFilled
	
	// setter function for isFilled
	public void setFilled(boolean isFilled)
	{
		this.isFilled = isFilled;
	} // end setFilled
	
	// calculates the height of the shape
	public int getHeight()
	{
		return Math.abs((int)getPointA().getY() - (int)getPointB().getY());
	} // end getHeight
	
	// calculates the width of the shape
	public int getWidth()
	{
		return Math.abs((int)getPointA().getX() - (int)getPointB().getX());
	} // end getWidth
	
	/* getUpperLeftX and GetUpperLeftY support drawing shapes
	   up and/or to the left of the starting point */
	
	public int getUpperLeftX()
	{
		if (getPointA().getX() > getPointB().getX())
		{
			Point temp = getPointA();
			setPointA(getPointB());
			setPointB(temp);
		}
		
		return (int)getPointA().getX();
	} // end getUpperLeftX
	
	public int getUpperLeftY()
	{
		if (getPointA().getY() > getPointB().getY())
		{
			Point temp = getPointA();
			setPointA(getPointB());
			setPointB(temp);
		}
		
		return (int)getPointA().getY();
	} // end getUpperLeftY
	
	// abstract method draw
	public abstract void draw( Graphics2D g2d );
} // end BoundedShape
