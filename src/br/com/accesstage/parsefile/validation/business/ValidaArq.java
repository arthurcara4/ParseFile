package br.com.accesstage.parsefile.validation.business; 

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import br.com.accesstage.parsefile.ParseFile;
import br.com.accesstage.parsefile.exceptions.ParseFileException;
import br.com.accesstage.parsefile.retornos.Valores;
import br.com.accesstage.parsefile.utils.Email;
import br.com.accesstage.parsefile.utils.StringUtil;
import br.com.accesstage.parsefile.utils.Valida;
import br.com.accesstage.parsefile.validation.dao.ValidaArqDAO;
import br.com.accesstage.parsefile.validation.util.ConnType;
import br.com.accesstage.parsefile.validation.vo.Arquivo;
import br.com.accesstage.parsefile.validation.vo.ColExcept;
import br.com.accesstage.parsefile.validation.vo.LinExcept;
import br.com.accesstage.parsefile.validation.vo.ValidarVO;


public abstract class ValidaArq 
{ 
    public static final int T_VALIDO = 0;
    public static final int T_INVALIDO = 1;
    public static final int T_ORIGINAL = 2;
    public static final int T_LOG = 3;
    public static final int T_MANUAL = 4;
    
    private String charset = "ISO-8859-1";
    
    
    
    
    //private static NonCatalogLogger log = LogFactory.getLog(ValidaArq.class);
    //private static NonCatalogLogger log =  new NonCatalogLogger(ValidaArq.class.getName());    
    
	private ConnType connType;
    private Hashtable params;
    
    private boolean temErro;
    
    private ValidarVO entrada;
    private String emailHost;
    
    private VelocityContext velocityContext = null;
    private VelocityEngine ve = null;
    
    LinkedList linhasExcept;

    
    public void init(boolean isXa, ValidarVO entrada)  throws Exception{
    	
    	// define o pool de conexão
        String JNDI_DS = "infodocument";
        if (isXa) {
	        JNDI_DS = "infodocumentXA";
        }
        
        this.connType = new ConnType(JNDI_DS);
        construtor(entrada);
    }
    
    /*
    public void init(String connStr, String user, String pass, ValidarVO entrada)  throws Exception{
        this.connType = new ConnType(connStr, user, pass);
        construtor(entrada);
    }
    */
    
    private void construtor(ValidarVO entrada) throws Exception {
        ValidaArqDAO dao = new ValidaArqDAO(this.connType);
        params = dao.getParams();
        
        setEntrada(entrada);
    }
    
    //------------------------------------
    // Gets e Sets
    //------------------------------------
    public void setCharset(String charset) {
    	this.charset = charset;
    }
    public String getCharset() {
    	return this.charset;
    }
    
    private void setEntrada(ValidarVO ent) { 
        this.entrada = ent; 
        this.emailHost = (String)params.get("EMAIL_HOST");
        
        if (this.entrada.isSendMail()) {
            
            this.entrada.setEmail(
                ent.getEmailTo()
                , (Valida.isEmpty(ent.getEmailFrom())) ? (String)params.get("EMAIL_FROM_PADRAO") : ent.getEmailFrom()
                , (Valida.isEmpty(ent.getEmailSubject())) ? (String)params.get("EMAIL_SUBJECT_PADRAO") : ent.getEmailSubject()
            );
        }
        
        if (Valida.isEmpty(this.entrada.getTemplateFileLog())) {
            this.entrada.setTemplateFileLog((String)params.get("VELOCITY_TEMPLATE_FILE_LOG"));
        }
        if (Valida.isEmpty(this.entrada.getTemplateEmail())) {
            this.entrada.setTemplateEmail((String)params.get("VELOCITY_TEMPLATE_EMAIL"));
        }
        if (Valida.isEmpty(this.entrada.getTemplateTela())) {
            this.entrada.setTemplateTela((String)params.get("VELOCITY_TEMPLATE_TELA"));
        }
        if (Valida.isEmpty(this.entrada.getHtmlEmailBody())) {
            this.entrada.setHtmlEmailBody((String)params.get("EMAIL_BODY"));
        }
        if (this.entrada.getLimiteErros() == 0) {
            this.entrada.setLimiteErros(Integer.parseInt((String)params.get("LIMITE_ERROS_ARQ")));
        }
    }
    
