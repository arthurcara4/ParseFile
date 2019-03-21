package br.com.accesstage.parsefile.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import br.com.accesstage.parsefile.retornos.DinVO;

public class VelocityUtil {
	
	public static final int OUT_FILE = 1;
	public static final int OUT_STRING = 2;
	
    private VelocityEngine ve;
    private String charset = "ISO-8859-1";
    private DinVO context; 

    
	public VelocityUtil() throws Exception {
	    ve = new VelocityEngine(); 
	    ve.init();
	}

	public VelocityUtil(VelocityEngine ve) throws Exception {
	    this.ve = ve; 
	}
	
	public void addContext(String id, Object obj) {
		if (Valida.isEmpty(context)) this.context = new DinVO();
		this.context.addset(id, obj);
	}
	
    public void setCharset(String charset) {
    	this.charset = charset;
    }
    public String getCharset() {
    	return this.charset;
    }
    public Object gerar(String fileTemplate, String pathTemp, String prefixoTemp, String extensaoTemp, int tipoRetorno) throws Exception {
    	return gerar(null, fileTemplate, pathTemp, prefixoTemp, extensaoTemp, tipoRetorno);
    }

    public Object gerar(Object dados, String fileTemplate, String pathTemp, String prefixoTemp, String extensaoTemp, int tipoRetorno) throws Exception {
        
        
        if (Valida.isEmpty(fileTemplate)) {
            throw new Exception("Não encontrou o arquivo de template: " + fileTemplate);
        }
        
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
            //------------------------------------
            // monta template velocity
            //------------------------------------
            
            File fTemplate = new File(fileTemplate);
            if (!fTemplate.isFile()) {
                throw new Exception("Não encontrou o arquivo de template: " + fileTemplate);
            }
        
            //fr = new FileReader(fTemplate);
            fs = new FileInputStream(fTemplate);
            fr = new InputStreamReader(fs, charset);
            br = new BufferedReader(fr);
            
            VelocityContext velocityContext = new VelocityContext();
            velocityContext.put("util", new StringUtil());
            
            if (!Valida.isEmpty(dados)) {
                velocityContext.put("dados", dados);
            }
            
            
            if (!Valida.isEmpty(context)) {
            	for (int i = 0; i < context.getIds().size() - 1; i++) {
            		velocityContext.put((String)context.getIds().get(i), context.get(i));
            	}
            }
            
                
            flOut = File.createTempFile(prefixoTemp,extensaoTemp,new File(pathTemp));
            fo = new FileOutputStream(flOut,false);
            fw = new OutputStreamWriter(fo, charset);
            
            VelocityEngine ve = new VelocityEngine(); 
            ve.init(); 

            ve.evaluate(velocityContext, fw, "VelocityUtil", br);
            
            fw.close();
            
            // retorna o arquivo temporario 
            if (tipoRetorno == OUT_FILE) {
                return flOut;
            }

            // (default) abre o arquivo e retorna uma string do conteudo
            if (tipoRetorno == OUT_STRING) {
                //------------------------------------
                // captura do codigo html gerado
                //------------------------------------
                
                //fr = new FileReader(flOut);
                fs = new FileInputStream(flOut);
                fr = new InputStreamReader(fs, charset);
                br = new BufferedReader(fr);
                
                
                String line = null;
                StringBuffer htmlBuff = new StringBuffer();
                
                while ((line = br.readLine()) != null) { 
                    line = line.replaceAll("\\t","");
                    htmlBuff.append(line); 
                }
                
                String html = htmlBuff.toString();
                
                br.close();
                fr.close();
                flOut.delete();
                
                return html;
            }
            
            return null;
            
            
        } catch (Exception e) {
            throw e;
        } finally {
			if (br != null) {br.close();br = null;}
			if (fr != null) {fr.close();fr = null;}
			if (fs != null) {fs.close();fs = null;}
			if (fw != null) {fw.close();fw = null;}
			if (fo != null) {fo.close();fo = null;}
		}
    }
	
	
}
