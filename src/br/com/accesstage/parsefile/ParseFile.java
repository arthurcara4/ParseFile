package br.com.accesstage.parsefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import br.com.accesstage.parsefile.daoutil.DaoBacthParse;
import br.com.accesstage.parsefile.daoutil.SqlExecVO;
import br.com.accesstage.parsefile.exceptions.ParseFileException;
import br.com.accesstage.parsefile.layout.ColunaLayout;
import br.com.accesstage.parsefile.layout.Layout;
import br.com.accesstage.parsefile.layout.LinhaLayout;
import br.com.accesstage.parsefile.layout.Totalizador;
import br.com.accesstage.parsefile.ocorrencias.Ocorrencias;
import br.com.accesstage.parsefile.retornos.DinVO;
import br.com.accesstage.parsefile.retornos.Valores;
import br.com.accesstage.parsefile.utils.ParseUtils;
import br.com.accesstage.parsefile.utils.Status;
import br.com.accesstage.parsefile.utils.StringUtil;
import br.com.accesstage.parsefile.utils.Valida;

/**
 * ParseFile é um componente construido para auxiliar a Leitura, Escrita e principalmente
 * Validação de arquivos CNAB e CVS.O ParseFile trabalha em um esquema de linha a linha do arquivo portanto para que ele funcione 
 * corretamente é necessário abrir o arquivo a ser manipulado e ler linha a linha, tanto leitura como gravação.
 * 
 * @author      Glauco A. Barroso
 */
