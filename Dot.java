public class Dot 
{
    int Row;
    int Col;
    
    Dot()
    {
        Row = 0;
        Col = 0;
    }

    Dot(int vRow, int vCol)
    {
        Row = vRow;
        Col = vCol;
    }

    Dot(Dot vDot)
    {
        Row = vDot.Row;
        Col = vDot.Col;
    }

    Dot(Point SetPnt)
    {
        Row = SetPnt.Row;
        Col = SetPnt.Col;
    }

	public float getDist(Dot ChkDot)
	{
		float Dist = 0;
        
		Dist = (float)(Math.sqrt(((ChkDot.Col - Col) * (ChkDot.Col - Col)) + ((ChkDot.Row - Row) * (ChkDot.Row - Row))));

		return Dist;
    }

    public void set(Dot SetDot)
    {
        Row = SetDot.Row;
        Col = SetDot.Col;
    }

    public void set(int SetRow, int SetCol)
    {
        Row = SetRow;
        Col = SetCol;
    }

	public float getAngle(Dot ChkDot) 
	{
		float Angle = (float) Math.toDegrees(Math.atan2(ChkDot.Col - Col, Row - ChkDot.Row));
		if (Angle < 0)
			Angle += 360;

		return Angle;
	}

    public boolean isConnected(Dot EndDot, ReadImage riICob) throws PixelDistOutOfBoundsException
    {
        if(this.equals(EndDot))
            return true;

        Line ChkLine = new Line(this, EndDot);

        ChkLine.getNxtDotByPixels(0);

        while(ChkLine.CrntDot != null)
        {
            if(riICob.PixelsRed[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] == 255)
            {
                ChkLine = null;
                return false;
            }

            ChkLine.getNxtDotByPixels(1);
        }

        ChkLine = null;
        return true;
    }

    public boolean isConnected(Dot EndDot, Image ImageOb) throws PixelDistOutOfBoundsException
    {
        if(this.equals(EndDot))
            return true;

        Line ChkLine = new Line(this, EndDot);

        ChkLine.getNxtDotByPixels(0);

        while(ChkLine.CrntDot != null)
        {
            if(ImageOb.PixelsRed[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] == 255)
            {
                ChkLine = null;
                return false;
            }

            ChkLine.getNxtDotByPixels(1);
        }

        ChkLine = null;
        return true;
    }

    public boolean equals(Dot ChkDot)
    {
        return((Row == ChkDot.Row) && (Col == ChkDot.Col));
    }
    
    public boolean equals(int ChkRow, int ChkCol)
    {
        return((Row == ChkRow) && (Col == ChkCol));
    }
}