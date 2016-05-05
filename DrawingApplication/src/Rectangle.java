import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

public class Rectangle extends BoundedShape 
{	
	float[] dashes = {getDashLength()}; // dash length
	
	public Rectangle()
	{
		// call superclass default constructor
		super();
	} // default Rectangle constructor
	
	public Rectangle( Point pointA, Point pointB, Color colorOne, Color colorTwo,
			boolean useGradient, boolean isDashed, boolean isFilled, 
				int strokeWidth, int dashLength)
	{
		// call superclass constructor
		super(pointA,pointB,colorOne,colorTwo,useGradient,isDashed,isFilled,
				strokeWidth,dashLength);
	} // end Rectangle constructor
	
	@Override
	public void draw( Graphics2D g2d )
	{
		Rectangle2D.Double myRectangle; // 2D Rectangle that will be created 
		                                //   when draw is called
		
		myRectangle = new Rectangle2D.Double(getUpperLeftX(),getUpperLeftY(),
												getWidth(),getHeight());
		
		g2d.setStroke(new BasicStroke(getstrokeWidth()));
		g2d.setColor(getColorOne());
		
		if (useGradient())
			g2d.setPaint(new GradientPaint(getUpperLeftX()+5,getUpperLeftY()+30,
							getColorOne(),getUpperLeftX()+35,getUpperLeftY()+100,
								getColorTwo(),true));
		
		if (isDashed())
			g2d.setStroke(new BasicStroke(getstrokeWidth(),BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_BEVEL,getDashLength(),dashes,0));
		
		if (isFilled())
			g2d.fill(myRectangle);
		
		g2d.draw(myRectangle);
	} // end draw
} // end Rectangle
