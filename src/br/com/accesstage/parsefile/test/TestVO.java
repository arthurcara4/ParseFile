/*
 * Created on Apr 24, 2009
 *
 */
package br.com.accesstage.parsefile.test;

import java.util.LinkedList;

public class TestVO {
    private String arquivo;
    private String xmlLayout;
    private String idLayout;
    private LinkedList exceptions;
    
	public String getArquivo() {
		return arquivo;
	}
	public void setArquivo(String arquivo) {
		this.arquivo = arquivo;
	}
	public String getXmlLayout() {
		return xmlLayout;
	}
	public void setXmlLayout(String xmlLayout) {
		this.xmlLayout = xmlLayout;
	}
	public String getIdLayout() {
		return idLayout;
	}
	public void setIdLayout(String idLayout) {
		this.idLayout = idLayout;
	}
	public LinkedList getExceptions() {
		return exceptions;
	}
	public void setExceptions(LinkedList exceptions) {
		this.exceptions = exceptions;
	}
    
    
    
}
