package fr.inria.soctrace.tools.ocelotl.core.queries;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.lib.query.conditions.ICondition;
import fr.inria.soctrace.lib.query.conditions.LogicalCondition;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.LogicalOperation;

public class ConditionManager {

	List<ICondition> conditions = new ArrayList<ICondition>();

	public void addCondition(ICondition iCondition) {
		conditions.add(iCondition);
	}

	public void setWhere(IteratorQueries query) {
		if (conditions.size() == 0)
			return;
		else if (conditions.size() == 1)
			query.setElementWhere(conditions.get(0));
		else {
			LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
			for (ICondition c : conditions)
				and.addCondition(c);
			query.setElementWhere(and);
		}
		return;
	}

}