package piece;

import Main.GamePanel;
import Main.Type;

public class Pawn extends Piece  {
    public Pawn(int color, int col, int row){
        super(color, col, row);
        type = Type.PAWN;

        if(color == GamePanel.WHITE){
            image = getImage("/piece/wP");
        }
        else {
            image = getImage("/piece/bP");
        }
    }
    public boolean canMove(int targetCol, int targetRow){
        //Điều kiện này đảm bảo rằng ô mục tiêu nằm trong phạm vi bàn cờ và không phải là ô mà quân cờ đang đứng
        if(isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false){
            // define the move value based on its color
            int moveValue;
            if(color == GamePanel.WHITE){
                moveValue = -1;// trắng sẽ di chuyển lên -1
            }else {
                moveValue = 1;// đen sẽ di chuyển xuống -1
            }
            //check the hitting piece
            hittingP = getHittingP(targetCol,targetRow);
            //1 square movement
            if(targetCol == preCol && targetRow == preRow + moveValue && hittingP == null){
                return true;
            }
            // 2 square movement
            if(targetCol == preCol && targetRow == preRow + moveValue*2 && hittingP == null && moved == false &&
                    pieceIsOnStraightLine(targetCol,targetRow) == false){
                         return true;
            }
            // Diagonal movement & capture (if a piece is on a square diagonally in front of its)
            if(Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue && hittingP != null &&
            hittingP.color != color){
                return true;
            }
        }
        return false;
    }
}
