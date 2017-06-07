import java.io.IOException;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;

public class testCNF {

	public static void main(String[] args) throws ParseFormatException, IOException, ContradictionException {
		
		   ISolver solver = SolverFactory.newDefault();
	        solver.setTimeout(3600); // 1 hour timeout
	        Reader reader = new DimacsReader(solver);
	        // CNF filename is given on the command line 
            IProblem problem = reader.parseInstance("./input/automotive_data/01.cnf");
            System.out.println(problem);
		
		
	}

}
