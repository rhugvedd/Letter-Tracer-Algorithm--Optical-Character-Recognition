import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ExtractChar extends Image implements ExtractCharConsts
{
    private static final int FILL_HORIZONTAL = 0;
    private static final int FILL_VERTICAL = 1;

    private static final int CHAR_NOT_DETECTED = 0;
    private static final char MULTIPLE_ANS_CHAR = '~';
    private static final char OPENING_DICTIONARY_LTR_SEPARATOR = '~';
    private static final char MULTIPLE_ANS_CHAR_NOT_DETECTED = '`';

    private static final int NO_TRAV            =  0;
    private static final int TOP_TRAV           =  1;
    private static final int RIGHT_TRAV         =  2;
    private static final int BOTTOM_TRAV        =  4;
    private static final int LEFT_TRAV          =  3;
    private static final int BUFFER_TRAV        =  5;
    private static final int TOP_RIGHT_BUF      =  7;
    private static final int BOTTOM_RIGHT_BUF   =  9;
    private static final int BOTTOM_LEFT_BUF    =  5;
    private static final int TOP_LEFT_BUF       =  8;

    private static final int FIRST_NXT_DIR_BY_PREV_BUFF[] = { 1, -1, 3, 2, 1};
    private static final int SECND_NXT_DIR_BY_PREV_BUFF[] = { 2, -1, 4, 4, 3};
    private static final int BUFFER_ANGLE_CHK[] = {-1, -1, 1, 2, -1, -1, -1, -1, 2, 4, -1, -1, -1, -1, 4, 3, -1, -1, -1, -1, 3, 1, -1, -1};
    
    private static final int FIRST_CONSEC_BUFR_EQUIVALENTS[] = {190, -1, 10, 280, 100};
    private static final int SECND_CONSEC_BUFR_EQUIVALENTS[] = {260, -1, 80, 350, 170};

    private List <Dot> TraceList = new ArrayList <Dot> ();

    private Dot TopLeftDot, TopRightDot, TopEdgeMidDot;

    private Dictionary LtrDictionary;

    private Dot LtrMinVal = new Dot(), LtrMaxVal = new Dot();

    ExtractChar(Image CharImg, LtrTracer TraceLtr, Dictionary LtrDictionary) throws IOException
    {
        super(CharImg);
        this.TraceList = TraceLtr.TraceList;

        this.TopLeftDot = TraceLtr.LtrMinVal;
        this.TopRightDot = new Dot(TraceLtr.LtrMinVal.Row, TraceLtr.LtrMaxVal.Col);
        this.TopEdgeMidDot = new Dot(TopLeftDot.Row, (TopLeftDot.Col + TopRightDot.Col) / 2);
        this.LtrDictionary = LtrDictionary;

        this.LtrMinVal = TraceLtr.LtrMinVal;
        this.LtrMaxVal = TraceLtr.LtrMaxVal;
    }

    public char getChar() throws PixelDistOutOfBoundsException, WrongTravelInitializerException, IOException
    {
        Long LtrCode = GetLtrCode();
        char DetectedChar = LtrDictionary.getChar(LtrCode);
        super.createImage("CharImage.bmp");

        if(DetectedChar == CHAR_NOT_DETECTED)
        {
            DecodeLtr(LtrCode);

            // System.out.println("\n Add this Code ? Which Letter is this??\n");

            // BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

            // char UserAns = (char)(input.read());

            // if((UserAns != '#') && (UserAns != '`'))
            // {
            //     LtrDictionary.add(LtrCode, UserAns);
            //     System.out.println("'" + UserAns + "' is added..!!\n");
            // }
            
            // return UserAns;

            return '#';
        }
        else if((DetectedChar == MULTIPLE_ANS_CHAR) || (DetectedChar == 'c') || (DetectedChar == 'e') || (DetectedChar == 'g') || (DetectedChar == 'p') || (DetectedChar == 'r'))
        {
            DecodeLtr(LtrCode);

            char AnsChar;

            try
            {
                AnsChar = LtrDictionary.distinguishChar(GetOpeningsCode(GetConeCode(FILL_HORIZONTAL), GetConeCode(FILL_VERTICAL)));
            }
            catch(Exception e)
            {
                return '`';
            }

            if(AnsChar == MULTIPLE_ANS_CHAR_NOT_DETECTED)
                return '#';
            else
            {
                if((AnsChar == 'l') || (AnsChar == '.') || (AnsChar == 'i'))
                    AnsChar = Distinguish_il();
             
                System.out.println("\nThe Detected Character -->\n" + AnsChar + "\n");
   
                return AnsChar;
            }
        }
        else
        {
            DecodeLtr(LtrCode);

            System.out.println("\nThe Detected Character -->\n" + DetectedChar + "\n");
            
            return DetectedChar;
        }
    }

    public Dot getLtrMinVal()
    {
        return LtrMinVal;
    }

    public Dot getLtrMaxVal()
    {
        return LtrMaxVal;
    }

    private char Distinguish_il()
    {
        final float LI_Distinguish = 2F;
        
        if(((float)ImageHeight / ImageWidth) >= LI_Distinguish)
            return 'l';
        else    
            return '.';
    }

    private void FillWhCones(int Direction) throws PixelDistOutOfBoundsException, WrongTravelInitializerException
    {
        final float NotFillConeRatio = (float)2 / 5;
        int FillConeConst = FILL_WH_CONE_HORIZNTL_CONST;
        int TravelType = Travel.HORIZONTAL;
        int TravelTo = Travel.RIGHT;
        int PrevRowChk = 1;
        int PrevColChk = 0;
        int NotFillConeChkPara = ImageHeight;

        if(Direction == FILL_VERTICAL)
        {
            TravelType = Travel.VERTICAL;
            TravelTo = Travel.BOTTOM;
            PrevRowChk = 0;
            PrevColChk = 1;
            FillConeConst = FILL_WH_CONE_VERTICAL_CONST;
            NotFillConeChkPara = ImageWidth;
        }

        List <Cone> ConeList = new ArrayList <Cone> ();
        
        Travel LtrTravel = new Travel   (
                                            ImageQuad,
                                            TravelType, 
                                            TravelTo, 
                                            Travel.STEP,
                                            1,
                                            0
                                        );

        Dot ChkDot = LtrTravel.getNxtChkDot();
        boolean ChkWh = true, ChkBlk = false;
        Dot BlkDot = new Dot(), WhiteDot = new Dot();
        Dot PrevDot = new Dot(ChkDot);
        
        while(ChkDot != null)
        {
            if(((PixelsRed[ChkDot.Row][ChkDot.Col] == 255) && (PixelsRed[PrevDot.Row][PrevDot.Col] == 0)) && ChkWh)
            {
                WhiteDot.set(ChkDot);

                ChkWh = false;
                ChkBlk = true;
            }
            
            if(((PixelsRed[ChkDot.Row][ChkDot.Col] == 0) && ChkBlk))
            {
                BlkDot.set(ChkDot);
                ChkWh = true;
                ChkBlk = false;

                Line ConeChkLine = new Line(WhiteDot, BlkDot);

                ConeChkLine.getNxtDotByPixels(0);
                boolean isConeIdentified = false;
                boolean isBlackTop = true;
                int ConeID = -1;

                while(ConeChkLine.CrntDot != null)
                {
                    if  (   
                            (ConeChkLine.CrntDot.Row == 0) || (ConeChkLine.CrntDot.Col == 0) || 
                            (PixelsRed[ConeChkLine.CrntDot.Row - PrevRowChk][ConeChkLine.CrntDot.Col - PrevColChk] == 255)
                        )
                        isBlackTop = false;

                    if  ( 
                            (isConeIdentified == false) && 
                            (
                                (ConeChkLine.CrntDot.Row != 0) && (ConeChkLine.CrntDot.Col != 0) && 
                                (PixelsRed[ConeChkLine.CrntDot.Row - PrevRowChk][ConeChkLine.CrntDot.Col - PrevColChk] == 255)
                            ) && 
                            (
                                (ConeChkLine.CrntDot.Row != 0) && (ConeChkLine.CrntDot.Col != 0) && 
                                (PixelsBlue[ConeChkLine.CrntDot.Row - PrevRowChk][ConeChkLine.CrntDot.Col - PrevColChk] >= FillConeConst)
                            )
                        )
                    {
                        isConeIdentified = true;
                        ConeID = PixelsBlue[ConeChkLine.CrntDot.Row - PrevRowChk][ConeChkLine.CrntDot.Col - PrevColChk];
                        break;
                    }
 
                    ConeChkLine.getNxtDotByPixels(1);

                    if(ConeChkLine.CrntDot.equals(ConeChkLine.EndDot))
                        break;
                }

                if(isConeIdentified)
                {
                    ConeChkLine.setCrntDot(ConeChkLine.StDot);
                    ConeList.get(ConeID - FillConeConst).ConeLines.add(new Line(ConeChkLine));
                    ConeChkLine.drawNumberLine(this, 3, ConeID, false);
                }
                else
                {
                    ConeList.add(new Cone());

                    if(isBlackTop)
                        ConeList.get(ConeList.size() - 1).TopType = Cone.BLACK_TOP;
                    else
                        ConeList.get(ConeList.size() - 1).TopType = Cone.WHITE_TOP;
                    
                    ConeList.get(ConeList.size() - 1).ConeLines.add(new Line(ConeChkLine));
                    
                    ConeChkLine.setCrntDot(ConeChkLine.StDot);
                    ConeChkLine.drawNumberLine(this, 3, FillConeConst + ConeList.size() - 1, false);
                }
            }

            PrevDot.set(ChkDot);

            ChkDot = LtrTravel.getNxtChkDot();

            if(LtrTravel.LineChange)
            {
                if(ChkDot != null)
                    PrevDot.set(ChkDot);
                    
                ChkWh = true;
                ChkBlk = false;
            }
        }

        for(int ChkCone = 0; ChkCone < ConeList.size(); ChkCone++)
        {
            Cone ThisCone = ConeList.get(ChkCone);

            if(ThisCone.ConeLines.size() == 0)
                continue;

            Line ConeLastLn = ThisCone.ConeLines.get(ThisCone.ConeLines.size() - 1);
            boolean isBlkBotm = true;

            ConeLastLn.getNxtDotByPixels(0);

            while(ConeLastLn.CrntDot != null)
            {
                if  (
                        (ConeLastLn.CrntDot.Row == (ImageHeight - 1)) || 
                        (ConeLastLn.CrntDot.Col == (ImageWidth - 1)) || 
                        (PixelsRed[ConeLastLn.CrntDot.Row + PrevRowChk][ConeLastLn.CrntDot.Col + PrevColChk] == 255)
                    )
                {
                    isBlkBotm = false;
                    break;
                }

                ConeLastLn.getNxtDotByPixels(1);

                if(ConeLastLn.CrntDot.equals(ConeLastLn.EndDot))
                    break;
            }
            
            if  (
                    (ThisCone.ConeLines.size() < (NotFillConeChkPara * NotFillConeRatio)) &&
                    (
                        ((ThisCone.TopType == Cone.BLACK_TOP) && (isBlkBotm == false)) || 
                        ((ThisCone.TopType == Cone.WHITE_TOP) && (isBlkBotm == true))
                    )
                )
            {
                for(int ConeLn = 0; ConeLn < ThisCone.ConeLines.size(); ConeLn++)
                {
                    if
                    (
                        (ConeLn == 0) && 
                        (ThisCone.TopType == Cone.WHITE_TOP) && 
                        (ThisCone.ConeLines.get(0).StDot.Row != 0) &&
                        (ThisCone.ConeLines.get(0).StDot.Col != 0)
                    )
                        DrawConeTopOrBotmLn(ThisCone.ConeLines.get(0), true, Direction);
                    else if 
                    (
                        (ConeLn == ThisCone.ConeLines.size() - 1) && 
                        (isBlkBotm == false) && 
                        (ThisCone.ConeLines.get(ThisCone.ConeLines.size() - 1).StDot.Row != (ImageHeight - 1)) && 
                        (ThisCone.ConeLines.get(ThisCone.ConeLines.size() - 1).StDot.Col != (ImageWidth - 1))
                    )
                        DrawConeTopOrBotmLn(ThisCone.ConeLines.get(ConeLn), false, Direction);   
                    else
                    {
                        ThisCone.ConeLines.get(ConeLn).setCrntDot(ThisCone.ConeLines.get(ConeLn).StDot);
                        ThisCone.ConeLines.get(ConeLn).drawNumberLine(this, 1, 0, true);
                    }
                }
            }
            
            if((ThisCone.TopType == Cone.BLACK_TOP) && (isBlkBotm == true))
            {
                int ConeSize = ThisCone.ConeLines.size();
                int FillLnRatio = ConeSize / 4;

                for(int ConeInd = 0; ConeInd < FillLnRatio; ConeInd++)
                {
                    ThisCone.ConeLines.get(ConeInd).setCrntDot(ThisCone.ConeLines.get(ConeInd).StDot);
                    ThisCone.ConeLines.get(ConeInd).drawNumberLine(this, 1, 0, true);
                }
                
                for(int ConeInd = (ConeSize - 1); ConeInd > (ConeSize - FillLnRatio - 1); ConeInd--)
                {
                    ThisCone.ConeLines.get(ConeInd).setCrntDot(ThisCone.ConeLines.get(ConeInd).StDot);
                    ThisCone.ConeLines.get(ConeInd).drawNumberLine(this, 1, 0, true);
                }
            }
        }
    }
    
    private void DecodeOpeningCode(int Code)
    {
        final String CodeStr[] = {"Closed Loop --> ", "Top Open --> ", "Bottom Open --> ", "Left Open --> ", "Rigth Open --> "};
        int Divisor = 100000;

        System.out.println();

        for(int i = 0; i < 5; i++)
            System.out.println(CodeStr[i] + (((Code / (Divisor /= 10)) % 10) - 1));

        System.out.println();

    }

    private String GetOpeningsCode(String HoriFillCode, String VertiFillCode)
    {
        return HoriFillCode + VertiFillCode.substring(1);
        // return (HoriFillCode * 100) + (VertiFillCode % 100);
    }

    private String GetConeCode(int Direction) throws PixelDistOutOfBoundsException, WrongTravelInitializerException
    {
        final float NotFillConeRatio = (float)2 / 5;
        int FillConeConst = FILL_WH_CONE_HORIZNTL_CONST;
        int TravelType = Travel.HORIZONTAL;
        int TravelTo = Travel.RIGHT;
        int PrevRowChk = 1;
        int PrevColChk = 0;
        int NotFillConeChkPara = ImageHeight;

        int BottomOpenCnt = 1, TopOpenCnt = 1, ClosedLoopCnt = 1;

        if(Direction == FILL_VERTICAL)
        {
            TravelType = Travel.VERTICAL;
            TravelTo = Travel.BOTTOM;
            PrevRowChk = 0;
            PrevColChk = 1;
            FillConeConst = FILL_WH_CONE_VERTICAL_CONST;
            NotFillConeChkPara = ImageWidth;
        }

        List <Cone> ConeList = new ArrayList <Cone> ();
        
        Travel LtrTravel = new Travel   (
                                            ImageQuad,
                                            TravelType, 
                                            TravelTo, 
                                            Travel.STEP,
                                            1,
                                            0
                                        );

        Dot ChkDot = LtrTravel.getNxtChkDot();
        boolean ChkWh = true, ChkBlk = false;
        Dot BlkDot = new Dot(), WhiteDot = new Dot();
        Dot PrevDot = new Dot(ChkDot);
        
        while(ChkDot != null)
        {
            if(((PixelsRed[ChkDot.Row][ChkDot.Col] == 255) && (PixelsRed[PrevDot.Row][PrevDot.Col] == 0)) && ChkWh)
            {
                WhiteDot.set(ChkDot);

                ChkWh = false;
                ChkBlk = true;
            }
            
            if(((PixelsRed[ChkDot.Row][ChkDot.Col] == 0) && ChkBlk))
            {
                BlkDot.set(ChkDot);
                ChkWh = true;
                ChkBlk = false;

                Line ConeChkLine = new Line(WhiteDot, BlkDot);

                ConeChkLine.getNxtDotByPixels(0);
                boolean isConeIdentified = false;
                boolean isBlackTop = true;
                int ConeID = -1;

                while(ConeChkLine.CrntDot != null)
                {
                    if  (   
                            (ConeChkLine.CrntDot.Row == 0) || (ConeChkLine.CrntDot.Col == 0) || 
                            (PixelsRed[ConeChkLine.CrntDot.Row - PrevRowChk][ConeChkLine.CrntDot.Col - PrevColChk] == 255)
                        )
                        isBlackTop = false;

                    if  ( 
                            (isConeIdentified == false) && 
                            (
                                (ConeChkLine.CrntDot.Row != 0) && (ConeChkLine.CrntDot.Col != 0) && 
                                (PixelsRed[ConeChkLine.CrntDot.Row - PrevRowChk][ConeChkLine.CrntDot.Col - PrevColChk] == 255)
                            ) && 
                            (
                                (ConeChkLine.CrntDot.Row != 0) && (ConeChkLine.CrntDot.Col != 0) && 
                                (PixelsBlue[ConeChkLine.CrntDot.Row - PrevRowChk][ConeChkLine.CrntDot.Col - PrevColChk] >= FillConeConst)
                            )
                        )
                    {
                        isConeIdentified = true;
                        ConeID = PixelsBlue[ConeChkLine.CrntDot.Row - PrevRowChk][ConeChkLine.CrntDot.Col - PrevColChk];
                        break;
                    }
 
                    ConeChkLine.getNxtDotByPixels(1);

                    if(ConeChkLine.CrntDot.equals(ConeChkLine.EndDot))
                        break;
                }

                if(isConeIdentified)
                {
                    ConeChkLine.setCrntDot(ConeChkLine.StDot);
                    ConeList.get(ConeID - FillConeConst).ConeLines.add(new Line(ConeChkLine));
                    ConeChkLine.drawNumberLine(this, 3, ConeID, false);
                }
                else
                {
                    ConeList.add(new Cone());

                    if(isBlackTop)
                        ConeList.get(ConeList.size() - 1).TopType = Cone.BLACK_TOP;
                    else
                        ConeList.get(ConeList.size() - 1).TopType = Cone.WHITE_TOP;
                    
                    ConeList.get(ConeList.size() - 1).ConeLines.add(new Line(ConeChkLine));
                    
                    ConeChkLine.setCrntDot(ConeChkLine.StDot);
                    ConeChkLine.drawNumberLine(this, 3, FillConeConst + ConeList.size() - 1, false);
                }
            }

            PrevDot.set(ChkDot);

            ChkDot = LtrTravel.getNxtChkDot();

            if(LtrTravel.LineChange)
            {
                if(ChkDot != null)
                    PrevDot.set(ChkDot);
                    
                ChkWh = true;
                ChkBlk = false;
            }
        }

        for(int ChkCone = 0; ChkCone < ConeList.size(); ChkCone++)
        {
            Cone ThisCone = ConeList.get(ChkCone);

            if(ThisCone.ConeLines.size() == 0)
                continue;

            Line ConeLastLn = ThisCone.ConeLines.get(ThisCone.ConeLines.size() - 1);
            boolean isBlkBotm = true;

            ConeLastLn.getNxtDotByPixels(0);

            while(ConeLastLn.CrntDot != null)
            {
                if  (
                        (ConeLastLn.CrntDot.Row == (ImageHeight - 1)) || 
                        (ConeLastLn.CrntDot.Col == (ImageWidth - 1)) || 
                        (PixelsRed[ConeLastLn.CrntDot.Row + PrevRowChk][ConeLastLn.CrntDot.Col + PrevColChk] == 255)
                    )
                {
                    isBlkBotm = false;
                    break;
                }

                ConeLastLn.getNxtDotByPixels(1);

                if(ConeLastLn.CrntDot.equals(ConeLastLn.EndDot))
                    break;
            }
            
            if(ThisCone.ConeLines.size() > (NotFillConeChkPara * NotFillConeRatio))
            {
                if((ThisCone.TopType == Cone.WHITE_TOP) && (isBlkBotm == true))
                    TopOpenCnt++;

                if((ThisCone.TopType == Cone.BLACK_TOP) && (isBlkBotm == false))
                    BottomOpenCnt++;
            }

            if  (
                    (ThisCone.ConeLines.size() < (NotFillConeChkPara * NotFillConeRatio)) &&
                    (
                        ((ThisCone.TopType == Cone.BLACK_TOP) && (isBlkBotm == false)) || 
                        ((ThisCone.TopType == Cone.WHITE_TOP) && (isBlkBotm == true))
                    )
                )
            {
                for(int ConeLn = 0; ConeLn < ThisCone.ConeLines.size(); ConeLn++)
                {
                    if
                    (
                        (ConeLn == 0) && 
                        (ThisCone.TopType == Cone.WHITE_TOP) && 
                        (ThisCone.ConeLines.get(0).StDot.Row != 0) &&
                        (ThisCone.ConeLines.get(0).StDot.Col != 0)
                    )
                        DrawConeTopOrBotmLn(ThisCone.ConeLines.get(0), true, Direction);
                    else if 
                    (
                        (ConeLn == ThisCone.ConeLines.size() - 1) && 
                        (isBlkBotm == false) && 
                        (ThisCone.ConeLines.get(ThisCone.ConeLines.size() - 1).StDot.Row != (ImageHeight - 1)) && 
                        (ThisCone.ConeLines.get(ThisCone.ConeLines.size() - 1).StDot.Col != (ImageWidth - 1))
                    )
                        DrawConeTopOrBotmLn(ThisCone.ConeLines.get(ConeLn), false, Direction);   
                    else
                    {
                        ThisCone.ConeLines.get(ConeLn).setCrntDot(ThisCone.ConeLines.get(ConeLn).StDot);
                        ThisCone.ConeLines.get(ConeLn).drawNumberLine(this, 1, 0, true);
                    }
                }
            }
            
            if((ThisCone.TopType == Cone.BLACK_TOP) && (isBlkBotm == true))
            {
                ClosedLoopCnt++;

                int ConeSize = ThisCone.ConeLines.size();
                int FillLnRatio = ConeSize / 4;

                for(int ConeInd = 0; ConeInd < FillLnRatio; ConeInd++)
                {
                    ThisCone.ConeLines.get(ConeInd).setCrntDot(ThisCone.ConeLines.get(ConeInd).StDot);
                    ThisCone.ConeLines.get(ConeInd).drawNumberLine(this, 1, 0, true);
                }
                
                for(int ConeInd = (ConeSize - 1); ConeInd > (ConeSize - FillLnRatio - 1); ConeInd--)
                {
                    ThisCone.ConeLines.get(ConeInd).setCrntDot(ThisCone.ConeLines.get(ConeInd).StDot);
                    ThisCone.ConeLines.get(ConeInd).drawNumberLine(this, 1, 0, true);
                }
            }
        }

        return Integer.toString(ClosedLoopCnt) + Integer.toString(TopOpenCnt) + Integer.toString(BottomOpenCnt);
        // return (ClosedLoopCnt * 100) + (TopOpenCnt * 10) + BottomOpenCnt;
    }
    
    private void DrawConeTopOrBotmLn(Line DrawLn, boolean TopOrBotmLn, int Direction) throws PixelDistOutOfBoundsException
    {
        int ChkRowIncr = 1;
        int ChkColIncr = 0;

        int ChkColAdjIncr = 1;
        int ChkRowAdjIncr = 0;

        DrawLn.setCrntDot(DrawLn.StDot);

        if(Direction == FILL_VERTICAL)
        {
            ChkRowIncr = 0;
            ChkColAdjIncr = 0;
            ChkRowAdjIncr = 1;

            if(TopOrBotmLn)
                ChkColIncr = -1;
            else
                ChkColIncr = 1;
        }
        else if(TopOrBotmLn)
            ChkRowIncr = -1;

        DrawLn.getNxtDotByPixels(0);

        while(DrawLn.CrntDot != null)
        {
            if  (
                    (PixelsRed[DrawLn.CrntDot.Row + ChkRowIncr][DrawLn.CrntDot.Col + ChkColIncr] == 255) && 
                    ((DrawLn.CrntDot.Row < (ImageHeight - 1)) && (PixelsRed[DrawLn.CrntDot.Row + ChkRowIncr + ChkRowAdjIncr][DrawLn.CrntDot.Col + ChkColIncr + ChkColAdjIncr] == 255)) &&
                    ((DrawLn.CrntDot.Col < (ImageWidth - 1)) && (PixelsRed[DrawLn.CrntDot.Row + ChkRowIncr - ChkRowAdjIncr][DrawLn.CrntDot.Col + ChkColIncr - ChkColAdjIncr] == 255))
                )
            {
                PixelsRed[DrawLn.CrntDot.Row][DrawLn.CrntDot.Col] = 0;
                PixelsGreen[DrawLn.CrntDot.Row][DrawLn.CrntDot.Col] = 0;
                PixelsBlue[DrawLn.CrntDot.Row][DrawLn.CrntDot.Col] = 0;
            }

            DrawLn.getNxtDotByPixels(1);
        }
    }

    private void DecodeLtr(long LtrCode)
    {
        final String Direction[] = {"XX", "Top", "Right", "Left", "Bottom", "Bottom Left", "XX", "Top Right", "Top Left", "Bottom Right"};
        // final String Travel[] = {"XX", "Top", "Right", "Left", "Bottom", "Bottom Left", "XX", "Top Right", "Top Left", "Bottom Right"};
        int Digit;

        String CodedStr = Long.toString(LtrCode);

        System.out.println("\nThe Decoded Letter --> \n");

        for(int CodeDig = 0; CodeDig < CodedStr.length(); CodeDig++)
        {
            Digit = Integer.valueOf("" + CodedStr.charAt(CodeDig));
            System.out.println(Direction[Digit]);
        }
    }
    
    private int GetStartIndex()
    {
        int ClosestIndx[] = {0, 0, 0}, CenterIndex = TraceList.size() / 2;
        Dot CrntDot;
        float ClosestDist[] = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
        float ChkDist = 0.0F;

        for(int PixIndex = 0; PixIndex < TraceList.size(); PixIndex++)
        {
            CrntDot = TraceList.get(PixIndex);

            ChkDist = CrntDot.getDist(TopLeftDot);
            if(ChkDist <= ClosestDist[0])
            {
                if(ChkDist == ClosestDist[0])
                {
                    if(((PixIndex < CenterIndex) && (PixIndex < ClosestIndx[0])) || ((PixIndex > CenterIndex) && ((TraceList.size() - PixIndex) < ClosestIndx[0])))
                        ClosestIndx[0] = PixIndex;      
                }
                else
                {
                    ClosestIndx[0] = PixIndex;
                    ClosestDist[0] = ChkDist;
                }
    	    }
            
            ChkDist = CrntDot.getDist(TopEdgeMidDot);
            if(ChkDist <= ClosestDist[1])
            {
                if(ChkDist == ClosestDist[1])
                {
                    if(((PixIndex < CenterIndex) && (PixIndex < ClosestIndx[1])) || ((PixIndex > CenterIndex) && ((TraceList.size() - PixIndex) < ClosestIndx[1])))
                        ClosestIndx[1] = PixIndex;      
                }
                else
                {
                    ClosestIndx[1] = PixIndex;
                    ClosestDist[1] = ChkDist;
                }
            }
            
            ChkDist = CrntDot.getDist(TopRightDot);
            if(ChkDist <= ClosestDist[2])
            {
                if(ChkDist == ClosestDist[2])
                {
                    if(((PixIndex < CenterIndex) && (PixIndex < ClosestIndx[2])) || ((PixIndex > CenterIndex) && ((TraceList.size() - PixIndex) < ClosestIndx[2])))
                        ClosestIndx[2] = PixIndex;
                }
                else
                {
                    ClosestIndx[2] = PixIndex;
                    ClosestDist[2] = ChkDist;
                }
            }
        }

        int AnsIndx;

        float Chk20Prcnt = (ClosestDist[0]  + ClosestDist[2]) / 10;
        
        if((Math.abs(ClosestDist[0] - ClosestDist[2]) <= Chk20Prcnt))
            AnsIndx = ClosestIndx[0];
        else
            AnsIndx = ClosestIndx   [
                                        (ClosestDist[0] < ClosestDist[1])? 
                                            ((ClosestDist[0] < ClosestDist[2])? 0 : 2) : 
                                            ((ClosestDist[1] < ClosestDist[2])? 1 : 2)
                                    ];

        Dot AnsDot = TraceList.get(AnsIndx);
        float AnsDotAng = 500;
        int ChkIndxIncr = Math.round((float)TraceList.size() / 25);

        for(int Indx = 0; Indx < TraceList.size(); Indx++)
        {
            if(AnsDot.equals(TraceList.get(Indx)))
            {
                int ChkIndx = (Indx + ChkIndxIncr) % (TraceList.size() - 1);

                float ChkAng = AnsDot.getAngle(TraceList.get(ChkIndx));
                
                if(ChkAng < AnsDotAng)
                {
                    AnsIndx = Indx;
                    AnsDotAng = ChkAng;
                }
            }
        }

        return AnsIndx;
    }

    private long GetLtrCode()
    {
        int StIndex = GetStartIndex();
        
        int SampleValIncr = Math.round((float)TraceList.size() / 25);
        
        if(SampleValIncr == 0)  
            SampleValIncr = 1;

        Dot PrevDot = TraceList.get(StIndex);
        Dot CrntDot;
        int SampleVal;
        boolean ChkForBreak = false;
        int ConsecutiveBufCnt = 0;
        
        if((StIndex + SampleValIncr) >= TraceList.size())
        {
            ChkForBreak = true;
            SampleVal = StIndex + (SampleValIncr * 2) - TraceList.size();
            CrntDot = TraceList.get(StIndex + SampleValIncr - TraceList.size());
        }
        else if(StIndex + (SampleValIncr * 2) >= TraceList.size())
        {
            ChkForBreak = true;
            SampleVal = StIndex + (SampleValIncr * 2) - TraceList.size();
            CrntDot = TraceList.get(StIndex + SampleValIncr);
        }
        else
        {
            SampleVal = StIndex + (SampleValIncr * 2);
            CrntDot = TraceList.get(StIndex + SampleValIncr);
        }
        
        PixelsRed[PrevDot.Row - TopLeftDot.Row][PrevDot.Col - TopLeftDot.Col] = 254;
        PixelsRed[CrntDot.Row - TopLeftDot.Row][CrntDot.Col - TopLeftDot.Col] = 254;

        long Code = 0;
        int PrevTravVal = GetDirectionVal(PrevDot.getAngle(CrntDot)), PrevTravHits = 0;
        int StTravVal = PrevTravVal;
        int CrntTravVal = 0, CrntTravHits = 1;
        int BufrPrevVal = 0, InstntPrevTravVal = PrevTravVal, LatestBufrVal = 0, InstntPrevTravHits = 0;
        float CrntTravAng = 0.0F, BufrPrevAng = 0.0F, PrevTravAng = 0.0F, LatestBufrAng = 0.0F, ConsecBufrPrevAng = 0.0F, InstntPrevTravAng = 0.0F;
        boolean SetBufFrmCrntTrav, SetPrevValFrmCrntVal, PrevTravSkippedCuzOfBufOriginl = false, PrevTravSkippedCuzOfBufNew = false;

        while(true)
        {
            PixelsRed[PrevDot.Row - TopLeftDot.Row][PrevDot.Col - TopLeftDot.Col] = 254;

            PrevDot = CrntDot;
            CrntDot = TraceList.get(SampleVal);

            if(!(PrevDot.equals(CrntDot)))
            {
                SetBufFrmCrntTrav = true;
                SetPrevValFrmCrntVal = true;

                CrntTravAng = PrevDot.getAngle(CrntDot);

                CrntTravVal = GetDirectionVal(CrntTravAng);
                
                if(CrntTravVal == InstntPrevTravVal)
                    CrntTravHits++;
                else
                {
                    PrevTravHits = CrntTravHits;
                    CrntTravHits = 1;
                }
                
                if  (
                        ((Code % 10) != LatestBufrVal) && 
                        (CrntTravVal != InstntPrevTravVal) &&
                        (InstntPrevTravVal < BUFFER_TRAV) && 
                        (PrevTravVal < BUFFER_TRAV) && 
                        (LatestBufrVal >= BUFFER_TRAV)
                    )
                    {
                        if  (
                                (PrevTravHits == 1) &&
                                (PrevTravAng >= FIRST_CONSEC_BUFR_EQUIVALENTS[LatestBufrVal - BUFFER_TRAV]) &&
                                (PrevTravAng <= SECND_CONSEC_BUFR_EQUIVALENTS[LatestBufrVal - BUFFER_TRAV])
                            )
                        {
                            if(PrevTravSkippedCuzOfBufOriginl && ((Code % 10) != PrevTravVal))
                                Code = (Code * 10) + PrevTravVal;

                            Code = (Code * 10) + LatestBufrVal;
                            
                            ConsecutiveBufCnt = 0;
                            PrevTravVal = LatestBufrVal;
                            PrevTravAng = LatestBufrAng;
                            PrevTravHits = 2;
                            SetPrevValFrmCrntVal = false;
                        }
                        else
                        {
                            ConsecutiveBufCnt = 0;
                            LatestBufrVal = 0;
                            LatestBufrAng = 0;
                            SetBufFrmCrntTrav = false;
                        }

                        BufrPrevVal = 0;
                    }
                    
                if(!(PrevTravSkippedCuzOfBufNew && (CrntTravVal >= BUFFER_TRAV)))
                    PrevTravSkippedCuzOfBufNew = false;

                PrevTravSkippedCuzOfBufOriginl = false;

                if((CrntTravVal < BUFFER_TRAV) && (PrevTravVal < BUFFER_TRAV))
                {
                    if((CrntTravVal != PrevTravVal) && ((Code % 10) != PrevTravVal))
                        Code = (Code * 10) + PrevTravVal;
                    else
                    {
                        PrevTravSkippedCuzOfBufNew = true;
                        PrevTravSkippedCuzOfBufOriginl = true;
                    }

                    if  (
                            (BufrPrevVal >= BUFFER_TRAV) &&
                            (ConsecutiveBufCnt < 2) && 
                            (
                                (CrntTravVal == FIRST_NXT_DIR_BY_PREV_BUFF[BufrPrevVal - BUFFER_TRAV]) ||
                                (CrntTravVal == SECND_NXT_DIR_BY_PREV_BUFF[BufrPrevVal - BUFFER_TRAV])
                            )
                        )
                        if((Code % 10) != BUFFER_ANGLE_CHK[(int)(BufrPrevAng / 15)])
                            Code = (Code * 10) + BUFFER_ANGLE_CHK[(int)(BufrPrevAng / 15)];
                }
                else if((CrntTravVal >= BUFFER_TRAV) && (BufrPrevVal >= BUFFER_TRAV) && (CrntTravVal != BufrPrevVal))
                {
                    if(((Code % 10) != PrevTravVal) && (PrevTravVal < BUFFER_TRAV))
                        Code = (Code * 10) + PrevTravVal;

                    if((ConsecutiveBufCnt >= 2) && ((Code % 10) != BufrPrevVal))
                    {
                        Code = (Code * 10) + BufrPrevVal;

                        PrevTravVal = BufrPrevVal;
                        PrevTravAng = BufrPrevAng;
                        PrevTravHits = ConsecutiveBufCnt;
                        
                        LatestBufrVal = BufrPrevVal;
                        LatestBufrAng = BufrPrevAng;

                        ConsecutiveBufCnt = 0;
                        BufrPrevVal = 0;
                        BufrPrevAng = 0.0F;
                        ConsecBufrPrevAng = 0;
                    }
                    else
                    {
                        int BufDirCode = (BufrPrevVal * 10) + CrntTravVal;
                        int SetNxtDir = NO_TRAV;
                        
                        switch(BufDirCode)
                        {
                            case 97: case 58: 
                                SetNxtDir = (BOTTOM_TRAV * 10) + TOP_TRAV;
                                break;

                            case 59: case 87:
                                SetNxtDir = (LEFT_TRAV * 10) + RIGHT_TRAV;
                                break;
                            
                            case 79: case 85:
                                SetNxtDir = (TOP_TRAV * 10) + BOTTOM_TRAV;
                                break;

                            case 95: case 78:
                                SetNxtDir = (RIGHT_TRAV * 10) + LEFT_TRAV;
                                break;
                        }

                        if(SetNxtDir != 0)
                        {
                            float DupBufrPrevAng = BufrPrevAng;
                            
                            DupBufrPrevAng -= 180;
                            if(DupBufrPrevAng < 0)
                                DupBufrPrevAng += 360;

                            float ChkAng = Math.abs(CrntTravAng - DupBufrPrevAng);

                            if(ChkAng > 180)
                                ChkAng = 360 - ChkAng;

                            if(ChkAng < 90)
                            {
                                if((Code % 10) == (int)(SetNxtDir / 10))
                                    Code = (Code * 10) + (SetNxtDir % 10);
                                else
                                    Code = (Code * 100) + SetNxtDir;

                                LatestBufrVal = CrntTravVal;
                                LatestBufrAng = CrntTravAng;

                                PrevTravVal = CrntTravVal;
                                ConsecutiveBufCnt = 0;

                                // PrevTravVal = (int)(Code % 10);

                                BufrPrevVal = 0;
                                BufrPrevAng = 0.0F;
                                SetBufFrmCrntTrav = false;
                            }
                            else
                            {
                                PrevTravVal = BufrPrevVal;
                                PrevTravAng = BufrPrevAng;
                            }
                        }
                    }
                }

                if((ConsecutiveBufCnt >= 2) && ((Code % 10) != BufrPrevVal))
                {
                    if(PrevTravSkippedCuzOfBufOriginl && ((Code % 10) != PrevTravVal))
                        Code = (Code * 10) + PrevTravVal;

                    Code = (Code * 10) + BufrPrevVal;

                    PrevTravVal = BufrPrevVal;
                    PrevTravAng = BufrPrevAng;
                    PrevTravHits = ConsecutiveBufCnt;

                    LatestBufrVal = BufrPrevVal;
                    LatestBufrAng = BufrPrevAng;

                    ConsecBufrPrevAng = 0;
                    ConsecutiveBufCnt = 0;
                    BufrPrevVal = 0;
                    BufrPrevAng = 0.0F;
                    SetBufFrmCrntTrav = false;
                }
                
                if  
                (
                    ((Code % 10) != CrntTravVal) && 
                    (InstntPrevTravHits == 1) && 
                    (InstntPrevTravVal < BUFFER_TRAV) && 
                    (CrntTravVal >= BUFFER_TRAV) && 
                    (InstntPrevTravAng >= FIRST_CONSEC_BUFR_EQUIVALENTS[CrntTravVal - BUFFER_TRAV]) && 
                    (InstntPrevTravAng <= SECND_CONSEC_BUFR_EQUIVALENTS[CrntTravVal - BUFFER_TRAV])
                )
                {
                    if(PrevTravSkippedCuzOfBufNew && ((Code % 10) != InstntPrevTravVal))
                        Code = (Code * 10) + InstntPrevTravVal;

                    Code = (Code * 10) + CrntTravVal;
                    ConsecutiveBufCnt = 0;

                    PrevTravVal = CrntTravVal;
                    PrevTravAng = CrntTravAng;
                    PrevTravHits = CrntTravHits;

                    LatestBufrVal = CrntTravVal;
                    LatestBufrAng = CrntTravAng;

                    BufrPrevVal = 0;
                    BufrPrevAng = 0.0F;
                    SetBufFrmCrntTrav = false;
                }

                // if  
                // (
                //     ((Code % 10) != CrntTravVal) && 
                //     (PrevTravHits == 1) && 
                //     (PrevTravVal < BUFFER_TRAV) && 
                //     (CrntTravVal >= BUFFER_TRAV) && 
                //     (PrevTravAng >= FIRST_CONSEC_BUFR_EQUIVALENTS[CrntTravVal - BUFFER_TRAV]) && 
                //     (PrevTravAng <= SECND_CONSEC_BUFR_EQUIVALENTS[CrntTravVal - BUFFER_TRAV])
                // )
                // {
                //     if(PrevTravSkippedCuzOfBufNew && ((Code % 10) != PrevTravVal))
                //         Code = (Code * 10) + PrevTravVal;

                //     Code = (Code * 10) + CrntTravVal;
                //     ConsecutiveBufCnt = 0;

                //     PrevTravVal = CrntTravVal;
                //     PrevTravAng = CrntTravAng;
                //     PrevTravHits = CrntTravHits;

                //     LatestBufrVal = CrntTravVal;
                //     LatestBufrAng = CrntTravAng;

                //     BufrPrevVal = 0;
                //     BufrPrevAng = 0.0F;
                //     SetBufFrmCrntTrav = false;
                // }

                if(CrntTravVal < BUFFER_TRAV)
                {
                    // if  (
                    //         (BufrPrevVal >= BUFFER_TRAV) && 
                    //         (ConsecutiveBufCnt == 1) &&
                    //         (CrntTravAng >= FIRST_CONSEC_BUFR_EQUIVALENTS[BufrPrevVal - BUFFER_TRAV]) &&
                    //         (CrntTravAng <= SECND_CONSEC_BUFR_EQUIVALENTS[BufrPrevVal - BUFFER_TRAV])
                    //     )
                    //     {
                    //         Code = (Code * 10) + BufrPrevVal;
                    //         ConsecutiveBufCnt = 0;

                    //         PrevTravVal = BufrPrevVal;
                    //         PrevTravAng = BufrPrevAng;
                    //         PrevTravHits = 2;

                    //         BufrPrevVal = 0;
                    //     }
                    // else
                    {
                        if(SetPrevValFrmCrntVal)
                        {
                            PrevTravVal = CrntTravVal;
                            PrevTravAng = CrntTravAng;
                            PrevTravHits = CrntTravHits;
                        }

                        ConsecBufrPrevAng = 0;
                        BufrPrevVal = 0;
                        ConsecutiveBufCnt = 0;
                        BufrPrevAng = 0.0F;
                    }
                }
                else
                {
                    if(ConsecBufrPrevAng == 0)
                        ConsecutiveBufCnt = 1;
                    else if(ConsecBufrPrevAng == CrntTravVal)
                        ConsecutiveBufCnt++;
                    else
                        ConsecutiveBufCnt = 0;

                    ConsecBufrPrevAng = CrntTravVal;

                    if(SetBufFrmCrntTrav)
                    {
                        LatestBufrVal = CrntTravVal;
                        BufrPrevVal = CrntTravVal;

                        LatestBufrAng = CrntTravAng;
                        BufrPrevAng = CrntTravAng;
                    }
                }
                
                InstntPrevTravVal = CrntTravVal;
                InstntPrevTravAng = CrntTravAng;
                InstntPrevTravHits = CrntTravHits;
            }

            SampleVal += SampleValIncr;

            if((SampleVal >= TraceList.size()) && ChkForBreak)
                break;

            if(SampleVal >= TraceList.size())
            {
                SampleVal = SampleVal - TraceList.size();
                ChkForBreak = true;
            }
            
            if((SampleVal >= (StIndex + SampleValIncr)) && ChkForBreak)
                break;
        }

        if((PrevTravVal < BUFFER_TRAV) && ((Code % 10) != PrevTravVal))
            Code = (Code * 10) + PrevTravVal;
        
        if((CrntTravVal < BUFFER_TRAV) && ((Code % 10) != CrntTravVal))
            Code = (Code * 10) + CrntTravVal;

        PixelsRed[PrevDot.Row - TopLeftDot.Row][PrevDot.Col - TopLeftDot.Col] = 254;
        PixelsRed[CrntDot.Row - TopLeftDot.Row][CrntDot.Col - TopLeftDot.Col] = 254;

        PixelsGreen[TraceList.get(StIndex).Row - TopLeftDot.Row][TraceList.get(StIndex).Col - TopLeftDot.Col] = 254;

        if((Code % 10) == StTravVal)
            Code /= 10;

        return Code;

        // TODO : In the loop above also record the hits each direction gets and if in
        // between two complementary directions (eg -->  Top x Bottom) if a third direction
        // appears of low hits then remove the direction coming in between.
    }

    public int GetDirectionVal(float Ang)
    {
        if(Ang <=  30) return TOP_TRAV;
        if(Ang <   60) return TOP_RIGHT_BUF;
        if(Ang <= 120) return RIGHT_TRAV;
        if(Ang <  150) return BOTTOM_RIGHT_BUF;
        if(Ang <= 210) return BOTTOM_TRAV;
        if(Ang <  240) return BOTTOM_LEFT_BUF;
        if(Ang <= 300) return LEFT_TRAV;
        if(Ang <  330) return TOP_LEFT_BUF;

        return TOP_TRAV;
    }
}