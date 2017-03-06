package ln.afm.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
import ln.afm.manager.Manager;
import ln.afm.manager.RunAnalysis;
import ln.afm.model.CurveData;
import ln.afm.model.FileParser;
import net.miginfocom.swing.MigLayout;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JMenuBar;
import javax.swing.JSeparator;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.SystemColor;
import javax.swing.SwingConstants;
import javax.swing.JSplitPane;
import javax.swing.JList;
import javax.swing.ScrollPaneConstants;

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
	private static JFormattedTextField impactZField = new JFormattedTextField(NumberFormat.getNumberInstance());
	private JFormattedTextField gelThicknessField;
	private static double clickedZ = 0;
	
	private double sensFactor;
	private double sprConst;
	private double alpha;
	private double impactZ;
	private double zLimit;
	private double gelThickness;
	
	//private File[] dataFiles;
	private File dataDirectory;
	private boolean directorySet = false;
	private boolean goodFit = true;
	private Manager manager = new Manager();
	private DefaultListModel<String> listModel;
	JList<String> fileList;
	
	private static boolean zUpdatedFX;
	private boolean limitZ = false;
	private JFormattedTextField indentLimit;
	private JTextField txtSlope;
	private JTextField txtRsquared;
	private JTextField txtYoungsModulus;
	private JTextField txtExp;
	private JCheckBox chckbxSelectZ;
	JCheckBox chckbxIndentThreshold;
	
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
		frmAfmanalytics.getContentPane().setLayout(new MigLayout("", "[140.00px:137.00px,grow][95.00px:95.00px,grow][49.00px:49.00px][][72.00px:84.00px][30px:50.00px][30px:30px][72.00px:72.00px][72.00px:72.00px,grow][35px:35.00px][30px:30px][30px:30px]", "[28px:28px][28px:28px][28px:28px][28px:28px][28px:28px][28px:28px][28px:28px][28:28][28px:28px][15px:15.00px][28px:28px,grow][28px:28px,grow][28px:28px][28px:28px][28px:28px][28px:28px]"));
		//frmAfmanalytics.getContentPane().add(fxPanel, "flowy,cell 0 0 7 10,grow");
		//FXPanels have to be on a separate thread. Initializing it now.
		
		
		//Panel to display our main chart
		JFXPanel fxPanel = new JFXPanel(); //https://docs.oracle.com/javase/8/javafx/interoperability-tutorial/swing-fx-interoperability.htm
		fxPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		fxPanel.setBackground(Color.WHITE);
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX(fxPanel);
            }
		});
		
		//Displays Force Indentation chart (processed data)
		chartPanel = new ChartPanel((JFreeChart) null);
		chartPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		chartPanel.setBackground(Color.WHITE);
		//frmAfmanalytics.getContentPane().add(chartPanel, "cell 7 0 5 10,grow");
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fxPanel, chartPanel);
		frmAfmanalytics.getContentPane().add(splitPane, "cell 0 0 12 10,grow");
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(400);
		
		//Clears the voltage-distance chart (raw data)
		JButton btnClearData = new JButton("Clear Data");
		btnClearData.setEnabled(false);
		btnClearData.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(btnClearData, "cell 0 10,growx");
		btnClearData.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				//log.append("Clearing data..." + "\n");
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
		frmAfmanalytics.getContentPane().add(btnView, "cell 1 10,growx");
		btnView.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				manager.setCurrentFile(fileList.getSelectedIndex());
				JFreeChart chart = manager.viewRawGraph();
				chartPanel.setChart(new JFreeChart(new XYPlot()));
				if(manager.hasRun())
				{
					updateResults(manager.getResults());
					chartPanel.setChart(manager.getPreviousRun());
					updateParameters(manager.getPreviousParameters());
				}
				btnClearData.setEnabled(true);
				Platform.runLater(new Runnable() { 
		            @Override
		            public void run() {
		                initFX(fxPanel, chart);
		            }
				});
			}
		});
		
		//Runs the  core analysis
		JButton btnRun = new JButton("Run");
		btnRun.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		btnRun.setEnabled(false);
		frmAfmanalytics.getContentPane().add(btnRun, "cell 2 10 3 1,growx");
		btnRun.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				boolean isReady = inputsChecked();
				if(isReady)
				{
					double[] inputs = getInputs();
					manager.setParameters(inputs, limitZ);
					chartPanel.setChart(manager.run(goodFit));
					updateResults(manager.getResults());
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
		frmAfmanalytics.getContentPane().add(btnClearChart, "cell 6 10 2 1");
		btnClearChart.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				chartPanel.removeAll();
			}
		});
		
		//Clears the log
		JButton btnClearFiles = new JButton("Clear Files");
		btnClearFiles.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(btnClearFiles, "cell 8 10 2 1,alignx right");
		btnClearFiles.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {				
				int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to clear all files?", "Notice", JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.OK_OPTION) {
					listModel.clear();
					manager.clearAllFiles();
				}
			}
		});
		
		listModel = new DefaultListModel<String>();
		fileList = new JList<String>(listModel);
		fileList.setVisibleRowCount(10);
		fileList.setFont(new Font("Tahoma", Font.PLAIN, 16));
		fileList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
		        if (e.getValueIsAdjusting() == false) {

		            if (fileList.getSelectedIndex() == -1) {
		            //No selection, disable fire button.
		            	btnView.setEnabled(false);
		            	btnRun.setEnabled(false);
		            } 
		            else if(fileList.getSelectedIndices().length > 1)
		            {
		            	btnView.setEnabled(false);
		            	btnRun.setEnabled(false);
		            }
		            else {
		            //Selection, enable the fire button.
		            	btnView.setEnabled(true);
		            	btnRun.setEnabled(true);
		            }
		        }
			}
			
		});
		
		JScrollPane scrollPane = new JScrollPane(fileList);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		frmAfmanalytics.getContentPane().add(scrollPane, "cell 8 11 4 5,grow");
		
		//Browses to and reads in a selected file
		JButton btnBrowse = new JButton("  Browse  ");
		btnBrowse.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(btnBrowse, "cell 10 10 2 1,alignx right");
		btnBrowse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(true);
				//FileFilter filter = new FileNameExtensionFilter("TXT files", "txt"); //Not all files are .txt
				//chooser.setFileFilter(filter);
				if(directorySet)
				{
					chooser.setCurrentDirectory(dataDirectory);
				}
				int returnVal = chooser.showOpenDialog(chooser);
				//if it's a valid file, we'll load it in
				if(returnVal == JFileChooser.APPROVE_OPTION) {
				   System.out.println("File approved");
				   File[] dataFiles = chooser.getSelectedFiles();
				   File dataDir = chooser.getCurrentDirectory();
				   File[] tempDataFiles = manager.parseFiles(dataFiles);
				   if(tempDataFiles.length > 0)
				   {
					   System.out.println("Updating the list of files");
					   updateListModel(tempDataFiles);
					   dataDirectory = dataDir;
					   directorySet = true;
					   //Now that we have data, we can view or run it
					   btnView.setEnabled(false);
					   btnRun.setEnabled(false);
				   }
				}
			}
		});
		
		ButtonGroup fitRadButtons = new ButtonGroup();
		zLimit = 20d;
		
		JLabel lblSensitivityFactor = new JLabel("Sensitivity Factor");
		lblSensitivityFactor.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(lblSensitivityFactor, "cell 0 12,alignx right");
		
		//Formatted field associated with sensFactor
		sensFactorField = new JFormattedTextField(NumberFormat.getNumberInstance());
		sensFactorField.setValue(new Double(0));
		sensFactorField.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(sensFactorField, "cell 1 12,growx");
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
		frmAfmanalytics.getContentPane().add(lblnmv, "cell 2 12,alignx left");
		
		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		frmAfmanalytics.getContentPane().add(separator, "cell 3 12 1 4");
		
		JRadioButton rdbtnForceFit = new JRadioButton("Force Fit");
		rdbtnForceFit.setFont(new Font("Tahoma", Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(rdbtnForceFit, "cell 4 12 2 1");
		rdbtnForceFit.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED)
				{
					goodFit = false;
				}
				if(e.getStateChange() == ItemEvent.DESELECTED)
				{
					goodFit = true;
				}
			}
		});
		fitRadButtons.add(rdbtnForceFit);
		
		JRadioButton rdbtnGoodFit = new JRadioButton("Good Fit");
		rdbtnGoodFit.setFont(new Font("Tahoma", Font.PLAIN, 18));
		rdbtnGoodFit.setSelected(true);
		frmAfmanalytics.getContentPane().add(rdbtnGoodFit, "cell 6 12 2 1");
		fitRadButtons.add(rdbtnGoodFit);
		
		//Do we want to limit how much of the data we use? Defaults to 20%
		chckbxIndentThreshold = new JCheckBox("Limit Indent");
		chckbxIndentThreshold.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(chckbxIndentThreshold, "cell 0 13");
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
		indentLimit.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(indentLimit, "cell 1 13");
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
		
		JLabel label = new JLabel("(%)");
		label.setFont(new Font("Tahoma", Font.PLAIN, 16));
		frmAfmanalytics.getContentPane().add(label, "cell 2 13,alignx left");
		
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
		
		JLabel lblGelThickness = new JLabel("Gel Thickness");
		lblGelThickness.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(lblGelThickness, "cell 0 14,alignx trailing");
		
		//Formatted field associated with gelThickness
		gelThicknessField = new JFormattedTextField(NumberFormat.getNumberInstance());
		gelThicknessField.setEditable(false);
		gelThicknessField.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		gelThicknessField.setValue(new Double(0));
		frmAfmanalytics.getContentPane().add(gelThicknessField, "cell 1 14,growx");
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
				boolean isNum = FileParser.isDouble(gelThicknessField.getText());
				if(isNum)
				{
					gelThickness = ((Number)gelThicknessField.getValue()).doubleValue();
				}
			}
		});
		
		JLabel lblnm2 = new JLabel("(nm)");
		lblnm2.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 16));
		frmAfmanalytics.getContentPane().add(lblnm2, "cell 2 14,alignx left");
				
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
		frmAfmanalytics.getContentPane().add(chckbxSelectZ, "cell 0 15,alignx right");
		
		//Formatted field associated with ImpactZ
		impactZField.setValue(new Double(0));
		impactZField.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		frmAfmanalytics.getContentPane().add(impactZField, "cell 1 15,growx");
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
		frmAfmanalytics.getContentPane().add(lblnm_1, "cell 2 15,alignx left");
		
		//Result field for Young's Modulus
		txtYoungsModulus = new JTextField();
		txtYoungsModulus.setBackground(UIManager.getColor("Button.background"));
		txtYoungsModulus.setEditable(false);
		txtYoungsModulus.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
		txtYoungsModulus.setText("Young's Modulus: ");
		txtYoungsModulus.setBorder(BorderFactory.createEmptyBorder());
		frmAfmanalytics.getContentPane().add(txtYoungsModulus, "cell 4 15 4 1,grow");
		txtYoungsModulus.setColumns(10);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBorderPainted(false);
		menuBar.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		menuBar.setMaximumSize(new Dimension(50, 15));
		frmAfmanalytics.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		mnFile.setHorizontalAlignment(SwingConstants.LEFT);
		mnFile.setBackground(SystemColor.menu);
		mnFile.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		menuBar.add(mnFile);
		
		JMenuItem mntmExport = new JMenuItem("Export File(s)");
		mntmExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				JPanel exportPanel = new JPanel();
				exportPanel.setLayout(new BoxLayout(exportPanel, BoxLayout.Y_AXIS));
				
				JList<String> exportList = new JList<String>(listModel);
				exportList.setModel(listModel);
				exportList.setVisibleRowCount(10);
				exportList.setFont(new Font("Tahoma", Font.PLAIN, 16));
				
				JScrollPane exportScroll = new JScrollPane(exportList);
				exportScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				exportScroll.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

				JLabel info = new JLabel("Please select the files to export and where to export them.");
				info.setHorizontalAlignment(SwingConstants.CENTER);
				exportPanel.add(info);
				exportPanel.add(Box.createVerticalStrut(15));
				exportPanel.add(exportScroll);
				exportPanel.add(Box.createVerticalStrut(15));
				
				JPanel locPanel = new JPanel();
				locPanel.setLayout(new BoxLayout(locPanel, BoxLayout.LINE_AXIS));
				locPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
				JTextField dirLocation = new JTextField(15);
				dirLocation.setEnabled(true);
				dirLocation.setEditable(false);
				dirLocation.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
				locPanel.add(dirLocation);
				locPanel.add(Box.createHorizontalStrut(2));
				JButton btnExpBrowse = new JButton("Browse");
				btnBrowse.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));
				locPanel.add(btnExpBrowse);
				
				JPanel radBtnPanel = new JPanel();
				radBtnPanel.setLayout(new GridBagLayout());
				ButtonGroup radFileExt = new ButtonGroup();
				
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = GridBagConstraints.RELATIVE;
				gbc.gridy = 0;
				gbc.ipadx = 10;
				gbc.anchor = GridBagConstraints.WEST;
				
				JRadioButton rdbtnJPEG = new JRadioButton(".jpg");
				rdbtnJPEG.setFont(new Font("Tahoma", Font.PLAIN, 18));
				radFileExt.add(rdbtnJPEG);
				radBtnPanel.add(rdbtnJPEG, gbc);
				
				radBtnPanel.add(Box.createHorizontalStrut(20));
				
				JRadioButton rdbtnPNG = new JRadioButton(".png");
				rdbtnPNG.setFont(new Font("Tahoma", Font.PLAIN, 18));
				rdbtnPNG.setSelected(true);
				radFileExt.add(rdbtnPNG);
				radBtnPanel.add(rdbtnPNG, gbc);
				
				
				exportPanel.add(locPanel);
				exportPanel.add(radBtnPanel);
				btnExpBrowse.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent arg0) {
						JFileChooser chooser = new JFileChooser();
						chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						if(directorySet)
						{
							chooser.setCurrentDirectory(dataDirectory);
						}
						int returnVal = chooser.showOpenDialog(chooser);
						//if it's a valid file, we'll load it in
						if(returnVal == JFileChooser.APPROVE_OPTION) {
							dirLocation.setText(chooser.getSelectedFile().toString());
						}
					}
				});
				
				
				int result = JOptionPane.showConfirmDialog(null, exportPanel, 
						"Export File(s)", JOptionPane.OK_CANCEL_OPTION);
				if (result == JOptionPane.OK_OPTION) {
					int[] exportFiles = exportList.getSelectedIndices();
					if(exportFiles.length > 0)
					{
						System.out.println("Length is: " + exportFiles.length);
						for(int i = 0;i < exportFiles.length; i++)
						{
							System.out.println("Selected: " + exportFiles[i]);
						}
						if(Files.isDirectory(Paths.get(dirLocation.getText())))
						{
							int value = Manager.PNG;
							if(rdbtnJPEG.isSelected())
							{
								value = Manager.JPEG;
							}
							manager.export(exportFiles, dirLocation.getText(), value);
						}
					}
				}
			}
		});
		mntmExport.setHorizontalAlignment(SwingConstants.LEFT);
		mntmExport.setFont(new Font("Segoe UI", Font.PLAIN, 20));
		mnFile.add(mntmExport);
		
		JMenu mnEdit = new JMenu("Edit");
		mnEdit.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		menuBar.add(mnEdit);
		
		JMenuItem mntmPreferences = new JMenuItem("Preferences");
		mntmPreferences.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				double[] tempValues = manager.getConfigValues();
				
				JTextField poissonField = new JTextField(5);
				JTextField alphaField = new JTextField(5);
				JTextField springField = new JTextField(5);
				JTextField sensitivityField = new JTextField(5);
				
				poissonField.setText(String.valueOf(tempValues[0]));
				alphaField.setText(String.valueOf(tempValues[1]));
				springField.setText(String.valueOf(tempValues[2]));
				sensitivityField.setText(String.valueOf(tempValues[3]));
				
				JPanel paramsPanel = new JPanel();
				paramsPanel.setLayout(new BoxLayout(paramsPanel, BoxLayout.Y_AXIS));
					
				JPanel paramsPanel1 = new JPanel();
				paramsPanel1.setLayout(new BoxLayout(paramsPanel1, BoxLayout.LINE_AXIS));
				paramsPanel1.add(Box.createHorizontalStrut(15));
				paramsPanel1.add(new JLabel("Poisson Ratio:"));
				paramsPanel1.add(Box.createHorizontalStrut(34));
				paramsPanel1.add(poissonField);
				
				JPanel paramsPanel2 = new JPanel();
				paramsPanel2.setLayout(new BoxLayout(paramsPanel2, BoxLayout.LINE_AXIS));
				paramsPanel2.add(Box.createHorizontalStrut(15)); // a spacer
				paramsPanel2.add(new JLabel("Alpha:"));
				paramsPanel2.add(Box.createHorizontalStrut(80));
				paramsPanel2.add(alphaField);
				
				JPanel paramsPanel3 = new JPanel();
				paramsPanel3.setLayout(new BoxLayout(paramsPanel3, BoxLayout.LINE_AXIS));
				paramsPanel3.add(Box.createHorizontalStrut(15)); // a spacer
				paramsPanel3.add(new JLabel("Spring Constant:"));
				paramsPanel3.add(Box.createHorizontalStrut(21));
				paramsPanel3.add(springField);
				
				JPanel paramsPanel4 = new JPanel();
				paramsPanel4.setLayout(new BoxLayout(paramsPanel4, BoxLayout.LINE_AXIS));
				paramsPanel4.add(Box.createHorizontalStrut(15)); // a spacer
				paramsPanel4.add(new JLabel("Sensitivity Factor:"));
				paramsPanel4.add(Box.createHorizontalStrut(15));
				paramsPanel4.add(sensitivityField);
				
				paramsPanel.add(paramsPanel1);
				paramsPanel.add(paramsPanel2);
				paramsPanel.add(paramsPanel3);
				paramsPanel.add(paramsPanel4);
				
				int result = JOptionPane.showConfirmDialog(null, paramsPanel, 
						"Parameters", JOptionPane.OK_CANCEL_OPTION);
				if (result == JOptionPane.OK_OPTION) {
						System.out.println("Poisson: " + poissonField.getText());
						System.out.println("Alpha: " + alphaField.getText());
						System.out.println("Spring: " + springField.getText());
						System.out.println("Sensitivity: " + sensitivityField.getText());
						String[] configs = {poissonField.getText(), alphaField.getText(), springField.getText(), sensitivityField.getText()};
						manager.setConfigValues(configs);
				}
			}
		});
		mntmPreferences.setHorizontalAlignment(SwingConstants.LEFT);
		mntmPreferences.setFont(new Font("Segoe UI", Font.PLAIN, 20));
		mnEdit.add(mntmPreferences);
		
		JMenu mnView = new JMenu("View");
		mnView.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		mnView.setEnabled(false);
		menuBar.add(mnView);
		
		JMenuItem mntmLog = new JMenuItem("Log");
		mntmLog.setFont(new Font("Segoe UI", Font.PLAIN, 20));
		mnView.add(mntmLog);
		
		JMenu mnHelp = new JMenu("Help");
		mnHelp.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		mnHelp.setEnabled(false);
		menuBar.add(mnHelp);
		
	}
	
	protected void updateParameters(double[] previousParameters) {
		gelThicknessField.setValue(previousParameters[0]);
		limitZ = (previousParameters[3] == 1.0);
		indentLimit.setValue(previousParameters[2]);
		impactZField.setValue(previousParameters[1]);
		if(previousParameters[3] == 1.0)
		{
			chckbxIndentThreshold.setEnabled(true);
			chckbxIndentThreshold.setSelected(true);
		}
	}

	private void updateListModel(File[] updateFiles) {
		for(int i=0; i < updateFiles.length;i++)
		{			
			String currentFile = updateFiles[i].getName();
			if(!listModel.contains(currentFile))
			{
				listModel.addElement(currentFile);
			}
		}
	}

	/**
	 * Check if the inputs are numbers. Defunct. To be removed
	 * @return if four of core fields are numbers
	 */
	private boolean inputsChecked() //TODO A bit defunct, change accordingly
	{
		boolean sff = FileParser.isDouble(sensFactorField.getText());
		//boolean scf = FileParser.isDouble(sprConstField.getText());
		//boolean af = FileParser.isDouble(alphaField.getText());
		boolean izf = FileParser.isDouble(impactZField.getText());
		return(sff && izf);
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
        //Scene  scene  =  new  Scene(new ChartDisplay(), FXWIDTH, FXHEIGHT);
    	Scene scene = new Scene(new ChartDisplay());
        return (scene);
    }
    
    /**
     * Constructs a Scene given a Chart
     * @param inChart Chart to be displayed
     * @return Scene to be displayed
     */
    private static Scene createScene(JFreeChart inChart){
        //Scene  scene  =  new  Scene(new ChartDisplay(inChart), FXWIDTH, FXHEIGHT);
    	Scene  scene  =  new  Scene(new ChartDisplay(inChart));
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
