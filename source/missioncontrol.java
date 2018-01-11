import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.lang.Math;

public  class missioncontrol{

	double clockstep = 100; //sec
	int globalClock = 0; //sec 
	ArrayList<satellite> orbiter = new ArrayList<satellite>();

	satellite satellite1 = new satellite(35786+6372,35786+6372 ,0,"GEOstar-3");
	satellite satellite2 = new satellite(402+6372,402+6372,0,"International Space Station");
	//satellite satellite3 = new satellite(36000,12000,180,"Voyeger");


	ArrayList<Number> drawOrbitParams = new ArrayList<Number>();
	ArrayList<Number> drawPosParams = new ArrayList<Number>();

	int lines = 6;
	JFrame frame;
	JFrame dataframe;
	JPanel datapanel;
	JTextArea text;
	JButton button;
	JButton transferButton= new JButton("Tranfer Orbit");
	ArrayList<JLabel> labels = new ArrayList<JLabel>();
	MyDrawPanel panel = new MyDrawPanel();

	int flag = 0;

	//launch panel frame
	JFrame popup = new JFrame();
	JPanel panel2 = new JPanel();
	JButton button2 = new JButton("Confirm");
	JTextField nameInput = new JTextField(10);
	JTextField perigeeInput = new JTextField(10);
	JTextField apogeeInput = new JTextField(10);
	JTextField rotationInput = new JTextField(10);


	//transfer panel frame
		JFrame transferPopup = new JFrame();
		JPanel transferPanel = new JPanel();
		JButton button3 = new JButton("Confirm");
		JTextField transPerigeeInput = new JTextField(10);
		JTextField transApogeeInput = new JTextField(10);
		ArrayList<JRadioButton> Radio = new ArrayList<JRadioButton> ();
		ButtonGroup group = new ButtonGroup();


	public void build(){
	//build main animation panel
		orbiter.add(satellite1);
		orbiter.add(satellite2);
		//orbiter.add(satellite3);

		button = new JButton("Launch Satellite");
		button.addActionListener(new launchListener());

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(BorderLayout.SOUTH,button);
		frame.getContentPane().add(BorderLayout.CENTER,panel);
		frame.setSize(1000,675);
		frame.setVisible(true);

		dataframe = new JFrame();
		datapanel = new JPanel();
		datapanel.setLayout(new BoxLayout(datapanel,BoxLayout.Y_AXIS));
		buildDataFrame();
		buildLaunchFrame();
		buildTransferFrame();
		flag = 1;


	}

	public void buildDataFrame(){

		for(int i = 0; i < orbiter.size()*lines + 1; i++){
			labels.add(new JLabel());
		}
		dataframe.setSize(400,800);
		dataframe.setVisible(true);

		for (JLabel temp:labels ) {
		datapanel.add(temp);
		}
		if (flag == 0){
			transferButton.addActionListener(new transferListener());
		}

		datapanel.add(transferButton);	
		
		dataframe.getContentPane().add(datapanel);
		
	}

	public void buildLaunchFrame(){
		if(flag == 0){
		button2.addActionListener(new confirmListener());
		}
	
		panel2.removeAll();

		popup.getContentPane().add(BorderLayout.SOUTH,button2);
		popup.getContentPane().add(BorderLayout.CENTER,panel2);
		popup.setSize(200,300);
		popup.setVisible(false);

		panel2.setLayout(new BoxLayout(panel2,BoxLayout.Y_AXIS));

		panel2.add(new JLabel("Satellite Name "));
		panel2.add(nameInput);
		

		panel2.add(new JLabel("Perigee (Altitude km) "));
		panel2.add(perigeeInput);

		panel2.add(new JLabel("Apogee (Altitude km) "));
		panel2.add(apogeeInput);

		panel2.add(new JLabel("Orbit Rotation (degree) "));
		panel2.add(rotationInput);

		panel2.revalidate();
		panel2.repaint();
	}

	public void buildTransferFrame(){
		transferPanel.removeAll();
		Radio.clear();
		if (flag == 0){
			button3.addActionListener(new transferConfirmListener());
		}
		
		transferPopup.getContentPane().add(BorderLayout.SOUTH,button3);
		transferPopup.getContentPane().add(BorderLayout.CENTER,transferPanel);
		transferPopup.setSize(200,175+25*orbiter.size());
		transferPopup.setVisible(false);

		transferPanel.setLayout(new BoxLayout(transferPanel,BoxLayout.Y_AXIS));

		
		for(int i = 0; i < orbiter.size(); i++){
			Radio.add(new JRadioButton(orbiter.get(i).name));

			group.add(Radio.get(i));
			Radio.get(i).setActionCommand(orbiter.get(i).name);
			transferPanel.add(Radio.get(i));
		}

		transferPanel.add(new JLabel("Target Orbit Perigee (km) "));
		transferPanel.add(transPerigeeInput);

		transferPanel.add(new JLabel("Target Orbit Apogee (km) "));
		transferPanel.add(transApogeeInput);

		transferPanel.revalidate();
		transferPanel.repaint();

		transferPopup.getContentPane().add(transferPanel);
	}

