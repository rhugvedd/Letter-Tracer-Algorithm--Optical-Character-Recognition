public class Line
{
    Dot StDot;
    Dot EndDot;
    Dot CrntDot;
    float TotDist;
    float CrntDotDist;

    static final int RED   = 1;
    static final int GREEN = 2;
    static final int BLUE  = 3;

    static final boolean SET_CRNTDOT = true;
    static final boolean DONT_SET_CRNTDOT = false;
    
    Line()
    {
        ComnLineConstructor(new Dot(-1, -1), new Dot(-1, -1));
    }

    Line(Dot vStDot, Dot vEndDot)
    {
        ComnLineConstructor(vStDot, vEndDot);
    }

    Line(Line SetLn)
    {
        ComnLineConstructor(SetLn.StDot, SetLn.EndDot);
    }

    Line(int StRow, int StCol, int EndRow, int EndCol)
    {
        ComnLineConstructor(new Dot(StRow, StCol), new Dot(EndRow, EndCol));
    }

    Line(Point StPnt, Point EndPnt)
    {
        ComnLineConstructor(new Dot(StPnt.Row, StPnt.Col), new Dot(EndPnt.Row, EndPnt.Col));
    }

    private void ComnLineConstructor(Dot vStDot, Dot vEndDot)
    {
        StDot = new Dot(vStDot);
        EndDot = new Dot(vEndDot);
        CrntDot = new Dot(vStDot);
        CrntDotDist = 0;
        TotDist = StDot.getDist(EndDot);
    }

    public float getRealDist(int PixelDist) throws PixelDistOutOfBoundsException
    {
        float RealDist = 0;

        if(PixelDist > (int)TotDist)
            throw new PixelDistOutOfBoundsException(PixelDist, TotDist);

        int TotPixDist = Math.abs(EndDot.Col - StDot.Col);

        if(Math.abs(EndDot.Row - StDot.Row) >= Math.abs(EndDot.Col - StDot.Col))
            TotPixDist = Math.abs(EndDot.Row - StDot.Row);
            
        if((PixelDist == 0) && (TotPixDist == 0))
            RealDist = 0;
        else
            RealDist = (TotDist * PixelDist) / TotPixDist;

        return RealDist;
    }

    public void getNxtDotByPixels(int Pixels) throws PixelDistOutOfBoundsException
    {
        getNxtDot(getRealDist(Pixels));
    }

    public void getPrevDotByPixels(int Pixels) throws PixelDistOutOfBoundsException
    {
        getPrevDot(getRealDist(Pixels));
    }

    public void setCrntDot(Dot SetDot)
    {
        if(CrntDot == null)
            CrntDot = new Dot(SetDot);
        else
            CrntDot.set(SetDot);
        
        CrntDotDist = StDot.getDist(CrntDot);
    }

    public void getNxtDot(float Dist)
    {
        CrntDotDist += Dist;

        if((TotDist == 0.0) && (Dist == 0))
            CrntDot.set(StDot);
        else if((int)CrntDotDist > (int)TotDist)
        {
            CrntDotDist = TotDist;
            CrntDot = null;
        }
        else
        {
            CrntDot.Row = (int)Math.round(((CrntDotDist * EndDot.Row) + ((TotDist - CrntDotDist) * StDot.Row)) / TotDist);
            CrntDot.Col = (int)Math.round(((CrntDotDist * EndDot.Col) + ((TotDist - CrntDotDist) * StDot.Col)) / TotDist);
        }
    }

    public void getNxtDot(float Antecedent, float Consequent)
    {
        getNxtDot((TotDist * Antecedent) / (Antecedent + Consequent));
    }

    public void getDot(float Antecedent, float Consequent, Dot AnsDot)
    {
        float AnsDotDist = (TotDist * Antecedent) / (Antecedent + Consequent);

        AnsDot.Row = (int)Math.round(((AnsDotDist * EndDot.Row) + ((TotDist - AnsDotDist) * StDot.Row)) / TotDist);
        AnsDot.Col = (int)Math.round(((AnsDotDist * EndDot.Col) + ((TotDist - AnsDotDist) * StDot.Col)) / TotDist);
    }

    public boolean equals(Line ChkLine)
    {
        return((StDot.equals(ChkLine.StDot)) && EndDot.equals(ChkLine.EndDot));
    }

    public void getPrevDot(float Dist)
    {
        CrntDotDist -= Dist;

        if((TotDist == 0.0) && (Dist == 0))
            CrntDot.set(StDot);
        else if(CrntDotDist < 0)
        {
            CrntDotDist = 0;
            CrntDot = null;
        }
        else
        {
            CrntDot.Row = (int)Math.round(((CrntDotDist * EndDot.Row) + ((TotDist - CrntDotDist) * StDot.Row)) / TotDist);
            CrntDot.Col = (int)Math.round(((CrntDotDist * EndDot.Col) + ((TotDist - CrntDotDist) * StDot.Col)) / TotDist);
        }
    }

    public void setLine(Line SetLine, boolean SetCrntDot)
    {
        StDot.set(SetLine.StDot);
        EndDot.set(SetLine.EndDot);

        if(SetCrntDot)
        {
            if(SetLine.CrntDot == null)
                CrntDot.set(SetLine.StDot);
            else
                CrntDot.set(SetLine.CrntDot);

            CrntDotDist = SetLine.CrntDotDist;
        }
        else
        {
            CrntDot.set(SetLine.StDot);
            CrntDotDist = 0;
        }

        TotDist = StDot.getDist(EndDot);
    }

    public void setLine(Dot StDot, Dot EndDot, Dot CrntDot)
    {
        this.StDot.set(StDot);
        this.EndDot.set(EndDot);
        this.CrntDot = new Dot(CrntDot);

        CrntDotDist = StDot.getDist(CrntDot);
        TotDist = StDot.getDist(EndDot);
    }

    public int drawLine(Image ImageOb, int Color) throws PixelDistOutOfBoundsException
    {
        CrntDot.set(StDot);
        CrntDotDist = 0;
        getNxtDotByPixels(0);

        int PixelPrintNo = 0;

        while(CrntDot != null)
        {
            ImageOb.PixelsRed[CrntDot.Row][CrntDot.Col] = 0;
            ImageOb.PixelsGreen[CrntDot.Row][CrntDot.Col] = 0;
            ImageOb.PixelsBlue[CrntDot.Row][CrntDot.Col] = 0;
            
            switch(Color)
            {
                case 1:{ ImageOb.PixelsRed[CrntDot.Row][CrntDot.Col] = 255; break; }
                case 2:{ ImageOb.PixelsGreen[CrntDot.Row][CrntDot.Col] = 255; break; }
                case 3:{ ImageOb.PixelsBlue[CrntDot.Row][CrntDot.Col] = 255; break; }
                case 4:
                        {
                            ImageOb.PixelsBlue[CrntDot.Row][CrntDot.Col] = 128;
                            ImageOb.PixelsGreen[CrntDot.Row][CrntDot.Col] = 128;
                            break; 
                        }
                case 5:{ ImageOb.PixelsBlue[CrntDot.Row][CrntDot.Col] = 100; break; }
            }

            PixelPrintNo++;

            getNxtDotByPixels(1);
        }

        CrntDotDist = 0;

        CrntDot = new Dot(StDot);

        return PixelPrintNo;
    }

    public int drawLine(ReadImage riImageOb, int Color) throws PixelDistOutOfBoundsException
    {
        CrntDot.set(StDot);
        CrntDotDist = 0;
        getNxtDotByPixels(0);

        int PixelPrintNo = 0;

        while(CrntDot != null)
        {
            riImageOb.PixelsRed[CrntDot.Row][CrntDot.Col] = 0;
            riImageOb.PixelsGreen[CrntDot.Row][CrntDot.Col] = 0;
            riImageOb.PixelsBlue[CrntDot.Row][CrntDot.Col] = 0;
            
            switch(Color)
            {
                case 1:{ riImageOb.PixelsRed[CrntDot.Row][CrntDot.Col] = 255; break; }
                case 2:{ riImageOb.PixelsGreen[CrntDot.Row][CrntDot.Col] = 255; break; }
                case 3:{ riImageOb.PixelsBlue[CrntDot.Row][CrntDot.Col] = 255; break; }
                case 4:
                        {
                            riImageOb.PixelsBlue[CrntDot.Row][CrntDot.Col] = 128;
                            riImageOb.PixelsGreen[CrntDot.Row][CrntDot.Col] = 128;
                            break; 
                        }
                case 5:{ riImageOb.PixelsBlue[CrntDot.Row][CrntDot.Col] = 100; break; }
            }

            PixelPrintNo++;

            getNxtDotByPixels(1);
        }

        CrntDotDist = 0;

        CrntDot = new Dot(StDot);

        return PixelPrintNo;
    }

    public void drawNumberLine(ReadImage riImageOb, int Color, int Num, boolean ReplaceOtherColors) throws PixelDistOutOfBoundsException
    {
        setCrntDot(StDot);

        getNxtDotByPixels(0);

        while(CrntDot != null)
        {
            if(ReplaceOtherColors)
            {
                riImageOb.PixelsRed[CrntDot.Row][CrntDot.Col] = 0;
                riImageOb.PixelsGreen[CrntDot.Row][CrntDot.Col] = 0;
                riImageOb.PixelsBlue[CrntDot.Row][CrntDot.Col] = 0;
            }

            switch(Color)
            {
                case 1:{ riImageOb.PixelsRed[CrntDot.Row][CrntDot.Col] = Num; break; }
                case 2:{ riImageOb.PixelsGreen[CrntDot.Row][CrntDot.Col] = Num; break; }
                case 3:{ riImageOb.PixelsBlue[CrntDot.Row][CrntDot.Col] = Num; break; }
            }

            getNxtDotByPixels(1);
        }

        CrntDotDist = 0;

        CrntDot = new Dot(StDot);
    }

    public void drawNumberLine(Image riImageOb, int Color, int Num, boolean ReplaceOtherColors) throws PixelDistOutOfBoundsException
    {
        setCrntDot(StDot);

        getNxtDotByPixels(0);

        while(CrntDot != null)
        {
            if(ReplaceOtherColors)
            {
                riImageOb.PixelsRed[CrntDot.Row][CrntDot.Col] = 0;
                riImageOb.PixelsGreen[CrntDot.Row][CrntDot.Col] = 0;
                riImageOb.PixelsBlue[CrntDot.Row][CrntDot.Col] = 0;
            }

            switch(Color)
            {
                case 1:{ riImageOb.PixelsRed[CrntDot.Row][CrntDot.Col] = Num; break; }
                case 2:{ riImageOb.PixelsGreen[CrntDot.Row][CrntDot.Col] = Num; break; }
                case 3:{ riImageOb.PixelsBlue[CrntDot.Row][CrntDot.Col] = Num; break; }
            }

            getNxtDotByPixels(1);
        }

        CrntDotDist = 0;

        CrntDot = new Dot(StDot);
    }

    public void drawNumLine(ReadImage riImageOb, int Color, int Num) throws PixelDistOutOfBoundsException
    {
        setCrntDot(StDot);

        getNxtDotByPixels(0);

        while(CrntDot != null)
        {
            riImageOb.PixelsRed[CrntDot.Row][CrntDot.Col] = 0;
            riImageOb.PixelsGreen[CrntDot.Row][CrntDot.Col] = 0;
            riImageOb.PixelsBlue[CrntDot.Row][CrntDot.Col] = 0;
            
            switch(Color)
            {
                case 1:{ riImageOb.PixelsRed[CrntDot.Row][CrntDot.Col] = (255 + Num); break; }
                case 2:{ riImageOb.PixelsGreen[CrntDot.Row][CrntDot.Col] = (255 + Num); break; }
                case 3:{ riImageOb.PixelsBlue[CrntDot.Row][CrntDot.Col] = (255 + Num); break; }
            }

            getNxtDotByPixels(1);
        }

        CrntDotDist = 0;

        CrntDot = new Dot(StDot);
    }   
    
    // public void drawNumLine(Image ImageOb, int Color, int Num) throws PixelDistOutOfBoundsException
    // {
    //     setCrntDot(StDot);

    //     getNxtDotByPixels(0);

    //     while(CrntDot != null)
    //     {
    //         ImageOb.PixelsRed[CrntDot.Row][CrntDot.Col] = 0;
    //         ImageOb.PixelsGreen[CrntDot.Row][CrntDot.Col] = 0;
    //         ImageOb.PixelsBlue[CrntDot.Row][CrntDot.Col] = 0;
            
    //         switch(Color)
    //         {
    //             case 1:{ ImageOb.PixelsRed[CrntDot.Row][CrntDot.Col] = (255 + Num); break; }
    //             case 2:{ ImageOb.PixelsGreen[CrntDot.Row][CrntDot.Col] = (255 + Num); break; }
    //             case 3:{ ImageOb.PixelsBlue[CrntDot.Row][CrntDot.Col] = (255 + Num); break; }
    //         }

    //         getNxtDotByPixels(1);
    //     }

    //     CrntDotDist = 0;

    //     CrntDot = new Dot(StDot);
    // }   
}