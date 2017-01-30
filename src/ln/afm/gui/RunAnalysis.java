package ln.afm.gui;

import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.ejml.data.DenseMatrix32F;
import org.ejml.data.DenseMatrix64F;

public class RunAnalysis {
	
	private CurveData userData;
	private JTextArea userLog;
	private double sensFactor;
	private double sprConst;
	private double alpha;
	private double impactZ;
	
	/**
	 * Columns are : 	0.Z(nm)		1.D(V)		2.D(nm)		3.Z-Z0(nm)		4.D-D0(nm)		5.D-D0(m) 
	 * 					6.del(nm)	7.del(m)	8.F(N)		9.ln(del)		10.ln(F)
	 */
	private DenseMatrix64F dataMatrix;

	public RunAnalysis(CurveData data, JTextArea log, double[] inputs) { //TODO ERROR HANDLING
		userData = data;
		userLog = log;
		sensFactor = inputs[0];
		sprConst = inputs[1];
		alpha = inputs[2];
		impactZ = inputs[3];
		log.append("Running: " + sensFactor + " " + sprConst + " " + alpha + " " + impactZ + "\n");
		initMatrix(data);
	}
	
	private void initMatrix(CurveData data)
	{
		userLog.append("Initializing matrix..." + "\n");
		double[][] curveArray = data.toArray();
		int numCols = 11;
		int numRows = curveArray[0].length;
		dataMatrix = new DenseMatrix64F(numRows, numCols);
		for(int i=0;i<2;i++)
		{
			for(int j=0;j<numRows;j++)
			{
				dataMatrix.set(j, i, curveArray[i][j]);
			}
		}
	}
	
	//returns index of value closest to that given
	private int getClosest(double value, int column) //TODO ERROR HANDLING
	{
		userLog.append("Find closest value..." + "\n");
		int closestIndex = -1;
		double closestDistance = value - dataMatrix.get(0, column);
		double tempDist = 0;
		for(int i=0;i < dataMatrix.numRows;i++)
		{
			tempDist = value - dataMatrix.get(i, column);
			tempDist = Math.abs(tempDist);
			if(tempDist < closestDistance)
			{
				closestDistance = tempDist;
				closestIndex = i;
			}
		}
		userLog.append("Closest value is: " + dataMatrix.get(closestIndex, column) +"\n");
		return closestIndex;
	}
	
	private double getAvgD0()
	{
		int lastIndex = getClosest(impactZ, 0) + 1;
		int firstIndex = lastIndex - 4;
		double currentTotal = 0;
		for(int i = firstIndex; i < lastIndex; i++)
		{
			currentTotal = currentTotal + dataMatrix.get(i, 0);
		}
		double avgD0 = currentTotal/4;
		userLog.append("Average D0 value is: " + avgD0 + "\n");
		return avgD0;
	}
	
	public void run()
	{
		userLog.append("Running..." + "\n");
		//calcMatrix();
		getAvgD0(); //call in calcMatrix
		//FitMatrix()
		//Save Force Distance Curve (regression overlay?)
		//Save BestFitLine
	}

}
