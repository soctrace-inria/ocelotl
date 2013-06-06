package fr.inria.soctrace.tools.paje.tracemanager.common.model;

import fr.inria.soctrace.lib.model.EventProducer;


public class PajeEventProducer extends EventProducer {
	
	private String alias="";

	public PajeEventProducer(int id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	
	public String getAlias() {
		return alias;
	}

	
	public void setAlias(String alias) {
		this.alias = alias;
	}
}
