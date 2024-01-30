import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import java.awt.image.DataBufferByte;

/**
 * This class extracts the letter out of a Quadrilateral.
 * @author Rhugved Pankaj Chaudhari
 */

public class ExtractLetters
{
    // CONSTANT DECLARATION ============================================================================================================================

    public static final int RED   = 0x0000FF;
    public static final int GREEN = 0x00FF00;
    public static final int BLUE  = 0xFF0000;

    public static final int NO_DOT = 0;
    public static final int ADJ_DOT_1 = 1;
    public static final int ADJ_DOT_3 = 2;

    public static final boolean PRINT_LINE = true;
    public static final boolean PRINT_DOT = false;

    public static final int LTR_COL_CNT = 6;
    public static final int LINE_PNT_CNT = 15;

    public static final int DERATE_FACT = 30;
    public static final int DIV_ROW_COL = 7;
    public static final int DOMINATION_DIFF = 2;

    public static final boolean REPLACE = true;
    public static final boolean NO_REPLACE = false;

    public static final int CHK_CONSECUTIVE_PIX[][] = {{-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}};
    public static final int ROW = 0;
    public static final int COL = 1;

    public static final int PATH_NOT_PROCESSED = 0;
    public static final int PATH_PROCESSED = 1;

    public static final float DOT_DIST = (float)1 / 4;

    // CONSTANT DECLARATION END ============================================================================================================================

    public void FontMain(ReadImage Text) throws IOException, PixelDistOutOfBoundsException, WrongTravelInitializerException
    {
        long StTime = System.currentTimeMillis();

        ReadImage Font = new ReadImage();
        Language English = new Language(26, LTR_COL_CNT, LINE_PNT_CNT);

        String ImageName = "Font.bmp";
        // String ImageName = "CapitalFont1.bmp";
        // String ImageName = "FontData1.bmp";
        // String ImageName = "FontData2.bmp";
        // String ImageName = "FontData3.bmp";
        
        String ImagePath = "./Images/" + ImageName;

        // INITIALISATION ==================================================================================================================================

        File ImageFile = new File(ImagePath);
        
        Font.OriginalImage = ImageIO.read(ImageFile);
        
        Font.PixelData = ((DataBufferByte) Font.OriginalImage.getRaster().getDataBuffer()).getData();
        Font.hasAlphaChannel = Font.OriginalImage.getAlphaRaster() != null;
        
        // Font.getWidth() = Font.getWidth();
        // Font.getHeight() = Font.getHeight();
    
        Font.PixelsRed = new int[Font.getHeight()][Font.getWidth()];
        Font.PixelsGreen = new int[Font.getHeight()][Font.getWidth()];
        Font.PixelsBlue = new int[Font.getHeight()][Font.getWidth()];
        
        // EXECUTION ===================================================================================================================

        Font.ConvertArray_1Dto2D(Font);
        
        Font.EliminateColors        (Font);
        Font.ExtractImageCorners    (Font, ImageName, true);
        Font.ExtractLines           (Font, false);
        Font.ExtractWordsLetters    (Font, false);
        Font.ExtractBorders         (Font, true);

        long Time1 = System.currentTimeMillis();

        System.out.println("Time Taken 'Font Image Segmentation' -->>" + (Time1 - StTime) + " mS\n");

        ExtractFontLetters(Font, English);

        // LineTest(Font);

        long Time2 = System.currentTimeMillis();

        Font.ConvertArray_2Dto1D(Font, Font.FinalImageWhite);
        
        PrintOnlyPoints(Font);

        Font.ConvertArray_2Dto1D(Font, Font.FinalImageBlack);

        System.out.println("\n\nTime Taken 'FONT LETTER EXTRACTION' -->> " + (Time2 - Time1) + " mS\n");

        long Time3 = System.currentTimeMillis();

        // ImageToText(Text, English, "Image Paragraph.txt");

        System.out.println("Time Taken 'TEXT LETTER DETECT' -->> " + (System.currentTimeMillis() - Time3) + " mS\n");

        Text.ConvertArray_2Dto1D(Text, "Final_Para_Wh.bmp");

        PrintOnlyPoints(Text);

        Text.ConvertArray_2Dto1D(Text, "Final_Para_Blk.bmp");

        // EXECUTION END ===============================================================================================================
    }
    
    public void ExtractFontLetters(ReadImage riEPNob, Language FontLang) throws PixelDistOutOfBoundsException, WrongTravelInitializerException, IOException
    {
        FileWriter LtrInfoFile = new FileWriter("LtrInfoFile.csv");

        WriteFileHeader(LtrInfoFile);

        for(int Row = 0; Row < 2; Row++)
        {
            for(int Col = 0; Col < 13; Col++)
            {
                int Letter = (Row * 13) + Col;
                
                long TimeSt = System.nanoTime();
                
                // if(Letter != 6)
                    ExtractLetter(riEPNob, riEPNob.iLetter[Row][Col][1], FontLang.oLetter[Letter]);
                
                WriteLtrInfo(FontLang.oLetter[Letter], LtrInfoFile, Letter);

                String Time = Double.toString((System.nanoTime() - TimeSt) / (double)1000000) + "mS";

                int Stop = 9;
            }
        }

        WriteFileFooter(LtrInfoFile);

        // WritePathInfo(FontLang);
        // WriteLtrInfo(FontLang);
    }

    public void ImageToText(ReadImage Text, Language FontLanguage, String OutFileName) throws PixelDistOutOfBoundsException, WrongTravelInitializerException, IOException
    {
        FileWriter TextLtrInfoFile = new FileWriter("TextLtrInfoFile.csv");

        WriteFileHeader(TextLtrInfoFile);
        
        String Paragraph = "" ;

        int LtrNo = -1;

        for(int Line = 0; Line < Text.LineCnt; Line++)
        {
            for(int Word = 0; Word < Text.LineWordsCnt[Line]; Word++)
            {
                for(int Ltr = 1; Ltr < Text.WordsLettersCnt[Line][Word]; Ltr++)
                {
                    LtrNo++;

                    Letter TextLtr = new Letter(LTR_COL_CNT, LINE_PNT_CNT);

                    long TimeSt = System.nanoTime();

                    char TextChar;

                    try
                    {
                        ExtractLetter(Text, Text.iLetter[Line][Word][Ltr], TextLtr);

                        WriteLtrInfo(TextLtr, TextLtrInfoFile, LtrNo);

                        TextChar = FontLanguage.getEnglishChar(FontLanguage.LetterDetect(TextLtr));
                    }
                    catch(Exception e)
                    {
                        WriteLtrInfo(TextLtr, TextLtrInfoFile, LtrNo);
                        TextChar = 0;
                        // throw e;
                    }

                    String Time = Double.toString((System.nanoTime() - TimeSt) / (double)1000000) + "mS";

                    Paragraph += TextChar;

                    int Stop = 9;
                }

                if(Word < (Text.LineWordsCnt[Line] - 1))
                    Paragraph += " ";
            }
            
            Paragraph += "\n";
        }

        WriteFileFooter(TextLtrInfoFile);

        FileWriter TextFile = new FileWriter(OutFileName);    

        TextFile.write(Paragraph);

        TextFile.close();
    }

    public void ExtractLetter(ReadImage riELob, int LtrCor[][], Letter GetLtr) throws PixelDistOutOfBoundsException, WrongTravelInitializerException
    {
        FillWhCones(riELob, LtrCor);

        RemoveBluePixels     (riELob, LtrCor);

        ExtractPoints        (riELob, LtrCor, GetLtr, false);
        
        RemoveBluePixels     (riELob, LtrCor);

        ExtractAdjacentPoints(riELob, GetLtr, PRINT_DOT, false, NO_DOT);
        
        ReducePathPrecision  (riELob, GetLtr, PRINT_LINE, false, NO_DOT, 60);
        
        DeleteUnwantedPath   (riELob, GetLtr, PRINT_DOT, false, ADJ_DOT_3, 5);
        
        PutJunctionPoints    (riELob, GetLtr, PRINT_DOT, false, NO_DOT);

        // The 'Processed' boolean of each point is set to false in DeleteComnJunctions.

        DeleteComnJunctions  (riELob, GetLtr, PRINT_LINE, true, ADJ_DOT_3);
        
        RemoveBluePixels     (riELob, LtrCor);

        ExtractLetterSpecs   (riELob, GetLtr, LtrCor, 5, DERATE_FACT, DIV_ROW_COL);

        // DecodeLtr            (riELob, GetLtr, LtrCor, DERATE_FACT, DOMINATION_DIFF, DIV_ROW_COL);

        GetLtrInfo           (riELob, GetLtr, LtrCor);
        int Stop = 9;
    }

    public void GetLtrInfo(ReadImage riGLIob, Letter Ltr, int LtrCor[][])
    {
        int LtHeight = 1 + (((riGLIob.Dist(LtrCor[0][0], LtrCor[0][1],  LtrCor[3][0], LtrCor[3][1])) + (riGLIob.Dist(LtrCor[1][0], LtrCor[1][1],  LtrCor[2][0], LtrCor[2][1]))) / 2);
        int LtWidth = 1 + (((riGLIob.Dist(LtrCor[0][0], LtrCor[0][1],  LtrCor[1][0], LtrCor[1][1])) + (riGLIob.Dist(LtrCor[3][0], LtrCor[3][1],  LtrCor[2][0], LtrCor[2][1]))) / 2);

        Path RefPath, ChkPath;
        Point JuncPnt;
        final int CORNER_DETECT_ANG = 135;

        for(int Col = 0; Col < Ltr.Point.size(); Col++)
        {
            for(int Pnt = 0; Pnt < Ltr.Point.get(Col).size(); Pnt++)
            {
                if(Ltr.getPoint(Col, Pnt).Adjacent.size() > 2)
                {
                    JuncPnt = Ltr.getPoint(Col, Pnt);
                    List <Path> JuncPath = new ArrayList <Path> ();
                    
                    for(int Pth = 0; Pth < Ltr.LtPath.size(); Pth++)
                    {
                        if((Ltr.LtPath.get(Pth).StPnt.equals(JuncPnt)) && (Ltr.LtPath.get(Pth).EndPnt.equals(JuncPnt)))
                        {
                            Ltr.LtrInfo.add(Letter.CLOSED_LOOP);

                            int SamePathIDCnt[] = new int[Ltr.LtPath.size()];

                            Ltr.LtPath.get(Pth).Processed = PATH_PROCESSED;

                            for(int Adj = 0; Adj < JuncPnt.Adjacent.size(); Adj++)
                            {
                                SamePathIDCnt[JuncPnt.Adjacent.get(Adj).PathID]++;
                                
                                if(SamePathIDCnt[JuncPnt.Adjacent.get(Adj).PathID] == 2)
                                {
                                    for(int FillAdj = 0; FillAdj < JuncPnt.Adjacent.size(); FillAdj++)
                                    {
                                        if(JuncPnt.Adjacent.get(FillAdj).PathID == JuncPnt.Adjacent.get(Adj).PathID)
                                        {
                                            int Index;
                                            float ChkAng = Math.round(JuncPnt.getAngle(JuncPnt.Adjacent.get(FillAdj)) / DERATE_FACT);

                                            for(Index = 0; Index < JuncPath.size(); Index++)
                                                if(JuncPath.get(Index).TwoEndsAngle > ChkAng)
                                                    break;

                                            JuncPath.add(Index, new Path(JuncPnt, JuncPnt.Adjacent.get(FillAdj), 0, 0, 0, 0, ChkAng));
                                            
                                            JuncPath.get(Index).PosDot.set
                                            (
                                                Math.round(((((float)JuncPnt.Row + JuncPnt.Adjacent.get(FillAdj).Row) / 2) - LtrCor[0][0]) * DIV_ROW_COL / LtHeight), 
                                                Math.round(((((float)JuncPnt.Col + JuncPnt.Adjacent.get(FillAdj).Col) / 2) - LtrCor[0][1]) * DIV_ROW_COL / LtWidth)
                                            );
                                        }
                                    }
                                }
                            }
                        }
                        else if(Ltr.LtPath.get(Pth).StPnt.equals(JuncPnt))
                        {
                            int Index;

                            for(Index = 0; Index < JuncPath.size(); Index++)
                                if(JuncPath.get(Index).TwoEndsAngle > Ltr.LtPath.get(Pth).TwoEndsAngle)
                                    break;

                            Ltr.LtPath.get(Pth).Processed = PATH_PROCESSED;
                            JuncPath.add(Index, Ltr.LtPath.get(Pth));
                        }
                    }

                    // for(int PathIndex = 0; PathIndex < JuncPath.size(); PathIndex++)
                    // {
                    //     int ChkPathIndex = (PathIndex + 1) % JuncPath.size();

                    //     Path RefPath = JuncPath.get(PathIndex);
                    //     Path ChkPath = JuncPath.get(ChkPathIndex);

                    //     if(RefPath.EndPnt.PathID == ChkPath.EndPnt.PathID)
                    //         continue;

                    //     int AvgAng = (int)(((RefPath.TwoEndsAngle + ChkPath.TwoEndsAngle) / 2) * DERATE_FACT);
                    //     int AngDiff = (int)(ChkPath.TwoEndsAngle - RefPath.TwoEndsAngle);
                    //     int EndsAng = Math.round(RefPath.EndPnt.getAngle(ChkPath.EndPnt) / 45);

                    //     float Angu = RefPath.EndPnt.getAngle(ChkPath.EndPnt);

                    //     if(EndsAng == 8) 
                    //         EndsAng = 0;

                    //     if(ChkPathIndex == 0)
                    //     {
                    //         AvgAng = (int)(((RefPath.TwoEndsAngle + 12 + ChkPath.TwoEndsAngle) / 2) * DERATE_FACT);
                    //         AngDiff = AngDiff + 12;
                    //     }

                    //     AvgAng = Math.round(AvgAng / (360 / 8));

                    //     if(AvgAng == 8) 
                    //         AvgAng = 0;

                    //     if(AngDiff < STRAIGHT_PATH_ANG)
                    //     {
                    //         if((RefPath.TotDeviation == 0) && (ChkPath.TotDeviation == 0))
                    //         {
                    //             // Method 1 =================================================================================================

                    //             if((RefPath.TwoEndsAngle == 0) || (ChkPath.TwoEndsAngle == 3))
                    //                 Ltr.LtrInfo.add(Letter.TOP_RIGHT_OPEN);
                    //             else if((RefPath.TwoEndsAngle == 3) || (ChkPath.TwoEndsAngle == 6))
                    //                 Ltr.LtrInfo.add(Letter.BOTTOM_RIGHT_OPEN);
                    //             else if((RefPath.TwoEndsAngle == 6) || (ChkPath.TwoEndsAngle == 9))
                    //                 Ltr.LtrInfo.add(Letter.BOTTOM_LEFT_OPEN);
                    //             else if((RefPath.TwoEndsAngle == 9) || (ChkPath.TwoEndsAngle == 0))
                    //                 Ltr.LtrInfo.add(Letter.TOP_LEFT_OPEN);
                    //             else if(AvgAng == 0)
                    //                 Ltr.LtrInfo.add(Letter.TOP_OPEN);
                    //             else if(AvgAng == 2)
                    //                 Ltr.LtrInfo.add(Letter.RIGHT_OPEN);
                    //             else if(AvgAng == 4)
                    //                 Ltr.LtrInfo.add(Letter.BOTTOM_OPEN);
                    //             else if(AvgAng == 6)
                    //                 Ltr.LtrInfo.add(Letter.LEFT_OPEN);
                    //         }
                    //         else
                    //         {
                    //             // Method 2 =================================================================================================

                    //             if((EndsAng == 0) || (EndsAng == 4))
                    //             {
                    //                 if(AvgAng < 4)
                    //                     Ltr.LtrInfo.add(Letter.RIGHT_OPEN);
                    //                 else
                    //                     Ltr.LtrInfo.add(Letter.LEFT_OPEN);
                    //             }
                    //             else if((EndsAng == 2) || (EndsAng == 6))
                    //             {
                    //                 if((AvgAng < 2) || (AvgAng > 6))
                    //                     Ltr.LtrInfo.add(Letter.TOP_OPEN);
                    //                 else 
                    //                     Ltr.LtrInfo.add(Letter.BOTTOM_OPEN);
                    //             }
                    //             else
                    //             {
                    //                 Point ChkPnt = new Point((RefPath.EndPnt.Row + ChkPath.EndPnt.Row) / 2, (RefPath.EndPnt.Col + ChkPath.EndPnt.Col) / 2);
                    //                 int ChkAng = Math.round(ChkPnt.getAngle(RefPath.StPnt) / 45);
                                    
                    //                 if((EndsAng == 1) || (EndsAng == 5))
                    //                 {
                    //                     if((ChkAng == 2) || (ChkAng == 3) || (ChkAng == 4))
                    //                         Ltr.LtrInfo.add(Letter.TOP_LEFT_OPEN);
                    //                     else if((ChkAng == 0) || (ChkAng == 6) || (ChkAng == 7))
                    //                         Ltr.LtrInfo.add(Letter.BOTTOM_RIGHT_OPEN);
                    //                 }
                    //                 else if((EndsAng == 3) || (EndsAng == 7))
                    //                 {
                    //                     if((ChkAng == 0) || (ChkAng == 1) || (ChkAng == 2))
                    //                         Ltr.LtrInfo.add(Letter.BOTTOM_LEFT_OPEN);
                    //                     else if((ChkAng == 4) || (ChkAng == 5) || (ChkAng == 6)) 
                    //                         Ltr.LtrInfo.add(Letter.TOP_RIGHT_OPEN);
                    //                 }
                    //             }
                    //         } 
                    //     }
                    // }

                    for(int PathIndex = 0; PathIndex < JuncPath.size(); PathIndex++)
                    {
                        int ChkPathIndex = (PathIndex + 1) % JuncPath.size();

                        RefPath = JuncPath.get(PathIndex);
                        ChkPath = JuncPath.get(ChkPathIndex);

                        if(RefPath.EndPnt.PathID == ChkPath.EndPnt.PathID)
                            continue;

                        AssignLtrInfo(Ltr, RefPath, ChkPath, JuncPnt);
                    }
                }
            }
        }

        for(int Path = 0; Path < Ltr.LtPath.size(); Path++)
        {
            Path LtrPath = Ltr.LtPath.get(Path);

            int EndsAng = (int)Math.round((LtrPath.TwoEndsAngle * DERATE_FACT) / 45);
            int ChkDev = (int)(Math.abs(LtrPath.TotDeviation) * DERATE_FACT);

            if(EndsAng == 8)
                EndsAng = 0;

            if(LtrPath.Processed == PATH_NOT_PROCESSED)
            {
                if(LtrPath.TotDeviation == 0)
                {
                    if(LtrPath.StPnt.getDist(LtrPath.EndPnt) < (LtHeight * DOT_DIST))
                        Ltr.LtrInfo.add(Letter.DOT);
                    else if((EndsAng == 0) || (EndsAng == 4))
                        Ltr.LtrInfo.add(Letter.VERTICAL_LINE);
                    else if((EndsAng == 2) || (EndsAng == 6))
                        Ltr.LtrInfo.add(Letter.HORIZONTAL_LINE);
                    else if((EndsAng == 1) || (EndsAng == 5))
                        Ltr.LtrInfo.add(Letter.RIGHT_SLANT_LINE);
                    else if((EndsAng == 7) || (EndsAng == 7))
                        Ltr.LtrInfo.add(Letter.LEFT_SLANT_LINE);
                }
                else if(LtrPath.StPnt.equals(LtrPath.EndPnt) && EndsAng == 0)
                    Ltr.LtrInfo.add(Letter.CLOSED_LOOP);
                else
                {
                    JuncPnt = LtrPath.PathPnts.get(((int)((LtrPath.PathPnts.size() + 1) / 2)) - 1);

                    float AngDiff = Math.abs(JuncPnt.getAngle(LtrPath.StPnt) - JuncPnt.getAngle(LtrPath.EndPnt));

                    int AvgAng;
        
                    if(AngDiff > 180)
                        AvgAng = (int)((JuncPnt.getAngle(LtrPath.StPnt) + JuncPnt.getAngle(LtrPath.EndPnt) + 360) / 2);
                    else
                        AvgAng = (int)((JuncPnt.getAngle(LtrPath.StPnt) + JuncPnt.getAngle(LtrPath.EndPnt)) / 2);
                    
                    AvgAng = Math.round((float)AvgAng / 45);

                    if(AvgAng == 8)
                        AvgAng = 0;

                    if((AvgAng == 0) && (ChkDev < CORNER_DETECT_ANG))
                        Ltr.LtrInfo.add(Letter.TOP_OPEN);
                    else if((AvgAng == 2) && (ChkDev < CORNER_DETECT_ANG))
                        Ltr.LtrInfo.add(Letter.RIGHT_OPEN);
                    else if((AvgAng == 4) && (ChkDev < CORNER_DETECT_ANG))
                        Ltr.LtrInfo.add(Letter.BOTTOM_OPEN);
                    else if((AvgAng == 6) && (ChkDev < CORNER_DETECT_ANG))
                        Ltr.LtrInfo.add(Letter.LEFT_OPEN);
                    else if(ChkDev < CORNER_DETECT_ANG)
                    {
                        switch(AvgAng)
                        {
                            case 0: case 1:
                            {Ltr.LtrInfo.add(Letter.TOP_RIGHT_OPEN); break;}
                            case 2: case 3: 
                            {Ltr.LtrInfo.add(Letter.BOTTOM_RIGHT_OPEN); break;}
                            case 4: case 5: 
                            {Ltr.LtrInfo.add(Letter.BOTTOM_LEFT_OPEN); break;}
                            case 6: case 7: 
                            {Ltr.LtrInfo.add(Letter.TOP_LEFT_OPEN); break;}
                        }
                    }    
                    else
                    {
                        Point ChkPnt = new Point((LtrPath.StPnt.Row + LtrPath.EndPnt.Row) / 2, (LtrPath.StPnt.Col + LtrPath.EndPnt.Col) / 2);
            
                        int ChkAng = Math.round(JuncPnt.getAngle(ChkPnt) / 45);

                        if(ChkAng == 8)
                            ChkAng = 0;

                        if((EndsAng == 0) || (EndsAng == 4))
                        {
                            if(ChkAng < 4)
                                Ltr.LtrInfo.add(Letter.RIGHT_OPEN);
                            else
                                Ltr.LtrInfo.add(Letter.LEFT_OPEN);
                        }
                        else if((EndsAng == 2) || (EndsAng == 6))
                        {
                            if((ChkAng < 2) || (ChkAng > 6))
                                Ltr.LtrInfo.add(Letter.TOP_OPEN);
                            else
                                Ltr.LtrInfo.add(Letter.BOTTOM_OPEN);
                        }
                        else
                        {
                            switch(ChkAng)
                            {
                                case 0: case 7: 
                                {Ltr.LtrInfo.add(Letter.TOP_OPEN); break;}
                                case 1: case 2: 
                                {Ltr.LtrInfo.add(Letter.RIGHT_OPEN); break;}
                                case 3: case 4: 
                                {Ltr.LtrInfo.add(Letter.BOTTOM_OPEN); break;}
                                case 5: case 6: 
                                {Ltr.LtrInfo.add(Letter.LEFT_OPEN); break;}
                            }
                        }
                    }
                }

                if((Ltr.LtrInfo.size() == 1) && (Ltr.LtrInfo.get(0) == Letter.TOP_OPEN) && (ChkDev == 180))
                    Ltr.LtrInfo.set(0, Letter.TOP_U);                    

                // if((ChkDev > CORNER_DETECT_ANG) || ((ChkAng % 2 == 0) && (ChkDev != 90)))
                // {
                //     if(((LtrPath.PositiveDev > 0) && ((ChkAng == 0) || (ChkAng == 1))) || ((LtrPath.NegativeDev > 0) && ((ChkAng == 4) || (ChkAng == 5))))
                //         Ltr.LtrInfo.add(Letter.RIGHT_OPEN);
                //     else if(((LtrPath.PositiveDev > 0) && ((ChkAng == 4) || (ChkAng == 5))) || ((LtrPath.NegativeDev > 0) && ((ChkAng == 0) || (ChkAng == 1))))
                //         Ltr.LtrInfo.add(Letter.LEFT_OPEN);
                //     else if(((LtrPath.PositiveDev > 0) && ((ChkAng == 2) || (ChkAng == 3))) || ((LtrPath.NegativeDev > 0) && ((ChkAng == 6) || (ChkAng == 7))))
                //         Ltr.LtrInfo.add(Letter.BOTTOM_OPEN);
                //     else if(((LtrPath.PositiveDev > 0) && ((ChkAng == 6) || (ChkAng == 7))) || ((LtrPath.NegativeDev > 0) && ((ChkAng == 2) || (ChkAng == 3))))
                //         Ltr.LtrInfo.add(Letter.TOP_OPEN);
                // }   
                // else
                // {
                //     if(((LtrPath.PositiveDev > 0) && ((ChkAng == 6) || (ChkAng == 7))) || ((LtrPath.NegativeDev > 0) && ((ChkAng == 2) || (ChkAng == 3))))
                //         Ltr.LtrInfo.add(Letter.TOP_RIGHT_OPEN);
                //     else if(((LtrPath.NegativeDev > 0) && ((ChkAng == 6) || (ChkAng == 7))) || ((LtrPath.PositiveDev > 0) && ((ChkAng == 2) || (ChkAng == 3))))
                //         Ltr.LtrInfo.add(Letter.BOTTOM_LEFT_OPEN);
                //     else if(((LtrPath.PositiveDev > 0) && ((ChkAng == 0) || (ChkAng == 1))) || ((LtrPath.NegativeDev > 0) && ((ChkAng == 4) || (ChkAng == 5))))
                //         Ltr.LtrInfo.add(Letter.BOTTOM_RIGHT_OPEN);
                //     else if(((LtrPath.NegativeDev > 0) && ((ChkAng == 0) || (ChkAng == 1))) || ((LtrPath.PositiveDev > 0) && ((ChkAng == 4) || (ChkAng == 5))))
                //         Ltr.LtrInfo.add(Letter.TOP_LEFT_OPEN);
                // }
            }
        }

        RefPath = null;
        ChkPath = null;
        JuncPnt = null;
    }

