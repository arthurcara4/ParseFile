package br.com.accesstage.parsefile.layout;

import java.io.Serializable;
import java.util.ArrayList;

import br.com.accesstage.parsefile.exceptions.ParseFileException;
import br.com.accesstage.parsefile.ocorrencias.Ocorrencias;
import br.com.accesstage.parsefile.retornos.DinVO;
import br.com.accesstage.parsefile.retornos.Valores;
import br.com.accesstage.parsefile.utils.ParseUtils;
import br.com.accesstage.parsefile.utils.Status;
import br.com.accesstage.parsefile.utils.Valida;



public class Layout implements Serializable{

	private static final long serialVersionUID = 1L;

	private String id;
	private StringBuffer mapLinhas;
	private StringBuffer mapTipos;
	private ArrayList linhas;
	private String[] identificadores;
	private DinVO totalizadores;
	private DinVO mapTotalizadores;
	//private StringBuffer mapTotTipos;
	private boolean validaRead = true;
	private boolean validaWrite = true;
	private String delimitador;
	private String converter;
	private Ocorrencias ocorrencias;
	private StringBuffer linhasEspeciais;
	private boolean ignoreLinhasNaoDeclaradas = false;
	
	private DinVO variaveis;
	
	
	public Layout(String id) {
		this(id,null,null,null);
	}
	
	public Layout(String id, String identificadores) {
		this(id,identificadores,null, null);
	}
	
	public Layout(String id, String identificadores, String delimitador, String converter) {
		setId(id);
		setIdentificadores(identificadores);
		this.mapLinhas = new StringBuffer();
		this.mapTipos = new StringBuffer();
		this.linhas = new ArrayList();
		this.delimitador = delimitador;
		setConverter(converter);
	}
	
	
	public boolean isIgnoreLinhasNaoDeclaradas() {
		return ignoreLinhasNaoDeclaradas;
	}
	public void setIgnoreLinhasNaoDeclaradas(boolean ignoreLinhasNaoDeclaradas) {
		this.ignoreLinhasNaoDeclaradas = ignoreLinhasNaoDeclaradas;
	}

	public boolean isDelimitado() {
		return !Valida.isEmpty(this.delimitador);
	}
	public void setDelimitador(String delimitador) {
		this.delimitador = delimitador;
	}
	public String getDelimitador() {
		return this.delimitador;
	}
	
	public boolean isConverter() {
		if (Valida.isEmpty(this.converter)) return false;
		
		return (!this.converter.equals(this.delimitador));
	}
	public void setConverter(String converter) {
		this.converter = converter;
	}
	public String getConverter() {
		return this.converter;
	}
	
	public DinVO getVariaveis() {
		return variaveis;
	}
	public void setVariaveis(DinVO variaveis) {
		this.variaveis = variaveis;
	}
	public void addsetVariavel(String id, String valor) {
		if (this.variaveis == null) this.variaveis = new DinVO();
		this.variaveis.addset(id, valor);
	}
	public String getVariavel(String id) {
		if (this.variaveis == null) this.variaveis = new DinVO();
		return this.variaveis.getString(id);
	}
	
