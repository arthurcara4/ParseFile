package br.com.accesstage.parsefile.layout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import br.com.accesstage.parsefile.daoutil.SqlExecVO;
import br.com.accesstage.parsefile.exceptions.ParseFileException;
import br.com.accesstage.parsefile.utils.ParseUtils;
import br.com.accesstage.parsefile.utils.Valida;


public class LinhaLayout implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String NUMERICO = "NUMERICO";
	public static final String ALFA = "ALFA";
	public static final String DATA = "DATA";
	public static final String VALOR = "VALOR";
	
	private String id;
	private String tipo;
	private StringBuffer mapColunas;
	private ArrayList colunas;
	private int tamanho;
	//private StringBuffer mapTotalizadores;
	
	private boolean read;
	private boolean write;
	
	private boolean delimitado;
	
	private boolean completeLinha;
	
	private String antes;
	private String depois;
	
	private LinkedList execucoesSql;
	
	public LinhaLayout() {
		
	}
	
	public LinhaLayout(String id, String tipo, boolean obrigatorio, boolean delimitado, boolean completeLinha, String antes, String depois) {
		setId(id);
		setTipo(tipo);
		this.mapColunas = new StringBuffer();
		this.colunas = new ArrayList();
		this.tamanho = 0;
		
		this.read = !obrigatorio;
		this.write = !obrigatorio;
		
		this.delimitado = delimitado;
		
		this.completeLinha = completeLinha;
		
		if (!Valida.isEmpty(antes)) this.antes = new StringBuffer(",").append(antes).append(",").toString();
		if (!Valida.isEmpty(depois)) this.depois = new StringBuffer(",").append(depois).append(",").toString();
	}

	/*
	public LinhaLayout(String id, String tipo, String mapColunas, ArrayList colunas) {
		setId(id);
		setTipo(tipo);
		setMapColunas(mapColunas);
		setColunas(colunas);
	}
	*/
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		if (Valida.isEmpty(tipo)) { return; }
		this.tipo = tipo;
	}
	public ArrayList getColunas() {
		if (colunas == null) return null;
		return (ArrayList)colunas.clone();
	}
	public void setColunas(ArrayList colunas) {
		this.colunas = colunas;
	}
	public int getTamanho() {
		return this.tamanho;
	}
	
	
	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isWrite() {
		return write;
	}

	public void setWrite(boolean write) {
		this.write = write;
	}
	
	public boolean isProcess(boolean read) {
		
		if (read) { return this.read; }
		
		return this.write;
	}
	
	public boolean isDelimitado() {
		return delimitado;
	}

	public void setDelimitado(boolean delimitado) {
		this.delimitado = read;
	}
	
	public boolean isCompleteLinha() {
		return completeLinha;
	}
	public void setCompleteLinha(boolean completeLinha) {
		this.completeLinha = completeLinha;
	}
	

	public String getAntes() {
		return antes;
	}
	public void setAntes(String antes) {
		this.antes = antes;
	}

	public String getDepois() {
		return depois;
	}
	public void setDepois(String depois) {
		this.depois = depois;
	}
	

	public LinkedList getExecucoesSql() {
		return execucoesSql;
	}

	public void setExecucoesSql(LinkedList execucoesSql) {
		this.execucoesSql = execucoesSql;
	}
	
	public void addExecucoesSql(SqlExecVO vo) {
		if (execucoesSql == null) execucoesSql = new LinkedList();
		execucoesSql.add(vo);
	}
	public boolean hasExecucoesSql() {
		return !Valida.isEmpty(execucoesSql);
	}

	public int getPosColuna(String idColuna) throws ParseFileException {
		
		int pos = ParseUtils.getPos(this.mapColunas, idColuna);
		
		if (pos == -1) {
			// coluna tipo(id): não existe no layout na tipo de linha(id)
			StringBuffer msg = new StringBuffer();
			msg.append("Coluna (").append(idColuna);
			msg.append(") não existe no layout para a linha (").append(this.id);
			msg.append(")");
			
			throw new ParseFileException(msg.toString());
		}
		
		return pos;
	}	
	
	public ColunaLayout getColuna(String idColuna) throws ParseFileException {
		
		int pos = getPosColuna(idColuna);
		
		return getColuna(pos);
		
	}

	public ColunaLayout getColuna(int index) throws ParseFileException {
		
		return (ColunaLayout)this.colunas.get(index);
		
	}

	public ArrayList getColunas(String colunas) throws ParseFileException {
		
		String[] cols = colunas.toUpperCase().split("\\,");
		
		if (Valida.isEmpty(cols)) {
			StringBuffer msg = new StringBuffer("Colunas inválidas no parse de colunas:");
			throw new ParseFileException(msg.toString());
		}
		
		ArrayList colsLay = new ArrayList();
		
		for (int c = 0; c < cols.length; c++) {
			colsLay.add(getColuna(cols[c].trim()));
		}
		
		return colsLay;
	}
	
	public void alteraTamanhoColuna(String idColuna, int tamanho) throws ParseFileException {
		
		int pos = getPosColuna(idColuna);
		
		if (getColuna(pos).getTamanho() == tamanho) return;
		
		int dif = (getColuna(pos).getTamanho() - tamanho) * -1;
		getColuna(pos).setTamanho(tamanho);
		
		for (int i = pos + 1; i < this.colunas.size(); i++) {
			int posi = getColuna(i).getPosIni() + dif + 1; 
			getColuna(i).setPosIni(posi);
		}
	}
	
	/*
	public void addColunaDATA(String id, int tamanho) throws ParseFileException {
		addColuna(id, DATA, tamanho, null, null, null, null, null, false);
	}
	
	public void addColunaDATA(String id, int posIni, int tamanho) throws ParseFileException {
		addColuna(id, DATA, tamanho, null, null, null, null, null, false);
	}
	
	public void addColunaDATA(String id, int tamanho, String params) throws ParseFileException {
		addColuna(id, DATA, tamanho, null, null, null, null, null, false);
	}
	
	public void addColunaDATA(String id, int posIni, int tamanho, String params) throws ParseFileException {
		addColuna(id, DATA, tamanho, null, null, null, null, null, false);
	}
	
	public void addColunaDATA(String id, int tamanho, String descricao, String formato) throws ParseFileException {
		addColuna(id, DATA, tamanho, descricao, null, null, formato, null, false);
	}
	public void addColunaDATA(String id, int posIni, int tamanho, String descricao, String formato) throws ParseFileException {
		addColuna(id, DATA, posIni, tamanho, descricao, null, null, formato, null, false);
	}
	public void addColunaDATA(String id, int tamanho, String descricao, String formato, boolean obrigatorio) throws ParseFileException {
		addColuna(id, DATA, tamanho, descricao, null, null, formato, null, obrigatorio);
	}
	public void addColunaDATA(String id, int posIni, int tamanho, String descricao, String formato, boolean obrigatorio) throws ParseFileException {
		addColuna(id, DATA, posIni, tamanho, descricao, null, null, formato, null, obrigatorio);
	}

	
	public void addColunaALFA(String id, int tamanho) throws ParseFileException {
		addColuna(id, ALFA, tamanho, null, null, null, null, null, true);
	}
	public void addColunaALFA(String id, int tamanho, String descricao) throws ParseFileException {
		addColuna(id, ALFA, tamanho, descricao, null, null, null, null, true);
	}
	public void addColunaALFA(String id, int tamanho, String descricao, String strDefault) throws ParseFileException {
		addColuna(id, ALFA, tamanho, descricao, strDefault, null, null, null, true);
	}
	public void addColunaALFA(String id, int tamanho, String descricao, String strDefault, String pad) throws ParseFileException {
		addColuna(id, ALFA, tamanho, descricao, strDefault, pad, null, null, true);
	}
	public void addColunaALFA(String id, int tamanho, String descricao, String strDefault, String pad, String dominio) throws ParseFileException {
		addColuna(id, ALFA, tamanho, descricao, strDefault, pad, null, dominio, true);
	}
	public void addColunaALFA(String id, int tamanho, String descricao, String strDefault, String pad, String dominio, boolean obrigatorio) throws ParseFileException {
		addColuna(id, ALFA, tamanho, descricao, strDefault, pad, null, dominio, obrigatorio);
	}
	
	public void addColunaALFA(String id, int posIni, int tamanho) throws ParseFileException {
		addColuna(id, ALFA, posIni, tamanho, null, null, null, null, null, true);
	}
	public void addColunaALFA(String id, int posIni, int tamanho, String descricao) throws ParseFileException {
		addColuna(id, ALFA, posIni, tamanho, descricao, null, null, null, null, true);
	}
	public void addColunaALFA(String id, int posIni, int tamanho, String descricao, String strDefault) throws ParseFileException {
		addColuna(id, ALFA, posIni, tamanho, descricao, strDefault, null, null, null, true);
	}
	public void addColunaALFA(String id, int posIni, int tamanho, String descricao, String strDefault, String pad) throws ParseFileException {
		addColuna(id, ALFA, posIni, tamanho, descricao, strDefault, pad, null, null, true);
	}
	public void addColunaALFA(String id, int posIni, int tamanho, String descricao, String strDefault, String pad, String dominio) throws ParseFileException {
		addColuna(id, ALFA, posIni, tamanho, descricao, strDefault, pad, null, dominio, true);
	}
	public void addColunaALFA(String id, int posIni, int tamanho, String descricao, String strDefault, String pad, String dominio, boolean obrigatorio) throws ParseFileException {
		addColuna(id, ALFA, posIni, tamanho, descricao, strDefault, pad, null, dominio, obrigatorio);
	}
	
	
	
	public void addColunaNUME(String id, int tamanho) throws ParseFileException {
		addColuna(id, NUMERICO, tamanho, null, null, null, null, null, false);
	}
	public void addColunaNUME(String id, int tamanho, String descricao) throws ParseFileException {
		addColuna(id, NUMERICO, tamanho, descricao, null, null, null, null, false);
	}
	public void addColunaNUME(String id, int tamanho, String descricao, String strDefault) throws ParseFileException {
		addColuna(id, NUMERICO, tamanho, descricao, strDefault, null, null, null, false);
	}
	public void addColunaNUME(String id, int tamanho, String descricao, String strDefault , String pad) throws ParseFileException {
		addColuna(id, NUMERICO, tamanho, descricao, strDefault, pad, null, null, false);
	}
	public void addColunaNUME(String id, int tamanho, String descricao, String strDefault , String pad, String dominio) throws ParseFileException {
		addColuna(id, NUMERICO, tamanho, descricao, strDefault, pad, null, dominio, false);
	}
	public void addColunaNUME(String id, int tamanho, String descricao, String strDefault , String pad, String dominio, boolean obrigatorio) throws ParseFileException {
		addColuna(id, NUMERICO, tamanho, descricao, strDefault, pad, null, dominio, obrigatorio);
	}
	public void addColunaNUME(String id, int posIni, int tamanho, String descricao) throws ParseFileException {
		addColuna(id, NUMERICO, posIni, tamanho, null, null, null, null, null, false);
	}
	public void addColunaNUME(String id, int posIni, int tamanho, String descricao, String strDefault) throws ParseFileException {
		addColuna(id, NUMERICO, posIni, tamanho, descricao, strDefault, null, null, null, false);
	}
	public void addColunaNUME(String id, int posIni, int tamanho, String descricao, String strDefault, String pad) throws ParseFileException {
		addColuna(id, NUMERICO, posIni, tamanho, descricao, strDefault, pad, null, null, false);
	}
	public void addColunaNUME(String id, int posIni, int tamanho, String descricao, String strDefault, String pad, String dominio) throws ParseFileException {
		addColuna(id, NUMERICO, posIni, tamanho, descricao, strDefault, pad, null, null, false);
	}
	public void addColunaNUME(String id, int posIni, int tamanho, String descricao, String strDefault, String pad, String dominio, boolean obrigatorio) throws ParseFileException {
		addColuna(id, NUMERICO, posIni, tamanho, descricao, strDefault, pad, null, null, obrigatorio);
	}
*/	

	public void addColunaXml(
			  String id
			, String tipo
			, String posIni
			, String tamanho
			, String descricao
			, String strDefault
			, String pad
			, String padSide
			, String formato
			, String dominio
			, String obrigatorio
			, String decimais
			, String upper
			, String lower
			, String totalizador
			, String zerar
			, String sepDecimais
			, String zerarTotalizadores
			, String sqlExec
			, String setVariavel
			, String getVariavel
			, String numericoCompleto
			) throws ParseFileException {
		
    	if (Valida.isEmpty(id)) {
    		// Erro do layout: Existem colunas sem id na descrição do layout. Seja consciente, leia o layout com o cérebro ligado!
    		throw new ParseFileException("Erro do layout: Existem colunas sem o atributo <id> no layout.");
    	}
    	
    	int tam = 0;
    	
    	if (!isDelimitado()) {
	    	if (Valida.isEmpty(tamanho)) {
	    		// Erro do layout: Existem colunas sem tamanho informado ! Seja consciente, leia o layout com o cérebro ligado!
	    		throw new ParseFileException("Erro do layout: Existem colunas sem o atributo <tamanho> no layout.");
	    	}
        	tam = Integer.parseInt(tamanho.trim());
    	}
    	
    	int posI = -1;
    	if (!Valida.isEmpty(posIni)) {
    		posI = Integer.parseInt(posIni.trim());
    	}
    	
    	if (Valida.isEmpty(tipo)) {
    		// Erro do layout: Existem colunas sem tipo informado !
    		throw new ParseFileException("Erro do layout: Existem colunas sem o atributo <tipo> no layout.");
    	}
    	tipo = tipo.toUpperCase();
    	
    	if ((!tipo.equals(LinhaLayout.NUMERICO)) &&  (!tipo.equals(LinhaLayout.ALFA)) && (!tipo.equals(LinhaLayout.DATA)) && (!tipo.equals(LinhaLayout.VALOR))) {
    		// Erro do layout: Existem colunas de tipo invalida. Tipos válidos: < , , , >
    		
    		StringBuffer msg = new StringBuffer("Erro do layout: Existem colunas com o tipo invalido (");
    		msg.append(tipo).append("). Tipos válidos: ");
    		msg.append(LinhaLayout.NUMERICO).append(", ");
    		msg.append(LinhaLayout.VALOR).append(", ");
    		msg.append(LinhaLayout.ALFA).append(" ou ");
    		msg.append(LinhaLayout.DATA).append(".");
    		
    		throw new ParseFileException(msg.toString());
    	}

    	if (tipo.equals(LinhaLayout.DATA)) {
        	if (Valida.isEmpty(formato)) {
        		// Erro do layout: Existem colunas tipo DATA sem formato atribuido
        		throw new ParseFileException("Erro do layout: Existem colunas tipo DATA sem o atributo <formato>.");
        	}
    	}
    	/*
    	if (!tipo.equals(LinhaLayout.NUMERICO)) {
        	if ((!Valida.isEmpty(dominio)) && (dominio.indexOf("-") != -1)) {
        		throw new ParseFileException("O hifen na propriedade <dominio> da tag <coluna> só pode ser utilizado quando o <tipo='NUMERICO'> no xml de layout.");
        	}
    	}
    	*/
    	
    	int dec = -1;
    	if (!Valida.isEmpty(decimais)) {
    		dec = Integer.parseInt(decimais.trim());
    	}

    	boolean obrig = true;
    	if (tipo.equals(LinhaLayout.ALFA)) {
    		obrig = false;
    	}
    	obrig = ParseUtils.validaBoolean(obrigatorio,obrig);
    	
    	if (!Valida.isEmpty(padSide)) {
    		padSide = padSide.toUpperCase();
    		
        	if (!((padSide.equals(ParseUtils.DIREITA)) || (padSide.equals(ParseUtils.ESQUERDA)))) {
        		// Erro do layout: Existem colunas com padding incorreto. Tipos válidos ( , )
        		StringBuffer msg = new StringBuffer("Erro do layout: Existem colunas com o atributo <padside> incorreto. Tipos válidos: ");
        		msg.append(ParseUtils.DIREITA).append(" ou ");
        		msg.append(ParseUtils.ESQUERDA).append(".");
        		throw new ParseFileException(msg.toString());
        	}
    		
    	}
		
    	boolean up = ParseUtils.validaBoolean(upper,false);
    	boolean lw = ParseUtils.validaBoolean(lower,false);
    	boolean zera = ParseUtils.validaBoolean(zerar,true);
    	
    	if ((!Valida.isEmpty(sepDecimais)) && (!sepDecimais.equals(",")) &&  (!sepDecimais.equals("."))) {
    		// Erro do layout: Existem colunas do tipo VALOR com separador inválido. Tipos vallidos: "," ou "."
    		StringBuffer msg = new StringBuffer("Erro do layout: Existem colunas do tipo VALOR com o atributo <separador> inválido. Tipos validos: ',' ou '.'");
    		throw new ParseFileException(msg.toString());
    	}
    	
    	int numComp = -1;
    	if (!Valida.isEmpty(numericoCompleto)) {
        	boolean comp = ParseUtils.validaBoolean(numericoCompleto,false);
        	numComp = 0;
        	if (comp) numComp = 1;
    	}    	
    	
		addColuna(id
				, tipo
				, posI
				, tam
				, descricao
				, strDefault
				, pad, padSide
				, formato
				, dominio
				, obrig
				, dec
				, up
				, lw
				, totalizador
				, zera
				, sepDecimais
				, zerarTotalizadores
				, sqlExec
				, setVariavel
				, getVariavel
				, numComp
				);
	}

	public void addColuna(String id
			, String tipo
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
			) throws ParseFileException {
		
		
		addColuna(id
				, tipo
				, -1
				, tamanho
				, descricao
				, strDefault
				, pad, padSide
				, formato
				, dominio
				, obrigatorio
				, decimais
				, upper
				, lower
				, totalizador
				, zerar
				, sepDecimais
				, zerarTotalizadores
				, sqlExec
				, setVariavel
				, getVariavel
				, numericoCompleto
				);
	}
	public void addColuna(String id
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
			) throws ParseFileException {

		if (this.colunas == null) {

			this.colunas = new ArrayList();
		}
		
		// passa-se -1 quando se deseja calcular a coluna inicial
		if (posIni == -1) { 
			posIni = 1;
			if ((this.colunas != null) && (!this.colunas.isEmpty())) {
				ColunaLayout col = ((ColunaLayout)this.colunas.get(this.colunas.size() - 1));
				posIni = col.getPosIni() + col.getTamanho() + 1; 
			}
		}
		
		String colunaId = ParseUtils.getIdStr(id);
		
		if (this.mapColunas.toString().indexOf(colunaId) != -1) {
    		// Erro do layout: Tipo de linha(id) com ids de colunas duplicados (id)
			StringBuffer msg = new StringBuffer();
			msg.append("Erro do layout: linha(").append(this.id).append(") com ids de colunas duplicados (").append(id).append(")");
			throw new ParseFileException(msg.toString());
		}
		
		this.colunas.add(new ColunaLayout(id
				, tipo
				, posIni
				, tamanho
				, descricao
				, strDefault
				, pad
				, padSide
				, formato
				, dominio
				, obrigatorio
				, decimais
				, upper
				, lower
				, totalizador
				, zerar
				, sepDecimais
				, zerarTotalizadores
				, sqlExec
				, setVariavel
				, getVariavel
				, numericoCompleto
				));
		this.mapColunas.append(ParseUtils.getIdStr(id, this.colunas.size()-1));
		
		//if (!Valida.isEmpty(totalizador)) {
		//	if (Valida.isNull(this.mapTotalizadores)) {
		//		this.mapTotalizadores = new StringBuffer();
		//	}
		//	
		//	this.mapTotalizadores.append(ParseUtils.getIdStr(totalizador, id));
		//}
		
		this.tamanho += tamanho;
	}
	
	public void clear() {
		if (this.colunas != null) {
			this.colunas.clear();
			this.colunas = null;
		}
	}
	
	public void sortColunas() {
		 // Em ordem crescente do início do mandato  
		 Collections.sort (this.colunas, new Comparator() {  
		     public int compare(Object o1, Object o2) {  
		         ColunaLayout p1 = (ColunaLayout) o1;  
		         ColunaLayout p2 = (ColunaLayout) o2;  
		         return p1.getPosIni() < p2.getPosIni() ? -1 : (p1.getPosIni() > p2.getPosIni() ? +1 : 0);  
		     }  
		 });   		
		
	}
	
	//public StringBuffer getMapTotalizadores() {
	//	return mapTotalizadores;
	//}
}
