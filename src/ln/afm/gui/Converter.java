package ln.afm.gui;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple limited converter of voltages and distances
 * @author Lynette Naler
 *
 */
public class Converter {
	private static ConcurrentHashMap<String, Double> zUnits = new ConcurrentHashMap<String,Double>(); //http://stackoverflow.com/questions/507602/how-can-i-initialise-a-static-map
    static {
        zUnits.put("m", 1.0);
		zUnits.put("cm", 100.0);
		zUnits.put("mm", 1000.0);
		zUnits.put("�m", 1000000.0);
		zUnits.put("nm", 1000000000.0);
    }
	private static ConcurrentHashMap<String, Double> vUnits = new ConcurrentHashMap<String, Double>();
    static {
        vUnits.put("v", 1.0);
		vUnits.put("cv", 100.0);
		vUnits.put("mv", 1000.0);
		vUnits.put("�v", 1000000.0);
		vUnits.put("nv", 1000000000.0);
    }
		
	public static double convert(String oldUnits, String newUnits, int unitType)
	{
		oldUnits = oldUnits.toLowerCase();
		newUnits = newUnits.toLowerCase();
		
		double numerator;
		double denominator;
		
		if(unitType == 0)
		{
			if(zUnits.containsKey(newUnits) && zUnits.containsKey(oldUnits))
			{
				numerator = zUnits.get(newUnits);
				denominator = zUnits.get(oldUnits);
				double result = (numerator/denominator);
				return result;
			}
		}
		if(unitType == 1)
		{
			if(vUnits.containsKey(newUnits) && vUnits.containsKey(oldUnits))
			{
				numerator = vUnits.get(newUnits);
				denominator = vUnits.get(oldUnits);
				double result = (numerator/denominator);
				return result;
			}
		}
		return 1.0;
	}

	/**
	 * Returns a conversion number by which original number * 
	 * @param oldUnits A string of the original units
	 * @param newUnits A string of the units to convert to
	 * @return the conversion number
	 */
	public static double[] getConversion(String[] oldUnits, String[] newUnits)
	{
		oldUnits[0] = oldUnits[0].toLowerCase();
		newUnits[0] = newUnits[0].toLowerCase();
		
		oldUnits[1] = oldUnits[1].toLowerCase();
		newUnits[1] = newUnits[1].toLowerCase();
		double[] results = {convert(oldUnits[0], newUnits[0], 0), convert(oldUnits[1], newUnits[1], 1)};
		return results;
	}
	
}
