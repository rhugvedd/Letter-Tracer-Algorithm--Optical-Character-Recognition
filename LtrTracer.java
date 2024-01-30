import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * This class traces out the bordders of any Letter given the Starting Point
 * of the trace. The Dots of the Letter Border are stored as a {@link List} 
 * with a private scope. The logic also extracts the extreme Right and Left 
 * borders of the letter, which are also stored as a {@link List} with public
 * as scope.
 * 
 * <p>
 * The algorithm of this class can be named as {@code "Catterpillar Trace"}
 * as the logic is based on the way any Cater pillar traces out and consumes 
 * a leaf. Once the tracing starts the logic terminates when the Cursor(the 
 * Current Dot) returns to the Start Dot.
 * 
 * @author Rhugved Pankaj Chaudhari
 * @since 1.0
 * 
 * @see Dot
 * @see List
 */

public class LtrTracer 
{
    private Dot StDot;
    private Dot Cursor;

    private int PixelsRed[][];
    private int PixelsGreen[][];
    private int PixelsBlue[][];

    private int ChkContVal;

    private int AdjPixStVal;
    private int ListIndex = 0;

    private static final int RowIncr[]   = {-1, -1,  0,  1,  1,  1,  0, -1};
    private static final int ColIncr[]   = { 0,  1,  1,  1,  0, -1, -1, -1};
    private static final int CursrIncr[] = {-1, -1,  0,  1,  1,  1,  0, -1};

    public List <Dot> TraceList = new ArrayList <Dot> ();
    public List <Dot> LeftTraceList = new ArrayList <Dot> ();
    public List <Dot> RightTraceList = new ArrayList <Dot> ();

    public Dot LtrMinVal = new Dot(Integer.MAX_VALUE, Integer.MAX_VALUE);
    public Dot LtrMaxVal = new Dot(Integer.MIN_VALUE, Integer.MIN_VALUE);

    LtrTracer(Dot StDot, int PixelsRed[][], int PixelsGreen[][], int PixelsBlue[][], int ChkContVal)
    {
        this.StDot = StDot;
        Cursor = new Dot(StDot);

        TraceList.add(new Dot(StDot));
        UpdateTraceLists();

        this.PixelsRed = PixelsRed;
        this.PixelsGreen = PixelsGreen;
        this.PixelsBlue = PixelsBlue;
        this.ChkContVal = ChkContVal;
    }

    public void Start()
    {
        StPixSetup();

        int TimePass = 0;

        while(true)
        {    
            if(Cursor.equals(StDot))
                if(CompletionChk())
                    break;

            if(++TimePass > 50)
            {
                TimePass =51;
            }

            GetNxtPix();
        }
    }

    private void StPixSetup()
    {
        int AdjPix = 0;
        boolean ChkWh = true;

        while(true)
        {
            if(ChkWh && PixelsRed[StDot.Row + RowIncr[AdjPix]][StDot.Col + ColIncr[AdjPix]] == 255)
                ChkWh = false;

            if(!ChkWh && (PixelsRed[StDot.Row + RowIncr[AdjPix]][StDot.Col + ColIncr[AdjPix]] == 255))
                PixelsBlue[StDot.Row + RowIncr[AdjPix]][StDot.Col + ColIncr[AdjPix]] = ChkContVal;

            if(!ChkWh && (PixelsRed[StDot.Row + RowIncr[AdjPix]][StDot.Col + ColIncr[AdjPix]] == 0))
            {        
                Cursor.set(StDot.Row + RowIncr[AdjPix], StDot.Col + ColIncr[AdjPix]);

                TraceList.add(new Dot(Cursor));
                ListIndex += CursrIncr[AdjPix];
                UpdateTraceLists();

                AdjPixStVal = ((AdjPix / 2) * 2) - 2;
                if(AdjPixStVal < 0)
                    AdjPixStVal = 6;

                break;
            }
            
            AdjPix = (AdjPix + 1) % 8;
        }
    }

