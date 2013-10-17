/* ===========================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * under an aggregated representation form.
 *
 * (C) Copyright 2013 INRIA
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Generoso Pagano <generoso.pagano@inria.fr>
 */

package fr.inria.soctrace.tools.ocelotl.core.iaggregop;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class AggregationOperators {
	
	@SuppressWarnings("rawtypes")
	public List<Class>	List;
//	Reflections reflections = new Reflections();
//	Set<Class<? extends Object>> classes = reflections.getSubTypesOf(Object.class);

 
public AggregationOperators(java.util.List<Class> list) {
	super();
	try {
		List = getClasses("fr.inria.soctrace.tools.ocelotl.core.aggreop");
	} catch (ClassNotFoundException | IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

public List<Class> getList() {
	return List;
}

public Object newOperator(int i){
	try {
		return Class.forName(List.get(i).getName()).getConstructor().newInstance();
	} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return null;
}

/**
 * Cette méthode permet de lister toutes les classes d'un package donné
 * 
 * @param pckgname Le nom du package à lister
 * @return La liste des classes
 */
public List<Class> getClasses(String pckgname)	throws ClassNotFoundException, IOException {
	// Création de la liste qui sera retournée
	ArrayList<Class> classes = new ArrayList<Class>();
 
	// On récupère toutes les entrées du CLASSPATH
	String [] entries = System.getProperty("java.class.path")
					.split(System.getProperty("path.separator"));
 
	// Pour toutes ces entrées, on verifie si elles contiennent
	// un répertoire ou un jar
	for (int i = 0; i < entries.length; i++) {
 
		if(entries[i].endsWith(".jar")){
			classes.addAll(traitementJar(entries[i], pckgname));
		}else{
			classes.addAll(traitementRepertoire(entries[i], pckgname));
		}
 
	}
 
	return classes;
}



/**
 * Cette méthode retourne la liste des classes présentes
 * dans un répertoire du classpath et dans un package donné
 * 
 * @param repertoire Le répertoire où chercher les classes
 * @param pckgname Le nom du package
 * @return La liste des classes
 */
private Collection<Class> traitementRepertoire(String repertoire, String pckgname) throws ClassNotFoundException {
	ArrayList<Class> classes = new ArrayList<Class>();
 
	// On génère le chemin absolu du package
	StringBuffer sb = new StringBuffer(repertoire);
	String[] repsPkg = pckgname.split("\\.");
	for (int i = 0; i < repsPkg.length; i++) {
		sb.append(System.getProperty("file.separator") + repsPkg[i]);
	}
	File rep = new File(sb.toString());
 
	// Si le chemin existe, et que c'est un dossier, alors, on le liste
	if(rep.exists() && rep.isDirectory()){
		// On filtre les entrées du répertoire
		FilenameFilter filter = new DotClassFilter();
		File[] liste = rep.listFiles(filter );
 
		// Pour chaque classe présente dans le package, on l'ajoute à la liste
		for (int i = 0; i < liste.length; i++) {
			classes.add(Class.forName(pckgname + "." + liste[i].getName().split("\\.")[0]));
		}
	}
 
	return classes;
}
 
/**
 * Cette méthode retourne la liste des classes présentes dans un jar du classpath et dans un package donné
 *
 * @param repertoire Le jar où chercher les classes
 * @param pckgname Le nom du package
 * @return La liste des classes
 * @throws IOException 
 * @throws ClassNotFoundException 
 */
private static Collection<Class> traitementJar(String jar, String pckgname) throws IOException, ClassNotFoundException {
	ArrayList<Class> classes = new ArrayList<Class>();
 
	JarFile jfile = new JarFile(jar);
	String pkgpath = pckgname.replace(".", "/");
 
 
	// Pour chaque entrée du Jar
	for (Enumeration<JarEntry> entries = jfile.entries(); entries.hasMoreElements();) {
		JarEntry element = entries.nextElement();
 
		// Si le nom de l'entrée commence par le chemin du package et finit par .class
		if(element.getName().startsWith(pkgpath)
			&& element.getName().endsWith(".class")){
 
			String nomFichier = element.getName().substring(pckgname.length() + 1);
 
			classes.add(Class.forName(pckgname + "." + nomFichier.split("\\.")[0]));
 
		}
 
	}
 
	return classes;
}
 
/**
 * Cette classe permet de filtrer les fichiers d'un répertoire. Il n'accepte que les fichiers .class.
 */
private class DotClassFilter implements FilenameFilter{
 
	public boolean accept(File arg0, String arg1) {
		return arg1.endsWith(".class");
	}
 
 
}

}
