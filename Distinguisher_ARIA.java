package aria;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.objective.ParetoOptimizer;
import org.chocosolver.solver.variables.IntVar;

public class Distinguisher_ARIA {
	

	public static int R = 4, bl = 16,keylimit = 30; 
	
	
//Mixcolumn matrix
    	public static int[][] M = {
{0, 0, 0, 1, 1, 0, 1, 0, 1, 1, 0, 0, 0, 1, 1, 0},
{0, 0, 1, 0, 0, 1, 0, 1, 1, 1, 0, 0, 1, 0, 0, 1},
{0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 1, 1, 0, 0, 1},
{1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 1, 1, 0, 1, 1, 0},
{1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 1},
{0, 1, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1},
{1, 0, 1, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 1, 0, 0},
{0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 1, 1, 0, 0},
{1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1},
{1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 1, 0},
{0, 0, 1, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 1, 0, 1},
{0, 0, 1, 1, 1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0},
{0, 1, 1, 0, 0, 0, 1, 1, 0, 1, 0, 1, 1, 0, 0, 0},
{1, 0, 0, 1, 0, 0, 1, 1, 1, 0, 1, 0, 0, 1, 0, 0},
{1, 0, 0, 1, 1, 1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 0},
{0, 1, 1, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1}
};

	
    public static void main(String[] args){

        
    	Model mitm_aria = new Model("mitm_aria");
    	IntVar[][] IS = mitm_aria.intVarMatrix("IS", R+1, bl,0,1);
    	IntVar[][] DIS = mitm_aria.intVarMatrix("DIS", R+1, bl,0,1);
    	IntVar[][] GIS = mitm_aria.intVarMatrix("GIS", R+1, bl,0,1);
    	for (int r = 0; r < R; r++){
    		for(int col=0; col<bl;col++){
    			IntVar[] AD = mitm_aria.intVarArray("AD"+'_'+(r)+'_'+(col), 2,0,7);
    			
    			mitm_aria.scalar(IS[r], M[col],"=", AD[0]).post();
    			mitm_aria.times(IS[r+1][col],7, AD[1]).post();
    			mitm_aria.arithm(AD[1],">=",AD[0]).post();
    			mitm_aria.arithm(AD[0], ">=", IS[r+1][col]).post();
    		}
    		
    	}
   
    	
    	for (int r = 0; r < R ; r++){
    		for(int col=0; col<bl;col++){
    			IntVar[] DAD = mitm_aria.intVarArray("DAD"+'_'+(r)+'_'+(col), 2,0,7);
    			
    			mitm_aria.scalar(DIS[r+1], M[col],"=", DAD[0]).post();
    			mitm_aria.times(DIS[r][col],7, DAD[1]).post();
    			mitm_aria.arithm(DAD[1],">=",DAD[0]).post();
    			mitm_aria.arithm(DAD[0], ">=", DIS[r][col]).post();
    		}
    		
    	}
    	
    	for (int r = 0; r <= R; r++){
    		for(int col=0; col<bl;col++){	
    			mitm_aria.scalar(new IntVar[] {GIS[r][col],IS[r][col],DIS[r][col]}, new int[] {1,-1,-1},">=",-1 ).post();//DAD[0] = DIS[r+1][i1]+...+DIS[r+1][it]
    			mitm_aria.arithm(IS[r][col],">=",GIS[r][col]).post();
    			mitm_aria.arithm(DIS[r][col], ">=", GIS[r][col]).post();
    		}
    		
    	}
    	
    	
    	
    	//Objective function
    	IntVar[] Var_Obj =  new IntVar[(R-1)*bl];
    	int index =0;
    	for (int r = 1;r<R;r++){
    		for(int j =0;j<bl;j++){
    			Var_Obj[index++] = GIS[r][j];   			
    		}
    	}    	
    	IntVar Obj = mitm_aria.intVar("Obj", 1,keylimit);
    	mitm_aria.sum(Var_Obj, "=", Obj).post();
    	
    	//additional constraints
    	mitm_aria.sum(IS[0], "=", 1).post();    
    	mitm_aria.sum(DIS[R],"=",1).post();
    	
   
   	  Solver solver_aria = mitm_aria.getSolver();
       System.out.println(solver_aria.findOptimalSolution(Obj, false)); 
       
        
    }

	
}