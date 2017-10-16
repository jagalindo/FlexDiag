package helpers;

import static choco.Choco.makeIntVar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import es.us.isa.FAMA.models.FAMAfeatureModel.Feature;

public class ChocoModel {

	Map<Integer, IntegerVariable> variables;
	Map<Integer, Constraint> constraints;
	Collection<Collection<Integer>> excludesPairs;
	Model model;

	public void parseFile(String path) {
		File file = new File(path);
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			int lineNo = 0;

			while ((line = br.readLine()) != null) {
				if (!line.equals("")) {
					String[] st = line.split(" ");
					if (lineNo == 0) {
						createModel(Integer.parseInt(st[2]));
					} else {
						Constraint c = createConstraint(st);
						this.calculateExcludePairs(st);
						if (c == null) {
							throw new IllegalStateException(line);
						}
						model.addConstraint(c);
						this.constraints.put(lineNo, c);
					}
				}
				lineNo++;

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void calculateExcludePairs(String[] cnfLine) {

		Collection<Integer> conflictset = new ArrayList<Integer>();
		for (int i = 0; i < cnfLine.length - 1; i++) {
			int parseInt = Integer.parseInt(cnfLine[i]);
			if (parseInt < 0) {
				conflictset.add(Math.abs(parseInt));
			}
		}
		if (conflictset.size() > 1) {
			this.excludesPairs.add(conflictset);
		}

	}

	public Collection<Integer> getMaxConfictSet() {
		ArrayList<Collection<Integer>> res = new ArrayList<Collection<Integer>>(excludesPairs);
		
		int maxConfict = 0;
		for (Collection<Integer> c : res) {
			if (c.size() > maxConfict) {
				maxConfict = c.size();
			}
		}
		Iterator<Collection<Integer>> it = res.listIterator();
		while(it.hasNext()){
			Collection<Integer> next = it.next();
			if(next.size()<maxConfict){
				it.remove();
			}
		}
		if (res.size() > 0) {
			// if there is only one max return that one; else, a random in the
			// max
			Random r = new Random();
			return res.get(r.nextInt(res.size()));
		} else {
			return new ArrayList<Integer>();
		}
		
	}

	private void createModel(int variables) {
		model = new CPModel();

		this.variables = new HashMap<Integer, IntegerVariable>();
		this.constraints = new HashMap<Integer, Constraint>();
		this.excludesPairs = new ArrayList<Collection<Integer>>();

		for (int i = 1; i <= variables; i++) {
			IntegerVariable var = makeIntVar("" + i, 0, 1);
			this.variables.put(i, var);
			model.addVariable(var);
		}

	}

	private Constraint createConstraint(String[] cnfLine) {
		Constraint res = null;

		for (int i = 0; i < cnfLine.length - 1; i++) {

			if (i == 0) {
				res = parseConstraintOfVariable(cnfLine[i]);
			} else {
				res = Choco.or(res, parseConstraintOfVariable(cnfLine[i]));
			}

		}

		return res;
	}

	private Constraint parseConstraintOfVariable(String var) {
		Integer i = Integer.parseInt(var);
		Integer abs = Math.abs(i);
		if (i < 0) {
			return Choco.not(Choco.eq(variables.get(abs), 1));
		} else {
			return Choco.eq(variables.get(abs), 1);
		}
	}

	public boolean isValidProduct(Collection<Integer> prod) {
		boolean res = false;
		Collection<Constraint> ctmp = new LinkedList<Constraint>();

		for (Integer i : prod) {
			Constraint c = Choco.eq(variables.get(i), 1);
			ctmp.add(c);
			model.addConstraint(c);
		}
		Solver s = new CPSolver();
		s.read(model);
		s.solve();
		res = s.isFeasible();
		for (Constraint c : ctmp) {
			model.removeConstraint(c);
		}
		return res;
	}

	public boolean isValid() {
		Solver s = new CPSolver();
		s.read(model);
		s.solve();
		return s.isFeasible();
	}

	public Map<Integer, IntegerVariable> getVariables() {
		return variables;
	}

	public void setVariables(Map<Integer, IntegerVariable> variables) {
		this.variables = variables;
	}

	public Map<Integer, Constraint> getConstraints() {
		return constraints;
	}

	public void setConstraints(Map<Integer, Constraint> constraints) {
		this.constraints = constraints;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

}
