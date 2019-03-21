package br.com.accesstage.parsefile.exceptions;

public class ParseFileException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int TAMANHO_INVALIDO = 1;
	public static final int TIPO_INVALIDO = 2;
	public static final int FORMATO_INVALIDO = 3;
	public static final int SQL_EXCEPTION = 4;
	public static final int BATCH_EXCEPTION = 5;
	
	private int codigoErro;
	private String idColuna;
	private String idLinha;
	private String tipoLinha;
	
	

	public ParseFileException() {
	}
	
	public ParseFileException(int codigoErro, String idColuna, String message, String idLinha, String tipoLinha) {
		super(message);
		setCodigoErro(codigoErro);
		setIdColuna(idColuna);
		setIdLinha(idLinha);
		setTipoLinha(tipoLinha);
	}
	
	public ParseFileException(int codigoErro, StringBuffer message) {
		super(message.toString());
		setCodigoErro(codigoErro);
	}
	
	public ParseFileException(int codigoErro, String message) {
		super(message.toString());
		setCodigoErro(codigoErro);
	}
	
	public ParseFileException(StringBuffer message) {
		super(message.toString());
	}

	public ParseFileException(String message) {
		super(message);
	}

	public ParseFileException(Throwable cause) {
		super(cause);
	}

	public ParseFileException(String message, Throwable cause) {
		super(message, cause);
	}
	
	

	public int getCodigoErro() {
		return codigoErro;
	}
	public void setCodigoErro(int codigoErro) {
		this.codigoErro = codigoErro;
	}
	public String getIdColuna() {
		return idColuna;
	}
	public void setIdColuna(String idColuna) {
		this.idColuna = idColuna;
	}

	public String getIdLinha() {
		return idLinha;
	}

	public void setIdLinha(String idLinha) {
		this.idLinha = idLinha;
	}

	public String getTipoLinha() {
		return tipoLinha;
	}

	public void setTipoLinha(String tipoLina) {
		this.tipoLinha = tipoLina;
	}
	
	public String getAtributeMsg(String att) {
		String m = this.getMessage();
		
		if (m == null) return "";
		
		int posi =  m.indexOf(att);
		
		if (posi == -1) return m;
		posi += att.length() + 1;
		
		int posf = m.indexOf("]", posi );
		
		if (posf == -1) return m; 
		
		return m.substring(posi, posf);
	
	}
	
}