	public boolean isValidaRead() {
		return validaRead;
	}
	public void setValidaRead(boolean validaRead) {
		this.validaRead = validaRead;
	}
	public boolean isValidaWrite() {
		return validaWrite;
	}
	public void setValidaWrite(boolean validaWrite) {
		this.validaWrite = validaWrite;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String[] getIdentificadores() {
		return identificadores;
	}
	public void setIdentificadores(String[] identificadores) {
		this.identificadores = identificadores;
	}
	public void setIdentificadores(String identificadores) {
		
		if (Valida.isEmpty(identificadores)) { return; }
		
		this.identificadores = identificadores.split("\\|");
	}
	
	public LinhaLayout getLinha(String idLinha) throws ParseFileException {
		// para linhas especiais quando o idLinha vier com o conteudo "#" é que esa linha não será validada
		// portando não efetuará o parse da mesma retornando um objeto valores null
		if (("#".equals(idLinha)) || (Valida.isEmpty(idLinha)))  {
			return new LinhaLayout();
		}
		
		int pos = ParseUtils.getPos(this.mapLinhas, idLinha);
		
		if (pos == -1) {
			throw new ParseFileException(new StringBuffer("Linha id:").append(idLinha).append(" não existe no layout").toString());
		}
		
		return getLinha(pos);
	}
	
	public LinhaLayout getLinhaPorTipo(String tipo) throws ParseFileException {
		// para linhas especiais quando o idLinha vier com o conteudo "#" é que esa linha não será validada
		// portando não efetuará o parse da mesma retornando um objeto valores null
		if (("#".equals(tipo)) || (Valida.isEmpty(tipo)))  {
			return new LinhaLayout();
		}
		
		int pos = ParseUtils.getPos(this.mapTipos, tipo);
		
		if (pos == -1) {
			throw new ParseFileException(new StringBuffer("Linha tipo:").append(tipo).append(" não existe no layout").toString());
		}
		
		return getLinha(pos);
	}


	public LinhaLayout getLinha(int index) throws ParseFileException {
		return (LinhaLayout)linhas.get(index);
	}
	
	private String getSubString(String linha, String coord) {
		String pos[] = coord.split("\\:");
		
		int posIni = Integer.parseInt(pos[0].trim()) - 1;
		int tamanho = Integer.parseInt(pos[1].trim());
		return linha.substring(posIni, posIni + tamanho); 
	}
	
	public String getDelimitedCol(String linha, String col, long numlinha) throws ParseFileException {
		return getDelimitedCol(linha, Integer.parseInt(col), numlinha);
	}
	
	public String getDelimitedCol(String linha, int col, long numlinha) throws ParseFileException {
		
		String cols[] = getDelimitedCols(linha, numlinha);
		
		try {
			return cols[col];
		} catch (IndexOutOfBoundsException e) {
			// Linha numero<num>: coluna solicitada (col) não foi encontrada
			
			StringBuffer msg = ParseUtils.msgErro(this.isDelimitado(), this.getId(), null, null, numlinha, null, -1,
					new StringBuffer("Coluna solicitada (").append(col).append(") não encontrada") );
			
			
			throw new ParseFileException(msg.toString());
		}
	}
	
	public String[] getDelimitedCols(String linha, long numlinha) throws ParseFileException {
		
		String auxDelimin = "\\ø";
		
		int posini = linha.indexOf("\"");
		
		boolean findExcept = false;
		
		if (posini != -1) {
			
			StringBuffer linRet = new StringBuffer(linha);
			
			while(posini != -1) {
				int posfim = linha.indexOf("\"",(posini + 1));
				
				if (posfim == -1) {
					// Linha numero<num>: coluna com delimitador " não foi fechada
					
					StringBuffer msg = ParseUtils.msgErro(this.isDelimitado(), this.getId(), null, null, numlinha, null, -1,
							new StringBuffer("Coluna não fecha aspas. posini:").append(posini) );
					
					throw new ParseFileException(msg.toString());
				}
				
				// verifica se encontrou delimitadores dentro das aspas
				int posDelim = linha.substring(posini, posfim).indexOf(getDelimitador());
				if (posDelim != -1) {
					findExcept = true;
					
					String subLinha = linha.substring(posini, posfim).replaceAll(getDelimitador(), (this.isConverter())?this.getConverter():auxDelimin);
					linha = linRet.replace(posini , posfim , subLinha).toString();
					
				}
				
				posini = linha.indexOf("\"", posfim + 1);
			}
			
			linha = linRet.toString();
			linha = linha.replaceAll("\"", "");
		}
		
		String cols[] = linha.split(new StringBuffer("\\").append(getDelimitador()).toString(), countMatch(linha,getDelimitador()));
		
		if ((findExcept) && (!this.isConverter())) {
			for (int c = 0; c < cols.length; c++) {
				cols[c] = cols[c].replaceAll(auxDelimin, getDelimitador());
			}
		}
		
		return cols;
				
		
	}
	
	private int countMatch(String str, String match) {
		int occ = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.substring(i, i+1).equals(match)) occ++;
		}
		return occ + 1;
	}
	
	
	public String getLinhaId(String linha, long numLinha) throws ParseFileException{
		
		if (numLinha <= 0) {
			// foi colocado esse "+ 1" pois teoricamete esse metodo só é chamado no ParseFile
			// nos derivados do metodo converteColuna. Porém nesse monento os contadores internos
			// ainda não foram atualizados
			numLinha = getTotalizador("#seqInterno").getContadorRead() + 1;
		}
		
		// ira verificar se a linha informada tem validação especial por numero de linha
		// essas condiçoes são determinadas pelos atributos numero e validar do xml de layou
		/// na tag <linha>
		String idLin = this.getIdLinhaEspecial(numLinha);
		
		if (!Valida.isEmpty(idLin)) {
			return idLin;
		}
		
		if (Valida.isEmpty(linha)) {
			return null;
		}
		
		
		if (Valida.isEmpty(this.identificadores)) {
			if (!isDelimitado()) {
				// Tipo de registro não informado no layout
				throw new ParseFileException("Tipo de registro não informado no layout");
			}
			return "default";
		}
		
		
		StringBuffer notFound = new StringBuffer();
		// monta a chave de tipo de acordo com o dominio
		// ex: se a chave for 1:2 (pega a string da posição 1 com o tamanho de 2)
		//     se a chave for 1:2+7:3 (pega a string da posição 1 com o tamanho de 2 e concatena com posição 7 tamanho 3)
		// para se testar mais de um tipo de chave pode se criar a string 1:2|1:2+7:3
		for (int ident = 0; ident < this.identificadores.length; ident++) {
			StringBuffer tipo = new StringBuffer();
			if (this.identificadores[ident].indexOf("+") == -1) {
				
				// em caso de layout delimitado (CSV) os identificadores vem com o indice da coluna e não com a posição
				// de substring.
				if (!isDelimitado()) {
					tipo.append(getSubString(linha,this.identificadores[ident]));
				} else {
					tipo.append(getDelimitedCol(linha,this.identificadores[ident], numLinha));
				}
			} else {
				String[] chave = this.identificadores[ident].split("\\+");
				for (int ch = 0; ch < chave.length; ch++) {
					
					if (!isDelimitado()) {
						tipo.append(getSubString(linha,chave[ch]));
					} else {
						tipo.append(getDelimitedCol(linha,chave[ch], numLinha));
					}
				}
			}
			
			int pos = ParseUtils.getPos(this.mapTipos, tipo.toString());
			
			// se encontrar retorna o tipo de linha 
			if (pos != -1) {
				return ((LinhaLayout)this.linhas.get(pos)).getId();
			} else {
				notFound.append(tipo).append(" ");
			}
		}
		
		//06/08/2010 - Colocada uma condição para ignorar linhas não declaradas no layout
		if (isIgnoreLinhasNaoDeclaradas()) {
			return "#";
		}
		
		
		// se sair do for é porque não encontrou nenhuma linha do tipo
		// Linha <numlinha>: Tipo de registro encontrado no arquivo e não informado no layout (tipos conhecidos: ())
		StringBuffer msg = ParseUtils.msgErro(this.isDelimitado(), this.getId(), null, null, numLinha, null, -1,
				new StringBuffer("Tipo de registro encontrado no arquivo não informado no layout. (tipos do layout: (").append(notFound.toString()).append("))") );
		
		throw new ParseFileException(Status.TIPO_LINHA_INVALIDO, "" , msg.toString(), "", notFound.toString());
	}
	
	public void addLinha(LinhaLayout linha) throws ParseFileException {
		if (this.linhas == null) {
			this.linhas = new ArrayList();
		}
		String linhaId = ParseUtils.getIdStr(linha.getId());
		
		if (this.mapLinhas.toString().indexOf(linhaId) != -1) {
			StringBuffer msg = new StringBuffer();
			msg.append("A linha: ").append(linha.getId());
			msg.append(" já existe no layout: ").append(this.id);
			throw new ParseFileException(msg.toString());
		}

		
		this.linhas.add(linha);
		
		this.mapLinhas.append(ParseUtils.getIdStr(linha.getId(), this.linhas.size()-1));
		this.mapTipos.append(ParseUtils.getIdStr(linha.getTipo(), this.linhas.size()-1));
	}
	
	
	public void addTotalizador(String id, int start) throws ParseFileException {
		if (this.totalizadores == null) this.totalizadores = new DinVO();
		
		if (this.totalizadores.hasId(id)) {
			StringBuffer msg = new StringBuffer();
			msg.append("o Totalizador: ").append(id);
			msg.append(" já existe no layout: ").append(this.id);
			throw new ParseFileException(msg.toString());
		}
		
		this.totalizadores.add(id, new Totalizador(id, start));
	}
	
	public void addMapTotalizadores(String id, String tipo, String campos) throws ParseFileException {
		
		if ((!Valida.isEmpty(campos)) && (!Valida.isEmpty(tipo))) {
    		// Erro do layout(totalizador): totalizador inválido (tentativa de totalizar campos e tipos de colunas)
			throw new ParseFileException(new StringBuffer("Erro do layout: totalizador (").append(id).append(") está com ambos os atributos <tipo> e <campo>. Só pode haver um!"));
		}
		
		if ((Valida.isEmpty(campos)) && (Valida.isEmpty(tipo))) {
			addMapTotalizador(id,"#SEQ#","#SOMATIPO#");
		}
		
		if (!Valida.isEmpty(tipo)) {
			String[] tps = tipo.split("\\+");
			
			for( int i = 0; i < tps.length; i++) {
				addMapTotalizador(id,tps[i],"#SOMATIPO#");
			}
		}
		
		if (!Valida.isEmpty(campos)) {
			
			String[] cps = campos.split("\\+");
			
			for( int i = 0; i < cps.length; i++) {
				String [] cp = cps[i].split(":");
				
				String tipos[] = cp[0].split("\\|");
				if (!Valida.isEmpty(tipos)) {
					for(int tp = 0; tp < tipos.length; tp++) {
						if (!Valida.isEmpty(tipos[tp])) {
							addMapTotalizador(id, tipos[tp], cp[1]);
						}
					}
				}
			}
		}
		
		
	}
	
	
	public void addMapTotalizador(String id, String tipo, String campo) {
		if (this.mapTotalizadores == null) this.mapTotalizadores = new DinVO();
		
		tipo = (Valida.isEmpty(tipo)?"":tipo.trim());
		
		if (!this.mapTotalizadores.hasId(tipo)) 
			this.mapTotalizadores.add(tipo);
		
		DinVO camps = this.mapTotalizadores.getDinVO(tipo);
		
		if (Valida.isEmpty(camps))
			camps = new DinVO();
		
		if (!camps.hasId(campo)) 
			camps.add(campo);
		
		ArrayList tots = (ArrayList)camps.get(campo);
		
		if (Valida.isEmpty(tots))
			tots = new ArrayList();
		
		if (tots.indexOf(id) == -1) 
			tots.add(id);
		
		camps.set(campo, tots);
		this.mapTotalizadores.set(tipo, camps);
	}
	

	
	
	
	public void totaliza(String tipo, String campo, int valor, boolean read, boolean write) throws ParseFileException {
		if (Valida.isEmpty(this.mapTotalizadores)) return;
		
		int post = this.mapTotalizadores.findInIds(tipo);
		if (post == -1) return; 
		
		DinVO camps = this.mapTotalizadores.getDinVO(post);
		
		if (Valida.isEmpty(camps)) return;
		
		int posc = camps.findInIds(campo);
		if (posc == -1) return; 
		
		ArrayList tots = (ArrayList)camps.get(posc);
		
		if (Valida.isEmpty(tots)) return;
		
		for (int tt = 0; tt < tots.size(); tt++) {
			
			String totName = (String)tots.get(tt);
			
			if (read)
				getTotalizador(totName).addRead(valor);
			
			if (write)
				getTotalizador(totName).addWrite(valor);
			
		}
		
		
		
	}
	
	
	
	