    private void UpdateTraceLists()
    {
        int DupListIndex = ListIndex;

        SetMaxMinVals();

        if(ListIndex == RightTraceList.size())
            RightTraceList.add(new Dot(Cursor));
        else
        {
            if(ListIndex < 0)
                RightTraceList.add(ListIndex = 0, new Dot(Cursor));
            else if(Cursor.Col > RightTraceList.get(ListIndex).Col)
                RightTraceList.set(ListIndex, new Dot(Cursor));
        }

        if(ListIndex == LeftTraceList.size())
            LeftTraceList.add(new Dot(Cursor));
        else
        {
            if(DupListIndex < 0)
                LeftTraceList.add(ListIndex = 0, new Dot(Cursor));
            else if(Cursor.Col < LeftTraceList.get(ListIndex).Col)
                LeftTraceList.set(ListIndex, new Dot(Cursor));
        }
    }

    public void SetMaxMinVals()
    {
        if(Cursor.Row < LtrMinVal.Row)
            LtrMinVal.Row = Cursor.Row;

        if(Cursor.Col < LtrMinVal.Col)
            LtrMinVal.Col = Cursor.Col;
        
        if(Cursor.Row > LtrMaxVal.Row)
            LtrMaxVal.Row = Cursor.Row;

        if(Cursor.Col > LtrMaxVal.Col)
            LtrMaxVal.Col = Cursor.Col;
    }

    private boolean CompletionChk()
    {
        ArrayList <ArrayList <Dot>> StDotAdjDots = new ArrayList <ArrayList<Dot>> ();
        boolean isNewListNeeded = false;
        Dot ChkDot = new Dot(), ChkTraceDot = new Dot();

        StDotAdjDots.add(new ArrayList <Dot> ());

        for(int AdjPix = 0; AdjPix < 8; AdjPix++)
        {   
            ChkDot.set(StDot.Row + RowIncr[AdjPix], StDot.Col + ColIncr[AdjPix]);
            
            if(PixelsRed[ChkDot.Row][ChkDot.Col] == 0)
            {
                for(int AdjPixAdj = 0; AdjPixAdj < 8; AdjPixAdj += 2)
                {
                    if(PixelsRed[ChkDot.Row + RowIncr[AdjPixAdj]][ChkDot.Col + ColIncr[AdjPixAdj]] == 255)
                    {
                        StDotAdjDots.get(StDotAdjDots.size() - 1).add(new Dot(StDot.Row + RowIncr[AdjPix], StDot.Col + ColIncr[AdjPix]));
                        isNewListNeeded =  true;
                        break;
                    }
                }
            }
            else if((PixelsRed[StDot.Row + RowIncr[AdjPix]][StDot.Col + ColIncr[AdjPix]] == 255) && isNewListNeeded)
            {
                StDotAdjDots.add(new ArrayList <Dot> ());
                isNewListNeeded = false;
            }
        }

        int LastListIndx = StDotAdjDots.size() - 1;
        if(StDotAdjDots.get(LastListIndx).isEmpty())
            StDotAdjDots.remove(LastListIndx);

        if  (
                (StDotAdjDots.get(0).get(0).equals(StDot.Row + RowIncr[0], StDot.Col + ColIncr[0])) &&
                (StDotAdjDots.get(LastListIndx).get(StDotAdjDots.get(LastListIndx).size() - 1).equals(StDot.Row + RowIncr[7], StDot.Col + ColIncr[7]))
            )
            {
                for(int CopyPix = 0; CopyPix < StDotAdjDots.get(LastListIndx).size(); CopyPix++)
                    StDotAdjDots.get(0).add(new Dot(StDotAdjDots.get(LastListIndx).get(CopyPix)));
                
                StDotAdjDots.remove(LastListIndx);
            }

        boolean IsPixPresent[] = new boolean[StDotAdjDots.size()];

        for(int TraceIndex = 0; TraceIndex < TraceList.size(); TraceIndex++)
        {
            ChkTraceDot.set(TraceList.get(TraceIndex));

            for(int ListIndx = 0; ListIndx < StDotAdjDots.size(); ListIndx++)
            {
                if(IsPixPresent[ListIndx])
                    continue;

                for(int ListDotIndx = 0; ListDotIndx < StDotAdjDots.get(ListIndx).size(); ListDotIndx++)
                {
                    if(StDotAdjDots.get(ListIndx).get(ListDotIndx).equals(ChkTraceDot))
                    {
                        IsPixPresent[ListIndx] = true;
                        break;
                    }
                }
            }
        }
        
        for(int Index = 0; Index < IsPixPresent.length; Index++)
            if(!IsPixPresent[Index])
                return false;

        return true;
    }

