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

public class AfmDisplay{
	private static final Logger LOGGER = Logger.getLogger(AfmDisplay.class.getName() );
	
	private JFrame frmAfmanalytics;
	private final Action action = new SwingAction();
	private JTextField sensFactorField;
	private JTextField sprConstField;
	private JTextField alphaField;
	private JTextField impactZField;
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
		frmAfmanalytics.setResizable(false);
		frmAfmanalytics.setTitle("AFM-Analytics");
		frmAfmanalytics.setBounds(100, 100, 900, 650);
		frmAfmanalytics.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmAfmanalytics.getContentPane().setLayout(new MigLayout("", "[125.00px:100.00px:125.00px,grow][125.00px:100px][45.00px:120.00px][90.00:80.00][121.00px:87.00px:85.00px,grow][:100px:91.00px,grow][100.00px:61.00px,grow][100px,grow]", "[35,grow][35px,grow][35px][35px][35px][35px][35][][35.00][grow][][][grow]"));
		
		JFXPanel fxPanel = new JFXPanel(); //https://docs.oracle.com/javase/8/javafx/interoperability-tutorial/swing-fx-interoperability.htm
		frmAfmanalytics.getContentPane().add(fxPanel, "cell 0 1 5 7");
		
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX(fxPanel);
            }
		});
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		frmAfmanalytics.getContentPane().add(scrollPane, "cell 5 1 3 7,grow");
		
		
		//test
		JTextArea log = new JTextArea();
		scrollPane.setViewportView(log);
		
		data = new CurveData(log);
		
		JButton btnRun = new JButton("View Data");
		btnRun.setEnabled(false);
		btnRun.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				String[] testUnits = {"nm","V"};
				data.changeUnits(testUnits);
				data.printData();
//				PieDataset test = createDataset();
//				JFreeChart chart = ChartFactory.createPieChart(      
//				         "Mobile Sales",  // chart title 
//				         test,        // data    
//				         true,           // include legend   
//				         true, 
//				         false);
				JFreeChart chart = data.getXYChart();
				//chartPanel.setChart(chart);
			}
		});
		frmAfmanalytics.getContentPane().add(btnRun, "cell 6 8");
		
		JLabel lblSensitivityFactor = new JLabel("Sensitivity Factor");
		frmAfmanalytics.getContentPane().add(lblSensitivityFactor, "cell 0 9 2 1,alignx right");
		
		sensFactorField = new JTextField();
		frmAfmanalytics.getContentPane().add(sensFactorField, "cell 2 9,growx");
		sensFactorField.setColumns(10);
		
		JLabel lblnmv = new JLabel("(nm/V)");
		frmAfmanalytics.getContentPane().add(lblnmv, "cell 3 9,alignx left");
		
		JLabel lblAlpha = new JLabel("Alpha");
		frmAfmanalytics.getContentPane().add(lblAlpha, "cell 4 9 2 1,alignx right");
		
		alphaField = new JTextField();
		frmAfmanalytics.getContentPane().add(alphaField, "cell 6 9,growx");
		alphaField.setColumns(10);
		
		JLabel lbldeg = new JLabel("(deg)");
		frmAfmanalytics.getContentPane().add(lbldeg, "cell 7 9");
		
//		JTextArea log = new JTextArea();
//		log.setLineWrap(true);
//		log.setEditable(false);
//		frame.getContentPane().add(log, "flowx,cell 5 1 2 7,grow");
		
		//JScrollPane scrollPane = new JScrollPane(log);
		//frame.getContentPane().add(scrollPane, "cell 7 1 1 7,grow");
		
		JLabel lblSpringConstant = new JLabel("Spring Constant");
		frmAfmanalytics.getContentPane().add(lblSpringConstant, "cell 0 10 2 1,alignx right");
		
		sprConstField = new JTextField();
		frmAfmanalytics.getContentPane().add(sprConstField, "cell 2 10,growx");
		sprConstField.setColumns(10);
		
		JLabel lblnm = new JLabel("(N/m)");
		frmAfmanalytics.getContentPane().add(lblnm, "cell 3 10");
		
		JLabel lblImpactPointz = new JLabel("Impact Point (Z)");
		frmAfmanalytics.getContentPane().add(lblImpactPointz, "cell 4 10 2 1,alignx right");
		
		impactZField = new JTextField();
		frmAfmanalytics.getContentPane().add(impactZField, "cell 6 10,growx");
		impactZField.setColumns(10);
		
		JLabel lblnm_1 = new JLabel("(nm)");
		frmAfmanalytics.getContentPane().add(lblnm_1, "cell 7 10");
		
		JTextPane dirPane = new JTextPane();
		dirPane.setEditable(false);
		dirPane.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		frmAfmanalytics.getContentPane().add(dirPane, "cell 0 12 7 1,growx,aligny center");
		
		JButton btnBrowse = new JButton("Browse");
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
				   btnRun.setEnabled(true);
				}
			}
		});
		frmAfmanalytics.getContentPane().add(btnBrowse, "cell 7 12");
		
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

    private static Scene createScene() {
        Group  root  =  new  Group();
        Scene  scene  =  new  Scene(new ChartDisplay());
        Text  text  =  new  Text();
        
        //text.setX(40);
        //text.setY(100);
        //text.setFont(new Font(25));
        //text.setText("Welcome JavaFX!");

        root.getChildren().add(text);

        return (scene);
    }
	  
    static class ChartDisplay extends StackPane implements ChartMouseListenerFX { //From JFreeChart CrosshairOverlayFXDemo1
        
        private ChartViewer chartViewer;
        private Crosshair xCrosshair;
        private Crosshair yCrosshair;
    
        public ChartDisplay() {
            XYDataset dataset = createDataset();
            JFreeChart chart = createChart(dataset); 
            this.chartViewer = new ChartViewer(chart);
            this.chartViewer.addChartMouseListener(this);
            getChildren().add(this.chartViewer);
           
            CrosshairOverlayFX crosshairOverlay = new CrosshairOverlayFX();
            this.xCrosshair = new Crosshair(Double.NaN, Color.GRAY, 
                    new BasicStroke(0f));
            this.xCrosshair.setStroke(new BasicStroke(1.5f, 
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, 
                    new float[]{2.0f, 2.0f}, 0));
            this.xCrosshair.setLabelVisible(true);
            this.yCrosshair = new Crosshair(Double.NaN, Color.GRAY, 
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
            // ignore
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

}
