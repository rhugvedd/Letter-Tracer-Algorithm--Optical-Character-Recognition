public class Quadrilateral
{
    Line Edge[] = new Line[4];

    public static final int TOP = 0;
    public static final int RIGHT = 1;
    public static final int BOTTOM = 2;
    public static final int LEFT = 3;

    Quadrilateral(Line TopEdge, Line RightEdge, Line BottomEdge, Line LeftEdge)
    {
        Edge[TOP] = TopEdge;
        Edge[RIGHT] = RightEdge;
        Edge[BOTTOM] = BottomEdge;
        Edge[LEFT] = LeftEdge;
    }

    Quadrilateral(Dot TopLeft, Dot TopRight, Dot BottomRight, Dot BottomLeft)
    {
        Edge[TOP] = new Line(TopLeft, TopRight);
        Edge[RIGHT] = new Line(TopRight, BottomRight);
        Edge[BOTTOM] = new Line(BottomLeft, BottomRight);
        Edge[LEFT] = new Line(TopLeft, BottomLeft);
    }

    Quadrilateral(int TopLeftRow, int TopLeftCol, int TopRightRow, int TopRightCol, int BottomRightRow, int BottomRightCol, int BottomLeftRow, int BottomLeftCol)
    {
        Edge[TOP] = new Line(TopLeftRow, TopLeftCol, TopRightRow, TopRightCol);
        Edge[RIGHT] = new Line(TopRightRow, TopRightCol, BottomRightRow, BottomRightCol);
        Edge[BOTTOM] = new Line(BottomLeftRow, BottomLeftCol, BottomRightRow, BottomRightCol);
        Edge[LEFT] = new Line(TopLeftRow, TopLeftCol, BottomLeftRow, BottomLeftCol);
    }
}