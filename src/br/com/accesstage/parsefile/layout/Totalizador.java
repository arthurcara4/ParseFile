/*
 * Created on 22/08/2008
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package br.com.accesstage.parsefile.layout;

import java.io.Serializable;

/**
 * @author glauco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Totalizador implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private long contadorRead;
	private long contadorWrite;
	private boolean problem;
	private String id;
	
	public Totalizador() {
		this.contadorRead = 0;
		this.contadorWrite = 0;
	}
	
	public Totalizador(String id, int start) {
		this.contadorRead = start;
		this.contadorWrite = start;
		this.id = id;
	}
	
	/*
	public Totalizador(String id, String tipo, String campos, int start) throws ParseFileException {
		
		if ((!Valida.isEmpty(campos)) && (!Valida.isEmpty(tipo))) {
    		// Erro do layout(totalizador): totalizador inválido (tentativa de totalizar campos e tipos de colunas)
			throw new ParseFileException(new StringBuffer("Erro do layout: totalizador (").append(id).append(") está com ambos os atributos <tipo> e <campo>. Só pode haver um!"));
		}
		
		if (!Valida.isEmpty(campos)) {
			
			String[] cps = campos.split("\\+");
			
			for( int i = 0; i < cps.length; i++) {
				String [] cp = cps[i].split(":");
				
				String tipos[] = cp[0].split("\\|");
				if (!Valida.isEmpty(tipos)) {
					for(int tp = 0; tp < tipos.length; tp++) {
						if (!Valida.isEmpty(tipos[tp])) {
							addTipo(tipos[tp]);
						}
					}
				}
			}
		}
		
		if (!Valida.isEmpty(tipo)) {
			String[] tps = tipo.split("\\+");
			
			for( int i = 0; i < tps.length; i++) {
				addTipo(tps[i]);
			}
		}
		
		if ((Valida.isEmpty(campos)) && (Valida.isEmpty(tipo))) {
			addTipo("#");
			//tipo = "#";
		}
		
		setId(id);
		//setTipo(tipo);
		setCampos(campos);
		this.contadorRead = start;
		this.contadorWrite = start;
	}
	
	public String getCampos() {
		return campos;
	}
	public void setCampos(String campos) {
		if (Valida.isEmpty(campos)) { return; }
		this.campos = campos;
	}
	*/
	
	public long getContadorRead() {
		return contadorRead;
	}
	public void setContadorRead(long contadorRead) {
		this.contadorRead = contadorRead;
	}
	public long getContadorWrite() {
		return contadorWrite;
	}
	public void setContadorWrite(long contadorWrite) {
		this.contadorWrite = contadorWrite;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	/*
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		// tipo generico
		if (Valida.isEmpty(tipo)) { tipo = "#"; }
		this.tipo = tipo;
	}
	*/
	
	
	public void addRead() {
		addRead(1);
	}
	public void addRead(long value) {
		this.contadorRead += value;
	}
	public void addWrite() {
		addWrite(1);
	}
	public void addWrite(long value) {
		this.contadorWrite += value;
	}
	/*
	private void addTipo(String tipo) {
		if (Valida.isEmpty(this.tipos)) {
			this.tipos = new StringBuffer("|");
		}
		if (this.tipos.indexOf(new StringBuffer("|").append(tipo).append("|").toString()) == -1) {
			this.tipos.append(tipo).append("|");
		}
	}
	
	public String[] getTipos() {
		if (Valida.isEmpty(this.tipos)) {
			return null;
		}
		return this.tipos.toString().substring(1,this.tipos.toString().length() - 1).split("\\|");
	}
	
	public ArrayList getCampos(String tipo) {
		if (Valida.isEmpty(campos)) {
			return null;
		}
		
		String[] cps = campos.split("\\+");
		
		ArrayList campos = new ArrayList();
		for( int i = 0; i < cps.length; i++) {
			String [] cp = cps[i].split(":");
			
			String tipos[] = cp[0].split("\\|");
			
			if (!Valida.isEmpty(tipos)) {
				for (int tp = 0; tp < tipos.length; tp++) {
					if (tipo.equals(tipos[tp])) {
						campos.add(cp[1]);
						tp = tipos.length;
					}
				}
			}
		}
		
		return campos;
	}
	
	public boolean hasTipo(String tipo) {
		if (Valida.isEmpty(this.tipos)) {
			return false;
		}
		return (this.tipos.indexOf(new StringBuffer("|").append(tipo).append("|").toString()) != -1);
	}
	*/
	
	public boolean isProblem() {
		return problem;
	}
	public void setProblem(boolean problem) {
		this.problem = problem;
	}
}
