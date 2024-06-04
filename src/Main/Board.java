package Main;

import java.awt.*;

public class Board {
    final int MAX_COL = 8;
    final int MAX_ROW = 8;
    public static final int SQUARE_SIZE = 100;
    public static final int HALF_SQUARE_SIZE = SQUARE_SIZE/2;

    public void draw(Graphics2D g2) {
        int c = 0;
        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                if (c == 0) {
                    g2.setColor(new Color(236, 236, 208));
                    c = 1;
                } else {
                    g2.setColor(new Color(119, 149, 87));
                    c = 0;
                }
                g2.fillRect(col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }
            if (c == 0) {
                c = 1;
            } else {
                c = 0;
            }
        }

        // Draw column labels (1 to 8)
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
//        for (int col = 0; col < MAX_COL; col++) {
//            char colLabel = (char) ('A' + col);
//            g2.drawString(String.valueOf(colLabel), col * SQUARE_SIZE + HALF_SQUARE_SIZE - 5, MAX_ROW * SQUARE_SIZE + 20);
//        }
        for (int col = 0; col < 8; col++) {
//            int rowLabel = MAX_ROW - row;
            g2.drawString(String.valueOf(col+1), col * SQUARE_SIZE + HALF_SQUARE_SIZE - 5, MAX_ROW * SQUARE_SIZE + 20);
        }


        // Draw row labels (1 to 8)
        for (int row = 0; row < 8; row++) {
//            int rowLabel = MAX_ROW - row;
            g2.drawString(String.valueOf(row+1), MAX_COL * SQUARE_SIZE + 5, row * SQUARE_SIZE + HALF_SQUARE_SIZE + 5);
        }
    }
}
