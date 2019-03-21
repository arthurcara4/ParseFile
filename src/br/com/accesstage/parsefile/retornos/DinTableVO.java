package br.com.accesstage.parsefile.retornos; 

import java.io.Serializable;
import java.util.LinkedList;

import br.com.accesstage.parsefile.exceptions.ParseFileException;
import br.com.accesstage.parsefile.utils.StringUtil;
import br.com.accesstage.parsefile.utils.Valida;

public class DinTableVO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
    public LinkedList ids;
    public LinkedList valores;
    
    public DinTableVO() {
        init();
    }
    
    public DinTableVO(LinkedList ids, LinkedList valores) {
        this.ids = ids;
        this.valores = valores;
    }
    
    
    public DinTableVO(String ids) {
        init();
        
        String arrIds[] = ids.split(",");
        
        for (int pp = 0; pp < arrIds.length; pp++) {
            if (!Valida.isEmpty(arrIds[pp])) {
                addId(arrIds[pp].trim());
            }
        }
    }
    
    private void init() {
        this.ids = new LinkedList();
    }
    
    public LinkedList getIds() {
        return this.ids;
    }
    
    public LinkedList getValores() {
        return this.valores;
    }
    
    public LinkedList getValores(int lin) {
        return (LinkedList)this.valores.get(lin);
    }
    
    public void setIds(LinkedList ids) {
		this.ids = ids;
	}

	public void setValores(LinkedList valores) {
		this.valores = valores;
	}

	public int getLinhasSize() {
    	LinkedList val = getValores();
        if (Valida.isEmpty(val)) return 0;
        return val.size();
    }
    public int sizeLinhas() {
        return getLinhasSize();
    }
    
    public int getIdsSize() {
        return ids.size();
    }
    
    public int sizeIds() {
        return getIdsSize();
    }
    
    public boolean hasId(String id) {
        return (findInIds(id) != -1);
    }
    
    public int findInIds(String id) {
        if (Valida.isEmpty(ids)) return -1;
        return ids.indexOf(id);
    }
    
    public int findSubstrIds(String subId) {
        return findSubstrIds(subId, 0);
    }
    public int findSubstrIds(String subId, int start) {
        if ((Valida.isEmpty(ids)) || (start >= ids.size())) return -1;
        
        for (int pos = start; pos < ids.size(); pos++) {
            if (((String)ids.get(pos)).indexOf(subId, start) != -1) {
                return pos;
            }
            
        }  
        return -1;
    }
    
    
    public void addId(String id) {
        if (findInIds(id) == -1) {
            ids.add(id);
        }
    }
    
    /*public void addValor(String id, Object valor) {
    	
    	int lin = 0;
        if (Valida.isEmpty(valores)) {
            valores = new LinkedList();
            lin = 1;
        } else {
        	lin = valores.size();
        }
        
        setValor(lin, findInIds(id), valor);
    }*/
    
    
    public void setValor(int lin, String id, Object valor) {
        
        setValor(lin, findInIds(id), valor);
    }
    public void setValor(int lin, int idx, Object valor) {
        
        if (Valida.isEmpty(valores)) {
            valores = new LinkedList();
        }
        
        while ((valores.size() - 1) < lin) {
            valores.add(new LinkedList());
        }
        
        if (idx == -1) return;
        
        LinkedList val = (LinkedList)valores.get(lin);
        
        while ((val.size() - 1) < idx) {
            val.add("");
        }
        val.set(idx, valor);
        
        valores.set(lin, val);
    }
    
    
    public Object get(int lin, String id) {
        return get(lin, findInIds(id));
    }
    
    public Object get(int lin, int idx) {
        
        if ((Valida.isEmpty(valores)) || (valores.size() <= lin)) return "";
        
        
        if (idx == -1) return "";
        
        LinkedList val = (LinkedList)valores.get(lin);
        
        if (val.size() <= idx) return "";
        
        return val.get(idx);
    }
    
	public DinVO getDinVO(int lin, String id) {
        return getDinVO(lin, findInIds(id));
    }
	
	public DinVO getDinVO(int lin, int idx) {
		if (idx == -1) return null;
		return (DinVO)get(lin, idx);
	}
	
	public DinTableVO getDinTableVO(int lin, String id) {
        return getDinTableVO(lin, findInIds(id));
    }
	
	public DinTableVO getDinTableVO(int lin, int idx) {
		if (idx == -1) return null;
		return (DinTableVO)get(lin, idx);
	}
    
    public DinVO toDinVO(int lin) {
    	if (lin > (valores.size() - 1)) return null;
    	
    	return new DinVO((LinkedList)ids.clone(), (LinkedList)((LinkedList)valores.get(lin)).clone());
    }

    
    public String getStringConcat(int lin, String ids) {
    	String arrIds[] = ids.split("\\,"); 
    	
    	StringBuffer ret = new StringBuffer();
    	for (int idx = 0; idx < arrIds.length; idx++) {
        	ret.append(getString(lin, arrIds[idx].trim()));
    		
    	}
        return ret.toString();
    }
    
	public String getStringPadEsq(int lin, String id, int tam, String pad) {
        return StringUtil.pad(getString(lin, findInIds(id)), tam, pad, StringUtil.PAD_ESQ);
    }
	
	public String getStringPadEsq(int lin, int idx, int tam, String pad) {
        return StringUtil.pad(getString(lin, idx), tam, pad, StringUtil.PAD_ESQ);
    }
	
	public String getStringPadDir(int lin, String id, int tam, String pad) {
        return StringUtil.pad(getString(lin, findInIds(id)), tam, pad, StringUtil.PAD_DIR);
    }
	
	public String getStringPadDir(int lin, int idx, int tam, String pad) {
        return StringUtil.pad(getString(lin, idx), tam, pad, StringUtil.PAD_DIR);
    }
    
    public String getString(int lin, String id) {
        return getString(lin, findInIds(id));
    }
    public String getString(int lin, int idx) {
        return String.valueOf(get(lin, idx));
    }
    
	public String[] getSplit(int lin, String id, String charSplit) {
        return getSplit(lin, findInIds(id), charSplit);
    }
	
	public String[] getSplit(int lin, int idx, String charSplit) {
        return getString(lin, idx).split(charSplit);
    }    
    
    public long getLong(int lin, String id) {
        return getLong(lin, findInIds(id));
    }
    public long getLong(int lin, int idx) {
        try {
            return Long.parseLong(getString(lin, idx));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
	public double getDouble(int lin, String id, int divisor) throws ParseFileException {
		return getDouble(lin, findInIds(id), divisor);
	}
	
	public double getDouble(int lin, int idx, int divisor) throws ParseFileException {
        try {
        	return (Double.parseDouble((getString(lin, idx).trim())) / divisor);
        } catch (NumberFormatException e) {
            return -1;
        }
	}
    
    
    public int getInt(int lin, String id) {
        return getInt(lin, findInIds(id));
    }
    public int getInt(int lin, int idx) {
        try {
            return Integer.parseInt(getString(lin, idx));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    public String getValor(int lin, String id,  int decimais) {
        return getValor(lin, findInIds(id), decimais);
    }
    public String getValor(int lin, int idx,  int decimais) {
		String value = String.valueOf(getLong(lin, idx));
		
		return StringUtil.fmtValor(value, decimais);

		/*
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
        */
        
    }
    
	public String getData(int lin, String id, String formato) {
        return getData(lin, findInIds(id), formato, "dd/MM/yyyy");
    }
	public String getData(int lin, int idx, String formato) {
        return getData(lin, idx, formato, "dd/MM/yyyy");
    }
	public String getData(int lin, String id, String formato, String formatoOut) {
        return getData(lin, findInIds(id), formato, formatoOut);
    }
	public String getData(int lin, int idx, String formato, String formatoOut) {
		return StringUtil.fmtData(getString(lin,idx), formato, formatoOut);
		/*
		try {
            SimpleDateFormat sdf = new SimpleDateFormat(formato);
            Date value = sdf.parse(getString(lin, idx));
            
			return new SimpleDateFormat(formatoOut).format(value);
		} catch (NumberFormatException e) {
			return "";
		} catch (ParseException pe) {
            return "";
        }
        */
	}
	
	public String getCnpj(int lin, String id) {
		return getCnpj(lin, findInIds(id));
	}
	public String getCnpj(int lin, int idx) {
		return StringUtil.fmtCnpj(getString(lin,idx));
		/*
		try {
			
			String c = getString(lin, idx).trim();
			if (c.length() < 14) {
				c = new StringBuffer(rpl("0",14 - c.length())).append(c).toString();
			}
			
			if (c.length() > 14) {
				c = c.substring(0, 14);
			}
            
			StringBuffer result = new StringBuffer();
			result.append(c.substring(0, 3)).append(".");
			result.append(c.substring(3, 6)).append(".");
			result.append(c.substring(6, 9)).append("/");
			result.append(c.substring(9, 13)).append("-");
			result.append(c.substring(13));
			
			return result.toString();
		} catch (Exception e) {
			return "";
        }
        */
	}
	
	public String getCpf(int lin, String id) {
		return getCpf(lin, findInIds(id));
	}

	public String getCpf(int lin, int idx) {
		return StringUtil.fmtCpf(getString(lin,idx));
		/*
		try {
			
			String c = getString(lin, idx).trim();
			if (c.length() < 11) {
				c = new StringBuffer(rpl("0",11 - c.length())).append(c).toString();
			}
			
			if (c.length() > 11) {
				c = c.substring(0, 11);
			}
            
			StringBuffer result = new StringBuffer();
			result.append(c.substring(0, 4)).append(".");
			result.append(c.substring(4, 7)).append(".");
			result.append(c.substring(7, 10)).append("-");
			result.append(c.substring(10));
			
			return result.toString();
		} catch (Exception e) {
			return "";
        }
        */
	}
	
	public String getCpfCnpj(int lin, String id, int tipo) {
		return getCpfCnpj(lin, findInIds(id), tipo);
	}
	
	public String getCpfCnpj(int lin, int idx, int tipo) {
		if (tipo == 1) return getCpf(lin, idx);
		
		return getCnpj(lin, idx);
	}
	
	
    public String toString() {
    	
    	StringBuffer ret = new StringBuffer("DinTableVO[");
    	
    	
    	if (!Valida.isEmpty(ids)) {
    		ret.append("{");
        	for (int i = 0; i < ids.size(); i++) {
        		if (i > 0) ret.append(";");
        		
        		ret.append(ids.get(i));
        	}
    		ret.append("}");
    	}
    	
    	if (!Valida.isEmpty(valores)) {
        	for (int i = 0; i < valores.size(); i++) {
        		LinkedList lin =  (LinkedList)valores.get(i);
        		
        		if (!Valida.isEmpty(lin)) {
        			ret.append("|");
                	for (int j = 0; j < lin.size(); j++) {
                		
                		if (j > 0) ret.append(";");
                		
                		ret.append(lin.get(j));
                		
                	}
        		
        		}
        	}
    	}
    	
    	return ret.append("]").toString();
    }
    
    public Object clone() {
    	return new DinTableVO((LinkedList)ids.clone(), (LinkedList)valores.clone());
    }
    
} 
