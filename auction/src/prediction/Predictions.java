package prediction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


// This class uses mutliple linear regression to make an estimate of the opponent's bid
public class Predictions {

	public Predictions() {
		
	}
	
	public double [] get_regression_coeff(double[][] matrix, double [] mat_y){
		
		double[][] matrix_T = transpose(matrix);
		double[][] XTX = multiply(matrix_T, matrix);
		double[][] XTX_inv = inverse(XTX);
		double[][] XTX_inv_XT = multiply(XTX_inv, matrix_T);
		double [] b = multiply_line(XTX_inv_XT, mat_y);
		
		return b;
	}
	public double estimated_bid(List<Long> y_, List<Long> x_, double bid){
		List<Long> y= new ArrayList<Long>(y_);
		List<Long> xl= new ArrayList<Long>(x_);
		
		List<Double> x = new ArrayList<Double>();
		x=xl.stream()
			    .map(Double::valueOf)
			    .collect(Collectors.toList());
		
		x.add(bid);
		y.remove(0);
		
		
		
		
		List<Double> avg=new ArrayList<Double>();
		
		for(Double xi : x) {
			Double a=0.0;
			int j=1;
			for(int i=0; i<x.indexOf(xi); i++) {
				j++;
				a+=x.get(i);
			}
			avg.add(a/j);
			j=0;
			a=0.0;
		}
		 
		Double x1 = Double.valueOf(x.remove(x.size()-1));
		Double avg1 = Double.valueOf(avg.remove(avg.size()-1));
		int height = y.size()-1;
		int width = 3;
		List<Double> Y = y.stream()
		         .map(e -> Double.valueOf(e))
		         .collect(Collectors.toList());
		
		
		double[][] matrix = matrix_creation( width,  height,  x, avg);
		double[] mat_y = matrix_creation_line(height, Y);
		
		double[] b = get_regression_coeff(matrix, mat_y);
		
		double estimated_bid = b[0]+
							   b[1]*x1+
							   b[2]*avg1;
							   
		
		return estimated_bid;
	}
	
	public void printm(double[][] arr) {
		for (int row = 0; row < arr.length; row++)//Cycles through rows
		{
		  for (int col = 0; col < arr[row].length; col++)//Cycles through columns
		  {
		    System.out.print(" " + arr[row][col]); //change the %5d to however much space you want
		  }
		  System.out.println(); //Makes a new row
		}
	}

	public double[][] matrix_creation(int width, int height, List<Double> x, List<Double> avg){
		double[][] matrix =new double[height][width];
		
		for(int i=0; i<height; i++) {
			matrix[i][0]= 1.0;
			matrix[i][1]=x.get(i);
			matrix[i][2]=avg.get(i);
			
		}
		
		
		return matrix;
	}
	public double[] matrix_creation_line( int height, List<Double> x){
		double[] matrix =new double[height];
		
		for(int i=0; i<height; i++) {
			matrix[i]= x.get(i);
			
		}
		return matrix;
	}
	public Long[][] matrix_mult(Long[][] m1, Long[][] m2, int width, int height){
	
		 Long C[][] = new Long[width][width];
		 
		 for(int i=0; i<width; i++){
		      for(int j=0; j<width; j++){ 
		        C[i][j] = (long) 0;    
		        for(int k=0; k<height ;k++)    
		        { 
		          C[i][j] += m1[i][k] * m2[k][j];    
		        }
		      }
		 }
		 
		 return C;
	}
	public Long[][] matrix_transpose(Long[][] a, int height, int width) {
		Long[][] b= new Long[width][height];
		for(int i = 0;i<width;i++){
	         for(int j = 0;j<height;j++){
	        	 b[i][j]=a[j][i];
	         }
	      }
		return b;
	}
	private static double determinant(double[][] matrix) {
        if (matrix.length != matrix[0].length)
            throw new IllegalStateException("invalid dimensions");

        if (matrix.length == 2)
            return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];

        double det = 0;
        for (int i = 0; i < matrix[0].length; i++)
            det += Math.pow(-1, i) * matrix[0][i]
                    * determinant(minor(matrix, 0, i));
        return det;
    }

    private static double[][] inverse(double[][] matrix) {
        double[][] inverse = new double[matrix.length][matrix.length];

        // minors and cofactors
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[i].length; j++)
                inverse[i][j] = Math.pow(-1, i + j)
                        * determinant(minor(matrix, i, j));

        // adjugate and determinant
        double det = 1.0 / determinant(matrix);
        for (int i = 0; i < inverse.length; i++) {
            for (int j = 0; j <= i; j++) {
                double temp = inverse[i][j];
                inverse[i][j] = inverse[j][i] * det;
                inverse[j][i] = temp * det;
            }
        }

        return inverse;
    }

    private static double[][] minor(double[][] matrix, int row, int column) {
        double[][] minor = new double[matrix.length - 1][matrix.length - 1];

        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; i != row && j < matrix[i].length; j++)
                if (j != column)
                    minor[i < row ? i : i - 1][j < column ? j : j - 1] = matrix[i][j];
        return minor;
    }

    private static double[][] multiply(double[][] a, double[][] b) {
        if (a[0].length != b.length)
            throw new IllegalStateException("invalid dimensions");

        double[][] matrix = new double[a.length][b[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b[0].length; j++) {
                double sum = 0;
                for (int k = 0; k < a[i].length; k++)
                    sum += a[i][k] * b[k][j];
                matrix[i][j] = sum;
            }
        }

        return matrix;
    }
    private static double[] multiply_line(double[][] a, double[] b) {
        if (a[0].length != b.length)
            throw new IllegalStateException("invalid dimensions");

        double[] matrix = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < 1; j++) {
                double sum = 0;
                for (int k = 0; k < a[i].length; k++)
                    sum += a[i][k] * b[k];
                matrix[i] = sum;
            }
        }

        return matrix;
    }


    private static double[][] transpose(double[][] matrix) {
        double[][] transpose = new double[matrix[0].length][matrix.length];

        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[i].length; j++)
                transpose[j][i] = matrix[i][j];
        return transpose;
    }
	
}
