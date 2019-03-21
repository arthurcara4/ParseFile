package br.com.accesstage.parsefile.ocorrencias;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import br.com.accesstage.parsefile.exceptions.ParseFileException;
import br.com.accesstage.parsefile.retornos.Valores;
import br.com.accesstage.parsefile.utils.Valida;

public class Ocorrencias implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private HashMap chaves;
	private MapBean chavesTipo;

	
	public HashMap getChaves() {
		return chaves;
	}

	public void setChaves(HashMap chaves) {
		this.chaves = chaves;
	}

	public MapBean getChavesTipo() {
		return chavesTipo;
	}

	public void setChavesTipo(MapBean chavesTipo) {
		this.chavesTipo = chavesTipo;
	}

	public Ocorrencias() {
		chaves = new HashMap();
		chavesTipo = new MapBean();
	}
	
	public synchronized void addChave(String id, String tipo, String colunas) throws ParseFileException {
		if (Valida.isEmpty(id)) {
    		throw new ParseFileException("Erro de layout: <id> é um atributo obrigatório na tag <ocorrencia> do xml de layout.");
		}
		
		if (Valida.isEmpty(tipo)) {
    		throw new ParseFileException("Erro de layout: <tipo> é um atributo obrigatório na tag <ocorrencia> do xml de layout.");
		}
		
		if (Valida.isEmpty(colunas)) {
    		throw new ParseFileException("Erro de layout: <campo> é um atributo obrigatório na tag <ocorrencia> do xml de layout.");
		}
		
		
		if (chaves.get(id) != null) {
    		throw new ParseFileException("Erro de layout: ids duplicados nas ocorrencias.");
		}
		
		int idx = chavesTipo.setObj(UtilOcorrencias.getChaveComp(tipo, colunas), new HashMap());
		chaves.put(id, String.valueOf(idx));
	}
	public synchronized void soma(Valores val) throws ParseFileException {
		soma(val,val.hasException());
	}
	
	public synchronized void soma(Valores val, boolean except) throws ParseFileException {
		
		if (val == null) {
			return;
		}
		
		ArrayList colunas = chavesTipo.getAllKeys(val.getLinhaTipo());
		
		if (Valida.isEmpty(colunas)) {
			return;
		}
		
		for (int col = 0; col < colunas.size(); col++) {
			String chave = (String)colunas.get(col);
			
			String itens[] = chave.split("\\:");
			
			String[] cols = itens[1].split("\\+");
			
			if (Valida.isEmpty(cols)) return;
			
			int idx = Integer.parseInt(itens[2]);
			
			StringBuffer chaveCols = new StringBuffer();
			
			for (int c = 0; c <  cols.length; c++) {
				String coluna = cols[c];
				if (val.hasColuna(coluna)) {
					boolean sep = !Valida.isEmpty(chaveCols);
					chaveCols.append((sep)?"|":"").append(val.getString(coluna));
				}
				
			}
			
			if (Valida.isEmpty(chaveCols)) return;
			
			String chaveCont = chaveCols.toString();
				
			HashMap conteudos = (HashMap)chavesTipo.getObj(idx);
			
			if (conteudos.containsKey(chaveCont)) {
				//((Ocorrencia)conteudos.get(chaveCont)).add(val.hasException());
				((Ocorrencia)conteudos.get(chaveCont)).add(except);
			} else {
				//Ocorrencia ocor = new Ocorrencia(chaveCont,val.hasException());
				Ocorrencia ocor = new Ocorrencia(chaveCont,except);
				
				conteudos.put(chaveCont, ocor);
			}
			
			chavesTipo.setObj(idx, conteudos);
			
		}
		
	}
	
	public ArrayList getOcorrencias(String id) {
		if (!chaves.containsKey(id)) {
			return null;
		}
		
		int idx = Integer.parseInt((String)chaves.get(id));
		
		
		HashMap result = (HashMap)chavesTipo.getObj(idx);
		
		
		return new ArrayList(result.values());
	}

	public ArrayList getOcorrenciasSorted(String id) {
		
		ArrayList result = getOcorrencias(id);
		
		if (result == null) return null;
		
		
		 // Em ordem crescente do início do mandato  
		 Collections.sort (result, new Comparator() {  
		     public int compare(Object o1, Object o2) {  
		         Ocorrencia p1 = (Ocorrencia) o1;  
		         Ocorrencia p2 = (Ocorrencia) o2;  
		         return p1.getId().compareTo(p2.getId());  
		     }  
		 });
		 
		 return result;
		
	}
	
	public ArrayList getIds() {
		return new ArrayList(chaves.keySet());
	}
	

}

