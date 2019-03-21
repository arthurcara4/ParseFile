package br.com.accesstage.parsefile.utils;

import br.com.accesstage.parsefile.exceptions.ParseFileException;
import br.com.accesstage.parsefile.layout.ColunaLayout;

public class ParseUtils {
	public static final String DIREITA = "DIREITA";
	public static final String ESQUERDA = "ESQUERDA";
	
	
	public static String getIdStr(String id) {
		if (Valida.isEmpty(id)) id = "";
		return new StringBuffer("|").append(id.toUpperCase()).append(":").toString();
	}
	
	public static String getPosStr(int posId) {
		return new StringBuffer(":").append(posId).append("|").toString();
	}
	
	public static String getIdStr(String id, int index) {
		return getIdStr(id, String.valueOf(index));
	}
	
	public static String getIdStr(String id, String index) {
		return new StringBuffer(getIdStr(id)).append(index).append("|").toString();
	}
	
	public static int getPos(StringBuffer mapa, String id) {
		return getPos(mapa,id,0);
	}
	
	public static int getPos(StringBuffer mapa, String id, int start) {
		String pos = getValue(mapa, id, start);
		if (Valida.isEmpty(pos)) {
			return -1;
		}
		return Integer.parseInt(pos);
	}
	
	public static String getValue(StringBuffer mapa, String id) {
		return getValue(mapa,id,0);
	}
	
	public static String getValue(StringBuffer mapa, String id, int start) {
		
		String map = mapa.toString().toUpperCase();
		
		String idStr = getIdStr(id);
		
		int pos = map.indexOf(idStr, start);
		
		if (pos == -1) { return null; }
		
		int posIndex = pos + idStr.length();
		
		return map.substring(posIndex, map.indexOf("|",posIndex)).trim();
		
	}
	
	
	public static String getIdMap(StringBuffer mapa, int posId) {
		
		String map = mapa.toString().toUpperCase();
		
		String posStr = getPosStr(posId);
		
		int pos = map.indexOf(posStr);
		
		if (pos == -1) { return null; }
		
		map = map.substring(0,pos);
		
		int posIndex = map.lastIndexOf("|");
		
		return map.substring(posIndex);
		
	}
	
	public static String getMap(StringBuffer mapa, String id) {
		
		String map = mapa.toString().toUpperCase();
		
		String idStr = getIdStr(id);
		
		int pos = map.indexOf(idStr);
		
		if (pos == -1) { return null; }
		
		//int posIndex = pos + idStr.length();
		
		return map.substring(pos, map.indexOf("|",pos+1)).trim();
		
	}

	public static int getPosInMap(StringBuffer mapa, String id) {
		return getPosInMap(mapa, id, 0);
	}
	
	public static int getPosInMap(StringBuffer mapa, String id, int start) {
		
		String map = mapa.toString().toUpperCase();
		
		String idStr = getIdStr(id);
		
		return map.indexOf(idStr, start);
	}
	
	public static String formataValor(String tipo, String pad, String padSide, int decimais, String valor, int tamanho) throws ParseFileException {
		
		if (decimais == -1) decimais = 0;
		
		if (decimais > 0) {
			valor = toStrNum(valor, decimais);
			return lpad(valor, "0", tamanho);
		}
		
		if (DIREITA.equalsIgnoreCase(padSide)) {
			return rpad(valor, pad, tamanho);
		}
		
		if (ESQUERDA.equalsIgnoreCase(padSide)) {
			return lpad(valor, pad, tamanho);
		}
		
		return valor.substring(0,tamanho);
	}

	public static String lpad(String valor, String pad, int tamanho) throws ParseFileException {
		return pad(valor, pad, tamanho, ESQUERDA);
	}
	
	public static String rpad(String valor, String pad, int tamanho) throws ParseFileException {
		return pad(valor, pad, tamanho, DIREITA);
	}
	
	public static String pad(String valor, String pad, int tamanho, String direcao) throws ParseFileException{
		
		if (Valida.isEmpty(valor)) {
			valor = "";
		}
		
		if (Valida.isEmpty(pad)) {
			pad = " ";
		}
		
		if ("b".equals(pad.toLowerCase())) { pad = " "; }

		
		// e o valor for maior que o tamnho, corta a string
		if (valor.length() > tamanho) {
			return valor.substring(0,tamanho);
		}
		
		int diff = tamanho - valor.length();
		
		StringBuffer complemento = new StringBuffer(diff);
		for (int i = 0; i < diff; i++) { complemento.append(pad); }
		
		if (DIREITA.equals(direcao)) {
			return new StringBuffer(valor).append(complemento).toString();
		} else {
			return new StringBuffer(complemento.toString()).append(valor).toString();
		}
	}
	
