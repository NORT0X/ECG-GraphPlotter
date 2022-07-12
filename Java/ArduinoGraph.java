import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Console;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.Series;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import com.fazecast.jSerialComm.SerialPort;

public class ArduinoGraph {
	
	static Stroke stroke;
	static boolean startCalculatingBpm = false;
	static SerialPort chosenPort; 
	static int x = 0;
	static int maxY = 1023;
	static int k = 0;
	static int maxX = 500;
	static int bpmInt = 0;
	static float beforeCnt;
	static float cnt;
	static int countR = 0;
	static int[] bpmArray = new int[100];
	static boolean checkFall = true;
	static OutputStream outputStream;
	public static void main(String[] args) {
		
		//create and cnfigure the window
		JFrame window = new JFrame();
		window.setTitle("ECG");
		window.setSize(600, 400);
		ImageIcon img = new ImageIcon("Icons/ecg-icon.png");
		window.setIconImage(img.getImage());
		window.setLayout(new BorderLayout());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		//create a drop-down box and connect button, then place them at the top of the window
		JComboBox<String> portList = new JComboBox<String>();
		JButton pauseButton = new JButton("Pause");
		JLabel bpmText = new JLabel();
		JLabel bpm = new JLabel();
		bpmText.setText("BPM : ");
		JButton connectButton = new JButton("Connect");
		JPanel topPanel = new JPanel();
		topPanel.add(portList);
		topPanel.add(connectButton);
		topPanel.add(pauseButton);
		topPanel.add(bpmText);
		topPanel.add(bpm);
		window.add(topPanel, BorderLayout.NORTH);
		topPanel.setBackground(Color.WHITE);
		
		//populate the drop-down box
		SerialPort[] portNames = SerialPort.getCommPorts();
		for(int i = 0; i < portNames.length; i++) {
			portList.addItem(portNames[i].getSystemPortName());
		}
		
		bpm.setText(Integer.toString(countR));
		
		//create the line graph
		XYSeries series = new XYSeries("Heart");
		series.setMaximumItemCount(1000);
		XYSeriesCollection dataset = new XYSeriesCollection(series);
		JFreeChart chart = ChartFactory.createXYLineChart("ECG Signal", "Time(centisecond)", "Signal", dataset);
		chart.setBorderStroke(stroke);
		chart.setBackgroundPaint(Color.WHITE);;
		window.add(new ChartPanel(chart), BorderLayout.CENTER);
		XYPlot plot = chart.getXYPlot();
		plot.setDataset(0, dataset);
		plot.setBackgroundPaint(Color.PINK);
		plot.setDomainGridlinePaint(Color.RED);
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesPaint(0, Color.BLACK);
		plot.getRendererForDataset(plot.getDataset(0)).setSeriesPaint(0, Color.BLACK);
		plot.setDomainMinorGridlinesVisible(true);
		plot.setDomainMinorGridlinePaint(Color.RED);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.RED);
		plot.setRangeMinorGridlinesVisible(true);
		plot.setRangeMinorGridlinePaint(Color.RED);
		plot.setRangeGridlineStroke(new BasicStroke(1.0F));
		plot.setDomainGridlineStroke(new BasicStroke(1.0F));
		
		class MyThread extends Thread {
			public boolean pause = false;
			public void run() {
				Scanner scanner = new Scanner(chosenPort.getInputStream());
				while(scanner.hasNextLine()) {
					try {
					if(pause) { wait();}
					String line = scanner.nextLine();
					float number = Float.parseFloat(line);
					series.add(x++,number);
					
					
				
					if(x>=1000 && x <= 2000) {
						if(number > 450.00 && checkFall) {
							calculateBpm();
							countR++;
							checkFall = false;
						}
						else if(number < 420.00) {
							checkFall = true;
						}
					}
					if(x==2000) {
						startCalculatingBpm = true;
					}
					if(x > 2000 && startCalculatingBpm==true) {
						for(int i = 1 ; i < k; i++) {
							bpmInt += bpmArray[i];
						}
						bpmInt = bpmInt / (k-1);
						bpm.setText(Integer.toString(bpmInt));
						startCalculatingBpm = false;
					}
					window.repaint();
					
					}catch(Exception e) {}
				}
				scanner.close();
			}
		};
		MyThread thread = new MyThread();
		
		
		//configure the connect button and use another thread to listen for data
		connectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(connectButton.getText().equals("Connect")) {
					//attempt to connect to the serial port
					chosenPort = SerialPort.getCommPort(portList.getSelectedItem().toString());
					chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
					if(chosenPort.openPort()) {
						connectButton.setText("Disconnect");
						portList.setEnabled(false);
					}
					
					thread.start();
				}else {
					//disconnect from the serial port
					chosenPort.closePort();
					portList.setEnabled(true);
					connectButton.setText("Connect");
					series.clear();
					x = 0;
				}
				
			}
			
		});
		pauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(pauseButton.getText().equals("Pause")){
					thread.pause = true;
					pauseButton.setText("Start");
				}
				else {
					series.clear();
					x = 0;
					thread.pause = false;
					pauseButton.setText("Pause");
				}
			}
			
		});
		//show the window
		window.setVisible(true);
		
	}

	public static void calculateBpm() {
		cnt = (int)System.currentTimeMillis();
		bpmArray[k] = (int)(60000/(cnt-beforeCnt));
		k++;
		beforeCnt = cnt;
	}
}
