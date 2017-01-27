package ln.afm.gui;

import javax.swing.JTextArea;
import javax.swing.JTextField;

public class RunAnalysis {
	
	private CurveData userData;
	private JTextArea userLog;
//	private double sensFactor;
//	private double sprConst;
//	private double alpha;
//	private double impactZ;
	private String sensFactor;
	private String sprConst;
	private String alpha;
	private String impactZ;

	public RunAnalysis(CurveData data, JTextArea log, String[] inputs) {
		userData = data;
		userLog = log;
//		sensFactor = Double.parseDouble(inputs[0]);
//		sprConst = Double.parseDouble(inputs[1]);
//		alpha = Double.parseDouble(inputs[2]);
//		impactZ = Double.parseDouble(inputs[3]);
		sensFactor = inputs[0];
		sprConst = inputs[1];
		alpha = inputs[2];
		impactZ = inputs[3];
		log.append("Running: " + sensFactor + " " + sprConst + " " + alpha + " " + impactZ + "\n");
	}

}