    //------------------------------------
    // métodos de validação de um único tracking ou uma lista de serviço
    // isso vem definido no vo de entrada
    //------------------------------------
    public ArrayList validaToCollection(ValidarVO vo) throws Exception {
        
        initVelocity(false);

        ArrayList arqs = getFiles(vo.getType(), vo.getChave());
        
        //------------------------------------
        // esse flag é setado no metodo validaArqs caso exista alguma exceção em qualquer um dos arquivos
        //------------------------------------
            
        sendMail(arqs);
        
        if (((this.temErro) && (this.entrada.getForcaRetorno() == ValidarVO.ERRO)) 
            || ((!this.temErro) && (this.entrada.getForcaRetorno() == ValidarVO.OK))
            || (this.entrada.getForcaRetorno() == ValidarVO.OK_E_ERRO))
        {
            return arqs;
        }
         
        return null;
    }
    
    public String validaToHtml(ValidarVO vo) throws Exception {
        
        initVelocity(true);
        
        ArrayList arqs = getFiles(vo.getType(), vo.getChave());
        
        //------------------------------------
        // esse flag é setado no metodo validaArqs caso exista alguma exceção em qualquer um dos arquivos
        //------------------------------------
        
        sendMail(arqs);
        
        if (((this.temErro) && (this.entrada.getForcaRetorno() == ValidarVO.ERRO)) 
            || ((!this.temErro) && (this.entrada.getForcaRetorno() == ValidarVO.OK))
            || (this.entrada.getForcaRetorno() == ValidarVO.OK_E_ERRO))
        {
            return montaTela(arqs);
         }
         
         return null;
    }
    
    public void valida(ValidarVO vo) throws Exception {
        
        initVelocity(true);
        
        ArrayList arqs = getFiles(vo.getType(), vo.getChave());
        
        //------------------------------------
        // esse flag é setado no metodo validaArqs caso exista alguma exceção em qualquer um dos arquivos
        //------------------------------------
        
        sendMail(arqs);
    }
    
    
    //------------------------------------
    // métodos privados de bussines da classe
    //------------------------------------
    
    private ArrayList getFiles(int tipo, String chave) throws Exception 
    {
        ValidaArqDAO dao = new ValidaArqDAO(this.connType);
        ArrayList arqs = null;
        try {
            if (tipo ==  ValidarVO.F_SERVICES) {
                arqs = validaArqs(dao.buscaFilesService(chave));
            }
            
            if (tipo == ValidarVO.F_TRACKING) {
                arqs = validaArqs(dao.buscaTracking(chave));
            }
            
            if (tipo == ValidarVO.F_EXTERNO) {
                arqs = validaArqs(this.entrada.getArqsExternos());
            }
            
        } finally {
            if (dao != null) { dao.close(); }
        }
        
        return (arqs);
    }

    //------------------------------------
    // Valida cada um dos arquivo e valida, salvando as exceções internamente
    //------------------------------------
    private ArrayList validaArqs(ArrayList arqs) throws Exception {
        
        if (Valida.isEmpty(arqs)) {
            return null;
        }
        
        this.temErro = false;
        
        // varre todos os arquivos
        for (int f = 0; f < arqs.size(); f++) {
            Arquivo arqRet = validar((Arquivo)arqs.get(f));
            
            if (arqRet.hasException()) {
                this.temErro = true;
                
                // Geração de arquivo de log. O metodo execVelocity espera
                // uma coleção de arquivos por isso é criado essa ArrayList com um unico arquivo.
                // anteriormente o log era gerado para todos os arquivos validados agora é um log para
                // cada arquivo
                if (this.entrada.isSubmitLog() || this.entrada.isGeraArqLog()) {
                    ArrayList arr = new ArrayList();
                    arr.add(arqRet);
                    
                    //File fLog = new File(execVelocity("VELOCITY_TEMPLATE_FILE_LOG", arr, false));
                    File fLog = new File(execVelocity(this.entrada.getTemplateFileLog(), arr, false));
                    
                    arqRet.setPathArqLog(fLog.getAbsolutePath());
                    
                    // submete os arquivo de log 
                    if (this.entrada.isSubmitLog()) {
                        submitFile(
                            fLog
                            ,(String)params.get("PATH_SUBMIT")
                            ,this.entrada.getSenderLog()
                            ,this.entrada.getReceiverLog()
                            ,this.entrada.getDoctypeLog()
                            ,T_LOG
                        );
                    
                        // exclui o arquivo após o submit
                        if (!fLog.delete()) {
                            System.out.println("ValidaArq:validaArqs()-Não foi possível excluir o arquivo: " + fLog.getAbsolutePath() + " através do componente de validação");
                        };
                    }
                    
                }
            }
            
            arqs.set(f, arqRet);
        }
        
        return(arqs);
    }
    