/*	
	public void addTotalizador(String id, String tipo, String campos, int start) throws ParseFileException {
		addTotalizador(new Totalizador(id, tipo, campos, start));
	}

	
	public void addTotalizador(Totalizador tot) throws ParseFileException {
		if (this.totalizadores == null) {
			this.totalizadores = new ArrayList();
			this.mapTotalizadores = new StringBuffer();
			this.mapTotTipos = new StringBuffer(":");
		}
		String totId = ParseUtils.getIdStr(tot.getId());
		
		if (this.mapTotalizadores.toString().indexOf(totId) != -1) {
			StringBuffer msg = new StringBuffer();
			msg.append("o Totalizador: ").append(tot.getId());
			msg.append(" já existe no layout: ").append(this.id);
			throw new ParseFileException(msg.toString());
		}

		
		this.totalizadores.add(tot);
		
		this.mapTotalizadores.append(ParseUtils.getIdStr(tot.getId(), this.totalizadores.size()-1));
		
		// adiciona no mapa os tipos que esse totalizador trabalha
		// ex: 01:campos1+01:campo2+02:campo1
		//     ira adiciona 01 e 02
		String[] tipos = tot.getTipos();
		for (int i = 0; i < tipos.length; i++) {
			this.mapTotTipos.append(ParseUtils.getIdStr(tipos[i], this.totalizadores.size()-1));
		}
		
	}

	public void totalizaRead(Valores valores) throws ParseFileException {
		totaliza("#",null,true); // totalizadores genericos (chamar sempre)
		totaliza(null,valores, true);
	}
	public void totalizaWrite(Valores valores) throws ParseFileException {
		totaliza("#",null, false); // totalizadores genericos (chamar sempre)
		totaliza(null,valores, false);
	}
	
	public void totalizaInternalRead() throws ParseFileException {
		totaliza("#int",null,true); // totalizadores internos
	}
	public void totalizaInternalWrite() throws ParseFileException {
		totaliza("#int",null,false); // totalizadores internos
	}
*/
	
	public void totaliza(String tipo, Valores valores, boolean read, boolean write, long numLinha) throws ParseFileException {
		
		// caso venha os valoes preenchidos, soma através do tipo da linha
		// senaõ soma os gerais (#).
		if (valores != null) {
			tipo = valores.getLinhaTipo();
		}
		
		
		if (Valida.isEmpty(this.mapTotalizadores)) return;
		
		int post = this.mapTotalizadores.findInIds(tipo);
		if (post == -1) return; 
		
		// soma os sequenciais desse tipo
		totaliza(tipo, "#SOMATIPO#", 1, read, write);
		
		DinVO camps = this.mapTotalizadores.getDinVO(post);
		
		if (Valida.isEmpty(camps)) return;
		
		//verifica se existe algum campos com totalizadores
		ArrayList cols = valores.getColunas();
		
		for (int cc = 0; cc < cols.size(); cc++) {
			ColunaLayout colLay = (ColunaLayout)cols.get(cc);
			int posc = camps.findInIds(colLay.getId());
			if (posc != -1) {
				
				ArrayList tots = (ArrayList)camps.get(posc);
				
				if (Valida.isEmpty(tots)) return;
				
				long valor = 0;
				
				try {
					String val = valores.getString(colLay.getId());
					if ("#tot#".equals(val)) {
						val = String.valueOf(getTotalizador(colLay.getTotalizador()).getContadorWrite());
					}
					valor = Long.parseLong(val);
				} catch(NumberFormatException e) {
					// Linha tipo 'id' (tipo) numero<num>: coluna posicao inicial (posini) tamanho (tam): erro tentando totalizar coluna alfanumérica
					ColunaLayout col = valores.getColuna(cc);
					StringBuffer msg = ParseUtils.msgErro(this.isDelimitado(), this.getId(), valores.getLinhaId(), valores.getLinhaTipo(), -1, col, cc,
							new StringBuffer("tipo inválido (").append(col.getTipo()).append(") tentando totalizar coluna não numérica") );
					
					throw new ParseFileException(msg);
					
				}
				
				for (int tt = 0; tt < tots.size(); tt++) {
					
						
					String totName = (String)tots.get(tt);
					
					if (read)
						getTotalizador(totName).addRead(valor);
					
					if (write) {
						Totalizador tot = getTotalizador(totName);
						
						tot.addWrite(valor);
						
						//valor = tot.getContadorWrite();
						// caso essa linha do layout possua totalizadores, é necessario setar
						// o valor calculado na coluna corresposndente. Senão o calculo de totalizadores
						// e a geração do layout não funcionam (isso somente para a geração)
						//if (valores != null) {
							//valores.setColunaTotalizador(colLay.getId(), valor);
							
							//valores.set(colLay.getId(), valor);
						//}
						
					}
					
					
				}
				
			}
		
		}
	}
