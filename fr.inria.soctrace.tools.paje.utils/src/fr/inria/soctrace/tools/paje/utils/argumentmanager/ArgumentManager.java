package fr.inria.soctrace.tools.paje.utils.argumentmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;

public class ArgumentManager implements IArgumentManager {

	protected Map<String, String>	options		= new HashMap<String, String>();
	protected String[]				args;
	protected List<String>			defaults	= new ArrayList<String>();			;

	public ArgumentManager(final String[] args) throws SoCTraceException {
		super();
		this.args = args;
	}

	@Override
	public void debugArgs() {
		final Iterator<Entry<String, String>> it = options.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry<String, String> pairs = it.next();
			System.out.println(pairs.getKey() + " = " + pairs.getValue());
		}
		System.out.println("defaults =");
		for (final String it2 : defaults)
			System.out.println(it2);
		printArgs();
	}

	@Override
	public void parseArgs() throws SoCTraceException {

		for (int i = 0; i < args.length; i++)
			switch (args[i].charAt(0)) {
			case '-':
				if (args[i].length() < 2)
					throw new IllegalArgumentException("Not a valid argument: " + args[i]);
				if (args[i].charAt(1) == '-')
					throw new IllegalArgumentException("Not a valid argument: " + args[i]);
				if (args.length - 1 == i)
					throw new IllegalArgumentException("Expected arg after: " + args[i]);
				options.put(args[i].substring(1), args[i + 1]);
				i++;
				break;
			default:
				defaults.add(args[i]);
			}
	}

	@Override
	public void printArgs() {
	}

	@Override
	public void processArgs() throws SoCTraceException {
	}

}