	public void textUpdate(){
		int j = 0;
		ArrayList<String> info = new ArrayList<String>();
			for (satellite temp:orbiter) {
				info = temp.getInfo();
				for(int i=0;i<lines;i++){
					String s1 = info.get(i);
					labels.get(i+j).setText(s1);
				}
				j = j+lines;
			
			}
			
	}

	public void getParams(satellite orbiter ){
		drawOrbitParams = orbiter.convertOrbit(orbiter.getOrbitParams());
		drawPosParams = orbiter.convertPosition();
	   //System.out.println("flag");

	}

	public void animate(){
		for (double t = 0;t != 1 ;t = t+clockstep) {
			for (satellite temp:orbiter){
			
				temp.update(clockstep);
				if(temp.getFlag() > 0){
					if (temp.getTrueAnomoly() < 5 && temp.getFlag() == 1){
						temp.changeOrbit(temp.getTransferOrbitParams().get(3),temp.getTransferOrbitParams().get(4));
						temp.transferFlag++;
					}
					if (temp.getTrueAnomoly() > 179 && temp.getTrueAnomoly() < 195 && temp.getFlag() == 2){
						temp.changeOrbit(temp.getNewOrbitParams().get(3),temp.getNewOrbitParams().get(4));
						temp.transferFlag = 0;
					} 
				}
			}
			textUpdate();
			panel.repaint();
			try{
				Thread.sleep(100);
			}catch(Exception ex){}
		}
	}

	public void hofmanTransfer(satellite target, double apogeeNew, double perigeeNew){
		target.transferFlag = 1; 
		target.calcTransfer(apogeeNew,perigeeNew);
		
	}

class transferListener implements ActionListener{
	public void actionPerformed(ActionEvent event){
		buildTransferFrame();
		transferPopup.setVisible(true);

	}
}

class transferConfirmListener implements ActionListener{
	public void actionPerformed(ActionEvent event){
		satellite transferTarget = new satellite();

		Double peri = Double.parseDouble(transPerigeeInput.getText())+6372;
		Double apo = Double.parseDouble(transApogeeInput.getText())+6372;
		String name = group.getSelection().getActionCommand();
		for(satellite temp:orbiter){
			if (temp.name == name){
				transferTarget = temp;
			}
		}

		hofmanTransfer(transferTarget,apo,peri);

		transferPopup.dispatchEvent(new WindowEvent(transferPopup, WindowEvent.WINDOW_CLOSING));	
	}

}


class launchListener implements ActionListener{
	public void actionPerformed(ActionEvent event){
		buildLaunchFrame();
		popup.setVisible(true);
	}
}
	
class confirmListener implements ActionListener{
	public void actionPerformed(ActionEvent event){
		Double peri = Double.parseDouble(perigeeInput.getText())+6372;
		Double apo = Double.parseDouble(apogeeInput.getText())+6372;
		Double rot = Double.parseDouble(rotationInput.getText());
		String name = nameInput.getText();
		
		orbiter.add(new satellite(apo,peri,rot,name));
		buildDataFrame();
		popup.dispatchEvent(new WindowEvent(popup, WindowEvent.WINDOW_CLOSING));

	}
}


class MyDrawPanel extends JPanel{

	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D) g;


		g2d.setColor(Color.black);
		g2d.fillRect(0,0,this.getWidth(),this.getHeight());
		g2d.setColor(Color.blue);
		g2d.fillOval(475,300,50,50);
		
		for(satellite temp:orbiter){
		
			getParams(temp);

			int x = (int)drawOrbitParams.get(0);
			int y = (int)drawOrbitParams.get(1);
			int w = (int)drawOrbitParams.get(2);
			int h = (int)drawOrbitParams.get(3);

			
			int xp = (int)drawPosParams.get(0);
			int yp = (int)drawPosParams.get(1);

			g2d.rotate((double)drawOrbitParams.get(4),500,325);
			g2d.setColor(Color.white);
			g2d.drawOval(x,y,w,h);

			g2d.fillOval(xp,yp,20,20);

			if(temp.getFlag() > 0 ){
				ArrayList<Number> transfer = temp.convertOrbit(temp.getTransferOrbitParams());
				int xt = (int)transfer.get(0);
				int yt = (int)transfer.get(1);
				int wt = (int)transfer.get(2);
				int ht = (int)transfer.get(3);
				g2d.setColor(Color.green);
				g2d.drawOval(xt,yt,wt,ht);


				ArrayList<Number> newOrbit = temp.convertOrbit(temp.getNewOrbitParams());
				int xn = (int) newOrbit.get(0);
				int yn = (int)newOrbit.get(1);
				int wn = (int)newOrbit.get(2);
				int hn = (int)newOrbit.get(3);
				g2d.setColor(Color.white);
				g2d.drawOval(xn,yn,wn,hn);

			}
		}
	}
}
}