    //------------------------------------
    // Valida os arquivos e grava suas exceções. Seta o flag temErro caso for encontrada alguma
    // exceção
    //------------------------------------
    private Arquivo validar(Arquivo arq) throws Exception {
        
        ParseFile pf = null;
        //FileReader fr = null;
        FileInputStream fs = null;
        InputStreamReader fr = null;
        BufferedReader in = null;
        
        //FileReader frEol = null;
        FileInputStream fsEol = null;
        InputStreamReader frEol = null;
        BufferedReader brEol = null;
        
        LinkedList linhas = new LinkedList();
        
        
        File fVal = null;
        //FileWriter fwValido = null;
        FileOutputStream foValido = null;
        OutputStreamWriter fwValido = null;
        BufferedWriter bwValido = null;
        
        File fInval = null;
        //FileWriter fwInvalido = null;
        FileOutputStream foInvalido = null;
        OutputStreamWriter fwInvalido = null;
        BufferedWriter bwInvalido = null;

        File fManual = null;
        //FileWriter fwManual = null;
        FileOutputStream foManual = null;
        OutputStreamWriter fwManual = null;
        BufferedWriter bwManual = null;

        File fileIn = null;
        
        arq.setEstouroLimite(false);
        
        int countLine = 0;

        try {
            
            LinkedList pre = preValidacao(arq);
            
            if (!Valida.isEmpty(pre)) {
                arq.setExceptions(pre);
                return arq;
            }
        
            // verifica o flag de gravação de registros validos e inicializa o buffer
            if (this.entrada.isSubmitValido() || this.entrada.isGeraArqValido()) {
                fVal = File.createTempFile("validos",".ok",new File((String)params.get("PATH_TEMP")));
                //fwValido = new FileWriter(fVal);
                foValido = new FileOutputStream(fVal);
                fwValido = new OutputStreamWriter(foValido, this.charset);
                bwValido = new BufferedWriter(fwValido);
                arq.setPathArqValido(fVal.getAbsolutePath());
            }
            
            // verifica o flag de gravação de registros invalidos e inicializa o buffer
            if (this.entrada.isSubmitInvalido() || this.entrada.isGeraArqInvalido()) {
                fInval = File.createTempFile("invalidos",".err",new File((String)params.get("PATH_TEMP")));
                //fwInvalido = new FileWriter(fInval);
                foInvalido = new FileOutputStream(fInval);
                fwInvalido = new OutputStreamWriter(foInvalido, this.charset);
                bwInvalido = new BufferedWriter(fwInvalido);
                arq.setPathArqInvalido(fInval.getAbsolutePath());
            }
            
            // verifica o flag de gravação de registros invalidos e inicializa o buffer
            if (this.entrada.isSubmitManual() || this.entrada.isGeraArqManual()) {
                fManual = File.createTempFile("manual",".man",new File((String)params.get("PATH_TEMP")));
                //fwManual = new FileWriter(fManual);
                foManual = new FileOutputStream(fManual);
                fwManual = new OutputStreamWriter(foManual, this.charset);
                bwManual = new BufferedWriter(fwManual);
                arq.setPathArqManual(fManual.getAbsolutePath());
            }
            
            
            pf = new ParseFile(arq.getXmlLayout(), arq.getIdLayout(), false, true);
            pf.setTotalizarOcorrencias(false);
            
            // os arquivos de saida são separados em dois portanto os totalizadores de saida não
            // fazem muito sentido
            // estava dando erro no metodo novo validaLinhaAlteraSaida
            pf.setTotalizarWrite(false);
            
            arq.setDelimitado(pf.getLayout().isDelimitado());
            
            pf.setTrim(false);
            
            fileIn = new File(arq.getArquivo());
            
            //new InputStreamReader(new FileInputStream(), "ISO-8859-1")
            
            //fr = new FileReader(fileIn);
            fs = new FileInputStream(fileIn);
            fr = new InputStreamReader(fs, this.charset);
            in = new BufferedReader(fr);

            //frEol = new FileReader(fileIn);
            fsEol = new FileInputStream(fileIn);
            frEol = new InputStreamReader(fsEol, this.charset);
            brEol = new BufferedReader(frEol);
            
            String line = null;
            
            
            //int limExcept = Integer.parseInt((String)params.get("LIMITE_ERROS_ARQ"));
            int limExcept = this.entrada.getLimiteErros();
            int countExcept = 0;
            
            int lenEol = 1; // considera inicialmente só o \n
            long fileSize = fileIn.length();
            long bytes = 0;
            
            while ((line = in.readLine()) != null) {
                
                countLine++;
                
                // codigo para verificação se a ultima linha do arquivo está em branco
                // exclusivamente para a ultima linha em branco não passa pelo parse file
                if (countLine == 1) {
                    // na primeira linha irá descobrir se o finalizador é somente \n ou \n\r
                    char[] eol = new char[line.length() + 4];
                    
                    brEol.read(eol);
                    
                    brEol.close(); brEol = null;
                    frEol.close(); frEol = null;
                    fsEol.close(); fsEol = null;
                    
                    String linAux = new String(eol);
                    
                    if ((linAux.indexOf("\r\n") != -1) || (linAux.indexOf("\n\r") != -1)){
                        lenEol = 2;
                    }
                }
                
                bytes += line.length() + lenEol;
                
                // irá ignorar a linha somente se for a ultima linha do arquivo e essa estiver
                // em branco
                boolean ignoreLine = ((Valida.isEmpty(line)) && (bytes == fileSize));
                
                Valores valEntrada = null;
                boolean comErroDeParse = false;
                
                if (!ignoreLine) {
                	try {
                		
                		valEntrada = pf.converteLinha(line);
                		
                    } catch (ParseFileException e) {
                    	comErroDeParse = true;
                    	
                        LinExcept lin = new LinExcept(countLine);
                        lin.addCol(new ColExcept("", e.getMessage()));
                        
                        if (entrada.isRetornaLinhasConteudo()) {
                        	lin.setLinha(line);
                        }
                        
                        linhas.add(lin);
                        
                        valEntrada = null;
					}
                    
                }
                if (valEntrada != null) {
                	
                	
                	// inicializa a validação externa
                	boolean hasExceptExterno = false;
                	linhasExcept = new LinkedList();

                	// validação externa de linha
                	// 09/02/2010 foi necessário passar a linha stind para uma validação
                	// externa portanto, quand for atualizadas as aplicaçãoes ira apresentar um erro
            		validaLinha(countLine, valEntrada, line);
            		
            		// altera a linha para gravação nos arquivos de saída
            		String lineAux = validaLinhaAlteraSaida(countLine, valEntrada, line, pf);
            		
            		if ((lineAux != null) && (!lineAux.equals(line))) {
            			line = lineAux;
            		}
            		
            		// conta quantas exceções foram lançadas na validação externa
            		// e alimenta a coleção interna de arquivos
            		for (int i = 0; i < linhasExcept.size(); i++) {
            			hasExceptExterno = true;
            			linhas.add(linhasExcept.get(i));
            			countExcept++; 
            		}
                	
            		// 24/05/2010 não levava em consideracao exceptions externas 
            		// para a soma de ocorrencias então foi feito o codigo abaixo
            		if (pf.getLayout().getOcorrencias() != null) {
            			boolean except = valEntrada.hasException();
            			if (!except) {
            				except = hasExceptExterno;
            			}
            			
            			pf.getLayout().getOcorrencias().soma(valEntrada, except);
            		}

                	
                    if (valEntrada.hasException()) {
                        
                        String[] val = valEntrada.getValores();
                        
                        LinExcept lin = new LinExcept(countLine);
                        
                        if (entrada.isRetornaLinhasConteudo()) {
                        	lin.setLinha(line);
                        }
                        
                        //boolean hasException = false;
                        for (int i = 0; i < val.length; i++) {
                            
                            ColExcept col = new ColExcept(val[i]);
                            if (valEntrada.getException(i) != null) {
                                countExcept++; 
                                col.setMsg(valEntrada.getException(i).getMessage());
                                col.setPosIni(valEntrada.getColuna(i).getPosIni() + 1);
                                col.setTamanho(valEntrada.getColuna(i).getTamanho());
                                col.setId(valEntrada.getColuna(i).getId());
                            }
                            
                            lin.addCol(col);
                        }
                        
                        linhas.add(lin);
                    }
                    
                    // se houve erro na linha ou erro Externo loga como linha inválida
                    if (valEntrada.hasException() || hasExceptExterno) {
                        
                        // grava as linhas que estão inválidas
                        if (this.entrada.isSubmitInvalido() || this.entrada.isGeraArqInvalido()) { bwInvalido.write(new StringBuffer(line).append("\r\n").toString()); }
                        
                        if (countExcept > limExcept) {
                            arq.setEstouroLimite(true);
                            arq.setExceptions(linhas);
                            
                            return arq;
                        }
                        
                        
                    } else {
                        // grava as linhas que estão válidas
                        if (this.entrada.isSubmitValido() || this.entrada.isGeraArqValido()) { bwValido.write(new StringBuffer(line).append("\r\n").toString()); }
                    }
                    
                    if (this.entrada.isSubmitManual() || this.entrada.isGeraArqManual()) { bwManual.write(new StringBuffer(line).append("\r\n").toString()); }
                    
                }  else {
                	
                    // se houve erro na linha ou erro Externo loga como linha inválida
                    if (comErroDeParse) {
                    	if ((valEntrada == null) || (valEntrada.hasException())) {
                        
	                        // grava as linhas que estão inválidas
	                        if (this.entrada.isSubmitInvalido() || this.entrada.isGeraArqInvalido()) { bwInvalido.write(new StringBuffer(line).append("\r\n").toString()); }
	                        
	                        if (countExcept > limExcept) {
	                            arq.setEstouroLimite(true);
	                            arq.setExceptions(linhas);
	                            
	                            return arq;
	                        }
                    	}
                    } else {
                        // grava as linhas que estão válidas
                        if (this.entrada.isSubmitValido() || this.entrada.isGeraArqValido()) { bwValido.write(new StringBuffer(line).append("\r\n").toString()); }
                    }
                    
                    if (this.entrada.isSubmitManual() || this.entrada.isGeraArqManual()) { bwManual.write(new StringBuffer(line).append("\r\n").toString()); }
                }
            }
            
            
            
        	try {
                pf.throwReadExceptions();
        		
            } catch (ParseFileException e) {
                LinExcept lin = new LinExcept(countLine);
                lin.addCol(new ColExcept("", e.getMessage()));
                
                if (entrada.isRetornaLinhasConteudo()) {
                	lin.setLinha(line);
                }
                
                linhas.add(lin);
			}
            
            
            arq.setExceptions(linhas);
            
            arq.setOcorrencias(pf.getLayout().getOcorrencias());
            //return linhas;
            
            
            
        
        } catch (ParseFileException e) {
            
            linhas = new LinkedList();
            
            LinExcept lin = new LinExcept(countLine);
            
            lin.addCol(new ColExcept("", e.getMessage()));
            
            linhas.add(lin);
            
            arq.setExceptions(linhas);
            //return linhas;
            
        } catch (Exception e) {
            throw e;
            
        } finally {
            if (pf != null) { pf.clear(); pf = null; }
            
            if (in != null) { in.close(); in = null; }
            if (fr != null) { fr.close(); fr = null; }
            if (fs != null) { fs.close(); fs = null; }
            
            if (brEol != null) { brEol.close(); brEol = null; }
            if (frEol != null) { frEol.close(); frEol = null; }
            if (fsEol != null) { fsEol.close(); fsEol = null; }
            
            if (bwValido != null) { bwValido.close(); bwValido = null; }
            if (fwValido != null) { fwValido.close(); fwValido = null; }
            if (foValido != null) { foValido.close(); foValido = null; }
            
            if (bwInvalido != null) { bwInvalido.close(); bwInvalido = null; }
            if (fwInvalido != null) { fwInvalido.close(); fwInvalido = null; }
            if (foInvalido != null) { foInvalido.close(); foInvalido = null; }
            
            if (bwManual != null) { bwManual.close(); bwManual = null; }
            if (fwManual != null) { fwManual.close(); fwManual = null; }
            if (foManual != null) { foManual.close(); foManual = null; }
            
            
            // se estourar o limite de exceções não deve gerar os arquivos de registros validos e inválidos
            if (!arq.isEstouroLimite()) {
                
                // se não estourar efetua um submit dos arquivos de log caso o flag de envio esteja setado
                if ((this.entrada.isSubmitValido()) && (fVal != null)) { 
                    
                    submitFile(
                        fVal
                        ,(String)params.get("PATH_SUBMIT")
                        ,this.entrada.getSenderValido()
                        ,this.entrada.getReceiverValido()
                        ,this.entrada.getDoctypeValido()
                        ,T_VALIDO
                    );
                }
                
                if ((this.entrada.isSubmitInvalido()) && (fInval != null)) { 
                    
                    submitFile(
                        fInval
                        ,(String)params.get("PATH_SUBMIT")
                        ,this.entrada.getSenderInvalido()
                        ,this.entrada.getReceiverInvalido()
                        ,this.entrada.getDoctypeInvalido()
                        ,T_INVALIDO
                    );
                    
                }
                
                if ((this.entrada.isSubmitManual()) && (fManual != null)) { 
                    
                    submitFile(
                    	fManual
                        ,(String)params.get("PATH_SUBMIT")
                        ,this.entrada.getSenderManual()
                        ,this.entrada.getReceiverManual()
                        ,this.entrada.getDoctypeManual()
                        ,T_MANUAL
                    );
                    
                }
                
            }
            
            // sempre exclui os temporários enviando-os ou não, porém caso estrure o limite
            // esses arquivos devem ser descartados e excluidos
            if ((this.entrada.isSubmitValido()) && (fVal != null)) {
                if (!fVal.delete()) {
                    System.out.println("ValidaArq:validar()-Não foi possível excluir o arquivo: " + fVal.getAbsolutePath() + " através do componente de validação");
                }
            }
            if ((this.entrada.isSubmitInvalido()) && (fInval != null)) { 
                if (!fInval.delete()) {
                    System.out.println("ValidaArq:validar()-Não foi possível excluir o arquivo: " + fInval.getAbsolutePath() + " através do componente de validação");
                }
            }
            if ((this.entrada.isSubmitManual()) && (fManual != null)) { 
                if (!fManual.delete()) {
                    System.out.println("ValidaArq:validar()-Não foi possível excluir o arquivo: " + fManual.getAbsolutePath() + " através do componente de validação");
                }
            }
            
            
            // submente o arquivo original caso esteja configurado
            if ( (fileIn != null) && (fileIn.isFile()) && (this.entrada.isSubmitOriginal()) ) {
                    submitFile(
                        fileIn
                        ,(String)params.get("PATH_SUBMIT")
                        ,this.entrada.getSenderOriginal()
                        ,this.entrada.getReceiverOriginal()
                        ,this.entrada.getDoctypeOriginal()
                        ,T_ORIGINAL
                    );
            }
            
        }
        
        return arq;
    }
    
