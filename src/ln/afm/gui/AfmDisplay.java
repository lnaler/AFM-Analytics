package ln.afm.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.fx.overlay.CrosshairOverlayFX;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.RectangleEdge;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import net.miginfocom.swing.MigLayout;

/**
 * GUI for AFM-Analytics program
 * @author Lynette Naler
 *
 */
public class AfmDisplay{
	//private static final Logger LOGGER = Logger.getLogger(AfmDisplay.class.getName() ); //TODO logging
	private static int FXWIDTH = 445;
	private static int FXHEIGHT = 340;
	
	private JFrame frmAfmanalytics;	
	private JFormattedTextField sensFactorField;
	private JFormattedTextField sprConstField;
	private JFormattedTextField alphaField;
	private static JFormattedTextField impactZField = new JFormattedTextField(NumberFormat.getNumberInstance());
	private JFormattedTextField gelThicknessField;
	private static double clickedZ = 0;
	
	private double sensFactor;
	private double sprConst;
	private double alpha;
	private double impactZ;
	private double zLimit;
	private double gelThickness;
	
	private File dataFile;
	private CurveData data;
	private static boolean zUpdatedFX;
	private boolean limitZ = false;
	private JFormattedTextField indentLimit;
	private JTextField txtSlope;
	private JTextField txtRsquared;
	private JTextField txtYoungsModulus;
	private JTextField txtExp;
	private JCheckBox chckbxSelectZ;
	
	@SuppressWarnings("unused")
	private boolean dataUpload;
	@SuppressWarnings("unused")
	private JTextArea log;
	private ChartPanel chartPanel;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AfmDisplay window = new AfmDisplay();
					window.frmAfmanalytics.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public AfmDisplay() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		sensFactor = 0;
		sprConst = 0;
		alpha = 0;
		impactZ = 0;
		
