package foogame;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Optional;
import java.util.function.Function;

public class GameLogger implements GameObserver {
	private final BufferedWriter writer;

	public GameLogger(String fileName) throws IOException {
		this(new FileWriter(fileName));
	}

	public GameLogger(Writer writer) {
		this(new BufferedWriter(writer));
	}

	public GameLogger(BufferedWriter writer) {
		this.writer = writer;
	}

	public void acceptUpdate(GameUpdate update) {
		try {
			writer.write(stringifyUpdate(update) + stringTPS(update));
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String stringifyUpdate(GameUpdate update) {
		StringBuilder b = new StringBuilder();
		// print out new board
		b.append(String.format("Last move: %s%n", update.last.ptn()));
		b.append(String.format("Move by %s resulted in board:%n", update.last.color));
		b.append(stringifyBoard(update.board));

		return b.toString();
	}
	
	public static String stringifyBoard(Board board) {
		StringBuilder b = new StringBuilder();
		int size = board.size;
		b.append("+----------+\n");
		for (int i = 0; i < size; i++) {
			b.append("|");
			for (int j = 0; j < size; j++) {
				b.append(" "
						+ Optional.of(board.getBoardArray()[size - i - 1][j]).filter(x -> !x.isEmpty()).map(Stack::top)
								.map(x -> ((Function<String, String>) (x.type == PieceType.FLAT
										? (Function<String, String>) (y -> y.toUpperCase())
										: (Function<String, String>) (y -> y.toLowerCase())))
												.apply(x.color.name().substring(0, 1)))
								.orElse(" "));
			}
			b.append("|\n");
		}
		b.append("+----------+\n");
		b.append(String.format("Num stones left: %s%n", board.getNumStones()));
		b.append(String.format("Board spots: %s%n", board.numStonesOnBoard()));
		return b.toString();
	}

	public static String stringTPS(GameUpdate update)
	{
		Board board = update.board;
		int size = board.size;
		// Tak Positional System (TPS)
		StringBuilder tps = new StringBuilder();
		tps.append("[TPS \"");
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				Stack stones = board.getBoardArray()[i][j];
				if (!stones.isEmpty()) {
					for (int r = 0; r < stones.getCopy().length; r++) {
						Stone piece = stones.getCopy()[r];
						if (piece.color == Color.WHITE) {
							tps.append("1");
						} else {
							tps.append("2");
						}
						if (piece.type == PieceType.WALL) {
							tps.append("S");
						}
						if (piece.type == PieceType.CAPSTONE) {
							tps.append("C");
						}
					}
				} else {
					tps.append("x");
				}
				if (j != size - 1) {
					tps.append(",");
				}
			}
			if (i != size - 1) {
				tps.append("/");
			}

		}
		int whoseTurn = board.whoseTurn == Color.WHITE ? 1 : 2;
		tps.append(String.format(" %d %d\"]%n", whoseTurn, ((update.board.turnNumber)/2)+1));
		return tps.toString();
	}
}
