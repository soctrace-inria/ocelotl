package fr.inria.soctrace.tools.ocelotl.core.microdesc;

public class MicroscopicDescriptionTypeResource {

	protected String type;
	protected String name;
	protected String microModelClass;
	protected String bundle;

	public MicroscopicDescriptionTypeResource() {
		// TODO Auto-generated constructor stub
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String aType) {
		type = aType;
	}
	
	public String getMicroModelClass() {
		return microModelClass;
	}

	public void setMicroModelClass(String microModelClass) {
		this.microModelClass = microModelClass;
	}
	
	public String getBundle() {
		return bundle;
	}

	public void setBundle(String bundle) {
		this.bundle = bundle;
	}

}
