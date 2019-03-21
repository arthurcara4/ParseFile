package br.com.accesstage.parsefile.validation.vo; 

import java.util.LinkedList;

public class LinExcept implements java.io.Serializable
{ 
	private static final long serialVersionUID = 1L;
    
	private int numero;
    private LinkedList cols; 
    private String linha; 

    public LinExcept() {
    }

    public LinExcept(int numero) {
        this.numero = numero;
    }
    
    public void setNumero(int value) { this.numero = value; }
    public int getNumero() { return this.numero; }
    
    public void addCol(ColExcept col) {
        if (this.cols == null) {
            cols = new LinkedList();
        }
        cols.add(col);
    }
    public LinkedList getCols() { return this.cols; }
    
    // métodos para trabalhar no Velocity
    public int getColsSize() {return this.cols.size();}
    
    public ColExcept getCol(int index) {return (ColExcept)this.cols.get(index);}

    
	public String getLinha() {
		return linha;
	}

	public void setLinha(String linha) {
		this.linha = linha;
	}
    
    
} 
