package ln.afm.model;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple limited converter of voltages and distances
 * @author Lynette Naler
 *
 */
public class Converter {
	//Our conversions
	private static ConcurrentHashMap<String, Double> zUnits = new ConcurrentHashMap<String,Double>(); //http://stackoverflow.com/questions/507602/how-can-i-initialise-a-static-map
    static {
        zUnits.put("m", 1.0);
		zUnits.put("cm", 100.0);
		zUnits.put("mm", 1000.0);
		zUnits.put("µm", 1000000.0);
		zUnits.put("um", 1000000.0);
		zUnits.put("nm", 1000000000.0);
    }
	private static ConcurrentHashMap<String, Double> vUnits = new ConcurrentHashMap<String, Double>();
    static {
        vUnits.put("v", 1.0);
		vUnits.put("cv", 100.0);
		vUnits.put("mv", 1000.0);
		vUnits.put("µv", 1000000.0);
		vUnits.put("uv", 1000000.0);
		vUnits.put("nv", 1000000000.0);
    }
		
    /**
     * Get the conversion rate
     * @param oldUnits units to be converted from
     * @param newUnits units to be converted to
     * @param unitType 0 if impact units, 1 if voltage units
     * @return
     */
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
	
	public static boolean hasUnits(String units1, String units2)
	{
		return (zUnits.containsKey(units1.toLowerCase()) && vUnits.containsKey(units2.toLowerCase()));
	}
}
