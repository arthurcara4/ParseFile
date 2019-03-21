package br.com.accesstage.parsefile.ocorrencias;

import java.io.Serializable;
import java.util.ArrayList;

public class MapBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int count = 0;
	private StringBuffer mapa = new StringBuffer();
	private ArrayList conteudo;
	
	public MapBean() {
		conteudo = new ArrayList();
	}
	
	public int setObj(String key, Object obj) {
		
		StringBuffer keyInt = new StringBuffer("|").append(key).append(":");
		
		//procura no mapa
		//int posi = mapa.indexOf(keyInt.toString());
		
		int idx = getIdx(keyInt.toString());
		
		if (idx == -1) {
			idx = count;
			keyInt.append(count++);
			
			mapa.append(keyInt.toString());
		}
		
		if (idx < conteudo.size()) {
			conteudo.set(idx, obj);
		} else {
			conteudo.add(idx, obj);
		}
		
		return idx;
	}

	public void setObj(int index, Object obj) {
		conteudo.set(index,obj);
	}


	public Object getObj(int index) {
		return conteudo.get(index);
	}

	public Object getObj(String key) {
		
		int idx = getIdx(key);
		
		if (idx == -1) {
			return null;
		}
		
		if (idx >= conteudo.size()) {
			return null;
		}
		
		return conteudo.get(idx);
	}
	
	public ArrayList getAllIndex(String subKey) {
		
		subKey = new StringBuffer("|").append(subKey).append(":").toString();
		
		int posi = mapa.indexOf(subKey);
		
		if (posi == -1) return null;
		
		ArrayList ret = new ArrayList();
		
		while (posi != -1) {
			int posf = mapa.indexOf("|", posi + 1);
			
			String item = "";
			if (posf == -1) {
				item = mapa.substring(posi + 1);
				posf = mapa.length();
			} else {
				item = mapa.substring(posi + 1, posf);
			}
			
			String[] itemToken = item.split(":");
			
			ret.add(itemToken[itemToken.length - 1]);
			
			posi = mapa.indexOf(subKey, posf);;
		}
		
		return ret;
		
		
	}
	
	public ArrayList getAllKeys(String subKey) {
		
		subKey = new StringBuffer("|").append(subKey).append(":").toString();
		
		int posi = mapa.indexOf(subKey);
		
		if (posi == -1) return null;
		
		ArrayList ret = new ArrayList();
		
		while (posi != -1) {
			int posf = mapa.indexOf("|", posi + 1);
			
			String item = "";
			
			
			if (posf == -1) {
				item = mapa.substring(posi + 1);
				posf = mapa.length();
			} else {
				item = mapa.substring(posi + 1, posf);
			}
			
			ret.add(item);
			
			posi = mapa.indexOf(subKey, posf);;
		}
		
		return ret;
	}
	
	private int getIdx(String key) {
		return getIdx(key, 0);
	}
	
	private int getIdx(String key, int posIni) {
		int idx = -1;
		
		//procura no mapa
		int posi = mapa.indexOf(key, posIni);
		
		if (posi == -1) {
			return idx;
		} else {
			
			int posf = mapa.indexOf("|", posi + 1);
			
			String item = "";
			if (posf == -1) {
				item = mapa.substring(posi + 1);
				posf = mapa.length();
			} else {
				item = mapa.substring(posi + 1, posf);
			}
			
			String[] itemToken = item.split(":");
			
			idx = Integer.parseInt(itemToken[itemToken.length - 1]);
		}
		
		return idx;
		
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public StringBuffer getMapa() {
		return mapa;
	}

	public void setMapa(StringBuffer mapa) {
		this.mapa = mapa;
	}

	public ArrayList getConteudo() {
		return conteudo;
	}

	public void setConteudo(ArrayList conteudo) {
		this.conteudo = conteudo;
	}
	
	
}
