import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;

public class Oval extends BoundedShape 
{	
	float[] dashes = {getDashLength()}; // dash length
	
	public Oval()
	{
		// call superclass default constructor
		super();
	} // default Oval constructor
	
	public Oval( Point pointA, Point pointB, Color colorOne, Color colorTwo,
			boolean useGradient, boolean isDashed, boolean isFilled, 
				int strokeWidth, int dashLength)
	{
		// call superclass constructor
		super(pointA,pointB,colorOne,colorTwo,useGradient,isDashed,isFilled,
				strokeWidth,dashLength);
	} // end Oval constructor
	
	@Override
	public void draw( Graphics2D g2d )
	{
		Ellipse2D.Double myOval; // 2D Oval that will be created when draw is called
		
		myOval = new Ellipse2D.Double(getUpperLeftX(),getUpperLeftY(),
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
			g2d.fill(myOval);
		
		g2d.draw(myOval);
	} // end draw
} // end Oval