    public void AssignLtrInfo(Letter Ltr, Path RefPath, Path ChkPath, Point JuncPnt)
    {
        final int STRAIGHT_PATH_ANG = 150 / DERATE_FACT;
        final int CORNER_DETECT_ANG = 135;

        Path StPath = RefPath, EndPath = ChkPath;
        boolean isStPathEndAngMin = true;

        if((StPath.TotDeviation != 0) && (EndPath.TotDeviation == 0))
        {
            StPath = ChkPath;
            EndPath = RefPath;
            isStPathEndAngMin = false;
        }

        float PrevAng = StPath.EndPnt.getAngle(StPath.StPnt), CrntAng = 0, Deviation;
        Point SameIDPnt[] = new Point[2];
        int SameIDPathCnt = 0, AngDiffPnt = 0;

        if(EndPath.TotDeviation != 0)
        {
            for(int Adj = 0; Adj < JuncPnt.Adjacent.size(); Adj++)
            {
                if(JuncPnt.Adjacent.get(Adj).PathID == EndPath.EndPnt.PathID)
                {
                    SameIDPnt[SameIDPathCnt] = JuncPnt.Adjacent.get(Adj);
                    SameIDPathCnt++;
                }
            }
        }
        else
        {
            SameIDPnt[SameIDPathCnt] = EndPath.EndPnt;
            SameIDPathCnt = 1;
        }

        if(SameIDPathCnt == 1)
        {
            CrntAng = JuncPnt.getAngle(SameIDPnt[0]);
            
            Deviation = CrntAng - PrevAng;

            if(Deviation > 180)
                Deviation -= 360;
        }
        else
        {
            CrntAng = JuncPnt.getAngle(SameIDPnt[0]);

            Deviation = CrntAng - PrevAng;

            if(Deviation > 180)
                Deviation -= 360;

            CrntAng = JuncPnt.getAngle(SameIDPnt[1]);

            float TempDev = (CrntAng - PrevAng);

            if(TempDev > 180)
                TempDev -= 360;

            if(TempDev < Deviation)
            {
                Deviation = TempDev;
                AngDiffPnt = 1;
            }
        }

        boolean isDevNeg = false;

        if(Deviation < 0)
            isDevNeg = true;
            
        if((Deviation < 0) && EndPath.TotDeviation < 0)
            Deviation = (Deviation * -1) + (EndPath.NegativeDev * DERATE_FACT);
        else if((Deviation > 0) && EndPath.TotDeviation > 0)
            Deviation += EndPath.PositiveDev * DERATE_FACT;

        Deviation = Math.abs(Deviation);

        int AvgAng = (int)(((StPath.TwoEndsAngle * DERATE_FACT) + JuncPnt.getAngle(SameIDPnt[AngDiffPnt])) / 2);
        
        int AngDiff;

        if(isStPathEndAngMin)
        {
            if((isDevNeg && (EndPath.NegativeDev > 0)) || ((!isDevNeg) && (EndPath.PositiveDev > 0)))
                AngDiff = (int)(EndPath.TwoEndsAngle - StPath.TwoEndsAngle);
            else
                AngDiff = (int)((JuncPnt.getAngle(SameIDPnt[AngDiffPnt]) / DERATE_FACT) - StPath.TwoEndsAngle);
        }
        else
        {
            if((isDevNeg && (EndPath.NegativeDev > 0)) || ((!isDevNeg) && (EndPath.PositiveDev > 0)))
                AngDiff = (int)(StPath.TwoEndsAngle - EndPath.TwoEndsAngle);
            else
                AngDiff = (int)(StPath.TwoEndsAngle - (JuncPnt.getAngle(SameIDPnt[AngDiffPnt]) / DERATE_FACT));
        }

        if(ChkPath.TwoEndsAngle < RefPath.TwoEndsAngle)
        {
            AvgAng = (int)(((RefPath.TwoEndsAngle + 12 + ChkPath.TwoEndsAngle) / 2) * DERATE_FACT);
            AngDiff = AngDiff + 12;
        }

        AvgAng = Math.round(((float)AvgAng) / (360 / 8));

        if(AvgAng == 8)
            AvgAng = 0;

        if(AngDiff < STRAIGHT_PATH_ANG)
            AssignOpeningInfo(Ltr, RefPath, ChkPath, StPath, EndPath, SameIDPnt, JuncPnt, Deviation, isDevNeg, CORNER_DETECT_ANG, AvgAng, AngDiffPnt);        
    }

    public void AssignOpeningInfo(Letter Ltr, Path RefPath, Path ChkPath, Path StPath, Path EndPath, Point SameIDPnt[], Point JuncPnt, float Deviation, boolean isDevNeg, int CORNER_DETECT_ANG, int AvgAng, int AngDiffPnt)
    {   
        if((AvgAng == 0) && (Deviation < CORNER_DETECT_ANG))
            Ltr.LtrInfo.add(Letter.TOP_OPEN);
        else if((AvgAng == 2) && (Deviation < CORNER_DETECT_ANG))
            Ltr.LtrInfo.add(Letter.RIGHT_OPEN);
        else if((AvgAng == 4) && (Deviation < CORNER_DETECT_ANG))
            Ltr.LtrInfo.add(Letter.BOTTOM_OPEN);
        else if((AvgAng == 6) && (Deviation < CORNER_DETECT_ANG))
            Ltr.LtrInfo.add(Letter.LEFT_OPEN);
        else if(Deviation < CORNER_DETECT_ANG)
        {
            switch(AvgAng)
            {
                case 0: case 1:
                {Ltr.LtrInfo.add(Letter.TOP_RIGHT_OPEN); break;}
                case 2: case 3: 
                {Ltr.LtrInfo.add(Letter.BOTTOM_RIGHT_OPEN); break;}
                case 4: case 5: 
                {Ltr.LtrInfo.add(Letter.BOTTOM_LEFT_OPEN); break;}
                case 6: case 7: 
                {Ltr.LtrInfo.add(Letter.TOP_LEFT_OPEN); break;}
            }
        }    
        else
        {
            Point ChkPnt = null;

            if((isDevNeg && (EndPath.NegativeDev > 0)) || ((!isDevNeg) && (EndPath.PositiveDev > 0)))
                ChkPnt = new Point((EndPath.EndPnt.Row + StPath.EndPnt.Row) / 2, (EndPath.EndPnt.Col + StPath.EndPnt.Col) / 2);
            else
                ChkPnt = new Point((SameIDPnt[AngDiffPnt].Row + StPath.EndPnt.Row) / 2, (SameIDPnt[AngDiffPnt].Col + StPath.EndPnt.Col) / 2);
            
            int ChkAng = Math.round(JuncPnt.getAngle(ChkPnt) / 45);

            if(ChkAng == 8)
                ChkAng = 0;

            int EndsAng = Math.round(RefPath.EndPnt.getAngle(ChkPath.EndPnt) / 45);
            if(EndsAng == 8)
                EndsAng = 0;
                
            if((EndsAng == 0) || (EndsAng == 4))
            {
                if(ChkAng < 4)
                    Ltr.LtrInfo.add(Letter.RIGHT_OPEN);
                else
                    Ltr.LtrInfo.add(Letter.LEFT_OPEN);
            }
            else if((EndsAng == 2) || (EndsAng == 6))
            {
                if((ChkAng < 2) || (ChkAng > 6))
                    Ltr.LtrInfo.add(Letter.TOP_OPEN);
                else
                    Ltr.LtrInfo.add(Letter.BOTTOM_OPEN);
            }
            else
            {
                switch(ChkAng)
                {
                    case 0: case 7: 
                    {Ltr.LtrInfo.add(Letter.TOP_OPEN); break;}
                    case 1: case 2: 
                    {Ltr.LtrInfo.add(Letter.RIGHT_OPEN); break;}
                    case 3: case 4: 
                    {Ltr.LtrInfo.add(Letter.BOTTOM_OPEN); break;}
                    case 5: case 6: 
                    {Ltr.LtrInfo.add(Letter.LEFT_OPEN); break;}
                }
            }
            // Point ChkPnt = null;

            // if((isDevNeg && (EndPath.NegativeDev > 0)) || ((!isDevNeg) && (EndPath.PositiveDev > 0)))
            //     ChkPnt = new Point((EndPath.EndPnt.Row + StPath.EndPnt.Row) / 2, (EndPath.EndPnt.Col + StPath.EndPnt.Col) / 2);
            // else
            //     ChkPnt = new Point((SameIDPnt[AngDiffPnt].Row + StPath.EndPnt.Row) / 2, (SameIDPnt[AngDiffPnt].Col + StPath.EndPnt.Col) / 2);
            
            // int ChkAng = Math.round(JuncPnt.getAngle(ChkPnt) / 45);

            // switch(ChkAng)
            // {
            //     case 0: case 7: 
            //     {Ltr.LtrInfo.add(Letter.TOP_OPEN); break;}
            //     case 1: case 2: 
            //     {Ltr.LtrInfo.add(Letter.RIGHT_OPEN); break;}
            //     case 3: case 4: 
            //     {Ltr.LtrInfo.add(Letter.BOTTOM_OPEN); break;}
            //     case 5: case 6: 
            //     {Ltr.LtrInfo.add(Letter.LEFT_OPEN); break;}
            // }

            // ==========================================================================================================================
        }
    }

    public void LineTest(ReadImage riLTob) throws PixelDistOutOfBoundsException
    {
        System.out.println("\n" + new Line(163, 140, 129, 140).drawLine(riLTob, 3));
        System.out.println("\n" + new Line(163, 140, 129, 174).drawLine(riLTob, 3));
        System.out.println("\n" + new Line(163, 140, 163, 174).drawLine(riLTob, 3));
        System.out.println("\n" + new Line(163, 140, 197, 174).drawLine(riLTob, 3));
        System.out.println("\n" + new Line(163, 140, 197, 140).drawLine(riLTob, 3));
        System.out.println("\n" + new Line(163, 140, 197, 106).drawLine(riLTob, 3));
        System.out.println("\n" + new Line(163, 140, 163, 106).drawLine(riLTob, 3));
        System.out.println("\n" + new Line(163, 140, 129, 106).drawLine(riLTob, 3));
        
        System.out.println("\n" + new Line(265, 707, 230, 701).drawLine(riLTob, 1));
        System.out.println("\n" + new Line(163, 140, 197, 224).drawLine(riLTob, 3));
    }

    public void TravelTest(ReadImage riTTob) throws PixelDistOutOfBoundsException, WrongTravelInitializerException
    {
        Travel QuadTravel = new Travel  (
                                            new Dot(0, 0), 
                                            new Dot(0, 50), 
                                            new Dot(50, 160), 
                                            new Dot(320, 0),
                                            Travel.HORIZONTAL, 
                                            Travel.RIGHT, 
                                            Travel.STEP, 
                                            1, 
                                            0           
                                        );
                                        
        new Line(QuadTravel.TopLeft, QuadTravel.TopRight).drawLine(riTTob, 2);
        new Line(QuadTravel.TopRight, QuadTravel.BottomRight).drawLine(riTTob, 2);
        new Line(QuadTravel.BottomRight, QuadTravel.BottomLeft).drawLine(riTTob, 2);
        new Line(QuadTravel.BottomLeft, QuadTravel.TopLeft).drawLine(riTTob, 2);

        new Line(QuadTravel.StLine.StDot, QuadTravel.StLine.EndDot).drawLine(riTTob, 3);
        new Line(QuadTravel.EndLine.StDot, QuadTravel.EndLine.EndDot).drawLine(riTTob, 4);

        Dot ChkDot = QuadTravel.getNxtChkDot();

        while (ChkDot != null)
        {
            if(QuadTravel.LineChange)
            {
                // System.out.println("\nLine Change --> CrntLine No = " + QuadTravel.CrntLineNo);
            }

            riTTob.PixelsRed[ChkDot.Row][ChkDot.Col] = 0;
            riTTob.PixelsGreen[ChkDot.Row][ChkDot.Col] = 0;
            riTTob.PixelsBlue[ChkDot.Row][ChkDot.Col] = 255;

            // System.out.println(ChkDot.Row + ", " + ChkDot.Col);

            ChkDot = QuadTravel.getNxtChkDot();   
        }        
    }
    
    public void ExtractLetterSpecs(ReadImage riELSob, Letter Ltr, int LtrCor[][], int StPntCnt, int DerateFact, int DivRowCol) throws PixelDistOutOfBoundsException
    {
        JunctionPnts LtJunctnPnts = GetStPoint(riELSob, Ltr, LtrCor, StPntCnt);

        int LtHeight = 1 + (((riELSob.Dist(LtrCor[0][0], LtrCor[0][1],  LtrCor[3][0], LtrCor[3][1])) + (riELSob.Dist(LtrCor[1][0], LtrCor[1][1],  LtrCor[2][0], LtrCor[2][1]))) / 2);
        int LtWidth = 1 + (((riELSob.Dist(LtrCor[0][0], LtrCor[0][1],  LtrCor[1][0], LtrCor[1][1])) + (riELSob.Dist(LtrCor[3][0], LtrCor[3][1],  LtrCor[2][0], LtrCor[2][1]))) / 2);

        if(LtJunctnPnts.LtStPnt[0] != null)
        {
            Point PrevPnt = LtJunctnPnts.LtStPnt[0].Pnt;
            Point CrntPnt = PrevPnt.getAdjacent(0);
        
            GetPathSpecs(riELSob, Ltr, LtrCor, PrevPnt, CrntPnt, DerateFact, DivRowCol, LtHeight, LtWidth, LtJunctnPnts, false);

            for(int StPnt = 0; StPnt < LtJunctnPnts.LtStPnt.length; StPnt++)
            {
                if(LtJunctnPnts.LtStPnt[StPnt] != null)
                {
                    if(LtJunctnPnts.LtStPnt[StPnt].Pnt.PathID == -1)
                    {
                        PrevPnt = LtJunctnPnts.LtStPnt[StPnt].Pnt;
                        CrntPnt = PrevPnt.getAdjacent(0);
                    
                        GetPathSpecs(riELSob, Ltr, LtrCor, PrevPnt, CrntPnt, DerateFact, DivRowCol, LtHeight, LtWidth, LtJunctnPnts, false);
                    }
                }
            }

            for(int Col = 0; Col < Ltr.Point.size(); Col++)
            {
                for(int Pnt = 0; Pnt < Ltr.Point.get(Col).size(); Pnt++)
                {
                    if(Ltr.getPoint(Col, Pnt).PathID == -1)
                    {
                        PrevPnt = Ltr.getPoint(Col, Pnt);
                        CrntPnt = PrevPnt.getAdjacent(0);

                        GetPathSpecs(riELSob, Ltr, LtrCor, PrevPnt, CrntPnt, DerateFact, DivRowCol, LtHeight, LtWidth, LtJunctnPnts, false);
                    }
                }
            }
        }
        else 
        {
            Point PrevPnt = null;
            Point CrntPnt = null;
            boolean Done = false;

            for(int ChkCol = 0; ChkCol < Ltr.Point.size(); ChkCol++)
            {
                for(int ChkPnt = 0; ChkPnt < Ltr.Point.get(ChkCol).size(); ChkPnt++)
                {
                    if(Ltr.getPoint(ChkCol, ChkPnt).Adjacent.size() > 2)
                    {
                        Done = true;

                        PrevPnt = Ltr.getPoint(ChkCol, ChkPnt);
                        for(int Adj = 0; Adj < PrevPnt.Adjacent.size(); Adj++)
                            if(PrevPnt.getAdjacent(Adj).PathID == -1)
                            {
                                CrntPnt = PrevPnt.getAdjacent(Adj);
                                break;
                            }

                            GetPathSpecs(riELSob, Ltr, LtrCor, PrevPnt, CrntPnt, DerateFact, DivRowCol, LtHeight, LtWidth, LtJunctnPnts, false);
                        break;
                    }
                }
                if(Done)
                    break;
            }

            if(!Done)
            {
                for(int ChkCol = 0; ChkCol < Ltr.Point.size(); ChkCol++)
                    if(Ltr.Point.get(ChkCol).size() != 0)
                    { 
                        PrevPnt = Ltr.getPoint(ChkCol, 0);
                        CrntPnt = Ltr.getPoint(ChkCol, 0).getAdjacent(0);
                        GetPathSpecs(riELSob, Ltr, LtrCor, PrevPnt, CrntPnt, DerateFact, DivRowCol, LtHeight, LtWidth, LtJunctnPnts, false);
                        break;
                    }
            }
            // for(int Col = 0; Col < Ltr.Point.size(); Col++)
            // {
            //     for(int Pnt = 0; Pnt < Ltr.Point.get(Col).size(); Pnt++)
            //     {
            //         if(Ltr.getPoint(Col, Pnt).Processed == false)
            //         {
            //             PrevPnt = Ltr.getPoint(Col, Pnt);
            //             CrntPnt = PrevPnt.getAdjacent(0);

            //             GetPathSpecs(riELSob, Ltr, LtrCor, PrevPnt, CrntPnt, DerateFact, DivRowCol, LtHeight, LtWidth);
            //         }
            //     }
            // }
        }

        int Stop = 90;
    }

