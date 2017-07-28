import java.io.IOException;

import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;

import helpers.ChocoModel;

public class testCNF {

	public static void main(String[] args) throws ParseFormatException, IOException, ContradictionException {
		
		
			ChocoModel m = new ChocoModel();
			m.parseFile("./input/automotive_data/01.cnf");
			System.out.println(m.getVariables().size()+" "+m.getConstraints().size());
	}
	
	
	

}
