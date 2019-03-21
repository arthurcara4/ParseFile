package br.com.accesstage.parsefile.validation.vo; 

import java.util.ArrayList;

public class ValidarVO implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	public static final int OK = 0;
    public static final int ERRO = 1;
    public static final int OK_E_ERRO = 2;

    public static final int F_SERVICES = 0;
    public static final int F_TRACKING = 1;
    public static final int F_EXTERNO = 2;
    
    private String emailFrom;
    private String[] emailTo;
    private String emailSubject;
    private boolean sendMail;
    private boolean sendAnexo = true;
    private int forceSendMail;
    
	private String senderValido;
	private String receiverValido;
	private String doctypeValido;
    private boolean submitValido;
	private boolean geraArqValido;
    
	private String senderInvalido;
	private String receiverInvalido;
	private String doctypeInvalido;
    private boolean submitInvalido;
	private boolean geraArqInvalido;

	private String senderLog;
	private String receiverLog;
	private String doctypeLog;
    private boolean submitLog;
	private boolean geraArqLog;	
    
	private String senderOriginal;
	private String receiverOriginal;
	private String doctypeOriginal;
    private boolean submitOriginal;
    
    
    private ArrayList arqsExternos;
    
    private int type;
    private String chave;
    
    private String templateFileLog;
    private String templateEmail;
    private String templateTela;
    private int limiteErros;
    private String htmlEmailBody;
    
    private int forcaRetorno;
    
    private boolean retornaLinhasErro;
    private boolean retornaLinhasConteudo;
    
	private String senderManual;
	private String receiverManual;
	private String doctypeManual;
    private boolean submitManual;
	private boolean geraArqManual;
    
    
    public ValidarVO() { 
        this.sendMail = false;
        this.submitValido = false;
        this.submitInvalido = false;
        this.submitLog = false;
        this.submitManual = false;
        
    	this.geraArqValido = false;
    	this.geraArqInvalido = false;
    	this.geraArqLog = false;
    	this.geraArqManual = false;
        
        this.forcaRetorno = ERRO;
        this.forceSendMail = ERRO;
        
        this.retornaLinhasErro = false;
        this.retornaLinhasConteudo = false;

        this.type = -1;
    }
    
    public void setTracking(String tracking) {
        this.type = F_TRACKING;
        this.chave = tracking;
    }
    
    public void setService(String service) {
        this.type = F_SERVICES;
        this.chave = service;
    }
    
    public void addArquivo(
         String arquivo
        , String xmlLayout
        , String idLayout
    ) {
        addArquivo(arquivo, xmlLayout, idLayout, null, null, null, null, null);
    }
    
    
    public void addArquivo(
         String arquivo
        , String xmlLayout
        , String idLayout
        , String sender
        , String receiver
        , String doctype
        , String dataGer
        , String trackid
    ) {
        this.type = F_EXTERNO;
        
        Arquivo arq = new Arquivo();
        arq.setArquivo(arquivo);
        arq.setXmlLayout(xmlLayout);
        arq.setIdLayout(idLayout);
        arq.setSender(sender);
        arq.setReceiver(receiver);
        arq.setDoctype(doctype);
        arq.setDataGer(dataGer);
        arq.setTrackid(trackid);
        
        if (this.arqsExternos == null) {
            this.arqsExternos = new ArrayList();
        }
        
        this.arqsExternos.add(arq);
    }

    public void addArquivo(Arquivo arq) {
        if (this.arqsExternos == null) {
            this.arqsExternos = new ArrayList();
        }
        
        this.arqsExternos.add(arq);
    }
    
    public void setEmail(String to[]) {
        setEmail(to, null, null);
    }
    
    public void setEmail(String to[], String subject) {
        setEmail(to, null, subject);
    }
    
    public void setEmail(String to[], String from, String subject) {
        this.emailTo = to;
        this.emailFrom = from;
        this.emailSubject = subject;
        this.sendMail = true;
    }
    
	public void submitValido(String sender, String receiver, String doctype) {
        this.submitValido = true;
		this.senderValido = sender;
        this.receiverValido = receiver;
        this.doctypeValido = doctype;
	}
	
	public void submitInvalido(String sender, String receiver, String doctype) {
        this.submitInvalido = true;
		this.senderInvalido = sender;
        this.receiverInvalido = receiver;
        this.doctypeInvalido = doctype;
	}
	
	public void submitLog(String sender, String receiver, String doctype) {
        this.submitLog = true;
		this.senderLog = sender;
        this.receiverLog = receiver;
        this.doctypeLog = doctype;
	}

	public void submitOriginal(String sender, String receiver, String doctype) {
        this.submitOriginal = true;
		this.senderOriginal = sender;
        this.receiverOriginal = receiver;
        this.doctypeOriginal = doctype;
	}

	public String getEmailFrom() {
		return emailFrom;
	}

	public void setEmailFrom(String emailFrom) {
		this.emailFrom = emailFrom;
	}

	public String[] getEmailTo() {
		return emailTo;
	}

	public void setEmailTo(String[] emailTo) {
		this.emailTo = emailTo;
	}

	public String getEmailSubject() {
		return emailSubject;
	}

	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	public boolean isSendMail() {
		return sendMail;
	}

	public void setSendMail(boolean sendMail) {
		this.sendMail = sendMail;
	}
    
	public boolean isSendAnexo() {
		return sendAnexo;
	}

	public void setSendAnexo(boolean sendAnexo) {
		this.sendAnexo = sendAnexo;
	}
    
	public int getForceSendMail() {
		return forceSendMail;
	}

	public void setForceSendMail(int forceSendMail) {
		this.forceSendMail = forceSendMail;
	}    
    

	public String getSenderValido() {
		return senderValido;
	}

	public void setSenderValido(String senderValido) {
		this.senderValido = senderValido;
	}

	public String getReceiverValido() {
		return receiverValido;
	}

	public void setReceiverValido(String receiverValido) {
		this.receiverValido = receiverValido;
	}

	public String getDoctypeValido() {
		return doctypeValido;
	}

	public void setDoctypeValido(String doctypeValido) {
		this.doctypeValido = doctypeValido;
	}

	public boolean isSubmitValido() {
		return submitValido;
	}

	public void setSubmitValido(boolean submitValido) {
		this.submitValido = submitValido;
	}
    
	public boolean isGeraArqValido() {
		return geraArqValido;
	}
    
	public void setGeraArqValido(boolean geraArqValido) {
		this.geraArqValido = geraArqValido;
	}
    
    
    
    
    
	public String getSenderOriginal() {
		return senderOriginal;
	}

	public void setSenderOriginal(String senderOriginal) {
		this.senderOriginal = senderOriginal;
	}

	public String getReceiverOriginal() {
		return receiverOriginal;
	}

	public void setReceiverOriginal(String receiverOriginal) {
		this.receiverOriginal = receiverOriginal;
	}

	public String getDoctypeOriginal() {
		return doctypeOriginal;
	}

	public void setDoctypeOriginal(String doctypeOriginal) {
		this.doctypeOriginal = doctypeOriginal;
	}

	public boolean isSubmitOriginal() {
		return submitOriginal;
	}

	public void setSubmitOriginal(boolean submitOriginal) {
		this.submitOriginal = submitOriginal;
	}
    
    



	public String getSenderInvalido() {
		return senderInvalido;
	}

	public void setSenderInvalido(String senderInvalido) {
		this.senderInvalido = senderInvalido;
	}

	public String getReceiverInvalido() {
		return receiverInvalido;
	}

	public void setReceiverInvalido(String receiverInvalido) {
		this.receiverInvalido = receiverInvalido;
	}

	public String getDoctypeInvalido() {
		return doctypeInvalido;
	}

	public void setDoctypeInvalido(String doctypeInvalido) {
		this.doctypeInvalido = doctypeInvalido;
	}

	public boolean isSubmitInvalido() {
		return submitInvalido;
	}

	public void setSubmitInvalido(boolean submitInvalido) {
		this.submitInvalido = submitInvalido;
	}

	public boolean isGeraArqInvalido() {
		return geraArqInvalido;
	}
    
	public void setGeraArqInvalido(boolean geraArqInvalido) {
		this.geraArqInvalido = geraArqInvalido;
	}




	public String getSenderLog() {
		return senderLog;
	}

	public void setSenderLog(String senderLog) {
		this.senderLog = senderLog;
	}

	public String getReceiverLog() {
		return receiverLog;
	}

	public void setReceiverLog(String receiverLog) {
		this.receiverLog = receiverLog;
	}

	public String getDoctypeLog() {
		return doctypeLog;
	}

	public void setDoctypeLog(String doctypeLog) {
		this.doctypeLog = doctypeLog;
	}

	public boolean isSubmitLog() {
		return submitLog;
	}

	public void setSubmitLog(boolean submitLog) {
		this.submitLog = submitLog;
	}

	public boolean isGeraArqLog() {
		return geraArqLog;
	}
    
	public void setGeraArqLog(boolean geraArqLog) {
		this.geraArqLog = geraArqLog;
	}



	public ArrayList getArqsExternos() {
		return arqsExternos;
	}

	public void setArqsExternos(ArrayList arqsExternos) {
		this.arqsExternos = arqsExternos;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getChave() {
		return chave;
	}

	public void setChave(String chave) {
		this.chave = chave;
	}
    
	public String getTemplateFileLog() {
		return templateFileLog;
	}
	public void setTemplateFileLog(String templateFileLog) {
		this.templateFileLog = templateFileLog;
	}
	public String getTemplateEmail() {
		return templateEmail;
	}
	public void setTemplateEmail(String templateEmail) {
		this.templateEmail = templateEmail;
	}
	public String getTemplateTela() {
		return templateTela;
	}
	public void setTemplateTela(String templateTela) {
		this.templateTela = templateTela;
	}
	public int getLimiteErros() {
		return limiteErros;
	}
	public void setLimiteErros(int limiteErros) {
		this.limiteErros = limiteErros;
	}
    
	public String getHtmlEmailBody() {
		return this.htmlEmailBody;
	}
	public void setHtmlEmailBody(String value) {
		this.htmlEmailBody = value;
	}


	public int getForcaRetorno() {
		return forcaRetorno;
	}    
    
	public void setForcaRetorno(int forcaRetorno) {
		this.forcaRetorno = forcaRetorno;
	}
    
	public boolean isRetornaLinhasErro() {
		return retornaLinhasErro;
	}
    
	public void setRetornaLinhasErro(boolean retornaLinhasErro) {
		this.retornaLinhasErro = retornaLinhasErro;
	}

	public boolean isRetornaLinhasConteudo() {
		return retornaLinhasConteudo;
	}
    
	public void setRetornaLinhasConteudo(boolean retornaLinhasConteudo) {
		this.retornaLinhasConteudo = retornaLinhasConteudo;
	}

	public String getSenderManual() {
		return senderManual;
	}

	public void setSenderManual(String senderManual) {
		this.senderManual = senderManual;
	}

	public String getReceiverManual() {
		return receiverManual;
	}

	public void setReceiverManual(String receiverManual) {
		this.receiverManual = receiverManual;
	}

	public String getDoctypeManual() {
		return doctypeManual;
	}

	public void setDoctypeManual(String doctypeManual) {
		this.doctypeManual = doctypeManual;
	}

	public boolean isSubmitManual() {
		return submitManual;
	}

	public void setSubmitManual(boolean submitManual) {
		this.submitManual = submitManual;
	}

	public boolean isGeraArqManual() {
		return geraArqManual;
	}

	public void setGeraArqManual(boolean geraArqManual) {
		this.geraArqManual = geraArqManual;
	}
	
	


}