/*	
	
	public boolean hasTotalizadores(String tipo) {
		// busca as concorrencias do tipo informado
		int pos = ParseUtils.getPosInMap(this.mapTotTipos,tipo);
		return (pos != -1);
	}
	
*/	
	
	
	public void clear() {
		if (this.linhas != null) {
			for (int l = 0; l < this.linhas.size(); l++) {
				((LinhaLayout)this.linhas.get(l)).clear();
			}
			this.linhas.clear();
			this.linhas = null;
		}
	}
	
	

	public ArrayList getLinhas() {
		return linhas;
	}
	public void setLinhas(ArrayList linhas) {
		this.linhas = linhas;
	}
	public DinVO getTotalizadores() {
		return totalizadores;
	}
	//public void setTotalizadores(DinVO totalizadores) {
	//	this.totalizadores = totalizadores;
	//}
	
	public int getTotalizadorIndex(String idTot) throws ParseFileException {
		return this.totalizadores.findInIds(idTot);
	}
	
	public Totalizador getTotalizador(String idTot) throws ParseFileException {
		int pos = this.totalizadores.findInIds(idTot);
		
		if (pos == -1) {
			throw new ParseFileException(new StringBuffer("O Totalizador id:").append(idTot).append(" não existe no layout").toString());
		}
		
		return (Totalizador)this.totalizadores.get(pos);
	}


	public Totalizador getTotalizador(int index) throws ParseFileException {
		return (Totalizador)totalizadores.get(index);
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
	
	public void addLinhaEspecial(String numLinha, String id) {
		if (Valida.isEmpty(linhasEspeciais)) {
			linhasEspeciais = new StringBuffer("|");
		}
		
		linhasEspeciais.append(numLinha).append(":").append(id).append("|");
	}
	
	public String getIdLinhaEspecial(long numLinha) {
		if (Valida.isEmpty(linhasEspeciais)) return null;
		
		String chave = new StringBuffer("|").append(numLinha).append(":").toString();
		
		int pos = linhasEspeciais.indexOf(chave);
		
		if (pos == -1) return null;
		
		int posf =  linhasEspeciais.indexOf("|", pos + 1);
		
		if (posf == -1) posf = linhasEspeciais.length();
		
		return linhasEspeciais.toString().substring(pos + chave.length(), posf );
	}
	
	/*
	public void setValReadTotalizador(int index, long value) throws ParseFileException {
		setValTotalizador(index, value, true);
	}
	
	public void setValReadTotalizador(String idTot, long value) throws ParseFileException {
		setValTotalizador(idTot, value, true);
	}
	
	public void setValWriteTotalizador(int index, long value) throws ParseFileException {
		setValTotalizador(index, value, false);
	}
	
	public void setValWriteTotalizador(String idTot, long value) throws ParseFileException {
		setValTotalizador(idTot, value, false);
	}
	
	private void setValTotalizador(String idTot, long value, boolean read) throws ParseFileException {
		int pos = ParseUtils.getPos(this.mapTotalizadores, idTot);
		
		if (pos == -1) {
			throw new ParseFileException(new StringBuffer("O Totalizador id:").append(idTot).append(" não exsite no layout").toString());
		}
		
		setValTotalizador(pos, value, read);
	}


	private void setValTotalizador(int index, long valor, boolean read) throws ParseFileException {
		if (read) {
			((Totalizador)totalizadores.get(index)).setContadorRead(valor);
		} else {
			((Totalizador)totalizadores.get(index)).setContadorWrite(valor);
		}
	}
	*/
	
}