    private boolean CompletionChkOld()
    {
        Dot ChkDot = new Dot();
        List <Dot> StDotAdjBlkPix = new ArrayList <Dot> ();
        
        for(int AdjPix = 0; AdjPix < 8; AdjPix++)
        {
            if(PixelsRed[StDot.Row + RowIncr[AdjPix]][StDot.Col + ColIncr[AdjPix]] == 0)
            {
                ChkDot.set(StDot.Row + RowIncr[AdjPix], StDot.Col + ColIncr[AdjPix]);

                for(int AdjPixAdj = 0; AdjPixAdj < 8; AdjPixAdj += 2)
                {
                    if(PixelsRed[ChkDot.Row + RowIncr[AdjPixAdj]][ChkDot.Col + ColIncr[AdjPixAdj]] == 255)
                    {
                        StDotAdjBlkPix.add(new Dot(ChkDot));
                        break;
                    }
                }
            }
        }
        
        boolean IsPixPresent[] = new boolean[StDotAdjBlkPix.size()];

        for(int TraceIndex = 0; TraceIndex < TraceList.size(); TraceIndex++)
        {
            for(int Index = 0; Index < StDotAdjBlkPix.size(); Index++)
            {
                if(IsPixPresent[Index])
                    continue;

                if(TraceList.get(TraceIndex).equals(StDotAdjBlkPix.get(Index)))
                {
                    IsPixPresent[Index] = true;
                    break;
                }
            }
        }
        
        for(int Index = 0; Index < IsPixPresent.length; Index++)
            if(!IsPixPresent[Index])
                return false;

        return true;
    }

    private void GetNxtPix()
    {
        int AdjPix = AdjPixStVal;
        boolean ChkNxtBlkPix = false;

        while(true)
        {
            if(!ChkNxtBlkPix &&  (PixelsBlue[Cursor.Row + RowIncr[AdjPix]][Cursor.Col + ColIncr[AdjPix]] == ChkContVal))
                ChkNxtBlkPix = true;
            else if(ChkNxtBlkPix && (PixelsRed[Cursor.Row + RowIncr[AdjPix]][Cursor.Col + ColIncr[AdjPix]] == 0))
            {
                Cursor.set(Cursor.Row + RowIncr[AdjPix], Cursor.Col + ColIncr[AdjPix]);
 
                TraceList.add(new Dot(Cursor));
                ListIndex += CursrIncr[AdjPix];
                UpdateTraceLists();

                AdjPixStVal = ((AdjPix / 2) * 2) - 2;
                if(AdjPixStVal < 0)
                    AdjPixStVal = 6;

                return;
            }
            else if(ChkNxtBlkPix)
                PixelsBlue[Cursor.Row + RowIncr[AdjPix]][Cursor.Col + ColIncr[AdjPix]] = ChkContVal;

            AdjPix = (AdjPix + 1) % 8;
        }
    }

    public void drawBorder()
    {
        for(int Pix = 0; Pix < TraceList.size(); Pix++)
        {
            PixelsGreen[TraceList.get(Pix).Row][TraceList.get(Pix).Col] = 255;
        }
    }
    
    public void drawLeftBorder()
    {
        for(int Pix = 0; Pix < LeftTraceList.size(); Pix++)
        {
            PixelsGreen[LeftTraceList.get(Pix).Row][LeftTraceList.get(Pix).Col] = 255;
        }
    }
    
    public void drawRightBorder()
    {
        for(int Pix = 0; Pix < RightTraceList.size(); Pix++)
        {
            PixelsGreen[RightTraceList.get(Pix).Row][RightTraceList.get(Pix).Col] = 255;
        }
    }
}