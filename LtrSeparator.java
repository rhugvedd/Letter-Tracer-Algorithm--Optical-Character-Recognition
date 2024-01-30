import java.io.IOException;

public class LtrSeparator
{
    private Quadrilateral DataBox;

    private int PixelsRed[][];
    private int PixelsGreen[][];
    private int PixelsBlue[][];

    private int TraceContDetectVal = 30;

    private Dictionary LtrDictionary;
    
    private String LineText = "";

    private Dot PrevLtrMinVal = new Dot(), PrevLtrMaxVal;

    private static final float SPACE_DETECT_CONST = 2.5F;

    LtrSeparator(Quadrilateral DataBox, int PixelsRed[][], int PixelsGreen[][], int PixelsBlue[][], Dictionary LtrDictionary) throws PixelDistOutOfBoundsException
    {
        this.DataBox = DataBox;
        
        this.PixelsRed = PixelsRed;
        this.PixelsGreen = PixelsGreen;
        this.PixelsBlue = PixelsBlue;

        this.LtrDictionary = LtrDictionary;
    }

    public void Start(ReadImage riTxtImage) throws PixelDistOutOfBoundsException, WrongTravelInitializerException, IOException
    {
        int TraceContDetectIncr = 0;
        boolean NoLtrDetected = true, StSpaceAvg = false;
        int SumOfObs = 0, NoOfObs = 0, CrntSpaceCnt = 0;
        
        System.out.println("\nThe Detected Sentence -->\n");

        Travel DataBoxTrav = new Travel (
                                            DataBox, 
                                            Travel.VERTICAL,
                                            Travel.BOTTOM, 
                                            Travel.STEP, 
                                            1, 
                                            0
                                        );

        Dot ChkDot = DataBoxTrav.getNxtChkDot();

        while(ChkDot != null)
        {
            if((PixelsBlue[ChkDot.Row][ChkDot.Col] >= TraceContDetectVal) && (PixelsBlue[ChkDot.Row][ChkDot.Col] <= TraceContDetectVal + 2))
            {
                DataBoxTrav.getNxtChkLine();
                NoLtrDetected = false;
            }
            else if(PixelsRed[ChkDot.Row][ChkDot.Col] == 0)
            {
                LtrTracer ExtrctLtrBorder = new LtrTracer   (
                                                                ChkDot, 
                                                                PixelsRed, 
                                                                PixelsGreen, 
                                                                PixelsBlue, 
                                                                TraceContDetectVal + TraceContDetectIncr
                                                            );

                ExtrctLtrBorder.Start();
                
                if(StSpaceAvg)
                {
                    CrntSpaceCnt = ExtrctLtrBorder.LtrMinVal.Col - PrevLtrMaxVal.Col - 1;
                 
                    if(CrntSpaceCnt <= 0)
                        CrntSpaceCnt = 1;
                
                    float ChkAvgSpace = (float)SumOfObs / NoOfObs;
                
                    if((NoOfObs == 0) || (ChkAvgSpace == 0))
                        ChkAvgSpace = 2;
                    
                    if(CrntSpaceCnt > (ChkAvgSpace * SPACE_DETECT_CONST))
                        LineText += ' ';
                    else
                    {    
                        SumOfObs += CrntSpaceCnt;
                        NoOfObs++;
                    }
                }

                if(DecodeLtr(ExtrctLtrBorder, riTxtImage))
                    break;

                PrevLtrMinVal = ExtrctLtrBorder.LtrMinVal;
                PrevLtrMaxVal = ExtrctLtrBorder.LtrMaxVal;

                TraceContDetectIncr = (TraceContDetectIncr + 1) % 3;

                DataBoxTrav.getNxtChkLine();

                ExtrctLtrBorder = null;
                NoLtrDetected = false;
                StSpaceAvg = true;
            }
            
            if(NoLtrDetected)
                ChkDot = DataBoxTrav.getNxtChkDot();
            else
            {
                ChkDot = DataBoxTrav.ChkLine.StDot;
                NoLtrDetected = true;
            }
        }
    }

    private void Distinguish_il(char DetectedChar, ExtractChar Decoder)
    {
        try 
        {
            if(LineText.length() == 0)
                LineText += 'i';
            else if(Decoder.getLtrMinVal().Row > ((PrevLtrMaxVal.Row + PrevLtrMinVal.Row) / 2))
                LineText += '.';
            else
                LineText += 'i';
        } 
        catch (Exception e) 
        {
            LineText += DetectedChar;
        }
    }

    private void Distinguish_ilOld(char DetectedChar, ExtractChar Decoder)
    {
        if(LineText.length() == 0)
            LineText += 'l';
        else
        {
            char PrevChar = LineText.charAt(LineText.length() - 1);

            if((PrevChar != 'i') && (PrevChar != '.') && (PrevChar != 'l'))
                LineText += DetectedChar;
            else
            {
                Dot DotLtrMinVal = PrevLtrMinVal, DotLtrMaxVal = PrevLtrMaxVal; 
                Dot LILtrMinVal = Decoder.getLtrMinVal(), LILtrMaxVal = Decoder.getLtrMaxVal();

                if((DetectedChar == '.') && ((PrevChar == 'l') || (PrevChar == 'i')))
                {
                    DotLtrMinVal = Decoder.getLtrMinVal();
                    DotLtrMaxVal = Decoder.getLtrMaxVal();
                    
                    LILtrMinVal = PrevLtrMinVal;
                    LILtrMaxVal = PrevLtrMaxVal;
                }

                if  (
                        (
                            (DotLtrMinVal.Col >= LILtrMinVal.Col) && (DotLtrMinVal.Col <= LILtrMaxVal.Col) ||
                            (DotLtrMaxVal.Col >= LILtrMinVal.Col) && (DotLtrMaxVal.Col <= LILtrMaxVal.Col)
                        )
                        &&
                        (DotLtrMaxVal.Row < LILtrMinVal.Row)
                    )
                {
                    String TempStr = LineText.substring(0, (LineText.length() - 1));

                    LineText = TempStr + 'i';
                }
                else
                    LineText += DetectedChar;
            }
        }
        // PrevLtrMinVal = Decoder.getLtrMinVal();
        // PrevLtrMaxVal = Decoder.getLtrMaxVal();
    }

    private boolean DecodeLtr(LtrTracer TraceChar, ReadImage riTextImg) throws PixelDistOutOfBoundsException, WrongTravelInitializerException, IOException
    {
        riTextImg.DeleteImage("CharImage.bmp");

        ExtractChar CharDecoder = 
        new ExtractChar (   
                            new Image   (
                                            TraceChar.LtrMaxVal, 
                                            TraceChar.LtrMinVal, 
                                            TraceChar.LeftTraceList, 
                                            TraceChar.RightTraceList, 
                                            riTextImg
                                        ), 
                            TraceChar, 
                            LtrDictionary
                        );

        char DetectedChar = CharDecoder.getChar();

        if(DetectedChar == '`')
            LineText += '#';

        // if(DetectedChar == '#')
        //     return true;

        if((DetectedChar == '.') || (DetectedChar == 'i'))// || (DetectedChar == 'l'))
            Distinguish_il(DetectedChar, CharDecoder);
        else
            LineText += DetectedChar;

        // System.out.println("The Decoded Sentence -->\n" + LineText);
        return false;
    }

    public String getText()
    {
        return LineText;
    }
}