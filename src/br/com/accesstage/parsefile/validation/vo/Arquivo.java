package br.com.accesstage.parsefile.validation.vo; 

import br.com.accesstage.parsefile.ocorrencias.Ocorrencias;
import java.util.LinkedList;

import br.com.accesstage.parsefile.utils.Valida;

public class Arquivo implements java.io.Serializable
{ 
	private static final long serialVersionUID = 1L;
	
	private String arquivo;
    private String xmlLayout;
    private String idLayout;
    private LinkedList exceptions; 

    private String sender;
    private String receiver;
    private String doctype;
    private String dataGer;
    private String trackid;
    
    private boolean estouroLimite = false;

    private boolean delimitado = false;
    
    private Ocorrencias ocorrencias;
    
	private String pathArqValido;
	private String pathArqInvalido;
	private String pathArqLog;
	private String pathArqManual;
    
    private boolean erros;
    
    public void setArquivo(String value) {this.arquivo = value;}
    
    public String getArquivo() {
        return (Valida.isEmpty(this.arquivo)?"":this.arquivo);
    }
    
    public String getArquivoNome() {
        if ((this.arquivo == null) || ("".equals(this.arquivo.trim()))) {
            return "";
        }
        
        int pos = this.arquivo.lastIndexOf("/");
        if (pos == -1) {
            return this.arquivo;
        }
        return this.arquivo.substring(pos + 1);
    }
    
    public void setXmlLayout(String value) {this.xmlLayout = value;}
    
    public String getXmlLayout() {
        return (Valida.isEmpty(this.xmlLayout)?"":this.xmlLayout);
    }
    
    public String getXmlLayoutNome() {
        if ((this.xmlLayout == null) || ("".equals(this.xmlLayout.trim()))) {
            return "";
        }

        int pos = this.xmlLayout.lastIndexOf("/");
        if (pos == -1) {
            return this.xmlLayout;
        }
        return this.xmlLayout.substring(pos + 1);
    }
    
    
    public void setIdLayout(String value) {this.idLayout = value;}
    
    public String getIdLayout() {
        return (Valida.isEmpty(this.idLayout)?"":this.idLayout);
    }
    
    public void setExceptions(LinkedList value) {
        this.erros = (!Valida.isEmpty(value));
        this.exceptions = value;
    }
    
    public void cleanExceptions() {
        if (!Valida.isEmpty(this.exceptions)) {
            this.exceptions.clear();
        }
        this.exceptions = null;
    }
    
    public LinkedList getExceptions() {return this.exceptions;}
    //public boolean hasException() { return (!Valida.isEmpty(this.exceptions)); }
    
    public boolean hasException() { return this.erros; }
    
    // m�todos para trabalhar no Velocity
    public int getLinSize() {
        return this.exceptions.size();
    }
    public int getLinElements() {
        return (this.exceptions.size() - 1);
    }
    
    public LinExcept getLin(int index) {
        return (LinExcept)this.exceptions.get(index);
    }

    public int getColsSize(int indexLin) {
        return ((LinExcept)this.exceptions.get(indexLin)).getColsSize();
    }
    public int getColsElements(int indexLin) {
        return (((LinExcept)this.exceptions.get(indexLin)).getColsSize() - 1);
    }

    public ColExcept getCol(int indexLin, int indexCol) { 
        return ((LinExcept)this.exceptions.get(indexLin)).getCol(indexCol);
    }
    
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	public String getDoctype() {
		return doctype;
	}
	public void setDoctype(String doctype) {
		this.doctype = doctype;
	}
	public String getDataGer() {
		return dataGer;
	}
	public void setDataGer(String dataGer) {
		this.dataGer = dataGer;
	}
	public String getTrackid() {
		return trackid;
	}
	public void setTrackid(String trackid) {
		this.trackid = trackid;
	}
    public boolean isEstouroLimite() {
		return estouroLimite;
	}
	public void setEstouroLimite(boolean estouroLimite) {
		this.estouroLimite = estouroLimite;
	}
    public boolean isDelimitado() {
		return delimitado;
	}
	public void setDelimitado(boolean delimitado) {
		this.delimitado = delimitado;
	}
    
    public Ocorrencias getOcorrencias() {
		return ocorrencias;
	}

	public void setOcorrencias(Ocorrencias ocorrencias) {
		this.ocorrencias = ocorrencias;
	}
	
	public boolean isOcorrencias() {
		return !(this.ocorrencias == null);
	}

	public String getPathArqValido() {
		return pathArqValido;
	}
	public void setPathArqValido(String pathArqValido) {
		this.pathArqValido = pathArqValido;
	}
	public String getPathArqInvalido() {
		return pathArqInvalido;
	}
	public void setPathArqInvalido(String pathArqInvalido) {
		this.pathArqInvalido = pathArqInvalido;
	}
	public String getPathArqLog() {
		return pathArqLog;
	}
	public void setPathArqLog(String pathArqLog) {
		this.pathArqLog = pathArqLog;
	}
    public String getPathArqManual() {
		return pathArqManual;
	}
	public void setPathArqManual(String pathArqManual) {
		this.pathArqManual = pathArqManual;
	}

	public boolean isErros() {
		return erros;
	}
	public void setErros(boolean erros) {
		this.erros = erros;
	}
} 
