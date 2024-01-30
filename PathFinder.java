import java.util.ArrayList;
import java.util.List;

/**
 * This class finds a {@code Path} of blank space form in between letters 
 * or precisely any obstruction. The Path is a {@link List} of the objects
 * of the class {@link Dot} who successively represent the path followed in 
 * pixels.
 * 
 * <p>This logic is based on the principle of flow of a liquid under 
 * the influece of gravity, to detect a continuous white space which
 * is present between the letters. A liquid would flow towards the gravity 
 * always which enables it to go through the blank spaces between the obstructions
 * 
 * @author Rhugved Pankaj Chaudhari
 * 
 */

public class PathFinder
{
    private Dot StDot;
    
    private List <Dot> FlowPixList = new ArrayList <Dot> ();
    
    private int Cursor = 0;
    private int FlowDirection = 0;
    private int FlowStopVal;

    private int NxtPixPriority[] = new int[8];

    private static final int RowIncr[]   = {-1, -1,  0,  1,  1,  1,  0, -1};
    private static final int ColIncr[]   = { 0,  1,  1,  1,  0, -1, -1, -1};
    private static final int CursrIncr[] = {-1, -1,  0,  1,  1,  1,  0, -1};

    public static final int FLOW_DOWN   = 1;
    public static final int FLOW_RIGHT  = 2;

    private static final int RED     = 3; 
    private static final int GREEN   = 4;
    private static final int BLUE    = 5; 

    public static final int PATH_CONTINUED     = 6;
    public static final int PATH_INTERUPTED    = 7;
    public static final int PATH_COMPLETED     = 8;

    private static final boolean CHK_BLK = true;
    private static final boolean CHK_WH  = false;

    private int PixelsRed[][];
    private int PixelsGreen[][];
    private int PixelsBlue[][];

    private int FinalContinuationStatus = PATH_CONTINUED;

    private int PrevCursor = -1;

    private boolean ChkMoveWithDirection = false;
    private boolean isMoveWithFlowDirection = true;

    // private boolean ExceptionOccured = false;

    /**
     * The Constructor
     * @param StDot
     * @param FlowDirection
     * @param FlowStopVal
     * @param PixelsRed
     * @param PixelsGreen
     * @param PixelsBlue
     */

    PathFinder(Dot StDot, int FlowDirection, int FlowStopVal, int PixelsRed[][], int PixelsGreen[][], int PixelsBlue[][])
    {
        this.StDot = StDot;
        this.FlowDirection = FlowDirection;
        this.FlowStopVal = FlowStopVal; 
        this.PixelsRed = PixelsRed;
        this.PixelsBlue = PixelsBlue;
        this.PixelsGreen = PixelsGreen;
    }

    public void Start()
    {
        SetPriorityPix();
        FlowPixList.add(StDot);

        while((FinalContinuationStatus == PATH_CONTINUED)) //&& !ExceptionOccured)
            FinalContinuationStatus = GetNxtPix();
    }

    public int getCompletionStatus()
    {
        return FinalContinuationStatus;
    }

    public void getPath(List <Dot> FlowList)
    {
        if(FinalContinuationStatus == PATH_COMPLETED)
            FlowList = FlowPixList;
        else
            FlowList = null;
    }

    public void drawPath()
    {
        for(int FlowIndex = 0; FlowIndex < FlowPixList.size(); FlowIndex++)
        {
            // PixelsRed[FlowPixList.get(FlowIndex).Row][FlowPixList.get(FlowIndex).Col] = 255;
            // PixelsGreen[FlowPixList.get(FlowIndex).Row][FlowPixList.get(FlowIndex).Col] = 255;
            // PixelsBlue[FlowPixList.get(FlowIndex).Row][FlowPixList.get(FlowIndex).Col] = 0;
        }
    }

