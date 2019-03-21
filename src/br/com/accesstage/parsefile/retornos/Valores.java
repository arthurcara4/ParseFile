package br.com.accesstage.parsefile.retornos;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import br.com.accesstage.parsefile.exceptions.ParseFileException;
import br.com.accesstage.parsefile.layout.ColunaLayout;
import br.com.accesstage.parsefile.utils.ParseUtils;
import br.com.accesstage.parsefile.utils.Valida;

/**
 * Valores é um objeto do ParseFile que contem todas as colunas e informações necessárias de determinada linha 
 * 
 * @author      Glauco A. Barroso
 */
public class Valores implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private StringBuffer mapValores;
	private String valores[];
	private ParseFileException exceptions[];
	private ArrayList colunas;
	private String linhaId;
	private String linhaTipo;
	//private StringBuffer mapTotalizadores;
	
	public Valores() {}
	
	/**
	 * Construtor utilizado pelo componente ParseFile para instanciar uma classe de Valores configurada 
	 * 
	 * @param mapValores <br>mapeamento dos conteúdos através de chaves<br><br>
	 * @param valores <br>Array de conteudos String referentas aos conteúdos reais das colunas<br><br>
	 * @param linhaId <br>Identificador da linha. Atributo id da tag <linha> do layout xml<br><br>
	 * @param linhaTipo <br>Tipo da linha. Atributo tipo da tag <linha> do layout xml<br><br>
	 * @param colunas <br>Array de objetos ColunaLayout contendo todas as informações referentes as colunas da linha<br><br>
	 * @param exceptions <br>Array de Exception ocorridas durante o parse da linha. Esse array só em preenchido caso o ParseFile esteja setado para logar as Exceções<br><br>
	 * @param mapTotalizadores <br>Um mapa dos totalizadores do layout para serem atualizados durante o processo de parse<br><br>
	 */
	public Valores(StringBuffer mapValores, String[] valores, String linhaId, String linhaTipo, ArrayList colunas, ParseFileException exceptions[], StringBuffer mapTotalizadores) {
		this.mapValores = mapValores;
		this.valores = valores;
		this.exceptions = exceptions;
		this.colunas = colunas;
		setLinhaId(linhaId);
		setLinhaTipo(linhaTipo);
		//this.mapTotalizadores = mapTotalizadores;
	}
	
	/**
	 * Retorna um Array dos conteúdos reais da linha
	 * 
	 * @return String[] <br>Array de conteúdos da linha<br><br>
	 */
	public String[] getValores() {
		return valores;
	}
	
	/**
	 * Retorna o conteudo de uma coluna em formato String
	 * 
	 * @param idColuna <br>Identificador da coluna. Atributo id da tag <coluna> no layout xml<br><br>
	 * @return String <br>Conteudo em String da coluna selecionada<br><br>
	 * @throws ParseFileException
	 */
	public String getString(String idColuna) throws ParseFileException {
		return valores[getPos(idColuna)];
	}
	
	/**
	 * @param idColunas <br>Identificadores das colunas separados por virgula. Atributos id das tags <coluna> no layout xml<br><br>
	 * @return String <br>Conteudo em String das colunas selecionadas concatenados<br><br>
	 * @throws ParseFileException
	 */
	public String getStringConcat(String idColunas) throws ParseFileException {
		String cols[] = idColunas.split(",");
		
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < cols.length; i++) {
	        sb.append(valores[getPos(cols[i].trim())] );
        }

		return sb.toString();
	}
	
	public Date getData(String idColuna, String fmtEntrada) throws ParseFileException {
		try {
			return new SimpleDateFormat(fmtEntrada).parse(valores[getPos(idColuna)]);
		} catch (Exception e) {
			throw new ParseFileException("Problemas converter data no formato de entrada " + fmtEntrada);
		}
	}
	
	public String getData(String idColuna, String fmtEntrada, String fmtSaida) throws ParseFileException {
		try {
			return new SimpleDateFormat(fmtSaida).format(getData(idColuna,fmtEntrada));
		} catch (Exception e) {
			throw new ParseFileException("Problemas converter data no formato de entrada " + fmtEntrada + " ou de saida " + fmtSaida);
		}
	}
	
	/**
	 * Retorna o conteudo de uma coluna convertido para long
	 * 
	 * @param idColuna <br>Identificador da coluna. Atributo id da tag <coluna> no layout xml<br><br>
	 * @return long <br>Conteudo long da coluna selecionada<br><br>
	 * @throws ParseFileException
	 */
	public long getLong(String idColuna) throws ParseFileException {
		if ("".equals(getString(idColuna).trim())) {
			return 0;
		}
		return Long.parseLong(getString(idColuna));
	}

	/**
	 * Retorna o conteudo de uma coluna convertido para int
	 * 
	 * @param idColuna <br>Identificador da coluna. Atributo id da tag <coluna> no layout xml<br><br>
	 * @return long <br>Conteudo int da coluna selecionada<br><br>
	 * @throws ParseFileException
	 */
	public int getInt(String idColuna) throws ParseFileException {
		if ("".equals(getString(idColuna).trim())) {
			return 0;
		}
		return Integer.parseInt(getString(idColuna).trim());
	}
	
	/**
	 * Retorna o conteudo de uma coluna convertido para double
	 * 
	 * @param idColuna <br>Identificador da coluna. Atributo id da tag <coluna> no layout xml<br><br>
	 * @return long <br>Conteudo int da coluna selecionada<br><br>
	 * @throws ParseFileException
	 */
	public double getDouble(String idColuna) throws ParseFileException {
		return getDouble(idColuna, 1);
	}
	
	public double getDouble(String idColuna, int divisor) throws ParseFileException {
		if ("".equals(getString(idColuna).trim())) {
			return 0;
		}
		return (Double.parseDouble((getString(idColuna).trim())) / divisor);
	}
	
	
	/**
	 * Verifica se hove alguma exceção durante o parse dessa linha
	 * 
	 * @return boolean <br><b>true</b> Ocorreram exceções durante o parse da linha<br><b>false</b> Não ocorreram exceções<br><br>
	 * @throws ParseFileException
	 */
	public boolean hasException() throws ParseFileException {
		return (exceptions != null);
	}

	/**
	 * Seta um array de exceções para o objeto Valores 
	 * 
	 * @param exceptions <br>Array de exceções<br><br>
	 * @throws ParseFileException
	 */
	public void setExceptions(ParseFileException[] exceptions) throws ParseFileException {
		this.exceptions = exceptions;
	}

	/**
	 * Retorna um Array com as exceções ocorridas durante o Parse da linha 
	 * 
	 * @return ParseFileException[] <br>Array de exceções<br><br>
	 * @throws ParseFileException
	 */
	public ParseFileException[] getExceptions() throws ParseFileException {
		return exceptions;
	}
	
	/**
	 * Retorna a exceção ocorrida em determinada coluna da linha
	 * 
	 * @param idColuna <br>Identificador da coluna. Atributo id da tag <coluna> no layout xml<br><br>
	 * @return ParseFileException <br>Exceção ocorrida na coluna<br><br>
	 * @throws ParseFileException
	 */
	public ParseFileException getException(String idColuna) throws ParseFileException {
		if ((this.exceptions == null) || (this.exceptions.length == 0))  {
			return null;
		}
		return exceptions[getPos(idColuna)];
	}
	
	/**
	 * Retorna a exceção ocorrida em determinada coluna da linha
	 * 
	 * @param index <br>Indice da coluna<br><br>
	 * @return ParseFileException <br>Exceção ocorrida na coluna<br><br>
	 * @throws ParseFileException
	 */
	public ParseFileException getException(int index) throws ParseFileException {
		if ((this.exceptions == null) || (this.exceptions.length == 0))  {
			return null;
		}
		return this.exceptions[index];
		
	}
	
	/**
	 * Retrona o Indice de determinada coluna no objeto Valores
	 * 
	 * @param idColuna <br>Identificador da coluna. Atributo id da tag <coluna> no layout xml<br><br>
	 * @return int <br>Indice da coluna<br><br>
	 * @throws ParseFileException
	 */
	private int getPos(String idColuna) throws ParseFileException {
		int pos = ParseUtils.getPos(this.mapValores, idColuna);
		
		if (pos == -1) {
    		// Linha (linhaTipo): Erro na tentativa de ler uma coluna inexistente  
			StringBuffer msg = new StringBuffer();
			msg.append("linha:").append(this.linhaId).append(" tipo:").append(this.linhaTipo).append(" msg->Coluna (").append(idColuna).append(") não existe. (Classe Valores)") ;
			throw new ParseFileException(msg.toString());
		}
		
		return pos;
	}
	
	
	/**
	 * Seta um valor String em determinada coluna do objeto Valores
	 * 
	 * @param idColuna <br>Identificador da coluna. Atributo id da tag <coluna> no layout xml<br><br>
	 * @param value <br>Valor String a ser setado na coluna<br><br>
	 * @throws ParseFileException
	 */
	public void set(String idColuna, String value) throws ParseFileException {
		valores[getPos(idColuna)] = value;
	}
	
	/**
	 * Seta um valor long em determinada coluna do objeto Valores
	 * 
	 * @param idColuna <br>Identificador da coluna. Atributo id da tag <coluna> no layout xml<br><br>
	 * @param value <br>Valor long a ser setado na coluna<br><br>
	 * @throws ParseFileException
	 */
	public void set(String idColuna, long value) throws ParseFileException {
		set(idColuna, String.valueOf(value));
	}

	/**
	 * Seta um valor int em determinada coluna do objeto Valores
	 * 
	 * @param idColuna <br>Identificador da coluna. Atributo id da tag <coluna> no layout xml<br><br>
	 * @param value <br>Valor int a ser setado na coluna<br><br>
	 * @throws ParseFileException
	 */
	public void set(String idColuna, int value) throws ParseFileException {
		set(idColuna, String.valueOf(value));
	}
	
	/**
	 * Seta um valor date em determinada coluna do objeto Valores
	 * 
	 * @param idColuna <br>Identificador da coluna. Atributo id da tag <coluna> no layout xml<br><br>
	 * @param value <br>Valor date a ser setado na coluna<br><br>
	 * @param formato <br>Foramto em que a data será setada no objeto. Ex: dd/MM/yyyy<br><br>
	 * @throws ParseFileException
	 */
	public void set(String idColuna, Date value, String formato) throws ParseFileException {
		set(idColuna, new SimpleDateFormat(formato).format(value));
	}
	
	/**
	 * Retorna as informações configuradas no layout xml referentes a coluna selecionada
	 * 
	 * @param idColuna <br>Identificador da coluna. Atributo id da tag <coluna> no layout xml<br><br>
	 * @return ColunaLayout <br>Informações referentes a coluna selecionada<br><br>
	 * @throws ParseFileException
	 */
	public ColunaLayout getColuna(String idColuna) throws ParseFileException {
		return getColuna(getPos(idColuna));
	}
	
	/**
	 * Retorna as informações configuradas no layout xml referentes a coluna selecionada
	 * 
	 * @param index <br>Indice da coluna<br><br>
	 * @return ColunaLayout <br>Informações referentes a coluna selecionada<br><br>
	 * @throws ParseFileException
	 */
	public ColunaLayout getColuna(int index) throws ParseFileException {
		return (ColunaLayout)this.colunas.get(index);
	}
	
	/**
	 * Retorna o identificador da linha
	 * 
	 * @return String <br>Id da linha. Atributo id da tag <linha> do layout xml<br><br>
	 */
	public String getLinhaId() {
		return linhaId;
	}
	
	/**
	 * Seta o identificador da linha
	 * 
	 * @param linhaId <br>Id da linha. Atributo id da tag <linha> do layout xml<br><br>
	 */
	public void setLinhaId(String linhaId) {
		this.linhaId = linhaId;
	}
	
	/**
	 * Retorna o tipo da linha
	 * 
	 * @return String <br>Tipo da linha. Atributo tipo da tag <linha> do layout xml<br><br>
	 */
	public String getLinhaTipo() {
		return linhaTipo;
	}
	
	/**
	 * Retorna o tipo da linha
	 * 
	 * @param linhaTipo <br>Tipo da linha. Atributo tipo da tag <linha> do layout xml<br><br>
	 */
	public void setLinhaTipo(String linhaTipo) {
		this.linhaTipo = linhaTipo;
	}
	
	/**
	 * Limpa os objetos internos do objeto Valores 
	 */
	public void clear() {
		this.colunas.clear();
		
		if (this.valores != null) {
			for (int v = 0; v < this.valores.length; v++) {
				this.valores[v] = null;
			}
		}
		
		if (this.exceptions != null) {
			for (int v = 0; v < this.exceptions.length; v++) {
				this.exceptions[v] = null;
			}
		}
		
		this.valores = null;
		this.exceptions = null;
		
	}
	
	/**
	 * Retorna o Indice de determinada coluna no objeto Valores
	 * 
	 * @param idColuna <br>Identificador da coluna. Atributo id da tag <coluna> no layout xml<br><br>
	 * @return int <br>Indice da coluna<br><br>
	 * @throws ParseFileException
	 */
	public int getIndex(String idColuna) throws ParseFileException  {
		return ParseUtils.getPos(this.mapValores, idColuna);
	}
	
	/**
	 * Retorna as informações de todas as colunas da linha configuradas no arquivo layout xml
	 * 
	 * @return ArrayList <br>Array de objetos ColunaLayout de todas as colunas da linha<br><br>
	 */
	public ArrayList getColunas() {
		return this.colunas;
	}
	
	
	/**
	 * Retorna o mapa dos totalizadores do layout
	 * 
	 * @return StringBuffer <br>Buffer de mapa dos totalizadores do layout<br><br>
	 */
	//public StringBuffer getMapTotalizadores() {
	//	return mapTotalizadores;
	//}
	
	//public void setMapTotalizadores(StringBuffer mapTotalizadores) {
	//	this.mapTotalizadores = mapTotalizadores;
	//}
	
	
	/**
	 * Seta um valor a determinado totalizador
	 * 
	 * @param idTot <br>Identificador do Totalizador. Atributo id da tag <totalizador> do arquivo layout xml<br><br>
	 * @param valor <br>Valora a ser setado no totalizador selecionado<br><br>
	 * @throws ParseFileException
	 */
	//public void setColunaTotalizador(String idTot, long valor) throws ParseFileException {
	//	String coluna = ParseUtils.getValue(this.mapTotalizadores,idTot);
	//	
	//	if (!Valida.isEmpty(coluna)) {
	//		set(coluna,String.valueOf(valor));
	//	}
	//}
	
	/**
	 * Verifica se existe determinada coluna no objeto Valores
	 * 
	 * @param idColuna <br>Identificador da coluna. Atributo id da tag <coluna> no layout xml<br><br>
	 * @return boolean <br><b>true</b> A coluna foi encontrada no objeto valores<br><b>false</b> A coluna não existe nesse objeto<br><br>
	 * @throws ParseFileException
	 */
	public boolean hasColuna(String idColuna) throws ParseFileException {
		return (getIndex(idColuna) != -1);
	}
	
	public DinVO toDinVO() {
		if (Valida.isEmpty(colunas)) {
			return null;
		}
		
		LinkedList ids = new LinkedList();
		LinkedList vals = new LinkedList(); 
		
		for (int id = 0; id < colunas.size(); id++) {
			ColunaLayout col = (ColunaLayout)colunas.get(id);
			ids.add ( col.getId() );
			vals.add( valores[id] );
		}
		
		return new DinVO(ids, vals);
	
	}

}