    public void addException(int numlinha, String value, String msg, int posIni, int tamanho, String colId) {
    	addException(numlinha, value, msg, posIni, tamanho, colId, null);  	
    }
    
    public void addException(int numlinha, String value, String msg, int posIni, int tamanho, String colId, String linha) {
    	
        LinExcept lin = new LinExcept(numlinha);
        
        ColExcept col = new ColExcept(value);
        col.setMsg(msg);
        col.setPosIni(posIni);
        col.setTamanho(tamanho);
        col.setId(colId);
        
        if (entrada.isRetornaLinhasConteudo()) {
        	lin.setLinha(linha);
        }
        
        lin.addCol(col);
        
        linhasExcept.add(lin);
    	
    }
    
    
    private LinkedList preValidacao(Arquivo arq) throws Exception {
        LinkedList preValid = new LinkedList();
        
        if (Valida.isEmpty(arq.getXmlLayout())) {
            LinExcept lin = new LinExcept(-1);
            lin.addCol(new ColExcept("", "Arquivo XML de Layout não configurado"));
            preValid.add(lin);
        } else {
            if (!(new File(arq.getXmlLayout())).isFile()) {
                LinExcept lin = new LinExcept(-1);
                lin.addCol(new ColExcept("", new StringBuffer("Arquivo XML de Layout: ").append(arq.getXmlLayout()).append(" inválido").toString()));
                preValid.add(lin);
            }
        }
        
        if (Valida.isEmpty(arq.getIdLayout())) {
            LinExcept lin = new LinExcept(-1);
            lin.addCol(new ColExcept("", "ID de Layout não configurado"));
            preValid.add(lin);
        }
        
        if (Valida.isEmpty(arq.getArquivo())) {
            LinExcept lin = new LinExcept(-1);
            lin.addCol(new ColExcept("", "Arquivo não configurado"));
            preValid.add(lin);
        } else {
            if (!(new File(arq.getArquivo())).isFile()) {
                LinExcept lin = new LinExcept(-1);
                lin.addCol(new ColExcept("", new StringBuffer("Arquivo: ").append(arq.getArquivo()).append(" inválido").toString()));
                preValid.add(lin);
            }
        }
        
        return preValid;
    }
    