    public int GetNxtPix()
    {
        if(isMoveWithFlowDirection && !ChkMoveWithDirection)
        {
            if((GetAdjPixVal(0, RED) == 0) && (GetAdjPixVal(1, RED) == 0) && (GetAdjPixVal(2, RED) == 0) && (GetAdjPixVal(3, RED) == 0))
            {
                isMoveWithFlowDirection = false;
                ChkMoveWithDirection = true;
            }            
        }
        else if(!isMoveWithFlowDirection && ChkMoveWithDirection)
        {
            if(GetAdjPixVal(0, RED) == 0)
            {
                isMoveWithFlowDirection = true;
                ChkMoveWithDirection = false;
            }
        }

        if(isMoveWithFlowDirection)
            return UpdateListNormally();
        else
            return UpdateListMoveAgainstFlow();
    }

    public int GetNxtPixOld()
    {
        boolean isMoveAgainstFlowDirection = false;

        if(ChkAdjPixVal(0, CHK_BLK) && ChkAdjPixVal(1, CHK_BLK) && ChkAdjPixVal(2, CHK_BLK) && ChkAdjPixVal(3, CHK_BLK))
            isMoveAgainstFlowDirection = true;
        else if(ChkAdjPixVal(0, CHK_WH) && ((ChkAdjPixVal(1, CHK_BLK)) || ChkAdjPixVal(2, CHK_BLK)) && (ChkAdjPixVal(3, CHK_BLK) || ChkAdjPixVal(4, CHK_BLK)))
            isMoveAgainstFlowDirection = true;
        else if(ChkAdjPixVal(0, CHK_WH) && ChkAdjPixVal(1, CHK_WH) && ChkAdjPixVal(2, CHK_WH) && ChkAdjPixVal(3, CHK_WH) && (GetAdjPixVal(4, RED) == 0))
            isMoveAgainstFlowDirection = true;

        if(!isMoveAgainstFlowDirection)
            return UpdateListNormally();
        else
            return UpdateListMoveAgainstFlow();
    }

    public int UpdateListMoveAgainstFlow()
    {
        Dot NxtDot = new Dot();

        for(int ChkAdjPix = 4; ChkAdjPix < 8; ChkAdjPix++)
        {
            NxtDot.set(FlowPixList.get(Cursor).Row + RowIncr[NxtPixPriority[ChkAdjPix]], FlowPixList.get(Cursor).Col + ColIncr[NxtPixPriority[ChkAdjPix]]);

            if((PrevCursor >= 0) && (NxtDot.equals(FlowPixList.get(PrevCursor))))
            {
                if(CntSurroundPix(NxtDot) != 1)
                    continue;
            }

            int ContinuationStatus = ChkContinuation(NxtDot);

            if((ContinuationStatus == PATH_CONTINUED) || (ContinuationStatus == PATH_INTERUPTED))
            {
                if((GetAdjPixVal(ChkAdjPix, RED) == 255) && (isImmediateAdjBlk(NxtDot)))
                {
                    PrevCursor = Cursor;
                    Cursor += CursrIncr[NxtPixPriority[ChkAdjPix]];
                    SetPixinList(NxtDot);
                    
                    return PATH_CONTINUED;
                }
            }
            else
                return ContinuationStatus;
            
            if(ChkAdjPix == 7)
                ChkAdjPix = -1;
        }

        return 0;
    }

    public int CntSurroundPix(Dot NxtDot)
    {
        int PixCnt = 0;

        for(int ChkPix = 0; ChkPix < 8; ChkPix++)
        {
            if(GetAdjPixVal(ChkPix, RED) == 255)
                PixCnt++;
        }
        
        return PixCnt;
    }

    public boolean isImmediateAdjBlk(Dot ChkDot)
    {
        return  
                (PixelsRed[ChkDot.Row - 1][ChkDot.Col    ] == 0) || 
                (PixelsRed[ChkDot.Row    ][ChkDot.Col + 1] == 0) ||
                (PixelsRed[ChkDot.Row + 1][ChkDot.Col    ] == 0) ||
                (PixelsRed[ChkDot.Row    ][ChkDot.Col - 1] == 0) ;
    }

