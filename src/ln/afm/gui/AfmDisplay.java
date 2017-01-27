package ln.afm.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.fx.overlay.CrosshairOverlayFX;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.miginfocom.swing.MigLayout;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javax.swing.border.SoftBevelBorder;
import javax.swing.UIManager;

public class AfmDisplay{
	private static final Logger LOGGER = Logger.getLogger(AfmDisplay.class.getName() );
	private static double FXWIDTH = 700d;
	private static double FXHEIGHT = 600d;
	
	private JFrame frmAfmanalytics;
	private final Action action = new SwingAction();
	private JTextField sensFactorField;
	private JTextField sprConstField;
	private JTextField alphaField;
	private final static JTextField impactZField = new JTextField();
	private JTextArea log;
	private File dataFile;
	private CurveData data;
	private boolean dataUpload;
	
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
		frmAfmanalytics = new JFrame();
		frmAfmanalytics.getContentPane().setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 22));
		frmAfmanalytics.setResizable(false);
		frmAfmanalytics.setTitle("AFM-Analytics");
		frmAfmanalytics.setBounds(100, 100, 900, 650);
		frmAfmanalytics.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmAfmanalytics.getContentPane().setLayout(new MigLayout("", "[100.00px:100.00px:100.00px][100.00px:100px:100px][100px:100px:100px][100px:100px:100px][100px:100px:100px][100px:100px:100px][100.00px:100px:100px][25px:25px:25px][70px:70px:70px][50px:50px:50px]", "[50px:50px:50px][50px:50px:50px][50px:50px:50px][50px:50px:50px][50px:50px:50px][50px:50px:50px][50px:50px:50px][50px:50px:50px][50px:50px:50px][50px:50px:50px][50px:50px:50px]"));
		
		JFXPanel fxPanel = new JFXPanel(); //https://docs.oracle.com/javase/8/javafx/interoperability-tutorial/swing-fx-interoperability.htm
		fxPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		fxPanel.setBackground(Color.WHITE);
		frmAfmanalytics.getContentPane().add(fxPanel, "flowx,cell 0 0 6 7");
		
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX(fxPanel);
            }
		});
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		frmAfmanalytics.getContentPane().add(scrollPane, "cell 6 0 4 7,grow");
		
		
		//test
		JTextArea log = new JTextArea();
		log.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 18));
		log.setEditable(false);
		log.setForeground(Color.BLACK);
		scrollPane.setViewportView(log);
		
		data = new CurveData(log);
		
		JButton btnClearData = new JButton("Clear");
		btnClearData.setEnabled(false);
		btnClearData.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 24));
		frmAfmanalytics.getContentPane().add(btnClearData, "cell 0 7,growx");
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
		
		JButton btnView = new JButton("View");
		btnView.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 24));
		btnView.setEnabled(false);
		btnView.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				String[] testUnits = {"nm","V"};
				data.changeUnits(testUnits);
				data.printData();
				JFreeChart chart = data.getXYChart();
				Platform.runLater(new Runnable() { 
		            @Override
		            public void run() {
		                initFX(fxPanel, chart);
		            }
				});
			}
		});
		frmAfmanalytics.getContentPane().add(btnView, "cell 1 7,growx");
		
		JButton btnRun = new JButton("Run");
		btnRun.setEnabled(false);
		frmAfmanalytics.getContentPane().add(btnRun, "cell 5 7,growx");
		btnRun.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				String[] inputs = getInputs();
				RunAnalysis(data, log, String[] inputs);
			}
		});
		
		JButton btnClearLog = new JButton("Clear Log");
		btnClearLog.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 24));
		frmAfmanalytics.getContentPane().add(btnClearLog, "cell 7 7 3 1,alignx right");
		btnClearLog.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				log.setText("");
			}
		});
		
		JLabel lblSensitivityFactor = new JLabel("Sensitivity Factor");
		lblSensitivityFactor.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 24));
		frmAfmanalytics.getContentPane().add(lblSensitivityFactor, "cell 0 8 2 1,alignx right");
		
		sensFactorField = new JTextField();
		sensFactorField.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 24));
		frmAfmanalytics.getContentPane().add(sensFactorField, "cell 2 8,growx");
		sensFactorField.setColumns(10);
		
		JLabel lblnmv = new JLabel("(nm/V)");
		lblnmv.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 22));
		frmAfmanalytics.getContentPane().add(lblnmv, "cell 3 8,alignx left");
		
		JLabel lblAlpha = new JLabel("Alpha");
		lblAlpha.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 24));
		frmAfmanalytics.getContentPane().add(lblAlpha, "cell 4 8 2 1,alignx right");
		
		alphaField = new JTextField();
		alphaField.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 24));
		frmAfmanalytics.getContentPane().add(alphaField, "cell 6 8 2 1,growx");
		alphaField.setColumns(10);
		
		JLabel lbldeg = new JLabel("(deg)");
		lbldeg.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 22));
		frmAfmanalytics.getContentPane().add(lbldeg, "cell 8 8");
		
		JLabel lblSpringConstant = new JLabel("Spring Constant");
		lblSpringConstant.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 24));
		frmAfmanalytics.getContentPane().add(lblSpringConstant, "cell 0 9 2 1,alignx right");
		
		sprConstField = new JTextField();
		sprConstField.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 24));
		frmAfmanalytics.getContentPane().add(sprConstField, "cell 2 9,growx");
		sprConstField.setColumns(10);
		
		JLabel lblnm = new JLabel("(N/m)");
		lblnm.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 22));
		frmAfmanalytics.getContentPane().add(lblnm, "cell 3 9");
		
		JLabel lblImpactPointz = new JLabel("Impact Point (Z)");
		lblImpactPointz.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 24));
		frmAfmanalytics.getContentPane().add(lblImpactPointz, "cell 4 9 2 1,alignx right");
		
		impactZField.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 24));
		frmAfmanalytics.getContentPane().add(impactZField, "cell 6 9 2 1,growx");
		impactZField.setColumns(10);
		
		JLabel lblnm_1 = new JLabel("(nm)");
		lblnm_1.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 22));
		frmAfmanalytics.getContentPane().add(lblnm_1, "cell 8 9");
		
		JTextPane dirPane = new JTextPane();
		dirPane.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 24));
		dirPane.setEditable(false);
		dirPane.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		frmAfmanalytics.getContentPane().add(dirPane, "cell 0 10 7 1,growx,aligny center");
		
		JButton btnBrowse = new JButton("Browse");
		btnBrowse.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 24));
		btnBrowse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				FileFilter filter = new FileNameExtensionFilter(
				    "TXT files", "txt");
				chooser.setFileFilter(filter);
				//chooser.setCurrentDirectory("<YOUR DIR COMES HERE>");
				int returnVal = chooser.showOpenDialog(chooser);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
				   log.append("File approved");
				   dataFile = chooser.getSelectedFile();
				   File dataDir = chooser.getCurrentDirectory();
				   dirPane.setText(dataDir.toString());
				   FileParser fP = new FileParser(log, data);
				   try {
					dataUpload = fP.readFile(dataFile);
					data = fP.getData();
				   } catch (IOException e){
					e.printStackTrace();
				   }
				   log.append("File has been processed successfully." + "\n");
				   btnView.setEnabled(true);
				   btnClearData.setEnabled(true);
				   btnRun.setEnabled(true);
				}
			}
		});
		frmAfmanalytics.getContentPane().add(btnBrowse, "cell 7 10 3 1,growx");
		
	}
	
	private String[] getInputs()
	{
		String[] result = {sensFactorField.getText(), sprConstField.getText(), alphaField.getText(), impactZField.getText()};
		return result;
	}

	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "SwingAction");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
		}
	}
	
	public void logUpdate(String update)
	{
		log.append(update);
	}
	