public class ParseFile implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Layout layout;
	private boolean throwException = true;
	private boolean logException = false;
	private boolean trim = true;
	private boolean ignoreEmptyLine = false;
	private boolean totalizarRead = true;
	private boolean totalizarWrite = true;
	private boolean hasSqlExec = false;
	
	private boolean totalizarOcorrencias = true;
	
	private boolean completeAllLines = false;
	private boolean numericoCompleto = false;
	
	private DaoBacthParse daoBatch = null;
	//private DaoParse_old daoParse = null;
	private String gruposExecucaoSql = null;
	
	private String ante = ",ini,";
	private String post = null;
	
	public ParseFile() {}
	
	/*
	
	public ParseFile(Layout layout, boolean throwException, boolean logException) {
		this.layout = layout;
		setLogException(logException);
		setThrowException(throwException);
	}
	*/
	
	/**
	 * Retorna um objeto de Layout (Esse objeto contem todas as informações referentes ao layout instanciado pelo Parse)
	 */
	public Layout getLayout() {
		return layout;
	}
	
	/**
	 * Retorna um objeto de Layout (Esse objeto contem todas as informações referentes ao layout instanciado pelo Parse)
	 *
	 * @param  layout <br>Objeto de Layout Externo<br><br>
	 */
	public void setLayout(Layout layout) {
		this.layout = layout;
	}

	/**
	 * Cria uma instancia do ParseFile considerando o primeiro layout encontrado no xml
	 *
	 * @param  xmlLayout <br>Caminho absoluto (caminho, arquivo e extensão) do arquivo xml quem contém o layout que irá ser tratado<br><br>
	 * @throws ParseFileException
	 * @throws Exception
	 */
	public ParseFile(String xmlLayout) throws ParseFileException, Exception {
		this(xmlLayout, null, true, false);
	}
	
	/**
	 * Cria uma instancia do ParseFile
	 *
	 * @param  xmlLayout <br>Caminho absoluto (caminho, arquivo e extensão) do arquivo xml quem contém o layout que irá ser tratado<br><br>
	 * @param  layoutId  <br>Dentro do xml existe uma tag <layout> essa tag possui um atributo chamado id. Esse id deve ser passado nesse parâmetro<br><br> 
	 * @throws ParseFileException
	 * @throws Exception
	 */
	public ParseFile(String xmlLayout, boolean throwException, boolean logException) throws ParseFileException, Exception {
		this(xmlLayout, null, throwException, logException);
	}
	
	
	/**
	 * Cria uma instancia do ParseFile
	 *
	 * @param  xmlLayout <br>Caminho absoluto (caminho, arquivo e extensão) do arquivo xml quem contém o layout que irá ser tratado<br><br>
	 * @param  layoutId  <br>Dentro do xml existe uma tag <layout> essa tag possui um atributo chamado id. Esse id deve ser passado nesse parâmetro<br><br> 
	 * @throws ParseFileException
	 * @throws Exception
	 */
	public ParseFile(String xmlLayout, String layoutId) throws ParseFileException, Exception {
		this(xmlLayout, layoutId, true, false);
	}
	
	/**
	 * Cria uma instancia do ParseFile definindo o controle de log de parse.
	 *
	 * @param  xmlLayout <br>Caminho absoluto (caminho, arquivo e extensão) do arquivo xml quem contém o layout que irá ser tratado<br><br>
	 * 
	 * @param  layoutId  <br>Dentro do xml existe uma tag <layout> essa tag possui um atributo chamado id. Esse id deve ser passado nesse parâmetro<br><br>
	 * 
	 * @param  throwException <br>Caso esse parametro seja informado como <b>true</b> irá interromper a exceução na primeira exceção encontrada durante o processo<br>Caso esse parametro seja informado como <b>false</b> irá ignorar as exceções e executar o processo até p final<br><br>
	 * 
	 * @param  logException <br>Caso esse parametro seja informado como <b>true</b> irá gravar (logar) um objeto de retorno com as exceções que foram encontradas durante o processo
	 *                        <br>Caso esse parametro seja informado como <b>false</b> não gera o log de exceções<br><br>
	 *  
	 * @throws ParseFileException
	 * @throws Exception
	 */
	public ParseFile(String xmlLayout, String layoutId, boolean throwException, boolean logException) throws ParseFileException, Exception {
		InputStream ip = null;
		try {
			ip = new FileInputStream(new File(xmlLayout));
			construtor(ip, xmlLayout, layoutId, throwException, logException);
		} catch (ParseFileException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		} finally {
			if (ip != null) {
				ip.close();
				ip = null;
			}
		}
	}
	
	/**
	 * Cria uma instancia do ParseFile através de um xml já aberto em InputStream
	 *
	 * @param  xmlFileLayout <br>InputStream do layout xml já instanciado<br><br>
	 * 
	 * @param  xmlFileName  <br>nome do arquivo apenas para efeito de log em caso de erros<br><br>
	 * 
	 * @param  layoutId <br>Dentro do xml existe uma tag <layout> essa tag possui um atributo chamado id. Esse id deve ser passado nesse parâmetro<br><br>
	 * 
	 * @throws ParseFileException
	 */
	public ParseFile(InputStream xmlFileLayout, String xmlFileName, String layoutId) throws ParseFileException {
		this(xmlFileLayout, xmlFileName, layoutId, true, false);
	}
	
	/**
	 * Cria uma instancia do ParseFile através de um xml já aberto em InputStream definindo o controle de log de parse
	 *
	 * @param  xmlFileLayout <br>InputStream do layout xml já instanciado<br><br>
	 * 
	 * @param  xmlFileName  <br>nome do arquivo apenas para efeito de log em caso de erros<br><br>
	 * 
	 * @param  layoutId <br>Dentro do xml existe uma tag <layout> essa tag possui um atributo chamado id. Esse id deve ser passado nesse parâmetro<br><br>
	 * 
	 * @param  throwException <br>Caso esse parametro seja informado como <b>true</b> irá interromper a exceução na primeira exceção encontrada durante o processo<br>Caso esse parametro seja informado como <b>false</b> irá ignorar as exceções e executar o processo até p final<br><br>
	 * 
	 * @param  logException <br>Caso esse parametro seja informado como <b>true</b> irá gravar (logar) um objeto de retorno com as exceções que foram encontradas durante o processo
	 *                        <br>Caso esse parametro seja informado como <b>false</b> não gera o log de exceções<br><br>
	 *  
	 * @throws ParseFileException
	 */
	public ParseFile(InputStream xmlFileLayout, String xmlFileName, String layoutId, boolean throwException, boolean logException) throws ParseFileException {
		construtor(xmlFileLayout, xmlFileName, layoutId, throwException, logException);
	}
	
	private void construtor(InputStream xmlFileLayout, String xmlFileName, String layoutId, boolean throwException, boolean logException) throws ParseFileException {
		montaLayoutXml(xmlFileLayout, xmlFileName, layoutId);
		
		setLogException(logException);
		setThrowException(throwException);
	}
	
	/**
	 * Verifica se o parse está setado para interromper o processo em caso de exceção
	 *
	 * @return  booelan <br><b>true</b> - cancela a execução em caso de exceção<br><b>false</b> - continua a execução em caso de exceção<br><br>
	 * 
	 */
	public boolean isThrowException() {
		return throwException;
	}
	
	
	
	/**
	 * Seta o parse para interromper ou não o processo em caso de exceção
	 * 
	 * @param throwException <br>Caso esse parametro seja informado como <b>true</b> irá interromper a exceução na primeira exceção encontrada durante o processo<br>Caso esse parametro seja informado como <b>false</b> irá ignorar as exceções e executar o processo até p final<br><br>
	 */
	public void setThrowException(boolean throwException) {
		this.throwException = throwException;
	}
	
	/**
	 * Verifica se o parse está setado para logar ou não as exceções encontradas durante o processo
	 * 
	 * @return  booelan <br><b>true</b> - está logando as exceções<br><b>false</b> - não está logando as exceções<br><br>
	 */
	public boolean isLogException() {
		return logException;
	}
	
	/**
	 * Seta o parse para logar ou não as exceções encontradas durante o processo
	 * 
	 * @param logException <br><b>true</b> - loga as exceções<br><b>false</b> - não loga as exceções<br><br>
	 */
	public void setLogException(boolean logException) {
		this.logException = logException;
	}
	
	/**
	 * Verifica se o parse está retirando os espaços ao manipular as informações (defaul - false) 
	 * 
	 * @return boolean <br><b>true</b> - retira os espaços<br><b>false</b> - mantém a informação como veio do arquivo<br><br>
	 */
	public boolean isTrim() {
		return trim;
	}
	
	/**
	 * Seta o parse para retirar ou não os espaços ao manipular as informações (defaul - false) 
	 * 
	 * @param trim <br><b>true</b> - retira os espaços<br><b>false</b> - mantém a informação como veio do arquivo<br><br>
	 */
	public void setTrim(boolean trim) {
		this.trim = trim;
	}
	
	/**
	 * Verifica o status do Parse se irá ignorar linhas em branco do arquivo 
	 * 
	 * @return boolean <br><b>true</b> - ignora linhas em branco<br><b>false</b> - valida linhas em branco<br><br>
	 */
	public boolean isIgnoreEmptyLine() {
		return ignoreEmptyLine;
	}
	
	/**
	 * Seta o status do Parse para ignorar ou não linhas em branco do arquivo 
	 * 
	 * @param ignoreEmptyLine <br><b>true</b> - ignora linhas em branco<br><b>false</b> - valida linhas em branco<br><br>
	 */
	public void setIgnoreEmptyLine(boolean ignoreEmptyLine) {
		this.ignoreEmptyLine = ignoreEmptyLine;
	}
	
	/**
	 * Verifica se o parse está totalizado/contabilizando as linhas de leitura e gravação 
	 * 
	 * @return boolean <br><b>true</b> - esta totalizando<br><b>false</b> - está ignorando os totalizadores<br><br>
	 */
	public boolean isTotalizar() {
		return (totalizarRead && totalizarWrite);
	}
	
	/**
	 * Verifica se o parse está totalizado/contabilizando as linhas de leitura  (metodo converteLinha) 
	 * 
	 * @return boolean <br><b>true</b> - esta totalizando<br><b>false</b> - está ignorando os totalizadores<br><br>
	 */
	public boolean isTotalizarRead() {
		return totalizarRead;
	}
	
	/**
	 * Verifica se o parse está totalizado/contabilizando as linhas de geração (metodo gerarLinha) 
	 * 
	 * @return boolean <br><b>true</b> - esta totalizando<br><b>false</b> - está ignorando os totalizadores<br><br>
	 */
	public boolean isTotalizarWrite() {
		return totalizarWrite;
	}
	
	
	/**
	 * Verifica se o parse está totalizado/contabilizando as ocorrências de arquivo 
	 * 
	 * @return boolean <br><b>true</b> - esta totalizando<br><b>false</b> - está ignorando os totalizadores<br><br>
	 */
	public boolean isTotalizarOcorrencias() {
		return totalizarOcorrencias;
	}

	/**
	 * Seta seta um flag para totalizar/contabilizar as ocorrências
	 * 
	 * @param totalizarOcorrencias <br><b>true</b> - habilita a totalização de Ocorrências<br><b>false</b> - desabilita a totalização de Ocorrências<br><br>
	 */
	public void setTotalizarOcorrencias(boolean totalizarOcorrencias) {
		this.totalizarOcorrencias = totalizarOcorrencias;
	}

	/**
	 * Seta seta um flag para totalizar/contabilizar as linhas de leitura e gravação 
	 * 
	 * @param totalizar <br><b>true</b> - habilita a totalização<br><b>false</b> - desabilita a totallização<br><br>
	 */
	public void setTotalizar(boolean totalizar) {
		this.totalizarRead = totalizar;
		this.totalizarWrite = totalizar;
	}
	
	/**
	 * Seta seta um flag para totalizar/contabilizar as linhas de leitura (metodo converteLinha) 
	 * 
	 * @param totalizar <br><b>true</b> - habilita a totalização<br><b>false</b> - desabilita a totallização<br><br>
	 */
	public void setTotalizarRead(boolean totalizar) {
		this.totalizarRead = totalizar;
	}
	
	/**
	 * Seta seta um flag para totalizar/contabilizar as linhas de gravacao (metodo gerarLinha) 
	 * 
	 * @param totalizar <br><b>true</b> - habilita a totalização<br><b>false</b> - desabilita a totallização<br><br>
	 */
	public void setTotalizarWrite(boolean totalizar) {
		this.totalizarWrite = totalizar;
	}
	
	
	/**
	 * Verifica se o parse está configurado para completar com espaços em branco as linhas para poder efetivar o parse 
	 * 
	 * @return boolean <br><b>true</b> - esta completanto<br><b>false</b> - mantém a linha original<br><br>
	 */
	public boolean isCompleteAllLines() {
		return completeAllLines;
	}

	/**
	 * Seta seta um flag para o parse completar as linhas automaticamente com espaços em branco 
	 * 
	 * @param completeAllLines <br><b>true</b> - completa as linhas com espaços em branco<br><b>false</b> - mantém a linha original<br><br>
	 */
	public void setCompleteAllLines(boolean completeAllLines) {
		this.completeAllLines = completeAllLines;
	}
	
	
	/**
	 * Verifica se os campos numéricos são totalmente numeros
	 * 
	 * @return boolean <br><b>true</b> - gera erro caso nao esteja completo com numeros inclusive com zeros a esquerda <br><b>false</b>(default) - so invalida caso possua caracteres nao numericos<br><br>
	 */
	public boolean isNumericoCompleto() {
		return numericoCompleto;
	}

	/**
	 * Seta seta um flag que obriga o parse a validar campos numericos completos 
	 * 
	 * @param numericoCompleto <br><b>true</b> - gera erro caso nao esteja completo com numeros inclusive com zeros a esquerda <br><b>false</b>(default) - so invalida caso possua caracteres nao numericos<br><br>
	 */
	public void setNumericoCompleto(boolean numericoCompleto) {
		this.numericoCompleto = numericoCompleto;
	}
	

	/**
	 * Retorna quais Grupos de Execução SQL foram definidos para esse layout
	 * 
	 * @return String <br>nomes do Grupos de Excução SQL separados por vírgula<br><br>
	 */
	public String getGruposExecucaoSql() {
		return gruposExecucaoSql;
	}

	/**
	 * É possível definir Grupos de Execução SQL para o Layout. Isso serve para definir todas as instruções SQL em um mesmo layout porém, determinar
	 * através do processo quais as instruções que serão realmente executadas   
	 * 
	 * @param gruposExecucaoSql <br>Informar os ids de grupos separados por vírgula<br><br>
	 */
	public void setGruposExecucaoSql(String gruposExecucaoSql) {
		if(Valida.isEmpty(gruposExecucaoSql)) return;
		
		gruposExecucaoSql = gruposExecucaoSql.trim().replaceAll(" ", "");
		
		this.gruposExecucaoSql = new StringBuffer(",").append(gruposExecucaoSql).append(",").toString();
	}

	/**
	 * Transforma uma linha String em um objeto Valores que contém todas as informações relativas a essa linha
	 * 
	 * @param linha <br>linha extraida do arquivo que está sendo manipulado<br><br>
	 * @return Valores <br>Todas as informações relativas a linha (Informações das Colunas, Conteúdo das colunas, Tipo da linha, Id, etc.)<br><br>
	 * @throws ParseFileException 
	 */
	public Valores converteLinha(String linha) throws ParseFileException {
		return converteLinha(linha, 0);
	}
	
	/**
	 * Transforma uma linha String em um objeto Valores que contém todas as informações relativas a essa linha
	 * 
	 * @param linha <br>linha extraida do arquivo que está sendo manipulado<br><br>
	 * @param numLinha <br>Numero da linha com relação ao arquivo para efeito de log. Caso esse atributo não seja informado assume um contador interno que sempre é contabilizado.<br><br>
	 * @return Valores <br>Todas as informações relativas a linha (Informações das Colunas, Conteúdo das colunas, Tipo da linha, Id, etc.)<br><br>
	 * @throws ParseFileException
	 */
	public Valores converteLinha(String linha, long numLinha) throws ParseFileException {
		String idLinha = this.layout.getLinhaId(linha, numLinha);
		return converteLinha(idLinha,linha, numLinha);
	}

	/**
	 * Transforma uma linha String em um objeto Valores que contém todas as informações relativas a essa linha
	 * 
	 * @param idLinha <br>Força o ParseFile a utilizar o formato de linha informado nesse parametro.<br>Esse parametro se refere ao atributo id da tag <linha> do xml de layout.<br>Caso o layout xml seja construido corretamente o ParseFile possui a inteligência de identificar o tipo da linha e manipula-la corretamente, não sendo necessário informar esse parametro.<br><br>
	 * @param linha <br>linha extraida do arquivo que está sendo manipulado<br><br>
	 * @return Valores <br>Todas as informações relativas a linha (Informações das Colunas, Conteúdo das colunas, Tipo da linha, Id, etc.)<br><br>
	 * @throws ParseFileException
	 */
	public Valores converteLinha(String idLinha, String linha) throws ParseFileException {
		return converteLinha(idLinha, linha, 0);
	}

	/**
	 * Transforma uma linha String em um objeto Valores que contém todas as informações relativas a essa linha
	 * 
	 * @param idLinha <br>Força o ParseFile a utilizar o formato de linha informado nesse parametro.<br>Esse parametro se refere ao atributo id da tag <linha> do xml de layout.<br>Caso o layout xml seja construido corretamente o ParseFile possui a inteligência de identificar o tipo da linha e manipula-la corretamente, não sendo necessário informar esse parametro.<br><br>
	 * @param linha <br>linha extraida do arquivo que está sendo manipulado<br><br>
	 * @param numLinha <br>Numero da linha com relação ao arquivo para efeito de log. Caso esse atributo não seja informado assume um contador interno que sempre é contabilizado.<br><br>
	 * @return Valores <br>Todas as informações relativas a linha (Informações das Colunas, Conteúdo das colunas, Tipo da linha, Id, etc.)<br><br>
	 * @throws ParseFileException
	 */
	public Valores converteLinha(String idLinha, String linha, long numLinha) throws ParseFileException {
		LinhaLayout linhaLay = this.layout.getLinha(idLinha);
		return converteColuna(idLinha, linha, linhaLay, linhaLay.getColunas(), numLinha);
	}
	
	/**
	 * Transforma uma linha String em um objeto Valores que contém todas as informações relativas a essa linha porém somente considerando as colunas informadas
	 * 
	 * @param idLinha <br>Força o ParseFile a utilizar o formato de linha informado nesse parametro.<br>Esse parametro se refere ao atributo id da tag <linha> do xml de layout.<br>Caso o layout xml seja construido corretamente o ParseFile possui a inteligência de identificar o tipo da linha e manipula-la corretamente, não sendo necessário informar esse parametro.<br><br>
	 * @param linha <br>linha extraida do arquivo que está sendo manipulado<br><br>
	 * @param colunas <br>Informa em uma String os id's das colunas (atributo id da tag <coluna> do arquivo xml de layout), separados por vírgula, que deseja que sejam retronadas no objeto de Valores.<br><br>
	 * @return Valores <br>Todas as informações relativas a linha (Informações das Colunas, Conteúdo das colunas, Tipo da linha, Id, etc.)<br><br>
	 * @throws ParseFileException
	 */
	public Valores converteColuna(String idLinha, String linha, String colunas) throws ParseFileException {
		return converteColuna(idLinha, linha, colunas, 0);
	}

	/**
	 * Transforma uma linha String em um objeto Valores que contém todas as informações relativas a essa linha porém somente considerando as colunas informadas
	 * 
	 * @param idLinha <br>Força o ParseFile a utilizar o formato de linha informado nesse parametro.<br>Esse parametro se refere ao atributo id da tag <linha> do xml de layout.<br>Caso o layout xml seja construido corretamente o ParseFile possui a inteligência de identificar o tipo da linha e manipula-la corretamente, não sendo necessário informar esse parametro.<br><br>
	 * @param linha <br>linha extraida do arquivo que está sendo manipulado<br><br>
	 * @param colunas <br>Informa em uma String os id's das colunas (atributo id da tag <coluna> do arquivo xml de layout), separados por vírgula, que deseja que sejam retronadas no objeto de Valores.<br><br>
	 * @param numLinha <br>Numero da linha com relação ao arquivo para efeito de log. Caso esse atributo não seja informado assume um contador interno que sempre é contabilizado.<br><br>
	 * @return Valores <br>Todas as informações relativas a linha (Informações das Colunas, Conteúdo das colunas, Tipo da linha, Id, etc.)<br><br>
	 * @throws ParseFileException
	 */
	public Valores converteColuna(String idLinha, String linha, String colunas, long numLinha) throws ParseFileException {
		LinhaLayout linhaLay = this.layout.getLinha(idLinha);
		return converteColuna(idLinha, linha, linhaLay, linhaLay.getColunas(colunas), numLinha);
	}
	
	private Valores converteColuna(String idLinha, String linha, LinhaLayout linhaLay, ArrayList cols, long numLinha) throws ParseFileException {
		
		//verifica se ira contabilizar/totalizar a linha
		if (totalizarRead) {
			this.layout.totaliza("#SEQ#","#SOMATIPO#",1,true,false);
		}
		
		// Caso o idLinha venha com o valor "#" é porque não deve efetuar a validação dessa linha específica (vide métodos: layout.getLinha() e layout.getLinhaId()
		// para maiores esclarecimentos
		// 06/08/2010 - Tambem sai por essa condicao quando na tag de configurações for setada a propriedade <ignoreLinhasNaoDeclaradas  value='true'/> 
		if ("#".equals(idLinha)) {
			return null;
		}
		
		
		// informa que esse tipo de linha foi processada na leitura de arquivo
		linhaLay.setRead(true);
		if (numLinha <= 0) {
			numLinha = this.layout.getTotalizador("#seqInterno").getContadorRead();
		}
		
		if (Valida.isEmpty(linha)) {
			
			if (isIgnoreEmptyLine()) {
				return null;
			}
			
			
			// Linha tipo 'id' (tipo) numero<num>: linha em branco
			
			StringBuffer msg = ParseUtils.msgErro(this.layout.isDelimitado(), this.layout.getId(), idLinha, linhaLay.getTipo(), numLinha, null, -1,
					"linha em branco"  );
			
			// isso foi feito na tentativa de dar continuidade ao parse do arquivo caso venham linhas em branco
			// e o isIgnoreEmptyLine estaja setado como false
			//
			ArrayList colBranco = new ArrayList();
			colBranco.add(new ColunaLayout("em_branco","",1,1,null,null,null,null,null,null,true,0,false,false,null,false,null,null,null,null,null, -1));
			
			Valores val = new Valores(
					new StringBuffer().append(ParseUtils.getIdStr("em_branco",0))
					, new String[] {""}
					, ""
					, ""
					, colBranco
					, log(msg,0,"em_branco",null, Status.LINHA_EM_BRANCO, 1, "", "")
					, new StringBuffer());
			
			return val;
			//throw new ParseFileException(Status.LINHA_EM_BRANCO,  msg);
		}
		
		linha = linha.replaceAll("\r","").replaceAll("\n","");
		
		// Linha tipo 'id' (tipo) numero<num>: o tamanho da linha(tam) não corresponde ao tamanho informado no layout (tam_informado)
		if ((!this.layout.isDelimitado()) && (linhaLay.getTamanho() != linha.length())) {
			
			if (!this.completeAllLines && !linhaLay.isCompleteLinha()) {
				StringBuffer msg = ParseUtils.msgErro(this.layout.isDelimitado(), this.layout.getId(), idLinha, linhaLay.getTipo(), numLinha, null, -1,
						new StringBuffer("tamanho da linha (").append(linha.length()).append(") não corresponde ao tamanho informado no layout (").append(linhaLay.getTamanho()).append(")")  );
				
				throw new ParseFileException(Status.TAMANHO_LINHA_INVALIDO,  msg);
			} else {
				if (linhaLay.getTamanho() < linha.length()) {
					linha = linha.substring(0, linhaLay.getTamanho());
				}
				
				if (linhaLay.getTamanho() > linha.length()) {
					linha = new StringBuffer().append(linha).append(StringUtil.rpl(" ", (linhaLay.getTamanho() - linha.length())) ).toString();
				}
			}
			
		}
		
		String[] valores = new String[cols.size()];
		
		String[] linhaDelim = null;

		if (this.layout.isDelimitado()) {
			linhaDelim = this.layout.getDelimitedCols(linha, numLinha);
			
			// Linha numero<num>: Quantidade de colunas(qtde) não corresponde ao esperado(qtde_esperado)
			if (linhaDelim.length != valores.length) {
				StringBuffer msg = ParseUtils.msgErro(this.layout.isDelimitado(), this.layout.getId(), idLinha, linhaLay.getTipo(), numLinha, null, -1,
						new StringBuffer("Quantidade de colunas da linha (").append(linhaDelim.length).append(") não corresponde ao layout (").append(valores.length).append(")")  );
				
				
				
				throw new ParseFileException(Status.NUM_COLUNAS_INVALIDO,  msg);
			}
		}
		
		
		
		ParseFileException[] exceptions = null;
		StringBuffer mapValores = new StringBuffer();
		
		ArrayList tots = null;
		
		for (int c = 0; c < cols.size(); c++) {
			ColunaLayout col = (ColunaLayout)cols.get(c);
			mapValores.append(ParseUtils.getIdStr(col.getId(),c));
		}
		
		
		for (int c = 0; c < cols.size(); c++) {
			
			ColunaLayout col = (ColunaLayout)cols.get(c);
			
			try {
				if (!this.layout.isDelimitado()) {
					valores[c] = linha.substring(col.getPosIni() , col.getPosIni() + col.getTamanho());
				} else {
					valores[c] = linhaDelim[c];
				}
			} catch (IndexOutOfBoundsException ex) {
				StringBuffer msg = new StringBuffer();
				if (!this.layout.isDelimitado()) {
					// Linha tipo 'id' (tipo) numero<num>: coluna posicao inicial (posini) tamanho (tam): tamanho inválido (tam_arquivo)
					valores[c] = linha.substring(col.getPosIni());
					
					msg = ParseUtils.msgErro(this.layout.isDelimitado(), this.layout.getId(), idLinha, linhaLay.getTipo(), numLinha, col, c,
							new StringBuffer("tamanho inválido (").append(valores[c].length()).append(")") );
				} else {
					// Linha numero<num>: Não existe a coluna (numcol)
					msg = ParseUtils.msgErro(this.layout.isDelimitado(), this.layout.getId(), idLinha, linhaLay.getTipo(), numLinha, col, c,
							new StringBuffer(" Não existe a coluna (").append(c).append(")"));
				}
				
				exceptions = log(msg,c, col.getId(),exceptions, Status.TAMANHO_INVALIDO, cols.size(), linhaLay.getId(), linhaLay.getTipo());

				// adiciona erro de tamanho
			}
			
			String valOrig = valores[c];
			
			if ((isTrim()) || (this.layout.isDelimitado())) {
				valores[c] = valores[c].trim();
			}
			if (col.isUpper()) {
				valores[c] = valores[c].toUpperCase();
			}
			if (col.isLower()) {
				valores[c] = valores[c].toLowerCase();
			}
			
			//mapValores.append(ParseUtils.getIdStr(col.getId(),c));
			
			// 21/12/2010 - Glauco
			// por ter colocado o trim acima ocorria um problema de validacao de campos numericos, ele ignorava numeroc om espaços 
			// dizendo que os mesmos eram campos numerico validos, isso ficou até 12/2010 portando nao é permitido mudar para nao
			// dar pau nos processos que nao validam isso.
			// Um POG para isso foi passar o valor original e criar algumas confirgurações de layout para validar o valor original
			// no caso de campos numericos que estiverem com esse flag (vide configuraçõe->NumericoCompleto e coluna->numericoCompleto
			int ret = validaTipo(col, valores[c], valOrig);
			
			StringBuffer msg = null; 
			if (ret == Status.FORMATO_INVALIDO) {
				// Linha tipo 'id' (tipo) numero<num>: coluna posicao inicial (posini) tamanho (tam): formato inválido (formato_esperado)
				msg = ParseUtils.msgErro(this.layout.isDelimitado(), this.layout.getId(), idLinha, linhaLay.getTipo(), numLinha, col, c,
						new StringBuffer(" Não existe a coluna (").append(c).append(")"));
				
			}
			
			if (ret == Status.TIPO_INVALIDO) {
				// Linha tipo 'id' (tipo) numero<num>: coluna posicao inicial (posini) tamanho (tam): tipo inválido (tipo_esperado)
				msg = ParseUtils.msgErro(this.layout.isDelimitado(), this.layout.getId(), idLinha, linhaLay.getTipo(), numLinha, col, c,
						new StringBuffer("tipo inválido (").append(col.getTipo()).append(")"));
			}

			if (ret == Status.OBRIGATORIO) {
				// Linha tipo 'id' (tipo) numero<num>: coluna posicao inicial (posini) tamanho (tam): campo obrigatório não informado
				msg = ParseUtils.msgErro(this.layout.isDelimitado(), this.layout.getId(), idLinha, linhaLay.getTipo(), numLinha, col, c,
						new StringBuffer("campo obrigatório não informado"));
			}
			
			if (!validaDominio(valores[c],col.getDominio(),col.isObrigatorio())) {
				// Linha tipo 'id' (tipo) numero<num>: coluna posicao inicial (posini) tamanho (tam): valor inesperado para este campo (conteudo) esperado (esperado)
				msg = ParseUtils.msgErro(this.layout.isDelimitado(), this.layout.getId(), idLinha, linhaLay.getTipo(), numLinha, col, c,
						new StringBuffer("valor inesperado para este campo (").append(valores[c]).append(") esperado (").append(col.getDominio()).append(")") );
				
				ret = Status.DOMINIO_INVALIDO;
				
			}
			
			if (ret == Status.VALOR_INVALIDO) {
				// Linha tipo 'id' (tipo) numero<num>: coluna posicao inicial (posini) tamanho (tam): valor inválido (conteudo)
				msg = ParseUtils.msgErro(this.layout.isDelimitado(), this.layout.getId(), idLinha, linhaLay.getTipo(), numLinha, col, c,
						new StringBuffer("valor inválido (").append(valores[c]).append(")") );
			}
			
			if (msg != null) {
				exceptions = log(msg,c,col.getId(),exceptions, ret, cols.size(), linhaLay.getId(), linhaLay.getTipo());
			}
			
			if (this.layout.isValidaRead()) {
				// verifica se há totalizadores e marca a posição deles nas colunas
				if (!Valida.isEmpty(col.getTotalizador())) {
					if (tots == null) {
						tots = new ArrayList();
					}
					tots.add(String.valueOf(c));
				}
			}
			
			if (this.layout.isValidaRead() && totalizarRead) {
				// zera totalizadores de acordo com a solicitacao
				// os totalizadores podem ser separados por vírgula
				if (!Valida.isEmpty(col.getZerarTotalizadores())) {
					
					String totals[] = col.getZerarTotalizadores().split("\\,");
					
					for(int tt = 0; tt < totals.length; tt++) {
						
						String ttName = totals[tt];
						
						if (!Valida.isEmpty(ttName)) {
							ttName = ttName.trim();
							
							this.layout.getTotalizador(ttName).setContadorRead(0);
						}
					}
				}
			}
			
			// set de variaveis
			if (!Valida.isEmpty(col.getSetVariavel())) {
				this.layout.addsetVariavel(col.getSetVariavel(), valores[c]);
			}
			
			// get de variaveis
			if (!Valida.isEmpty(col.getGetVariavel())) {
				valores[c] = this.layout.getVariavel(col.getGetVariavel());
			}
			
			
		}
		
		//Valores val = new Valores(mapValores, valores, linhaLay.getId(), linhaLay.getTipo(), cols, exceptions, linhaLay.getMapTotalizadores());
		Valores val = new Valores(mapValores, valores, linhaLay.getId(), linhaLay.getTipo(), cols, exceptions, null);
		
		// validacao de estrutura
		if (!Valida.isEmpty(linhaLay.getAntes())) {
			if (linhaLay.getAntes().indexOf(ante) == -1) {
				
				StringBuffer m = ParseUtils.msgErro(this.layout.isDelimitado(), this.layout.getId(), idLinha, linhaLay.getTipo(), numLinha, null, 0 ,
						new StringBuffer("Erro de Estrutura: linha fora de estrutura - antes. linhaAnterior[" + ante.substring(1, (ante.length() -1) ) + "] esperadoAntes[" + linhaLay.getAntes().substring(1, (linhaLay.getAntes().length() -1) ) + "]") );
				
				throw new ParseFileException(m);
			}
		}
		
		ante = new StringBuffer(",").append(linhaLay.getTipo()).append(",").toString();
		
		if (!Valida.isEmpty(post)) {
			if (post.indexOf(ante) == -1) {
				
				StringBuffer m = ParseUtils.msgErro(this.layout.isDelimitado(), this.layout.getId(), idLinha, linhaLay.getTipo(), numLinha, null, 0 ,
						new StringBuffer("Erro de Estrutura: linha fora de estrutura - depois. esperado[" + post.substring(1, (post.length() -1) ) + "]") );
				
				throw new ParseFileException(m);
			}
		}
		
		post = linhaLay.getDepois();
		
		
		if (this.layout.isValidaRead() && totalizarRead) {
			// totalizadores de registros (leitura)
			this.layout.totaliza(null, val, true, false, numLinha);

			// verifica se existem totalizadores para essa linha
			// essa verificação foi feita nesse momento, pois para verificar
			// os totalizadores é preciso contabiliza-los primeiro
			if (!Valida.isEmpty(tots)) {
				for (int t = 0; t < tots.size(); t++) {
					int colIndex = Integer.parseInt((String)tots.get(t));
					
					ColunaLayout col = (ColunaLayout)cols.get(colIndex);
					
					int indexTot = this.layout.getTotalizadorIndex(col.getTotalizador());
					
					if (indexTot == -1) {
						// Erro no analise do arquivo: totalizador (id) não foi encontrado no layout
						throw new ParseFileException(new StringBuffer("o totalizador id: ").append(col.getTotalizador()).append(" não foi encontrado no layout."));
					}
					
					Totalizador tot = this.layout.getTotalizador(indexTot);
					
					
					StringBuffer msg = null;
					
				
					// o flag problem serve para identificar se esse contador possui um erro posteiror,
					// isso é feito para não ficar exibindo erros caso já tenha ocorrido um anteriormente
					// o exempro de um sequence é um caso desses, em caso de quebra de sequence, só aponta
					// erro onde houve a quebra
					if ((val.getLong(col.getId()) != tot.getContadorRead()) && (!tot.isProblem())) {
						// Linha tipo 'id' (tipo) numero<num>: coluna posicao inicial (posini) tamanho (tam): totalizador (id) com valor diferente do esperado, informado (totalizador_informado) esperado (valor_calculado)
						
						msg = ParseUtils.msgErro(this.layout.isDelimitado(), this.layout.getId(), idLinha, linhaLay.getTipo(), numLinha, col, colIndex,
								new StringBuffer("O totalizador:").append(tot.getId()).append(" deu diferença. Informado (").append(val.getLong(col.getId())).append(") esperado (").append(tot.getContadorRead()).append(")") );
						
					}
					
					if (msg != null) {
						tot.setProblem(true);
						exceptions = log(msg,colIndex,col.getId(),exceptions, Status.TOTALIZADOR_INVALIDO, cols.size(), linhaLay.getId(), linhaLay.getTipo());
					}
					
					// ao validar os totalizadores, ele os zera para iniciar uma nova contagem
					// a não ser que o flag zerar esteja como false (isso deve ser setado para
					// sequenciais ou totalizadores que somam todo o arquivo);
					if (col.isZerar()) {
						tot.setProblem(false);
						this.layout.getTotalizador(indexTot).setContadorRead(0);
					}
				}
				
				val.setExceptions(exceptions);
	
			}
		}
		
		if (this.layout.isOcorrencias() && totalizarRead) {
			if (totalizarOcorrencias) {
				this.layout.getOcorrencias().soma(val);
			}
		}
		
		// verifica se existem execucoes SQL nesta linha
		execSql(linhaLay, val, numLinha, layout.getTotalizadores(), true);		
		
		
		return val;
	}
	
	private void execSql(LinhaLayout linhaLay, Valores val, long numLinha, DinVO tots, boolean read) throws ParseFileException {
		// verifica se existem execucoes SQL nesta linha
		if (linhaLay.hasExecucoesSql()) {
			
			if (daoBatch == null) daoBatch = new DaoBacthParse();
			
			LinkedList list = linhaLay.getExecucoesSql();
			for (int es = 0; es < list.size(); es++) {
				SqlExecVO vo = (SqlExecVO)list.get(es);
				
				// se forem definidos gurpos de validação e se a execução pertence a um grupo verifica se a cbatch sera executada
				if ( (!Valida.isEmpty(gruposExecucaoSql)) && (!Valida.isEmpty(vo.getIdGrupo())) ) {
					if (gruposExecucaoSql.indexOf(vo.getIdGrupoFmt()) == -1) continue;
				}
				
				
				setOper(vo,"setPreExec", layout.getVariaveis(), val, tots, read);
				
				daoBatch.exec(vo, layout.getVariaveis() , val, numLinha, tots, read);
				
				hasSqlExec = true;
				
				setOper(vo,"setPosExec", layout.getVariaveis(), val, tots, read);
				
			}
		}
		
		
	}
	
	private void setOper(SqlExecVO vo, String attrib, DinVO var, Valores val, DinVO tots, boolean read) throws ParseFileException {
		
		String expr = "";

		if ("setPreExec".equals(attrib)) {
			expr = vo.getPre();
		} else {
			expr = vo.getPos();
		}
		
		if (Valida.isEmpty(expr)) return;
		
		String ex[] = expr.split("\\;");
		
		for (int ee = 0; ee < ex.length; ee++) {
			String oper = ex[ee];
			if ( (Valida.isEmpty(oper)) || (oper.indexOf("=") == -1) ) {
				throw new ParseFileException("Erro de Layout: Operacao invalida no atributo '" + attrib + "' da tag <execSql>. linhaId[" + val.getLinhaId() + "]");
			}
			String opp[] = oper.split("\\=");
			if (( opp.length != 2 ) || Valida.isEmpty(opp[0]) || Valida.isEmpty(opp[1]) ) {
				throw new ParseFileException("Erro de Layout: Operacao invalida no atributo '" + attrib + "' da tag <execSql>. linhaId[" + val.getLinhaId() + "]");
			}
			
			if (opp[0].indexOf("v:") != -1) {
				String target =  opp[0].replaceAll("v:", "").trim();
				if (opp[1].indexOf("v:") != -1) {
					String source =  opp[1].replaceAll("v:", "").trim();
					var.addset(target,var.getString(source));
				}
				if (opp[1].indexOf("t:") != -1) {
					String source =  opp[1].replaceAll("t:", "").trim();
					
					if (Valida.isEmpty(tots.get(source))) {
						throw new ParseFileException("Erro de Layout: Totalizador " + source + " nao encontrado nos parametros SQL de setVariavel");
					}
					
					if (read) var.addset(target, ((Totalizador)tots.get(source)).getContadorRead());
					if (!read) var.addset(target, ((Totalizador)tots.get(source)).getContadorWrite());
				}
				if (opp[1].indexOf("c:") != -1) {
					String source =  opp[1].replaceAll("c:", "").trim();
					var.addset(target,val.getString(source));
				}
			}
			
			if (opp[0].indexOf("c:") != -1) {
				String target =  opp[0].replaceAll("c:", "").trim();
				if (opp[1].indexOf("v:") != -1) {
					String source =  opp[1].replaceAll("v:", "").trim();
					val.set(target,var.getString(source));
				}
				if (opp[1].indexOf("t:") != -1) {
					String source =  opp[1].replaceAll("t:", "").trim();
					
					if (Valida.isEmpty(tots.get(source))) {
						throw new ParseFileException("Erro de Layout: Totalizador " + source + " nao encontrado nos parametros SQL de setVariavel");
					}
					
					if (read) val.set(target, ((Totalizador)tots.get(source)).getContadorRead());
					if (!read) val.set(target, ((Totalizador)tots.get(source)).getContadorWrite());
				}
				if (opp[1].indexOf("c:") != -1) {
					String source =  opp[1].replaceAll("c:", "").trim();
					val.set(target,val.getString(source));
				}
			}
			
		}
		
	}
	
	private ParseFileException[] log(StringBuffer msg, int index, String idColuna, ParseFileException[] exceptions, int codigo, int size, String idLinha, String tipoLinha) throws ParseFileException {
		// validação de tamanho de coluna
		if (throwException || logException) {
			
			ParseFileException p = new ParseFileException(codigo,idColuna, msg.toString(), idLinha, tipoLinha);
			
			if (throwException) {
				throw p;
			}
			
			if (logException) {
				if (exceptions == null) {
					exceptions = new ParseFileException[size];
				}
				exceptions[index] = p;
				
				return exceptions;
			}
		}
		
		return null;
	}
	
	
	/**
	 * Gera uma linha em formato String contendo as informações do objeto Valores corretamente distribuidas por tipo e tamanho de acordo com o arquivo xml de layout e o id da linha (propriedade linhaId do objeto Valores)
	 * O Objeto Valores deve conter as informações que a linha possuir.
	 * Obs: em caso de Arquivo CSV constro a linha com o delimitador infromado no layout
	 * 
	 * @param valores <br>Objeto de valores previamente preenchido com as informações que a linha deverá conter<br><br>
	 * @return String <br>Linha montada de acordo com o layout xml. Essa linha deve ser escrita no arquivo de saída<br><br>
	 * @throws ParseFileException
	 */
	public String geraLinha(Valores valores) throws ParseFileException {
		return geraLinha(valores, true);
	}
	
	/**
	 * Gera uma linha de determinado ID (id da linha no layout) com as informações do DinVO lembrando que os ids do DinVO devem corresponder aos ids das colunas no xml de layout
	 * 
	 * @param valores <br>DinVO de valores previamente preenchido com as informações que a linha deverá conter<br><br>
	 * @param linhaId <br>Id da linha no XML de layout<br><br>
	 * @return String <br>Linha montada de acordo com o layout xml. Essa linha deve ser escrita no arquivo de saída<br><br>
	 * @throws ParseFileException
	 */
	public String geraLinha(DinVO valores, String linhaId) throws ParseFileException {
		return geraLinha(valores, linhaId, true);
	}

	/**
	 * Gera uma linha em formato String contendo as informações do objeto Valores corretamente distribuidas por tipo e tamanho de acordo com o arquivo xml de layout e o id da linha (propriedade linhaId do objeto Valores)
	 * O Objeto Valores deve conter as informações que a linha possuir.
	 * Obs: em caso de Arquivo CSV constro a linha com o delimitador infromado no layout
	 * 
	 * @param valores <br>Objeto de valores previamente preenchido com as informações que a linha deverá conter<br><br>
	 * @param linebreak <br><b>true</b> - (default) insere os caracteres \r\n ao final da linha gerada<br><b>false</b> - não insere os caracteres de final de linha<br><br>
	 * @return String <br>Linha montada de acordo com o layout xml. Essa linha deve ser escrita no arquivo de saída<br><br>
	 * @throws ParseFileException
	 */
	public String geraLinha(Valores valores, boolean linebreak) throws ParseFileException {
		return geraLinha(valores, valores.getLinhaId(), linebreak, true);
	}
	
	/**
	 * Gera uma linha de determinado ID (id da linha no layout) com as informações do DinVO lembrando que os ids do DinVO devem corresponder aos ids das colunas no xml de layout
	 * 
	 * @param valores <br>DinVO de valores previamente preenchido com as informações que a linha deverá conter<br><br>
	 * @param linhaId <br>Id da linha no XML de layout<br><br>
	 * @param linebreak <br><b>true</b> - (default) insere os caracteres \r\n ao final da linha gerada<br><b>false</b> - não insere os caracteres de final de linha<br><br>
	 * @return String <br>Linha montada de acordo com o layout xml. Essa linha deve ser escrita no arquivo de saída<br><br>
	 * @throws ParseFileException
	 */
	public String geraLinha(DinVO valores, String linhaId, boolean linebreak) throws ParseFileException {
		return geraLinha(valores, linhaId, linebreak, true);
	}
	
	/**
	 * Gera uma linha em formato String contendo as informações do objeto Valores corretamente distribuidas por tipo e tamanho de acordo com o arquivo xml de layout e o id da linha (propriedade linhaId do objeto Valores)
	 * O Objeto Valores deve conter as informações que a linha possuir.
	 * Obs: em caso de Arquivo CSV constro a linha com o delimitador infromado no layout
	 * 
	 * @param valores <br>Objeto de valores previamente preenchido com as informações que a linha deverá conter<br><br>
	 * @param linebreak <br><b>true</b> - (default) insere os caracteres \r\n ao final da linha gerada<br><b>false</b> - não insere os caracteres de final de linha<br><br>
	 * @param totaliza <br><b>true</b> - (default) atualiza os totalizadores informados no layout xml que corresponderem a esse tipo de linha<br><b>false</b> - não atualiza os totalizadores<br><br>
	 * @return String <br>Linha montada de acordo com o layout xml. Essa linha deve ser escrita no arquivo de saída<br><br>
	 * @throws ParseFileException
	 */
	public String geraLinha(Object valores, String linhaId, boolean linebreak, boolean totaliza) throws ParseFileException {
		
		// o flag totaliza é mais antigo portanto é mandatório
		/// se ele estiver como true considera o flag setado em contabilizar dai segue o processo normal 
		if (totaliza) {
			totaliza = totalizarWrite;
		}
		
		LinhaLayout linhaLay = this.layout.getLinha(linhaId);
		
		ArrayList cols =  linhaLay.getColunas();
		
		// informa que esse tipo de linha foi processada na geração de arquivo
		linhaLay.setWrite(true);
		if (totaliza)
			this.layout.totaliza("#SEQ#","#SOMATIPO#",1,false,true);
		//this.layout.totalizaInternalWrite();
		
		// não consegue totalizar DinVO por faltarem informações de totalizadores
		
		
		Valores valObj = null;
		
		if (valores instanceof Valores) {  
			valObj = (Valores)valores;
		}
			
		if (valores instanceof DinVO) {
			LinhaLayout lay = this.layout.getLinha(linhaId);
			
			valObj = getArray(linhaId, lay, lay.getColunas(), (DinVO)valores);
		}
		 
		// verifica se a o layout tem o mesmo tamanho dos valores
		if (cols.size() != valObj.getValores().length) {
			throw new ParseFileException("Quantidade de colunas do Layout diferente da quantidade de Valores");
		}

		long numLinha = this.layout.getTotalizador("#seqInterno").getContadorWrite();
		
		// totalizadores de registros (gravação)
		// IMPORTANTE: caso o geraLinha seja chamado mais de uma vez para a mesma linha de arquivo
		// isso causará um problema nos totalizadores, por isso o parametro totaliza (default é true)
		if ((this.layout.isValidaWrite()) && (totaliza)) {
			this.layout.totaliza(null, valObj, false, true, numLinha);
			//this.layout.totalizaWrite(valObj);
		}

		
		// verifica se existem execucoes SQL nesta linha
		execSql(linhaLay, valObj, numLinha, layout.getTotalizadores(), false);		
		
		String linha = null;
		
		for (int p = 0; p < cols.size(); p++) {
			ColunaLayout col = (ColunaLayout)cols.get(p);
			
			String val = valObj.getString(col.getId());
			
			if (("#tot#").equals(val)) {
				val = String.valueOf(this.layout.getTotalizador(col.getTotalizador()).getContadorWrite());
				
				valObj.set(col.getId(), val);
			}
			
			String campoStr = "";
			if (!this.layout.isDelimitado()) {
				campoStr = ParseUtils.formataValor(col.getTipo(), col.getPad() , col.getPadSide(), col.getDecimais(), val, col.getTamanho());
			} else {
				campoStr = ((Valida.isNull(val))?"":val.trim()) ;
				if (campoStr.indexOf(this.layout.getDelimitador()) != -1) {
					campoStr = new StringBuffer("\"").append(campoStr).append("\"").toString();
				}
			}
			
			if (col.isUpper()) {
				campoStr = campoStr.toUpperCase();
			}
			if (col.isLower()) {
				campoStr = campoStr.toLowerCase();
			}
			
			
			// set de variaveis
			if (!Valida.isEmpty(col.getSetVariavel())) {
				this.layout.addsetVariavel(col.getSetVariavel(), campoStr);
			}
			
			// get de variaveis
			if (!Valida.isEmpty(col.getGetVariavel())) {
				campoStr = this.layout.getVariavel(col.getGetVariavel());
			}

			campoStr = ParseUtils.formataValor(col.getTipo(), col.getPad() , col.getPadSide(), col.getDecimais(), campoStr, col.getTamanho());
			
			if (!this.layout.isDelimitado()) {
				linha = setLinha(linha, campoStr, col.getPosIni(), col.getTamanho());
			} else {
				boolean ultima = (p ==  (cols.size() - 1));
				linha = setLinhaDelim(linha, campoStr, ultima);
			}

			if ((this.layout.isValidaWrite()) && (totaliza)) {
				if ( (col.isZerar())  && (!Valida.isEmpty(col.getTotalizador())) ) {
					this.layout.getTotalizador(col.getTotalizador()).setContadorWrite(0);
					//this.layout.getTotalizador(col.getTotalizador()).setProblem(false);
				}
				
				// zera totalizadores de acordo com a solicitacao
				// os totalizadores podem ser separados por vírgula
				if (!Valida.isEmpty(col.getZerarTotalizadores())) {
					
					String totals[] = col.getZerarTotalizadores().split("\\,");
					
					for(int tt = 0; tt < totals.length; tt++) {
						
						String ttName = totals[tt];
						
						if (!Valida.isEmpty(ttName)) {
							ttName = ttName.trim();
							
							this.layout.getTotalizador(ttName).setContadorWrite(0);
						}
					}
				}
				
			}

			
		}
		
		if (linebreak) {
			linha = new StringBuffer(linha).append("\r\n").toString();
		}
		

		return linha;
	}
	
	/*
	public String geraLinha(Valores valores, boolean linebreak, boolean totaliza) throws ParseFileException {
		
		// o flag totaliza é mais antigo portanto é mandatório
		/// se ele estiver como true considera o flag setado em contabilizar dai segue o processo normal 
		if (totaliza) {
			totaliza = totalizar;
		}
		
		LinhaLayout linhaLay = this.layout.getLinha(valores.getLinhaId());
		
		ArrayList cols =  linhaLay.getColunas();
		 
		// informa que esse tipo de linha foi processada na geração de arquivo
		linhaLay.setWrite(true);
		this.layout.totalizaInternalWrite();
		 
		// verifica se a o layout tem o mesmo tamanho dos valores
		if (cols.size() != valores.getValores().length) {
			throw new ParseFileException("Quantidade de colunas do Layout diferente da quantidade de Valores");
		}
		
		// totalizadores de registros (gravação)
		// IMPORTANTE: caso o geraLinha seja chamado mais de uma vez para a mesma linha de arquivo
		// isso causará um problema nos totalizadores, por isso o parametro totaliza (default é true)
		if ((this.layout.isValidaWrite()) && (totaliza)) {
			this.layout.totalizaWrite(valores);
		}
		
		String linha = null;
		
		for (int p = 0; p < cols.size(); p++) {
			ColunaLayout col = (ColunaLayout)cols.get(p);
			
			
			
			String val =  valores.getString(col.getId());
			if (("#tot#").equals(val)) {
				val = String.valueOf(this.layout.getTotalizador(col.getTotalizador()).getContadorWrite());
			}
			
			String campoStr = "";
			if (!this.layout.isDelimitado()) {
				campoStr = ParseUtils.formataValor(col.getTipo(), col.getPad() , col.getPadSide(), col.getDecimais(), val, col.getTamanho());
			} else {
				campoStr = ((Valida.isNull(val))?"":val.trim()) ;
				if (campoStr.indexOf(this.layout.getDelimitador()) != -1) {
					campoStr = new StringBuffer("\"").append(campoStr).append("\"").toString();
				}
			}
			
			if (col.isUpper()) {
				campoStr = campoStr.toUpperCase();
			}
			if (col.isLower()) {
				campoStr = campoStr.toLowerCase();
			}
			
			
			if (!this.layout.isDelimitado()) {
				linha = setLinha(linha, campoStr, col.getPosIni(), col.getTamanho());
			} else {
				boolean ultima = (p ==  (cols.size() - 1));
				linha = setLinhaDelim(linha, campoStr, ultima);
			}

			if ((this.layout.isValidaWrite()) && (totaliza) && (!Valida.isEmpty(col.getTotalizador()))) {
				if (col.isZerar()) {
					this.layout.getTotalizador(col.getTotalizador()).setContadorWrite(0);
					//this.layout.getTotalizador(col.getTotalizador()).setProblem(false);
				}
			}

			
		}
		
		if (linebreak) {
			linha = new StringBuffer(linha).append("\r\n").toString();
		}
		

		return linha;
	}
	*/
	
	private String setLinha(String linha, String valor, int pos, int tamanho) throws ParseFileException {
		
		linha = ParseUtils.rpad(linha," ", (pos + tamanho) ); 
		
		StringBuffer linhaBuff = new StringBuffer(linha.substring(0, pos));
		
		linhaBuff.append(valor.substring(0, tamanho));
		
		linhaBuff.append(linha.substring((pos + tamanho)));
		
		return linhaBuff.toString();
	}

	private String setLinhaDelim(String linha, String valor, boolean ultima) throws ParseFileException {
		
		StringBuffer linhaBuff = new StringBuffer((linha == null)?"":linha);
		
		linhaBuff.append(valor);
		
		if (!ultima) {
			linhaBuff.append(this.layout.getDelimitador());
		}
		
		return linhaBuff.toString();
	}

	/**
	 * Retorna um objeto Valores contendo as informações relativas as colunas do id da linha informado. Esse metodo é utilizado para efetuar uma pre-carga no objeto valores, para que os respectivos conteúdos sejam informados posteriormente
	 *  
	 * @param idLinha <br>Esse parametro deve ser informado com o conteúdo do atributo id na tag <linha> do layout xml<br><br>
	 * @return Valores <br>Todas as informações relativas a esse tipo de linha conforme o layout xml (Informações das Colunas, Tipo da linha, Id, etc.)<br><br>
	 * @throws ParseFileException
	 */
	public Valores getArray(String idLinha) throws ParseFileException {
		LinhaLayout linhaLay = this.layout.getLinha(idLinha);
		return getArray(idLinha, linhaLay, linhaLay.getColunas());
	}

	/**
	 * Retorna um objeto Valores contendo as informações relativas as colunas do TIPO da linha informado. Esse metodo é utilizado para efetuar uma pre-carga no objeto valores, para que os respectivos conteúdos sejam informados posteriormente
	 *  
	 * @param tipoLinha <br>Esse parametro deve ser informado com o conteúdo do atributo tipo na tag <linha> do layout xml<br><br>
	 * @return Valores <br>Todas as informações relativas a esse tipo de linha conforme o layout xml (Informações das Colunas, Tipo da linha, Id, etc.)<br><br>
	 * @throws ParseFileException
	 */
	public Valores getArrayPorTipo(String tipoLinha) throws ParseFileException {
		LinhaLayout linhaLay = this.layout.getLinhaPorTipo(tipoLinha);
		return getArray(linhaLay.getId(), linhaLay, linhaLay.getColunas());
	}
	
	/**
	 * Retorna um objeto Valores contendo as informações relativas as colunas do id da linha informado porém somente com as colunas determinadas no parametro colunas. Esse metodo é utilizado para efetuar uma pre-carga no objeto valores, para que os respectivos conteúdos sejam informados posteriormente
	 *  
	 * @param idLinha <br>Esse parametro deve ser informado com o conteúdo do atributo id na tag <linha> do layout xml<br><br>
	 * @param colunas colunas <br>Informa em uma String os id's das colunas (atributo id da tag <coluna> do layout xml), separados por vírgula, que deseja que sejam retronadas no objeto de Valores.<br><br>
	 * @return Valores <br>Todas as informações relativas a esse tipo de linha conforme o layout xml (Informações das Colunas, Tipo da linha, Id, etc.)<br><br>
	 * @throws ParseFileException
	 */
	public Valores getArray(String idLinha, String colunas) throws ParseFileException {
		LinhaLayout linhaLay = this.layout.getLinha(idLinha);
		return getArray(idLinha, linhaLay, linhaLay.getColunas(colunas));
	}
	
	private Valores getArray(String idLinha, LinhaLayout linhaLay, ArrayList cols) throws ParseFileException {
		String[] valores = new String[cols.size()];
		StringBuffer mapValores = new StringBuffer();
		
		for (int c = 0; c < cols.size(); c++) {
			ColunaLayout col = (ColunaLayout)cols.get(c);
			
			valores[c] = col.getStrDefault();
			
			mapValores.append(ParseUtils.getIdStr(col.getId(),c));
		}
		
		//return new Valores(mapValores, valores, linhaLay.getId(), linhaLay.getTipo(), cols, null, linhaLay.getMapTotalizadores());
		return new Valores(mapValores, valores, linhaLay.getId(), linhaLay.getTipo(), cols, null, null);
	}

	private Valores getArray(String idLinha, LinhaLayout linhaLay, ArrayList cols, DinVO values) throws ParseFileException {
		String[] valores = new String[cols.size()];
		StringBuffer mapValores = new StringBuffer();
		
		for (int c = 0; c < cols.size(); c++) {
			ColunaLayout col = (ColunaLayout)cols.get(c);
			
			String valor = values.getString(col.getId());
			if (Valida.isEmpty(valor)) valor = col.getStrDefault();
			
			valores[c] = valor;
			
			mapValores.append(ParseUtils.getIdStr(col.getId(),c));
		}
		
		//return new Valores(mapValores, valores, linhaLay.getId(), linhaLay.getTipo(), cols, null, linhaLay.getMapTotalizadores());
		return new Valores(mapValores, valores, linhaLay.getId(), linhaLay.getTipo(), cols, null, null);
	}
	
	private int validaTipo(ColunaLayout col, String valor, String valorOriginal) {
		
		if ((!col.isObrigatorio()) && (Valida.isEmpty(valor))) { return Status.OK;}
		
		if (LinhaLayout.NUMERICO.equals(col.getTipo())) {
			
			// foi colocada a validação regex pois haviam numeros maiores que 
			// o limite do long
			
			String regex = "([0-9])+";
			String val = valor;
			
			// valida campos numerico completo
			if (col.getNumericoCompleto() == 1) {
				val = valorOriginal;
			} else if (col.getNumericoCompleto() == 0) {
				val = valor;
			} else {
				if (this.numericoCompleto) {
					val = valorOriginal;
				} else { 
					val = valor;
				}
			}
			
			if (!Pattern.matches(regex, val)) {
				return Status.TIPO_INVALIDO;
			}
			
			/*
			try {
				Long.parseLong(valor);
			} catch (NumberFormatException e) {
				return Status.TIPO_INVALIDO;
			}
			*/
		}
		
		if (LinhaLayout.DATA.equals(col.getTipo())) {
			if (Valida.isEmpty(col.getFormato())) {
				return Status.FORMATO_INVALIDO;
			}
			// para arquivos com hora 
			if (Valida.isZero(valor)) {
				if (col.isObrigatorio()) {
					return Status.OBRIGATORIO;
				}
				return Status.OK;
			}
			if (!Valida.isData(valor, col.getFormato())) {
				return Status.TIPO_INVALIDO;
			}
		}
		
		if (LinhaLayout.ALFA.equals(col.getTipo())) {
			if ((col.isObrigatorio()) && (Valida.isEmpty(valor))) { return Status.OBRIGATORIO;}
		}
		
		if (LinhaLayout.VALOR.equals(col.getTipo())) {
			
			try {
				if (",".equals(col.getSepDecimais())) {
					valor = valor.replaceAll("\\.", "");
					valor = valor.replaceAll("\\,", ".");
				}
				if (".".equals(col.getSepDecimais())) {
					valor = valor.replaceAll("\\,", "");
				}
				
				int pos = valor.indexOf("."); 
				if (pos != -1) {
					int dec = col.getDecimais();
					if (dec == -1) dec = 2; 
					
					if (valor.substring(pos + 1).length() > dec) {
						return Status.VALOR_INVALIDO;
					}
				}
				
				Double.parseDouble(valor);
			} catch (NumberFormatException e) {
				return Status.VALOR_INVALIDO;
			}
		}
		
		return Status.OK;
	}
	
	/**
	 * Limpa o objeto ParseFile e todos seus objetos internos
	 */
	public void clear() {
		close();
	}
	/**
	 * Limpa o objeto ParseFile e todos seus objetos internos
	 */
	public void close() {
		this.layout.clear();
		try {
			if (daoBatch != null) {daoBatch.close(); daoBatch = null;}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean validaDominio(String valor, String dominio, boolean obrigatorio) {
		if (Valida.isEmpty(dominio)) { return true; }
		
		if ((!obrigatorio) && (Valida.isEmpty(valor))) { return true; }
		
		String dominios[] = dominio.split("\\,");
		
		for (int i = 0; i < dominios.length; i++) {
			int posInt = dominios[i].indexOf("-");
			if (posInt == -1) {
				if (dominios[i].trim().equals(valor.trim())) {
					return true;
				}
			} else {
				try {
					long ini = Long.parseLong(dominios[i].substring(0, posInt));
					long fim = Long.parseLong(dominios[i].substring(posInt+1));
					long vlr = Long.parseLong(valor);
					
					if ((vlr >= ini) && (vlr <= fim)) {
						return true;
					}
				} catch (NumberFormatException e) { 
					// não faz nada poi a função retorna false	
				}
				/*
				int comp = valor.compareTo();
				// se for maior que o inicial compara o final 
				if (comp >= 0) {
					comp = valor.compareTo(dominios[i].substring(posInt+1));
					// se for maior que o inicial compara o final 
					if (comp <= 0) {
						ret = true;
					}
				}
				*/
				
				
			}
		}
		
		return false;
		
	}
	
	/*
	public void validaTotalizadores(String tipo, int[] colunasTot) {
		// se não houverem totalizadores sai da validação 
		if (!this.layout.hasTotalizadores(tipo)) {
			return;
		}
		// se nessa linha não houver totalizadores sai da validação
		if (colunasTot == null) {
			return;
		}
	}
	*/

	/**
	 * Retorna um objeto do tipo LinhaLayout contendo toda a configuração do layout xml de determinada linha (idLinha)
	 * 
	 * @param idLinha <br>Esse parametro deve ser informado com o conteúdo do atributo id na tag <linha> do layout xml que deseja retornar a configuração<br><br>
	 * @return LinhaLayout <br>Contém todas as informações referentes a linha informada (Colunas, Tipos, etc.)<br><br>
	 * @throws ParseFileException
	 */
	public LinhaLayout getLinha(String idLinha) throws ParseFileException {
		return this.layout.getLinha(idLinha);
	}
	
	/**
	 * Retorna um objeto do tipo LinhaLayout contendo toda a configuração do layout xml de determinada linha (tipoLinha)
	 * 
	 * @param tipo <br>Esse parametro deve ser informado com o conteúdo do atributo tipo na tag <linha> do layout xml que deseja retornar a configuração<br><br>
	 * @return LinhaLayout <br>Contém todas as informações referentes a linha informada (Colunas, Tipos, etc.)<br><br>
	 * @throws ParseFileException
	 */
	public LinhaLayout getLinhaPorTipo(String tipo) throws ParseFileException {
		return this.layout.getLinhaPorTipo(tipo);
	}
	
	/**
	 * Retorna um objeto do tipo LinhaLayout contendo toda a configuração do layout xml de determinada linha (idLinha)
	 * 
	 * @param index <br>Indice da linha que deseja retornar a configuração (utilizado internamente)<br><br>
	 * @return LinhaLayout <br>Contém todas as informações referentes a linha informada (Colunas, Tipos, etc.)<br><br>
	 * @throws ParseFileException
	 */
	public LinhaLayout getLinha(int index) throws ParseFileException {
		return this.layout.getLinha(index);
	}
	
	private void montaLayoutXml(InputStream xmlFile, String xmlFileName, String layoutId) throws ParseFileException {
	    try {
	    	// abre o arquivo
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse (xmlFile);
            
            doc.getDocumentElement().normalize();
            
            //System.out.println ("Root element of the doc is " + doc.getDocumentElement().getNodeName());

            NodeList layouts = doc.getElementsByTagName("layout");
            
            //int totalPersons = layouts.getLength();
            //System.out.println("Total de layouts : " + totalPersons);

            // caso o construtor não informe um layout considera o primeiro layout que encontrar no arquivo
            int max = layouts.getLength();
            if (layoutId == null) {
            	max = 1;
            }
            
            // varre as tags de layout
            for(int lay = 0; lay < max; lay++){
            	
            	Element layoutEle = (Element)layouts.item(lay);
                String id = layoutEle.getAttribute("id");
                
            	if (Valida.isEmpty(id)) {
					// <id> é um atributo obrigatório na tag <layout> do xml de layout.
            		throw new ParseFileException("Erro de layout: <id> é um atributo obrigatório na tag <layout> do xml de layout.");
            	}
                
                
                // se for o layout selecionado 
                if ((layoutId == null) || (layoutId.equals(id)))  {
                	
                	this.layout = new Layout(id, layoutEle.getAttribute("identificadores"), layoutEle.getAttribute("delimitador"), layoutEle.getAttribute("converter"));
                	
                	NodeList configuracoes = layoutEle.getElementsByTagName("configuracoes");
                	if (configuracoes.getLength() > 0) {
                    	Element totEle = (Element)configuracoes.item(0);
                    	
                    	NodeList confNode = totEle.getElementsByTagName("ignoreLinhasNaoDeclaradas");
                    	if (confNode.getLength() > 0) {
                        	Element confEle = (Element)confNode.item(0);
                        	
                        	boolean value = ParseUtils.validaBoolean(confEle.getAttribute("value"), false);
                        	
                        	this.layout.setIgnoreLinhasNaoDeclaradas(value);
                    	}
                    	
                    	confNode = totEle.getElementsByTagName("ignoreLinhasEmBranco");
                    	if (confNode.getLength() > 0) {
                        	Element confEle = (Element)confNode.item(0);
                        	
                        	boolean value = ParseUtils.validaBoolean(confEle.getAttribute("value"), false);
                        	
                        	setIgnoreEmptyLine(value);
                    	}
                    	
                    	confNode = totEle.getElementsByTagName("completeTodasLinhas");
                    	if (confNode.getLength() > 0) {
                        	Element confEle = (Element)confNode.item(0);
                        	
                        	boolean value = ParseUtils.validaBoolean(confEle.getAttribute("value"), false);
                        	
                        	setCompleteAllLines(value);
                    	}
                    	
                    	confNode = totEle.getElementsByTagName("numericoCompleto");
                    	if (confNode.getLength() > 0) {
                        	Element confEle = (Element)confNode.item(0);
                        	
                        	boolean value = ParseUtils.validaBoolean(confEle.getAttribute("value"), false);
                        	
                        	setNumericoCompleto(value);
                    	}
                    	
                	}

                	NodeList variaveis = layoutEle.getElementsByTagName("variaveis");
                	if (variaveis.getLength() > 0) {
                    	Element totEle = (Element)variaveis.item(0);
                    	
                    	NodeList confNode = totEle.getElementsByTagName("variavel");
                    	
                        for(int vv = 0; vv < confNode.getLength() ; vv++) {
                		
                        	Element confEle = (Element)confNode.item(vv);
                        	
                        	String idVar = confEle.getAttribute("id");
                        	
                        	if (Valida.isEmpty(idVar)) {
                        		throw new ParseFileException("Erro de layout: <id> é um atributo obrigatório na tag <variavel> do xml de layout.");
                        	}
                        	
                        	String valor = confEle.getAttribute("value");
                        	
                        	this.layout.addsetVariavel(idVar, valor);
                        }
                	}
                	
                	
                	// verifica se existem totalizadores
                	NodeList totalizadores = layoutEle.getElementsByTagName("totalizadores");
                	if (totalizadores.getLength() > 0) {
                    	Element totEle = (Element)totalizadores.item(0);
                    	
                    	boolean read = ParseUtils.validaBoolean(totEle.getAttribute("read"), true);
                    	boolean write = ParseUtils.validaBoolean(totEle.getAttribute("write"), true);
                    	
                    	this.layout.setValidaRead(read);
                    	this.layout.setValidaWrite(write);
                    	
                    	totalizadores = totEle.getElementsByTagName("totalizador");
                    	
                        for(int tot = 0; tot < totalizadores.getLength() ; tot++) {
                        	
                        	totEle = (Element)totalizadores.item(tot);
                        	
                        	int start = StringUtil.toInt(totEle.getAttribute("inicial"));
                        	
                        	this.layout.addTotalizador(totEle.getAttribute("id"), start);
                        	
                        	
                        	this.layout.addMapTotalizadores(
                    			  totEle.getAttribute("id")
                    			, totEle.getAttribute("tipo")
                    			, totEle.getAttribute("campos")
                        	);
                        }
                		
                	} else {
                    	this.layout.setValidaRead(false);
                    	this.layout.setValidaWrite(false);
                	}
                	
                	
                	// insere um contador interno que pode ou não ser utilizado porem será sempre atualizado
                	this.layout.addTotalizador("#seqInterno", 0);
                	this.layout.addMapTotalizadores(
              			  "#seqInterno"
              			, null
              			, null
                  	);
                	
                	
                	// verifica se existem totalizadores
                	NodeList ocorrencias = layoutEle.getElementsByTagName("ocorrencias");
                	if (ocorrencias.getLength() > 0) {
                    	Element ocoEle = (Element)ocorrencias.item(0);
                    	
                    	//boolean read = ParseUtils.validaBoolean(ocoEle.getAttribute("read"), true);
                    	//boolean write = ParseUtils.validaBoolean(ocoEle.getAttribute("write"), true);
                    	
                    	//this.layout.setValidaRead(read);
                    	//this.layout.setValidaWrite(write);
                    	
                    	ocorrencias = ocoEle.getElementsByTagName("ocorrencia");
                    	
                    	Ocorrencias ocs = new Ocorrencias();
                    	
                        for(int tot = 0; tot < ocorrencias.getLength() ; tot++) {
                        	ocoEle = (Element)ocorrencias.item(tot);
                        	ocs.addChave(
                        			ocoEle.getAttribute("id")
                        			, ocoEle.getAttribute("tipo")
                        			, ocoEle.getAttribute("campo")
                        			);
                        }
                        
                        this.layout.setOcorrencias(ocs);
                	}
                	
                	
                	DinVO listSqls = new DinVO();
                	
                	NodeList sqlItems = layoutEle.getElementsByTagName("sqlList");
                	if (sqlItems.getLength() > 0) {
                    	Element totEle = (Element)sqlItems.item(0);
                    	
                    	NodeList batchs = totEle.getElementsByTagName("batch");
                    	if (batchs.getLength() > 0) {
                            for(int bt = 0; bt < batchs.getLength() ; bt++) {
                            	Element batchEle = (Element)batchs.item(bt);
                            	
                            	if (Valida.isEmpty(batchEle.getAttribute("id"))) {
                            		throw new ParseFileException("Erro de layout: <id> eh um atributo obrigatorio na tag <batch> do xml de layout.");
                            	}
                            	
                            	if (Valida.isEmpty(batchEle.getAttribute("dataSource"))) {
                            		throw new ParseFileException("Erro de layout: <dataSource> eh um atributo obrigatorio na tag <batch> do xml de layout.");
                            	}
                            	
                            	if (Valida.isEmpty(batchEle.getAttribute("sql"))) {
                            		throw new ParseFileException("Erro de layout: <sql> eh um atributo obrigatorio na tag <batch> do xml de layout.");
                            	}
                            	
                            	int buffer = StringUtil.toInt(batchEle.getAttribute("buffer"));
                            	if (buffer <= 0) buffer = 500;
                            	
                            	SqlExecVO vo = new SqlExecVO();
                            	vo.setId        ( batchEle.getAttribute("id") );
                            	vo.setDataSource( batchEle.getAttribute("dataSource") );
                            	vo.setSql       ( batchEle.getAttribute("sql") );
                            	vo.setBuffer    ( buffer );
                            	vo.setTipo      (SqlExecVO.T_BACTH);

                            	if (listSqls.hasId(vo.getId())) {
                            		throw new ParseFileException("Erro de layout: id[" + vo.getId() + "] já existente na lista de SQLs <sqlList>.");
                            	}
                            	
                            	listSqls.addset(vo.getId(), vo);
                            }
                    	}
                    	
                    	NodeList sqls = totEle.getElementsByTagName("proc");
                    	if (sqls.getLength() > 0) {
                            for(int bt = 0; bt < sqls.getLength() ; bt++) {
                            	Element sqlEle = (Element)sqls.item(bt);
                            	
                            	if (Valida.isEmpty(sqlEle.getAttribute("id"))) {
                            		throw new ParseFileException("Erro de layout: <id> eh um atributo obrigatorio na tag <proc> do xml de layout.");
                            	}
                            	
                            	if (Valida.isEmpty(sqlEle.getAttribute("dataSource"))) {
                            		throw new ParseFileException("Erro de layout: <dataSource> eh um atributo obrigatorio na tag <proc> do xml de layout.");
                            	}
                            	
                            	if (Valida.isEmpty(sqlEle.getAttribute("sql"))) {
                            		throw new ParseFileException("Erro de layout: <sql> eh um atributo obrigatorio na tag <proc> do xml de layout.");
                            	}
                            	
                            	SqlExecVO vo = new SqlExecVO();
                            	vo.setId        ( sqlEle.getAttribute("id") );
                            	vo.setDataSource( sqlEle.getAttribute("dataSource") );
                            	vo.setSql       ( sqlEle.getAttribute("sql") );
                            	vo.setTipo      (SqlExecVO.T_PROC);
                            	
                            	if (listSqls.hasId(vo.getId())) {
                            		throw new ParseFileException("Erro de layout: id[" + vo.getId() + "] já existente na lista de SQLs <sqlList>.");
                            	}
                            	
                            	listSqls.addset(vo.getId(), vo);
                            }
                    	}
                    	
                	}
                	
                    NodeList linhas = layoutEle.getElementsByTagName("linha");
                    
                    // varre as tags linha
                    for(int lin = 0; lin < linhas.getLength() ; lin++) {
                    	Element linhaEle = (Element)linhas.item(lin);
                    	
                    	id = linhaEle.getAttribute("id");
                    	
                    	if (Valida.isEmpty(id)) {
                    		// Erro do layout: <id> é um atributo obrigatório na tag <linha> do xml de layout.
                    		throw new ParseFileException("Erro de layout: <id> é um atributo obrigatório na tag <linha> do xml de layout.");
                    	}
                    	
                    	boolean validar = ParseUtils.validaBoolean(linhaEle.getAttribute("validar"), true);
                    	
                    	boolean obrigatorio = ParseUtils.validaBoolean(linhaEle.getAttribute("obrigatorio"), validar);
                    	
                    	boolean completeLinha = ParseUtils.validaBoolean(linhaEle.getAttribute("completeLinha"), false);
                    	
                    	String numlinha = linhaEle.getAttribute("numero");
                    	
                    	if (!Valida.isEmpty(numlinha)) {
                    		this.layout.addLinhaEspecial(numlinha, ((validar)?id:"#"));
                    	}
                    	                    	                   	
                    	LinhaLayout linhaObj = new LinhaLayout(id, linhaEle.getAttribute("tipo"), obrigatorio, this.layout.isDelimitado(), completeLinha, linhaEle.getAttribute("linhasAntes"), linhaEle.getAttribute("linhasDepois"));
                    	
                    	NodeList colunas = linhaEle.getElementsByTagName("coluna");
                    	
                    	if ((validar) && ( (colunas == null) || (colunas.getLength() == 0) ) ) {
                    		// Erro do layout: <id> é um atributo obrigatório na tag <linha> do xml de layout.
                    		throw new ParseFileException("Erro de layout: linha <id=\"" + id + "\"> nao possui colunas no xml de layout.");
                    	}
                    	
                    	// varre as colunas da linha 
                        for(int col = 0; col < colunas.getLength() ; col++) {
                        	
                        	Element colunaEle = (Element)colunas.item(col);
                        	
                        	linhaObj.addColunaXml(
                        			  colunaEle.getAttribute("id")
                        			, colunaEle.getAttribute("tipo")
                        			, colunaEle.getAttribute("posini")
                        			, colunaEle.getAttribute("tamanho")
                        			, colunaEle.getAttribute("descricao")
                        			, colunaEle.getAttribute("default")
                        			, colunaEle.getAttribute("pad")
                        			, colunaEle.getAttribute("padside")
                        			, colunaEle.getAttribute("formato")
                        			, colunaEle.getAttribute("dominio")
                        			, colunaEle.getAttribute("obrigatorio")
                        			, colunaEle.getAttribute("decimais")
                        			, colunaEle.getAttribute("uppercase")
                        			, colunaEle.getAttribute("lowercase")
                        			, colunaEle.getAttribute("totalizador")
                        			, colunaEle.getAttribute("zerartotal")
                        			, colunaEle.getAttribute("sepDecimais")
                        			, colunaEle.getAttribute("zerartotalizadores")
									, colunaEle.getAttribute("sqlExec")
									, colunaEle.getAttribute("setVariavel")
									, colunaEle.getAttribute("getVariavel")
									, colunaEle.getAttribute("numericoCompleto")
                        	);
                        	
                        }
                    	
                        // foi feito um sort nas colunas por posini para garantir
                        // que as colunas fiquem na ordem correta, caso no layout
                        // as mesmas tenham sido invertidas
                        linhaObj.sortColunas();
                        

                        // captura as execucoes em batch dessa coluna
                    	NodeList sqlBachs = linhaEle.getElementsByTagName("execSql");
                    	
                    	// varre as colunas da linha 
                        for(int sb = 0; sb < sqlBachs.getLength() ; sb++) {
                        	
                        	Element sqlEle = (Element)sqlBachs.item(sb);
                        	
                        	String sqlId = sqlEle.getAttribute("idSql");
                        	
                        	if (Valida.isEmpty(sqlId)) {
                        		throw new ParseFileException("Erro de layout: <sqlId> eh um atributo obrigatorio na tag <execSql> do xml de layout.");
                        	}
                        	
                        	if (Valida.isEmpty(sqlEle.getAttribute("parametros"))) {
                        		throw new ParseFileException("Erro de layout: <parametros> eh um atributo obrigatorio na tag <execSql> do xml de layout.");
                        	}
                        	
                        	int pos = listSqls.findInIds(sqlId);
                        	if (pos == -1) throw new ParseFileException("Erro de layout: id[" + sqlId + "] nao declarado em nenhuma tag de <sqlList>. linhaId [" + id + "]");
                        	
                        	SqlExecVO vo = (SqlExecVO)listSqls.get(pos);
                        	vo.setParametros( sqlEle.getAttribute("parametros") );
                        	vo.setIdGrupo   ( sqlEle.getAttribute("idGrupoExecucaoSql") );
                        	vo.setPre       ( sqlEle.getAttribute("setPreExec") );
                        	vo.setPos       ( sqlEle.getAttribute("setPosExec") );
                        	
                        	linhaObj.addExecucoesSql(vo);
                        }
                        
                        
                    	this.layout.addLinha(linhaObj);
                    	
                    	
                    }
                    // se entrar no if é porque encontrou o layout
                    return;
                }
            }
            
            if (this.layout == null) {
        		// Erro do layout: o layout <id> não foi encontrado no arquivo de layout XML
            	StringBuffer msg = new StringBuffer("Erro de layout:  O layout (");
            	msg.append(layoutId).append(") não foi encontrado no arquivo de layout XML (");
            	msg.append(xmlFileName).append(")");
            	throw new ParseFileException(msg.toString());
            }
            	
	    } catch(ParserConfigurationException e) {
	    	throw new ParseFileException("Problemas ao criar o builder do XML no ParseFile - montaLayoutXml()",e);
	    } catch(SAXException e) {
	    	throw new ParseFileException(new StringBuffer("Problemas ao tentar abrir ou parsear o arquivo XML de layout: ").append(xmlFileName).toString(),e);
	    } catch(IOException e) {
	    	throw new ParseFileException(new StringBuffer("Problemas ao tentar abrir ou parsear o arquivo XML de layout: ").append(xmlFileName).toString(),e);
	    }
		
	
	}
	
	
	/**
	 * Retorna as exceções de leitura que forma encontras, mas não em nivel de linhas (Utilizar esse metodo quando o ParseFile estiver setado para não sair por exceção)  
	 * 
	 * @throws ParseFileException
	 */
	public void throwReadExceptions() throws ParseFileException {
		linhasProcessadas(true);
	}
	
	/**
	 * Retorna as exceções de geração que forma encontras, mas não em nivel de linhas (Utilizar esse metodo quando o ParseFile estiver setado para não sair por exceção)
	 *   
	 * @throws ParseFileException
	 */
	public void throwWriteExceptions() throws ParseFileException {
		linhasProcessadas(false);
	}
	
	private void linhasProcessadas(boolean read)  throws ParseFileException {
		ArrayList linhas = this.layout.getLinhas();
		
		if (Valida.isEmpty(linhas)) { return; }
		
		for (int i =0; i < linhas.size(); i++) {
			// verifica se a linha foi processada na leitura ou gravação (variavel read)
			// quando a linha não é obrigatoria ela já vem setada processada = true
			LinhaLayout linhaLay = (LinhaLayout)linhas.get(i);
			if (!linhaLay.isProcess(read)) {
        		// Tipo de registro (tipo) é obrigatório mas não foi encontrado no arquivo
				
				StringBuffer msg = new StringBuffer("Erro de Consistência: Registro tipo (").append(linhaLay.getTipo()).append(")");
				msg.append(" é obrigatório mas não foi encontrado no arquivo.");
				if (read) {
					msg.append("(leitura)");
				} else {
					msg.append("(gravação)");
				}
				
				throw new ParseFileException(Status.LINHA_NAO_PROCESSADA,msg);
			}
			
		}
	}
	
	/**
	 * Retorna o contador de linhas de Leitura interno do parse
	 * 
	 * @return long <br>número de linha de leitura calculado pelo parse<br><br>
	 */
	public long getNumLinhaRead() throws Exception{
		return this.layout.getTotalizador("#seqInterno").getContadorRead();
	}
	
	/**
	 * Retorna o contador de linhas de geração interno do parse
	 * 
	 * @return long <br>número de linha de geração calculado pelo parse<br><br>
	 */
	public long getNumLinhaWrite() throws Exception{
		return this.layout.getTotalizador("#seqInterno").getContadorWrite();
	}
	
	/**
	 * Adiciona uma variável e seu respectivo valor ao layout. 
	 * 
	 * @param id <br>id da variável<br><br>
	 * @param valor <br>valor da variável<br><br>
	 */
	public void addsetVariavel(String id, String valor) {
		if (this.layout == null) return;
		
		this.layout.addsetVariavel(id, valor);
	}
	
	/**
	 * Retorna o valor de uma variavel de layout (String). 
	 * 
	 * @param id <br>id da variável<br><br>
	 * @return String <br>valor da variável<br><br>
	 */
	public String getVariavel(String id) {
		if (this.layout == null) return "";
		
		return this.layout.getVariavel(id);
	}
	
	/**
	 * Retorna todas as variáveis de layout definidas. 
	 * 
	 * @return DinVO <br>variáveis<br><br>
	 */
	public DinVO getVariaveis() {
		if (this.layout == null) return null;
		
		return this.layout.getVariaveis();
	}
	
	/**
	 * Verifica a existência de uma determinada variável na coleção de variáveis de layout. 
	 * 
	 * @return boolean <br><b>true</b> - existe a variável<br><b>false</b> - não existe a variável<br><br>
	 */
	public boolean hasVariavel(String id) {
		if (this.layout == null) return false;
		if (Valida.isEmpty(this.layout.getVariaveis())) return false;
		
		return this.layout.getVariaveis().hasId(id);
	}
	
	
	/**
	 * Efetua um commit de todas as instruções SQL BATCH que ficaram pendentes no processo de parse
	 * É recomendado colocar esse método sempre fora do looping de leitura ou geração de arquivo   
	 * 
	 * @throws ParseFileException
	 */
	public void commitExecucoesBatch() throws ParseFileException {
		if (daoBatch == null) {
			if (hasSqlExec) throw new ParseFileException("Erro de Layout: Dao de batch nao inicializado para efetuar o commit");
		} else {
			daoBatch.commitBatchs();
		}
	}

	
}
