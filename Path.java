import java.util.ArrayList;
import java.util.List;

public class Path
{
    Point StPnt;
    Point EndPnt;

    List <Point> PathPnts = new ArrayList <Point> ();

    Dot PosDot;
    
    float TotDeviation;
    float PositiveDev;
    float NegativeDev;
    float MagnitudeDev;
    float TwoEndsAngle;
    
    int Type;

    int Processed;

    // Posible Type Values and their meaming is givendown in the constants

    static final int CLOSED_LOOP = 0;
    static final int POS_DOM = 1;
    static final int NEUTRAL = 2;
    static final int NEG_DOM = 3;
    
    Path(Point vStPnt, Point vEndPnt, float vTotDeviation, float vPositiveDev, float vNegativeDev, float vMagnitudeDev, float vTwoEndsAngle)
    {    
        Processed = 0;
        PosDot = new Dot();
        StPnt = vStPnt;
        EndPnt = vEndPnt;
        TotDeviation = vTotDeviation;
        PositiveDev  = vPositiveDev;
        NegativeDev  = vNegativeDev;
        MagnitudeDev = vMagnitudeDev;
        TwoEndsAngle = vTwoEndsAngle;
    }
    
    Path()
    {    
        Processed = 0;
        PosDot = new Dot();
        StPnt = new Point(-1, -1);
        EndPnt = new Point(-1, -1);
        TotDeviation = 0;
        PositiveDev  = 0;
        NegativeDev  = 0;
        MagnitudeDev = 0;
        TwoEndsAngle = 0;
    }

    public void ReversePath()
    {
        TotDeviation *= -1;

        float Swap = PositiveDev;
        PositiveDev = NegativeDev;
        NegativeDev = Swap;

        Point SwapPnt = StPnt;
        StPnt = EndPnt;
        EndPnt = SwapPnt;

        int SwapID = StPnt.PathID;
        StPnt.PathID = EndPnt.PathID;
        EndPnt.PathID = SwapID;

        TwoEndsAngle = StPnt.getAngle(EndPnt);
    }
}