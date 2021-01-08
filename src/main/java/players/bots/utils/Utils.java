package players.bots.utils;

import java.util.Locale;

/**
 * Note that in all matrices, a (n*m) matrix would be represented by a double[n][m] where n denotes the height and m denotes the width<br>
 * Distinction is also made between column vectors and row vectors, and the type may be assumed in Matrix*vector and vector*Matrix multiplication methods
 */
public final class Utils {
	
	/**
	 * @param <U> input-1
	 * @param <V> input-2
	 * @param <W> input-3
	 * @param <Y> output parameter
	 */
	@FunctionalInterface
	public static interface TriFunction<U, V, W, Y> {
		Y apply(U u, V v, W w);
	}
	
	public static void printMatrix(double[][] m) {
		System.out.println(matrixToString(m));
	}
	
	public static double[][] parseMatrix(String string) {
		String mstr = string.replaceAll("[\\(\\)\\s\\h\\v]", "");
		String[] raw_rows = mstr.split("\\]\\[");
		double[][] matrix = new double[raw_rows.length][];
		for (int i=0; i < raw_rows.length; i++) {
			String[] numbers = raw_rows[i].replaceAll("[\\[\\]]", "").split(",");
			matrix[i] = new double[numbers.length];
			for (int j=0; j < numbers.length; j++)
				matrix[i][j] = Double.parseDouble(numbers[j]);
		}
		return matrix;
	}
	
	public static String matrixToString(double[][] m) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (int i=0; i < m.length; i++) {
			if (i > 0) sb.append(" ");
			sb.append("[");
			for (int j=0; j < m[i].length; j++) {
				sb.append(String.format(Locale.CANADA, "% .15f", m[i][j]));
				if (j < m[i].length - 1) sb.append(", ");
			}
			sb.append("]");
			if (i < m.length - 1) sb.append("\n");
			else sb.append(")");
		}
		return sb.toString();
	}
	
	public static double[][] transpose(double[][] m) {
		double[][] result = new double[m[0].length][m.length];
		for (int i=0; i < m.length; i++)
			for (int j=0; j < m[i].length; j++)
				result[j][i] = m[i][j];
		return result;
	}
	
	public static double[][] getColumnVector(double[] v) {
		double[][] res = new double[v.length][1];
		for (int i=0; i < v.length; i++)
			res[i][0] = v[i];
		return res;
	}
	public static double[][] getRowVector(double[] h) {
		double[][] res = new double[1][h.length];
		for (int i=0; i < h.length; i++)
			res[0][i] = h[i];
		return res;
	}
	
	/**
	 * @param vector must be of shape (1xN) or (Nx1)
	 * @return the corresponding vector in array form
	 */
	public static double[] extractVector(double[][] vector) {
		int height = vector.length;
		int width = vector[0].length;
		if (height > 1 && width > 1) throw new RuntimeException("given vector is not of shape 1xN or Nx1: ("+height+"x"+width+")");
		if (width > height) return vector[0];
		double[] res = new double[height];
		for (int i=0; i < height; i++)
			res[i] = vector[i][0];
		return res;
	}
	
	/**
	 * @param m a matrix
	 * @param v a vector, handled as column vector
	 * @return resulting matrix from computing m * v
	 */
	public static double[][] matrixVectorMul(double[][] m, double[] v) {
		return martixMatrixMul(m, getColumnVector(v));
	}
	
	/**
	 * @param h a vector, handled as row vector
	 * @param m a matrix
	 * @return resulting matrix from computing h * m
	 */
	public static double[][] vectorMatrixMul(double[] h, double[][] m) {
		return martixMatrixMul(getRowVector(h), m);
	}
	
	public static double[][] martixMatrixMul(double[][] a, double[][] b) {
		// courtesy of a CS1 assignment (code by Pascal Anema)
		int rowsA = a.length;
		int colsA = a[0].length;
		int colsB = b[0].length;
		if (colsA != b.length) throw new MatrixSizeMismatchException(rowsA, colsA, b.length, colsB);
		double[][] result = new double[rowsA][colsB];
		for (int i=0; i < rowsA; i++)
			for (int j=0; j < colsB; j++)
				for (int z=0; z < colsA; z++)
					result[i][j] += a[i][z] * b[z][j];
		return result;
	}
	
	public static class MatrixSizeMismatchException extends RuntimeException {
		
		private static final long serialVersionUID = -6058559419243957181L;

		public MatrixSizeMismatchException(int rowsA, int colsA, int rowsB, int colsB) {
			super("can't match matrices of shapes ("+rowsA+"x"+colsA+") and ("+rowsB+"x"+colsB+")");
		}
	}
	
}
