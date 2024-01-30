import java.sql.Ref;

import javax.lang.model.util.ElementScanner6;

public class Language
{
    Letter oLetter[];

    int LtrNum, LtrColCnt, LtrLnPntCnt;

    Language(int LetrNum, int PntColCnt, int LnPntCnt)
    {
        LtrNum = LetrNum;
        LtrColCnt = PntColCnt;
        LtrLnPntCnt = LnPntCnt;
        oLetter = new Letter[LetrNum];

        for(int Ltr = 0; Ltr < LetrNum; Ltr++)
            oLetter[Ltr] = new Letter(PntColCnt, LnPntCnt);

    }

    /**
     * This method takes in "ChkLtr" of type Letter and 
     * returns the Letter from Font which matches most 
     * with the "Chkltr"
     * @param ChkLtr
     * @return 
     */

    public int LetterDetect(Letter ChkLtr)
    {
        int MatchLtr = -97;

        for(int RefLtr = 0, RefChar = 'a'; RefLtr < LtrNum; RefLtr++, RefChar++)
        {
            if(RefChar == 'm')
            {
                if(ChkLtr.LtrInfo.size() > 1)
                    if((ChkLtr.LtrInfo.get(ChkLtr.LtrInfo.size() - 1) == Letter.BOTTOM_OPEN) && (ChkLtr.LtrInfo.get(ChkLtr.LtrInfo.size() - 2) == Letter.BOTTOM_OPEN))  
                        return 'm' - 'a';
            }
            else if(RefChar == 'n')
            {
                if(ChkLtr.LtrInfo.size() <= 2)
                    if(ChkLtr.LtrInfo.get(ChkLtr.LtrInfo.size() - 1) == Letter.BOTTOM_OPEN)
                        return 'n' - 'a';
            }
            else if(RefChar == 'u')
            {
                if(ChkLtr.LtrInfo.size() == 1)
                    if(ChkLtr.LtrInfo.get(0) == Letter.TOP_U)
                        return 'u' - 'a';
                else if((ChkLtr.LtrInfo.size() == 2) && (ChkLtr.LtrInfo.get(0) == Letter.TOP_OPEN))
                    return 'u' - 'a';
            }
            else if(RefChar == 'y')
            {
                if(ChkLtr.LtrInfo.size() == 2)
                    if((ChkLtr.LtrInfo.get(0) == Letter.LEFT_OPEN) || (ChkLtr.LtrInfo.get(0) == Letter.VERTICAL_LINE))
                        if(ChkLtr.LtrInfo.get(1) == Letter.TOP_OPEN)
                            return 'y' - 'a';
            }
            else if(oLetter[RefLtr].LtrInfo.size() == ChkLtr.LtrInfo.size())
            {
                boolean isLtrMatched = true;

                for(int ChkInfo = 0; ChkInfo < ChkLtr.LtrInfo.size(); ChkInfo++)
                {
                    if(ChkLtr.LtrInfo.get(ChkInfo) != oLetter[RefLtr].LtrInfo.get(ChkInfo))
                    {
                        isLtrMatched = false;
                        break;
                    }
                }

                if(isLtrMatched)
                    return RefLtr;
            }
        }

        return MatchLtr;
    }

    public int LetterDetectOld(Letter ChkLtr)
    {
        int MaxMatchCoefficient = Integer.MIN_VALUE;
        int MatchLtr = -1;

        if(ChkLtr.ClosedPathCnt == 2)
            return 6;

        for(int RefLtr = 0; RefLtr < LtrNum; RefLtr++)
        {
            char RefChar = (char)(RefLtr + 97);

            int MatchCoefficient;

            MatchCoefficient = oLetter[RefLtr].LettertMatch(ChkLtr);

            if(MatchCoefficient > MaxMatchCoefficient)
            {
                MaxMatchCoefficient = MatchCoefficient;
                MatchLtr = RefLtr;
            }
        }

        return MatchLtr;
    }

    public char getEnglishChar(int ltrCode)
    {
        return ((char)(ltrCode + 97));
    }
}