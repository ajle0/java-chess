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
    Piece activePiece;


    // COLOR
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int currentColor = WHITE;

    // GAME-STATE BOOLEANS
    boolean canMove;
    boolean validSquare;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.black);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);
        setPieces();
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
                    changePlayer();
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
    public void simulate() {
        canMove = false;
        validSquare = false;

        // Repeat the list in every loop
        copyPieces(pieces, simPieces);

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
            validSquare = true;
        }
    }

    private void changePlayer() {
        if(currentColor == WHITE) {
            currentColor = BLACK;
        }
        else {
            currentColor = WHITE;
        }
        activePiece = null;
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
                g2.setColor(Color.white);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                g2.fillRect(activePiece.col * Board.SQUARE_SIZE, activePiece.row * Board.SQUARE_SIZE,
                        Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }

            activePiece.draw(g2);
        }
        // STATUS MESSAGES
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Book Antiqua", Font.PLAIN, 30));
        g2.setColor(Color.white);

        if(currentColor == WHITE) {
            g2.drawString("White's turn", 700, 550);
        }
        else {
            g2.drawString("Black's turn", 700, 150);
        }
    }
}
