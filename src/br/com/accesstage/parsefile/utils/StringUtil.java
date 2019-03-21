/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.com.accesstage.parsefile.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author glauco
 */
public class StringUtil {
	
	public static String PAD_ESQ = "ESQ";
	public static String PAD_DIR = "DIR";

    public static String delim(String source, String delim, int start) {
        return delim(source, delim, delim, start, false);
    }

    public static String delim(String source, String delim, int start, boolean ignoreFim) {
        return delim(source, delim, delim, start, ignoreFim );
    }

    public static String delim(String source, String ini, String fim, int start) {
        return delim(source, ini, fim, start, false);
    }

    public static String delim(String source, String ini, String fim, int start, boolean ignoreFim) {
        int posi = source.indexOf(ini, start);
        if (posi == -1) return null;

        posi += ini.length() - 1;
        int posf = source.indexOf(fim, posi + 1);
        if (posf == -1) {
            if (!ignoreFim) {
                return null;
            }
            posf = source.length();
        }


        return source.substring(posi + 1,posf);
    }
    
    public static String maxDelim(String source, String delim, int start) {
        return maxDelim(source, delim, delim, start, false);
    }

    public static String maxDelim(String source, String delim, int start, boolean ignoreFim) {
        return maxDelim(source, delim, delim, start, ignoreFim );
    }

    public static String maxDelim(String source, String ini, String fim, int start) {
        return maxDelim(source, ini, fim, start, false);
    }

    public static String maxDelim(String source, String ini, String fim, int start, boolean ignoreFim) {
    	
		int posi = source.indexOf(ini, start);
		if (posi == -1) return null;
		
		int posf = source.indexOf(fim, posi + 1);
		
		if (posf == -1) {
            if (!ignoreFim) {
                return null;
            }
            return source.substring(posi+1);
		}
		
		
		String inner = source.substring(posi + 1, posf);
		
		int abre = StringUtil.count(ini, inner);
		
		int fecha = StringUtil.count(fim, inner);
		
		while (abre != fecha) {
			
			posf = source.indexOf(fim, posf + 1);
			
			if (posf == -1) {
	            if (!ignoreFim) {
	                return null;
	            }
	            return inner;
			}
			
			inner = source.substring(posi + 1, posf);
			
			abre = StringUtil.count(ini, inner);
			
			fecha = StringUtil.count(fim, inner);
		}
    	
		return inner;
    }
    
	public static String padEsq(Object str, int len, String pad) {
		return pad(String.valueOf(str), len, pad, StringUtil.PAD_ESQ);
	}
	public static String padDir(Object str, int len, String pad) {
		return pad(String.valueOf(str), len, pad, StringUtil.PAD_DIR);
	}
	public static String padEsq(String str, int len, String pad) {
		return pad(str, len, pad, StringUtil.PAD_ESQ);
	}
	public static String padDir(String str, int len, String pad) {
		return pad(str, len, pad, StringUtil.PAD_DIR);
	}
    
    
	public static String pad(String str, int len, String pad, String padSide) {
		
		if (Valida.isEmpty(str)) str = "";
		
		if (Valida.isEmpty(padSide)) padSide = PAD_DIR;
		padSide = padSide.toUpperCase();
		
		if (str.length() > len) return str.substring(0, len);
		
		if (str.length() == len) return str;
		
		
		int diff = (len - str.length());
		
		String compl = rpl(pad, diff);
		
		if (PAD_ESQ.equals(padSide)) {
			return new StringBuffer().append(compl).append(str).toString();
		}
		
		return new StringBuffer().append(str).append(compl).toString();
		
	}
    
	public static String rpl(String ch, int count) {
		StringBuffer result = new StringBuffer();
		for(int cc = 0; cc  < count; cc++) {
			result.append(ch);
		}
		return result.toString();
	}
	
	public static int count(String regex, String text) {
		if (regex.indexOf("\\") == -1) regex = new StringBuffer("\\").append(regex).toString();
		
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(text);
		int count = 0;
		while (m.find()) { ++count; }
		
		return count;
	}
	
	public static String fmtValor(long value, int decimais) {
		return fmtValor(String.valueOf(value) , decimais );
	}
	public static String fmtValor(String value, int decimais) {
		
		while (value.length() < (decimais + 1)) {
			value = new StringBuffer().append("0").append(value).toString();
		}
		
		int decCount = 0;
		int intCount = 0;
		
		StringBuffer res = new StringBuffer();
		
		for (int p = (value.length() - 1); p >= 0; p--) {
			
			res.insert(0, value.substring(p, p+1));
			
			decCount++;
			
			if (decCount == decimais) {
				res.insert(0,",");
			}
			
			if (decCount > decimais) {
				intCount++;
				
				if ((intCount == 3) && (p > 0)) {
					res.insert(0,".");
					intCount = 0;
				}
			}
		}
        
        return res.toString();
	}
	
	public static String fmtData(String source, String formato, String formatoOut) {
		if (source == null) return "";
		
		try {
            SimpleDateFormat sdf = new SimpleDateFormat(formato);
            Date value = sdf.parse(source);
            
			return new SimpleDateFormat(formatoOut).format(value);
		} catch (NumberFormatException e) {
			return "";
		} catch (ParseException pe) {
            return "";
        }
	}
	
	public static String fmtCnpj(String source) {
		if (source == null) return "";
		
		try {
			
			String c = source.trim();
			if (c.length() < 14) {
				c = new StringBuffer(rpl("0",14 - c.length())).append(c).toString();
			}
			
			if (c.length() > 14) {
				c = c.substring(c.length() - 14, c.length());
			}
            
			StringBuffer result = new StringBuffer();
			result.append(c.substring(0, 2)).append(".");
			result.append(c.substring(2, 5)).append(".");
			result.append(c.substring(5, 8)).append("/");
			result.append(c.substring(8, 12)).append("-");
			result.append(c.substring(12));
			
			return result.toString();
		} catch (Exception e) {
			return "";
        }
	}
	
