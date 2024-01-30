public class Travel 
{
    // PUBLIC VARIABLE DECLARATION --------------------------------------------------------------------------------------------------------
    Dot TopLeft;
    Dot TopRight;
    Dot BottomLeft;
    Dot BottomRight;

    boolean LineChange;
    int CrntLineNo, ChkLnCrntDist;
    float EndLnI;
    Line ChkLine = null, StLine, EndLine;

    // PRIVATE VARIABLE DECLARATION --------------------------------------------------------------------------------------------------------
    private Dot ChkDot = null;

    private int MaxLnStVal, SlotCnt; 
    private int RectTravEndVal, RectInLoopIndx, RectSLotCnt = 1;
    private float IncrVal, StLnI, MaxLnDist, RectTravIncr, RectTravStVal, RectOutLoopIndx;
    private boolean isStLnMax, SltOrStp, isQuadRectangle;
    private int TravelType;

    // CONSTANT DECLARATION ---------------------------------------------------------------------------------------------------------------
    
    public static final int VERTICAL = 0;
    public static final int HORIZONTAL = 1;

    public static final boolean SLOT = true;
    public static final boolean STEP = false;

    public static final int RIGHT = 0;
    public static final int LEFT = 1;

    public static final int TOP = 2;
    public static final int BOTTOM = 3;

    // DECLARATION END --------------------------------------------------------------------------------------------------------------------

    Travel(Dot vTopLeft, Dot vTopRight, Dot vBottomRight, Dot vBottomLeft, int TravelType, int TravelTo, boolean SlotOrStep, int SlotOrStepCnt, int StartValue)  
    throws PixelDistOutOfBoundsException, WrongTravelInitializerException
    {
        TopLeft = vTopLeft;
        TopRight = vTopRight;
        BottomLeft = vBottomLeft;
        BottomRight = vBottomRight;
        this.TravelType = TravelType;
        isQuadRectangle = isQuadRectangle();

        if(isQuadRectangle)
            SetTravelTypeRect(TravelType, TravelTo, SlotOrStep, SlotOrStepCnt, StartValue);
        else
            SetTravelType(TravelType, TravelTo, SlotOrStep, SlotOrStepCnt, StartValue);
    }

    Travel(Quadrilateral vQuadrilateral, int TravelType, int TravelTo, boolean SlotOrStep, int SlotOrStepCnt, int StartValue) 
    throws PixelDistOutOfBoundsException, WrongTravelInitializerException
    {
        TopLeft = vQuadrilateral.Edge[Quadrilateral.TOP].StDot;
        TopRight = vQuadrilateral.Edge[Quadrilateral.TOP].EndDot;
        BottomRight = vQuadrilateral.Edge[Quadrilateral.BOTTOM].EndDot;
        BottomLeft = vQuadrilateral.Edge[Quadrilateral.BOTTOM].StDot;
        this.TravelType = TravelType;
        isQuadRectangle = isQuadRectangle();

        if(isQuadRectangle)
            SetTravelTypeRect(TravelType, TravelTo, SlotOrStep, SlotOrStepCnt, StartValue);
        else
            SetTravelType(TravelType, TravelTo, SlotOrStep, SlotOrStepCnt, StartValue);
    }

    public boolean isQuadRectangle()
    {
        return  (TopLeft.getAngle(TopRight)         == 90.00 ) && 
                (TopRight.getAngle(BottomRight)     == 180.00) && 
                (BottomRight.getAngle(BottomLeft)   == 270.00) && 
                (BottomLeft.getAngle(TopLeft)       == 0.00  );
    }

    public Line getNxtChkLine()
    {
        StLine.getNxtDot(StLnI);
        EndLine.getNxtDot(EndLnI);

        Dot StLineDot = StLine.CrntDot;
        Dot EndLineDot = EndLine.CrntDot;

        if((StLineDot == null) || (EndLineDot == null))
        {
            ChkLine = null;
            LineChange = true;
            CrntLineNo++;
        }
        else
        {
            CrntLineNo++;
            
            if((SltOrStp == SLOT) && (CrntLineNo >= SlotCnt))
            {
                ChkLine = null;
                LineChange = true;
            }
            else
            {
                ChkLine = new Line(StLineDot, EndLineDot);
                ChkLnCrntDist = 0;

                if(isStLnMax)
                {
                    StLnI = IncrVal;

                    EndLnI = (EndLine.TotDist * IncrVal) / StLine.TotDist;
                }
                else
                {
                    EndLnI = IncrVal;

                    StLnI = (StLine.TotDist * IncrVal) / EndLine.TotDist;
                }

                LineChange = true;
            }
        }
        
        return ChkLine;
    }

    public Dot getNxtChkDot() throws PixelDistOutOfBoundsException
    {   
        if(isQuadRectangle)
        {
            return RectNxtDot();
        }
        else
        {
            LineChange = false;

            if(ChkLine != null)
            {
                if(ChkLine.TotDist != 0)
                {
                    ChkLine.getNxtDotByPixels(1);
                    ChkDot = ChkLine.CrntDot;
        
                    ChkLnCrntDist++;
                }
                else 
                    ChkDot = null;
            }
                
            if(ChkDot == null)
                if(getNxtChkLine() == null)    
                    return null;
                else 
                {
                    ChkLine.getNxtDotByPixels(0);
                    ChkDot = ChkLine.CrntDot;
                    ChkLnCrntDist++;
                }

            return ChkDot;
        }
    }

    public Dot RectNxtDot()
    {
        LineChange = false;

        RectInLoopIndx += IncrVal;

        if((RectInLoopIndx * IncrVal) > EndLnI)
        {
            RectOutLoopIndx += RectTravIncr;
            
            RectInLoopIndx = (int)StLnI;

            LineChange = true;

            if(TravelType == VERTICAL)
                ChkLine = new Line((int)StLnI, (int)RectOutLoopIndx, (int)EndLnI, (int)RectOutLoopIndx);
            else
                ChkLine = new Line((int)RectOutLoopIndx, (int)StLnI, (int)RectOutLoopIndx, (int)EndLnI);

            if((SltOrStp == SLOT) && (++RectSLotCnt > SlotCnt))
                return null;

            if(RectOutLoopIndx > RectTravEndVal)
                return null;
        }

        if(TravelType == VERTICAL)
            ChkDot.set(RectInLoopIndx , (int)Math.round(RectOutLoopIndx));
        else
            ChkDot.set((int)Math.round(RectOutLoopIndx), RectInLoopIndx);

        return ChkDot;
    }

    private void SetTravelType(int TravelType, int TravelTo, boolean SlotOrStep, int SlotOrStepCnt, int StartValue) 
    throws PixelDistOutOfBoundsException, WrongTravelInitializerException
    {
        CrntLineNo = -1;
        SltOrStp = SlotOrStep;
        SlotCnt = SlotOrStepCnt;

        if(TravelType == VERTICAL)
        {
            if(TravelTo == BOTTOM)
            {
                StLine = new Line(TopLeft, TopRight);
                EndLine = new Line(BottomLeft, BottomRight);
            }
            else if(TravelTo == TOP)
            {
                StLine = new Line(BottomLeft, BottomRight);
                EndLine = new Line(TopLeft, TopRight);
            }
            else 
            {
                if(TravelTo == RIGHT) throw new WrongTravelInitializerException("Invalid Travel Type 'VERTICAL' for Travel Direction 'RIGHT'");
                else throw new WrongTravelInitializerException("Invalid Travel Type 'VERTICAL' for Travel Direction 'LEFT'");
            }
        }
        else
        {
            if(TravelTo == RIGHT)
            {
                StLine = new Line(TopLeft, BottomLeft);
                EndLine = new Line(TopRight, BottomRight);
            }
            else if(TravelTo == LEFT)
            {
                StLine = new Line(TopRight, BottomRight);
                EndLine = new Line(TopLeft, BottomLeft);
            }
            else 
            {
                if(TravelTo == BOTTOM) throw new WrongTravelInitializerException("Invalid Travel Type 'HORIZONTAL' for Travel Direction 'BOTTOM'");
                else throw new WrongTravelInitializerException("Invalid Travel Type 'HORIZONTAL' for Travel Direction 'TOP'");
            }
        }
        
        float StLineDist = StLine.TotDist;
        float EndLineDist = EndLine.TotDist;

        if(StLineDist > EndLineDist) 
        {
            MaxLnDist = StLineDist;
            isStLnMax = true;
        }
        else
        {
            MaxLnDist = EndLineDist;
            isStLnMax = false;
        }
        
        if(SlotOrStep == SLOT)
        {
            MaxLnStVal = (int)(Math.floor(((float)MaxLnDist) / (SlotOrStepCnt * 2)));
            IncrVal = ((float)MaxLnDist) / SlotOrStepCnt;

            if(SlotOrStepCnt <= 0)
                throw new WrongTravelInitializerException("Invalid Slot Number '" + SlotOrStepCnt + "' for Travel");
        }
        else
        {
            if(SlotOrStepCnt <= 0)
                throw new WrongTravelInitializerException("Invalid Step Number '" + SlotOrStepCnt + "' for Travel");

            if((StartValue < 0) || (StartValue > StLineDist))
                throw new WrongTravelInitializerException("Invalid Start Value '" + StartValue + "' for Start Line Bounds '0 to " + StLineDist + "'");

            MaxLnStVal = StartValue;

            if(isStLnMax)
                IncrVal = StLine.getRealDist(SlotOrStepCnt);
            else 
                IncrVal = EndLine.getRealDist(SlotOrStepCnt);
        }

        if(isStLnMax)
        {
            StLnI = MaxLnStVal;
            EndLnI = EndLine.TotDist * ((float)MaxLnStVal / StLine.TotDist);
        }
        else
        {
            EndLnI = MaxLnStVal;
            StLnI = StLine.TotDist * ((float)MaxLnStVal / EndLine.TotDist);
        }
    }

    private void SetTravelTypeRect(int TravelType, int TravelTo, boolean SlotOrStep, int SlotOrStepCnt, int StartValue) 
    throws WrongTravelInitializerException
    {
        ChkDot = new Dot();
        SlotCnt = SlotOrStepCnt;
        SltOrStp = SlotOrStep;

        if((TravelType == VERTICAL) && (TravelTo == BOTTOM))
        {
            StLnI = TopLeft.Row; 
            EndLnI = BottomLeft.Row;
            IncrVal = 1;
        }
        else if((TravelType == VERTICAL) && (TravelTo == TOP))
        {
            StLnI = BottomLeft.Row; 
            EndLnI = TopLeft.Row;
            IncrVal = -1;
        }
        else if((TravelType == HORIZONTAL) && (TravelTo == RIGHT))
        {
            StLnI = TopLeft.Col; 
            EndLnI = TopRight.Col;
            IncrVal = 1;
        }
        else if((TravelType == HORIZONTAL) && (TravelTo == LEFT))
        {
            StLnI = TopRight.Col; 
            EndLnI = TopLeft.Col;
            IncrVal = -1;
        }
        else if ((TravelType == VERTICAL) && (TravelTo == RIGHT))
            throw new WrongTravelInitializerException("Invalid Travel Type 'VERTICAL' for Travel Direction 'RIGHT'");
        else if ((TravelType == VERTICAL) && (TravelTo == LEFT))
            throw new WrongTravelInitializerException("Invalid Travel Type 'VERTICAL' for Travel Direction 'LEFT'");
        else if((TravelType == HORIZONTAL) && (TravelTo == BOTTOM))
            throw new WrongTravelInitializerException("Invalid Travel Type 'HORIZONTAL' for Travel Direction 'BOTTOM'");
        else if((TravelType == HORIZONTAL) && (TravelTo == TOP)) 
            throw new WrongTravelInitializerException("Invalid Travel Type 'HORIZONTAL' for Travel Direction 'TOP'");

        if(SlotOrStep == STEP)
        {
            if(SlotOrStepCnt <= 0)
                throw new WrongTravelInitializerException("Invalid Step Number '" + SlotOrStepCnt + "' for Travel");
                
            if(StartValue < 0)
                throw new WrongTravelInitializerException("Invalid Start Value '" + StartValue);
    
            if(TravelType == VERTICAL)
            {
                RectTravStVal = TopLeft.Col + StartValue;
                RectTravEndVal = TopRight.Col;               
            }
            else
            {
                RectTravStVal = TopLeft.Row + StartValue;
                RectTravEndVal = BottomLeft.Row;
            }
            RectTravIncr = SlotOrStepCnt;
        }
        else
        {
            if(SlotOrStepCnt <= 0)
                throw new WrongTravelInitializerException("Invalid Slot Number '" + SlotOrStepCnt + "' for Travel");

            if(TravelType == VERTICAL)
            {
                MaxLnDist = TopRight.Col - TopLeft.Col;

                RectTravStVal = (float)((float)TopLeft.Col + Math.floor(((float)MaxLnDist) / (SlotOrStepCnt * 2)));
                RectTravEndVal = TopRight.Col;
            }
            else
            {
                MaxLnDist = BottomLeft.Row - TopLeft.Row;

                RectTravStVal = (float)((float)TopLeft.Row + Math.floor(((float)MaxLnDist) / (SlotOrStepCnt * 2)));
                RectTravEndVal = BottomLeft.Row;
            }
            RectTravIncr = (float)MaxLnDist / SlotOrStepCnt;
        }

        RectOutLoopIndx = RectTravStVal;
        RectInLoopIndx = (int)(StLnI - IncrVal);
        
        if(TravelType == VERTICAL)
            ChkDot.set(RectInLoopIndx , (int)Math.round(RectOutLoopIndx));
        else
            ChkDot.set((int)Math.round(RectOutLoopIndx), RectInLoopIndx);

        if(TravelType == VERTICAL)
            ChkLine = new Line((int)StLnI, (int)RectOutLoopIndx, (int)EndLnI, (int)RectOutLoopIndx);
        else
            ChkLine = new Line((int)RectOutLoopIndx, (int)StLnI, (int)RectOutLoopIndx, (int)EndLnI);
    }
}