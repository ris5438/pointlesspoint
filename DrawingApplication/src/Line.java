import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;	
import java.awt.geom.Line2D;

public class Line extends Shape
{
	private float[] dashes = {getDashLength()}; // dash length
	
	public Line()
	{
		// call superclass default constructor
		super();
	} // end default Line constructor
	
	public Line( Point pointA, Point pointB, Color colorOne, Color colorTwo,
				boolean useGradient, boolean isDashed, int strokeWidth, int dashLength)
	{
		// call superclass constructor
		super(pointA,pointB,colorOne,colorTwo,useGradient,isDashed,strokeWidth,dashLength); 
	} // end Line constructor
	
	@Override
	public void draw( Graphics2D g2d )
	{	
		Line2D.Double myLine; // 2D line that will be created when draw is called
		
		myLine = new Line2D.Double(getPointA().getX(),getPointA().getY(),
				getPointB().getX(),getPointB().getY());
		
		g2d.setStroke(new BasicStroke(getstrokeWidth()));
		g2d.setColor(getColorOne());
		
		if (useGradient())
			g2d.setPaint(new GradientPaint((int)getPointA().getX()+5,
							(int)getPointA().getY()+30,getColorOne(),
								(int)getPointA().getX()+35,
									(int)getPointA().getY()+100,getColorTwo(),true));
		
		if (isDashed())
			g2d.setStroke(new BasicStroke(getstrokeWidth(),BasicStroke.CAP_BUTT,
							BasicStroke.JOIN_BEVEL,getDashLength(),dashes,0));
		
		g2d.draw(myLine);
	} // end draw
} // end Line