	public static String fmtCpf(String source) {
		if (source == null) return "";
		
		try {
			
			String c = source.trim();
			if (c.length() < 11) {
				c = new StringBuffer(rpl("0",11 - c.length())).append(c).toString();
			}
			
			if (c.length() > 11) {
				c = c.substring(c.length() - 11, c.length());
			}
            
			StringBuffer result = new StringBuffer();
			result.append(c.substring(0, 3)).append(".");
			result.append(c.substring(3, 6)).append(".");
			result.append(c.substring(6, 9)).append("-");
			result.append(c.substring(9));
			
			return result.toString();
		} catch (Exception e) {
			return "";
        }
	}
	
	
	public static long toLong(String num) {
		try {
			return new Long(num).longValue();
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static int toInt(String num) {
		try {
			return new Integer(num).intValue();
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static String toNumber(String num) {
		if (Valida.isEmpty(num)) return "";
		
		StringBuffer ret = new StringBuffer();
		
		for(int i = 0; i < num.length(); i++) {
			if ( "0123456789".indexOf(num.substring(i, i+1)) != -1) {
				ret.append(num.substring(i, i+1));
			}
		}
		
		return ret.toString();
	}
	
	public static int calcDac10(String num) {
		int mult = 2;
		
		int total = 0;
		
		for (int i = (num.length() - 1); i >= 0; i--) {
			
			int  d = Integer.parseInt(num.substring(i, i + 1));
			
			int res = d * mult;
			
			if (res > 9) {
				String resStr = String.valueOf(res);
				for (int y = 0; y < resStr.length(); y++) 
					total += Integer.parseInt(resStr.substring(y, y + 1));
			} else {
				total += res;
			}
			
			mult = ((mult == 2)?1:2);
		}
		int i = 1;
		while ((i * 10) < total) i++; 
		
		return ((i * 10) - total);
	}

	public static String calcDac11(String num, boolean zerarFat) {
		
		num = StringUtil.toNumber(num);
		if ((num == null) || ("".equals(num.trim())) ) return "";
		
		String digStr[] = num.split("");
		
		int fat1 = 1;
		int fat2 = 2;
		int d1 = 0;
		int d2 = 0;
		for (int dd = (digStr.length - 1); dd > 0; dd--) {
			int dig = Integer.parseInt(digStr[dd]);
			fat1++;
			if ((zerarFat) && (fat1 > 9)) {fat1 = 2;}
			d1 += (dig * fat1);
			
			fat2++;
			if ((zerarFat) && (fat2 > 9)) {fat2 = 2;}
			d2 += (dig * fat2);
		}
		d1 = (11 - (d1%11));
		if (d1 >= 10) d1 = 0;
		
		d2 = (d1 * 2) + d2;
		d2 = (11 - (d2%11));
		if (d2 >= 10) d2 = 0;
		
		return new StringBuffer().append(d1).append(d2).toString();
	}
	
	
    public static String fileToString(String filePath) throws java.io.IOException{
    	return fileToString(new File(filePath));
    }
    
    public static String fileToString(File file) throws java.io.IOException{
    	if (!file.isFile()) return "";
    	
        byte[] buffer = new byte[(int) file.length()];
        BufferedInputStream f = new BufferedInputStream(new FileInputStream(file.getAbsolutePath()));
        f.read(buffer);
        return new String(buffer);
    }
    
    /**
     * Retorna a diferença entre datas no formado dd hh:mm:ss
     * @param dtaInicio - Data inicial
     * @param dtaFim - Data final
     * @retur dd hh:mm:ss
     */
	public static String getDifData(String dtaInicio, String formatoDtaIni, String dtaFim, String formatoDtaFim, String formatoOut) {
		
		try {
			 // Data final menos inicial
	        SimpleDateFormat sdf = new SimpleDateFormat(formatoDtaIni);
	        Date dt1 = sdf.parse(dtaInicio);
			
	        sdf = new SimpleDateFormat(formatoDtaFim);
	        Date dt2 = sdf.parse(dtaFim);
			
			long diff = dt2.getTime() -dt1.getTime();
			
			// Tempo = diferença em milisegundos dividido pela representação em milisegundos e recupera a sobra
			String dias  = Long.toString((diff / (1000*60*60*24)   ) % 24);  
			String horas  = Long.toString((diff / (1000*60*60)      ) % 24);  
			String minutos  = Long.toString((diff / (1000*60)       ) % 60);
			String segundos = Long.toString((diff / (1000)        ) % 60);
			 
			// formata com zeros a esquerda
			dias     = padEsq(dias   , 2, "0");
			horas    = padEsq(horas   , 2, "0");
			minutos  = padEsq(minutos , 2, "0");
			segundos = padEsq(segundos, 2, "0");
			
			 // Formata diferença de acordo com formato de saida
			formatoOut = formatoOut.replaceAll("dd",dias);
			formatoOut = formatoOut.replaceAll("hh",horas);
			formatoOut = formatoOut.replaceAll("HH",horas);
			formatoOut = formatoOut.replaceAll("mm",minutos);
			formatoOut = formatoOut.replaceAll("ss",segundos);
			 
			return(formatoOut);
		} catch (Exception e) {
			return "";
		}
	}    
	
	
    public static String barrasToDir(String str) throws java.io.IOException{
    	if (Valida.isEmpty(str)) return "";
    	
    	return str.replaceAll("\\", "/");
    	
    }
	
    
}
