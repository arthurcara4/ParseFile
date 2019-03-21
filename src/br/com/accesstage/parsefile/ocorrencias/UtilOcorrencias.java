package br.com.accesstage.parsefile.ocorrencias;

public class UtilOcorrencias {
	public static String getChaveComp(String tipo, String colunas) {
		return new StringBuffer((tipo == null)?"":tipo).append(":").append((colunas == null)?"":colunas).toString();
	}
}
