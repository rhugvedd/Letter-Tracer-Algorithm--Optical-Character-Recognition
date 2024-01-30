import java.lang.Exception;

/**
 * IPException = Image Processing Exception.
 * This class is a class of User Defined Custom Exceptions.
 * The main public class is kept empty because of Suitable name issues.
 * Each subclass here is used as a user defined exception.
 * 
 * @author Rhugved Pankaj Chaudhari
 */

public class IPException extends Exception
{}

class PixelDistOutOfBoundsException extends Exception
{
    PixelDistOutOfBoundsException(int PixelDist, float LnTotDist)
    {
        super("Required Pixel Distance '" + PixelDist + "' is greater than the Total Line Distance '" + LnTotDist + "'");
    }
}

class WrongTravelInitializerException extends Exception
{
    WrongTravelInitializerException(String Message)
    {
        super(Message);
    }
}

class WrongColourException extends Exception
{
    WrongColourException()
    {
        super("Specified color not recognized in the colours defined.");
    }
}