    public void GetPathSpecs(ReadImage riGPSob, Letter Ltr, int LtrCor[][], Point PrevPnt, Point CrntPnt, int DerateFact, int DivRowCol, int LtHeight, int LtWidth, JunctionPnts LtJunctnPnts, boolean isAddPathCall) throws PixelDistOutOfBoundsException
    {        
        // INITIALISATION ================================================================================================================================
        
        Path PntPath = new Path();
        Point NxtJunctnPnts[] = null;
        Point PthStPnt = PrevPnt, DupCrntPnt = CrntPnt;

        // if(PrevPnt.Adjacent.size() < 3)
            // PrevPnt.Processed = true;

            // PrevPnt.PathID = Ltr.LtPath.size();

            if(PrevPnt.PathID == -1)
                PrevPnt.PathID = 0xFF;

        float PrevAng, CrntAng, AngDiff;
        int NxtAdjPntLen = 1;
        boolean AddPth = false;

        int PthMaxRow = PrevPnt.Row, PthMaxCol = PrevPnt.Col;
        int PthMinRow = PrevPnt.Row, PthMinCol = PrevPnt.Col;

        PrevAng = PrevPnt.getAngle(CrntPnt);

        // EXTRACT PATH ==================================================================================================================================

        while(NxtAdjPntLen == 1)
        {
            // if(CrntPnt.Adjacent.size() < 3)
                // CrntPnt.Processed = true;

            new Line(PrevPnt, CrntPnt).drawLine(riGPSob, 3);

            CrntAng = PrevPnt.getAngle(CrntPnt);
            float DupCrntAng = CrntAng;
            
            if(CrntAng < PrevAng) 
                CrntAng += 360;

            AngDiff = CrntAng - PrevAng;

            if(AngDiff <= 180)
            {
                if(PntPath.NegativeDev == 0)
                    PntPath.PositiveDev += AngDiff;
                else
                    AddPth = true;
            }
            else
            {
                if(PntPath.PositiveDev == 0)
                {
                    AngDiff -= 360;
                    PntPath.NegativeDev -= AngDiff;
                }
                else
                    AddPth = true;
            }

            if(AddPth)
                break;

            CrntPnt.PathID = Ltr.LtPath.size();

            if(CrntPnt.Row > PthMaxRow) PthMaxRow = CrntPnt.Row;
            if(CrntPnt.Row < PthMinRow) PthMinRow = CrntPnt.Row;

            if(CrntPnt.Col > PthMaxCol) PthMaxCol = CrntPnt.Col;
            if(CrntPnt.Col < PthMinCol) PthMinCol = CrntPnt.Col;

            PntPath.TotDeviation += AngDiff;

            PrevAng = DupCrntAng;

            Point NxtAdjPnts[] = new Point[CrntPnt.Adjacent.size() - 1];
            
            CrntPnt.getNxtAdjPoints(PrevPnt, NxtAdjPnts);

            NxtAdjPntLen = NxtAdjPnts.length;
            
            PrevPnt = CrntPnt;
        
            if(NxtAdjPntLen == 1)
            {
                PntPath.PathPnts.add(CrntPnt);
                CrntPnt = NxtAdjPnts[0];

                // if(NxtAdjPnts[0].Processed)

                if((NxtAdjPnts[0].PathID != -1) && (!isAddPathCall) && (NxtAdjPnts[0].Adjacent.size() <= 2))
                    break;
            }

            if(NxtAdjPntLen > 1) 
            {
                NxtJunctnPnts = new Point[NxtAdjPntLen];
                NxtJunctnPnts = NxtAdjPnts;
            }
        }

        // CALCULATE POSITION DOT =====================================================================================================================

        PntPath.PosDot.Row = Math.round(((((float)PthMaxRow + PthMinRow) / 2) - LtrCor[0][0]) * DivRowCol / LtHeight);
        PntPath.PosDot.Col = Math.round(((((float)PthMaxCol + PthMinCol) / 2) - LtrCor[0][1]) * DivRowCol / LtWidth);

        // SET START AND END POINTS ===================================================================================================================

        if(!(AddPth))
        {
            // PntPath.StPnt.setDot(new Dot(PthStPnt.Row, PthStPnt.Col));
            // PntPath.EndPnt.setDot(new Dot(CrntPnt.Row, CrntPnt.Col));

            // PntPath.StPnt.setPoint(PthStPnt);
            // PntPath.EndPnt.setPoint(CrntPnt);

            PntPath.StPnt = PthStPnt;
            PntPath.EndPnt = CrntPnt;
        }
        else
        {
            // PntPath.StPnt.setDot(new Dot(PthStPnt.Row, PthStPnt.Col));
            // PntPath.EndPnt.setDot(new Dot(PrevPnt.Row, PrevPnt.Col));

            // PntPath.StPnt.setPoint(PthStPnt);
            // PntPath.EndPnt.setPoint(PrevPnt);
            
            PntPath.StPnt = PthStPnt;
            PntPath.EndPnt = PrevPnt;
        }

        PntPath.TwoEndsAngle = PntPath.StPnt.getAngle(PntPath.EndPnt);

        // CLOSED LOOP DETECT ===================================================================================================================

        if((PntPath.TwoEndsAngle == 0.0) && (PntPath.StPnt.equals(PntPath.EndPnt)))
        {
            PntPath.TotDeviation = 360;
            PntPath.PositiveDev = 360;
            PntPath.NegativeDev = 0;
            PntPath.MagnitudeDev = 360;
        }

        if(!(AddPth))
            PntPath.MagnitudeDev = PntPath.PositiveDev + PntPath.NegativeDev;

        // DERATE PATH SPECS ====================================================================================================================
        // STORE PATH ROW / COL VALUES AS DECIMALS OF DEVIATIONS ================================================================================

        // PntPath.TotDeviation = (Math.round(PntPath.TotDeviation / DerateFact)) + ((float)PthMaxRow / 10000);
        // PntPath.PositiveDev = (Math.round(PntPath.PositiveDev / DerateFact)) + ((float)PthMaxCol / 10000);
        // PntPath.NegativeDev = (Math.round(PntPath.NegativeDev / DerateFact)) + ((float)PthMinRow / 10000);
        // PntPath.MagnitudeDev = (Math.round(PntPath.MagnitudeDev / DerateFact)) + ((float)PthMinCol / 10000);
        
        PntPath.TotDeviation = Math.round(PntPath.TotDeviation / DerateFact);
        PntPath.PositiveDev = Math.round(PntPath.PositiveDev / DerateFact);
        PntPath.NegativeDev = Math.round(PntPath.NegativeDev / DerateFact);
        PntPath.MagnitudeDev = Math.round(PntPath.MagnitudeDev / DerateFact);
        PntPath.TwoEndsAngle = Math.round(PntPath.TwoEndsAngle / DerateFact);

        // ASSIGN PATH TO LETTER ================================================================================================================

        if((PntPath.StPnt.Adjacent.size() <= 2) && (PntPath.EndPnt.Adjacent.size() > 2))
        {
            PntPath.ReversePath();
            PntPath.TwoEndsAngle = Math.round(PntPath.TwoEndsAngle / DerateFact);
        }

        Ltr.LtPath.add(PntPath);

        // EXTRACT NEXT PATH - RECURSION ======================================================================================================

        if((NxtJunctnPnts != null) && (NxtJunctnPnts.length > 1))
        {
            for(int JuncPath = 0; JuncPath < NxtJunctnPnts.length; JuncPath++)
            {
                // if(NxtJunctnPnts[JuncPath].Processed == false)
                if(NxtJunctnPnts[JuncPath].PathID == -1)
                    GetPathSpecs(riGPSob, Ltr, LtrCor, CrntPnt, NxtJunctnPnts[JuncPath], DerateFact, DivRowCol, LtHeight, LtWidth, LtJunctnPnts, false);
            }
        }
        else if(AddPth)
        {
            int StPnt;
            boolean StPathFound = false;

            for(StPnt = 0; StPnt < 5; StPnt++)
                if((LtJunctnPnts.LtStPnt[StPnt] != null) && (LtJunctnPnts.LtStPnt[StPnt].Pnt.PathID == -1))
                {
                    StPathFound = true;
                    break;
                }
                
            if(StPathFound)
                GetPathSpecs(riGPSob, Ltr, LtrCor, LtJunctnPnts.LtStPnt[StPnt].Pnt, LtJunctnPnts.LtStPnt[StPnt].Pnt.getAdjacent(0), DerateFact, DivRowCol, LtHeight, LtWidth, LtJunctnPnts, true);
        }
    }

    public void DecodeLtr(ReadImage riDLob, Letter Ltr, int LtrCor[][], int DerateFact, int DominationDiff, int DivRowCol)
    {
        int LtHeight = 1 + (((riDLob.Dist(LtrCor[0][0], LtrCor[0][1],  LtrCor[3][0], LtrCor[3][1])) + (riDLob.Dist(LtrCor[1][0], LtrCor[1][1],  LtrCor[2][0], LtrCor[2][1]))) / 2);
        int LtWidth = 1 + (((riDLob.Dist(LtrCor[0][0], LtrCor[0][1],  LtrCor[1][0], LtrCor[1][1])) + (riDLob.Dist(LtrCor[3][0], LtrCor[3][1],  LtrCor[2][0], LtrCor[2][1]))) / 2);

        for(int Pth = 0; Pth < Ltr.LtPath.size(); Pth++)
        {
            Path Path = Ltr.LtPath.get(Pth);

            if(Path.StPnt.equals(Path.EndPnt))
                continue;

            for(int ChkPth = 0; ChkPth < Ltr.LtPath.size(); ChkPth++)
            {
                if(Pth == ChkPth)
                    continue;
                
                Path ChkPath = Ltr.LtPath.get(ChkPth);

                if(ChkPath.StPnt.equals(ChkPath.EndPnt))
                    continue;

                boolean isClosedLoop = false;

                if((Path.StPnt.equals(ChkPath.StPnt)) && ((Path.EndPnt.equals(ChkPath.EndPnt))) || (Path.StPnt.equals(ChkPath.EndPnt)) && ((Path.EndPnt.equals(ChkPath.StPnt))))
                    isClosedLoop = true;
                
                if(isClosedLoop)
                {
                    Path.PosDot = GetPositionDot(Path, ChkPath, LtrCor, DivRowCol, LtHeight, LtWidth);
                    Path.TotDeviation = 360 / DerateFact;
                    Path.PositiveDev = 360 / DerateFact;
                    Path.NegativeDev = 0;
                    Path.MagnitudeDev = 360 / DerateFact;
                    Path.TwoEndsAngle = 0;
                    Path.EndPnt.setDot(new Dot(Path.StPnt.Row, Path.StPnt.Col));
                    Path.EndPnt.PathID = Path.StPnt.PathID;

                    Ltr.LtPath.remove(ChkPath);

                    Pth = -1;
                    break;
                }
            }
        }

        for(int Pth = 0; Pth < Ltr.LtPath.size(); Pth++)
        {
            Path ChkPath = Ltr.LtPath.get(Pth);

            ChkPath.TotDeviation = (int)ChkPath.TotDeviation;
            ChkPath.PositiveDev = (int)ChkPath.PositiveDev;
            ChkPath.NegativeDev = (int)ChkPath.NegativeDev;
            ChkPath.MagnitudeDev = (int)ChkPath.MagnitudeDev;

            float ChkNegDev = ChkPath.NegativeDev;
            if(ChkNegDev == 0) 
                ChkNegDev = 1;

            float ChkPosDev = ChkPath.PositiveDev;
                if(ChkPosDev == 0) 
                    ChkPosDev = 1;

            if(ChkPath.StPnt.equals(ChkPath.EndPnt))
            {
                ChkPath.Type = Path.CLOSED_LOOP;
                Ltr.ClosedPathCnt++;
            }
            else if(ChkPath.PositiveDev > (ChkNegDev * DominationDiff))
            {
                ChkPath.Type = Path.POS_DOM;
                Ltr.PosDomPthCnt++;
            }
            else if(ChkPath.NegativeDev > (ChkPosDev * DominationDiff))
            {
                ChkPath.Type = Path.NEG_DOM;
                Ltr.NegDomPthCnt++;
            }
            else
            {
                ChkPath.Type = Path.NEUTRAL;
                Ltr.NeutrlPthCnt++;
            }

            int PrintRow = ((ChkPath.PosDot.Row * LtHeight) / DivRowCol) + LtrCor[0][0];
            int PrintCol = ((ChkPath.PosDot.Col * LtWidth) / DivRowCol) + LtrCor[0][1];

            riDLob.PixelsRed[PrintRow][PrintCol] = 255;
            riDLob.PixelsGreen[PrintRow][PrintCol] = 0;
            riDLob.PixelsBlue[PrintRow][PrintCol] = 0;
        }
    }

    public Dot GetPositionDot(Path Path, Path ChkPath, int LtrCor[][], int DivRowCol, int LtHeight, int LtWidth)
    {
        Dot PosDot = new Dot(-1, -1);

        int NewMaxRow = (int)((Path.TotDeviation * 10000) % 10000); 
        int NewMinRow = (int)((Path.NegativeDev * 10000) % 10000);
        int NewMaxCol = (int)((Path.PositiveDev * 10000) % 10000);
        int NewMinCol = (int)((Path.MagnitudeDev * 10000) % 10000);

        int ChkVal = (int)((ChkPath.TotDeviation * 10000) % 10000);

        if(ChkVal > NewMaxRow)
            NewMaxRow = ChkVal;

        ChkVal = (int)((ChkPath.NegativeDev * 10000) % 10000);

        if(ChkVal < NewMinRow)
            NewMinRow = ChkVal;

        ChkVal = (int)((ChkPath.PositiveDev * 10000) % 10000);

        if(ChkVal > NewMaxCol)
            NewMaxCol = ChkVal;

        ChkVal = (int)((ChkPath.MagnitudeDev * 10000) % 10000);

        if(ChkVal < NewMinCol)
            NewMinCol = ChkVal;

        PosDot.Row = Math.round(((((float)NewMaxRow + NewMinRow) / 2) - LtrCor[0][0]) * DivRowCol / LtHeight);
        PosDot.Col = Math.round(((((float)NewMaxCol + NewMinCol) / 2) - LtrCor[0][1]) * DivRowCol / LtWidth);

        return PosDot;
    }

    public void WriteLtrInfo(Language Font) throws IOException
    {
        FileWriter LtrInfoFile = new FileWriter("LtrInfoFile.csv");

        LtrInfoFile.write("Letter, Type\n");
        LtrInfoFile.write("\n");

        String InfoType[] = {
                                "Closed Loop", 
                                "Top", "Top Right", 
                                "Right", "Bottom Right", 
                                "Bottom", "Bottom Left", 
                                "Left", "Top Left", 
                                "Dot", 
                                "Vertical Line", "Horizontal Line", 
                                "Right Slant Line", "Left Slant Line",
                                "Top U"
                            };

        for(int Ltr = 0; Ltr < Font.LtrNum; Ltr++)
        {
            LtrInfoFile.write((char)(Ltr + 97));

            Letter PrintLtr = Font.oLetter[Ltr];

            for(int Info = 0; Info < PrintLtr.LtrInfo.size(); Info++)
                LtrInfoFile.write("," + InfoType[PrintLtr.LtrInfo.get(Info)]);

            LtrInfoFile.write("\n");
        }

        LtrInfoFile.close();
    }

    public void WriteFileHeader(FileWriter LtrInfoFile) throws IOException
    {
        LtrInfoFile.write("Letter, Type\n");
        LtrInfoFile.write("\n");
    }

    public void WriteFileFooter(FileWriter LtrInfoFile) throws IOException
    {
        LtrInfoFile.close();
    }

    public void WriteLtrInfo(Letter Ltr, FileWriter LtrInfoFile, int LtrNo) throws IOException
    {
        String InfoType[] = {
                                "Closed Loop", 
                                "Top", "Top Right", 
                                "Right", "Bottom Right", 
                                "Bottom", "Bottom Left", 
                                "Left", "Top Left", 
                                "Dot", 
                                "Vertical Line", "Horizontal Line", 
                                "Right Slant Line", "Left Slant Line",
                                "Top U"
                            };

        LtrInfoFile.write((char)(LtrNo + 97));

        for(int Info = 0; Info < Ltr.LtrInfo.size(); Info++)
            LtrInfoFile.write("," + InfoType[Ltr.LtrInfo.get(Info)]);

        LtrInfoFile.write("\n");
    }

    public void WritePathInfo(Language Font) throws IOException
    {
        FileWriter LtrPathFile = new FileWriter("LtrPathFile.csv");    

        LtrPathFile.write("Letter,Path,PosDot Row,PosDot Col,StPnt Row,StPnt Col,EndPnt Row,EndPnt Col,TotDeviation,PositiveDev,NegativeDev,MagnitudeDev,TwoEndsAngle,Path Type,\n");
        LtrPathFile.write("\n");

        for(int Ltr = 0; Ltr < Font.LtrNum; Ltr++)
        {
            LtrPathFile.write((char)(Ltr + 97));

            Letter PrintLtr = Font.oLetter[Ltr];

            for(int Path = 0; Path < Font.oLetter[Ltr].LtPath.size(); Path++)
            {
                Path oPath = PrintLtr.LtPath.get(Path);

                LtrPathFile.write("," + Path + "," + oPath.PosDot.Row + "," + oPath.PosDot.Col);
                LtrPathFile.write("," + oPath.StPnt.Row + "," + oPath.StPnt.Col + "," + oPath.EndPnt.Row + "," + oPath.EndPnt.Col);
                LtrPathFile.write("," + oPath.TotDeviation + "," + oPath.PositiveDev + "," + oPath.NegativeDev + "," + oPath.MagnitudeDev);
                LtrPathFile.write("," + oPath.TwoEndsAngle + "," + oPath.Type + ",\n");
            }
        }

        LtrPathFile.close();
    }

    public JunctionPnts GetStPoint(ReadImage riGPob, Letter Ltr, int LtrCor[][], int StPntCnt)
    {
        JunctionPnts LtJunctnPnts = new JunctionPnts(StPntCnt);
        int PntIndex = -1;
        
        for(int FillCor = 0; FillCor < 4; FillCor++)
        {
            for(int Col = 0; Col < Ltr.Point.size(); Col++)
            {
                for(int Pnt = 0; Pnt < Ltr.Point.get(Col).size(); Pnt++)
                {
                    if(Ltr.getPoint(Col, Pnt).Adjacent.size() == 1)
                    {
                        int MaxDist = 65536, AnsCor = -1;

                        for(int Cor = 0; Cor < 4; Cor++)
                        {
                            int ChkDist = (int)(Ltr.getPoint(Col, Pnt).getDist(LtrCor[Cor][0], LtrCor[Cor][1]));
                            if(ChkDist < MaxDist)
                            {
                                MaxDist = ChkDist;
                                AnsCor = Cor;
                            }
                        }

                        if(AnsCor == FillCor)
                        {
                            PntIndex++;
                            LtJunctnPnts.LtStPnt[PntIndex] = new StPnt(Ltr.getPoint(Col, Pnt), AnsCor);
                        }

                        if(FillCor == 0)
                            LtJunctnPnts.Cnt++;
                    } 
                    else if((FillCor == 0) && (Ltr.getPoint(Col, Pnt).Adjacent.size() >= 3))
                    {
                        LtJunctnPnts.Cnt++;
                    }
                }
            }
        }

        return LtJunctnPnts;
    }

    public void ExtractAdjacentPoints(ReadImage riEAPob, Letter Ltr, boolean PrintLine, boolean PrintPoints, int DotWithAdjNo) throws PixelDistOutOfBoundsException
    {
        for(int Col = 0; Col < Ltr.Point.size(); Col++)
        {
            for(int Pnt = 0; Pnt < Ltr.Point.get(Col).size(); Pnt++)
                AssignAdjacentPoints(riEAPob, Ltr, Col, Pnt);
        }
        
        if(PrintPoints)
            PrintLtr(riEAPob, Ltr, PrintLine, GREEN, DotWithAdjNo);

    }

    public void AddAdjInAllDir(Letter Ltr, int Directions)
    {
        for(int Col = 0; Col < Ltr.Point.size(); Col++)
        {
            for(int Pnt = 0; Pnt < Ltr.Point.get(Col).size(); Pnt++)
            {
                for(int DircnIndex = 0; DircnIndex < Directions; DircnIndex++)
                    Ltr.getPoint(Col, Pnt).Adjacent.add(null);
            }
        }
    }

    public void DeleteDeadAdjs(Letter Ltr)
    {
        for(int Col = 0; Col < Ltr.Point.size(); Col++)
        {
            for(int Pnt = 0; Pnt < Ltr.Point.get(Col).size(); Pnt++)
            {
                for(int Del = 0; Del < Ltr.getPoint(Col, Pnt).Adjacent.size(); Del++)
                {
                    if(Ltr.getPoint(Col, Pnt).Adjacent.get(Del) == null)
                    {
                        Ltr.getPoint(Col, Pnt).Adjacent.remove(Del);
                        Del = -1;
                    }
                }
            }
        }
    }

    public void PrintLtr(ReadImage riPPob, Letter Ltr, Boolean isLine, int Color, int DotWithAdjNo) throws PixelDistOutOfBoundsException
    {
        for(int Col = 0; Col < Ltr.Point.size(); Col++)
        {
            for(int Pnt = 0; Pnt < Ltr.Point.get(Col).size(); Pnt++)
            {
                int PntRow = Ltr.getPoint(Col, Pnt).Row;
                int PntCol = Ltr.getPoint(Col, Pnt).Col;

                // if(Ltr.getPoint(Col, Pnt).Processed)
                if(Ltr.getPoint(Col, Pnt).PathID != -1)
                {
                    riPPob.PixelsRed[PntRow][PntCol] = 255;
                    riPPob.PixelsGreen[PntRow][PntCol] = 0;
                    riPPob.PixelsBlue[PntRow][PntCol] = 0; 
                }
                else
                {
                    riPPob.PixelsRed[PntRow][PntCol] = Color & 0xff;
                    riPPob.PixelsGreen[PntRow][PntCol] = (Color >> 8) & 0xff;
                    riPPob.PixelsBlue[PntRow][PntCol] = (Color >> 16) & 0xff; 
                }
                  
                if(isLine)
                {
                    for(int Print = 0; Print < (Ltr.getPoint(Col, Pnt).Adjacent.size()); Print++)
                    {
                        // Point PrintPnt = Ltr.getPoint(Col, Pnt).getAdjacent(Print);

                        Line PrintLn = new Line(Ltr.getPoint(Col, Pnt).Row, Ltr.getPoint(Col, Pnt).Col, Ltr.getPoint(Col, Pnt).getAdjacent(Print).Row, Ltr.getPoint(Col, Pnt).getAdjacent(Print).Col);
                        PrintLn.drawLine(riPPob, 2);

                        // Line2(riPPob, Ltr.getPoint(Col, Pnt).Row, Ltr.getPoint(Col, Pnt).Col, PrintPnt.Row, PrintPnt.Col, 2);
                    }
                }
            }
        }

        if(DotWithAdjNo > 0)
        {
            for(int Col = 0; Col < Ltr.Point.size(); Col++)
            {
                for(int Pnt = 0; Pnt < Ltr.Point.get(Col).size(); Pnt++)
                {
                    int PntRow = Ltr.getPoint(Col, Pnt).Row;
                    int PntCol = Ltr.getPoint(Col, Pnt).Col;
    
                    if((DotWithAdjNo > 1) && (Ltr.getPoint(Col, Pnt).Adjacent.size() > 2))
                    {
                        riPPob.PixelsRed[PntRow][PntCol] = 0;
                        riPPob.PixelsGreen[PntRow][PntCol] = 0;
                        riPPob.PixelsBlue[PntRow][PntCol] = 255;
                    }

                    if(Ltr.getPoint(Col, Pnt).Adjacent.size() == 1)
                    {
                        riPPob.PixelsRed[PntRow][PntCol] = 0;
                        riPPob.PixelsGreen[PntRow][PntCol] = 195;
                        riPPob.PixelsBlue[PntRow][PntCol] = 225;
                    }
                }
            }   
        }
    }

    public void AssignAdjacentPoints(ReadImage riAAPob, Letter Ltr, int Col, int Pnt) throws PixelDistOutOfBoundsException
    {
        List <Float> Direction = new ArrayList <Float>();

        Dot RefDot = new Dot(Ltr.getPoint(Col, Pnt).Row, Ltr.getPoint(Col, Pnt).Col);
        Dot ChkDot = new Dot();

        for(int ChkCol = 0; ChkCol < Ltr.Point.size(); ChkCol++)
        {
            for(int ChkPnt = 0; ChkPnt < Ltr.Point.get(ChkCol).size(); ChkPnt++)
            {
                if((ChkCol == Col) && (ChkPnt == Pnt))
                    continue;

                ChkDot.set(Ltr.getPoint(ChkCol, ChkPnt).Row, Ltr.getPoint(ChkCol, ChkPnt).Col);

                if(RefDot.isConnected(ChkDot, riAAPob))
                {
                    Point CrntPnt = Ltr.getPoint(Col, Pnt);
                    Point NewChkPnt = Ltr.getPoint(ChkCol, ChkPnt);

                    float DirecttionVal = CrntPnt.getAngle(NewChkPnt);
                    
                    int Index;
                    for(Index = 0; Index < Direction.size(); Index++)
                    {
                        if(Direction.get(Index) > DirecttionVal)
                        {
                            Direction.add(Index, DirecttionVal);
                            CrntPnt.Adjacent.add(Index, NewChkPnt);  
                            break;
                        }   
                    }
                    
                    if(Index == Direction.size())
                    {
                        Direction.add(DirecttionVal);
                        CrntPnt.Adjacent.add(NewChkPnt);  
                    }
                }                    
            }
        }

        Direction = null;
        RefDot = null;
        ChkDot = null;
    }

