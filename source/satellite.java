import java.util.ArrayList;
import java.lang.Math;

public class satellite {
	int windowx = 1000;
	int windowy = 650;
	String name;
	double vel; 
	double clock = 0; //sec
	double trueAnomoly = 0; // rad
	double apogee; //km
	double perigee; //km
	double rotation; //rad
	double mu = 3.986*Math.pow(10,5); // gravitatonal parameter km^3/s^2
	double a = (apogee+perigee)/2; // semimajor axis (km)
	double e = 2*apogee/(apogee+perigee) -1; // orbital eccentricity
	double b = a*Math.sqrt(1-Math.pow(e,2)); //semiminor axis (km)

	double apogeeNew;
	double perigeeNew;

	double aNew;
	double eNew;
	double bNew;

	double aTrans;
	double bTrans;
	double eTrans;

	double apogeeTrans;
	double perigeeTrans;

	double delV1;
	double delV2;

	int transferFlag = 0;

	double r = (a*(1-Math.pow(e,2)))/(1+e*Math.cos(trueAnomoly));
	double nudot = Math.sqrt(-mu/(Math.pow(r,2)*a)+2*mu/(Math.pow(r,3)));
	satellite(){
		//No argument constructor to build temp satellite object in mission control
	}

	satellite(double inputapogee,double inputperigee,double inputrotation, String inname){
		name = inname;
		apogee = inputapogee;
		perigee = inputperigee;
		rotation = Math.toRadians(360-inputrotation%360);
		a = (apogee+perigee)/2; // semimajor axis (km)
		e = 2*apogee/(apogee+perigee) -1; // orbital eccentricity
		b = a*Math.sqrt(1-Math.pow(e,2)); //semiminor axis (km)
		r = (a*(1-Math.pow(e,2)))/(1+e*Math.cos(trueAnomoly));
		nudot = Math.sqrt(-mu*Math.pow(r,2)/a+2*mu*r);

	} 
	public void update(double clockstep){
		trueAnomoly = (trueAnomoly+nudot*clockstep)%(2*Math.PI);
		r = (a*(1-Math.pow(e,2)))/(1+e*Math.cos(trueAnomoly));
		nudot = Math.sqrt(-mu/(Math.pow(r,2)*a)+2*mu/(Math.pow(r,3)));
		vel = nudot*r;
		clock = clock + clockstep;

		//System.out.println(Math.toDegrees(trueAnomoly));
	}

	public int getFlag(){
		return transferFlag;
	}

	public ArrayList<Double> getOrbitParams(){
		ArrayList<Double> params = new ArrayList<Double>();
		params.add(a);
		params.add(b);
		params.add(e);

		return params;
	}

	public ArrayList<Double> getNewOrbitParams(){
		ArrayList<Double> params = new ArrayList<Double>();
		params.add(aNew);
		params.add(bNew);
		params.add(eNew);

		params.add(apogeeNew);
		params.add(perigeeNew);

		return params;
	}

	public ArrayList<Double> getTransferOrbitParams(){
		ArrayList<Double> params = new ArrayList<Double>();
		params.add(aTrans);
		params.add(bTrans);
		params.add(eTrans);
		params.add(apogeeTrans);
		params.add(perigeeTrans);

		return params;
	}

	public double getTrueAnomoly(){
		return Math.toDegrees(trueAnomoly);
	}

	public ArrayList<String> getInfo(){
		ArrayList<String> info = new ArrayList<String>();
		String condition = " ";
		if (transferFlag == 0){
			condition = "Orbiting";
		}
		if (transferFlag == 1){
			condition = String.format("Awaiting injection to transfer orbit, Delta V1:  %.3f km/s",delV1);
		}
			if (transferFlag == 2){
			condition = String.format("Awaiting injection to target orbit, Delta V2:  %.3f km/s",delV2);
		}

		info.add(name);
		info.add("Orbit: "+ (perigee -6372)+ " km perigee     "
			+ (apogee-6372) + " km apogee");
		info.add(String.format("Velocity: %.3f km/s", vel));
		info.add(String.format("True Anomoly: %.3f degrees",(360 - Math.toDegrees(trueAnomoly)%360)));
		info.add(String.format("Status: "+ condition));
		info.add(" "); // for spaceing between entries


		return info;
	}