    //------------------------------------
    // Inicia o contexto do velocity caso vá utilizar algum retorno que necesite do velocity
    //------------------------------------
    private void initVelocity(boolean tela) throws Exception {
        if (this.entrada.isSendMail() 
                || this.entrada.isSubmitLog()
                || tela
            ) {
                
            velocityContext = new VelocityContext();
            
            ve = new VelocityEngine();
            ve.init();
        }
    }
    
    
    
    
    //------------------------------------
    // Efetua o tratamentos de saídas da validação
    //------------------------------------
    
    private String montaTela(ArrayList arqs) throws Exception {
        // para envio de email retorna o conteudo e exclui o arquivo gerado
        //return execVelocity("VELOCITY_TEMPLATE_TELA", arqs, true);
        if (((this.temErro) && (this.entrada.getForcaRetorno() == ValidarVO.ERRO)) 
            || ((!this.temErro) && (this.entrada.getForcaRetorno() == ValidarVO.OK))
            || (this.entrada.getForcaRetorno() == ValidarVO.OK_E_ERRO))
        {
            return execVelocity(this.entrada.getTemplateTela(), arqs, true);
        }
        
        return null;
    }
    
    private void sendMail(ArrayList arqs) throws Exception {
        
        // para envio de email retorna o conteudo e exclui o arquivo gerado
        if (!this.entrada.isSendMail()) return;
        
        
        if (!(((this.temErro) && (this.entrada.getForceSendMail() == ValidarVO.ERRO)) 
            || ((!this.temErro) && (this.entrada.getForceSendMail() == ValidarVO.OK))
            || (this.entrada.getForceSendMail() == ValidarVO.OK_E_ERRO)))
        {
            return;
        }
        
        //String anexo = execVelocity("VELOCITY_TEMPLATE_EMAIL", arqs, false);
        
        StringBuffer files = new StringBuffer();
        for (int f = 0; f < arqs.size(); f++) {
            Arquivo arq = (Arquivo)arqs.get(f);
            
            if (arq.hasException()) {
                files.append("<br>");
                files.append(arq.getArquivoNome());
            }
        }
        
        //String body = (String)params.get("EMAIL_BODY");
        String body = this.entrada.getHtmlEmailBody();
        
        body = body.replaceAll("<:files:>",files.toString());
        
        File fAnexo = null;
        
        if (body.indexOf("<:contentFile:>") != -1) {
        	
        	if (fAnexo == null) {
	            String anexo = execVelocity(this.entrada.getTemplateEmail(), arqs, false);
	            
	            fAnexo = new File(anexo);
        	}
        	
        	String fileStr = StringUtil.fileToString(fAnexo);
        	
            body = body.replaceAll("<:contentFile:>",fileStr);
        }
        
        if (this.entrada.isSendAnexo()) {
        	if (fAnexo == null) {
	            String anexo = execVelocity(this.entrada.getTemplateEmail(), arqs, false);
	         
	            fAnexo = new File(anexo);
        	}
        	
            body = body.replaceAll("<:anexo:>",fAnexo.getName());
            
            Email.send(
                 this.emailHost
                ,this.entrada.getEmailFrom()
                ,this.entrada.getEmailTo()
                ,this.entrada.getEmailSubject()
                , body
                , fAnexo
            );
        } else {
            
            Email.send(
                 this.emailHost
                ,this.entrada.getEmailFrom()
                ,this.entrada.getEmailTo()
                ,this.entrada.getEmailSubject()
                , body
            );
        }
        
        
        if(fAnexo != null && fAnexo.isFile()) {
            // exclui o arquivo temporário gerado para o email pois já foi enviado como anexo
            fAnexo.delete();
        }
    }
    
    
    
