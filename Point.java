import java.util.ArrayList;
import java.util.List;

public class Point
{
	int Row, Col;
	int PathID;

	List <Point> Adjacent = new ArrayList <Point>();

	Point(int vRow, int vCol) 
	{
		Row = vRow;
		Col = vCol;
		PathID = -1;
	}

	Point(Point SetPnt)
	{
		Row = SetPnt.Row;
		Col = SetPnt.Col;
		PathID = SetPnt.PathID;
	}

	public float getAngle(Point Pnt) 
	{
		float Angle = (float) Math.toDegrees(Math.atan2(Pnt.Col - Col, Row - Pnt.Row));
		if (Angle < 0)
			Angle += 360;

		return Angle;
	}

	public Vector toVector(Point Pnt)
	 {
		Vector oVector = new Vector();

		oVector.Angle = this.getAngle(Pnt);
		oVector.Dist = getDist(this, Pnt);

		return oVector;
	}

	public float getDist(Point P1, Point P2) 
	{
		float Dist = 0;

		Dist = (float)(Math.sqrt(((P2.Col - P1.Col) * (P2.Col - P1.Col)) + ((P2.Row - P1.Row) * (P2.Row - P1.Row))));

		return Dist;
	}

	public float getDist(Point ChkPnt)
	{
		float Dist = 0;
		
		Dist = (float)(Math.sqrt(((ChkPnt.Col - Col) * (ChkPnt.Col - Col)) + ((ChkPnt.Row - Row) * (ChkPnt.Row - Row))));

		return Dist;
	}

	public float getDist(int ChkRow, int ChkCol)
	{
		float Dist = 0;
		
		Dist = (float)(Math.sqrt(((ChkCol - Col) * (ChkCol - Col)) + ((ChkRow - Row) * (ChkRow - Row))));

		return Dist;
	}

	public Dot getDot()
	{
		return(new Dot(Row, Col));
	}

	public void setDot(Dot SetDot)
	{
		Row = SetDot.Row;
		Col = SetDot.Col;
	}

	public void setPoint(Point SetPnt)
	{
		Row = SetPnt.Row;
		Col = SetPnt.Col;
		PathID = SetPnt.PathID;
	}

	public Point getPoint(int r, int Angle)
	 {
		Point SolnPnt = new Point(0, 0);

		int AngCoefficient = ((int) (Math.floor(Angle / 90)));
		int RowMultiplier = 10000, ColMultiplier = 10000;

		switch (AngCoefficient) 
		{
			case 0:{RowMultiplier = -1; ColMultiplier =  1; Angle = 90 - Angle;  break;}
			case 1:{RowMultiplier =  1; ColMultiplier =  1; break;}
			case 2:{RowMultiplier =  1; ColMultiplier = -1; Angle = 450 - Angle; break;}
			case 3:{RowMultiplier = -1; ColMultiplier = -1;	break;}
		}

		Angle -= 90 * AngCoefficient;

		SolnPnt.Row = this.Row + (RowMultiplier * ((int) (Math.round(r * (Math.sin(Math.toRadians(Angle)))))));
		SolnPnt.Col = this.Col + (ColMultiplier * ((int) (Math.round(r * (Math.cos(Math.toRadians(Angle)))))));

		return SolnPnt;
	}

	public void AddAdjacent(Point AddAdjPnt)
	{
		float ChkAng = this.getAngle(AddAdjPnt);

		int Adj;

		for(Adj = 0; Adj < Adjacent.size(); Adj++)
		{
			if(this.getAngle(Adjacent.get(Adj)) > ChkAng)
			{
				Adjacent.add(Adj, AddAdjPnt);
				break;
			}
		}

		if(Adj == Adjacent.size())
			Adjacent.add(AddAdjPnt);
	}

	public void DeleteAndRepairAdjs(Point DelPnt, Point ThirdPntOfTriangle)
	{
		if(Adjacent.contains(DelPnt))
			Adjacent.remove(DelPnt);

		if(DelPnt.Adjacent.contains(this))	
			DelPnt.Adjacent.remove(this);

		if(!(ThirdPntOfTriangle.Adjacent.contains(DelPnt)))
			ThirdPntOfTriangle.AddAdjacent(DelPnt);
		
		if(!(DelPnt.Adjacent.contains(ThirdPntOfTriangle)))
			DelPnt.AddAdjacent(ThirdPntOfTriangle);

		if(!(ThirdPntOfTriangle.Adjacent.contains(this)))	
			ThirdPntOfTriangle.AddAdjacent(this);
		
		if(!(this.Adjacent.contains(ThirdPntOfTriangle)))
			this.AddAdjacent(ThirdPntOfTriangle);
	}

	public boolean equals(Point ChkPnt)
	{
		return((Row == ChkPnt.Row) && (Col == ChkPnt.Col));
	}

	public boolean equals(int ChkRow, int ChkCol)
	{
		return((Row == ChkRow) && (Col == ChkCol));
	}

	public boolean equals(Dot ChkDot)
	{
		return((Row == ChkDot.Row) && (Col == ChkDot.Col));
	}

	public void getNxtAdjPoints(Point PrevPnt, Point NxtAdjPnt[])
	{
		int Index = 0;

		for(int Adj = 0; Adj < Adjacent.size(); Adj++)
		{
			if(Adjacent.get(Adj).equals(PrevPnt))
				continue;
			
			NxtAdjPnt[Index] = Adjacent.get(Adj);

			Index++;
		}
	}

	public Point getAdjacent(int Index)
	{
		return Adjacent.get(Index);
	}

	public void deleteSelfFromAdjacents()
	{
		for(int Adj = 0; Adj < Adjacent.size(); Adj++)
			if(Adjacent.get(Adj).Adjacent.contains(this))
				Adjacent.get(Adj).Adjacent.remove(this);
	}

	public int getPntIndex(Letter Ltr)
	{
		int Index = 0;

		for(int Col = 0; Col < Ltr.Point.size(); Col++)
		{
			if(Ltr.Point.get(Col).contains(this))
			{
				Index += Ltr.Point.get(Col).indexOf(this);
				break;
			}
			else 
				Index += Ltr.Point.get(Col).size();
		}
	
		return Index;
	}
}