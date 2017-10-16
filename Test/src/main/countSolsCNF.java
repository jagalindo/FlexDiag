package main;

import choco.cp.solver.CPSolver;
import choco.kernel.solver.Solver;
import helpers.ChocoModel;

public class countSolsCNF {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ChocoModel mod = new ChocoModel();
		mod.parseFile("./input/automotive_data/01.cnf");
		
		Solver s = new CPSolver();
		s.read(mod.getModel());
		s.solveAll();
	//	System.out.println(s.gets);
		System.out.println(s.getNbIntVars()+" "+s.getNbIntConstraints()+" "+s.getNbSolutions());
	}

}