    public void DeleteDeadPoints(Letter Ltr)
    {
        for(int Col = 0; Col < Ltr.Point.size(); Col++)
        {
            for(int Pnt = 0; Pnt < Ltr.Point.get(Col).size(); Pnt++)
            {
                if(Ltr.getPoint(Col, Pnt).equals(-1, -1))
                {
                    Ltr.Point.get(Col).remove(Pnt);
                    Pnt = -1;
                }
            }
        }
    }

    public void ReducePathPrecisionOld(ReadImage riRPob, Letter Ltr, boolean PrintLine, boolean PrintPoints, int DotWithAdjNo, int SnglPntDetctAng) throws PixelDistOutOfBoundsException
    {
        for(int Col = 0; Col < Ltr.Point.size(); Col++)
        {
            for(int Pnt = 0; Pnt < Ltr.Point.get(Col).size(); Pnt++)
            {
                Point ChkPnt = Ltr.getPoint(Col, Pnt);
                
                if(ChkPnt.Adjacent.size() < 2)
                    continue;

                boolean ChainList[] = new boolean[ChkPnt.Adjacent.size()];
                ChainList[0] = true;

                for(int Adj = 0; Adj < ChkPnt.Adjacent.size(); Adj++)
                {
                    if(ChainList[Adj] == false)
                        continue;

                    for(int ChkAdj = 0; ChkAdj < ChkPnt.Adjacent.size(); ChkAdj++)
                    {
                        if((Adj == ChkAdj) || (ChainList[ChkAdj]))
                            continue;

                        int ChkDist = (int)(Math.round(ChkPnt.Adjacent.get(Adj).getDist(ChkPnt.Adjacent.get(ChkAdj))));

                        boolean IsBlack = true;
                        for(int Len = 1; Len < ChkDist; Len++)
                        {
                            int ChkLnRow = ((Len * ChkPnt.Adjacent.get(ChkAdj).Row) + ((ChkDist - Len) * ChkPnt.Adjacent.get(Adj).Row)) / ChkDist;
                            int ChkLnCol = ((Len * ChkPnt.Adjacent.get(ChkAdj).Col) + ((ChkDist - Len) * ChkPnt.Adjacent.get(Adj).Col)) / ChkDist;

                            if(riRPob.PixelsRed[ChkLnRow][ChkLnCol] == 255)
                            {
                                IsBlack = false;
                                break;
                            }
                        }    

                        if(IsBlack)
                            ChainList[ChkAdj] = true;
                    }
                }

                boolean DelPnt = true;
                for(int Adj = 0; Adj < ChkPnt.Adjacent.size(); Adj++)
                {
                    if(ChainList[Adj] == false)
                    {
                        DelPnt = false;
                        break;
                    }
                }

                if(DelPnt)
                {
                    Point DeletePnt = Ltr.getPoint(Col, Pnt);
                                        
                    boolean ChkQuad[] = new boolean[4];

                    for(int ChkAngCol = 0; ChkAngCol < Ltr.Point.size(); ChkAngCol++)
                    {   
                        for(int ChkAngPnt = 0; ChkAngPnt < Ltr.Point.get(ChkAngCol).size(); ChkAngPnt++)
                        {
                            Point AngPnt = Ltr.getPoint(ChkAngCol, ChkAngPnt);

                            if(AngPnt.equals(DeletePnt))
                                continue;

                            int CrntAngQuad = (int)(DeletePnt.getAngle(AngPnt) / 90);

                            ChkQuad[CrntAngQuad] = true;
                        }
                    }

                    int ConsecutiveQuadCnt = 0;

                    for(int Quad = 0; Quad < 4; Quad++)
                    {
                        int NxtQuad = (Quad + 1) % 4;

                        if(ChkQuad[Quad] && ChkQuad[NxtQuad])
                            ConsecutiveQuadCnt++;
                    }

                    if(ConsecutiveQuadCnt != 1)
                    {
                        Ltr.DeletePoint(Col, Pnt);
                        Col = -1;
                        break;
                    }
                }
            }
        }

        if(PrintPoints)
            PrintLtr(riRPob, Ltr, PrintLine, GREEN, DotWithAdjNo);
    }

    public void ReducePathPrecision(ReadImage riRPob, Letter Ltr, boolean PrintLine, boolean PrintPoints, int DotWithAdjNo, int SnglPntDetctAng) throws PixelDistOutOfBoundsException
    {
        for(int Col = 0; Col < Ltr.Point.size(); Col++)
        {
            for(int Pnt = 0; Pnt < Ltr.Point.get(Col).size(); Pnt++)
            {
                Point ChkPnt = Ltr.getPoint(Col, Pnt);
                
                if(ChkPnt.Adjacent.size() < 2)
                    continue;

                boolean ChainList[] = new boolean[ChkPnt.Adjacent.size()];
                ChainList[0] = true;

                for(int Adj = 0; Adj < ChkPnt.Adjacent.size(); Adj++)
                {
                    if(ChainList[Adj] == false)
                        continue;

                    Dot AdjDot = new Dot(ChkPnt.Adjacent.get(Adj).Row, ChkPnt.Adjacent.get(Adj).Col);

                    for(int ChkAdj = 0; ChkAdj < ChkPnt.Adjacent.size(); ChkAdj++)
                    {
                        if((Adj == ChkAdj) || (ChainList[ChkAdj]))
                            continue;

                        Dot ChkAdjDot = new Dot(ChkPnt.Adjacent.get(ChkAdj).Row, ChkPnt.Adjacent.get(ChkAdj).Col);

                        // int ChkDist = (int)(Math.round(ChkPnt.Adjacent.get(Adj).getDist(ChkPnt.Adjacent.get(ChkAdj))));

                        // boolean IsBlack = true;
                        // for(int Len = 1; Len < ChkDist; Len++)
                        // {
                        //     int ChkLnRow = ((Len * ChkPnt.Adjacent.get(ChkAdj).Row) + ((ChkDist - Len) * ChkPnt.Adjacent.get(Adj).Row)) / ChkDist;
                        //     int ChkLnCol = ((Len * ChkPnt.Adjacent.get(ChkAdj).Col) + ((ChkDist - Len) * ChkPnt.Adjacent.get(Adj).Col)) / ChkDist;

                        //     if(riRPob.PixelsRed[ChkLnRow][ChkLnCol] == 255)
                        //     {
                        //         IsBlack = false;
                        //         break;
                        //     }
                        // }    

                        // if(IsBlack)
                        if(AdjDot.isConnected(ChkAdjDot, riRPob))
                            ChainList[ChkAdj] = true;
                    }
                }

                boolean DelPnt = true;
                for(int Adj = 0; Adj < ChkPnt.Adjacent.size(); Adj++)
                {
                    if(ChainList[Adj] == false)
                    {
                        DelPnt = false;
                        break;
                    }
                }

                if(DelPnt)
                {
                    Point DeletePnt = Ltr.getPoint(Col, Pnt);
                    boolean DeletePntChk = false;
                    float StrPntAngle = DeletePnt.getAngle(DeletePnt.Adjacent.get(0));

                    for(int ChkAng = 1; ChkAng < DeletePnt.Adjacent.size(); ChkAng++)
                    {                        
                        float EndPntAngle = DeletePnt.getAngle(DeletePnt.Adjacent.get(ChkAng));
                        float AngDiff = EndPntAngle - StrPntAngle;

                        if(AngDiff > 180) 
                            AngDiff = 360 - AngDiff;

                        if((AngDiff) > SnglPntDetctAng)
                        {
                            DeletePntChk = true;
                            break;
                        }
                    }
                    
                    if(DeletePntChk)
                    {
                        Ltr.DeletePoint(Col, Pnt);
                        Col = -1;
                        break;
                    }
                }
            }
        }

        if(PrintPoints)
            PrintLtr(riRPob, Ltr, PrintLine, GREEN, DotWithAdjNo);
    }

    public void DeleteUnwantedPath(ReadImage riDUPob, Letter Ltr, boolean PrintLine, boolean PrintPoints, int DotWithAdjNo, int DelAngle) throws PixelDistOutOfBoundsException
    {
        for(int Col = 0; Col < Ltr.Point.size(); Col++)
        {
            for(int Pnt = 0; Pnt < Ltr.Point.get(Col).size(); Pnt++)
            {        
                int AdjPntCnt = Ltr.getPoint(Col, Pnt).Adjacent.size();
                
                if(AdjPntCnt > 1)
                {   
                    for(int Adj = 0; Adj < AdjPntCnt; Adj++)
                    {
                        Point AdjPnt = Ltr.getPoint(Col, Pnt).Adjacent.get(Adj);
                        Point ChkAdjPnt = null;

                        ChkAdjPnt = Ltr.getPoint(Col, Pnt).Adjacent.get((Adj + 1) % AdjPntCnt);

                        float AngDiff = (Ltr.getPoint(Col, Pnt).getAngle(ChkAdjPnt) - Ltr.getPoint(Col, Pnt).getAngle(AdjPnt));

                        if(AngDiff < 0)
                            AngDiff += 360;

                        if(AngDiff < DelAngle)
                        {
                            if(Ltr.getPoint(Col, Pnt).getDist(AdjPnt) < Ltr.getPoint(Col, Pnt).getDist(ChkAdjPnt))
                                Ltr.getPoint(Col, Pnt).Adjacent.remove(ChkAdjPnt);
                            else 
                                Ltr.getPoint(Col, Pnt).Adjacent.remove(AdjPnt);

                            Adj = -1;
                            AdjPntCnt = Ltr.getPoint(Col, Pnt).Adjacent.size();
                            
                            if(AdjPntCnt <= 1)
                                break;
                        }
                    }
                }
            }
        }

        if(PrintPoints)
            PrintLtr(riDUPob, Ltr, PrintLine, GREEN, DotWithAdjNo);
    }

    public void PutJunctionPointsOld(ReadImage riPJPob, Letter Ltr, boolean PrintLine, boolean PrintPoints, int DotWithAdjNo)
    {
        for(int Col = 0; Col < Ltr.Point.size(); Col++)
        {
            for(int Pnt = 0; Pnt < Ltr.Point.get(Col).size(); Pnt++)
            {   
                // if(Ltr.getPoint(Col, Pnt).Processed)
                if(Ltr.getPoint(Col, Pnt).PathID != - 1)
                    continue;

                // Ltr.getPoint(Col, Pnt).Processed = true;
                Ltr.getPoint(Col, Pnt).PathID = 0;

                for(int Print = 0; Print < (Ltr.getPoint(Col, Pnt).Adjacent.size()); Print++)
                {
                    Point StPnt = Ltr.getPoint(Col, Pnt);
                    Point EndPnt = Ltr.getPoint(Col, Pnt).getAdjacent(Print);

                    // if(EndPnt.Processed)
                    if(EndPnt.PathID != -1)
                        continue;

                    int ChkDist = Math.round(StPnt.getDist(EndPnt));

                    for(int Dist = 1; Dist < ChkDist; Dist++)
                    {
                        int ChkLnRow = ((Dist * EndPnt.Row) + ((ChkDist - Dist) * StPnt.Row)) / ChkDist;
                        int ChkLnCol = ((Dist * EndPnt.Col) + ((ChkDist - Dist) * StPnt.Col)) / ChkDist;

                        if((riPJPob.PixelsBlue[ChkLnRow][ChkLnCol] == 0) || (riPJPob.PixelsBlue[ChkLnRow][ChkLnCol] == 255))
                        {
                            int LnPointVal = (StPnt.getPntIndex(Ltr) * 100) + (EndPnt.getPntIndex(Ltr));
                            
                            riPJPob.PixelsBlue[ChkLnRow][ChkLnCol] = LnPointVal;
                        }
                        else
                        {
                            int BluePixVal = riPJPob.PixelsBlue[ChkLnRow][ChkLnCol];

                            Point PrevStPnt = Ltr.getPntFrmIndex(BluePixVal % 100);
                            Point PrevEndPnt = Ltr.getPntFrmIndex((int)(BluePixVal / 100));

                            Point NewPoint = new Point(ChkLnRow, ChkLnCol);

                            PrevStPnt.Adjacent.set(PrevStPnt.Adjacent.indexOf(PrevEndPnt), NewPoint);
                            PrevEndPnt.Adjacent.set(PrevEndPnt.Adjacent.indexOf(PrevStPnt), NewPoint);

                            try 
                            {
                                StPnt.Adjacent.set(StPnt.Adjacent.indexOf(EndPnt), NewPoint);
                                EndPnt.Adjacent.set(EndPnt.Adjacent.indexOf(StPnt), NewPoint);
                            }
                             catch (Exception e) 
                            {
                                int Stop = 9;
                            }
                            
                            List <Point> SetPnt = new ArrayList <Point>();
                            
                            SetPnt.add(StPnt); SetPnt.add(EndPnt);
                            SetPnt.add(PrevStPnt); SetPnt.add(PrevEndPnt);

                            for(int Set = 0; Set < SetPnt.size(); Set++)
                                NewPoint.Adjacent.add(null);

                            int SetIndex = 0;
                            while(SetPnt.size() != 0)
                            {
                                float LowestAng = 500;
                                int LowestPntIndex = -1;

                                for(int i = 0; i < SetPnt.size(); i++)
                                {
                                    float ChkAng = NewPoint.getAngle(SetPnt.get(i));

                                    if(ChkAng < LowestAng)
                                    {
                                        LowestPntIndex = i;
                                        LowestAng = ChkAng;
                                    }
                                }

                                NewPoint.Adjacent.set(SetIndex, SetPnt.get(LowestPntIndex));
                                SetIndex++;
                                SetPnt.remove(LowestPntIndex);
                            }
                        }
                    }
                }
            }
        }        
    }

    public void PutJunctionPoints(ReadImage riPJPob, Letter Ltr, boolean PrintLine, boolean PrintPoints, int DotWithAdjNo) throws PixelDistOutOfBoundsException
    {
        List <Point> AddPnts = new ArrayList <Point> ();
        List <Intersection> DoneIntr = new ArrayList <Intersection> ();

        for(int Col = 0; Col < Ltr.Point.size(); Col++)
        {
            for(int Pnt = 0; Pnt < Ltr.Point.get(Col).size(); Pnt++)
            {   
                // if(Ltr.getPoint(Col, Pnt).Processed)
                if(Ltr.getPoint(Col, Pnt).PathID != -1)
                    continue;

                // Ltr.getPoint(Col, Pnt).Processed = true;
                Ltr.getPoint(Col, Pnt).PathID = 0;

                for(int Print = 0; Print < (Ltr.getPoint(Col, Pnt).Adjacent.size()); Print++)
                {
                    Point StPnt = Ltr.getPoint(Col, Pnt);
                    Point EndPnt = Ltr.getPoint(Col, Pnt).getAdjacent(Print);

                    // if(EndPnt.Processed)
                    if(EndPnt.PathID != -1)
                        continue;

                    Line ChkLine = new Line(StPnt, EndPnt);

                    boolean isRowDiffMax = false;

                    if(Math.abs(ChkLine.StDot.Row - ChkLine.EndDot.Row) > Math.abs(ChkLine.StDot.Col - ChkLine.EndDot.Col))
                        isRowDiffMax =  true;

                    try
                    {
                        ChkLine.getNxtDotByPixels(1);
                    }
                    catch(Exception e)
                    {
                        int Stop = 9;
                        throw e;
                    }

                    while(ChkLine.CrntDot != null)
                    {
                        if((riPJPob.PixelsBlue[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] == 255) || (riPJPob.PixelsBlue[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] == 0))
                        {
                            int LnPointVal = (StPnt.getPntIndex(Ltr) * 100) + (EndPnt.getPntIndex(Ltr));

                            riPJPob.PixelsBlue[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] = LnPointVal;

                            if(isRowDiffMax)
                                riPJPob.PixelsBlue[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col + 1] = LnPointVal;
                            else
                                riPJPob.PixelsBlue[ChkLine.CrntDot.Row + 1][ChkLine.CrntDot.Col] = LnPointVal;
                        }
                        else
                        {
                            int BluePixVal = riPJPob.PixelsBlue[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col];
                            
                            Point PrevStPnt = Ltr.getPntFrmIndex(BluePixVal % 100);
                            Point PrevEndPnt = Ltr.getPntFrmIndex((int)(BluePixVal / 100));

                            try
                            {
                                if(!((StPnt.equals(PrevStPnt)) || (StPnt.equals(PrevEndPnt)) || (EndPnt.equals(PrevStPnt)) || (EndPnt.equals(PrevEndPnt))))
                                {
                                    // boolean AssnPnt = true;
                                    // Intersection SetIntr = new Intersection(new Dot(StPnt), new Dot(EndPnt), new Dot(PrevStPnt), new Dot(PrevEndPnt));

                                    // for(int ChkIntr = 0; ChkIntr < DoneIntr.size(); ChkIntr++)
                                    // {
                                    //     if(DoneIntr.get(ChkIntr).equals(SetIntr))
                                    //     {
                                    //         AssnPnt = false;
                                    //         break;
                                    //     }    
                                    // }
                                    
                                    // if(AssnPnt)
                                    // {
                                        AddPnts.add(new Point(ChkLine.CrntDot.Row, ChkLine.CrntDot.Col));
                                        // DoneIntr.add(SetIntr);
                                        break;
                                    // }
                                }
                            }
                            catch(Exception e)
                            {
                                int Stop = 9;
                            }
                        }

                        ChkLine.getNxtDotByPixels(1);
                        if(ChkLine.CrntDot != null)
                            if(ChkLine.CrntDot.equals(ChkLine.EndDot))
                                ChkLine.CrntDot = null;
                    }

                    ChkLine = null;
                }
            }
        }
        
        for(int AssnPnt = 0; AssnPnt < AddPnts.size(); AssnPnt++)
        {
            Ltr.Point.get(0).add(new Point(AddPnts.get(AssnPnt)));
            AssignAdjacentPoints(riPJPob, Ltr, 0, (Ltr.Point.get(0).size() - 1));
        }

        AddPnts = null;

        if(PrintPoints)
            PrintLtr(riPJPob, Ltr, PrintLine, GREEN, DotWithAdjNo);
    }

