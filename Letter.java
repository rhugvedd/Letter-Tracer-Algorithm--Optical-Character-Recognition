import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public class Letter
{
    public static final int CLOSED_LOOP        =  0;
    public static final int TOP_OPEN           =  1;
    public static final int TOP_RIGHT_OPEN     =  2;
    public static final int RIGHT_OPEN         =  3;
    public static final int BOTTOM_RIGHT_OPEN  =  4;
    public static final int BOTTOM_OPEN        =  5;
    public static final int BOTTOM_LEFT_OPEN   =  6;
    public static final int LEFT_OPEN          =  7;
    public static final int TOP_LEFT_OPEN      =  8;
    public static final int DOT                =  9;
    public static final int VERTICAL_LINE      = 10;
    public static final int HORIZONTAL_LINE    = 11;
    public static final int RIGHT_SLANT_LINE   = 12;
    public static final int LEFT_SLANT_LINE    = 13;
    public static final int TOP_U              = 14;

    ArrayList < ArrayList <Point>> Point = new ArrayList < ArrayList<Point>> (); 
    List <Path> LtPath = new ArrayList <Path> ();
    List <Integer> LtrInfo = new ArrayList <Integer> ();

    int ClosedPathCnt;
    int PosDomPthCnt;
    int NeutrlPthCnt;
    int NegDomPthCnt;

    static final int NOT_PROCESSED = 2;
    static final int PROCESSED = 3;

    public int LettertMatch(Letter ChkLtr)
    {
        int MatchCoefficient = 0;

        for(int ChkPth = 0; ChkPth < ChkLtr.LtPath.size(); ChkPth++)
            ChkLtr.LtPath.get(ChkPth).Processed = NOT_PROCESSED;

        for(int Pth = 0; Pth < LtPath.size(); Pth++)
        {
            Path RefPath = LtPath.get(Pth);

            boolean RefPathMatched = false;
            int MaxPosCoefficient = -1;
            int MaxPathCoefficient = -1;

            for(int ChkPthIndex = 0; ChkPthIndex < ChkLtr.LtPath.size(); ChkPthIndex++)
            {
                Path ChkPath = ChkLtr.LtPath.get(ChkPthIndex);
        
                if(ChkPath.Processed == PROCESSED)
                    continue;

                int PosCoefficient;
                
                PosCoefficient = ChkMatchPosDot(RefPath.PosDot, ChkPath.PosDot);

                if(PosCoefficient > MaxPosCoefficient)
                    MaxPosCoefficient = PosCoefficient;

                if(PosCoefficient > 0)
                {
                    MatchCoefficient += PosCoefficient;

                    int PathCoefficient = ChkMatchPathType(RefPath, ChkPath);

                    if(PathCoefficient > MaxPathCoefficient)
                        MaxPathCoefficient = PathCoefficient;

                    if(PathCoefficient > 0)
                    {
                        MatchCoefficient += PathCoefficient;

                        int EndsAngCoefficient = ChkMatchEndsAng((int)(RefPath.TwoEndsAngle), (int)ChkPath.TwoEndsAngle);

                        if(EndsAngCoefficient > 0)
                        {
                            MatchCoefficient += EndsAngCoefficient;

                            MatchCoefficient += ChkMatchPathDev((int)RefPath.PositiveDev, (int)ChkPath.PositiveDev) * PosCoefficient * PathCoefficient * EndsAngCoefficient;
                            MatchCoefficient += ChkMatchPathDev((int)RefPath.NegativeDev, (int)ChkPath.NegativeDev) * PosCoefficient * PathCoefficient * EndsAngCoefficient;
                            MatchCoefficient += ChkMatchPathDev((int)RefPath.TotDeviation, (int)ChkPath.TotDeviation) * PosCoefficient * PathCoefficient * EndsAngCoefficient;
                            MatchCoefficient += ChkMatchPathDev((int)RefPath.MagnitudeDev, (int)ChkPath.MagnitudeDev) * PosCoefficient * PathCoefficient * EndsAngCoefficient;
                        
                            RefPathMatched = true;
                            ChkLtr.LtPath.get(ChkPthIndex).Processed = PROCESSED;
    
                            break;
                        }
                    }
                }
            }

            if(!(RefPathMatched))
            {
                if((MaxPathCoefficient == 0) || (MaxPosCoefficient == 0))
                    MatchCoefficient -= 48;
                else if((MaxPosCoefficient == 1) && (MaxPathCoefficient == 1))
                    MatchCoefficient -= 36;
                else if(((MaxPosCoefficient == 1) && (MaxPathCoefficient == 2)) || ((MaxPosCoefficient == 2) && (MaxPathCoefficient == 1)))
                    MatchCoefficient -= 24;
                else if((MaxPosCoefficient == 2) && (MaxPathCoefficient == 2))
                    MatchCoefficient -= 12;
            }
        }

        return MatchCoefficient;
    }

    public int ChkMatchEndsAng(int RefEndsAng, int ChkEndsAng)
    {
        int MatchCoefficient = 0;

        if(RefEndsAng == ChkEndsAng)
            MatchCoefficient = 2;
        else if(Math.abs(RefEndsAng - ChkEndsAng) == 1)
            MatchCoefficient = 1;

        return MatchCoefficient;
    }

    public int ChkMatchPathDev(int RefDev, int ChkDev)
    {
        int MatchCoefficient = 0;

        if(RefDev == ChkDev)
            MatchCoefficient = 3;
        else if(Math.abs(ChkDev - RefDev) == 1)    
            MatchCoefficient = 2;
        else if(Math.abs(ChkDev - RefDev) == 2)    
            MatchCoefficient = 1;

        return MatchCoefficient;
    }

    public int ChkMatchPathType(Path RefPath, Path ChkPath)
    {
        int MatchCoefficient = 0;

        if(RefPath.Type == ChkPath.Type)
            MatchCoefficient = 2;
        else if(RefPath.Type == Path.CLOSED_LOOP)
        {
            if(ChkPath.Type != Path.NEUTRAL)
                if((ChkPath.PositiveDev > 9) || (ChkPath.NegativeDev > 9))
                    MatchCoefficient = 1;
        }   
        else if(Math.abs(RefPath.Type - ChkPath.Type) == 1)
            MatchCoefficient = 1;

        return MatchCoefficient;
    }

    public int ChkMatchPosDot(Dot RefDot, Dot ChkDot)
    {
        int MatchCoefficient = 0;

        if(ChkDot.equals(RefDot))
            MatchCoefficient = 2;
        else if((Math.abs(ChkDot.Row - RefDot.Row) <= 1) && (Math.abs(ChkDot.Col - RefDot.Col) <= 1))
            MatchCoefficient = 1;
        
        return MatchCoefficient;
    }

    Letter(int ColCnt, int LnPntCnt)
    {    
        ClosedPathCnt = 0;
        PosDomPthCnt = 0;
        NeutrlPthCnt = 0;
        NegDomPthCnt = 0;

        for(int Col = 0; Col < ColCnt; Col++)
        {
            Point.add(new ArrayList <Point>());

            for(int Pnt = 0; Pnt < LnPntCnt; Pnt++)
                Point.get(Col).add(new Point(-1, -1));
        }
    }

    public Point getPoint(int Col, int Pnt)
    {
        return Point.get(Col).get(Pnt);
    }

    public void DeletePoint(int Col, int Pnt)
	{
        getPoint(Col, Pnt).deleteSelfFromAdjacents();
        Point.get(Col).remove(Pnt);
    }
    
    public void DeletePoint(Point DelPnt)
    {
        for(int Col = 0; Col < Point.size(); Col++)
        {
            if(Point.get(Col).contains(DelPnt))
            {
                DelPnt.deleteSelfFromAdjacents();

                Point.get(Col).remove(DelPnt);
                break;
            }
        }
    }

    public void SetPntToNull(Point NullPnt)
    {
        for(int Col = 0; Col < Point.size(); Col++)
        {
            if(Point.get(Col).contains(NullPnt))
            {
                Point.get(Col).set(Point.get(Col).indexOf(NullPnt), null);
                break;
            }
        }
    }

	public Point getPntFrmIndex(int Index)
	{
		Point Pnt = null;

        for(int Col = 0; Col < Point.size(); Col++)
        {
            if(Index < Point.get(Col).size())
            {
                Pnt = getPoint(Col, Index);
                break;
            }
            else
                Index -= Point.get(Col).size();
        }
        
        return Pnt;
	}
}