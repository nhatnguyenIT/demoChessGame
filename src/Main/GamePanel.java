package Main;

import piece.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable {
    public static final int WIDTH = 1100;
    public static final int HEIGHT = 825;
    final int FPS = 60;
    Thread gameThread;
    Board board = new Board();
    Mouse mouse = new Mouse();
    //PIECE
    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();
    ArrayList<Piece> promoPieces = new ArrayList<>();
    Piece activeP, checkingP;
    public static Piece castlingP;
    //color
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int currentColor = WHITE;
    //Boolean
    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameover;
    //Time
    long whiteStartTime, blackStartTime;
    long whiteElapsedTime = 0, blackElapsedTime = 0;

    // an quan
    ArrayList<Piece> capturedWhitePieces = new ArrayList<>();
    ArrayList<Piece> capturedBlackPieces = new ArrayList<>();


    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(143, 152, 152));
        addMouseMotionListener(mouse);
        addMouseListener(mouse);

        setPieces();
//        testPromotion();
//        testIllegal();
        copyPieces(pieces, simPieces);

        whiteStartTime = System.nanoTime();
        blackStartTime = System.nanoTime();
    }


    public void launchGame() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void setPieces() {
        // WHITE TEAM
        pieces.add(new Pawn(WHITE, 0, 6));
        pieces.add(new Pawn(WHITE, 1, 6));
        pieces.add(new Pawn(WHITE, 2, 6));
        pieces.add(new Pawn(WHITE, 3, 6));
        pieces.add(new Pawn(WHITE, 4, 6));
        pieces.add(new Pawn(WHITE, 5, 6));
        pieces.add(new Pawn(WHITE, 6, 6));
        pieces.add(new Pawn(WHITE, 7, 6));
        pieces.add(new Rook(WHITE, 0, 7));
        pieces.add(new Rook(WHITE, 7, 7));
        pieces.add(new Knight(WHITE,1,7));
        pieces.add(new Knight(WHITE,6,7));
        pieces.add(new Bishop(WHITE,2,7));
        pieces.add(new Bishop(WHITE,5,7));
        pieces.add(new Queen(WHITE,3,7));
        pieces.add(new King(WHITE, 4, 7));
        //BLACK TEAM
        pieces.add(new Pawn(BLACK, 0, 1));
        pieces.add(new Pawn(BLACK, 1, 1));
        pieces.add(new Pawn(BLACK, 2, 1));
        pieces.add(new Pawn(BLACK, 3, 1));
        pieces.add(new Pawn(BLACK, 4, 1));
        pieces.add(new Pawn(BLACK, 5, 1));
        pieces.add(new Pawn(BLACK, 6, 1));
        pieces.add(new Pawn(BLACK, 7, 1));
        pieces.add(new Rook(BLACK, 0, 0));
        pieces.add(new Rook(BLACK, 7, 0));
        pieces.add(new Knight(BLACK, 1, 0));
        pieces.add(new Knight(BLACK, 6, 0));
        pieces.add(new Bishop(BLACK, 2, 0));
        pieces.add(new Bishop(BLACK, 5, 0));
        pieces.add(new Queen(BLACK, 3, 0));
        pieces.add(new King(BLACK, 4, 0));

    }

    private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {
        target.clear();
        for (int i = 0; i < source.size(); i++) {
            target.add(source.get(i));
        }
    }

    public void run() {
        // game loop
        double drawInterval = 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;
            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    private void update() {
        if (promotion) {
            promoting();
        } else if(gameover == false) {
            ///Mouse Button Pressed
            if (mouse.pressed) {
                if (activeP == null) {
                    //if active is null, check if you can pick up a piece
                    for (Piece piece : simPieces) {
                        //if the mouse is on an ally piece, pick it up as the activeP
                        if (piece.color == currentColor &&
                                piece.col == mouse.x / Board.SQUARE_SIZE &&
                                piece.row == mouse.y / Board.SQUARE_SIZE) {

                            activeP = piece;
                        }
                    }
                } else {
                    simulate();
                }
            }
            ///MOUSE BUTTON RELEASE
            if (mouse.pressed == false) {
                if (activeP != null) {
                    if (validSquare) {
                        // move confirmed
                        // update the piece list in case a piece has been captured and removed during the simulation
                        copyPieces(simPieces, pieces);
                        activeP.updatePosition();
                        if (castlingP != null) {
                            castlingP.updatePosition();
                        }
                        if (isKingInCheck() && ischekmate()) {
                             gameover =true;
                        } else {
                            if (canPromote()) {
                                promotion = true;
                            } else {
                                changePlayer();
                            }
                        }

                    } else {
                        // the move is not valid so reset everything
                        copyPieces(pieces, simPieces);
                        activeP.resetPosition();
                        activeP = null;
                    }
                }
            }
        }
    }

    private void updateTimers() {

        long currentTime = System.nanoTime();

        if (currentColor == WHITE) {
            whiteElapsedTime += (currentTime - whiteStartTime);
            whiteStartTime = currentTime;
            blackStartTime = System.nanoTime();
        } else {
            blackElapsedTime += (currentTime - blackStartTime);
            blackStartTime = currentTime;
            whiteStartTime = System.nanoTime();
        }
    }
    private String formatTime(long nanoseconds) {
        long totalSeconds = nanoseconds / 1_000_000_000L;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void simulate() {
        canMove = false;
        validSquare = false;

        //Reset the piece list in every loop
        //this is basically for restoring the removed piece during the simulation
        copyPieces(pieces, simPieces);
        //reset the castling piecce's position
        if (castlingP != null) {
            castlingP.col = castlingP.preCol;
            castlingP.x = castlingP.getX(castlingP.col);
            castlingP = null;
        }
        // if a piece is being hold, update its position
        activeP.x = mouse.x - Board.HALF_SQUARE_SIZE;
        activeP.y = mouse.y - Board.HALF_SQUARE_SIZE;
        activeP.col = activeP.getCol(activeP.x);
        activeP.row = activeP.getRow(activeP.y);

        //check if the piece is hovering over a reachable square
        if (activeP.canMove(activeP.col, activeP.row)) {
            canMove = true;
            //if hitting a piece, remove it from the list
            if (activeP.hittingP != null) {
                Piece capturedPiece = activeP.hittingP;
                simPieces.remove(capturedPiece.getIndex());

                // Add the captured piece to the respective list
                if (capturedPiece.color == WHITE) {
                    if (!capturedWhitePieces.contains(capturedPiece)) {
                        capturedWhitePieces.add(capturedPiece);
                    }
                } else {
                    if (!capturedBlackPieces.contains(capturedPiece)) {
                        capturedBlackPieces.add(capturedPiece);
                    }
                }
            }
            checkCastling();
            if (isIllegal(activeP) == false && oppenentCanCaptureKing() == false) {
                validSquare = true;
            }
        }
    }

    private boolean isIllegal(Piece king) {
        if (king.type == Type.KING) {
            for (Piece piece : simPieces) {
                if (piece != king && piece.color != king.color && piece.canMove(king.col, king.row)) {
                    return true;
                }
            }
        }
        return false;
    }
    private boolean oppenentCanCaptureKing(){
        Piece king = getKing(false);
        for(Piece piece : simPieces){
            if(piece.color != king.color && piece.canMove(king.col, king.row)){
                return true;
            }
        }
        return false;
    }

    private boolean isKingInCheck() {
        Piece king = getKing(true);
        if (activeP.canMove(king.col, king.row)) {
            checkingP = activeP;
            return true;
        } else {
            checkingP = null;
        }
        return false;
    }

    private Piece getKing(boolean oppenent) {
        Piece king = null;
        for (Piece piece : simPieces) {
            if (oppenent) {
                if (piece.type == Type.KING && piece.color != currentColor) {
                    king = piece;
                }
            } else {
                if (piece.type == Type.KING && piece.color == currentColor) {
                    king = piece;
                }
            }
        }
        return king;
    }
    private boolean ischekmate(){
        Piece king  = getKing(true);

        if(kingCanMove(king)){
            return false;
        }
        else {
            //check the position of the checking piece and the king in check
            int colDiff = Math.abs(checkingP.col - king.col);
            int rowDiff = Math.abs(checkingP.row - king.row);

            if(colDiff == 0){
                //the checking piece is attack vertically
                if(checkingP.row < king.row){
                    //the checking piece is a bove the king
                    for(int row = checkingP.row; row < king.row; row++){
                        for (Piece piece : simPieces){
                            if(piece != king && piece.color != currentColor && piece.canMove(checkingP.col, row)){
                                return false;
                            }
                        }
                    }
                }
                if(checkingP.row > king.row){
                    //the checking piece is a bove the king
                    for(int row = checkingP.row; row > king.row; row--){
                        for (Piece piece : simPieces){
                            if(piece != king && piece.color != currentColor && piece.canMove(checkingP.col, row)){
                                return false;
                            }
                        }
                    }
                }

            }
            else if(rowDiff == 0){
                // the checking piece is attacking horizontally
                if(checkingP.col < king.col){
                    //the checking piece is to the left
                    for(int col = checkingP.col; col < king.col; col++){
                        for (Piece piece : simPieces){
                            if(piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)){
                                return false;
                            }
                        }
                    }
                }
                if(checkingP.col > king.col){
                    //the checking piece is to the right
                    for(int col = checkingP.col; col > king.col; col--){
                        for (Piece piece : simPieces){
                            if(piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)){
                                return false;
                            }
                        }
                    }
                }

            }
            else if(colDiff == rowDiff){
                //the checking piece is attacking diagonally
                if(checkingP.row < king.row){
                    //the checking piece is above the king
                    if(checkingP.col < king.col){
                        //the checking piece is in the upper left
                       for(int col = checkingP.col,  row = checkingP.row; col < king.col; col++, row++){
                           for(Piece piece : simPieces) {
                               if(piece != king && piece.color != currentColor && piece.canMove(col, row)){
                                   return false;
                               }
                           }
                        }
                    }
                    if(checkingP.col > king.col){
                        //the checking piece is in the upper right
                        for(int col = checkingP.col,  row = checkingP.row; col > king.col; col--, row++){
                            for(Piece piece : simPieces) {
                                if(piece != king && piece.color != currentColor && piece.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                }
                if(checkingP.row > king.row){
                    //the checking piece is below the king
                    if(checkingP.col < king.col){
                        //the checking piece is in the lower left
                        for(int col = checkingP.col,  row = checkingP.row; col < king.col; col++, row--){
                            for(Piece piece : simPieces) {
                                if(piece != king && piece.color != currentColor && piece.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                    if(checkingP.col > king.col){
                        //the checking piece is in the lower right
                        for(int col = checkingP.col,  row = checkingP.row; col > king.col; col--, row--){
                            for(Piece piece : simPieces) {
                                if(piece != king && piece.color != currentColor && piece.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                }

            }
            else{

            }
        }


        return true;

    }
    private boolean kingCanMove(Piece king){
        // simulate if there is any square where the king can move to
        if(isValidMove(king, -1, -1)){return true;}
        if(isValidMove(king, 0, -1)){return true;}
        if(isValidMove(king, 1, -1)){return true;}
        if(isValidMove(king, -1, 0)){return true;}
        if(isValidMove(king, 1, 0)){return true;}
        if(isValidMove(king, -1, 1)){return true;}
        if(isValidMove(king, 0, 1)){return true;}
        if(isValidMove(king, 1, 1)){return true;}

        return false;
    }
    private boolean isValidMove(Piece king, int colPlus, int rowPlus){
        boolean isValidMove = false;
        //update the king's position for a second
        king.col += colPlus;
        king.row += rowPlus;

        if(king.canMove(king.col, king.row)){
            if(king.hittingP != null){
                simPieces.remove(king.hittingP.getIndex());
            }
            if(isIllegal(king) == false){
                isValidMove = true;
            }
        }

        // reset the king's position and restore the removed piece
        king.resetPosition();
        copyPieces(pieces, simPieces);

        return isValidMove;

    }

    private void checkCastling() {
        if (castlingP != null) {
            if (castlingP.col == 0) {
                castlingP.col += 3;
            } else if (castlingP.col == 7) {
                castlingP.col -= 2;
            }
            castlingP.x = castlingP.getX(castlingP.col);
        }
    }

    private void changePlayer() {
        if (currentColor == WHITE) {
            currentColor = BLACK;
        } else {
            currentColor = WHITE;
        }
        activeP = null; // active theo dõi quân cờ đang di chuyển, ở đây có nghĩa là không có quân cờ nào
    }

    private boolean canPromote() {
        if (activeP.type == Type.PAWN) {
            if (currentColor == WHITE && activeP.row == 0 || currentColor == BLACK && activeP.row == 7) {
                promoPieces.clear();
                promoPieces.add(new Rook(currentColor, 2, 9));
                promoPieces.add(new Knight(currentColor, 3, 9));
                promoPieces.add(new Bishop(currentColor, 4, 9));
                promoPieces.add(new Queen(currentColor, 5, 9));
                return true;
            }
        }
        return false;
    }

    private void promoting() {
        if (mouse.pressed) {
            for (Piece piece : promoPieces) {
                if (piece.row == mouse.x / Board.SQUARE_SIZE && piece.col == mouse.y / Board.SQUARE_SIZE) {
                    switch (piece.type) {
                        case ROOK:
                            simPieces.add(new Rook(currentColor, activeP.col,  activeP.row));
                            break;
                        case KNIGHT:
                            simPieces.add(new Knight(currentColor, activeP.col,  activeP.row));
                            break;
                        case BISHOP:
                            simPieces.add(new Bishop(currentColor, activeP.col,  activeP.row));
                            break;
                        case QUEEN:
                            simPieces.add(new Queen(currentColor, activeP.col,  activeP.row));
                            break;
                    }
                    simPieces.remove(activeP.getIndex());
                    copyPieces(simPieces, pieces);
                    activeP = null;
                    promotion = false;
                    changePlayer();
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        //Board
        board.draw(g2);
        //Pieces
        for (Piece p : simPieces) {
            p.draw(g2);
        }
        if (activeP != null) {
            if (canMove) {
                if (isIllegal(activeP) || oppenentCanCaptureKing()) {
                    g2.setColor(Color.red);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE,
                            Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                } else {
                    g2.setColor(Color.blue);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE,
                            Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }

            }
            activeP.draw(g2);
        }

        int capturedPieceSize = Board.SQUARE_SIZE / 2; // Kích thước của các quân cờ bị ăn nhỏ hơn
        int x = 815;
        int y = 100;

        // Draw captured pieces for Player 1 (white pieces captured by black)
        int capturedCount = 0;
        for (Piece p : capturedWhitePieces) {
            g2.drawImage(p.image, x + (capturedCount * capturedPieceSize), y, capturedPieceSize, capturedPieceSize, null);
            capturedCount++;
        }

        // Draw captured pieces for Player 2 (black pieces captured by white)
        x = 815;
        y = 770;
        capturedCount = 0;
        for (Piece p : capturedBlackPieces) {
            g2.drawImage(p.image, x + (capturedCount * capturedPieceSize), y, capturedPieceSize, capturedPieceSize, null);
            capturedCount++;
        }

            //Status messages
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(new Font("Book Antiqua", Font.PLAIN, 30));
            g2.setColor(Color.white);
            if (promotion) {
                g2.drawString("Promote to: ", 840, 200);
                for (Piece piece : promoPieces) {
                    g2.drawImage(piece.image, piece.getX(piece.row), piece.getY(piece.col), Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
                }
            } else {
                if (currentColor == WHITE) {
//                    g2.drawString("Player1 Move", 840, 550);
                    if (checkingP != null && checkingP.color == BLACK) {
                        g2.setColor(Color.red);
                        g2.drawString("the King", 840, 640);
                        g2.drawString("is in check!", 840, 690);
                    }
                } else {
//                    g2.drawString("Player2 Move", 840, 250);
                    if (checkingP != null && checkingP.color == WHITE) {
                        g2.setColor(Color.red);
                        g2.drawString("the King", 840, 110);
                        g2.drawString("is in check!", 840, 160);
                    }
                }
            }
        g2.setColor(Color.BLACK);
        g2.drawString("Player 1", 840, 720);
        g2.drawString("Player 2", 840, 50);

        // hiện time lên màn hình
        g2.setColor(Color.GREEN);
        updateTimers();
        g2.setFont(new Font("Book Antiqua", Font.PLAIN, 20));
        g2.drawString("Time: " + formatTime(whiteElapsedTime), 840, 750); // Display Player 1's time
        g2.drawString("Time: " + formatTime(blackElapsedTime), 840, 80); // Display Player 2's time
        if(gameover){
            String s = "";
            if(currentColor == WHITE){
                s = "WHITE WIN";
            }else{
                s = "BLACK WIN";
            }
            g2.setFont(new Font("Arial", Font.PLAIN, 90));
            g2.setColor(Color.RED);
            g2.drawString(s, 200, 420);
        }
    }
}