//	private static PieDataset createDataset( ) 
//	   {
//	      DefaultPieDataset dataset = new DefaultPieDataset( );
//	      dataset.setValue( "IPhone 5s" , new Double( 20 ) );  
//	      dataset.setValue( "SamSung Grand" , new Double( 20 ) );   
//	      dataset.setValue( "MotoG" , new Double( 40 ) );    
//	      dataset.setValue( "Nokia Lumia" , new Double( 10 ) );  
//	      return dataset;         
//	   }
	
	private static void initFX(JFXPanel fxPanel) {
        // This method is invoked on the JavaFX thread
        Scene scene = createScene();
        fxPanel.setScene(scene);
    }
	
	private static void initFX(JFXPanel fxPanel, JFreeChart inChart) {
        // This method is invoked on the JavaFX thread
        Scene scene = createScene(inChart);
        fxPanel.setScene(scene);
    }
	
    private static Scene createScene() {
        Group  root  =  new  Group();
        Scene  scene  =  new  Scene(new ChartDisplay(), FXWIDTH, FXHEIGHT);
        return (scene);
    }
    
    private static Scene createScene(JFreeChart inChart){
    	Group  root  =  new  Group();
        Scene  scene  =  new  Scene(new ChartDisplay(inChart), FXWIDTH, FXHEIGHT);
        return (scene);
    }
    
    static class ChartDisplay extends StackPane implements ChartMouseListenerFX { //From JFreeChart CrosshairOverlayFXDemo1
        
        private ChartViewer chartViewer;
        private Crosshair xCrosshair;
        private Crosshair yCrosshair;
        private JFreeChart chart;
    
        public ChartDisplay()
        {
        	//Displays basic white box
        }
        
        
        public ChartDisplay(JFreeChart inChart) {
            //XYDataset dataset = createDataset();
            //JFreeChart chart = createChart(dataset); 
        	chart = inChart;
            this.chartViewer = new ChartViewer(chart);
            this.chartViewer.addChartMouseListener(this);
            getChildren().add(this.chartViewer);
           
            CrosshairOverlayFX crosshairOverlay = new CrosshairOverlayFX();
            this.xCrosshair = new Crosshair(Double.NaN, Color.WHITE, 
                    new BasicStroke(0f));
            this.xCrosshair.setStroke(new BasicStroke(1.5f, 
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, 
                    new float[]{2.0f, 2.0f}, 0));
            this.xCrosshair.setLabelVisible(true);
            this.yCrosshair = new Crosshair(Double.NaN, Color.WHITE, 
                    new BasicStroke(0f));
            this.yCrosshair.setStroke(new BasicStroke(1.5f, 
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, 
                    new float[] {2.0f, 2.0f}, 0));
            this.yCrosshair.setLabelVisible(true);
            crosshairOverlay.addDomainCrosshair(xCrosshair);
            crosshairOverlay.addRangeCrosshair(yCrosshair);
            
            Platform.runLater(() -> {
                this.chartViewer.getCanvas().addOverlay(crosshairOverlay);
            });
        }

        @Override
        public void chartMouseClicked(ChartMouseEventFX event) {
        	double xValue = this.xCrosshair.getValue();
        	SwingUtilities.invokeLater(new Runnable() {
        	    @Override
        	    public void run() {
        	    	//impactZField.setText("Woo");
        	    	impactZField.setText(String.format("%,.3f", xValue));
        	    }
        	});
        }

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
 
    public static XYDataset createDataset() {
        XYSeries series = new XYSeries("S1");
        for (int x = 0; x < 10; x++) {
            series.add(x, x + Math.random() * 4.0);
        }
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        return dataset;
    }

    public static JFreeChart createChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                "CrosshairOverlayDemo1", "X", "Y", dataset);
        return chart;
    }
    
    public static Paint toPaint(Color c) //http://stackoverflow.com/questions/30466405/java-convert-java-awt-color-to-javafx-scene-paint-color
    {
    	int r = c.getRed();
    	int g = c.getGreen();
    	int b = c.getBlue();
    	int a = c.getAlpha();
    	double opacity = a / 255.0 ;
    	return javafx.scene.paint.Color.rgb(r, g, b, opacity);
    }
}
