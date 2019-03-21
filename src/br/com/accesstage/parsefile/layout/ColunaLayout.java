package br.com.accesstage.parsefile.layout;

import java.io.Serializable;

import br.com.accesstage.parsefile.utils.ParseUtils;
import br.com.accesstage.parsefile.utils.Valida;


public class ColunaLayout implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String id;
	private String tipo;
	private int posIni;
	private int tamanho;
	private String formato;
	private String pad;
	private String padSide;
	private String strDefault;
	private String descricao;
	private String dominio;
	private boolean obrigatorio;
	private int decimais;
	private boolean upper = false;
	private boolean lower = false;
	private String totalizador;
	private boolean zerar = true;
	private String sepDecimais;
	private String zerarTotalizadores;
	private String sqlExec;
	private String setVariavel;
	private String getVariavel;
	private int numericoCompleto = -1;
	
	
	
	public ColunaLayout() {}
	
	public ColunaLayout(String id
			, String tipo
			, int posIni
			, int tamanho
			, String descricao
			, String strDefault
			, String pad
			, String padSide
			, String formato
			, String dominio
			, boolean obrigatorio
			, int decimais
			, boolean upper
			, boolean lower
			, String totalizador
			, boolean zerar
			, String sepDecimais
			, String zerarTotalizadores
			, String sqlExec
			, String setVariavel
			, String getVariavel
			, int numericoCompleto
			) {
		setId(id);
		setTipo(tipo);
		setPosIni(posIni);
		setTamanho(tamanho);
		setFormato(formato);
		setPad(pad);
		setPadSide(padSide);
		setStrDefault(strDefault);
		setDescricao(descricao);
		setDominio(dominio);
		setObrigatorio(obrigatorio);
		setDecimais(decimais);
		setUpper(upper);
		setLower(lower);
		setTotalizador(totalizador);
		setZerar(zerar);
		setSepDecimais(sepDecimais);
		setZerarTotalizadores(zerarTotalizadores);
		setSqlExec(sqlExec);
		setSetVariavel(setVariavel);
		setGetVariavel(getVariavel);
		setNumericoCompleto(numericoCompleto);
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		if (Valida.isEmpty(id)) { return; }
		this.id = id;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		if (Valida.isEmpty(tipo)) { return; }
		
		this.tipo = tipo.toUpperCase();
		
		if (LinhaLayout.ALFA.equals(this.tipo)) {
			setPad(" ");
			setPadSide(ParseUtils.DIREITA);
			setObrigatorio(true);
		}
		
		if (LinhaLayout.NUMERICO.equals(this.tipo)) {
			setPad("0");
			setPadSide(ParseUtils.ESQUERDA);
			setObrigatorio(false);
		}

		if (LinhaLayout.DATA.equals(this.tipo)) {
			setPad("0");
			setPadSide(ParseUtils.ESQUERDA);
			setObrigatorio(false);
		}
	}
	public int getPosIni() {
		return posIni;
	}
	public void setPosIni(int posIni) {
		this.posIni = posIni - 1;
	}
	public int getTamanho() {
		return tamanho;
	}
	public void setTamanho(int tamanho) {
		this.tamanho = tamanho;
	}
	public String getFormato() {
		return formato;
	}
	public void setFormato(String formato) {
		if (Valida.isEmpty(formato)) { return; }
		this.formato = formato;
	}

	public String getPad() {
		return pad;
	}

	public void setPad(String pad) {
		if ((Valida.isNull(pad)) || ("".equals(pad))) { return; }
		this.pad = pad;
	}

	public String getPadSide() {
		return padSide;
	}
	
	public void setPadSide(String padSide) {
		if (Valida.isEmpty(padSide)) { return; }
		this.padSide = padSide;
	}
	
	public String getStrDefault() {
		return strDefault;
	}

	public void setStrDefault(String strDefault) {
		this.strDefault = ((strDefault == null)?"":strDefault);
	}
	
	public String getDescricao() {
		return descricao;
	}
	public void setDescricao(String descricao) {
		if (Valida.isEmpty(descricao)) { 
			descricao = getId();
		}
		this.descricao = descricao;
	}
	
	public String getDominio() {
		return dominio;
	}
	public void setDominio(String dominio) {
		if (Valida.isEmpty(dominio)) { return; }
		this.dominio = dominio;
	}
	
	public boolean isObrigatorio() {
		return obrigatorio;
	}
	public void setObrigatorio(boolean obrigatorio) {
		this.obrigatorio = obrigatorio;
	}
	
	
	public int getDecimais() {
		return decimais;
	}
	public void setDecimais(int decimais) {
		this.decimais = decimais;
	}
	
	
	public boolean isLower() {
		return lower;
	}
	public void setLower(boolean lower) {
		this.lower = lower;
	}
	public boolean isUpper() {
		return upper;
	}
	public void setUpper(boolean upper) {
		this.upper = upper;
	}
	
	public String getTotalizador() {
		return totalizador;
	}
	public void setTotalizador(String totalizador) {
		if (Valida.isEmpty(totalizador)) { return; }
		this.totalizador = totalizador;
		
		if (Valida.isEmpty(this.strDefault)) {
			setStrDefault("#tot#");
		}
	}
	public boolean isZerar() {
		return zerar;
	}
	public void setZerar(boolean zerar) {
		if (Valida.isEmpty(totalizador)) { zerar = false; }
		this.zerar = zerar;
	}

	public String getSepDecimais() {
		return sepDecimais;
	}

	public void setSepDecimais(String sepDecimais) {
		this.sepDecimais = sepDecimais;
	}

	public String getZerarTotalizadores() {
		return zerarTotalizadores;
	}

	public void setZerarTotalizadores(String zerarTotalizadores) {
		this.zerarTotalizadores = zerarTotalizadores;
	}

	public String getSqlExec() {
		return sqlExec;
	}

	public void setSqlExec(String sqlExec) {
		this.sqlExec = sqlExec;
	}
	
	public boolean isSqlExec() {
		return !Valida.isEmpty(this.sqlExec);
	}

	public String getSetVariavel() {
		return setVariavel;
	}

	public void setSetVariavel(String setVariavel) {
		this.setVariavel = setVariavel;
	}

	public String getGetVariavel() {
		return getVariavel;
	}

	public void setGetVariavel(String getVariavel) {
		this.getVariavel = getVariavel;
	}

	public int getNumericoCompleto() {
		return numericoCompleto;
	}

	public void setNumericoCompleto(int numericoCompleto) {
		this.numericoCompleto = numericoCompleto;
	}
	
}
