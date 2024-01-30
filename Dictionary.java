import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public class Dictionary 
{
    private static final String DictionaryPath = "./Dictionary/";

    private static final String CodeDictionaryName = DictionaryPath + "Code Dictionary.txt";
    private static final String CharDictionaryName = DictionaryPath + "Char Dictionary.txt";

    private static final String BackUpCodeDictName = DictionaryPath + "BackUp Code Dictionary.txt";
    private static final String BackUpCharDictName = DictionaryPath + "BackUp Char Dictionary.txt";

    private static final String OpeningDctionaryName = DictionaryPath + "Opening Dictionary.txt";

    private static final char MULTIPLE_ANS_CHAR = '~';
    private static final char OPENING_DICTIONARY_LTR_SEPARATOR = '~';
    private static final char MULTIPLE_ANS_CHAR_NOT_DETECTED = '`';

    char OpeningAnsChar;

    long LtrCode[];
    String CharInfo;
    String OpeningLtrInfo[];

    Dictionary() throws IOException
    {
        ImportDictionaryData();
        BackUpDictionaries();
    }

    private void BackUpDictionaries() throws IOException
    {
        Files.copy(new File(CodeDictionaryName).toPath(), new FileOutputStream(BackUpCodeDictName));
        Files.copy(new File(CharDictionaryName).toPath(), new FileOutputStream(BackUpCharDictName));
    }

    private void ImportDictionaryData() throws IOException
    {
        BufferedReader CodeDictionaryReader = new BufferedReader(new FileReader(CodeDictionaryName));
        BufferedReader CharDictionaryReader = new BufferedReader(new FileReader(CharDictionaryName));
        BufferedReader OpeningDictionaryReader = new BufferedReader(new FileReader(OpeningDctionaryName));

        List <String> StringLtrCode = CodeDictionaryReader.lines().collect(Collectors.toList());
        List <String> OpeningLtrCode = OpeningDictionaryReader.lines().collect(Collectors.toList());
        CharInfo = CharDictionaryReader.readLine();
    
        CodeDictionaryReader.close();
        CharDictionaryReader.close();
        OpeningDictionaryReader.close();

        LtrCode = new long[StringLtrCode.size()];
        OpeningLtrInfo = new String[OpeningLtrCode.size()];

        for(int CodeIndx = 0; CodeIndx < StringLtrCode.size(); CodeIndx++)
            LtrCode[CodeIndx] = Long.valueOf(StringLtrCode.get(CodeIndx));
            
        for(int OpnCodeIndx = 0; OpnCodeIndx < OpeningLtrInfo.length; OpnCodeIndx++)
            OpeningLtrInfo[OpnCodeIndx] = OpeningLtrCode.get(OpnCodeIndx);

        StringLtrCode = null;
        OpeningLtrCode = null;

        CodeDictionaryReader.close();
        CharDictionaryReader.close();
        OpeningDictionaryReader.close();
    }

    public char getChar(long ChkCode) 
    {
        int StIndx = 0, EndIndx = LtrCode.length - 1, CenterIndx = (StIndx + EndIndx) / 2, AnsIndx;
        Long StCode = LtrCode[StIndx], CenterCode = LtrCode[CenterIndx], EndCode = LtrCode[EndIndx];
        long AnsCode;

        while((StCode != ChkCode) && (CenterCode != ChkCode) && (EndCode != ChkCode))
        {
            if(ChkCode < CenterCode)
                EndIndx = CenterIndx;
            else
                StIndx = CenterIndx;

            CenterIndx = (StIndx + EndIndx) / 2;
            CenterCode = LtrCode[CenterIndx];

            if((StIndx == EndIndx) || (StIndx == CenterIndx) || (EndIndx == CenterIndx))
                break;
        }

        if(StCode == ChkCode)
        {
            AnsCode = StCode;
            AnsIndx = StIndx;

            // return CharInfo.charAt(StIndx);
        }    
        else if(CenterCode == ChkCode)
        {
            AnsCode = CenterCode;
            AnsIndx = CenterIndx;

            // return CharInfo.charAt(CenterIndx);
        }    
        else if(EndCode == ChkCode)
        {
            AnsCode = EndCode;
            AnsIndx = EndIndx;

            // return CharInfo.charAt(EndIndx);
        }    
        else 
            return 0;

        
        if  (   
                ((AnsIndx + 1) != LtrCode.length) && ((LtrCode[AnsIndx + 1] == AnsCode)) || 
                ((AnsIndx != 0) && (LtrCode[AnsIndx - 1] == AnsCode))
            )
        {
            OpeningAnsChar = CharInfo.charAt(AnsIndx);
            return MULTIPLE_ANS_CHAR;
        }
        else
            OpeningAnsChar = CharInfo.charAt(AnsIndx);
            return CharInfo.charAt(AnsIndx);
    }

    public char distinguishChar(String OpeningCode)
    {
        for(int OpnCodeIndx = 0; OpnCodeIndx < OpeningLtrInfo.length; OpnCodeIndx++)
        {
            String AnsOpnCode = OpeningLtrInfo[OpnCodeIndx];

            if(AnsOpnCode.indexOf(OpeningAnsChar) != -1)
            {
                char AnsOpenValCnt = OpeningCode.charAt(AnsOpnCode.charAt(0) - 48 - 1);
                int SeparatingCharIndx = AnsOpnCode.indexOf(OPENING_DICTIONARY_LTR_SEPARATOR);

                int AnsCharPos = AnsOpnCode.lastIndexOf(AnsOpenValCnt) - SeparatingCharIndx;
                return AnsOpnCode.charAt(AnsCharPos);
            }
        }

        return MULTIPLE_ANS_CHAR_NOT_DETECTED;
    }

    public void add(long Code, char Ch) throws IOException
    {
        long NewLtrCode[] = new long[LtrCode.length + 1];
        String CharData = "";

        boolean ChkInsrt = true;

        for(int CodeIndx = 0; CodeIndx < LtrCode.length; CodeIndx++)
        {
            if(ChkInsrt && (LtrCode[CodeIndx] > Code))
            {
                // CodeData += Long.toString(Code) + "\n" + Long.toString(LtrCode[CodeIndx]) + "\n";
                NewLtrCode[CodeIndx] = Code;
                NewLtrCode[CodeIndx + 1] = LtrCode[CodeIndx];

                CharData = CharInfo.substring(0, CodeIndx) + Ch + CharInfo.substring(CodeIndx);
                ChkInsrt = false;
            }
            else
            {
                if(ChkInsrt)
                    NewLtrCode[CodeIndx] = LtrCode[CodeIndx];
                else
                    NewLtrCode[CodeIndx + 1] = LtrCode[CodeIndx];
            }
        }

        if(ChkInsrt)
        {
            NewLtrCode[NewLtrCode.length - 1] = Code;
            // CodeData += Long.toString(Code);
            CharData = CharInfo + Ch;
        }

        CharInfo = null;
        CharInfo = CharData;

        LtrCode = null;
        LtrCode = NewLtrCode;

        CharData = null;
        NewLtrCode = null;
    }

    public void UpdateDictionary() throws IOException
    {
        FileWriter CodeDictionaryWriter = new FileWriter(CodeDictionaryName);
        FileWriter CharDictionaryWriter = new FileWriter(CharDictionaryName);

        String CodeData = "";

        for(int Indx = 0; Indx < LtrCode.length; Indx++)
            CodeData += Long.toString(LtrCode[Indx]) + '\n';

        CodeDictionaryWriter.write(CodeData);
        CharDictionaryWriter.write(CharInfo);

        CodeDictionaryWriter.close();
        CharDictionaryWriter.close();

        CodeDictionaryWriter = null;
        CharDictionaryWriter = null;
    }
}