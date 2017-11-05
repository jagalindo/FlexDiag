package helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;

import org.jenetics.BitChromosome;
import org.jenetics.BitGene;
import org.jenetics.Genotype;
import org.jenetics.engine.Engine;
import org.jenetics.engine.EvolutionResult;
import org.jenetics.util.Factory;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import es.us.isa.ChocoReasoner.ChocoResult;
import es.us.isa.FAMA.Benchmarking.PerformanceResult;
import es.us.isa.FAMA.Exceptions.FAMAException;
import es.us.isa.FAMA.models.featureModel.GenericFeature;
import es.us.isa.FAMA.models.featureModel.Product;

public class ChocoPureExplainErrorEvolutionary  {

	public boolean returnAllPossibeExplanations = false;
	
	private static Map<String, Constraint> relations = null;

	public Map<String, Constraint> result = new HashMap<String, Constraint>();
	
	static ChocoModel chReasoner;
	static Map<String, Constraint> productConstraint= new HashMap<String, Constraint>();
	static Map<String, Constraint> requirementConstraint= new HashMap<String, Constraint>();
	static ArrayList<String> pc;
	Product s,r;

	public void setConfiguration(Product s) {
		this.s=s;
	}

	public void setRequirement(Product r) {
		this.r=r;
	}

	public void setProduct(Product p) {
		this.s = p;
	}


	public boolean isValid() {
		return false;
	}

	public static Integer eval(Genotype<BitGene> gt) {
		int res = Integer.MAX_VALUE;

		try {
			int i = 0;

			Model p = new CPModel();
			for(IntegerVariable v:chReasoner.variables.values()){
				p.addVariables(v);
			}

			//Add model constraints
			for(Entry<Integer,Constraint> e:chReasoner.getConstraints().entrySet()){
				p.addConstraint(e.getValue());
			}
			
			//Add requirement constraints
			for(Constraint c: requirementConstraint.values()){
				p.addConstraints(c);	
			}
			
					
			//Add product constraints if the gene is set to true
			Iterator<BitGene> iterator = gt.getChromosome().iterator();
			while (iterator.hasNext()) {
				if (iterator.next().booleanValue()) {
					p.addConstraint(productConstraint.get(pc.get(i)));
				}
				i++;
			}
			
			Solver s = new CPSolver();
			s.read(p);
			s.solve();
			
			if (!s.isFeasible()) {
				res = gt.getChromosome().as(BitChromosome.class).bitCount();
			}
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		//System.out.println(res);

		return res;
	}

	public PerformanceResult answer(ChocoModel r) throws FAMAException {

		ChocoResult res = new ChocoResult();
		chReasoner =  r;
		pc= new ArrayList<String>();	
		
		for (GenericFeature f : this.s.getFeatures()) {
			IntegerVariable var = chReasoner.getVariables().get(f.getName());
			productConstraint.put("U_" + f.getName(), Choco.eq(var, 1));
			pc.add("U_" + f.getName());
		}

		for (GenericFeature f : this.r.getFeatures()) {
			IntegerVariable var = chReasoner.getVariables().get(f.getName());
			requirementConstraint.put("R_" + f.getName(), Choco.eq(var, 1));
		}
		
		// 1.) Define the genotype (factory) suitable
		// for the problem.
		Factory<Genotype<BitGene>> gtf = Genotype.of(BitChromosome.of(pc.size(), 0.1));

		// 3.) Create the execution environment.
		Engine<BitGene, Integer> engine = Engine.builder(ChocoPureExplainErrorEvolutionary::eval, gtf)
				.populationSize(10)
		//		.optimize(Optimize.MINIMUM)
				.minimizing()
				.executor((Executor) Runnable::run).build();

		// 4.) Start the execution (evolution) and collect the result.
		Genotype<BitGene> result = engine.stream().limit(100).collect(EvolutionResult.toBestGenotype());

		for (int g = 0; g < result.getChromosome().length(); g++) {
			if (result.getChromosome().getGene(g).booleanValue()) {
				String C = pc.get(g);
				this.result.put(C,productConstraint.get(C));
			}
		}

		return res;

	}

	public static Map<String, Constraint> getRelations() {
		return relations;
	}

	public static void setRelations(Map<String, Constraint> relations) {
		ChocoPureExplainErrorEvolutionary.relations = relations;
	}

	

}
