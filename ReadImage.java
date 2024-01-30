import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import java.awt.image.DataBufferByte;

public class ReadImage implements ExtractCharConsts
{
    BufferedImage OriginalImage;

    boolean hasAlphaChannel;
    
    int PixelsRed[][];
    int PixelsGreen[][];
    int PixelsBlue[][];
    
    int ImageCorner[][] = new int[4][2];
    
    int LineCorner[][][] = new int[100][4][2];
    int Word[][][][] = new int[10][20][4][2];
    int iLetter[][][][][] = new int[10][20][20][4][2];

    int LineCnt = 0;
    int LineWordsCnt[] = new int[20];
    int WordsLettersCnt[][] = new int[10][20];

    byte PixelData[];

    String FinalImageWhite = "Final_Font_Wh.bmp";
    String FinalImageBlack = "Final_Font_Blk.bmp";
    String FinalImageText = "SegmentedImage.bmp";

    public static void main(String args[]) throws IOException, PixelDistOutOfBoundsException, WrongTravelInitializerException
    {   
        long PrevTime = System.currentTimeMillis();

        // String ImageName = "Story.bmp";
        // String ImageName = "LongStory.bmp";
        // String ImageName = "LongStory1.bmp";
        // String ImageName = "Test.bmp";
        // String ImageName = "NewImage.bmp";
        String ImageName = "RealParagraph.bmp";
        
        String ImagePath = "./Images/" + ImageName;

        File ImageFile = new File(ImagePath);
        
        ReadImage Text = new ReadImage();
        
        Text.DeleteImage(Text.FinalImageWhite);
        Text.DeleteImage(Text.FinalImageBlack);
        Text.DeleteImage(Text.FinalImageText);

        Text.OriginalImage = ImageIO.read(ImageFile);
        
        Text.PixelData = ((DataBufferByte) Text.OriginalImage.getRaster().getDataBuffer()).getData();
        Text.hasAlphaChannel = Text.OriginalImage.getAlphaRaster() != null;
    
        Text.PixelsRed = new int[Text.getHeight()][Text.getWidth()];
        Text.PixelsGreen = new int[Text.getHeight()][Text.getWidth()];
        Text.PixelsBlue = new int[Text.getHeight()][Text.getWidth()];

        Text.ConvertArray_1Dto2D(Text);

        Text.EliminateColors(Text);

        Text.ExtractImageCorners    (Text, ImageName, false);
        Text.ExtractLines           (Text, true);

        Text.LtrSeparator(Text);

        // Text.ExtractWordsLetters    (Text, false);
        // Text.ExtractBorders         (Text, false);

        Text.ConvertArray_2Dto1D(Text, Text.FinalImageText);
        
        System.out.println("Time Taken 'Text Image Segmentation' -->>" + (System.currentTimeMillis() - PrevTime) + " mS\n");

        // elMainOb.FontMain(Text);

        System.out.println("Total ime Taken -->> " + (System.currentTimeMillis() - PrevTime) + " mS");
    }
    
    public void LtrTracerTest(ReadImage riLTTob)
    {
        LtrTracer Chingya = new LtrTracer   (
                                                new Dot(384, 371),
                                                riLTTob.PixelsRed, 
                                                riLTTob.PixelsGreen,
                                                riLTTob.PixelsBlue,
                                                30
                                            );

        Chingya.Start();

        Chingya.drawBorder();
        // Chingya.drawLeftBorder();
        // Chingya.drawRightBorder();
    }

    public void ExtractCharTest(ReadImage riECTob) throws PixelDistOutOfBoundsException, IOException, WrongTravelInitializerException
    {
        Dictionary LtrDictionary = new Dictionary();

        LtrTracer TraceLtr = new LtrTracer  (
                                                new Dot(392, 761),
                                                riECTob.PixelsRed,
                                                riECTob.PixelsGreen,
                                                riECTob.PixelsBlue,
                                                LTR_TRACER_PIX_DETECT_CONST
                                            );

        TraceLtr.Start();
        
        Image CharImg = new Image    (
                                            TraceLtr.LtrMaxVal,
                                            TraceLtr.LtrMinVal,
                                            TraceLtr.LeftTraceList,
                                            TraceLtr.RightTraceList,
                                            riECTob
                                        );

        DeleteImage("CharImage.bmp");

        ExtractChar Raanti = new ExtractChar(CharImg, TraceLtr, LtrDictionary);
        Raanti.getChar();

        LtrDictionary.UpdateDictionary();
    }
    
    public void LtrDataTest(ReadImage riLDTob) throws PixelDistOutOfBoundsException, IOException
    {
        LtrTracer Chingya = new LtrTracer   (
                                                new Dot(139, 397),
                                                riLDTob.PixelsRed,
                                                riLDTob.PixelsGreen,
                                                riLDTob.PixelsBlue,
                                                30
                                            );

        Chingya.Start();
        
        Image ChingyaImg = new Image   (
                                                Chingya.LtrMaxVal, 
                                                Chingya.LtrMinVal, 
                                                Chingya.LeftTraceList, 
                                                Chingya.RightTraceList, 
                                                riLDTob
                                            );

        ChingyaImg.createImage("ChingyaImage.bmp");

        // Chingya.drawBorder();
        // Chingya.drawLeftBorder();
        // Chingya.drawRightBorder();
    }   

