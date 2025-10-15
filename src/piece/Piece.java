package piece;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;
import javax.imageio.ImageIO;
import main.Board;
import main.GamePanel;
import main.Type;

public class Piece {
    public Type type;
    public BufferedImage image;
    public int x, y;
    public int col, row, preCol, preRow;
    public int color;
    public Piece hittingPiece;
    public boolean moved, twoStepped;

    public Piece(int color, int col, int row) {
        this.color = color;
        this.col = col;
        this.row = row;
        x = getX(col);
        y = getY(row);
        preCol = col;
        preRow = row;
    }
    public BufferedImage getImage(String imagePath) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream(imagePath + ".png")));
            Image scaledImage = image.getScaledInstance(Board.SQUARE_SIZE, Board.SQUARE_SIZE, Image.SCALE_SMOOTH);
            BufferedImage resizedImage = new BufferedImage(Board.SQUARE_SIZE, Board.SQUARE_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = resizedImage.createGraphics();
            g2.drawImage(scaledImage, 0, 0, null);
            g2.dispose();
            image = resizedImage;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    public int getX(int col) {
        return col * Board.SQUARE_SIZE;
    }

    public int getY(int row) {
        return row * Board.SQUARE_SIZE;
    }

    public int getCol(int x) {
        return (x + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
    }

    public int getRow(int y) {
        return (y + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
    }

    public int getIndex() {
        for (int index = 0; index < GamePanel.simPieces.size(); index++) {
            if (GamePanel.simPieces.get(index) == this) {
                return index;
            }
        }
        return 0;
    }

    public void updatePosition() {

        // Check En Passant
        if(type == Type.PAWN) {
            if(Math.abs(row - preRow) == 2) {
                twoStepped = true;
            }
        }

        x = getX(col);
        y = getY(row);
        preCol = getCol(x);
        preRow = getRow(y);
        moved = true;
    }

    public void resetPosition() {
        col = preCol;
        row = preRow;
        x = getX(col);
        y = getY(row);
    }

    public boolean canMove(int targetCol, int targetRow) {
        return false;
    }

    public boolean isWithinBoard(int targetCol, int targetRow) {
        if (targetCol >= 0 && targetCol <= 7 && targetRow >= 0 && targetRow <= 7) {
            return true;
        }
        return false;
    }

    public boolean isSameSquare(int targetCol, int targetRow) {
        if (targetCol == preCol && targetRow == preRow) {
            return true;
        }
        return false;
    }

    public Piece getHittingPiece(int targetCol, int targetRow) {
        for (Piece piece : GamePanel.simPieces) {
            if (piece.col == targetCol && piece.row == targetRow && piece != this) {
                return piece;
            }
        }
        return null;
    }

    public boolean isValidSquare(int targetCol, int targetRow) {
        hittingPiece = getHittingPiece(targetCol, targetRow);
        if (hittingPiece == null) { // This square is vacant
            return true;
        }
        else { // This square is occupied
            if ( hittingPiece.color != this.color ) { // if the color is different, it can be captured
                return true;
            }
            else {
                hittingPiece = null;
            }
        }
        return false;
    }

    public boolean pieceIsOnStraightLine(int targetCol, int targetRow) {
        // When piece is moving to the left
        for (int c = preCol - 1; c > targetCol; c--) {
            for (Piece piece : GamePanel.simPieces) {
                if (piece.col == c && piece.row == targetRow) {
                    hittingPiece = piece;
                    return true;
                }
            }
        }
        // When piece is moving to the right
        for (int c = preCol + 1; c < targetCol; c++) {
            for (Piece piece : GamePanel.simPieces) {
                if (piece.col == c && piece.row == targetRow) {
                    hittingPiece = piece;
                    return true;
                }
            }
        }
        // When piece is moving up
        for (int r = preRow - 1; r > targetRow; r--) {
            for (Piece piece : GamePanel.simPieces) {
                if (piece.col == targetCol && piece.row == r) {
                    hittingPiece = piece;
                    return true;
                }
            }
        }
        // When piece is moving down
        for (int r = preRow + 1; r < targetRow; r++) {
            for (Piece piece : GamePanel.simPieces) {
                if (piece.col == targetCol && piece.row == r) {
                    hittingPiece = piece;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean pieceIsOnDiagonalLine(int targetCol, int targetRow) {
        if(targetRow < preRow) {
            // Up Left
            for(int c = preCol - 1; c > targetCol; c--) {
                int diff = Math.abs(c - preCol);
                for(Piece piece : GamePanel.simPieces) {
                    if(piece.col == c && piece.row == preRow - diff) {
                        hittingPiece = piece;
                        return true;
                    }
                }
            }
            // Up Right
            for(int c = preCol + 1; c < targetCol; c++) {
                int diff = Math.abs(c - preCol);
                for(Piece piece : GamePanel.simPieces) {
                    if(piece.col == c && piece.row == preRow - diff) {
                        hittingPiece = piece;
                        return true;
                    }
                }
            }
        }
        if(targetRow > preRow) {
            // Down Left
            for(int c = preCol - 1; c > targetCol; c--) {
                int diff = Math.abs(c - preCol);
                for(Piece piece : GamePanel.simPieces) {
                    if(piece.col == c && piece.row == preRow + diff) {
                        hittingPiece = piece;
                        return true;
                    }
                }
            }
            // Down Right
            for(int c = preCol + 1; c < targetCol; c++) {
                int diff = Math.abs(c - preCol);
                for(Piece piece : GamePanel.simPieces) {
                    if(piece.col == c && piece.row == preRow + diff) {
                        hittingPiece = piece;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void draw(Graphics2D g2) {
        if (image != null) {
            // Calculate offset to center the 50x50 piece in a 100x100 square
            int offset = (Board.SQUARE_SIZE - 50) / 2; // (100 - 50)/2 = 25
            int drawX = x + offset; // x = col * 100
            int drawY = y + offset; // y = row * 100
            g2.drawImage(image, drawX, drawY, 50, 50, null);
        } else {
            // Fallback (optional)
            g2.setColor(Color.RED);
            g2.fillRect(x, y, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
        }
        g2.drawImage(image, x, y, Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
    }
}
