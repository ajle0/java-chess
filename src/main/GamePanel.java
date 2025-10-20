package main;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.JPanel;
import piece.*;


public class GamePanel extends JPanel implements Runnable {
    public static final int WIDTH = 940;
    public static final int HEIGHT = 640;
    final int FPS = 60;
    Thread gameThread;
    Board board = new Board();
    Mouse mouse = new Mouse();

    // PIECES
    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();
    ArrayList<Piece> promotionPieces = new ArrayList<>();
    Piece activePiece, checkingPiece;
    public static Piece castlingPiece;


    // COLOR
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int currentColor = WHITE;

    // GAME-STATE BOOLEANS
    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameOver;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.black);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);
        //setPieces();
        //testPromotion();
        testIllegal();

        copyPieces(pieces, simPieces);

    }
    public void launchGame(){
        gameThread = new Thread(this);
        gameThread.start();
    }
    public void setPieces() {
        // White
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
        pieces.add(new Knight(WHITE, 1, 7));
        pieces.add(new Knight(WHITE, 6, 7));
        pieces.add(new Bishop(WHITE, 2, 7));
        pieces.add(new Bishop(WHITE, 5, 7));
        pieces.add(new Queen(WHITE, 3, 7));
        pieces.add(new King(WHITE, 4, 7));

        // Black
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
    public void testPromotion(){
        pieces.add(new Pawn(WHITE, 0, 3));
        pieces.add(new Pawn(BLACK, 5,4));
    }

    public void testIllegal(){
        pieces.add(new Pawn(WHITE, 7, 6));
        pieces.add(new King(WHITE, 3, 7));
        pieces.add(new King(BLACK, 0, 3));
        pieces.add(new Bishop(BLACK, 1, 4));
        pieces.add(new Queen(BLACK, 4, 5));
    }

    private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {
        target.clear();
        for(int i = 0; i < source.size(); i++) {
            target.add(source.get(i));
        }
    }
    @Override
    public void run() {
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
                delta --;
            }
        }
    }
    private void update() {
        if (promotion) {
            promoting();
        }
        else {
            /// MOUSE BUTTON PRESSED
            if (mouse.pressed) {
                if (activePiece == null) {
                    /// If there is no active piece, check if you can pick up a piece
                    for (Piece piece : simPieces) {
                        ///  If the mouse is on the same color, pick it up as the active piece
                        if (piece.color == currentColor && piece.col == mouse.x / Board.SQUARE_SIZE && piece.row == mouse.y / Board.SQUARE_SIZE) {
                            activePiece = piece;
                            break;
                        }
                    }
                }
                else {
                    // If the player is holding a piece, simulate the move
                    simulate();
                }
            }
            /// MOUSE BUTTON RELEASED
            if (!mouse.pressed) {
                if (activePiece != null) {
                    if (validSquare) {
                        // Move confirmed

                        // Update the piece list in case a piece has been captured and removed during the simulation
                        copyPieces(simPieces, pieces);
                        activePiece.updatePosition();
                        if(castlingPiece != null) {
                            castlingPiece.updatePosition();
                        }

                        if(isKingInCheck()) {

                        }

                        if(canPromote()) {
                            promotion = true;
                        }
                        else {
                            changePlayer();
                        }

                    }
                    else {
                        // The move is not valid, so reset
                        copyPieces(simPieces, pieces);
                        activePiece.resetPosition();
                        activePiece = null;
                    }
                }
            }
        }
    }
    public void simulate() {
        canMove = false;
        validSquare = false;

        // Repeat the list in every loop
        copyPieces(pieces, simPieces);

        if (castlingPiece != null) {
            castlingPiece.col = castlingPiece.preCol;
            castlingPiece.x = castlingPiece.getX(castlingPiece.col);
            castlingPiece = null;
        }

        // If a piece is being held, update its positioned
        activePiece.x = mouse.x - Board.HALF_SQUARE_SIZE;
        activePiece.y = mouse.y - Board.HALF_SQUARE_SIZE;
        activePiece.col = activePiece.getCol(activePiece.x);
        activePiece.row = activePiece.getRow(activePiece.y);

        // Check if piece is hovering above a reachable square
        if (activePiece.canMove(activePiece.col, activePiece.row)) {
            canMove = true;

            // if hitting a piece, remove from board
            if (activePiece.hittingPiece != null) {
                simPieces.remove(activePiece.hittingPiece.getIndex());
            }
            checkCastling();

            if(!isIllegal(activePiece) && !opponentCanCaptureKing()) {
                validSquare = true;
            }
        }
    }

    private boolean isIllegal (Piece king) {
        if(king.type == Type.KING) {
            for(Piece piece : simPieces) {
                if(piece != king && piece.color != king.color && piece.canMove(king.col, king.row))  {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean opponentCanCaptureKing() {
        Piece king = getKing(false);

        for(Piece piece : simPieces) {
            if(piece.color != king.color && piece.canMove(king.col, king.row)) {
                return true;
            }
        }
        return false;
    }

    private boolean isKingInCheck() {

        Piece king = getKing(true);

        if(activePiece.canMove(king.col, king.row)) {
            checkingPiece = activePiece;
            return true;
        }
        else {
            checkingPiece = null;
        }

        return false;
    }

    private Piece getKing(boolean opponent) {

        Piece king = null;

        for(Piece piece : simPieces) {
            if(opponent) {
                if(piece.type == Type.KING && piece.color != currentColor) {
                    king = piece;
                }
            }
            else {
                if(piece.type == Type.KING && piece.color == currentColor) {
                    king = piece;
                }
            }
        }
        return king;
    }

    private boolean isCheckmate() {

    }

    private boolean kingCanMove(Piece king) {

    }

    private boolean isValidMove(Piece king, int colPlus, int rowPlus) {

    }

    private void checkCastling(){
        if(castlingPiece != null) {
            if(castlingPiece.col == 0) {
                castlingPiece.col += 3;
            } else if (castlingPiece.col == 7) {
                castlingPiece.col -= 2;
            }
            castlingPiece.x = castlingPiece.getX(castlingPiece.col);
        }
    }
    private void changePlayer() {
        if(currentColor == WHITE) {
            currentColor = BLACK;
            // Change black's two-stepped status
            for(Piece piece : pieces) {
                if(piece.color == BLACK) {
                    piece.twoStepped = false;
                }
            }
        }
        else {
            currentColor = WHITE;
            for(Piece piece : pieces) {
                if(piece.color == WHITE) {
                    piece.twoStepped = false;
                }
            }
        }
        activePiece = null;
    }
    private boolean canPromote() {
        if(activePiece.type == Type.PAWN) {
            if (currentColor == WHITE && activePiece.row == 0 || currentColor == BLACK && activePiece.row == 7) {
                promotionPieces.clear();
                promotionPieces.add(new Rook(currentColor, 9, 2));
                promotionPieces.add(new Knight(currentColor, 9, 3));
                promotionPieces.add(new Bishop(currentColor, 9, 4));
                promotionPieces.add(new Queen(currentColor, 9, 5));
                return true;
            }
        }
        return false;
    }

    private void promoting() {
        if(mouse.pressed) {
            for(Piece piece : promotionPieces) {
                if(piece.col == mouse.x/Board.SQUARE_SIZE && piece.row == mouse.y/Board.SQUARE_SIZE) {
                    switch (piece.type) {
                        case ROOK: simPieces.add(new Rook(currentColor, activePiece.col, activePiece.row)); break;
                        case KNIGHT: simPieces.add(new Knight(currentColor, activePiece.col, activePiece.row)); break;
                        case BISHOP: simPieces.add(new Bishop(currentColor, activePiece.col, activePiece.row)); break;
                        case QUEEN: simPieces.add(new Queen(currentColor, activePiece.col, activePiece.row)); break;
                        default: break;
                    }
                    simPieces.remove(activePiece.getIndex());
                    copyPieces(simPieces, pieces);
                    activePiece = null;
                    promotion = false;
                    changePlayer();  
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Draw the chessboard (left side)
        board.draw(g2);

        // Draw pieces on the board
        for (Piece p : simPieces) {
            p.draw(g2);
        }

        if (activePiece != null) {
            if (canMove) {
                if(isIllegal(activePiece) || opponentCanCaptureKing()) {
                    g2.setColor(Color.red);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2.fillRect(activePiece.col * Board.SQUARE_SIZE, activePiece.row * Board.SQUARE_SIZE,
                            Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }
                else {
                    g2.setColor(Color.white);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2.fillRect(activePiece.col * Board.SQUARE_SIZE, activePiece.row * Board.SQUARE_SIZE,
                            Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }
            }

            activePiece.draw(g2);
        }
        // STATUS MESSAGES

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Book Antiqua", Font.PLAIN, 30));
        g2.setColor(Color.white);

        if(promotion) {
            g2.drawString("Promote to: ", 700, 150);
            for(Piece piece : promotionPieces) {
                g2.drawImage(piece.image, piece.getX(piece.col),  piece.getY(piece.row), Board.SQUARE_SIZE,
                        Board.SQUARE_SIZE, null);
            }
        }
        else {
            if(currentColor == WHITE) {
                g2.drawString("White's turn", 700, 550);
                if (checkingPiece != null && checkingPiece.color == BLACK) {
                    g2.setColor(Color.red);
                    g2.drawString("King in check", 700, 450);
                }
            }
            else {
                g2.drawString("Black's turn", 700, 150);
                if (checkingPiece != null && checkingPiece.color == WHITE) {
                    g2.setColor(Color.red);
                    g2.drawString("King in check", 700, 50);
                }
            }
        }
    }
}