		//Key window setup
		frmAfmanalytics = new JFrame();
		frmAfmanalytics.setResizable(false);
		frmAfmanalytics.getContentPane().setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 16));
		frmAfmanalytics.setTitle("AFM-Analytics");
		frmAfmanalytics.setSize(800, 600);
		frmAfmanalytics.setLocationByPlatform(true);
		frmAfmanalytics.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmAfmanalytics.getContentPane().setLayout(new MigLayout("", "[pref!][130.00px:130.00px][95.00px:95.00px,grow][49.00px:49.00px][72.00px:84.00px][30px:50.00px][30px:30px][72.00px:72.00px][72.00px:72.00px][35px:35.00px][30px:30px][30px:30px]", "[28px:28px][28px:28px][28px:28px][28px:28px][28px:28px][28px:28px][28px:28px][28px:28px][28:28][28px:28px][23px:23px][28px:28px][28px:28px][28px:28px][28px:28px][28px:28px][28px:28px]"));
		
		//Panel to display our main chart
		JFXPanel fxPanel = new JFXPanel(); //https://docs.oracle.com/javase/8/javafx/interoperability-tutorial/swing-fx-interoperability.htm
		fxPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		fxPanel.setBackground(Color.WHITE);
		frmAfmanalytics.getContentPane().add(fxPanel, "flowx,cell 0 0 7 11,grow");
		//FXPanels have to be on a separate thread. Initializing it now.
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX(fxPanel);
            }
		});
		
		JLabel lblGelThickness = new JLabel("Gel Thickness");
		lblGelThickness.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(lblGelThickness, "cell 1 11,alignx trailing");
		
		//Formatted field associated with gelThickness
		gelThicknessField = new JFormattedTextField(NumberFormat.getNumberInstance());
		gelThicknessField.setEditable(false);
		gelThicknessField.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		gelThicknessField.setValue(new Double(0));
		frmAfmanalytics.getContentPane().add(gelThicknessField, "cell 2 11,growx");
		//Update gelThickness if the field changes
		gelThicknessField.getDocument().addDocumentListener(new DocumentListener() { //http://stackoverflow.com/questions/3953208/value-change-listener-to-jtextfield
			public void changedUpdate(DocumentEvent e) {
				updateVal();
			}
			public void removeUpdate(DocumentEvent e) {
				updateVal();
			}
			public void insertUpdate(DocumentEvent e) {
				updateVal();
			}
			
			public void updateVal(){
				boolean isNum = FileParser.isDouble(sensFactorField.getText());
				if(isNum)
				{
					gelThickness = ((Number)gelThicknessField.getValue()).doubleValue();
				}
			}
		});
		
		JLabel lblnm2 = new JLabel("(nm)");
		lblnm2.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 16));
		frmAfmanalytics.getContentPane().add(lblnm2, "cell 3 11,alignx left");
		
		//Scrollpane for our log (next)
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		frmAfmanalytics.getContentPane().add(scrollPane, "cell 8 12 4 4,grow");
		
		//Log that will display information to the user
		JTextArea log = new JTextArea();
		log.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 16));
		log.setEditable(false);
		log.setForeground(Color.BLACK);
		scrollPane.setViewportView(log);
		
		//Now that we have a log to pass, we can instantiate our data //TODO fix this somehow
		data = new CurveData(log);
		
		//Clears the voltage-distance chart (raw data)
		JButton btnClearData = new JButton("Clear");
		btnClearData.setEnabled(false);
		btnClearData.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(btnClearData, "cell 4 11,alignx left");
		btnClearData.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				log.append("Clearing data..." + "\n");
				Platform.runLater(new Runnable() { 
		            @Override
		            public void run() {
		                initFX(fxPanel);
		            }
				});
			}
		});
		
		//View the voltage-distance chart (raw data)
		JButton btnView = new JButton("View");
		btnView.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		btnView.setEnabled(false);
		frmAfmanalytics.getContentPane().add(btnView, "cell 5 11 2 1,growx");
		btnView.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				String[] testUnits = {"nm","V"};
				data.changeUnits(testUnits);
				data.printData();
				JFreeChart chart = data.getXYChart();
				btnClearData.setEnabled(true);
				Platform.runLater(new Runnable() { 
		            @Override
		            public void run() {
		                initFX(fxPanel, chart);
		            }
				});
			}
		});
		
		//Displays Force Indentation chart (processed data)
		chartPanel = new ChartPanel((JFreeChart) null);
		chartPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		chartPanel.setBackground(Color.WHITE);
		frmAfmanalytics.getContentPane().add(chartPanel, "cell 7 0 5 11,grow");
		
		//Runs the  core analysis
		JButton btnRun = new JButton("Run");
		btnRun.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		btnRun.setEnabled(false);
		frmAfmanalytics.getContentPane().add(btnRun, "cell 7 11,growx");
		btnRun.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				boolean isReady = inputsChecked();
				if(isReady)
				{
					double[] inputs = getInputs();
					//sets up the analyst
					RunAnalysis analyst = new RunAnalysis(data, log, inputs, limitZ);
					//get the force indentation chart and set the chart panel
					JFreeChart forceInd = analyst.run();
					chartPanel.setChart(forceInd);
					//Display results
					updateResults(analyst.getResults());
				}
				if(!isReady)
				{
					infoBox("All variables must be numeric", "ERROR");
				}
			}
		});
		
		//Clears the Force-Distance Chart //TODO Fix this
		JButton btnClearChart = new JButton("Clear Chart");
		btnClearChart.setEnabled(false);
		btnClearChart.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(btnClearChart, "cell 8 11 2 1");
		btnClearChart.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				chartPanel.removeAll();
			}
		});
		
		//Clears the log
		JButton btnClearLog = new JButton("Clear Log");
		btnClearLog.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(btnClearLog, "cell 10 11 2 1,alignx right");
		btnClearLog.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				log.setText("");
			}
		});
		
		JLabel lblSensitivityFactor = new JLabel("Sensitivity Factor");
		lblSensitivityFactor.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(lblSensitivityFactor, "cell 1 12,alignx right");
		
		//Formatted field associated with sensFactor
		sensFactorField = new JFormattedTextField(NumberFormat.getNumberInstance());
		sensFactorField.setValue(new Double(0));
		sensFactorField.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(sensFactorField, "cell 2 12,growx");
		sensFactorField.setColumns(5);
		//Update sensFactor if field is changed
		sensFactorField.getDocument().addDocumentListener(new DocumentListener() { //http://stackoverflow.com/questions/3953208/value-change-listener-to-jtextfield
			public void changedUpdate(DocumentEvent e) {
				updateVal();
			}
			public void removeUpdate(DocumentEvent e) {
				updateVal();
			}
			public void insertUpdate(DocumentEvent e) {
				updateVal();
			}
			
			public void updateVal(){
				boolean isNum = FileParser.isDouble(sensFactorField.getText());
				if(isNum)
				{
					sensFactor = ((Number)sensFactorField.getValue()).doubleValue();
				}
			}
		});
		
		JLabel lblnmv = new JLabel("(nm/V)");
		lblnmv.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 16));
		frmAfmanalytics.getContentPane().add(lblnmv, "cell 3 12,alignx left");
		
		//Do we want to limit how much of the data we use? Defaults to 20%
		JCheckBox chckbxIndentThreshold = new JCheckBox("Limit Indent %");
		chckbxIndentThreshold.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(chckbxIndentThreshold, "cell 4 12 3 1");
		chckbxIndentThreshold.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				//If we do want to limit it, enable relevant fields
				if(e.getStateChange() == ItemEvent.SELECTED)
				{
					limitZ = true;
					indentLimit.setEditable(true);
					gelThicknessField.setEditable(true);
				}
				if(e.getStateChange() == ItemEvent.DESELECTED)
				{
					limitZ = false;
					indentLimit.setEditable(false);
					gelThicknessField.setEditable(false);
				}
			}
		});
		
		//Formatted field associated with zLimit
		indentLimit = new JFormattedTextField(NumberFormat.getNumberInstance());
		indentLimit.setValue(new Double(20));
		zLimit = 20d;
		indentLimit.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(indentLimit, "cell 7 12,growx");
		indentLimit.setEditable(false);
		indentLimit.setColumns(10);
		//We need to update zLimit if indentLimit is changed
		indentLimit.getDocument().addDocumentListener(new DocumentListener() { //http://stackoverflow.com/questions/3953208/value-change-listener-to-jtextfield
			public void changedUpdate(DocumentEvent e) {
				updateVal();
			}
			public void removeUpdate(DocumentEvent e) {
				updateVal();
			}
			public void insertUpdate(DocumentEvent e) {
				updateVal();
			}
			
			public void updateVal(){
				boolean isNum = FileParser.isDouble(indentLimit.getText());
				if(isNum)
				{
					zLimit = ((Number)indentLimit.getValue()).doubleValue();
				}
			}
		});
		
		JLabel lblSpringConstant = new JLabel("Spring Constant");
		lblSpringConstant.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(lblSpringConstant, "cell 1 13,alignx right");
		
		//Formatted field associated with sprConst
		sprConstField = new JFormattedTextField(NumberFormat.getNumberInstance());
		sprConstField.setValue(new Double(0));
		sprConstField.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(sprConstField, "cell 2 13,growx");
		sprConstField.setColumns(5);
		//If it updates, we need to change sprConst
		sprConstField.getDocument().addDocumentListener(new DocumentListener() { //http://stackoverflow.com/questions/3953208/value-change-listener-to-jtextfield
			public void changedUpdate(DocumentEvent e) {
				updateVal();
			}
			public void removeUpdate(DocumentEvent e) {
				updateVal();
			}
			public void insertUpdate(DocumentEvent e) {
				updateVal();
			}
			
			public void updateVal(){
				boolean isNum = FileParser.isDouble(sprConstField.getText());
				if(isNum)
				{
					sprConst = ((Number)sprConstField.getValue()).doubleValue();
				}
			}
		});
		
		JLabel lblnm = new JLabel("(N/m)");
		lblnm.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 16));
		frmAfmanalytics.getContentPane().add(lblnm, "cell 3 13,alignx left");
		
		//Results field for slope.
		txtSlope = new JTextField();
		txtSlope.setBackground(UIManager.getColor("Button.background"));
		txtSlope.setEditable(false);
		txtSlope.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		txtSlope.setBorder(BorderFactory.createEmptyBorder());
		txtSlope.setText("Slope: ");
		frmAfmanalytics.getContentPane().add(txtSlope, "cell 4 13 2 1,growx");
		txtSlope.setColumns(10);
		
		//Results field for exponent.
		txtExp = new JTextField();
		txtExp.setBackground(UIManager.getColor("Button.background"));
		txtExp.setEditable(false);
		txtExp.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		txtExp.setText("Exp:");
		txtExp.setBorder(BorderFactory.createEmptyBorder());
		frmAfmanalytics.getContentPane().add(txtExp, "cell 6 13 2 1,growx");
		txtExp.setColumns(10);
		
		JLabel lblAlpha = new JLabel("Alpha");
		lblAlpha.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(lblAlpha, "cell 1 14,alignx right");
		
		//Formatted field associated with alpha
		alphaField = new JFormattedTextField(NumberFormat.getNumberInstance());
		alphaField.setValue(new Double(0));
		alphaField.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(alphaField, "cell 2 14,growx");
		alphaField.setColumns(10);
		//We want to update alpha if field is changed
		alphaField.getDocument().addDocumentListener(new DocumentListener() { //http://stackoverflow.com/questions/3953208/value-change-listener-to-jtextfield
			public void changedUpdate(DocumentEvent e) {
				updateVal();
			}
			public void removeUpdate(DocumentEvent e) {
				updateVal();
			}
			public void insertUpdate(DocumentEvent e) {
				updateVal();
			}
			
			public void updateVal(){
				boolean isNum = FileParser.isDouble(alphaField.getText());
				if(isNum)
				{
					alpha = ((Number)alphaField.getValue()).doubleValue();
				}
			}
		});
		
		JLabel lbldeg = new JLabel("(deg)");
		lbldeg.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 16));
		frmAfmanalytics.getContentPane().add(lbldeg, "cell 3 14,alignx left");
		
		//Results field for R-Squared value
		txtRsquared = new JTextField();
		txtRsquared.setBackground(UIManager.getColor("Button.background"));
		txtRsquared.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		txtRsquared.setEditable(false);
		txtRsquared.setText("R-Squared:");
		txtRsquared.setBorder(BorderFactory.createEmptyBorder());
		frmAfmanalytics.getContentPane().add(txtRsquared, "cell 4 14 4 1,growx");
		txtRsquared.setColumns(10);
		
		//Do we want to automatically run without Z0 input? No. Not yet.
		chckbxSelectZ = new JCheckBox("   Select Z0");
		chckbxSelectZ.setEnabled(false);
		chckbxSelectZ.setSelected(true);
		chckbxSelectZ.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(chckbxSelectZ, "cell 1 15,alignx right");
		
		//Formatted field associated with ImpactZ
		impactZField.setValue(new Double(0));
		impactZField.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(impactZField, "cell 2 15,growx");
		impactZField.setColumns(10);
		//If this field changes, we want to know
		impactZField.getDocument().addDocumentListener(new DocumentListener() { //http://stackoverflow.com/questions/3953208/value-change-listener-to-jtextfield
			public void changedUpdate(DocumentEvent e) {
				updateVal();
			}
			public void removeUpdate(DocumentEvent e) {
				updateVal();
			}
			public void insertUpdate(DocumentEvent e) {
				updateVal();
			}
			
			public void updateVal(){
				boolean isNum = FileParser.isDouble(impactZField.getText());
				if(isNum)
				{
					//If it was update by the textbox
					if(!zUpdatedFX)
					{
						double tempVal = ((Number)impactZField.getValue()).doubleValue();
						updateClickedZ(tempVal);
						impactZ = tempVal;
					}
					//If it was updated by the crosshairs
					if(zUpdatedFX)
					{
						impactZ = clickedZ;
						zChanged(false);
					}
				}
			}
		
		});

		JLabel lblnm_1 = new JLabel("(nm)");
		lblnm_1.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 16));
		frmAfmanalytics.getContentPane().add(lblnm_1, "cell 3 15,alignx left");
		
		//Result field for Young's Modulus
		txtYoungsModulus = new JTextField();
		txtYoungsModulus.setBackground(UIManager.getColor("Button.background"));
		txtYoungsModulus.setEditable(false);
		txtYoungsModulus.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		txtYoungsModulus.setText("Young's Modulus: ");
		txtYoungsModulus.setBorder(BorderFactory.createEmptyBorder());
		frmAfmanalytics.getContentPane().add(txtYoungsModulus, "cell 4 15 4 1,grow");
		txtYoungsModulus.setColumns(10);
		
		//displays the directory of the file
		JTextField dirPane = new JTextField();
		dirPane.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		dirPane.setEditable(false);
		dirPane.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		frmAfmanalytics.getContentPane().add(dirPane, "cell 1 16 9 1,growx,aligny center");
		
		//Browses to and reads in a selected file
		JButton btnBrowse = new JButton("  Browse  ");
		btnBrowse.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(btnBrowse, "cell 10 16 2 1,alignx right");
		btnBrowse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				//FileFilter filter = new FileNameExtensionFilter("TXT files", "txt"); //Not all files are .txt
				//chooser.setFileFilter(filter);
				//chooser.setCurrentDirectory("<YOUR DIR COMES HERE>"); //TODO
				int returnVal = chooser.showOpenDialog(chooser);
				//if it's a valid file, we'll load it in
				if(returnVal == JFileChooser.APPROVE_OPTION) {
				   data = new CurveData(log);
				   log.append("File approved");
				   dataFile = chooser.getSelectedFile();
				   File dataDir = chooser.getCurrentDirectory();
				   dirPane.setText(dataDir.toString()+dataFile.toString());
				   FileParser fP = new FileParser(log, data);
				   //see if we can parse out some data
				   try {
					dataUpload = fP.readFile(dataFile);
					data = fP.getData();
				   } catch (IOException e){
					e.printStackTrace();
					AfmDisplay.infoBox("Error parsing file.", "ERROR");
				   }
				   log.append("File has been processed successfully." + "\n");
				   //Now that we have data, we can view or run it
				   btnView.setEnabled(true);
				   btnRun.setEnabled(true);
				}
			}
		});
		
	}
	
	/**
	 * Check if the inputs are numbers. Defunct. To be removed
	 * @return if four of core fields are numbers
	 */
	private boolean inputsChecked() //TODO A bit defunct, change accordingly
	{
		boolean sff = FileParser.isDouble(sensFactorField.getText());
		boolean scf = FileParser.isDouble(sprConstField.getText());
		boolean af = FileParser.isDouble(alphaField.getText());
		boolean izf = FileParser.isDouble(impactZField.getText());
		return(sff && scf & af & izf);
	}
	
	/**
	 * Updates the value of clickedZ
	 * @param newVal The value that clickedZ will be updated to
	 */
	private static void updateClickedZ(double newVal)
	{
		clickedZ = newVal;
	}
	
	/**
	 * Puts the inputs in a double array {sensFactor, sprConst, alpha, impactZ, zLimit, gelThickness}
	 * @return The inputs in a double array
	 */
	private double[] getInputs()
	{
		double[] result = {sensFactor, sprConst, alpha, impactZ, zLimit, gelThickness};
		return result;
	}
	
	/**
	 * Updates the values on the GUI to display the results
	 * @param results Results to be displayed
	 */
	private void updateResults(double[] results)
    {	
    	txtSlope.setText(String.format("Slope: %,.1f", results[0]));
    	txtExp.setText(String.format("Exp: %.3f", results[1]));
    	txtRsquared.setText(String.format("R-Squared: %.3f", results[2]));
    	txtYoungsModulus.setText(String.format("Young's Modulus: %,.3f"+"kPa", results[3]));
    }
	
	/**
	 * Updates JavaFX panel with empty Scene
	 * @param fxPanel Panel to be updated
	 */
	private static void initFX(JFXPanel fxPanel) {
        // This method is invoked on the JavaFX thread
        Scene scene = createScene();
        fxPanel.setScene(scene);
    }
	
	/**
	 * Updates JavaFX panel with chart
	 * @param fxPanel Panel to be updated
	 * @param inChart Chart to be displayed
	 */
	private static void initFX(JFXPanel fxPanel, JFreeChart inChart) {
        // This method is invoked on the JavaFX thread
        Scene scene = createScene(inChart);
        fxPanel.setScene(scene);
    }
	
	/**
	 * Constructs a blank Scene
	 * @return Scene to be displayed
	 */
    private static Scene createScene() {
        Scene  scene  =  new  Scene(new ChartDisplay(), FXWIDTH, FXHEIGHT);
        return (scene);
    }
    
    /**
     * Constructs a Scene given a Chart
     * @param inChart Chart to be displayed
     * @return Scene to be displayed
     */
    private static Scene createScene(JFreeChart inChart){
        Scene  scene  =  new  Scene(new ChartDisplay(inChart), FXWIDTH, FXHEIGHT);
        return (scene);
    }
    
    /**
     * Updates zUpdatedFX if the JavaFX thread updated the Z-location (from crosshairs)
     * @param yesno Whether or not the value has been updated
     */
    private static void zChanged(boolean yesno){
    	zUpdatedFX = yesno;
    }
    
    /**
     * 
     * Adapted from: JFreeChart CrosshairOverlayFXDemo1
     *
     */
    static class ChartDisplay extends StackPane implements ChartMouseListenerFX {
        
        private ChartViewer chartViewer;
        private Crosshair xCrosshair;
        private Crosshair yCrosshair;
        private JFreeChart chart;
    
        public ChartDisplay() {
        	//Displays basic white box
        }
        
        /**
         * Displays a chart in the FXPanel with crosshair overlays
         * @param inChart The Chart to be displayed
         */
        public ChartDisplay(JFreeChart inChart) {
        	chart = inChart;
            this.chartViewer = new ChartViewer(chart);
            this.chartViewer.addChartMouseListener(this);
            getChildren().add(this.chartViewer);
           
            CrosshairOverlayFX crosshairOverlay = new CrosshairOverlayFX();
            this.xCrosshair = new Crosshair(Double.NaN, Color.BLACK, 
                    new BasicStroke(0f));
            this.xCrosshair.setStroke(new BasicStroke(1.5f, 
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, 
                    new float[]{2.0f, 2.0f}, 0));
            this.xCrosshair.setLabelVisible(false);
            this.yCrosshair = new Crosshair(Double.NaN, Color.BLACK, 
                    new BasicStroke(0f));
            this.yCrosshair.setStroke(new BasicStroke(1.5f, 
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, 
                    new float[] {2.0f, 2.0f}, 0));
            this.yCrosshair.setLabelVisible(false);
            
            crosshairOverlay.addDomainCrosshair(xCrosshair);
            crosshairOverlay.addRangeCrosshair(yCrosshair);
            
            Platform.runLater(() -> {
                this.chartViewer.getCanvas().addOverlay(crosshairOverlay);
            });
        }

        /*
         *  If a user uses the crosshairs to find a location and selects it, returns the x value
         */
        @Override
        public void chartMouseClicked(ChartMouseEventFX event) {
        	double xValue = this.xCrosshair.getValue();
        	SwingUtilities.invokeLater(new Runnable() {
        	    @Override
        	    public void run() {
        	    	updateClickedZ(xValue);
        	    	zChanged(true);
        	    	impactZField.setText(String.format("%,.3f", xValue));
        	    }
        	});
        }

        /*
         * Tracks user mouse movement and moves crosshairs appropriately
         */
        @Override
        public void chartMouseMoved(ChartMouseEventFX event) {
            Rectangle2D dataArea = this.chartViewer.getCanvas().getRenderingInfo().getPlotInfo().getDataArea();
            JFreeChart chart = event.getChart();
            XYPlot plot = (XYPlot) chart.getPlot();
            ValueAxis xAxis = plot.getDomainAxis();
            double x = xAxis.java2DToValue(event.getTrigger().getX(), dataArea, 
                    RectangleEdge.BOTTOM);
            // make the crosshairs disappear if the mouse is out of range
            if (!xAxis.getRange().contains(x)) { 
                x = Double.NaN;                  
            }
            double y = DatasetUtilities.findYValue(plot.getDataset(), 0, x);
            this.xCrosshair.setValue(x);
            this.yCrosshair.setValue(y);
        }
        
    }
    
    /**
     * Converts Color to Paint
     * @param c The Color to you want to Paint
     * @return the Paint with your color
     */
    public static Paint toPaint(Color c) //http://stackoverflow.com/questions/30466405/java-convert-java-awt-color-to-javafx-scene-paint-color
    {
    	int r = c.getRed();
    	int g = c.getGreen();
    	int b = c.getBlue();
    	int a = c.getAlpha();
    	double opacity = a / 255.0 ;
    	return javafx.scene.paint.Color.rgb(r, g, b, opacity);
    }
    
    /**
     * Notify the user of information with popup
     * @param infoMessage Message to be displayed
     * @param titleBar The title of the popup
     */
    public static void infoBox(String infoMessage, String titleBar) //http://stackoverflow.com/questions/7080205/popup-message-boxes
    {
        JOptionPane.showMessageDialog(null, infoMessage, "Notice: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
}