    //------------------------------------
    // retorna o codigo gerado pela execução do velocity
    //------------------------------------
    private String execVelocity (String template, ArrayList arqs, boolean retConteudo) throws Exception {
        
        //------------------------------------
        // parse dos dados para o velocity
        //------------------------------------
        
        //FileReader fr = null;
        FileInputStream fs = null;
        InputStreamReader fr = null;
        BufferedReader br = null;
        
        //FileWriter fw = null;
        FileOutputStream fo = null;
        OutputStreamWriter fw = null;
        
        
        
        //VelocityContext velocityContext = null;
        //VelocityEngine ve = null;
        
        File flOut = null;
        
        try {
            
            //fr = new FileReader(new File((String)params.get(template)));
            //fr = new FileReader(new File(template));
        	fs = new FileInputStream(new File(template));
        	fr = new InputStreamReader(fs, this.charset);
            br = new BufferedReader(fr);
        
            //velocityContext = new VelocityContext();
            velocityContext.put("arquivos",arqs);
            //velocityContext.put("limite",(String)params.get("LIMITE_ERROS_ARQ"));
            velocityContext.put("limite",String.valueOf(this.entrada.getLimiteErros()));
            
            //ve = new VelocityEngine(); 
            //ve.init(); 
                    
            flOut = File.createTempFile("except",".html",new File((String)params.get("PATH_TEMP")));
            //fw = new FileWriter(flOut, false);
            fo = new FileOutputStream(flOut,false);
            fw = new OutputStreamWriter(fo, this.charset);
            
            ve.evaluate(velocityContext, fw, "validacaoErro", br);
            fw.close();
            
            // caso esteja setado para não retornar o conteudo sai após a geração do arquivo
            // senão captura o contedúdo e exclui o arquivo gerado
            if (!retConteudo) {
                return flOut.getAbsolutePath();
            }
                
            //------------------------------------
            // captura do codigo html gerado
            //------------------------------------
			if (br != null) {br.close();br = null;}
			if (fr != null) {fr.close();fr = null;}
			if (fw != null) {fw.close();fw = null;}
			if (fs != null) {fs.close();fs = null;}
            
            //fr = new FileReader(flOut);
        	fs = new FileInputStream(flOut);
        	fr = new InputStreamReader(fs, this.charset);
            br = new BufferedReader(fr);
            
            
            String line = null;
            StringBuffer htmlBuff = new StringBuffer();
            
	        while ((line = br.readLine()) != null) { 
                line = line.replaceAll("\\t","");
                htmlBuff.append(line); 
            }
            
            String htm = htmlBuff.toString();

            br.close();
            fr.close();
            fs.close();
            flOut.delete();
            
            return htm;
            
        } catch (Exception e) {
            throw e;
        } finally {
			if (br != null) {br.close();br = null;}
			if (fr != null) {fr.close();fr = null;}
			if (fw != null) {fw.close();fw = null;}
			if (fs != null) {fs.close();fs = null;}
		}

    }
    
    public abstract void validaLinha(int numLinha, Valores val, String linha) throws Exception;
    
    public abstract String validaLinhaAlteraSaida(int numLinha, Valores val, String linhaOrig, ParseFile pf) throws Exception;
    
    public abstract void submitFile(File file, String pathSubmit, String sender, String receiver, String doctype, int tipo) throws Exception;
} 

