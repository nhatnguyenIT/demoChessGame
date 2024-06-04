package piece;

import Main.GamePanel;
import Main.Type;

public class Queen extends Piece{
    public Queen(int color, int col, int row){
        super(color, col, row);
        type = Type.QUEEN;

        if(color == GamePanel.WHITE){
            image = getImage("/piece/wQ");
        }
        else {
            image = getImage("/piece/bQ");
        }
    }
    public boolean canMove(int targetCol, int targetRow){
        if(isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false){
            // extend ROOK
            if(targetCol == preCol ||  targetRow == preRow){
                if(isValidSquare(targetCol,targetRow) && pieceIsOnStraightLine(targetCol, targetRow) == false){
                    return true;
                }
            }
            // extend bishop
            if(Math.abs(targetCol - preCol) == Math.abs(targetRow - preRow)){
                if(isValidSquare(targetCol, targetRow) && pieceIsOnDiagonalLine(targetCol,targetRow) == false){
                    return true;
                }
            }
        }
        return false;
    }

}
