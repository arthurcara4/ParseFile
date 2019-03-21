package br.com.accesstage.parsefile.validation.vo; 

import br.com.accesstage.parsefile.utils.ParseUtils;
import br.com.accesstage.parsefile.utils.Valida;

public class ColExcept implements java.io.Serializable
{ 
	private static final long serialVersionUID = 1L;

	private String conteudo;
    private String msg;
    private int posIni;
    private int tamanho;
    private String id;
    
    public ColExcept() {
    }
    
    public ColExcept(String conteudo) {
        this.conteudo = conteudo;
    }
    
    public ColExcept(String conteudo, String msg) {
        this.conteudo = conteudo;
        this.msg = msg;
    }
    
    public void setMsg(String value) { this.msg = value; }
    public String getMsg() { 
        if (this.msg == null) return "";
        return this.msg; 
    }
    
    public String getTextoMsg() { 
        return ParseUtils.subString(this.msg,"msg[", "]");
    }
    
    public boolean hasMsg() { return !Valida.isEmpty(this.msg); }
    
    public void setConteudo(String value) { this.conteudo = value; }
    public String getConteudo() { return this.conteudo; }
    public String getConteudoHtml() { return toHtml(this.conteudo); }
    
    public void setPosIni(int value) { this.posIni = value; }
    public int getPosIni() { return this.posIni; }

    public void setTamanho(int value) { this.tamanho = value; }
    public int getTamanho() { return this.tamanho; }
    
    public boolean hasExcept() {
        return (!Valida.isEmpty(this.msg));
    }
    
    private String toHtml(String source) {
        source = source.replaceAll(" ","&nbsp;");
        return source;
    }
    
    public void setId(String value) { this.id = value; }
    public String getId() { 
        if (id == null) return "";
        return this.id; 
    }
    
    
} 
