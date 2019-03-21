package br.com.accesstage.parsefile.utils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Date;

import br.com.accesstage.parsefile.retornos.DinTableVO;
import br.com.accesstage.parsefile.retornos.DinVO;

public class Valida {
	
	public static final int DATA_MAIOR = 1;
	public static final int DATA_MENOR = -1;
	public static final int DATA_IGUAL = 0;
	public static final int DATA_INVALIDA = -2;

	public static boolean isNull(Object value) {
		return (value == null);
	}
	
	public static boolean isEmpty(String value) {
		return (isNull(value) || ("".equals(value.trim())));
	}
	
	public static boolean isEmpty(String[] value) {
		return (isNull(value) || (value.length == 0));
	}
	
	public static boolean isEmpty(ArrayList value) {
		return (isNull(value) || (value.isEmpty()) || (value.size() == 0));
	}

	public static boolean isEmpty(AbstractList value) {
		return (isNull(value) || (value.isEmpty()) || (value.size() == 0));
	}
	
	public static boolean isEmpty(StringBuffer value) {
		return (isNull(value) || (isEmpty(value.toString())));
	}
	
	public static boolean isEmpty(File value) {
		return (isNull(value) || !value.exists() || (value.length() <= 0));
	}
	
    public static boolean isEmpty(Object source) {
        return (source == null);
    }

    public static boolean isEmpty(Object[] source) {
        return ((source == null) || (source.length == 0));
    }
    
    public static boolean isEmpty(Object[][] source) {
        return ((source == null) || (source.length == 0));
    }
	
    public static boolean isEmpty(DinVO source) {
        return ((source == null) || (source.getIds() == null) || source.getIds().isEmpty());
    }

    public static boolean isEmpty(DinTableVO source) {
        return ((source == null) || (source.getIds() == null) || source.getIds().isEmpty());
    }

    public static boolean isSenha(String senha) {
        
        if (senha == null) return false;
        
        if (senha.length() < 7) return false;
        
        boolean hasNumber = false;
        boolean hasAlfa = false;
        
        char[] c = senha.toCharArray();  
        
        for ( int i = 0; i < c.length; i++ )  {
            if ( Character.isLetter( c[ i ] ) ) {  
                hasAlfa = true;
            }  
            
            if ( Character.isDigit( c[ i ] ) ) {  
                hasNumber = true;
            }  
        }  
        
        return (hasNumber && hasAlfa);
    }
    
    public static boolean isData(String data) {
        return isData(data,"dd/MM/yyyy");
    }
  
    public static boolean isData(String data,String format) {
        if (isEmpty(data)) {
            return false;
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false);
        try {
            sdf.parse(data);
            return true;
        }
        catch (ParseException ex) {
            return false;
        }
    }
    
    
    public static boolean isZero(String num) {
        if (isEmpty(num)) {
            return true;
        }
        
        String mask = "0123456789";
        
        StringBuffer aux = new StringBuffer();
        for (int i = 0; i < num.length(); i++) {
        	String ch = num.substring(i, i+1);
        	if (mask.indexOf(ch) != -1) {
        		aux.append(ch);
        	}
        }
        if (Valida.isEmpty(aux)) return true;
        
        num = aux.toString();
        
        try {
            return (Long.parseLong(num) == 0);
        }
        catch (NumberFormatException ex) {
            return true;
        }
    }
    
    public static int statusData(String data) {
		return statusData(data,"dd/MM/yyyy");
	}

	
	public static int statusData(String data, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String data2 = sdf.format(new Date());
        
        return compareData(data, data2, format);
	}
	
	public static int compareData(String data1, String data2) {
		return compareData(data1,data2,"dd/MM/yyyy");
	}

	public static int compareData(String data1, String data2, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false);
        try {
        	String compFormat = "yyyyMMdd";
        	if ("MMyy".equalsIgnoreCase(format)) {
        		compFormat = "yyyyMM";
        	}
        		
            SimpleDateFormat sdf2 = new SimpleDateFormat(compFormat);
            
            long dt1 = Long.parseLong(sdf2.format(sdf.parse(data1)));
            long dt2 = Long.parseLong(sdf2.format(sdf.parse(data2)));
            
            
            if (dt1 > dt2 ) { return DATA_MAIOR; }
            if (dt1 < dt2 ) { return DATA_MENOR; }

            return DATA_IGUAL;
        }
        catch (ParseException ex) {
        	return DATA_INVALIDA;
        }
	}
	
	
	public static boolean isCpf(String cpf) {
		
		cpf = StringUtil.toNumber(cpf);
		if (Valida.isEmpty(cpf)) return false;
		
		cpf = StringUtil.padEsq(cpf, 11, "0");
		
		String digOrig = cpf.substring(cpf.length() - 2);
		String digCalc = StringUtil.calcDac11(cpf.substring(0,(cpf.length() - 2)), false );
		return (digOrig.equals(digCalc));
	}
	
	public static boolean isCnpj(String cnpj) {
		
		cnpj = StringUtil.toNumber(cnpj);
		if (Valida.isEmpty(cnpj)) return false;
		
		cnpj = StringUtil.padEsq(cnpj, 14, "0");
		
		String digOrig = cnpj.substring(cnpj.length() - 2);
		String digCalc = StringUtil.calcDac11(cnpj.substring(0,(cnpj.length() - 2)), true );
		return (digOrig.equals(digCalc));
	}
}
