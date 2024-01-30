
public class Vector
{
	float Dist, Angle;
	
	Vector(float vDist, float vAngle)
	{
		Dist = vDist;
		Angle = vAngle;
	}

	Vector()
	{
		Dist = 0;
		Angle = 0;
	}

	public String toString()
	{
		String Str;
		
		Str = Float.toString(Dist);
		Str = Str + " <" + Float.toString(Angle);
		
		return Str;
	}
}