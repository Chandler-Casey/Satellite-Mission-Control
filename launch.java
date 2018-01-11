import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.lang.Math;

public  class launch{

	public static void main (String[] args) {
		missioncontrol MC = new missioncontrol();
		MC.build();
		MC.animate();
	}
}