    public void DeleteComnJunctions(ReadImage riDCJob, Letter Ltr, boolean PrintLine, boolean PrintPoints, int DotWithAdjNo) throws PixelDistOutOfBoundsException
    {
        for(int Col = 0; Col < Ltr.Point.size(); Col++)
        {
            for(int Pnt = 0; Pnt < Ltr.Point.get(Col).size(); Pnt++)
            {
                Point SelfPnt = Ltr.getPoint(Col, Pnt);
                SelfPnt.PathID = -1;

                if(SelfPnt.Adjacent.size() >= 2)
                {
                    for(int SelfPntAdj1 = 0; SelfPntAdj1 < SelfPnt.Adjacent.size(); SelfPntAdj1++)   
                    {
                        Point SelfPntAdjPnt1 = SelfPnt.Adjacent.get(SelfPntAdj1);

                        for(int SelfPntAdj2 = 0; SelfPntAdj2 < SelfPnt.Adjacent.size(); SelfPntAdj2++)
                        {
                            if(SelfPntAdj1 == SelfPntAdj2) 
                                continue;

                            Point SelfPntAdjPnt2 = SelfPnt.Adjacent.get(SelfPntAdj2);

                            for(int Lvl1Loop = 0; Lvl1Loop < SelfPntAdjPnt1.Adjacent.size(); Lvl1Loop++)
                            {
                                if(SelfPntAdjPnt2.equals(SelfPntAdjPnt1.Adjacent.get(Lvl1Loop)))
                                {
                                    float SelfToAdj1Dist = SelfPnt.getDist(SelfPntAdjPnt1);
                                    float Adj1ToAdj2Dist = SelfPntAdjPnt1.getDist(SelfPntAdjPnt2);
                                    float SelfToAdj2Dist = SelfPnt.getDist(SelfPntAdjPnt2);

                                    if(SelfPnt.Adjacent.size() == 2)
                                    {
                                        if(SelfToAdj1Dist > SelfToAdj2Dist)
                                            SelfPnt.DeleteAndRepairAdjs(SelfPntAdjPnt1, SelfPntAdjPnt2);
                                        else
                                            SelfPnt.DeleteAndRepairAdjs(SelfPntAdjPnt2, SelfPntAdjPnt1);
                                    }
                                    else if(SelfPntAdjPnt1.Adjacent.size() == 2)
                                    {
                                        if(SelfToAdj1Dist > Adj1ToAdj2Dist)
                                            SelfPntAdjPnt1.DeleteAndRepairAdjs(SelfPnt, SelfPntAdjPnt2);
                                        else
                                            SelfPntAdjPnt1.DeleteAndRepairAdjs(SelfPntAdjPnt2, SelfPnt);    
                                    }
                                    else if(SelfPntAdjPnt2.Adjacent.size() == 2)
                                    {
                                        if(SelfToAdj2Dist > Adj1ToAdj2Dist)
                                            SelfPntAdjPnt2.DeleteAndRepairAdjs(SelfPnt, SelfPntAdjPnt1);
                                        else
                                            SelfPntAdjPnt2.DeleteAndRepairAdjs(SelfPntAdjPnt1, SelfPnt);   
                                    }
                                    else
                                    {
                                        if(SelfToAdj1Dist > SelfToAdj2Dist)
                                        {
                                            if(SelfToAdj1Dist > Adj1ToAdj2Dist)
                                                SelfPntAdjPnt1.DeleteAndRepairAdjs(SelfPnt, SelfPntAdjPnt2);
                                            else
                                                SelfPntAdjPnt1.DeleteAndRepairAdjs(SelfPntAdjPnt2, SelfPnt);
                                        }
                                        else
                                        {
                                            if(SelfToAdj2Dist > Adj1ToAdj2Dist)
                                                SelfPntAdjPnt2.DeleteAndRepairAdjs(SelfPnt, SelfPntAdjPnt1);
                                            else
                                                SelfPntAdjPnt2.DeleteAndRepairAdjs(SelfPntAdjPnt1, SelfPnt);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if(PrintPoints)
            PrintLtr(riDCJob, Ltr, PrintLine, GREEN, DotWithAdjNo);
    }

    public void GetMaxLetterSize(ReadImage riEPCob, int LtrSize[])
    {
        int MaxLtHeight = 0, MaxLtWidth = 0;

        for(int Row = 0; Row < 2; Row++)
        {
            for(int Col = 0; Col < 13; Col++)
            {
                int CrntLtHeight = riEPCob.Dist(riEPCob.iLetter[Row][Col][1][0][0], riEPCob.iLetter[Row][Col][1][0][1], riEPCob.iLetter[Row][Col][1][3][0], riEPCob.iLetter[Row][Col][1][3][1]);
                CrntLtHeight = (CrntLtHeight + riEPCob.Dist(riEPCob.iLetter[Row][Col][1][1][0], riEPCob.iLetter[Row][Col][1][1][1], riEPCob.iLetter[Row][Col][1][2][0], riEPCob.iLetter[Row][Col][1][2][1])) / 2;

                int CrntLtWidth = riEPCob.Dist(riEPCob.iLetter[Row][Col][1][0][0], riEPCob.iLetter[Row][Col][1][0][1], riEPCob.iLetter[Row][Col][1][1][0], riEPCob.iLetter[Row][Col][1][1][1]);
                CrntLtWidth = (CrntLtWidth + riEPCob.Dist(riEPCob.iLetter[Row][Col][1][3][0], riEPCob.iLetter[Row][Col][1][3][1], riEPCob.iLetter[Row][Col][1][2][0], riEPCob.iLetter[Row][Col][1][2][1])) / 2;

                if(CrntLtHeight > MaxLtHeight)
                    MaxLtHeight = CrntLtHeight;

                if(CrntLtWidth > MaxLtWidth)    
                    MaxLtWidth = CrntLtWidth;
            }
        }

        LtrSize[0] = MaxLtHeight;
        LtrSize[1] = MaxLtWidth;
    }

    public boolean ChkSurroundPix(ReadImage riCSPob, int ChkPixRow, int ChkPixCol, int WhPixTolerence)
    {
        int WhPixCnt = 0, MaxWhPixCnt = 0;

            for(int Index = 0; Index < CHK_CONSECUTIVE_PIX.length; Index++)
            {
                    if((riCSPob.PixelsRed[ChkPixRow + CHK_CONSECUTIVE_PIX[Index][ROW]][ChkPixCol + CHK_CONSECUTIVE_PIX[Index][COL]] == 255))
                        WhPixCnt++;
                    else
                    {
                        if(WhPixCnt > WhPixTolerence)
                            return false;

                        if(WhPixCnt > MaxWhPixCnt)
                            MaxWhPixCnt = WhPixCnt;

                        WhPixCnt = 0;
                    }
             
            }
        
        return true;
    }

    public boolean ChkUnwantedPix(ReadImage riCUPob, int ChkPixRow, int ChkPixCol)
    {
        boolean isUnwantedPix = false;

        // TODO : Complete This Function to remove Pix No (Row = 50, Col = 271) from FontData2.bmp

        for(int Row = -1; Row <= 1; Row++)
        {    
            for(int Col = -1; Col <= 1; Col++)
            {
                if(Row == 0 && Col == 0)
                    continue;
                
                if((riCUPob.PixelsRed[ChkPixRow + Row][ChkPixCol + Col] == 255))
                {
                }
            }
        }

        return isUnwantedPix;
    }

    public void ExtractPointsBestNew(ReadImage riEPCob, int LtrCor[][], Letter Ltr, boolean PrintPoints) throws PixelDistOutOfBoundsException, WrongTravelInitializerException
    {
        final float PutBlueLnRatio = (float)2 / 5;
        final float PutShortBlueLnRatio = (float)1 / 2;
        final float DeleteEdgeRatio = (float)1 / 5;
        final int SkipPixFrmStAndEnd_IntrsctLn = 1;
        final int WhPixTolerence = 1;
        final int LtrColCnt = Ltr.Point.size();
        final int LtrLnPntCnt = Ltr.Point.get(0).size();

        int LtHeight = 1 + (((riEPCob.Dist(LtrCor[0][0], LtrCor[0][1],  LtrCor[3][0], LtrCor[3][1])) + (riEPCob.Dist(LtrCor[1][0], LtrCor[1][1],  LtrCor[2][0], LtrCor[2][1]))) / 2);
        int LtWidth = 1 + (((riEPCob.Dist(LtrCor[0][0], LtrCor[0][1],  LtrCor[1][0], LtrCor[1][1])) + (riEPCob.Dist(LtrCor[3][0], LtrCor[3][1],  LtrCor[2][0], LtrCor[2][1]))) / 2);
                
        float ChkHtOrWdt;
        
        List <Line> VerticalLn = new ArrayList <Line> ();
        List <Line> VrticlChkLn = new ArrayList <Line> ();
        List <Line> SkipLine = new ArrayList <Line> ();
        List <Boolean> VrticlLnIsStDotJuncDot = new ArrayList <Boolean> ();
        List <Boolean> VrticlLnIsEndDotJuncDot = new ArrayList <Boolean> ();

        Quadrilateral LtrQuad = new Quadrilateral   (
                                                        new Dot(LtrCor[0][0], LtrCor[0][1]),
                                                        new Dot(LtrCor[1][0], LtrCor[1][1]),
                                                        new Dot(LtrCor[2][0], LtrCor[2][1]),
                                                        new Dot(LtrCor[3][0], LtrCor[3][1])
                                                    );
        
        for(int Method = 0; Method < 4; Method++)
        {   
            Travel LtrTravel;

            if(Method == 0)
            {
                LtrTravel = new Travel(LtrQuad, Travel.VERTICAL, Travel.BOTTOM, Travel.STEP, 1, 0);
                ChkHtOrWdt = LtHeight;
            }
            else if(Method == 1)
            {
                LtrTravel = new Travel(LtrQuad, Travel.HORIZONTAL, Travel.RIGHT, Travel.STEP, 1, 0);
                ChkHtOrWdt = LtWidth;
            }
            else if(Method == 2)
            {
                LtrTravel = new Travel(LtrQuad, Travel.VERTICAL, Travel.BOTTOM, Travel.SLOT, LtrColCnt, -1);
                ChkHtOrWdt = LtHeight;
            }
            else 
            {
                LtrTravel = new Travel(LtrQuad, Travel.HORIZONTAL, Travel.RIGHT, Travel.SLOT, LtrColCnt, -1);
                ChkHtOrWdt = LtWidth;
            }

            Dot ChkDot = LtrTravel.getNxtChkDot();
            Dot BlkDot = new Dot(), WhiteDot = new Dot();
            boolean ChkBlk = true, ChkWh = false;
            boolean PutBlueLn = false, isBlueLn = false, LnChange = true;
            Line PrevMaxDistLn = new Line(0, 0, 0, 0);
            Line TempLtrTravelChkLn = new Line(0, 0, 0, 0);

            while(ChkDot != null)
            {
                if((riEPCob.PixelsRed[ChkDot.Row][ChkDot.Col] == 0) && ChkBlk)
                {
                    BlkDot.set(ChkDot);

                    ChkWh = true;
                    ChkBlk = false;
                }
                
                if(((riEPCob.PixelsRed[ChkDot.Row][ChkDot.Col] == 255) && ChkWh) || ((LtrTravel.ChkLnCrntDist > LtrTravel.ChkLine.TotDist) && (riEPCob.PixelsRed[ChkDot.Row][ChkDot.Col] == 0)))
                {
                    WhiteDot.set(ChkDot);
                    ChkWh = false;
                    ChkBlk = true;
                    
                    int ChkBlkDist = (int)BlkDot.getDist(WhiteDot);

                    if(Method < 2)
                    {
                        if(ChkBlkDist > (ChkHtOrWdt * PutBlueLnRatio))
                        {
                            int BlkDistChk = ChkBlkDist - 1;

                            if(riEPCob.PixelsRed[WhiteDot.Row][WhiteDot.Col] == 0)
                                BlkDistChk++;

                            if((BlkDistChk) >= PrevMaxDistLn.TotDist)
                            {   
                                TempLtrTravelChkLn.setLine(LtrTravel.ChkLine, Line.DONT_SET_CRNTDOT);
                                TempLtrTravelChkLn.setCrntDot(WhiteDot);

                                if(riEPCob.PixelsRed[WhiteDot.Row][WhiteDot.Col] == 255)
                                    TempLtrTravelChkLn.getPrevDot(1);

                                PrevMaxDistLn.StDot.set(BlkDot);
                                PrevMaxDistLn.EndDot.set(TempLtrTravelChkLn.CrntDot);

                                PrevMaxDistLn.TotDist = PrevMaxDistLn.StDot.getDist(PrevMaxDistLn.EndDot);

                                PrevMaxDistLn.CrntDot.set(PrevMaxDistLn.StDot);
                                PrevMaxDistLn.CrntDotDist = 0;
                                PutBlueLn = true;

                                TempLtrTravelChkLn.setLine(LtrTravel.ChkLine, Line.DONT_SET_CRNTDOT);
                            }

                            LnChange = false;
                            isBlueLn = true;
                        }
                    }
                    else
                    {
                        /*
                            TODO : 
                            If the slots in ExtractPoints in Method == 2 or Method == 3 don't detect a Line less than the specified threshold then,
                            the line should travel till the next slot to detect any Line which is under the threshold, so that we dont miss any such 
                            line due to the slots.
                        */

                        if(ChkBlkDist <= (ChkHtOrWdt * PutShortBlueLnRatio))
                        {
                            if(!(BlkDot.equals(WhiteDot)))
                            {
                                Line ChkLine = new Line(BlkDot, WhiteDot);

                                ChkLine.getNxtDotByPixels(0);

                                boolean isIntersection = false;
                                List <Dot> PutDots = new ArrayList <Dot> ();

                                while(ChkLine.CrntDot != null)
                                {
                                    if((riEPCob.PixelsBlue[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] >= 255) && (riEPCob.PixelsGreen[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] == 0) && (riEPCob.PixelsRed[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] == 0))
                                    {
                                        if(riEPCob.PixelsBlue[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] > 255)
                                            // if(ChkSurroundPix(riEPCob, ChkLine.CrntDot.Row, ChkLine.CrntDot.Col, WhPixTolerence))
                                                AssignPoint(ChkLine.CrntDot, Method - 2, Ltr, LtrTravel.ChkLine, LtrQuad, LtrLnPntCnt, NO_REPLACE);
                                        
                                        isIntersection = true;
                                    }
                                    else if((riEPCob.PixelsBlue[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] == 100) && (riEPCob.PixelsGreen[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] == 0) && (riEPCob.PixelsRed[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] == 0))
                                    {
                                        // if(Method == 3)
                                        // {
                                        //     AssignPoint(ChkLine.CrntDot, 1, Ltr, LtrTravel.ChkLine, LtrQuad, LtrLnPntCnt, NO_REPLACE);
                                            // if(ChkSurroundPix(riEPCob, ChkLine.CrntDot.Row, ChkLine.CrntDot.Col, WhPixTolerence))
                                                PutDots.add(new Dot(ChkLine.CrntDot));
                                        // }
                                    }
                                    else if(Method == 3)
                                    {
                                        riEPCob.PixelsRed[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] = 0;
                                        riEPCob.PixelsGreen[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] = 0;
                                        riEPCob.PixelsBlue[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] = 100;
                                    }
                                    
                                    ChkLine.getNxtDotByPixels(1);

                                }

                                if(!isIntersection)
                                {
                                    if(Method == 3)
                                    {
                                        for(int PutDot = 0; PutDot < PutDots.size(); PutDot++)
                                            AssignPoint(PutDots.get(PutDot), Method, Ltr, LtrTravel.ChkLine, LtrQuad, LtrLnPntCnt, NO_REPLACE);
                                    }
                                    else 
                                    if(Method == 2)
                                    {
                                        ChkLine.setCrntDot(ChkLine.StDot);
                                        ChkLine.drawLine(riEPCob, 5);
                                    }
                                }

                                PutDots = null;
                                ChkLine = null;
                            }
                        }
                    }
                }

                ChkDot = LtrTravel.getNxtChkDot();

                if(LtrTravel.LineChange)
                {
                    ChkBlk = true;
                    ChkWh = false;
                    LnChange = true;

                    if(((PutBlueLn && (isBlueLn == false)) || (PutBlueLn && (ChkDot == null))))
                    {
                        if(Method == 0)
                        {
                            VerticalLn.add(new Line(0, 0, 0, 0));
                            VerticalLn.get(VerticalLn.size() - 1).setLine(PrevMaxDistLn, Line.DONT_SET_CRNTDOT);
                            
                            VrticlChkLn.add(new Line(0, 0, 0, 0));
                            VrticlChkLn.get(VrticlChkLn.size() - 1).setLine(TempLtrTravelChkLn, Line.DONT_SET_CRNTDOT);   

                            VrticlLnIsStDotJuncDot.add(false);
                            VrticlLnIsEndDotJuncDot.add(false);
                            /*
                                TODO :
                                The non intersected lines should be printed after skipping 1 or 2 points form both the Start and the  End.
                            */

                            PrevMaxDistLn.drawNumLine(riEPCob, 3, VerticalLn.size());
                        }
                        else if(Method == 1)
                        {   
                            List <Integer> VrticlIntrsectLnIndex = new ArrayList <Integer> ();

                            PrevMaxDistLn.getNxtDotByPixels(0);

                            Dot FirstJuncDot = new Dot();
                            Dot LastJuncDot = new Dot();
                            
                            int FirstJuncLnNo = 1000, LastJuncLnNo = 1000;

                            boolean isIntersection = false;

                            while(PrevMaxDistLn.CrntDot != null)
                            {
                                int ChkR = PrevMaxDistLn.CrntDot.Row;
                                int ChkC = PrevMaxDistLn.CrntDot.Col;

                                if((riEPCob.PixelsBlue[ChkR][ChkC] > 255) && (riEPCob.PixelsGreen[ChkR][ChkC] == 0) && (riEPCob.PixelsRed[ChkR][ChkC] == 0))
                                { 
                                    int LnIndexNo = riEPCob.PixelsBlue[ChkR][ChkC] - 256;

                                    if(FirstJuncLnNo == 1000)
                                    {
                                        FirstJuncDot.set(PrevMaxDistLn.CrntDot);
                                        FirstJuncLnNo = LnIndexNo;
                                    }

                                    LastJuncDot.set(PrevMaxDistLn.CrntDot);
                                    LastJuncLnNo = LnIndexNo;

                                    VrticlIntrsectLnIndex.add(LnIndexNo);

                                    isIntersection = true;

                                    AssignPoint(PrevMaxDistLn.CrntDot, 1, Ltr, TempLtrTravelChkLn, LtrQuad, LtrLnPntCnt, REPLACE);
                                }
                                PrevMaxDistLn.getNxtDotByPixels(1);
                            }
                            
                            PrevMaxDistLn.CrntDot = new Dot();
                            PrevMaxDistLn.setCrntDot(PrevMaxDistLn.StDot);
                            PrevMaxDistLn.drawNumLine(riEPCob, 3, 1);

                            if(isIntersection)
                            {
                                Line DupPrevMaxDistLn = new Line(PrevMaxDistLn);
                                Line DupVrtcl1stJuncDotLn = new Line(VerticalLn.get(FirstJuncLnNo));
                                Line DupVrtclLastJuncDotLn = new Line(VerticalLn.get(LastJuncLnNo));

                                boolean isStDotJuncDot = false;
                                boolean isEndDotJuncDot = false;

                                if(!(SkipLine.contains(PrevMaxDistLn)))
                                    SkipLine.add(new Line(PrevMaxDistLn));

                                for(int i = 0; i < VrticlIntrsectLnIndex.size(); i++)
                                    if(!(SkipLine.contains(VerticalLn.get(VrticlIntrsectLnIndex.get(i)))))
                                        SkipLine.add(new Line(VerticalLn.get(VrticlIntrsectLnIndex.get(i))));
                                
                                if(DupPrevMaxDistLn.StDot.getDist(FirstJuncDot) < (LtWidth * DeleteEdgeRatio))
                                {
                                    PrevMaxDistLn.StDot.set(FirstJuncDot);
                                    isStDotJuncDot = true;
                                }
                                else if((DupPrevMaxDistLn.StDot.isConnected(DupVrtcl1stJuncDotLn.StDot, riEPCob)) && (DupPrevMaxDistLn.StDot.isConnected(DupVrtcl1stJuncDotLn.EndDot, riEPCob)))
                                {
                                    PrevMaxDistLn.StDot.set(FirstJuncDot);
                                    isStDotJuncDot = true;
                                }

                                float VerticlLnStDotToJuncDotDist = DupVrtcl1stJuncDotLn.StDot.getDist(FirstJuncDot);
                                float VerticlLnEndDotToJuncDotDist = DupVrtcl1stJuncDotLn.EndDot.getDist(FirstJuncDot);

                                if((VerticlLnStDotToJuncDotDist) < (VerticlLnEndDotToJuncDotDist))
                                {
                                    if(VerticlLnStDotToJuncDotDist < (LtHeight * DeleteEdgeRatio))
                                    {
                                        VerticalLn.get(FirstJuncLnNo).StDot.set(FirstJuncDot);
                                        VrticlLnIsStDotJuncDot.set(FirstJuncLnNo, true);
                                    }
                                    else if((DupVrtcl1stJuncDotLn.StDot.isConnected(DupPrevMaxDistLn.StDot, riEPCob)) && (DupVrtcl1stJuncDotLn.StDot.isConnected(DupPrevMaxDistLn.EndDot, riEPCob)))
                                    {
                                        VerticalLn.get(FirstJuncLnNo).StDot.set(FirstJuncDot);
                                        VrticlLnIsStDotJuncDot.set(FirstJuncLnNo, true);
                                    }
                                }
                                else
                                {
                                    if(VerticlLnEndDotToJuncDotDist < (LtHeight * DeleteEdgeRatio))
                                    {
                                        VerticalLn.get(FirstJuncLnNo).EndDot.set(FirstJuncDot);
                                        VrticlLnIsEndDotJuncDot.set(FirstJuncLnNo, true);
                                    }
                                    else if((DupVrtcl1stJuncDotLn.EndDot.isConnected(DupPrevMaxDistLn.StDot, riEPCob)) && (DupVrtcl1stJuncDotLn.EndDot.isConnected(DupPrevMaxDistLn.EndDot, riEPCob)))
                                    {
                                        VerticalLn.get(FirstJuncLnNo).EndDot.set(FirstJuncDot);
                                        VrticlLnIsEndDotJuncDot.set(FirstJuncLnNo, true);
                                    }
                                }

                                if(DupPrevMaxDistLn.EndDot.getDist(LastJuncDot) < (LtWidth * DeleteEdgeRatio))
                                {
                                    PrevMaxDistLn.EndDot.set(LastJuncDot);
                                    isEndDotJuncDot = true;
                                }
                                else if((DupPrevMaxDistLn.EndDot.isConnected(DupVrtclLastJuncDotLn.StDot, riEPCob)) && (DupPrevMaxDistLn.EndDot.isConnected(DupVrtclLastJuncDotLn.EndDot, riEPCob)))
                                {
                                    PrevMaxDistLn.EndDot.set(LastJuncDot);
                                    isEndDotJuncDot = true;
                                }

                                VerticlLnStDotToJuncDotDist = DupVrtclLastJuncDotLn.StDot.getDist(LastJuncDot);
                                VerticlLnEndDotToJuncDotDist = DupVrtclLastJuncDotLn.EndDot.getDist(LastJuncDot);

                                if((VerticlLnStDotToJuncDotDist) < (VerticlLnEndDotToJuncDotDist))
                                {   
                                    if(VerticlLnStDotToJuncDotDist < (LtHeight * DeleteEdgeRatio))
                                    {
                                        VerticalLn.get(LastJuncLnNo).StDot.set(LastJuncDot);
                                        VrticlLnIsStDotJuncDot.set(LastJuncLnNo, true);
                                    }
                                    else if((DupVrtclLastJuncDotLn.StDot.isConnected(DupPrevMaxDistLn.StDot, riEPCob)) && (DupVrtclLastJuncDotLn.StDot.isConnected(DupPrevMaxDistLn.EndDot, riEPCob)))
                                    {
                                        VerticalLn.get(LastJuncLnNo).StDot.set(LastJuncDot);
                                        VrticlLnIsStDotJuncDot.set(LastJuncLnNo, true);
                                    }
                                }
                                else
                                {
                                    if(VerticlLnEndDotToJuncDotDist < (LtHeight * DeleteEdgeRatio))
                                    {
                                        VerticalLn.get(LastJuncLnNo).EndDot.set(LastJuncDot);
                                        VrticlLnIsEndDotJuncDot.set(LastJuncLnNo, true);
                                    }
                                    else if((DupVrtclLastJuncDotLn.EndDot.isConnected(DupPrevMaxDistLn.StDot, riEPCob)) && (DupVrtclLastJuncDotLn.EndDot.isConnected(DupPrevMaxDistLn.EndDot, riEPCob)))
                                    {
                                        VerticalLn.get(LastJuncLnNo).EndDot.set(LastJuncDot);
                                        VrticlLnIsEndDotJuncDot.set(LastJuncLnNo, true);
                                    }
                                }

                                if(isStDotJuncDot)
                                    AssignPoint(PrevMaxDistLn.StDot, 1, Ltr, TempLtrTravelChkLn, LtrQuad, LtrLnPntCnt, NO_REPLACE);
                                else
                                {
                                    PrevMaxDistLn.TotDist = PrevMaxDistLn.StDot.getDist(PrevMaxDistLn.EndDot);
                                    PrevMaxDistLn.setCrntDot(PrevMaxDistLn.StDot);
                                    PrevMaxDistLn.getNxtDotByPixels(SkipPixFrmStAndEnd_IntrsctLn);
                                    AssignPoint(PrevMaxDistLn.CrntDot, 1, Ltr, TempLtrTravelChkLn, LtrQuad, LtrLnPntCnt, NO_REPLACE);
                                }
                                
                                if(isEndDotJuncDot)
                                    AssignPoint(PrevMaxDistLn.EndDot, 1, Ltr, TempLtrTravelChkLn, LtrQuad, LtrLnPntCnt, NO_REPLACE);
                                else
                                {
                                    PrevMaxDistLn.TotDist = PrevMaxDistLn.StDot.getDist(PrevMaxDistLn.EndDot);
                                    PrevMaxDistLn.setCrntDot(PrevMaxDistLn.EndDot);
                                    PrevMaxDistLn.getPrevDotByPixels(SkipPixFrmStAndEnd_IntrsctLn);
                                    AssignPoint(PrevMaxDistLn.CrntDot, 1, Ltr, TempLtrTravelChkLn, LtrQuad, LtrLnPntCnt, NO_REPLACE);
                                }

                                DupPrevMaxDistLn = null;
                                DupVrtcl1stJuncDotLn = null;
                                DupVrtclLastJuncDotLn = null;
                            }

                            VrticlIntrsectLnIndex = null;
                            FirstJuncDot = null;
                            LastJuncDot = null;
                        }
                        
                        PutBlueLn = false;

                        PrevMaxDistLn.StDot.set(0, 0);
                        PrevMaxDistLn.EndDot.set(0, 0);
                        
                        PrevMaxDistLn.setCrntDot(PrevMaxDistLn.StDot);
                        PrevMaxDistLn.TotDist = 0;
                    }

                    isBlueLn = false;
                }
            }

            if(Method == 1)
            {
                for(int i = 0; i < SkipLine.size(); i++)
                {
                    SkipLine.get(i).setCrntDot(SkipLine.get(i).StDot);
                    SkipLine.get(i).drawLine(riEPCob, 3);
                }

                for(int Index = 0; Index < VerticalLn.size(); Index++)
                {
                    if((riEPCob.PixelsBlue[VerticalLn.get(Index).StDot.Row][VerticalLn.get(Index).StDot.Col] == 255) || (riEPCob.PixelsBlue[VerticalLn.get(Index).EndDot.Row][VerticalLn.get(Index).EndDot.Col] == 255))
                    {
                        Line TempLine;

                        if(VrticlLnIsStDotJuncDot.get(Index))
                            AssignPoint(VerticalLn.get(Index).StDot, 0, Ltr, VrticlChkLn.get(Index), LtrQuad, LtrLnPntCnt, NO_REPLACE);
                        else
                        {
                            TempLine = new Line(VerticalLn.get(Index));
                            TempLine.TotDist = TempLine.StDot.getDist(TempLine.EndDot);
                            TempLine.setCrntDot(TempLine.StDot);
                            TempLine.getNxtDotByPixels(SkipPixFrmStAndEnd_IntrsctLn);
                            AssignPoint(TempLine.CrntDot, 0, Ltr, VrticlChkLn.get(Index), LtrQuad, LtrLnPntCnt, NO_REPLACE);
                        }

                        if(VrticlLnIsEndDotJuncDot.get(Index))
                            AssignPoint(VerticalLn.get(Index).EndDot, 0, Ltr, VrticlChkLn.get(Index), LtrQuad, LtrLnPntCnt, NO_REPLACE);
                        else
                        {
                            TempLine = new Line(VerticalLn.get(Index));
                            TempLine.TotDist = TempLine.StDot.getDist(TempLine.EndDot);
                            TempLine.setCrntDot(TempLine.EndDot);
                            TempLine.getPrevDotByPixels(SkipPixFrmStAndEnd_IntrsctLn);
                            AssignPoint(TempLine.CrntDot, 0, Ltr, VrticlChkLn.get(Index), LtrQuad, LtrLnPntCnt, NO_REPLACE);
                        }

                        TempLine = null;
                    }
                }   
            }
        }
        
        DeleteDeadPoints(Ltr);

        if(PrintPoints)
            PrintLtr(riEPCob, Ltr, PRINT_DOT, GREEN, NO_DOT);
    }
  
    public void ExtractPoints(ReadImage riEPCob, int LtrCor[][], Letter Ltr, boolean PrintPoints) throws PixelDistOutOfBoundsException, WrongTravelInitializerException
    {
        final float PutBlueLnRatio = (float)2 / 5;
        final float PutShortBlueLnRatio = (float)2 / 5;
        final float DeleteEdgeRatio = (float)1 / 5;
        final int SkipPixFrmStAndEnd_IntrsctLn = 1;
        final int LtrColCnt = Ltr.Point.size();
        final int LtrLnPntCnt = Ltr.Point.get(0).size();

        int LtHeight = 1 + (((riEPCob.Dist(LtrCor[0][0], LtrCor[0][1],  LtrCor[3][0], LtrCor[3][1])) + (riEPCob.Dist(LtrCor[1][0], LtrCor[1][1],  LtrCor[2][0], LtrCor[2][1]))) / 2);
        int LtWidth = 1 + (((riEPCob.Dist(LtrCor[0][0], LtrCor[0][1],  LtrCor[1][0], LtrCor[1][1])) + (riEPCob.Dist(LtrCor[3][0], LtrCor[3][1],  LtrCor[2][0], LtrCor[2][1]))) / 2);
                
        float ChkHtOrWdt;
        
        List <Line> VerticalLn = new ArrayList <Line> ();
        List <Line> VrticlChkLn = new ArrayList <Line> ();
        List <Line> SkipLine = new ArrayList <Line> ();
        List <Boolean> VrticlLnIsStDotJuncDot = new ArrayList <Boolean> ();
        List <Boolean> VrticlLnIsEndDotJuncDot = new ArrayList <Boolean> ();

        Quadrilateral LtrQuad = new Quadrilateral   (
                                                        new Dot(LtrCor[0][0], LtrCor[0][1]),
                                                        new Dot(LtrCor[1][0], LtrCor[1][1]),
                                                        new Dot(LtrCor[2][0], LtrCor[2][1]),
                                                        new Dot(LtrCor[3][0], LtrCor[3][1])
                                                    );
        
        for(int Method = 0; Method < 4; Method++)
        {   
            Travel LtrTravel;

            if(Method == 0)
            {
                LtrTravel = new Travel(LtrQuad, Travel.VERTICAL, Travel.BOTTOM, Travel.STEP, 1, 0);
                ChkHtOrWdt = LtHeight;
            }
            else if(Method == 1)
            {
                LtrTravel = new Travel(LtrQuad, Travel.HORIZONTAL, Travel.RIGHT, Travel.STEP, 1, 0);
                ChkHtOrWdt = LtWidth;
            }
            else if(Method == 2)
            {
                LtrTravel = new Travel(LtrQuad, Travel.VERTICAL, Travel.BOTTOM, Travel.SLOT, LtrColCnt, -1);
                ChkHtOrWdt = LtHeight;
            }
            else 
            {
                LtrTravel = new Travel(LtrQuad, Travel.HORIZONTAL, Travel.RIGHT, Travel.SLOT, LtrColCnt, -1);
                ChkHtOrWdt = LtWidth;
            }

            Dot ChkDot = LtrTravel.getNxtChkDot();
            Dot BlkDot = new Dot(), WhiteDot = new Dot();
            boolean ChkBlk = true, ChkWh = false;
            boolean PutBlueLn = false, isBlueLn = false, LnChange = true;
            Line PrevMaxDistLn = new Line(0, 0, 0, 0);
            Line TempLtrTravelChkLn = new Line(0, 0, 0, 0);

            while(ChkDot != null)
            {
                if((riEPCob.PixelsRed[ChkDot.Row][ChkDot.Col] == 0) && ChkBlk)
                {
                    BlkDot.set(ChkDot);

                    ChkWh = true;
                    ChkBlk = false;
                }
                
                if(((riEPCob.PixelsRed[ChkDot.Row][ChkDot.Col] == 255) && ChkWh) || ((LtrTravel.ChkLnCrntDist > LtrTravel.ChkLine.TotDist) && (riEPCob.PixelsRed[ChkDot.Row][ChkDot.Col] == 0)))
                {
                    WhiteDot.set(ChkDot);
                    ChkWh = false;
                    ChkBlk = true;
                    
                    int ChkBlkDist = (int)BlkDot.getDist(WhiteDot);

                    if(Method < 2)
                    {
                        if(ChkBlkDist > (ChkHtOrWdt * PutBlueLnRatio))
                        {
                            int BlkDistChk = ChkBlkDist - 1;

                            if(riEPCob.PixelsRed[WhiteDot.Row][WhiteDot.Col] == 0)
                                BlkDistChk++;

                            if((BlkDistChk) >= PrevMaxDistLn.TotDist)
                            {   
                                TempLtrTravelChkLn.setLine(LtrTravel.ChkLine, Line.DONT_SET_CRNTDOT);
                                TempLtrTravelChkLn.setCrntDot(WhiteDot);

                                if(riEPCob.PixelsRed[WhiteDot.Row][WhiteDot.Col] == 255)
                                    TempLtrTravelChkLn.getPrevDot(1);

                                PrevMaxDistLn.StDot.set(BlkDot);
                                PrevMaxDistLn.EndDot.set(TempLtrTravelChkLn.CrntDot);

                                PrevMaxDistLn.TotDist = PrevMaxDistLn.StDot.getDist(PrevMaxDistLn.EndDot);

                                PrevMaxDistLn.CrntDot.set(PrevMaxDistLn.StDot);
                                PrevMaxDistLn.CrntDotDist = 0;
                                PutBlueLn = true;

                                TempLtrTravelChkLn.setLine(LtrTravel.ChkLine, Line.DONT_SET_CRNTDOT);
                            }

                            LnChange = false;
                            isBlueLn = true;
                        }
                    }
                    else
                    {
                        /*
                            TODO : 
                            If the slots in ExtractPoints in Method == 2 or Method == 3 don't detect a Line less than the specified threshold then,
                            the line should travel till the next slot to detect any Line which is under the threshold, so that we dont miss any such 
                            line due to the slots.
                        */

                        if(ChkBlkDist <= (ChkHtOrWdt * PutShortBlueLnRatio))
                        {
                            if(!(BlkDot.equals(WhiteDot)))
                            {
                                Line ChkLine = new Line(BlkDot, WhiteDot);

                                if(riEPCob.PixelsRed[WhiteDot.Row][WhiteDot.Col] == 255)
                                {
                                    ChkLine.setCrntDot(ChkLine.EndDot);
                                    ChkLine.getPrevDotByPixels(1);
                                    ChkLine.EndDot.set(ChkLine.CrntDot);
                                    ChkLine.TotDist = ChkLine.StDot.getDist(ChkLine.EndDot);
                                    ChkLine.setCrntDot(ChkLine.StDot);
                                }

                                if(ChkLine.TotDist != 0)
                                {
                                    ChkLine.getNxtDotByPixels(0);

                                    boolean isIntersection = false;
                                    // List <Dot> PutDots = new ArrayList <Dot> ();

                                    while(ChkLine.CrntDot != null)
                                    {
                                        if((riEPCob.PixelsBlue[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] >= 255) && (riEPCob.PixelsGreen[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] == 0) && (riEPCob.PixelsRed[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] == 0))
                                        {
                                            if(riEPCob.PixelsBlue[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] > 255)
                                                if(ChkSurroundPix(riEPCob, ChkLine.CrntDot.Row, ChkLine.CrntDot.Col, 3))
                                                    AssignPoint(ChkLine.CrntDot, Method - 2, Ltr, LtrTravel.ChkLine, LtrQuad, LtrLnPntCnt, NO_REPLACE);
                                            
                                            isIntersection = true;
                                        }
                                        else if((riEPCob.PixelsBlue[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] == 120) && (riEPCob.PixelsGreen[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] == 0) && (riEPCob.PixelsRed[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] == 0))
                                        {
                                            // PutDots.add(new Dot(ChkLine.CrntDot));
                                            isIntersection = true;
                                        }
                                        else if(Method >= 2)
                                        {
                                            riEPCob.PixelsRed[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] = 0;
                                            riEPCob.PixelsGreen[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] = 0;
                                            riEPCob.PixelsBlue[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] = 100;
                                        }
                                        
                                        ChkLine.getNxtDotByPixels(1);
                                    }

                                    if(isIntersection && Method == 2)
                                    {
                                        ChkLine.CrntDot = new Dot(ChkLine.StDot);
                                        ChkLine.getNxtDotByPixels(0);

                                        while(ChkLine.CrntDot != null)
                                        {
                                            if((riEPCob.PixelsBlue[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] < 255) && (riEPCob.PixelsGreen[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] == 0) && (riEPCob.PixelsRed[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] == 0))
                                            {    
                                                riEPCob.PixelsRed[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] = 0;
                                                riEPCob.PixelsGreen[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] = 0;
                                                riEPCob.PixelsBlue[ChkLine.CrntDot.Row][ChkLine.CrntDot.Col] = 120;
                                            }

                                            ChkLine.getNxtDotByPixels(1);
                                        }
                                    }

                                    if(!isIntersection)
                                    {
                                        Dot PutDot = new Dot((BlkDot.Row + WhiteDot.Row) / 2, (BlkDot.Col + WhiteDot.Col) / 2);

                                        // if(riEPCob.PixelsRed[WhiteDot.Row][WhiteDot.Col] == 255)
                                        // {
                                        //     ChkLine.setCrntDot(WhiteDot);
                                        //     ChkLine.getPrevDotByPixels(1);
                                        //     PutDot.set((BlkDot.Row + ChkLine.CrntDot.Row) / 2, (BlkDot.Col + ChkLine.CrntDot.Col) / 2);
                                        // }

                                        // if(Method == 3)
                                        // {
                                            
                                            // for(int PutDot = 0; PutDot < PutDots.size(); PutDot++)
                                            if(ChkSurroundPix(riEPCob, PutDot.Row, PutDot.Col, 2))
                                                AssignPoint(PutDot, Method - 2, Ltr, LtrTravel.ChkLine, LtrQuad, LtrLnPntCnt, NO_REPLACE);
                                        // }
                                        // else if(Method == 2)
                                        // {

                                            // AssignPoint(PutDot, 0, Ltr, LtrTravel.ChkLine, LtrQuad, LtrLnPntCnt, NO_REPLACE);

                                            // ChkLine.setCrntDot(ChkLine.StDot);
                                            // ChkLine.drawLine(riEPCob, 5);
                                        // }
                                    }
                                }
                                // PutDots = null;
                                ChkLine = null;
                            }
                        }
                    }
                }

                ChkDot = LtrTravel.getNxtChkDot();

                if(LtrTravel.LineChange)
                {
                    ChkBlk = true;
                    ChkWh = false;
                    LnChange = true;

                    if(((PutBlueLn && (isBlueLn == false)) || (PutBlueLn && (ChkDot == null))))
                    {
                        if(Method == 0)
                        {
                            VerticalLn.add(new Line(0, 0, 0, 0));
                            VerticalLn.get(VerticalLn.size() - 1).setLine(PrevMaxDistLn, Line.DONT_SET_CRNTDOT);
                            
                            VrticlChkLn.add(new Line(0, 0, 0, 0));
                            VrticlChkLn.get(VrticlChkLn.size() - 1).setLine(TempLtrTravelChkLn, Line.DONT_SET_CRNTDOT);   

                            VrticlLnIsStDotJuncDot.add(false);
                            VrticlLnIsEndDotJuncDot.add(false);
                            /*
                                TODO :
                                The non intersected lines should be printed after skipping 1 or 2 points form both the Start and the  End.
                            */

                            PrevMaxDistLn.drawNumLine(riEPCob, 3, VerticalLn.size());
                        }
                        else if(Method == 1)
                        {   
                            List <Integer> VrticlIntrsectLnIndex = new ArrayList <Integer> ();

                            PrevMaxDistLn.getNxtDotByPixels(0);

                            Dot FirstJuncDot = new Dot();
                            Dot LastJuncDot = new Dot();
                            
                            int FirstJuncLnNo = 1000, LastJuncLnNo = 1000;

                            boolean isIntersection = false;

                            while(PrevMaxDistLn.CrntDot != null)
                            {
                                int ChkR = PrevMaxDistLn.CrntDot.Row;
                                int ChkC = PrevMaxDistLn.CrntDot.Col;

                                if((riEPCob.PixelsBlue[ChkR][ChkC] > 255) && (riEPCob.PixelsGreen[ChkR][ChkC] == 0) && (riEPCob.PixelsRed[ChkR][ChkC] == 0))
                                { 
                                    int LnIndexNo = riEPCob.PixelsBlue[ChkR][ChkC] - 256;

                                    if(FirstJuncLnNo == 1000)
                                    {
                                        FirstJuncDot.set(PrevMaxDistLn.CrntDot);
                                        FirstJuncLnNo = LnIndexNo;
                                    }

                                    LastJuncDot.set(PrevMaxDistLn.CrntDot);
                                    LastJuncLnNo = LnIndexNo;

                                    VrticlIntrsectLnIndex.add(LnIndexNo);

                                    isIntersection = true;

                                    AssignPoint(PrevMaxDistLn.CrntDot, 1, Ltr, TempLtrTravelChkLn, LtrQuad, LtrLnPntCnt, REPLACE);
                                }
                                PrevMaxDistLn.getNxtDotByPixels(1);
                            }
                            
                            PrevMaxDistLn.CrntDot = new Dot();
                            PrevMaxDistLn.setCrntDot(PrevMaxDistLn.StDot);
                            PrevMaxDistLn.drawNumLine(riEPCob, 3, 1);

                            if(isIntersection)
                            {
                                Line DupPrevMaxDistLn = new Line(PrevMaxDistLn);
                                Line DupVrtcl1stJuncDotLn = new Line(VerticalLn.get(FirstJuncLnNo));
                                Line DupVrtclLastJuncDotLn = new Line(VerticalLn.get(LastJuncLnNo));

                                boolean isStDotJuncDot = false;
                                boolean isEndDotJuncDot = false;

                                if(!(SkipLine.contains(PrevMaxDistLn)))
                                    SkipLine.add(new Line(PrevMaxDistLn));

                                for(int i = 0; i < VrticlIntrsectLnIndex.size(); i++)
                                    if(!(SkipLine.contains(VerticalLn.get(VrticlIntrsectLnIndex.get(i)))))
                                        SkipLine.add(new Line(VerticalLn.get(VrticlIntrsectLnIndex.get(i))));
                                
                                if(DupPrevMaxDistLn.StDot.getDist(FirstJuncDot) < (LtWidth * DeleteEdgeRatio))
                                {
                                    PrevMaxDistLn.StDot.set(FirstJuncDot);
                                    isStDotJuncDot = true;
                                }
                                else if((DupPrevMaxDistLn.StDot.isConnected(DupVrtcl1stJuncDotLn.StDot, riEPCob)) && (DupPrevMaxDistLn.StDot.isConnected(DupVrtcl1stJuncDotLn.EndDot, riEPCob)))
                                {
                                    PrevMaxDistLn.StDot.set(FirstJuncDot);
                                    isStDotJuncDot = true;
                                }

                                float VerticlLnStDotToJuncDotDist = DupVrtcl1stJuncDotLn.StDot.getDist(FirstJuncDot);
                                float VerticlLnEndDotToJuncDotDist = DupVrtcl1stJuncDotLn.EndDot.getDist(FirstJuncDot);

                                if((VerticlLnStDotToJuncDotDist) < (VerticlLnEndDotToJuncDotDist))
                                {
                                    if(VerticlLnStDotToJuncDotDist < (LtHeight * DeleteEdgeRatio))
                                    {
                                        VerticalLn.get(FirstJuncLnNo).StDot.set(FirstJuncDot);
                                        VrticlLnIsStDotJuncDot.set(FirstJuncLnNo, true);
                                    }
                                    else if((DupVrtcl1stJuncDotLn.StDot.isConnected(DupPrevMaxDistLn.StDot, riEPCob)) && (DupVrtcl1stJuncDotLn.StDot.isConnected(DupPrevMaxDistLn.EndDot, riEPCob)))
                                    {
                                        VerticalLn.get(FirstJuncLnNo).StDot.set(FirstJuncDot);
                                        VrticlLnIsStDotJuncDot.set(FirstJuncLnNo, true);
                                    }
                                }
                                else
                                {
                                    if(VerticlLnEndDotToJuncDotDist < (LtHeight * DeleteEdgeRatio))
                                    {
                                        VerticalLn.get(FirstJuncLnNo).EndDot.set(FirstJuncDot);
                                        VrticlLnIsEndDotJuncDot.set(FirstJuncLnNo, true);
                                    }
                                    else if((DupVrtcl1stJuncDotLn.EndDot.isConnected(DupPrevMaxDistLn.StDot, riEPCob)) && (DupVrtcl1stJuncDotLn.EndDot.isConnected(DupPrevMaxDistLn.EndDot, riEPCob)))
                                    {
                                        VerticalLn.get(FirstJuncLnNo).EndDot.set(FirstJuncDot);
                                        VrticlLnIsEndDotJuncDot.set(FirstJuncLnNo, true);
                                    }
                                }

                                if(DupPrevMaxDistLn.EndDot.getDist(LastJuncDot) < (LtWidth * DeleteEdgeRatio))
                                {
                                    PrevMaxDistLn.EndDot.set(LastJuncDot);
                                    isEndDotJuncDot = true;
                                }
                                else if((DupPrevMaxDistLn.EndDot.isConnected(DupVrtclLastJuncDotLn.StDot, riEPCob)) && (DupPrevMaxDistLn.EndDot.isConnected(DupVrtclLastJuncDotLn.EndDot, riEPCob)))
                                {
                                    PrevMaxDistLn.EndDot.set(LastJuncDot);
                                    isEndDotJuncDot = true;
                                }

                                VerticlLnStDotToJuncDotDist = DupVrtclLastJuncDotLn.StDot.getDist(LastJuncDot);
                                VerticlLnEndDotToJuncDotDist = DupVrtclLastJuncDotLn.EndDot.getDist(LastJuncDot);

                                if((VerticlLnStDotToJuncDotDist) < (VerticlLnEndDotToJuncDotDist))
                                {   
                                    if(VerticlLnStDotToJuncDotDist < (LtHeight * DeleteEdgeRatio))
                                    {
                                        VerticalLn.get(LastJuncLnNo).StDot.set(LastJuncDot);
                                        VrticlLnIsStDotJuncDot.set(LastJuncLnNo, true);
                                    }
                                    else if((DupVrtclLastJuncDotLn.StDot.isConnected(DupPrevMaxDistLn.StDot, riEPCob)) && (DupVrtclLastJuncDotLn.StDot.isConnected(DupPrevMaxDistLn.EndDot, riEPCob)))
                                    {
                                        VerticalLn.get(LastJuncLnNo).StDot.set(LastJuncDot);
                                        VrticlLnIsStDotJuncDot.set(LastJuncLnNo, true);
                                    }
                                }
                                else
                                {
                                    if(VerticlLnEndDotToJuncDotDist < (LtHeight * DeleteEdgeRatio))
                                    {
                                        VerticalLn.get(LastJuncLnNo).EndDot.set(LastJuncDot);
                                        VrticlLnIsEndDotJuncDot.set(LastJuncLnNo, true);
                                    }
                                    else if((DupVrtclLastJuncDotLn.EndDot.isConnected(DupPrevMaxDistLn.StDot, riEPCob)) && (DupVrtclLastJuncDotLn.EndDot.isConnected(DupPrevMaxDistLn.EndDot, riEPCob)))
                                    {
                                        VerticalLn.get(LastJuncLnNo).EndDot.set(LastJuncDot);
                                        VrticlLnIsEndDotJuncDot.set(LastJuncLnNo, true);
                                    }
                                }

                                if(isStDotJuncDot)
                                    AssignPoint(PrevMaxDistLn.StDot, 1, Ltr, TempLtrTravelChkLn, LtrQuad, LtrLnPntCnt, NO_REPLACE);
                                else
                                {
                                    PrevMaxDistLn.TotDist = PrevMaxDistLn.StDot.getDist(PrevMaxDistLn.EndDot);
                                    PrevMaxDistLn.setCrntDot(PrevMaxDistLn.StDot);
                                    PrevMaxDistLn.getNxtDotByPixels(SkipPixFrmStAndEnd_IntrsctLn);
                                    AssignPoint(PrevMaxDistLn.CrntDot, 1, Ltr, TempLtrTravelChkLn, LtrQuad, LtrLnPntCnt, NO_REPLACE);
                                }
                                
                                if(isEndDotJuncDot)
                                    AssignPoint(PrevMaxDistLn.EndDot, 1, Ltr, TempLtrTravelChkLn, LtrQuad, LtrLnPntCnt, NO_REPLACE);
                                else
                                {
                                    PrevMaxDistLn.TotDist = PrevMaxDistLn.StDot.getDist(PrevMaxDistLn.EndDot);
                                    PrevMaxDistLn.setCrntDot(PrevMaxDistLn.EndDot);
                                    PrevMaxDistLn.getPrevDotByPixels(SkipPixFrmStAndEnd_IntrsctLn);
                                    AssignPoint(PrevMaxDistLn.CrntDot, 1, Ltr, TempLtrTravelChkLn, LtrQuad, LtrLnPntCnt, NO_REPLACE);
                                }

                                DupPrevMaxDistLn = null;
                                DupVrtcl1stJuncDotLn = null;
                                DupVrtclLastJuncDotLn = null;
                            }

                            VrticlIntrsectLnIndex = null;
                            FirstJuncDot = null;
                            LastJuncDot = null;
                        }
                        
                        PutBlueLn = false;

                        PrevMaxDistLn.StDot.set(0, 0);
                        PrevMaxDistLn.EndDot.set(0, 0);
                        
                        PrevMaxDistLn.setCrntDot(PrevMaxDistLn.StDot);
                        PrevMaxDistLn.TotDist = 0;
                    }

                    isBlueLn = false;
                }
            }

            if(Method == 1)
            {
                for(int i = 0; i < SkipLine.size(); i++)
                {
                    SkipLine.get(i).setCrntDot(SkipLine.get(i).StDot);
                    SkipLine.get(i).drawLine(riEPCob, 3);
                }

                for(int Index = 0; Index < VerticalLn.size(); Index++)
                {
                    if((riEPCob.PixelsBlue[VerticalLn.get(Index).StDot.Row][VerticalLn.get(Index).StDot.Col] == 255) || (riEPCob.PixelsBlue[VerticalLn.get(Index).EndDot.Row][VerticalLn.get(Index).EndDot.Col] == 255))
                    {
                        Line TempLine;

                        if(VrticlLnIsStDotJuncDot.get(Index))
                            AssignPoint(VerticalLn.get(Index).StDot, 0, Ltr, VrticlChkLn.get(Index), LtrQuad, LtrLnPntCnt, NO_REPLACE);
                        else
                        {
                            TempLine = new Line(VerticalLn.get(Index));
                            TempLine.TotDist = TempLine.StDot.getDist(TempLine.EndDot);
                            TempLine.setCrntDot(TempLine.StDot);
                            TempLine.getNxtDotByPixels(SkipPixFrmStAndEnd_IntrsctLn);
                            AssignPoint(TempLine.CrntDot, 0, Ltr, VrticlChkLn.get(Index), LtrQuad, LtrLnPntCnt, NO_REPLACE);
                        }

                        if(VrticlLnIsEndDotJuncDot.get(Index))
                            AssignPoint(VerticalLn.get(Index).EndDot, 0, Ltr, VrticlChkLn.get(Index), LtrQuad, LtrLnPntCnt, NO_REPLACE);
                        else
                        {
                            TempLine = new Line(VerticalLn.get(Index));
                            TempLine.TotDist = TempLine.StDot.getDist(TempLine.EndDot);
                            TempLine.setCrntDot(TempLine.EndDot);
                            TempLine.getPrevDotByPixels(SkipPixFrmStAndEnd_IntrsctLn);
                            AssignPoint(TempLine.CrntDot, 0, Ltr, VrticlChkLn.get(Index), LtrQuad, LtrLnPntCnt, NO_REPLACE);
                        }

                        TempLine = null;
                    }
                }   
            }
        }
        
        DeleteDeadPoints(Ltr);

        if(PrintPoints)
            PrintLtr(riEPCob, Ltr, PRINT_DOT, GREEN, NO_DOT);
    }

    public void ExtractPointsNew(ReadImage riEPCob, int LtrCor[][], Letter Ltr, boolean PrintPoints) throws PixelDistOutOfBoundsException, WrongTravelInitializerException
    {
        float PutBlueLnRatio = (float)2 / 5;
        float DeleteEdgeRatio = (float)1 / 5;
        int SkipPixFrmStAndEnd_NonIntrsctLn = 1;
        int LtrColCnt = Ltr.Point.size();
        int LtrLnPntCnt = Ltr.Point.get(0).size();

        int LtHeight = 1 + (((riEPCob.Dist(LtrCor[0][0], LtrCor[0][1],  LtrCor[3][0], LtrCor[3][1])) + (riEPCob.Dist(LtrCor[1][0], LtrCor[1][1],  LtrCor[2][0], LtrCor[2][1]))) / 2);
        int LtWidth = 1 + (((riEPCob.Dist(LtrCor[0][0], LtrCor[0][1],  LtrCor[1][0], LtrCor[1][1])) + (riEPCob.Dist(LtrCor[3][0], LtrCor[3][1],  LtrCor[2][0], LtrCor[2][1]))) / 2);
                
        float ChkHtOrWdt;
        
        List <Line> VerticalLn = new ArrayList <Line> ();
        List <Line> HorizntlLn = new ArrayList <Line> ();
        List <Line> VrticlChkLn = new ArrayList <Line> ();

        Quadrilateral LtrQuad = new Quadrilateral   (
                                                        new Dot(LtrCor[0][0], LtrCor[0][1]),
                                                        new Dot(LtrCor[1][0], LtrCor[1][1]),
                                                        new Dot(LtrCor[2][0], LtrCor[2][1]),
                                                        new Dot(LtrCor[3][0], LtrCor[3][1])
                                                    );

        for(int Method = -2; Method < 2; Method++)
        {   
            Travel LtrTravel;

            if(Method == -2)
            {
                LtrTravel = new Travel(LtrQuad, Travel.VERTICAL, Travel.BOTTOM, Travel.STEP, 1, 0);
                ChkHtOrWdt = LtHeight;
            }
            else if(Method == -1)
            {
                LtrTravel = new Travel(LtrQuad, Travel.HORIZONTAL, Travel.RIGHT, Travel.STEP, 1, 0);
                ChkHtOrWdt = LtWidth;
            }
            else if(Method == 0)
            {
                LtrTravel = new Travel(LtrQuad, Travel.VERTICAL, Travel.BOTTOM, Travel.SLOT, LtrColCnt, -1);
                ChkHtOrWdt = LtHeight;
            }
            else
            {
                LtrTravel = new Travel(LtrQuad, Travel.HORIZONTAL, Travel.RIGHT, Travel.SLOT, LtrColCnt, -1);
                ChkHtOrWdt = LtWidth;
            }
            
            Dot ChkDot = LtrTravel.getNxtChkDot();
            Dot BlkDot = new Dot(), WhiteDot = new Dot();
            boolean ChkBlk = true, ChkWh = false, ChkBluePix = false;
            boolean PutBlueLn = false, isBlueLn = false;
            Line PrevMaxDistLn = new Line(0, 0, 0, 0);
            Line PrevLtrTravelChkLn = new Line(LtrTravel.ChkLine);

            while(ChkDot != null)
            {
                if((riEPCob.PixelsRed[ChkDot.Row][ChkDot.Col] == 0) && ChkBlk)
                {
                    BlkDot.set(ChkDot);

                    ChkWh = true;
                    ChkBluePix = true;
                    ChkBlk = false;
                }
                
                if((Method >= 0) && ChkBluePix)
                {
                    if((riEPCob.PixelsBlue[ChkDot.Row][ChkDot.Col] == 128) && (riEPCob.PixelsGreen[ChkDot.Row][ChkDot.Col] == 128) && (riEPCob.PixelsRed[ChkDot.Row][ChkDot.Col] == 0))
                    {
                        AssignPoint(ChkDot, Method, Ltr, LtrTravel.ChkLine, LtrQuad, LtrLnPntCnt, NO_REPLACE);
                        ChkBluePix = false;
                    }
                }

                if(((riEPCob.PixelsRed[ChkDot.Row][ChkDot.Col] == 255) && ChkWh) || ((LtrTravel.ChkLnCrntDist > LtrTravel.ChkLine.TotDist) && (riEPCob.PixelsRed[ChkDot.Row][ChkDot.Col] == 0)))
                {
                    WhiteDot.set(ChkDot);
                    ChkWh = false;
                    ChkBlk = true;
                    
                    int ChkBlkDist = (int)BlkDot.getDist(WhiteDot);

                    if(Method >= 0)
                    {
                        if(ChkBluePix != false)
                        {
                            if(((ChkBlkDist) < (ChkHtOrWdt / 2)) || ((ChkBlkDist < ChkHtOrWdt) && (LtWidth < LtrColCnt)))
                            {
                                Dot PutDot = new Dot((int)((BlkDot.Row + WhiteDot.Row) / 2), (int)((BlkDot.Col + WhiteDot.Col) / 2));
                            
                                AssignPoint(PutDot, Method, Ltr, LtrTravel.ChkLine, LtrQuad, LtrLnPntCnt, NO_REPLACE);
                            }
                        }

                        ChkBluePix = false;
                    }
                    else
                    {
                        if(ChkBlkDist >= (ChkHtOrWdt * PutBlueLnRatio))
                        {
                            int BlkDistChk = ChkBlkDist - 3;

                            if(riEPCob.PixelsRed[WhiteDot.Row][WhiteDot.Col] == 0)
                                BlkDistChk++;

                            if((BlkDistChk) >= PrevMaxDistLn.TotDist)
                            {
                                Line TempLtrTravelChkLn = new Line(0, 0, 0, 0);

                                TempLtrTravelChkLn.setLine(LtrTravel.ChkLine, Line.DONT_SET_CRNTDOT);
                                TempLtrTravelChkLn.setCrntDot(BlkDot);
                                TempLtrTravelChkLn.getNxtDot(1);
                                PrevMaxDistLn.StDot.set(TempLtrTravelChkLn.CrntDot);

                                TempLtrTravelChkLn.setLine(LtrTravel.ChkLine, Line.DONT_SET_CRNTDOT);
                                TempLtrTravelChkLn.setCrntDot(WhiteDot);

                                if(riEPCob.PixelsRed[WhiteDot.Row][WhiteDot.Col] == 0)
                                    TempLtrTravelChkLn.getPrevDot(1);
                                else
                                    TempLtrTravelChkLn.getPrevDot(2);

                                PrevMaxDistLn.EndDot.set(TempLtrTravelChkLn.CrntDot);

                                PrevMaxDistLn.CrntDot.set(PrevMaxDistLn.StDot);
                                PrevMaxDistLn.CrntDotDist = 0;
                                PrevMaxDistLn.TotDist = PrevMaxDistLn.StDot.getDist(PrevMaxDistLn.EndDot);
                                PutBlueLn = true;
                                TempLtrTravelChkLn = null;
                            }

                            isBlueLn = true;
                        }
                    }
                }

                Line TempLtrTravelChkLn = new Line(PrevLtrTravelChkLn);
                PrevLtrTravelChkLn.setLine(LtrTravel.ChkLine, Line.DONT_SET_CRNTDOT);

                ChkDot = LtrTravel.getNxtChkDot();

                if(LtrTravel.LineChange)
                {
                    ChkBlk = true;
                    ChkWh = false;

                    if(((PutBlueLn && (isBlueLn == false)) || (PutBlueLn && (ChkDot == null))) && (Method < 0))
                    {
                        if(Method == -2)
                        {
                            VerticalLn.add(new Line(0, 0, 0, 0));
                            VerticalLn.get(VerticalLn.size() - 1).setLine(PrevMaxDistLn, Line.DONT_SET_CRNTDOT);
                            
                            VrticlChkLn.add(new Line(0, 0, 0, 0));
                            VrticlChkLn.get(VrticlChkLn.size() - 1).setLine(TempLtrTravelChkLn, Line.DONT_SET_CRNTDOT);   

                            PrevMaxDistLn.drawNumLine(riEPCob, 3, VerticalLn.size());
                        }
                        else if(Method == -1)
                        {
                            PrevMaxDistLn.getNxtDot(0);
                            
                            Dot FirstJuncDot = new Dot();
                            Dot LastJuncDot = new Dot();
                            
                            int FirstJuncLnNo = 1000, LastJuncLnNo = 1000;

                            boolean isIntersection = false;

                            while(PrevMaxDistLn.CrntDot != null)
                            {    
                                int ChkR = PrevMaxDistLn.CrntDot.Row;
                                int ChkC = PrevMaxDistLn.CrntDot.Col;

                                if((riEPCob.PixelsBlue[ChkR][ChkC] > 255) && (riEPCob.PixelsGreen[ChkR][ChkC] == 0) && (riEPCob.PixelsRed[ChkR][ChkC] == 0))
                                {
                                    int LnIndexNo = riEPCob.PixelsBlue[ChkR][ChkC] - 256;

                                    isIntersection = true;

                                    if(FirstJuncLnNo == 1000)
                                    {
                                        FirstJuncDot.set(PrevMaxDistLn.CrntDot);
                                        FirstJuncLnNo = LnIndexNo;
                                    }

                                    LastJuncDot.set(PrevMaxDistLn.CrntDot);
                                    LastJuncLnNo = LnIndexNo;

                                    AssignPoint(PrevMaxDistLn.CrntDot, 1, Ltr, TempLtrTravelChkLn, LtrQuad, LtrLnPntCnt, REPLACE);
                                }

                                PrevMaxDistLn.getNxtDot(1);
                            }

                            PrevMaxDistLn.CrntDot = new Dot(PrevMaxDistLn.StDot);
                            
                            if(isIntersection)
                            {
                                if(PrevMaxDistLn.StDot.getDist(FirstJuncDot) < (LtWidth * DeleteEdgeRatio))
                                    PrevMaxDistLn.StDot.set(FirstJuncDot);
                                else if((PrevMaxDistLn.StDot.isConnected(VerticalLn.get(FirstJuncLnNo).StDot, riEPCob)) && (PrevMaxDistLn.StDot.isConnected(VerticalLn.get(FirstJuncLnNo).EndDot, riEPCob)))
                                    PrevMaxDistLn.StDot.set(FirstJuncDot);

                                float VerticlLnStDotToJuncDotDist = VerticalLn.get(FirstJuncLnNo).StDot.getDist(FirstJuncDot);
                                float VerticlLnEndDotToJuncDotDist = VerticalLn.get(FirstJuncLnNo).EndDot.getDist(FirstJuncDot);

                                if((VerticlLnStDotToJuncDotDist) < (VerticlLnEndDotToJuncDotDist))
                                {
                                    if(VerticlLnStDotToJuncDotDist < (LtHeight * DeleteEdgeRatio))
                                        VerticalLn.get(FirstJuncLnNo).StDot.set(FirstJuncDot);
                                    else if((VerticalLn.get(FirstJuncLnNo).StDot.isConnected(PrevMaxDistLn.StDot, riEPCob)) && (VerticalLn.get(FirstJuncLnNo).StDot.isConnected(PrevMaxDistLn.EndDot, riEPCob)))
                                        VerticalLn.get(FirstJuncLnNo).StDot.set(FirstJuncDot);
                                }
                                else
                                {
                                    if(VerticlLnEndDotToJuncDotDist < (LtHeight * DeleteEdgeRatio))
                                        VerticalLn.get(FirstJuncLnNo).EndDot.set(FirstJuncDot);
                                    else if((VerticalLn.get(FirstJuncLnNo).EndDot.isConnected(PrevMaxDistLn.StDot, riEPCob)) && (VerticalLn.get(FirstJuncLnNo).EndDot.isConnected(PrevMaxDistLn.EndDot, riEPCob)))
                                        VerticalLn.get(FirstJuncLnNo).EndDot.set(FirstJuncDot);
                                }

                                if(PrevMaxDistLn.EndDot.getDist(LastJuncDot) < (LtWidth * DeleteEdgeRatio))
                                    PrevMaxDistLn.EndDot.set(LastJuncDot);
                                else if((PrevMaxDistLn.EndDot.isConnected(VerticalLn.get(LastJuncLnNo).StDot, riEPCob)) && (PrevMaxDistLn.EndDot.isConnected(VerticalLn.get(LastJuncLnNo).EndDot, riEPCob)))
                                    PrevMaxDistLn.EndDot.set(LastJuncDot);
                
                                VerticlLnStDotToJuncDotDist = VerticalLn.get(LastJuncLnNo).StDot.getDist(LastJuncDot);
                                VerticlLnEndDotToJuncDotDist = VerticalLn.get(LastJuncLnNo).EndDot.getDist(LastJuncDot);

                                if((VerticlLnStDotToJuncDotDist) < (VerticlLnEndDotToJuncDotDist))
                                {   
                                    if(VerticlLnStDotToJuncDotDist < (LtHeight * DeleteEdgeRatio))
                                        VerticalLn.get(LastJuncLnNo).StDot.set(LastJuncDot);
                                    else if((VerticalLn.get(LastJuncLnNo).StDot.isConnected(PrevMaxDistLn.StDot, riEPCob)) && (VerticalLn.get(LastJuncLnNo).StDot.isConnected(PrevMaxDistLn.EndDot, riEPCob)))
                                        VerticalLn.get(LastJuncLnNo).StDot.set(LastJuncDot);
                                }
                                else
                                {
                                    if(VerticlLnEndDotToJuncDotDist < (LtHeight * DeleteEdgeRatio))
                                        VerticalLn.get(LastJuncLnNo).EndDot.set(LastJuncDot);
                                    else if((VerticalLn.get(LastJuncLnNo).EndDot.isConnected(PrevMaxDistLn.StDot, riEPCob)) && (VerticalLn.get(LastJuncLnNo).EndDot.isConnected(PrevMaxDistLn.EndDot, riEPCob)))
                                        VerticalLn.get(LastJuncLnNo).EndDot.set(LastJuncDot);
                                }
                            }
                            
                            HorizntlLn.add(new Line(0, 0, 0, 0));
                            HorizntlLn.get(HorizntlLn.size() - 1).setLine(PrevMaxDistLn, Line.DONT_SET_CRNTDOT);

                            AssignPoint(HorizntlLn.get(HorizntlLn.size() - 1).StDot, 1, Ltr, TempLtrTravelChkLn, LtrQuad, LtrLnPntCnt, NO_REPLACE);
                            AssignPoint(HorizntlLn.get(HorizntlLn.size() - 1).EndDot, 1, Ltr, TempLtrTravelChkLn, LtrQuad, LtrLnPntCnt, NO_REPLACE);

                            HorizntlLn.get(HorizntlLn.size() - 1).drawLine(riEPCob, 4);

                            FirstJuncDot = null;
                            LastJuncDot = null;
                        }

                        PutBlueLn = false;

                        PrevMaxDistLn.StDot.set(0, 0);
                        PrevMaxDistLn.EndDot.set(0, 0);
                        
                        PrevMaxDistLn.CrntDot.set(PrevMaxDistLn.StDot);   
                        PrevMaxDistLn.CrntDotDist = 0;
                        PrevMaxDistLn.TotDist = PrevMaxDistLn.StDot.getDist(PrevMaxDistLn.EndDot);
                    }

                    isBlueLn = false;
                }
            }
            
            if(Method == -1)
                for(int Index = 0; Index < VerticalLn.size(); Index++)
                {
                    AssignPoint(VerticalLn.get(Index).StDot, 0, Ltr, VrticlChkLn.get(Index), LtrQuad, LtrLnPntCnt, NO_REPLACE);
                    AssignPoint(VerticalLn.get(Index).EndDot, 0, Ltr, VrticlChkLn.get(Index), LtrQuad, LtrLnPntCnt, NO_REPLACE);

                    VerticalLn.get(Index).drawLine(riEPCob, 4);
                }
        }

        DeleteDeadPoints(Ltr);

        if(PrintPoints)
            PrintLtr(riEPCob, Ltr, PRINT_DOT, GREEN, ADJ_DOT_1);
    }

    public void ExtractPointsOld(ReadImage riEPCob, int LtrCor[][], Letter Ltr, boolean PrintPoints) throws PixelDistOutOfBoundsException, WrongTravelInitializerException
    {
        int LtrColCnt = Ltr.Point.size();
        int LtrLnPntCnt = Ltr.Point.get(0).size();

        int LtHeight = 1 + (((riEPCob.Dist(LtrCor[0][0], LtrCor[0][1],  LtrCor[3][0], LtrCor[3][1])) + (riEPCob.Dist(LtrCor[1][0], LtrCor[1][1],  LtrCor[2][0], LtrCor[2][1]))) / 2);
        int LtWidth = 1 + (((riEPCob.Dist(LtrCor[0][0], LtrCor[0][1],  LtrCor[1][0], LtrCor[1][1])) + (riEPCob.Dist(LtrCor[3][0], LtrCor[3][1],  LtrCor[2][0], LtrCor[2][1]))) / 2);
                
        int ChkHtOrWdt;
        
        Quadrilateral LtrQuad = new Quadrilateral   (
                                                        new Dot(LtrCor[0][0], LtrCor[0][1]),
                                                        new Dot(LtrCor[1][0], LtrCor[1][1]),
                                                        new Dot(LtrCor[2][0], LtrCor[2][1]),
                                                        new Dot(LtrCor[3][0], LtrCor[3][1])
                                                    );

        for(int Method = 0; Method < 2; Method++)
        {   

            Travel LtrTravel;
            if(Method == 0)
            {
                LtrTravel = new Travel(LtrQuad, Travel.VERTICAL, Travel.BOTTOM, Travel.SLOT, LtrColCnt, -1);
                ChkHtOrWdt = LtHeight;
            }
            else
            {
                LtrTravel = new Travel(LtrQuad, Travel.HORIZONTAL, Travel.RIGHT, Travel.SLOT, LtrColCnt, -1);
                ChkHtOrWdt = LtWidth;
            }
            
            Dot ChkDot = LtrTravel.getNxtChkDot();
            Dot BlkDot = new Dot(), WhiteDot = new Dot(); 
            boolean ChkBlk = true, ChkWh = false;

            while(ChkDot != null)
            {
                if((riEPCob.PixelsRed[ChkDot.Row][ChkDot.Col] == 0) && ChkBlk)
                {
                    BlkDot.set(ChkDot);

                    ChkWh = true;
                    ChkBlk = false;
                }
                
                if(((riEPCob.PixelsRed[ChkDot.Row][ChkDot.Col] == 255) && ChkWh) || ((LtrTravel.ChkLnCrntDist > LtrTravel.ChkLine.TotDist) && (riEPCob.PixelsRed[ChkDot.Row][ChkDot.Col] == 0)))
                {
                    WhiteDot.set(ChkDot);
                    ChkWh = false;
                    ChkBlk = true;
                    
                    int ChkBlkDist = (int)BlkDot.getDist(WhiteDot);

                    if(((ChkBlkDist) < (ChkHtOrWdt / 2)) || ((ChkBlkDist < ChkHtOrWdt) && (LtWidth < LtrColCnt)))
                    {        
                        Dot PutDot = new Dot((int)((BlkDot.Row + WhiteDot.Row) / 2), (int)((BlkDot.Col + WhiteDot.Col) / 2));
                    
                        AssignPoint(PutDot, Method, Ltr, LtrTravel.ChkLine, LtrQuad, LtrLnPntCnt, NO_REPLACE);
                    }
                    else if(Method == 0)
                        new Line(BlkDot, WhiteDot).drawLine(riEPCob, 3);
                    else if(Method == 1)
                    {
                        Line HorizntlLn = new Line(BlkDot, WhiteDot);
                        
                        HorizntlLn.getNxtDotByPixels(0);

                        while(HorizntlLn.CrntDot != null)
                        {
                            int ChkR = HorizntlLn.CrntDot.Row;
                            int ChkC = HorizntlLn.CrntDot.Col;
                            
                            if((riEPCob.PixelsBlue[ChkR][ChkC] == 255) && (riEPCob.PixelsGreen[ChkR][ChkC] == 0) && (riEPCob.PixelsRed[ChkR][ChkC] == 0))
                                AssignPoint(HorizntlLn.CrntDot, Method, Ltr, LtrTravel.ChkLine, LtrQuad, LtrLnPntCnt, REPLACE);
                            
                            HorizntlLn.getNxtDotByPixels(1);
                        }
                    }    
                }
        
                ChkDot = LtrTravel.getNxtChkDot();

                if(LtrTravel.LineChange)
                {
                    ChkBlk = true;
                    ChkWh = false; 
                }
            }
            
        }

        DeleteDeadPoints(Ltr);

        if(PrintPoints)
            PrintLtr(riEPCob, Ltr, PRINT_DOT, GREEN, ADJ_DOT_1);
    }

    public void RemoveBluePixels(ReadImage riRBPob, int LtrCor[][]) throws PixelDistOutOfBoundsException, WrongTravelInitializerException
    {
        /*
            TODO : 
            You can do this part in the class ReadImage -> method ConvertArray_2Dto1D 
            by setting all the blue pixel values to red pixel values in the byte array.
        */

        Travel LtrTravel = null;

        LtrTravel = new Travel  (
                                    new Dot(LtrCor[0][0], LtrCor[0][1]), 
                                    new Dot(LtrCor[1][0], LtrCor[1][1]), 
                                    new Dot(LtrCor[2][0], LtrCor[2][1]), 
                                    new Dot(LtrCor[3][0], LtrCor[3][1]),  
                                    Travel.HORIZONTAL, 
                                    Travel.RIGHT, 
                                    Travel.STEP, 
                                    1, 
                                    0
                                );

        Dot ChkDot = LtrTravel.getNxtChkDot();
        
        while(ChkDot != null)
        {   
            // if((riRBPob.PixelsBlue[ChkDot.Row][ChkDot.Col] >= 255) || (riRBPob.PixelsBlue[ChkDot.Row][ChkDot.Col] == 100))
                riRBPob.PixelsBlue[ChkDot.Row][ChkDot.Col] = riRBPob.PixelsRed[ChkDot.Row][ChkDot.Col];
            
            // if(riRBPob.PixelsBlue[ChkDot.Row][ChkDot.Col] > 255)
            // {
            //     riRBPob.PixelsRed[ChkDot.Row][ChkDot.Col] = 0;
            //     riRBPob.PixelsGreen[ChkDot.Row][ChkDot.Col] = 128;
            //     riRBPob.PixelsBlue[ChkDot.Row][ChkDot.Col] = 128;
            // }

            ChkDot = LtrTravel.getNxtChkDot();
        }
    }

    public void FillWhPix(ReadImage riFWPob, int LtrCor[][]) throws PixelDistOutOfBoundsException, WrongTravelInitializerException
    {
        Travel LtrTravel = new Travel   (
                                            new Dot(LtrCor[0][0], LtrCor[0][1]), 
                                            new Dot(LtrCor[1][0], LtrCor[1][1]), 
                                            new Dot(LtrCor[2][0], LtrCor[2][1]), 
                                            new Dot(LtrCor[3][0], LtrCor[3][1]),  
                                            Travel.HORIZONTAL, 
                                            Travel.RIGHT, 
                                            Travel.STEP, 
                                            1, 
                                            0
                                        );

        Dot ChkDot = LtrTravel.getNxtChkDot();
        boolean LoopBrk = false;
        int WhPixCnt;
        
        while(ChkDot != null)
        {   
            LoopBrk = false;
            WhPixCnt = 0;

            if(riFWPob.PixelsRed[ChkDot.Row][ChkDot.Col] == 255)
            {   
                for(int Row = -1; Row <= 1; Row++)
                {   
                    if(((Row == -1) && (ChkDot.Row == 0)) || ((Row == 1) && (ChkDot.Row == riFWPob.getHeight())))
                        continue;

                    for(int Col = -1; Col <= 1; Col++)
                    {    
                        if(((Col == -1) && (ChkDot.Col == 0)) || ((Col == 1) && (ChkDot.Col == riFWPob.getWidth())))
                        continue;

                        if(Row == 0 && Col == 0)
                            continue;
                    
                        if((riFWPob.PixelsRed[ChkDot.Row + Row][ChkDot.Col + Col] == 0) && (riFWPob.PixelsGreen[ChkDot.Row + Row][ChkDot.Col + Col] != 1))
                        {
                            WhPixCnt++;

                            // riFWPob.PixelsRed[ChkDot.Row][ChkDot.Col] = 0;
                            // riFWPob.PixelsGreen[ChkDot.Row][ChkDot.Col] = 1;

                            // riFWPob.PixelsBlue[ChkDot.Row][ChkDot.Col] = 0;

                            // LoopBrk = true;
                            // break;
                        }
                    }

                    // if(LoopBrk)
                    //     break;
                }

                if(WhPixCnt >= 5)
                {
                    riFWPob.PixelsRed[ChkDot.Row][ChkDot.Col] = 0;
                    riFWPob.PixelsGreen[ChkDot.Row][ChkDot.Col] = 1;

                    riFWPob.PixelsBlue[ChkDot.Row][ChkDot.Col] = 0;
                }

            }

            ChkDot = LtrTravel.getNxtChkDot();
        }        
    }

    public void FillWhConesOld(ReadImage riFWCob, int LtrCor[][]) throws PixelDistOutOfBoundsException, WrongTravelInitializerException
    {
        int LtHeight = 1 + (((riFWCob.Dist(LtrCor[0][0], LtrCor[0][1],  LtrCor[3][0], LtrCor[3][1])) + (riFWCob.Dist(LtrCor[1][0], LtrCor[1][1],  LtrCor[2][0], LtrCor[2][1]))) / 2);
        int LtWidth =  1 + (((riFWCob.Dist(LtrCor[0][0], LtrCor[0][1],  LtrCor[1][0], LtrCor[1][1])) + (riFWCob.Dist(LtrCor[3][0], LtrCor[3][1],  LtrCor[2][0], LtrCor[2][1]))) / 2);
        
        final float EraseLnRatio = (float)1 / 5;
        
        List <Cone> ConeList = new ArrayList <Cone> ();
        
        Travel LtrTravel = new Travel   (
                                            new Dot(LtrCor[0][0], LtrCor[0][1]), 
                                            new Dot(LtrCor[1][0], LtrCor[1][1]), 
                                            new Dot(LtrCor[2][0], LtrCor[2][1]), 
                                            new Dot(LtrCor[3][0], LtrCor[3][1]),  
                                            Travel.HORIZONTAL, 
                                            Travel.RIGHT, 
                                            Travel.STEP, 
                                            1, 
                                            0
                                        );

        Dot ChkDot = LtrTravel.getNxtChkDot();
        boolean ChkWh = true, ChkBlk = false;
        Dot BlkDot = new Dot(), WhiteDot = new Dot();

        while(ChkDot != null)
        {
            if((riFWCob.PixelsRed[ChkDot.Row][ChkDot.Col] == 255) && ChkWh)
            {
                WhiteDot.set(ChkDot);

                ChkWh = false;
                ChkBlk = true;
            }

            if(((riFWCob.PixelsRed[ChkDot.Row][ChkDot.Col] == 0) && ChkBlk)) //|| ((LtrTravel.ChkLnCrntDist > LtrTravel.ChkLine.TotDist) && (riFWCob.PixelsRed[ChkDot.Row][ChkDot.Col] == 255)))
            {
                BlkDot.set(ChkDot);
                ChkWh = true;
                ChkBlk = false;

                Line DrawLine = new Line(WhiteDot, BlkDot);

                DrawLine.getNxtDotByPixels(0);
                boolean isConeIdentified = false;
                boolean isBlackTop = true;
                boolean isWhiteTop = true;
                int LineID = -1;
                Dot StFillDot = null;

                while(DrawLine.CrntDot != null)
                {
                    if(riFWCob.PixelsRed[DrawLine.CrntDot.Row - 1][DrawLine.CrntDot.Col] == 255)
                    {
                        isBlackTop = false;
                    }

                    if((isConeIdentified == false) && (riFWCob.PixelsRed[DrawLine.CrntDot.Row - 1][DrawLine.CrntDot.Col] == 255) && (riFWCob.PixelsBlue[DrawLine.CrntDot.Row - 1][DrawLine.CrntDot.Col] >= 500))
                    {
                        isWhiteTop = false;
                        isConeIdentified = true;
                        LineID = riFWCob.PixelsBlue[DrawLine.CrntDot.Row - 1][DrawLine.CrntDot.Col];
                        riFWCob.PixelsBlue[DrawLine.CrntDot.Row][DrawLine.CrntDot.Col] = LineID;
                        StFillDot = new Dot(DrawLine.CrntDot);
                    }

                    if(isConeIdentified)
                    {
                        riFWCob.PixelsBlue[DrawLine.CrntDot.Row][DrawLine.CrntDot.Col] = LineID;
                    }

                    if(riFWCob.PixelsRed[DrawLine.CrntDot.Row - 1][DrawLine.CrntDot.Col] == 0)
                    {}
                    if(riFWCob.PixelsBlue[DrawLine.CrntDot.Row - 1][DrawLine.CrntDot.Col] >= 500)
                    {}

                    DrawLine.getNxtDotByPixels(1);
                }
            }

            ChkDot = LtrTravel.getNxtChkDot();

            if(LtrTravel.LineChange)
            {
                ChkWh = true;
                ChkBlk = false;
            }
        }
    }

    public void FillWhCones(ReadImage riFWCob, int LtrCor[][]) throws PixelDistOutOfBoundsException, WrongTravelInitializerException
    {
        int LtHeight = 1 + (((riFWCob.Dist(LtrCor[0][0], LtrCor[0][1],  LtrCor[3][0], LtrCor[3][1])) + (riFWCob.Dist(LtrCor[1][0], LtrCor[1][1],  LtrCor[2][0], LtrCor[2][1]))) / 2);
        int LtWidth =  1 + (((riFWCob.Dist(LtrCor[0][0], LtrCor[0][1],  LtrCor[1][0], LtrCor[1][1])) + (riFWCob.Dist(LtrCor[3][0], LtrCor[3][1],  LtrCor[2][0], LtrCor[2][1]))) / 2);
        
        final float NotFillConeRatio = (float)1 / 5;
        
        List <Cone> ConeList = new ArrayList <Cone> ();
        
        Travel LtrTravel = new Travel   (
                                            new Dot(LtrCor[0][0], LtrCor[0][1]), 
                                            new Dot(LtrCor[1][0], LtrCor[1][1]), 
                                            new Dot(LtrCor[2][0], LtrCor[2][1]), 
                                            new Dot(LtrCor[3][0], LtrCor[3][1]),  
                                            Travel.HORIZONTAL, 
                                            Travel.RIGHT, 
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
            if(((riFWCob.PixelsRed[ChkDot.Row][ChkDot.Col] == 255) && (riFWCob.PixelsRed[PrevDot.Row][PrevDot.Col] == 0)) && ChkWh)
            {
                WhiteDot.set(ChkDot);

                ChkWh = false;
                ChkBlk = true;
            }

            if(((riFWCob.PixelsRed[ChkDot.Row][ChkDot.Col] == 0) && ChkBlk)) //|| ((LtrTravel.ChkLnCrntDist > LtrTravel.ChkLine.TotDist) && (riFWCob.PixelsRed[ChkDot.Row][ChkDot.Col] == 255)))
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
                    if(riFWCob.PixelsRed[ConeChkLine.CrntDot.Row - 1][ConeChkLine.CrntDot.Col] == 255)
                    {
                        isBlackTop = false;
                    }

                    if((isConeIdentified == false) && (riFWCob.PixelsRed[ConeChkLine.CrntDot.Row - 1][ConeChkLine.CrntDot.Col] == 255) && (riFWCob.PixelsBlue[ConeChkLine.CrntDot.Row - 1][ConeChkLine.CrntDot.Col] >= 500))
                    {
                        isConeIdentified = true;
                        ConeID = riFWCob.PixelsBlue[ConeChkLine.CrntDot.Row - 1][ConeChkLine.CrntDot.Col];
                        break;
                    }

                    ConeChkLine.getNxtDotByPixels(1);

                    if(ConeChkLine.CrntDot.equals(ConeChkLine.EndDot))
                        break;
                }

                if(isConeIdentified)
                {
                    ConeChkLine.setCrntDot(ConeChkLine.StDot);
                    ConeList.get(ConeID - 500).ConeLines.add(new Line(ConeChkLine));
                    ConeChkLine.drawNumberLine(riFWCob, 3, ConeID, false);
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
                    ConeChkLine.drawNumberLine(riFWCob, 3, 500 + ConeList.size() - 1, false);
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

            if(ThisCone.ConeLines.size() > (LtHeight * NotFillConeRatio))
                continue;

            Line ConeLastLn = ThisCone.ConeLines.get(ThisCone.ConeLines.size() - 1);
            boolean isBlkBotm = true;

            ConeLastLn.getNxtDotByPixels(0);

            while(ConeLastLn.CrntDot != null)
            {
                if(riFWCob.PixelsRed[ConeLastLn.CrntDot.Row + 1][ConeLastLn.CrntDot.Col] == 255)
                {
                    isBlkBotm = false;
                    break;
                }

                ConeLastLn.getNxtDotByPixels(1);
            }
            
            if(((ThisCone.TopType == Cone.BLACK_TOP) && (isBlkBotm == false)) || ((ThisCone.TopType == Cone.WHITE_TOP) && (isBlkBotm == true)))
            {
                for(int ConeLn = 0; ConeLn < ThisCone.ConeLines.size(); ConeLn++)
                {
                    ThisCone.ConeLines.get(ConeLn).setCrntDot(ThisCone.ConeLines.get(ConeLn).StDot);
                    ThisCone.ConeLines.get(ConeLn).drawNumberLine(riFWCob, 1, 0, true);
                }
            }
        }

        int Stop = 9;
    }

    public void AssignPoint(Dot PutDot, int Method, Letter Ltr, Line LtrTravelChkLn, Quadrilateral LtrQuad, int LtrLnPntCnt, boolean isReplace)
    {
        Dot StDot = new Dot();
        Dot EndDot = new Dot();

        if(Method == 0)
        {
            int PutDotAtPos = (int)((LtrTravelChkLn.StDot.getDist(PutDot) * LtrLnPntCnt) / (LtrTravelChkLn.TotDist + 1));

            float Antecedent = LtrTravelChkLn.StDot.getDist(PutDot);
            float Consequent = LtrTravelChkLn.EndDot.getDist(PutDot);

            LtrQuad.Edge[Quadrilateral.LEFT].getDot(Antecedent, Consequent, StDot);
            LtrQuad.Edge[Quadrilateral.RIGHT].getDot(Antecedent, Consequent, EndDot);

            Line RowLine = new Line(StDot, EndDot);

            int PutDotAtCol = (int)((RowLine.StDot.getDist(PutDot) * Ltr.Point.size()) / (RowLine.TotDist + 1));
            
            if((isReplace == REPLACE) || (Ltr.getPoint(PutDotAtCol, PutDotAtPos).equals(-1, -1)))
                Ltr.getPoint(PutDotAtCol, PutDotAtPos).setDot(PutDot);

            RowLine = null;
        }
        else
        {
            int PutDotAtCol = (int)((LtrTravelChkLn.StDot.getDist(PutDot) * Ltr.Point.size()) / (LtrTravelChkLn.TotDist + 1));
            
            float Antecedent = LtrTravelChkLn.StDot.getDist(PutDot);
            float Consequent = LtrTravelChkLn.EndDot.getDist(PutDot);

            LtrQuad.Edge[Quadrilateral.TOP].getDot(Antecedent, Consequent, StDot);
            LtrQuad.Edge[Quadrilateral.BOTTOM].getDot(Antecedent, Consequent, EndDot);

            Line ColLine = new Line(StDot, EndDot);
            
            int PutDotAtPos = (int)((ColLine.StDot.getDist(PutDot) * LtrLnPntCnt) / (ColLine.TotDist + 1));

            if((isReplace == REPLACE) || (Ltr.getPoint(PutDotAtCol, PutDotAtPos).equals(-1, -1)))
                Ltr.getPoint(PutDotAtCol, PutDotAtPos).setDot(PutDot);
            
            ColLine = null;
        }
        
        StDot = null;
        EndDot = null;
    }

    public void DetectSlantLine(ReadImage riDSLob, int LtrCor[][], Letter Ltr) throws PixelDistOutOfBoundsException, WrongTravelInitializerException
    { 
        int LtHeight = 1 + (((riDSLob.Dist(LtrCor[0][0], LtrCor[0][1],  LtrCor[3][0], LtrCor[3][1])) + (riDSLob.Dist(LtrCor[1][0], LtrCor[1][1],  LtrCor[2][0], LtrCor[2][1]))) / 2);
        int LtWidth =  1 + (((riDSLob.Dist(LtrCor[0][0], LtrCor[0][1],  LtrCor[1][0], LtrCor[1][1])) + (riDSLob.Dist(LtrCor[3][0], LtrCor[3][1],  LtrCor[2][0], LtrCor[2][1]))) / 2);
        
        final int PIX_TOLERENCE = 3;
        final int WIDTH_TOLERENCE = 3;
        
        Travel LtrTravel = new Travel   (
                                            new Dot(LtrCor[0][0], LtrCor[0][1]), 
                                            new Dot(LtrCor[1][0], LtrCor[1][1]), 
                                            new Dot(LtrCor[2][0], LtrCor[2][1]), 
                                            new Dot(LtrCor[3][0], LtrCor[3][1]),  
                                            Travel.HORIZONTAL, 
                                            Travel.LEFT, 
                                            Travel.STEP, 
                                            1, 
                                            0
                                        );

        Dot ChkDot = LtrTravel.getNxtChkDot();
        boolean ChkBlk = true, ChkWh = false;
        Dot BlkDot = new Dot(), WhiteDot = new Dot();
        int SlantPixCnt = 0;
        int PrevCrntDotDist = 0, PrevWidth = 0;

        while(ChkDot != null)
        {
            if((riDSLob.PixelsRed[ChkDot.Row][ChkDot.Col] == 0) && ChkBlk)
            {
                ChkBlk = false;
                ChkWh = true;
                BlkDot.set(ChkDot);

                
            }

            if((riDSLob.PixelsRed[ChkDot.Row][ChkDot.Col] == 255) && ChkWh)
            {
                ChkBlk = true;
                ChkWh = false;
                WhiteDot.set(ChkDot);


            }

            ChkDot = LtrTravel.getNxtChkDot();

            if(LtrTravel.LineChange)
            {   
                ChkBlk = true; 
                ChkWh = false;
            }
        }
    }

    public void drawCircle(ReadImage riDCob, Point StPnt, int rad) throws PixelDistOutOfBoundsException
    {
    	Point SolPnt, PrevPnt = StPnt.getPoint(rad, 0);
    	
    	for(int Ang = 0; Ang < 360; Ang++)
    	{
	        SolPnt = StPnt.getPoint(rad, Ang);
	        
	        new Line(SolPnt.Row, SolPnt.Col, PrevPnt.Row, PrevPnt.Col).drawLine(riDCob, 1);
	        
	        PrevPnt = SolPnt;
	        
	        if(Ang == 359) new Line(SolPnt.Row, SolPnt.Col, StPnt.getPoint(rad, 0).Row, StPnt.getPoint(rad, 0).Col).drawLine(riDCob, 1);
		    
	        riDCob.PixelsRed[SolPnt.Row][SolPnt.Col] = 0;
	        riDCob.PixelsGreen[SolPnt.Row][SolPnt.Col] = 0;
	        riDCob.PixelsBlue[SolPnt.Row][SolPnt.Col] = 255;
	        
	        System.out.println(riDCob.Dist(StPnt.Row, StPnt.Col, SolPnt.Row, SolPnt.Col));
    	}
        
        riDCob.PixelsRed[StPnt.Row][StPnt.Col] = 0;
        riDCob.PixelsGreen[StPnt.Row][StPnt.Col] = 0;
        riDCob.PixelsBlue[StPnt.Row][StPnt.Col] = 255;
    }
    
    public void PrintOnlyPoints(ReadImage obRiPOP)
    {
        for(int Row = 0; Row < obRiPOP.getHeight(); Row++)
        {
            for(int Col = 0; Col < obRiPOP.getWidth(); Col++)
            {
                if(!((obRiPOP.PixelsRed[Row][Col] == 0) && (obRiPOP.PixelsGreen[Row][Col] == 255) && (obRiPOP.PixelsBlue[Row][Col] == 0)))
                {
                    if((obRiPOP.PixelsRed[Row][Col] == 0) && (obRiPOP.PixelsGreen[Row][Col] == 0) && (obRiPOP.PixelsBlue[Row][Col] == 255))
                    {
                        obRiPOP.PixelsRed[Row][Col] = 255;
                        obRiPOP.PixelsGreen[Row][Col] = 0;
                        obRiPOP.PixelsBlue[Row][Col] = 0;                    
                    }
                    else if((obRiPOP.PixelsRed[Row][Col] == 0) && (obRiPOP.PixelsGreen[Row][Col] == 195) && (obRiPOP.PixelsBlue[Row][Col] == 225))
                    {
                        obRiPOP.PixelsRed[Row][Col] = 104;
                        obRiPOP.PixelsGreen[Row][Col] = 30;
                        obRiPOP.PixelsBlue[Row][Col] = 126;                    
                    }
                    else
                    {
                          obRiPOP.PixelsRed[Row][Col] = 0;
                        obRiPOP.PixelsGreen[Row][Col] = 0;
                         obRiPOP.PixelsBlue[Row][Col] = 0;                    
                    }
                }
                
            }
        }
    }
}

class Cone
{
    List <Line> ConeLines = new ArrayList <Line> ();
    boolean TopType;

    static final boolean BLACK_TOP = true;
    static final boolean WHITE_TOP = false;
}

class Intersection
{
    Dot StDot; 
    Dot EndDot;
    Dot PrevStDot; 
    Dot PrevEndDot;

    Intersection(Dot vStDot, Dot vEndDot, Dot vPrevStDot, Dot vPrevEndDot)
    {
        StDot = vStDot;
        EndDot = vEndDot;
        PrevStDot = vPrevStDot;
        PrevEndDot = vPrevEndDot;
    }

    public boolean equals(Intersection ChkIntrsct)
    {
        boolean equals = true;

        if(!(((StDot.equals(ChkIntrsct.StDot)) && (EndDot.equals(ChkIntrsct.EndDot))) || ((EndDot.equals(ChkIntrsct.StDot)) && (StDot.equals(ChkIntrsct.EndDot)))))
            equals = false;
                        
        if(!(((PrevStDot.equals(ChkIntrsct.PrevStDot)) && (PrevEndDot.equals(ChkIntrsct.PrevEndDot))) || ((PrevEndDot.equals(ChkIntrsct.PrevStDot)) && (PrevStDot.equals(ChkIntrsct.PrevEndDot)))))
            equals = false;

        return equals;
    }
}

class StPnt
{
    Point Pnt;
    int Cor;
    
    StPnt(Point vPnt, int vCor)
    {
        Pnt = vPnt;
        Cor = vCor;
    }   
}

class JunctionPnts
{
    StPnt LtStPnt[];
    int Cnt;

    JunctionPnts(int SingleAdjPntCnt)
    {
        Cnt = 0;
        LtStPnt = new StPnt[SingleAdjPntCnt];
    }
}