    public boolean ChkAdjPixVal(int AdjPixIndex, boolean ChkWhOrBlk)
    {
        if(ChkWhOrBlk == CHK_BLK)
            return(GetAdjPixVal(AdjPixIndex, RED) == 0) || (GetAdjPixVal(AdjPixIndex, BLUE) == 50);
        else
            return(GetAdjPixVal(AdjPixIndex, RED) == 255) || (GetAdjPixVal(AdjPixIndex, BLUE) == 50);
    }

    public int UpdateListNormally()
    {
        Dot NxtDot = new Dot();

        for(int ChkAdjPix = 0; ChkAdjPix < 4; ChkAdjPix++)
        {   
            NxtDot.set(FlowPixList.get(Cursor).Row + RowIncr[NxtPixPriority[ChkAdjPix]], FlowPixList.get(Cursor).Col + ColIncr[NxtPixPriority[ChkAdjPix]]);
    
            if((PrevCursor >= 0) && (NxtDot.equals(FlowPixList.get(PrevCursor))))
                continue;

            int ContinuationStatus = ChkContinuation(NxtDot);
            
            if(ContinuationStatus == PATH_CONTINUED)
            {
                if(GetAdjPixVal(ChkAdjPix, RED) == 255)
                {
                    PrevCursor = Cursor;
                    Cursor += CursrIncr[NxtPixPriority[ChkAdjPix]];
                    SetPixinList(NxtDot);
                    
                    return ContinuationStatus;
                }
            }
            else
                return ContinuationStatus;
        }

        return 0;
    }

    public int ChkContinuation(Dot NxtDot)
    {
        if(PixelsBlue[NxtDot.Row][NxtDot.Col] == FlowStopVal)
            return PATH_COMPLETED;
    
        if(PixelsBlue[NxtDot.Row][NxtDot.Col] == 50)
            return PATH_INTERUPTED;

        return PATH_CONTINUED;
    }

    public void SetPixinList(Dot SetDot)
    {
        if(Cursor == FlowPixList.size())
            FlowPixList.add(SetDot);
        else if(Cursor < FlowPixList.size())
            FlowPixList.get(Cursor).set(SetDot);

        PixelsBlue[SetDot.Row][SetDot.Col] = 50;
    }

    public int GetAdjPixVal(int AdjPixJNo, int PixColor)
    {
        // try
        // {
            int Index = NxtPixPriority[AdjPixJNo];

            if(PixColor == BLUE)
                return PixelsBlue[FlowPixList.get(Cursor).Row + RowIncr[Index]][FlowPixList.get(Cursor).Col + ColIncr[Index]];
            
            if(PixColor == RED)
                return PixelsRed[FlowPixList.get(Cursor).Row + RowIncr[Index]][FlowPixList.get(Cursor).Col + ColIncr[Index]];
        // }
        // catch(Exception e)
        // {
        //     ExceptionOccured = true;
        // }

        return -1;
    }

    public void SetPriorityPix()
    {
        switch(FlowDirection)
        {
            case FLOW_DOWN : 
            {
                for(int SetIndex = 0, SetPrio = 4; SetIndex < 4; SetIndex++, SetPrio++)
                    NxtPixPriority[SetIndex] = SetPrio;
                    
                for(int SetIndex = 4, SetPrio = 0; SetIndex < 8; SetIndex++, SetPrio++)
                    NxtPixPriority[SetIndex] = SetPrio;
                
                break;
            }
            case FLOW_RIGHT :
            {
                for(int SetIndex = 0, SetPrio = 2; SetIndex < 6; SetIndex++, SetPrio++)
                    NxtPixPriority[SetIndex] = SetPrio;

                NxtPixPriority[6] = 0;
                NxtPixPriority[7] = 1;

                break;
            }
        }
    }
}