    public void LtrSeparator(ReadImage riLSTob) throws PixelDistOutOfBoundsException, WrongTravelInitializerException, IOException
    {
        FileWriter ParaDecodeWriter = new FileWriter("Image Paragraph.txt");

        Dictionary LtrDictionary = new Dictionary();

        for(int Ln = 0; Ln < riLSTob.LineCnt; Ln++)
        {
            Quadrilateral DataBox = new Quadrilateral   (
                                                            LineCorner[Ln][0][0],
                                                            LineCorner[Ln][0][1],
                                                            LineCorner[Ln][1][0],
                                                            LineCorner[Ln][1][1], 
                                                            LineCorner[Ln][2][0],
                                                            LineCorner[Ln][2][1],
                                                            LineCorner[Ln][3][0],
                                                            LineCorner[Ln][3][1]
                                                        );

            LtrSeparator SeparateLtrs = new LtrSeparator(DataBox, riLSTob.PixelsRed, riLSTob.PixelsGreen, riLSTob.PixelsBlue, LtrDictionary);

            SeparateLtrs.Start(riLSTob);
            
            ParaDecodeWriter.write(SeparateLtrs.getText() + "\n");

            System.out.println("\n The Sentence --> " + SeparateLtrs.getText());

            DataBox = null;
            SeparateLtrs = null;
            LtrDictionary.UpdateDictionary();
        }

        ParaDecodeWriter.close();
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

    public void PathFinderTest(ReadImage riPFTob) throws PixelDistOutOfBoundsException
    {      
        Quadrilateral DataBox = new Quadrilateral   (
                                                        new Dot(15, 27),
                                                        new Dot(15, 52), 
                                                        new Dot(27, 52), 
                                                        new Dot(27, 27)
                                                    );

        int FLowStopVal = 30;

        DataBox.Edge[Quadrilateral.BOTTOM].drawNumberLine(riPFTob, 3, FLowStopVal, false);

        PathFinder Chingya = new PathFinder(new Dot(15, 41), PathFinder.FLOW_DOWN, FLowStopVal, riPFTob.PixelsRed, riPFTob.PixelsGreen, riPFTob.PixelsBlue);
        
        long PathSttime = System.nanoTime();

        Chingya.Start();

        Chingya.drawPath();

        System.out.println("\nTime Taken to find Path -->" + (System.nanoTime() - PathSttime) + "nS\n");

        // List <Dot> FlowPath = new ArrayList <Dot> (); 

        // Chingya.getPath(FlowPath);

        // for(int FlowIndex = 0; FlowIndex < FlowPath.size(); FlowIndex++)
        // {
        //     riPFTob.PixelsRed[FlowPath.get(FlowIndex).Row][FlowPath.get(FlowIndex).Col] = 0;
        //     riPFTob.PixelsGreen[FlowPath.get(FlowIndex).Row][FlowPath.get(FlowIndex).Col] = 255;
        //     riPFTob.PixelsBlue[FlowPath.get(FlowIndex).Row][FlowPath.get(FlowIndex).Col] = 0;
        // }
    }

    public int getWidth()
    {
        return OriginalImage.getWidth();
    }

    public int getHeight()
    {
        return OriginalImage.getHeight();
    }
    
    public void ExtractImageCorners(ReadImage EICob, String ImageName, boolean Print) throws PixelDistOutOfBoundsException
    {
        boolean LoopBrk = false;
        int RowStrt = 0, RowEnd = 0, ColStrt = 0, ColEnd = 0;
        
        for(int Row = 0; Row < EICob.getHeight(); Row++)
        {
            for(int Col = 0; Col < EICob.getWidth(); Col++)
            {
                if(EICob.PixelsRed[Row][Col] == 0)
                {
                    RowStrt = Row;
                    LoopBrk = true;
                    break;
                }
            }
            if(LoopBrk) break;
        }
        
        LoopBrk = false;
        
        for(int Row = (EICob.getHeight() - 1); Row >= 0; Row--)
        {
            for(int Col = 0; Col < EICob.getWidth(); Col++)
            {
                if(EICob.PixelsRed[Row][Col] == 0)
                {
                    RowEnd = Row;
                    LoopBrk = true;
                    break;
                }
            }
            if(LoopBrk) break;
        }
        
        LoopBrk = false;
        
        for(int Col = 0; Col < EICob.getWidth(); Col++)
        {
            for(int Row = 0; Row < EICob.getHeight(); Row++)
            {
                if(EICob.PixelsRed[Row][Col] == 0)
                {
                    ColStrt = Col;
                    LoopBrk = true;
                    break;
                }
            }
            if(LoopBrk) break;
        }
        
        LoopBrk = false;
        
        for(int Col = (EICob.getWidth() - 1); Col >= 0; Col--)
        {
            for(int Row = 0; Row < EICob.getHeight(); Row++)
            {
                if(EICob.PixelsRed[Row][Col] == 0)
                {
                    ColEnd = Col;
                    LoopBrk = true;
                    break;
                }
            }
            if(LoopBrk) break;
        }
        
        boolean Cor0Chk = true, Cor1Chk = true, Cor2Chk = true, Cor3Chk = true;
        
        for(int Row = 0; Row < EICob.getHeight(); Row++)
        {
            if(EICob.PixelsRed[Row][ColStrt] == 0)
            {
                if((Row >= ((RowEnd + RowStrt) / 2)))
                {
                    EICob.ImageCorner[3][0] = Row;
                    EICob.ImageCorner[3][1] = ColStrt;
                    Cor3Chk = false;
                }
                else if ((Row < ((RowEnd + RowStrt) / 2)) && (Cor0Chk))
                {
                    EICob.ImageCorner[0][0] = Row;
                    EICob.ImageCorner[0][1] = ColStrt;
                    Cor0Chk = false;
                }
            }
            
            if(EICob.PixelsRed[Row][ColEnd] == 0)
            {
                if((Row >= ((RowEnd + RowStrt) / 2)))
                {
                    EICob.ImageCorner[2][0] = Row;
                    EICob.ImageCorner[2][1] = ColEnd;
                    Cor2Chk = false;
                }
                else if((Row < ((RowEnd + RowStrt) / 2)) && (Cor1Chk))
                {
                    EICob.ImageCorner[1][0] = Row;
                    EICob.ImageCorner[1][1] = ColEnd;
                    Cor1Chk = false;
                }
            }
        }
        
        for(int Col = 0; Col < EICob.getWidth(); Col++)
        {
            if(EICob.PixelsRed[RowStrt][Col] == 0)
            {
                if((Col >= ((ColEnd + ColStrt) / 2)))
                {
                    EICob.ImageCorner[1][0] = RowStrt;
                    EICob.ImageCorner[1][1] = Col;
                    Cor1Chk = false;
                }
                else if((Col < ((ColEnd + ColStrt) / 2)) && (Cor0Chk))
                {
                    EICob.ImageCorner[0][0] = RowStrt;
                    EICob.ImageCorner[0][1] = Col;
                    Cor0Chk = false;
                }
            }
            
            if(EICob.PixelsRed[RowEnd][Col] == 0)
            {
                if((Col >= ((ColEnd + ColStrt) / 2)))
                {
                    EICob.ImageCorner[2][0] = RowEnd;
                    EICob.ImageCorner[2][1] = Col;
                    Cor2Chk = false;
                }
                else if((Col < ((ColEnd + ColStrt) / 2)) && (Cor3Chk))
                {
                    EICob.ImageCorner[3][0] = RowEnd;
                    EICob.ImageCorner[3][1] = Col;
                    Cor3Chk = false;
                }
            }
        }
        
        if(ImageName.equals("RealParagraph.bmp"))
        {
            EICob.ImageCorner[0][0] = 70;  EICob.ImageCorner[0][1] = 144;
            EICob.ImageCorner[1][0] = 82;  EICob.ImageCorner[1][1] = 899;
            EICob.ImageCorner[2][0] = 436; EICob.ImageCorner[2][1] = 876;
            EICob.ImageCorner[3][0] = 417; EICob.ImageCorner[3][1] = 131;        
        }
        else if(ImageName.equals("FontNew.bmp"))
        {
            EICob.ImageCorner[0][0] = 13;  EICob.ImageCorner[0][1] = 52;
            EICob.ImageCorner[1][0] = 13;  EICob.ImageCorner[1][1] = 1068;
            EICob.ImageCorner[2][0] = 314; EICob.ImageCorner[2][1] = 1020;
            EICob.ImageCorner[3][0] = 314; EICob.ImageCorner[3][1] = 3;
        }
        else if(ImageName.equals("Font.bmp"))
        {
            EICob.ImageCorner[0][0] = 13;  EICob.ImageCorner[0][1] = 3;
            EICob.ImageCorner[1][0] = 13;  EICob.ImageCorner[1][1] = 1019;
            EICob.ImageCorner[2][0] = 314; EICob.ImageCorner[2][1] = 1019;
            EICob.ImageCorner[3][0] = 314; EICob.ImageCorner[3][1] = 3;
        }
        // else if(ImageName.equals("NewImage.bmp"))
        // {
        //     EICob.ImageCorner[0][0] = 17;  EICob.ImageCorner[0][1] = 24;
        //     EICob.ImageCorner[1][0] = 17;  EICob.ImageCorner[1][1] = 949;
        //     EICob.ImageCorner[2][0] = 70;  EICob.ImageCorner[2][1] = 949;
        //     EICob.ImageCorner[3][0] = 70;  EICob.ImageCorner[3][1] = 24;
        // }
        else
        {
            EICob.ImageCorner[0][0] = 1;               EICob.ImageCorner[0][1] = 2;
            EICob.ImageCorner[1][0] = 2;               EICob.ImageCorner[1][1] = getWidth() - 1;
            EICob.ImageCorner[2][0] = getHeight() - 1; EICob.ImageCorner[2][1] = getWidth() - 2;
            EICob.ImageCorner[3][0] = getHeight() - 2; EICob.ImageCorner[3][1] = 1;
        }
        
        if(Print)
        {
            new Line(EICob.ImageCorner[0][0], EICob.ImageCorner[0][1], EICob.ImageCorner[1][0], EICob.ImageCorner[1][1]).drawLine(EICob, 1);
            new Line(EICob.ImageCorner[1][0], EICob.ImageCorner[1][1], EICob.ImageCorner[2][0], EICob.ImageCorner[2][1]).drawLine(EICob, 1);
            new Line(EICob.ImageCorner[2][0], EICob.ImageCorner[2][1], EICob.ImageCorner[3][0], EICob.ImageCorner[3][1]).drawLine(EICob, 1);
            new Line(EICob.ImageCorner[3][0], EICob.ImageCorner[3][1], EICob.ImageCorner[0][0], EICob.ImageCorner[0][1]).drawLine(EICob, 1);
        }
    }
    
    public void ConvertArray_1Dto2D(ReadImage ConvOb)
    {
        int row = 0, col = 0;
        
        if(!(ConvOb.hasAlphaChannel))
            for(int i = 0; (i + 2) < ConvOb.PixelData.length; i = i + 3)
            {
                ConvOb.PixelsRed[row][col] = 0;
                if(PixelData[i] < 0)   
                    ConvOb.PixelsRed[row][col] = (int)(ConvOb.PixelData[i]) + 256;             
                else
                    ConvOb.PixelsRed[row][col] = (int)(ConvOb.PixelData[i]);                   
                
                if(PixelData[i + 1] < 0)   
                    ConvOb.PixelsGreen[row][col] = (int)(ConvOb.PixelData[i + 1]) + 256;             
                else
                    ConvOb.PixelsGreen[row][col] = (int)(ConvOb.PixelData[i + 1]);                   
    
                if(PixelData[i + 2] < 0)   
                    ConvOb.PixelsBlue[row][col] = (int)(ConvOb.PixelData[i + 2]) + 256;               
                else
                    ConvOb.PixelsBlue[row][col] = (int)(ConvOb.PixelData[i + 2]);                     
                
                ++col;
                if(col == ConvOb.getWidth())
                {
                    col = 0;
                    ++row;
                }
            }
    }
    
    public int Dist(int Row1, int Col1, int Row2, int Col2)
    {
        return (int)(Math.sqrt(((Col2 - Col1) * (Col2 - Col1)) + ((Row2 - Row1) * (Row2 - Row1))));
    }

    public void ExtractLinesWithoutTravel(ReadImage ELob, boolean Print) throws PixelDistOutOfBoundsException
    {
        int RowStrt, RowEnd, ColStrt, ColEnd;
        
        int Dist[] = new int[2], ChkRow[] = new int[2], ChkCol[] = new int[2], ChkDist, DistDiff, MaxDist = 1, MinI = 0, PauseVal = 0;
        int MaxDistVal1 = 2, MaxDistVal2 = 1, MinDistVal1 = 3, MinDistVal2 = 0;
        
        Dist[0] = Dist(ELob.ImageCorner[0][0], ELob.ImageCorner[0][1], ELob.ImageCorner[3][0], ELob.ImageCorner[3][1]);
        Dist[1] = Dist(ELob.ImageCorner[1][0], ELob.ImageCorner[1][1], ELob.ImageCorner[2][0], ELob.ImageCorner[2][1]);
        if(Dist[0] >= Dist[1])
        {
            ChkDist = Dist[0];
            MaxDist = 0;
            MaxDistVal1 = 3;
            MaxDistVal2 = 0;
            
            MinDistVal1 = 2;
            MinDistVal2 = 1;
        }
        else ChkDist = Dist[1];    
        
        DistDiff = Math.abs(Dist[1] - Dist[0]);
        
        int ChkRowDist, ChkRowR, ChkRowC;
        boolean AllLineWh = true, ChkBlk = true;
        
        for(int MaxI = 1; MaxI <= ChkDist; MaxI++)
        {
            ChkCol[MaxDist] = ((MaxI * ELob.ImageCorner[MaxDistVal1][1]) + ((Dist[MaxDist] - MaxI) * ELob.ImageCorner[MaxDistVal2][1])) / (Dist[MaxDist]);
            ChkRow[MaxDist] = ((MaxI * ELob.ImageCorner[MaxDistVal1][0]) + ((Dist[MaxDist] - MaxI) * ELob.ImageCorner[MaxDistVal2][0])) / (Dist[MaxDist]);
            
            if((MinI == PauseVal) && (DistDiff != 0)) PauseVal = Math.round(PauseVal + (ChkDist / DistDiff));
            else MinI++;
            
            ChkCol[Math.abs(MaxDist - 1)] = ((MinI * ELob.ImageCorner[MinDistVal1][1]) + ((Dist[Math.abs(MaxDist - 1)] - MinI) * ELob.ImageCorner[MinDistVal2][1])) / (Dist[Math.abs(MaxDist - 1)]);
            ChkRow[Math.abs(MaxDist - 1)] = ((MinI * ELob.ImageCorner[MinDistVal1][0]) + ((Dist[Math.abs(MaxDist - 1)] - MinI) * ELob.ImageCorner[MinDistVal2][0])) / (Dist[Math.abs(MaxDist - 1)]);
            
            ChkRowDist = Dist(ChkRow[0], ChkCol[0], ChkRow[1], ChkCol[1]);
            AllLineWh = true;
            for(int ChkRI = 1; ChkRI <= ChkRowDist; ChkRI++)
            {
                ChkRowC = ((ChkRI * ChkCol[1]) + ((ChkRowDist - ChkRI) * ChkCol[0])) / (ChkRowDist);
                ChkRowR = ((ChkRI * ChkRow[1]) + ((ChkRowDist - ChkRI) * ChkRow[0])) / (ChkRowDist);
                
                if((ELob.PixelsRed[ChkRowR][ChkRowC] == 0) && (ChkBlk))
                {
                    ELob.LineCorner[ELob.LineCnt][0][0] = ChkRow[0];
                    ELob.LineCorner[ELob.LineCnt][1][0] = ChkRow[1];
                    
                    ELob.LineCorner[ELob.LineCnt][0][1] = ChkCol[0];
                    ELob.LineCorner[ELob.LineCnt][1][1] = ChkCol[1];
                        
                    ChkBlk = false;
                }
                
                if((ELob.PixelsRed[ChkRowR][ChkRowC] == 0) && (ChkBlk == false))
                {
                    AllLineWh = false;
                    break;
                }
            }
            
            if(AllLineWh && (ChkBlk == false))
            {
                ELob.LineCorner[ELob.LineCnt][3][0] = ChkRow[0];
                ELob.LineCorner[ELob.LineCnt][2][0] = ChkRow[1];
                
                ELob.LineCorner[ELob.LineCnt][3][1] = ChkCol[0];
                ELob.LineCorner[ELob.LineCnt][2][1] = ChkCol[1];
                
                ChkBlk = true;
                ELob.LineCnt++;
            }
        }
        
        if(ELob.LineCorner[ELob.LineCnt][2][0] == 0)
        {
            ELob.LineCorner[ELob.LineCnt][3][0] = ELob.ImageCorner[3][0];
            ELob.LineCorner[ELob.LineCnt][3][1] = ELob.ImageCorner[3][1];
            
            ELob.LineCorner[ELob.LineCnt][2][0] = ELob.ImageCorner[2][0];
            ELob.LineCorner[ELob.LineCnt][2][1] = ELob.ImageCorner[2][1];

        }

        if(Print)
            for(int Ln = 0; Ln < ELob.LineCnt; Ln++)
                PrintLnBox(ELob, ELob.LineCorner[Ln]);
    }   

    public void ExtractLines(ReadImage ELob, boolean Print) throws PixelDistOutOfBoundsException, WrongTravelInitializerException
    {   
        boolean AllLineWh = true, ChkBlk = true;
        
        Travel ImageTravel = new Travel (   
                                            new Dot(ImageCorner[0][0], ImageCorner[0][1]), 
                                            new Dot(ImageCorner[1][0], ImageCorner[1][1]), 
                                            new Dot(ImageCorner[2][0], ImageCorner[2][1]), 
                                            new Dot(ImageCorner[3][0], ImageCorner[3][1]), 
                                            Travel.HORIZONTAL, 
                                            Travel.RIGHT, 
                                            Travel.STEP, 
                                            1, 
                                            0
                                        );
        
        Dot ChkDot = ImageTravel.getNxtChkDot();

        while(ChkDot != null)
        {
            if((ELob.PixelsRed[ChkDot.Row][ChkDot.Col] == 0) && (ChkBlk))
            {
                ELob.LineCorner[ELob.LineCnt][0][0] = ImageTravel.ChkLine.StDot.Row;
                ELob.LineCorner[ELob.LineCnt][0][1] = ImageTravel.ChkLine.StDot.Col;

                ELob.LineCorner[ELob.LineCnt][1][0] = ImageTravel.ChkLine.EndDot.Row;
                ELob.LineCorner[ELob.LineCnt][1][1] = ImageTravel.ChkLine.EndDot.Col;
                    
                ChkBlk = false;
            }
            
            if((ELob.PixelsRed[ChkDot.Row][ChkDot.Col] == 0) && (ChkBlk == false))
            {
                AllLineWh = false;
                ImageTravel.getNxtChkLine();
            }

            ChkDot = ImageTravel.getNxtChkDot();

            if(ImageTravel.LineChange)
            {
                if((ChkBlk == false) && AllLineWh)
                {
                    ELob.LineCorner[ELob.LineCnt][3][0] = ImageTravel.ChkLine.StDot.Row;
                    ELob.LineCorner[ELob.LineCnt][3][1] = ImageTravel.ChkLine.StDot.Col;
    
                    ELob.LineCorner[ELob.LineCnt][2][0] = ImageTravel.ChkLine.EndDot.Row;
                    ELob.LineCorner[ELob.LineCnt][2][1] = ImageTravel.ChkLine.EndDot.Col;
                    
                    ChkBlk = true;
                    ELob.LineCnt++;
                }
                else if((ChkBlk == false) && (ChkDot == null))
                {
                    ELob.LineCorner[ELob.LineCnt][3][0] = ImageTravel.BottomLeft.Row;
                    ELob.LineCorner[ELob.LineCnt][3][1] = ImageTravel.BottomLeft.Col;
    
                    ELob.LineCorner[ELob.LineCnt][2][0] = ImageTravel.BottomRight.Row;
                    ELob.LineCorner[ELob.LineCnt][2][1] = ImageTravel.BottomRight.Col;
                    
                    ChkBlk = true;
                    ELob.LineCnt++;
                }
                
                AllLineWh = true;
            }
        }
        
        if(ELob.LineCorner[ELob.LineCnt][2][0] == 0)
        {
            ELob.LineCorner[ELob.LineCnt][3][0] = ELob.ImageCorner[3][0];
            ELob.LineCorner[ELob.LineCnt][3][1] = ELob.ImageCorner[3][1];
            
            ELob.LineCorner[ELob.LineCnt][2][0] = ELob.ImageCorner[2][0];
            ELob.LineCorner[ELob.LineCnt][2][1] = ELob.ImageCorner[2][1];
        }
        
        if(Print)
            for(int Ln = 0; Ln < ELob.LineCnt; Ln++)
                PrintLnBox(ELob, ELob.LineCorner[Ln]);
    }   

    public void PrintLnBox(ReadImage riPLBob, int LnBoxCor[][]) throws PixelDistOutOfBoundsException
    {
        new Line(LnBoxCor[0][0], LnBoxCor[0][1], LnBoxCor[1][0], LnBoxCor[1][1]).drawLine(riPLBob, 1);
        new Line(LnBoxCor[1][0], LnBoxCor[1][1], LnBoxCor[2][0], LnBoxCor[2][1]).drawLine(riPLBob, 1);
        new Line(LnBoxCor[2][0], LnBoxCor[2][1], LnBoxCor[3][0], LnBoxCor[3][1]).drawLine(riPLBob, 1);
        new Line(LnBoxCor[3][0], LnBoxCor[3][1], LnBoxCor[0][0], LnBoxCor[0][1]).drawLine(riPLBob, 1);
    }

    public void ExtractBorders(ReadImage EBob, boolean Print) throws PixelDistOutOfBoundsException
    {
        for(int Line = 0; Line < EBob.LineCnt; Line++)
        {   
            for(int Word = 0; Word < EBob.LineWordsCnt[Line]; Word++)
            {
                for(int Letter = 0; Letter < EBob.WordsLettersCnt[Line][Word]; Letter++)
                {
                    int Dist[] = new int[2], ChkRow[] = new int[2], ChkCol[] = new int[2];
                    
                    int ChkDist, DistDiff, MaxDist = 1, MinI = 0, PauseVal = 0;
                    int MaxDistVal1 = 1, MaxDistVal2 = 2, MinDistVal1 = 0, MinDistVal2 = 3;
                    
                    Dist[0] = Dist(EBob.iLetter[Line][Word][Letter][0][0], EBob.iLetter[Line][Word][Letter][0][1], EBob.iLetter[Line][Word][Letter][3][0], EBob.iLetter[Line][Word][Letter][3][1]);
                    Dist[1] = Dist(EBob.iLetter[Line][Word][Letter][1][0], EBob.iLetter[Line][Word][Letter][1][1], EBob.iLetter[Line][Word][Letter][2][0], EBob.iLetter[Line][Word][Letter][2][1]);
                    if(Dist[0] >= Dist[1])
                    {
                        ChkDist = Dist[0];
                        MaxDist = 0;
                        MaxDistVal1 = 0;
                        MaxDistVal2 = 3;
                        
                        MinDistVal1 = 1;
                        MinDistVal2 = 2;
                    }
                    else 
                        ChkDist = Dist[1];    
                    
                    DistDiff = Math.abs(Dist[1] - Dist[0]);
                    
                    int ChkRowDist, ChkRowR, ChkRowC, LastBlkLn[][] = new int[2][2];
                    boolean ChkBlk = true, ChkWh = false;
                    
                    for(int MaxI = 1; MaxI < ChkDist; MaxI++)
                    {
                        ChkCol[MaxDist] = ((MaxI * EBob.iLetter[Line][Word][Letter][MaxDistVal2][1]) + ((Dist[MaxDist] - MaxI) * EBob.iLetter[Line][Word][Letter][MaxDistVal1][1])) / (Dist[MaxDist]);
                        ChkRow[MaxDist] = ((MaxI * EBob.iLetter[Line][Word][Letter][MaxDistVal2][0]) + ((Dist[MaxDist] - MaxI) * EBob.iLetter[Line][Word][Letter][MaxDistVal1][0])) / (Dist[MaxDist]);
                        
                        if((MinI == PauseVal) && (DistDiff != 0)) PauseVal = Math.round(PauseVal + (ChkDist / DistDiff));
                        else MinI++;
                        
                        ChkCol[Math.abs(MaxDist - 1)] = ((MinI * EBob.iLetter[Line][Word][Letter][MinDistVal2][1]) + ((Dist[Math.abs(MaxDist - 1)] - MinI) * EBob.iLetter[Line][Word][Letter][MinDistVal1][1])) / (Dist[Math.abs(MaxDist - 1)]);
                        ChkRow[Math.abs(MaxDist - 1)] = ((MinI * EBob.iLetter[Line][Word][Letter][MinDistVal2][0]) + ((Dist[Math.abs(MaxDist - 1)] - MinI) * EBob.iLetter[Line][Word][Letter][MinDistVal1][0])) / (Dist[Math.abs(MaxDist - 1)]);
                        
                        ChkRowDist = Dist(ChkRow[0], ChkCol[0], ChkRow[1], ChkCol[1]);
                        for(int ChkRI = 1; ChkRI < ChkRowDist; ChkRI++)
                        {
                            ChkRowC = ((ChkRI * ChkCol[1]) + ((ChkRowDist - ChkRI) * ChkCol[0])) / (ChkRowDist);
                            ChkRowR = ((ChkRI * ChkRow[1]) + ((ChkRowDist - ChkRI) * ChkRow[0])) / (ChkRowDist);
                            
                            if((EBob.PixelsRed[ChkRowR][ChkRowC] == 0) && ChkBlk)
                            {
                                EBob.iLetter[Line][Word][Letter][0][0] = ChkRow[0];
                                EBob.iLetter[Line][Word][Letter][1][0] = ChkRow[1];
                                
                                EBob.iLetter[Line][Word][Letter][0][1] = ChkCol[0];
                                EBob.iLetter[Line][Word][Letter][1][1] = ChkCol[1];
                                
                                ChkBlk = false;
                                ChkWh = true;
                            }
                            
                            if((EBob.PixelsRed[ChkRowR][ChkRowC] == 0) && ChkWh)
                            {
                                LastBlkLn[0][0] = ChkRow[0];
                                LastBlkLn[0][1] = ChkCol[0];
                                
                                LastBlkLn[1][0] = ChkRow[1];
                                LastBlkLn[1][1] = ChkCol[1];
                            }
                        }
                    }
                
                    EBob.iLetter[Line][Word][Letter][3][0] = LastBlkLn[0][0];
                    EBob.iLetter[Line][Word][Letter][2][0] = LastBlkLn[1][0];
                    
                    EBob.iLetter[Line][Word][Letter][3][1] = LastBlkLn[0][1];
                    EBob.iLetter[Line][Word][Letter][2][1] = LastBlkLn[1][1];

                    if(Print)
                        PrintBorder(EBob, EBob.iLetter[Line][Word][Letter]);
                }
            }
        }
    }

    public void PrintBorder(ReadImage riPBob, int LtrCor[][]) throws PixelDistOutOfBoundsException
    {        
        try
        {
            new Line(LtrCor[0][0] - 1, LtrCor[0][1] - 1, LtrCor[1][0] - 1, LtrCor[1][1] + 1).drawLine(riPBob, 1);
            new Line(LtrCor[1][0] - 1, LtrCor[1][1] + 1, LtrCor[2][0] + 1, LtrCor[2][1] + 1).drawLine(riPBob, 1);
            new Line(LtrCor[2][0] + 1, LtrCor[2][1] + 1, LtrCor[3][0] + 1, LtrCor[3][1] - 1).drawLine(riPBob, 1);
            new Line(LtrCor[3][0] + 1, LtrCor[3][1] - 1, LtrCor[0][0] - 1, LtrCor[0][1] - 1).drawLine(riPBob, 1);
        }
        catch(Exception e)
        {
            // TODO : Remove this Exception
            int Stop = 9;
        }
    }

    public void ExtractWordsLetters(ReadImage EWLob, boolean Print) throws PixelDistOutOfBoundsException
    {
        for(int Line = 0; Line < EWLob.LineCnt; Line++)
        {   
            int Dist[] = new int[2], ChkRow[] = new int[2], ChkCol[] = new int[2];
            
            int ChkDist, DistDiff, MaxDist = 1, MinI = 0, PauseVal = 0;
            int MaxDistVal1 = 3, MaxDistVal2 = 2, MinDistVal1 = 0, MinDistVal2 = 1;
            
            Dist[0] = Dist(EWLob.LineCorner[Line][0][0], EWLob.LineCorner[Line][0][1], EWLob.LineCorner[Line][1][0], EWLob.LineCorner[Line][1][1]);
            Dist[1] = Dist(EWLob.LineCorner[Line][3][0], EWLob.LineCorner[Line][3][1], EWLob.LineCorner[Line][2][0], EWLob.LineCorner[Line][2][1]);
            if(Dist[0] >= Dist[1])
            {
                ChkDist = Dist[0];
                MaxDist = 0;
                MaxDistVal1 = 0;
                MaxDistVal2 = 1;
                
                MinDistVal1 = 3;
                MinDistVal2 = 2;
            }
            else ChkDist = Dist[1];    
            
            DistDiff = Math.abs(Dist[1] - Dist[0]);
            
            int ChkRowDist, ChkRowR, ChkRowC, PrevCol = EWLob.LineCorner[Line][0][1], WordCnt = -1, LetterCnt = 0, WhiteLineCnt = 1, PrevWordVal[][] = new int[2][2];
            boolean AllLineWh = true, ChkBlk = true, FrstBlk = false;
            int BlkPixCnt = 0, BlkCnt;
            
            for(int MaxI = 1; MaxI <= ChkDist; MaxI++)
            {
                ChkCol[MaxDist] = ((MaxI * EWLob.LineCorner[Line][MaxDistVal2][1]) + ((Dist[MaxDist] - MaxI) * EWLob.LineCorner[Line][MaxDistVal1][1])) / (Dist[MaxDist]);
                ChkRow[MaxDist] = ((MaxI * EWLob.LineCorner[Line][MaxDistVal2][0]) + ((Dist[MaxDist] - MaxI) * EWLob.LineCorner[Line][MaxDistVal1][0])) / (Dist[MaxDist]);
                
                if((MinI == PauseVal) && (DistDiff != 0)) PauseVal = Math.round(PauseVal + (ChkDist / DistDiff));
                else MinI++;
                
                ChkCol[Math.abs(MaxDist - 1)] = ((MinI * EWLob.LineCorner[Line][MinDistVal2][1]) + ((Dist[Math.abs(MaxDist - 1)] - MinI) * EWLob.LineCorner[Line][MinDistVal1][1])) / (Dist[Math.abs(MaxDist - 1)]);
                ChkRow[Math.abs(MaxDist - 1)] = ((MinI * EWLob.LineCorner[Line][MinDistVal2][0]) + ((Dist[Math.abs(MaxDist - 1)] - MinI) * EWLob.LineCorner[Line][MinDistVal1][0])) / (Dist[Math.abs(MaxDist - 1)]);
                
                ChkRowDist = Dist(ChkRow[0], ChkCol[0], ChkRow[1], ChkCol[1]);
                AllLineWh = true; BlkPixCnt = 0; BlkCnt = 0;
                for(int ChkRI = 1; ChkRI <= ChkRowDist; ChkRI++)
                {
                    ChkRowC = ((ChkRI * ChkCol[1]) + ((ChkRowDist - ChkRI) * ChkCol[0])) / (ChkRowDist);
                    ChkRowR = ((ChkRI * ChkRow[1]) + ((ChkRowDist - ChkRI) * ChkRow[0])) / (ChkRowDist);
                       
                    if((EWLob.PixelsRed[ChkRowR][ChkRowC] == 0) && (ChkBlk))
                    {
                        if(WordCnt == -1)
                        {
                            WordCnt++;
                            
                            EWLob.Word[Line][WordCnt][0][0] = ChkRow[0];
                            EWLob.Word[Line][WordCnt][0][1] = ChkCol[0];
                            
                            EWLob.Word[Line][WordCnt][3][0] = ChkRow[1];
                            EWLob.Word[Line][WordCnt][3][1] = ChkCol[1];
                        }
                        
                        if(WhiteLineCnt > 6)
                        {
                            WordCnt++;
                            
                            EWLob.Word[Line][WordCnt][0][0] = ChkRow[0];
                            EWLob.Word[Line][WordCnt][0][1] = ChkCol[0];
                            
                            EWLob.Word[Line][WordCnt][3][0] = ChkRow[1];
                            EWLob.Word[Line][WordCnt][3][1] = ChkCol[1];
                            LetterCnt = 0;
                            
                            LetterCnt++;
                            
                            EWLob.iLetter[Line][WordCnt][LetterCnt][0][0] = ChkRow[0];
                            EWLob.iLetter[Line][WordCnt][LetterCnt][0][1] = ChkCol[0];
                            
                            EWLob.iLetter[Line][WordCnt][LetterCnt][3][0] = ChkRow[1];
                            EWLob.iLetter[Line][WordCnt][LetterCnt][3][1] = ChkCol[1];
                        }
                        else
                        {
                            LetterCnt++;
                            
                            EWLob.iLetter[Line][WordCnt][LetterCnt][0][0] = ChkRow[0];
                            EWLob.iLetter[Line][WordCnt][LetterCnt][0][1] = ChkCol[0];
                            
                            EWLob.iLetter[Line][WordCnt][LetterCnt][3][0] = ChkRow[1];
                            EWLob.iLetter[Line][WordCnt][LetterCnt][3][1] = ChkCol[1];
                        }    
                        
                        FrstBlk = true;
                        ChkBlk = false;
                        WhiteLineCnt = 0;
                    }
                    
                    if((EWLob.PixelsRed[ChkRowR][ChkRowC] == 0) && (ChkBlk == false))
                    {
                        AllLineWh = false;
                        break;
                    }
                }
                
                if((AllLineWh) && FrstBlk) WhiteLineCnt++;
                
                if((AllLineWh) && (ChkBlk == false))
                {
                    PrevCol = ChkCol[0];
                    
                    int ColDiff = 1;
                    if(BlkPixCnt == 1)  ColDiff = 0;
                    
                    EWLob.iLetter[Line][WordCnt][LetterCnt][1][0] = ChkRow[0];
                    EWLob.iLetter[Line][WordCnt][LetterCnt][1][1] = ChkCol[0] - ColDiff;
                    
                    EWLob.iLetter[Line][WordCnt][LetterCnt][2][0] = ChkRow[1];
                    EWLob.iLetter[Line][WordCnt][LetterCnt][2][1] = ChkCol[1] - ColDiff;
            
                    PrevWordVal[0][0] = ChkRow[0];
                    PrevWordVal[0][1] = ChkCol[0];
                    
                    PrevWordVal[1][0] = ChkRow[1];
                    PrevWordVal[1][1] = ChkCol[1];
                    
                    ChkBlk = true;
                }
            
                if(((WhiteLineCnt > 6) || (MaxI == ChkDist)) && (WordCnt >= 0))
                {
                    EWLob.Word[Line][WordCnt][1][0] = PrevWordVal[0][0];
                    EWLob.Word[Line][WordCnt][1][1] = PrevWordVal[0][1];
                    
                    EWLob.Word[Line][WordCnt][2][0] = PrevWordVal[1][0];
                    EWLob.Word[Line][WordCnt][2][1] = PrevWordVal[1][1];
                
                    WordsLettersCnt[Line][WordCnt] = LetterCnt + 1;
                }
            }
            
            EWLob.LineWordsCnt[Line] = WordCnt + 1;
        }
        
        if(Print)
            for(int Line = 0; Line < EWLob.LineCnt; Line++)
                for(int Word = 0; Word < EWLob.LineWordsCnt[Line]; Word++)
                    for(int Letter = 0; Letter < EWLob.WordsLettersCnt[Line][Word]; Letter++)
                    {
                        try
                        {
                            new Line(EWLob.iLetter[Line][Word][Letter][0][0], EWLob.iLetter[Line][Word][Letter][0][1], EWLob.iLetter[Line][Word][Letter][1][0], EWLob.iLetter[Line][Word][Letter][1][1]).drawLine(EWLob, 1);
                            new Line(EWLob.iLetter[Line][Word][Letter][1][0], EWLob.iLetter[Line][Word][Letter][1][1], EWLob.iLetter[Line][Word][Letter][2][0], EWLob.iLetter[Line][Word][Letter][2][1]).drawLine(EWLob, 1);
                            new Line(EWLob.iLetter[Line][Word][Letter][2][0], EWLob.iLetter[Line][Word][Letter][2][1], EWLob.iLetter[Line][Word][Letter][3][0], EWLob.iLetter[Line][Word][Letter][3][1]).drawLine(EWLob, 1);
                            new Line(EWLob.iLetter[Line][Word][Letter][3][0], EWLob.iLetter[Line][Word][Letter][3][1], EWLob.iLetter[Line][Word][Letter][0][0], EWLob.iLetter[Line][Word][Letter][0][1]).drawLine(EWLob, 1);
                        }
                        catch(Exception e)
                        {
                            // TODO : Remove this Exception
                            int Stop = 9;
                        }
                    }            
    }
    
    public void ExtractWordsLettersWithTravel(ReadImage EWLob, boolean Print) throws PixelDistOutOfBoundsException, WrongTravelInitializerException
    {
        for(int Line = 0; Line < EWLob.LineCnt; Line++)
        {
            Travel SentenceTravel = new Travel  (
                                                    new Dot(EWLob.LineCorner[Line][0][0], EWLob.LineCorner[Line][0][1]),
                                                    new Dot(EWLob.LineCorner[Line][1][0], EWLob.LineCorner[Line][1][1]),
                                                    new Dot(EWLob.LineCorner[Line][2][0], EWLob.LineCorner[Line][2][1]),
                                                    new Dot(EWLob.LineCorner[Line][3][0], EWLob.LineCorner[Line][3][1]),
                                                    Travel.VERTICAL, 
                                                    Travel.BOTTOM, 
                                                    Travel.STEP, 
                                                    1, 
                                                    0
                                                );

            Dot ChkDot = SentenceTravel.getNxtChkDot();

            int WordCnt = -1, LetterCnt = 0, WhiteLineCnt = 1, PrevWordVal[][] = new int[2][2];
            boolean AllLineWh = true, ChkBlk = true, FrstBlk = false;
            int BlkPixCnt = 0, BlkCnt;
            
            while(ChkDot != null)
            {
                if(SentenceTravel.LineChange)
                {}
            }
        }
    }
    
    public int Min(int Num1, int Num2)
    {
        int Min;
        
        Min = (Num1 <= Num2)? Num1 : Num2;
        
        return Min;
    }
    
    public void ExtractWordsLettersNew(ReadImage EWLNob) throws PixelDistOutOfBoundsException
    {
        for(int Line = 0; Line < EWLNob.LineCnt; Line++)
        {   
            int Dist[] = new int[2], ChkRow[] = new int[2], ChkCol[] = new int[2];
            
            int ChkDist, DistDiff, MaxDist = 1, MinI = 0, PauseVal = 0;
            int MaxDistVal1 = 3, MaxDistVal2 = 2, MinDistVal1 = 0, MinDistVal2 = 1;
            
            Dist[0] = Dist(EWLNob.LineCorner[Line][0][0], EWLNob.LineCorner[Line][0][1], EWLNob.LineCorner[Line][1][0], EWLNob.LineCorner[Line][1][1]);
            Dist[1] = Dist(EWLNob.LineCorner[Line][3][0], EWLNob.LineCorner[Line][3][1], EWLNob.LineCorner[Line][2][0], EWLNob.LineCorner[Line][2][1]);
            if(Dist[0] >= Dist[1])
            {
                ChkDist = Dist[0];
                MaxDist = 0;
                MaxDistVal1 = 0;
                MaxDistVal2 = 1;
                
                MinDistVal1 = 3;
                MinDistVal2 = 2;
            }
            else ChkDist = Dist[1];    
            
            DistDiff = Math.abs(Dist[1] - Dist[0]);
            
            int ChkRowDist, ChkRowR, ChkRowC, PrevCol = EWLNob.LineCorner[Line][0][1], WordCnt = -1, LetterCnt = 0, WhiteLineCnt = 1, PrevWordVal[][] = new int[2][2];
            boolean AllLineWh = true, ChkBlk = true, FrstBlk = false , LoopBrk;
            int BlkPixCnt = 0, BlkCnt, SkipCol = -1;
            
            for(int MaxI = 1; MaxI <= ChkDist; MaxI++)
            {
                ChkCol[MaxDist] = ((MaxI * EWLNob.LineCorner[Line][MaxDistVal2][1]) + ((Dist[MaxDist] - MaxI) * EWLNob.LineCorner[Line][MaxDistVal1][1])) / (Dist[MaxDist]);
                ChkRow[MaxDist] = ((MaxI * EWLNob.LineCorner[Line][MaxDistVal2][0]) + ((Dist[MaxDist] - MaxI) * EWLNob.LineCorner[Line][MaxDistVal1][0])) / (Dist[MaxDist]);
                
                if((MinI == PauseVal) && (DistDiff != 0)) PauseVal = Math.round(PauseVal + (ChkDist / DistDiff));
                else MinI++;
                
                ChkCol[Math.abs(MaxDist - 1)] = ((MinI * EWLNob.LineCorner[Line][MinDistVal2][1]) + ((Dist[Math.abs(MaxDist - 1)] - MinI) * EWLNob.LineCorner[Line][MinDistVal1][1])) / (Dist[Math.abs(MaxDist - 1)]);
                ChkRow[Math.abs(MaxDist - 1)] = ((MinI * EWLNob.LineCorner[Line][MinDistVal2][0]) + ((Dist[Math.abs(MaxDist - 1)] - MinI) * EWLNob.LineCorner[Line][MinDistVal1][0])) / (Dist[Math.abs(MaxDist - 1)]);
                
                LoopBrk = false;
                for(int Col0 = -3; Col0 <= 3; Col0++)
                {
                    for(int Col1 = -3; Col1 <= 3; Col1++)
                    {
                        if((Min((ChkCol[0] + Col0), (ChkCol[1] + Col1))) < SkipCol)
                            continue;
                            
                        ChkRowDist = Dist(ChkRow[0], (ChkCol[0] + Col0), ChkRow[1],(ChkCol[1] + Col1));
                        AllLineWh = true; BlkPixCnt = 0; BlkCnt = 0;
                        for(int ChkRI = 1; ChkRI <= ChkRowDist; ChkRI++)
                        {
                            ChkRowC = ((ChkRI * (ChkCol[1] + Col1)) + ((ChkRowDist - ChkRI) * (ChkCol[0] + Col0))) / (ChkRowDist);
                            ChkRowR = ((ChkRI * ChkRow[1]) + ((ChkRowDist - ChkRI) * ChkRow[0])) / (ChkRowDist);
                               
                            if((EWLNob.PixelsRed[ChkRowR][ChkRowC] == 0) && (ChkBlk))
                            {
                                if(WordCnt == -1)
                                {
                                    WordCnt++;
                                    
                                    EWLNob.Word[Line][WordCnt][0][0] = ChkRow[0];
                                    EWLNob.Word[Line][WordCnt][0][1] = ChkCol[0];
                                    
                                    EWLNob.Word[Line][WordCnt][3][0] = ChkRow[1];
                                    EWLNob.Word[Line][WordCnt][3][1] = ChkCol[1];
                                }
                                
                                if(WhiteLineCnt > 6)
                                {
                                    WordCnt++;

                                    EWLNob.Word[Line][WordCnt][0][0] = ChkRow[0];
                                    EWLNob.Word[Line][WordCnt][0][1] = ChkCol[0];

                                    EWLNob.Word[Line][WordCnt][3][0] = ChkRow[1];
                                    EWLNob.Word[Line][WordCnt][3][1] = ChkCol[1];
                                    LetterCnt = 0;
                                    
                                    LetterCnt++;
                                    
                                    EWLNob.iLetter[Line][WordCnt][LetterCnt][0][0] = ChkRow[0];
                                    EWLNob.iLetter[Line][WordCnt][LetterCnt][0][1] = ChkCol[0];
                                    
                                    EWLNob.iLetter[Line][WordCnt][LetterCnt][3][0] = ChkRow[1];
                                    EWLNob.iLetter[Line][WordCnt][LetterCnt][3][1] = ChkCol[1];
                                }
                                else
                                {
                                    LetterCnt++;
                                    try
                                    {
                                        EWLNob.iLetter[Line][WordCnt][LetterCnt][0][0] = ChkRow[0];
                                        EWLNob.iLetter[Line][WordCnt][LetterCnt][0][1] = ChkCol[0];
                                        
                                        EWLNob.iLetter[Line][WordCnt][LetterCnt][3][0] = ChkRow[1];
                                        EWLNob.iLetter[Line][WordCnt][LetterCnt][3][1] = ChkCol[1];
                                    }
                                    catch(Exception e)
                                    {
                                        // TODO : Remove this Exception
                                        int Exception = 9;
                                    }
                                }    
                                
                                FrstBlk = true;
                                ChkBlk = false;
                                WhiteLineCnt = 0;
                            }
                            
                            if((EWLNob.PixelsRed[ChkRowR][ChkRowC] == 0) && (ChkBlk == false))
                            {
                                AllLineWh = false;
                                break;
                            }
                        }
                        
                        if((AllLineWh) && FrstBlk) 
                        {
                            WhiteLineCnt++;
                            LoopBrk = true;
                        }
                        
                        if((AllLineWh) && (ChkBlk == false))
                        {
                            PrevCol = ChkCol[0] + Col0;
                            
                            SkipCol = Min((ChkCol[0] + Col0), (ChkCol[1] + Col1));
                            
                            // int ColDiff = 1;
                            // if(BlkPixCnt == 1)  ColDiff = 0;
                            
                            EWLNob.iLetter[Line][WordCnt][LetterCnt][1][0] = ChkRow[0];
                            EWLNob.iLetter[Line][WordCnt][LetterCnt][1][1] = ChkCol[0] - 1;
                            
                            EWLNob.iLetter[Line][WordCnt][LetterCnt][2][0] = ChkRow[1];
                            EWLNob.iLetter[Line][WordCnt][LetterCnt][2][1] = ChkCol[1] - 1;
                    
                            PrevWordVal[0][0] = ChkRow[0];
                            PrevWordVal[0][1] = ChkCol[0];
                            
                            PrevWordVal[1][0] = ChkRow[1];
                            PrevWordVal[1][1] = ChkCol[1];
                            
                            ChkBlk = true;
                        }
                    
                        if(((WhiteLineCnt > 6) || (MaxI == ChkDist)) && (WordCnt >= 0))
                        {
                            EWLNob.Word[Line][WordCnt][1][0] = PrevWordVal[0][0];
                            EWLNob.Word[Line][WordCnt][1][1] = PrevWordVal[0][1];
                            
                            EWLNob.Word[Line][WordCnt][2][0] = PrevWordVal[1][0];
                            EWLNob.Word[Line][WordCnt][2][1] = PrevWordVal[1][1];
                            
                            EWLNob.WordsLettersCnt[Line][WordCnt] = LetterCnt + 1;
                        }
                        
                        if(LoopBrk) break;
                    }
                    if(LoopBrk) break;
                }
            }
            
            EWLNob.LineWordsCnt[Line] = WordCnt + 1;
        }
        
        for(int Ln = 0; Ln <= EWLNob.LineCnt; Ln++)
            for(int Wrd = 0; Wrd <= LineWordsCnt[Ln]; Wrd++)
            {
                
                for(int Ltr = 0; Ltr <= WordsLettersCnt[Ln][Wrd]; Ltr++)
                {
                    new Line(EWLNob.iLetter[Ln][Wrd][Ltr][0][0], EWLNob.iLetter[Ln][Wrd][Ltr][0][1], EWLNob.iLetter[Ln][Wrd][Ltr][1][0], EWLNob.iLetter[Ln][Wrd][Ltr][1][1]).drawLine(EWLNob, 1);
                    new Line(EWLNob.iLetter[Ln][Wrd][Ltr][1][0], EWLNob.iLetter[Ln][Wrd][Ltr][1][1], EWLNob.iLetter[Ln][Wrd][Ltr][2][0], EWLNob.iLetter[Ln][Wrd][Ltr][2][1]).drawLine(EWLNob, 1);
                    new Line(EWLNob.iLetter[Ln][Wrd][Ltr][2][0], EWLNob.iLetter[Ln][Wrd][Ltr][2][1], EWLNob.iLetter[Ln][Wrd][Ltr][3][0], EWLNob.iLetter[Ln][Wrd][Ltr][3][1]).drawLine(EWLNob, 1);
                    new Line(EWLNob.iLetter[Ln][Wrd][Ltr][3][0], EWLNob.iLetter[Ln][Wrd][Ltr][3][1], EWLNob.iLetter[Ln][Wrd][Ltr][0][0], EWLNob.iLetter[Ln][Wrd][Ltr][0][1]).drawLine(EWLNob, 1);
                }
            }    
    }
        
    public void EliminateColors(ReadImage ECob)
    {
        int LowestVal, HighestVal;
        int CheckVal;
        
        int DivideRowBlks = 6;
        
        int ChkBlkH = (int)(ECob.getHeight() / DivideRowBlks), ChkBlkW = (int)(ECob.getWidth() / DivideRowBlks);
        int ChkRowE = 0, ChkColE = 0;
        
        for(int RowS = 0; RowS < ECob.getHeight(); RowS += ChkBlkH)
        {
            for(int ColS = 0; ColS < ECob.getWidth(); ColS += ChkBlkW)
            {
                ChkRowE = RowS + ChkBlkH;
                ChkColE = ColS + ChkBlkW;
                LowestVal = 255; HighestVal = 0; CheckVal = 127;
                
                if(ChkRowE > ECob.getHeight()) ChkRowE = ECob.getHeight();
                if(ChkColE > ECob.getWidth()) ChkColE = ECob.getWidth();
                
                for(int i = RowS; i < ChkRowE; i++)
                {
                    for(int j = ColS; j < ChkColE; j++)
                    {
                        int iGray = 0;
                        
                        iGray =  (ECob.PixelsRed[i][j] + ECob.PixelsGreen[i][j] + ECob.PixelsBlue[i][j]) / 3;
                        if(iGray < LowestVal)
                            LowestVal = iGray;
                        if(iGray > HighestVal)
                            HighestVal = iGray;
                    } 
                } 
                
                CheckVal = ((int)(((HighestVal + LowestVal) * 2) / 3));
                
                for(int i = RowS; i < ChkRowE; i++)
                {
                    for(int j = ColS; j < ChkColE; j++)
                    {
                        int iGray = 0;
                        
                        iGray =  (ECob.PixelsRed[i][j] + ECob.PixelsGreen[i][j] + ECob.PixelsBlue[i][j]) / 3;
                        
                        if(iGray > (127 + 80))
                        {   
                            ECob.PixelsRed[i][j] = 255;
                            ECob.PixelsGreen[i][j] = 255;
                            ECob.PixelsBlue[i][j] = 255;
                        }
                        else
                        {
                            ECob.PixelsRed[i][j] = 0;
                            ECob.PixelsGreen[i][j] = 0;
                            ECob.PixelsBlue[i][j] = 0;
                        }
                    } 
                }
                /*
                System.out.println("\n=============================================================");
                System.out.println("HighestVal = " + HighestVal);
                System.out.println("LowestVal = " + LowestVal);
                System.out.println("CheckVal = " + CheckVal);
                System.out.println("\nRowS = " + RowS);
                System.out.println("ChkRowE = " + ChkRowE);
                System.out.println("\nColS = " + ColS);
                System.out.println("ChkColE = " + ChkColE);
                System.out.println("=============================================================");
                */
            }
        }
    }
    
    public void EliminateColors2()
    {
        int LowestVal, HighestVal;
        int CheckVal;
        
        int ChkBlkH = (int)(getHeight() / 4), ChkBlkW = (int)(getWidth() / 4);
        int ChkRowE = 0, ChkColE = 0;
        
        long AvgVal;
        int PixCnt;
        
        for(int RowS = 0; RowS < getHeight(); RowS += ChkBlkH)
        {
            for(int ColS = 0; ColS < getWidth(); ColS += ChkBlkW)
            {
                ChkRowE = RowS + ChkBlkH;
                ChkColE = ColS + ChkBlkW;
                LowestVal = 255; HighestVal = 0; CheckVal = 127;
                AvgVal = 0; PixCnt = 0;
                
                if(ChkRowE > getHeight()) ChkRowE = getHeight();
                if(ChkColE > getWidth()) ChkColE = getWidth();
                
                for(int i = RowS; i < ChkRowE; i++)
                {
                    for(int j = ColS; j < ChkColE; j++)
                    {
                        int iGray = 0;
                        
                        iGray =  (PixelsRed[i][j] + PixelsGreen[i][j] + PixelsBlue[i][j]) / 3;
                        
                        AvgVal += iGray;
                        ++PixCnt;
                        
                        if(iGray < LowestVal)
                            LowestVal = iGray;
                        if(iGray > HighestVal)
                            HighestVal = iGray;
                    } 
                } 
                
                AvgVal = (long)(AvgVal / PixCnt);
                
                // CheckVal = ((int)((HighestVal + LowestVal) / 2));
                CheckVal = (int)AvgVal;
                CheckVal = 255 - CheckVal;
                
                for(int i = RowS; i < ChkRowE; i++)
                {
                    for(int j = ColS; j < ChkColE; j++)
                    {
                        int iGray = 0;
                        
                        iGray =  (PixelsRed[i][j] + PixelsGreen[i][j] + PixelsBlue[i][j]) / 3;
                        
                        if(iGray > CheckVal)
                        {   
                            PixelsRed[i][j] = 255;
                            PixelsGreen[i][j] = 255;
                            PixelsBlue[i][j] = 255;
                        }
                        else
                        {
                            PixelsRed[i][j] = 0;
                            PixelsGreen[i][j] = 0;
                            PixelsBlue[i][j] = 0;
                        }
                    } 
                }
                System.out.println("\n=============================================================");
                System.out.println("HighestVal = " + HighestVal);
                System.out.println("LowestVal = " + LowestVal);
                System.out.println("CheckVal = " + CheckVal);
                System.out.println("\nRowS = " + RowS);
                System.out.println("ChkRowE = " + ChkRowE);
                System.out.println("\nColS = " + ColS);
                System.out.println("ChkColE = " + ChkColE);
                System.out.println("=============================================================");
            }
        }
    }
    
    public void ConvertArray_2Dto1D(ReadImage CovertOb, String ImgName) throws IOException
    {
        CovertOb.PixelsRed[ImageCorner[0][0]][ImageCorner[0][1]] = 0;
        CovertOb.PixelsRed[ImageCorner[1][0]][ImageCorner[1][1]] = 0;
        CovertOb.PixelsRed[ImageCorner[2][0]][ImageCorner[2][1]] = 0;
        CovertOb.PixelsRed[ImageCorner[3][0]][ImageCorner[3][1]] = 0;
        
        CovertOb.PixelsGreen[ImageCorner[0][0]][ImageCorner[0][1]] = 0;
        CovertOb.PixelsGreen[ImageCorner[1][0]][ImageCorner[1][1]] = 0;
        CovertOb.PixelsGreen[ImageCorner[2][0]][ImageCorner[2][1]] = 0;
        CovertOb.PixelsGreen[ImageCorner[3][0]][ImageCorner[3][1]] = 0;
        
        CovertOb.PixelsBlue[ImageCorner[0][0]][ImageCorner[0][1]] = 255;
        CovertOb.PixelsBlue[ImageCorner[1][0]][ImageCorner[1][1]] = 255;
        CovertOb.PixelsBlue[ImageCorner[2][0]][ImageCorner[2][1]] = 255;
        CovertOb.PixelsBlue[ImageCorner[3][0]][ImageCorner[3][1]] = 255; 
        
        //=========================================================================================
        
        byte EditedImage[] = new byte[CovertOb.getWidth() * CovertOb.getHeight() * 3];
        
        int Cnt = 0;
        
        for(int Row = 0; Row < CovertOb.getHeight(); Row++)
        {
            for(int Col = 0; Col < CovertOb.getWidth(); Col++)
            {
                if(CovertOb.PixelsRed[Row][Col] > 127)
                    EditedImage[Cnt] = (byte)(CovertOb.PixelsRed[Row][Col] - 256);
                else    
                    EditedImage[Cnt] = (byte)(CovertOb.PixelsRed[Row][Col]);
                Cnt++;
                
                if(CovertOb.PixelsGreen[Row][Col] > 127)
                    EditedImage[Cnt] = (byte)(CovertOb.PixelsGreen[Row][Col] - 256);
                else    
                    EditedImage[Cnt] = (byte)(CovertOb.PixelsGreen[Row][Col]);
                Cnt++;
                
                if(CovertOb.PixelsBlue[Row][Col] > 127)
                    EditedImage[Cnt] = (byte)(CovertOb.PixelsBlue[Row][Col] - 256);
                else    
                    EditedImage[Cnt] = (byte)(CovertOb.PixelsBlue[Row][Col]);
                Cnt++;
            }
        }
        
        DisplayAndSaveImage(EditedImage, CovertOb.getWidth(), CovertOb.getHeight(), ImgName);
    }

    public void DeleteImage(String ImageName)
    {
        new File(ImageName).delete();
    }

    public void DisplayAndSaveImage(byte[] Newpixels, int ImageWidth, int ImageHeight, String ImageName) throws IOException 
    {   
        BufferedImage NewImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        NewImage.getWritableTile(0, 0).setDataElements(0, 0, getWidth(), getHeight(), Newpixels);
        
        ImageIO.write(NewImage , "bmp", new File(ImageName));
        
        /*
        ImageIcon icon = new ImageIcon(NewImage);
        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(getWidth(), getHeight());
        JLabel lbl = new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        */
    }
}