package br.com.accesstage.parsefile.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;

public class FileUtil {
    private String charset = "ISO-8859-1";
	
    public void setCharset(String charset) {
    	this.charset = charset;
    }
    public String getCharset() {
    	return this.charset;
    }
    
	public int eolLength(String fileName) throws Exception {
		
        //FileReader     fr = null;
        FileInputStream fs = null;
        InputStreamReader fr = null;
        BufferedReader br = null;
		
		
		try {
	        //fr = new FileReader(new File(fileName));
            fs = new FileInputStream(new File(fileName));
            fr = new InputStreamReader(fs, charset);
	        br = new BufferedReader(fr);
	        
	        
            String line = null;
            
            while ((line = br.readLine()) != null) {
            	
            	int lenLine = 0;
            	
            	// identifica o tamanho da linha
                if (!Valida.isEmpty(line)) lenLine = line.length();
                
                // le 5 caracteres a mais por margem de segurança
    	        char[] eol = new char[lenLine + 5];
    	        br.read(eol);
    	        
    	        // fecha o arquivo
    	        br.close(); br = null;
    	        fr.close(); fr = null;
    	        
    	        // verifica se encontra esse tipo de final de linha, se não encontra é só o \n que termina alinha
    	        String linAux = new String(eol);
    	        if ((linAux.indexOf("\r\n") != -1) || (linAux.indexOf("\n\r") != -1)){
    	            return 2;
    	        } else {
    	        	return 1;
    	        }
            }
            
            return 0;
        } catch (Exception e) {
            throw e;
        } finally {
            if (br  != null) {br.close();  br = null; }
            if (fr  != null) {fr.close();  fr = null; }
            if (fs  != null) {fs.close();  fs = null; }
        }
	}
	
    // Copia um arquivo.
    public void copyFile(File in, File out) throws Exception {
        FileChannel sourceChannel = new FileInputStream(in).getChannel();
        FileChannel destinationChannel = new FileOutputStream(out).getChannel();
        sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
        sourceChannel.close();
        destinationChannel.close();
    }

	

}
