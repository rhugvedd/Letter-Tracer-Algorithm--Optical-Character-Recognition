import java.io.File;
import java.io.IOException;
import java.util.List;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Image 
{
    int PixelsRed[][];
    int PixelsGreen[][];
    int PixelsBlue[][];
    
    int ImageWidth;
    int ImageHeight;

    Quadrilateral ImageQuad;

    Image(Image vCharData)
    {
        this.PixelsRed = vCharData.PixelsRed;
        this.PixelsGreen = vCharData.PixelsGreen;
        this.PixelsBlue = vCharData.PixelsBlue;

        this.ImageWidth = vCharData.ImageWidth;
        this.ImageHeight = vCharData.ImageHeight;

        this.ImageQuad = vCharData.ImageQuad;
    }

    Image(Dot LtrMaxVal, Dot LtrMinVal, List <Dot> LeftTrace, List <Dot> RightTrace, ReadImage ImageOb) throws PixelDistOutOfBoundsException
    {   
        ImageHeight = LtrMaxVal.Row - LtrMinVal.Row + 1;
        ImageWidth = LtrMaxVal.Col - LtrMinVal.Col + 1;

        ImageQuad = new Quadrilateral    (
                                            0,              0, 
                                            0,              ImageWidth - 1, 
                                            ImageHeight - 1, ImageWidth - 1, 
                                            ImageHeight - 1, 0
                                        );

        PixelsRed = new int[ImageHeight][ImageWidth];
        PixelsGreen = new int[ImageHeight][ImageWidth];
        PixelsBlue = new int[ImageHeight][ImageWidth];

        CopyPixels(LtrMinVal, LeftTrace, RightTrace, ImageOb);
    }

    private void CopyPixels(Dot LtrMinVal, List <Dot> LeftTrace, List <Dot> RightTrace, ReadImage riCPob) throws PixelDistOutOfBoundsException
    {
        Line CopyLine = new Line();

        for(int LnIndex = 0; LnIndex < RightTrace.size(); LnIndex++)
        {
            CopyLine.setLine(LeftTrace.get(LnIndex), RightTrace.get(LnIndex), LeftTrace.get(LnIndex));
            CopyLine.getNxtDotByPixels(0);

            int SetColIndex = LeftTrace.get(LnIndex).Col - LtrMinVal.Col;

            for(int Col = 0; Col < SetColIndex; Col++)
            {
                PixelsRed[LnIndex][Col] = 255;
                PixelsGreen[LnIndex][Col] = 255;
                PixelsBlue[LnIndex][Col] = 255;
            }

            while(CopyLine.CrntDot != null)
            {
                int SetVal = riCPob.PixelsRed[CopyLine.CrntDot.Row][CopyLine.CrntDot.Col];
                
                PixelsRed[LnIndex][SetColIndex] = SetVal;
                PixelsGreen[LnIndex][SetColIndex] = SetVal;
                PixelsBlue[LnIndex][SetColIndex] = SetVal;

                if(CopyLine.TotDist == 0)
                    break;

                SetColIndex++;

                CopyLine.getNxtDotByPixels(1);
            }
            
            for(int Col = SetColIndex; Col < ImageWidth; Col++)
            {
                PixelsRed[LnIndex][Col] = 255;
                PixelsGreen[LnIndex][Col] = 255;
                PixelsBlue[LnIndex][Col] = 255;
            }
        }
    }

    public void createImage(String ImageName) throws IOException
    {
        byte PixelData[] = new byte[ImageWidth * ImageHeight * 3];
        
        int Cnt = 0;
        
        for(int Row = 0; Row < ImageHeight; Row++)
        {
            for(int Col = 0; Col < ImageWidth; Col++)
            {
                if(PixelsRed[Row][Col] > 127)
                    PixelData[Cnt] = (byte)(PixelsRed[Row][Col] - 256);
                else    
                    PixelData[Cnt] = (byte)(PixelsRed[Row][Col]);
                Cnt++;
                
                if(PixelsGreen[Row][Col] > 127)
                    PixelData[Cnt] = (byte)(PixelsGreen[Row][Col] - 256);
                else    
                    PixelData[Cnt] = (byte)(PixelsGreen[Row][Col]);
                Cnt++;
                
                if(PixelsBlue[Row][Col] > 127)
                    PixelData[Cnt] = (byte)(PixelsBlue[Row][Col] - 256);
                else    
                    PixelData[Cnt] = (byte)(PixelsBlue[Row][Col]);
                Cnt++;
            }
        }
       
        BufferedImage NewImage = new BufferedImage(ImageWidth, ImageHeight, BufferedImage.TYPE_3BYTE_BGR);
        NewImage.getWritableTile(0, 0).setDataElements(0, 0, ImageWidth, ImageHeight, PixelData);
        
        ImageIO.write(NewImage , "bmp", new File(ImageName));
    }

    public void createRedPixImage(String ImageName) throws IOException
    {
        byte PixelData[] = new byte[ImageWidth * ImageHeight * 3];
        
        int Cnt = 0;
        
        for(int Row = 0; Row < ImageHeight; Row++)
        {
            for(int Col = 0; Col < ImageWidth; Col++)
            {
                int CntBrdr = Cnt + 3;

                for(;Cnt < CntBrdr; Cnt++)
                {
                    if(PixelsRed[Row][Col] > 127)
                        PixelData[Cnt] = (byte)(PixelsRed[Row][Col] - 256);
                    else
                        PixelData[Cnt] = (byte)(PixelsRed[Row][Col]);
                }
            }
        }
       
        BufferedImage NewImage = new BufferedImage(ImageWidth, ImageHeight, BufferedImage.TYPE_3BYTE_BGR);
        NewImage.getWritableTile(0, 0).setDataElements(0, 0, ImageWidth, ImageHeight, PixelData);
        
        ImageIO.write(NewImage , "bmp", new File(ImageName));
    }
}