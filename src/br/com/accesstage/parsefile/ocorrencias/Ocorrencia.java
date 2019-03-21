package br.com.accesstage.parsefile.ocorrencias;

import java.io.Serializable;

public class Ocorrencia implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String id;
	private int ok;
	private int erro;
	private int total;
	
	public Ocorrencia() {}
	
	public Ocorrencia(String id, boolean isErro) {
		this.id = id;
		ok = 0;
		erro = 0;
		total = 0;
		
		add(isErro);
	}
	
	public void add(boolean isErro) {
		if (isErro) {
			erro++;
		} else {
			ok++;
		}
		total++;
	}
	
	public String getId() {
		return id;
	}
	
	public String getId(int idx) {
		String ids[] =  id.split("\\|");
		
		if (idx < ids.length) {
			return ids[idx];
		}
		
		return "";
	}
	
	public void setId(String id) {
		this.id = id;
	}
	public int getOk() {
		return ok;
	}
	public void setOk(int ok) {
		this.ok = ok;
	}
	public int getErro() {
		return erro;
	}
	public void setErro(int erro) {
		this.erro = erro;
	}
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	

}