	public static String toStrNum(String valor, int decimais) throws ParseFileException {
		
		if (valor.indexOf(",") != -1)  {
			valor = valor.replaceAll("\\,", "\\.");
		}
		
		// pega a ultima posição em caso de separador de milhar
		int posDec = valor.lastIndexOf(".");
		
		StringBuffer num = new StringBuffer();
		
		if (posDec == -1) {
			// inteiras
			num.append(valor);
			// decimais
			num.append(rpad("", "0", decimais));
		} else {
			// inteiras
			num.append(valor.substring(0,posDec).replaceAll("\\.",""));
			// decimais
			num.append(rpad(valor.substring(posDec+1), "0", decimais));
		}
		
		
		// não irá validar os valores passados 
		//try {
		//	Long.parseLong(num.toString());
		//} catch (NumberFormatException e) {
		//	throw new Exception("Problemas de Parse - toStrNum() - o numero informado para o parse não é um numero valido");
		//}
		
		return num.toString();
		
	}
	
	public static String replicate(String source, int rep) {
		if (Valida.isEmpty(source)) { return ""; }
		StringBuffer ret = new StringBuffer();
		for (int i = 0; i < rep; i++) {
			ret.append(source);
		}
		
		return ret.toString();
	}
	
	public static boolean validaBoolean(String valor, boolean def) {
    	if (!Valida.isEmpty(valor)) {
        	if ("true".equals(valor.toLowerCase())) { return true; }
        	if ("false".equals(valor.toLowerCase())) { return false; }
    	}
    	
    	return def;

	}
	
	public static StringBuffer msgErro(boolean delim, String idLayout, String idLinha, String tipoLinha, long numLinha, ColunaLayout col, int indice, StringBuffer msg) {
		return  msgErro(delim, idLayout, idLinha, tipoLinha, numLinha, col, indice, msg.toString());
	}
	
	public static StringBuffer msgErro(boolean delim, String idLayout, String idLinha, String tipoLinha, long numLinha, ColunaLayout col, int indice, String msg) {
		// Linha tipo 'id' (tipo) numero<num>: coluna posicao inicial (posini) tamanho (tam): totalizador (id) com valor diferente do esperado, informado (totalizador_informado) esperado (valor_calculado)
		StringBuffer msgBuff = new StringBuffer();

		if (delim) {
			msgBuff.append(" arquivo[ ").append((delim)?"CSV":"CNAB").append(" ]");
		}

		if (!Valida.isEmpty(idLayout)) {
			msgBuff.append(" layout[ ").append(idLayout).append(" ]");
		}
		if (!Valida.isEmpty(idLinha)) {
			msgBuff.append(" linha[ ").append(idLinha).append(" ]");
		}
		if (!Valida.isEmpty(tipoLinha)) {
			msgBuff.append(" tipo-lin[ ").append(tipoLinha).append(" ]");
		}
		
		msgBuff.append(" num-lin[ ").append(numLinha).append(" ]");
		
		if (indice >= 0) {
			msgBuff.append(" colunaIdx[").append(indice).append("]");
		}
		
		if (col != null) {
			if (!Valida.isEmpty(col.getId())) {
				msgBuff.append(" colunaId[ ").append(col.getId()).append(" ]");
			}
	
			if (!Valida.isEmpty(col.getTipo())) {
				msgBuff.append(" tipo-col[ ").append(col.getTipo()).append(" ]");
			}
			
			if ((col.getPosIni() > 0) && !delim) {
				msgBuff.append(" posini[ ").append(col.getPosIni() + 1).append(" ]");
			}
			
			if ((col.getTamanho() > 0)  && !delim) {
				msgBuff.append(" tamanho[ ").append(col.getTamanho()).append(" ]");
			}
		}

		msgBuff.append(" msg[ ").append(msg).append(" ]");
		
		return msgBuff;
		
	}
	
	public static String subString(String source, String ini, String fim) {
		
        if (Valida.isEmpty(source)) return "";
        
        int posi = source.indexOf(ini);
        
        if (posi == -1) return source;
        
        posi += ini.length();
        
        int posf = source.indexOf(fim,posi);
        
        if (posf == -1) return source.substring(posi);
        
        return source.substring(posi, posf); 
	}

	

}