	public ArrayList<Number> convertOrbit(ArrayList<Double> orbit){
		// earth is <0,0> in real space, must convert to CPU space
		double tempA = orbit.get(0);
		double tempB = orbit.get(1);
		double tempE = orbit.get(2);

		double scale = (double) (6372/40); // km:pixel
		double OEx = windowx/2;
		double OEy = windowy/2;
		double localX = -tempA*(1+tempE)/scale;
		double localY = tempB/scale;

		//corner of orbit, in real space is <localx,localy>
		// corner of orbit, CPU space <CPUx,CPUy>
		int CPUx =(int)(OEx + localX);
		int CPUy =(int)(OEy - localY);
		int w = (int)(2*tempA/scale);
		int h = (int)(2*tempB/scale);
		ArrayList<Number> drawOrbit = new ArrayList<Number>();
		drawOrbit.add(CPUx);
		drawOrbit.add(CPUy);
		drawOrbit.add(w);
		drawOrbit.add(h);
		drawOrbit.add(rotation);

		return drawOrbit;

		}


	public ArrayList<Number>convertPosition(){
		// earth is <0,0> in real space, must convert to CPU space
		double scale = (double) (6372/40); // km:pixel
		double OEx = windowx/2;
		double OEy = windowy/2;
		double localX = 10;
		double localY = 10;

		//corner of orbit, in real space is <localx,localy>
		// corner of orbit, CPU space <CPUx,CPUy>
		int CPUx =(int)(OEx - localX + r*Math.cos(trueAnomoly)/scale);
		int CPUy =(int)(OEy - localY + r*Math.sin(trueAnomoly)/scale);

		ArrayList<Number> drawPos = new ArrayList<Number>();
		drawPos.add(CPUx);
		drawPos.add(CPUy);

		//System.out.println(CPUx);
		//System.out.println(CPUy);
		//System.out.println(r*Math.cos(trueAnomoly));


		return drawPos;

		}

	public void changeOrbit(double inputapogee,double inputperigee){
		apogee = inputapogee;
		perigee = inputperigee;
		a = (apogee+perigee)/2; // semimajor axis (km)
		e = 2*apogee/(apogee+perigee) -1; // orbital eccentricity
		b = a*Math.sqrt(1-Math.pow(e,2)); //semiminor axis (km)
	}

	public void calcTransfer(double newapogee,double newperigee){
		aNew = (newapogee+newperigee)/2; // semimajor axis (km)
		eNew = 2*newapogee/(newapogee+newperigee) -1; // orbital eccentricity
		bNew = aNew*Math.sqrt(1-Math.pow(eNew,2)); //semiminor axis (km)

		apogeeNew = newapogee;
		perigeeNew = newperigee;

		apogeeTrans = newapogee;
		perigeeTrans = perigee;

		aTrans = (apogeeTrans+perigeeTrans)/2;
		eTrans = 2*apogeeTrans/(apogeeTrans+perigeeTrans) -1; // orbital eccentricity
		bTrans = aTrans*Math.sqrt(1-Math.pow(eTrans,2)); //semiminor axis (km)

		double Vpo = Math.sqrt(-mu/a+2*mu/perigee);
		double Vpt = Math.sqrt(-mu/aTrans+2*mu/perigeeTrans);

		delV1 = Vpt-Vpo;

		double Vapn = Math.sqrt(-mu/aNew+2*mu/apogeeNew);
		double Vat = Math.sqrt(-mu/aTrans+2*mu/apogeeTrans);

		delV2 = Vapn - Vat;
	}

}