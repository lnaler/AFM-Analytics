package ln.afm.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

/**
 * Fits a curve of the form ax^2 using a genetic algorithm by minimizing mean square error. Adapted from matlab code 
 * from Engineering Math Fall 2016 taught by Ryan Senger
 * @author Lynette Naler
 *
 */
public class GeneticLinReg {
		
	int minmax = 1; //1 maximize -1 minimize
	int numChromosomes = 120;
	int numGenerations = 1000;
	double reproduced = 0.25; //percentage of the elite population that is reproduced 
	double operator1 = 0.25; //percentage of new population produced by first operator
	double BLXa = 0.35; //alpha of the BLX-a operator
	double operator2 = 0.25; //percentage of new population produced by second operator
	int num_b = 1; //parameter (b) of non-uniform mutation operator
	double rand_gen = 0.25;	 //percentage of new population produced randomly (reproduced+operator1+operator2+rand_gen=1)
	
	double slope = 0;
	
	List<Point2D> comparePoints;

	/**
	 * Constructor
	 * @param points the data points to compare against
	 * @param iterations number of generations to run
	 */
	public GeneticLinReg(List<Point2D> points, int iterations)
	{
		comparePoints = points;
		numGenerations = iterations;
		//shiftPoints();
	}
	
	/**
	 * Performs the genetic algorithm
	 * @return true if the run is successful
	 */
	public boolean run()
	{
		
		System.out.println("Beginning Genetic Analysis.");
		// ----------------------------------------------------
		//check percentages 
		if(reproduced+operator1+operator2+rand_gen != 1)
		{
			System.out.println("Error. The percentages from reproduction, operator 1, operator 2, and random generation should add up to 1.");     
			return false;
		}
		
		
		//input data and run an initial model calculation
		double ga_input= 2500;   //initial chromosome (1 gene) Slope 
		double limits[] = new double[3];
		Random rand = new Random();
		limits[0]= 0;    //lower limits for each gene 
		limits[1]= 50000;    //upper limits for each gene ~1113kPa
		limits[2]=limits[1]-limits[0];
		
		//inititate chromosomes 
		//int chromLength = 1; 
		List<Double> chrom = new ArrayList<Double>();
		for(int i = 0; i < numChromosomes; i++)
		{
			chrom.add(ga_input);
		}
		for(int i = 1; i < numChromosomes;i++)
		{
			double nextValue = limits[0]+ rand.nextDouble()*limits[2];
			while(chrom.contains(nextValue))
			{
				nextValue = limits[0]+ rand.nextDouble()*limits[2];
			}
			chrom.set(i, nextValue);
		}

			
		//%evaluate all initial chromosomes (first generation) 
		int generation = 0;
		List<Double> chromResult = new ArrayList<Double>();
		for(int i = 0; i < numChromosomes; i++)
		{
			chromResult.add(0.0);
		}
		for(int i = 0; i < numChromosomes;i++)
		{
			double testSlope = chrom.get(i);
			chromResult.set(i, getError(testSlope));
		}
		
		TreeMap<Long,Double> chromOld = new TreeMap<Long,Double>();
		for(int i = 0; i < numChromosomes;i++)
		{
			System.out.println("Putting Key: " + chromResult.get(i) + ", Value: " + chrom.get(i));
			
			//3 decimal places
			long key = Math.round(chromResult.get(i)*1000);
			//System.out.println("Key is: " + key);
			chromOld.put(key, chrom.get(i)); //key is error, value is slope. Possible, but unlikely for overlap
			//System.out.println("Contains Key?: " + chromOld.containsKey(chromResult.get(i)));
		}
			
				
		//Should be autosorted;
//		%sort results (mse) in ascending order 
//		if minmax==1     
//			chrom_old=sortrows(chrom_old,-(chrom_length+1)); 
//		else     
//			chrom_old=sortrows(chrom_old,(chrom_length+1)); 
//		end
//			
		
		chrom = new ArrayList<Double>();
		for(int i = 0; i < numChromosomes; i++)
		{
			chrom.add(0.0);
		}   
		//%start the loop of the genetic algorithm
		while(generation < numGenerations)
		{
			//%initiate the new chromosomes variable
			int n = 0;
			//%reproduction     
			//%elite chromosomes    
			for(n = 0; n < Math.ceil(numChromosomes*reproduced); n++)
			{
				double lowest = chromOld.get(chromOld.firstKey());
				chrom.set(n, lowest);
				chromOld.remove(chromOld.firstKey());
			}         
			
			System.out.println("Part B");
			//%operator1: BLX-a crossover (a=0.35)     
			//%strategy: (i) choose two of the "reproduced" chromosomes, (ii) perform operation and (iii) add them both     
			//%to the chrom list.         
			
			while(n < (numChromosomes*reproduced + numChromosomes*operator1))
			{
				//%choose the reproduced chromosomes and save them as "temporary" chromosomes      
				int tempVal = ((Number)Math.ceil(rand.nextDouble()*numChromosomes*reproduced)).intValue();
				double BLX_temp1=chrom.get(tempVal);
				tempVal = ((Number)Math.ceil(rand.nextDouble()*numChromosomes*reproduced)).intValue();
				double BLX_temp2=chrom.get(tempVal);                 
				
				//%choose the column (gene) to manipulate         
				//double BLX_col=Math.ceil(rand.nextDouble()*1);                 
				//%BLX-a operators         
				double cmax=Math.max(BLX_temp1,BLX_temp2);         
				double cmin=Math.min(BLX_temp1,BLX_temp2);                 
				double Ic=cmax-cmin;                 
				
				if(Ic == 0)
				{
					cmax=cmax+(rand.nextDouble()*0.5*cmax);             
					cmin=cmin-(rand.nextDouble()*0.5*cmin);             
					Ic=cmax-cmin;   
				}                 
				
				double BLX_min=cmin-Ic*BLXa;         
				double BLX_max=cmax+Ic*BLXa;                 
				
				//%perform the operation         
				BLX_temp1=BLX_min+rand.nextDouble()*(BLX_max-BLX_min);         
				BLX_temp2=BLX_min+rand.nextDouble()*(BLX_max-BLX_min);                 
				
				//%check that these are within defined limits.  if not, reset value to limit.         
				if(BLX_temp1 < limits[0])
				{
					BLX_temp1=limits[0];         
				}         
				if(BLX_temp1 > limits[1])
				{
					BLX_temp1=limits[1];
				}
				if(BLX_temp2 < limits[0])
				{
					BLX_temp2=limits[0];         
				}         
				if(BLX_temp2 > limits[1])
				{
					BLX_temp2=limits[1];
				}                 
				
				//%insert into the chrom list  
				chrom.set(n, BLX_temp1);
				n++;
				chrom.set(n, BLX_temp2);
				n++;
			}

		    //%operator2: non-uniform mutation         
		    while (n<(numChromosomes*reproduced+numChromosomes*operator1+numChromosomes*operator2))               
		    {
		    	//%choose a reproduced chromosomes and save it as "temporary" chromosome         
		    	double num_temp1=chrom.get(((Number)Math.ceil(rand.nextDouble()*numChromosomes*reproduced)).intValue());                 
				
				//%choose the column (gene) to manipulate         
				//int num_col=1;
				
				//%non-uniform mutation operators and stochastic variables         
				int tau= Math.round(rand.nextFloat());                 
				
				double num_y_up = limits[1]-num_temp1;         
				double num_y_down=num_temp1-limits[0];                 
				
				double num_t_up=num_y_up*(1-Math.pow(rand.nextDouble(),Math.pow((1-generation/numGenerations),num_b)));         
				double num_t_down=num_y_down*(1-Math.pow(rand.nextDouble(),Math.pow((1-generation/numGenerations),num_b)));                 
				
				if(tau==0)
				{
					num_temp1=num_temp1-num_t_down; 
				}
				else
				{
					num_temp1=num_temp1+num_t_up;  
				}                 
				
				//%check limits (really not necessary given nature of the mutation)         
				if(num_temp1 < limits[0])
				{
					num_temp1=limits[0];         
				}
				if(num_temp1 > limits[1])
				{
					num_temp1=limits[1];
				}                 
				
				//%insert into chrom list
				chrom.set(n, num_temp1);
				n++;
				
		    }
		    
			
			//%fill-in the rest of the available chrom spaces with randomly-generated chromosomes       
		    while (n < numChromosomes)
		    {
		    	double tempValue = limits[0]+rand.nextDouble()*limits[2];
		    	while(chrom.contains(tempValue))
		    	{
		    		tempValue = limits[0]+rand.nextDouble()*limits[2];
		    	}
		    	chrom.set(n, tempValue);
		    	n++;
		    }    
			
			         
			//%evaluate all initial chromosomes (this generation)
		    chromResult = new ArrayList<Double>();
			for(int i = 0; i < numChromosomes; i++)
			{
				chromResult.add(0.0);
			}        
			for(int i=0; i < numChromosomes; i++)
			{
				double tempSlope = chrom.get(i);
				chromResult.set(i, getError(tempSlope));
				
			}
			
			chromOld = new TreeMap<>();
			for(int i = 0; i < numChromosomes;i++)
			{
				long key = Math.round(chromResult.get(i)*1000);
				//System.out.println("Key is: " + key);
				chromOld.put(key, chrom.get(i)); //key is error, value is slope. Possible, but unlikely for overlap
			}
			
			
			System.out.println("Computing generation " + generation + " of " + numGenerations + ". Optimum result: " + (chromOld.firstKey()/1000.0));  
			generation++;
		}
		double optimumResult = ((double)chromOld.firstKey()/1000.0);
		double optimumChrom = chromOld.get(chromOld.firstKey());
		slope = optimumChrom;
		
		System.out.println("Optimum Slope: " + optimumChrom + " Error: " + optimumResult);
		
		//System.out.println("Least Squares: " + getLS());
		return true;
	}
	
	/**
	 * Calculates the error of a given slope
	 * @param slope the test slope
	 * @return the error of the test slope
	 */
	private double getError(double slope)
	{
		double error = 0;
		double n = comparePoints.size();
		for(int i = 0;i < n; i++)
		{
//			double x = comparePoints.get(i).x;
//			double actY = comparePoints.get(i).y;
//			double predY = slope*Math.pow(x,2);

			double logx = comparePoints.get(i).x;
			double actY = comparePoints.get(i).y;
			double predY = Math.log(slope) + 2*logx;
			
			double thisError = Math.pow((predY - actY), 2);
			error = error + thisError;
		}
		error = error/n;
		return error;
	}
	
	/**
	 * Returns the slope found
	 * @return the optimum slope found
	 */
	public double getSlope()
	{
		return slope;
	}

}
