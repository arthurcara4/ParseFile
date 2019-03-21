package br.com.accesstage.parsefile.retornos; 

import java.io.Serializable;
import java.util.LinkedList;

import br.com.accesstage.parsefile.exceptions.ParseFileException;
import br.com.accesstage.parsefile.utils.StringUtil;
import br.com.accesstage.parsefile.utils.Valida;

public class DinVO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private LinkedList ids;
	private LinkedList values;
	
	public DinVO() {
		ids = new LinkedList();
		values = new LinkedList();
	}
	
	public DinVO(LinkedList ids, LinkedList values) {
		this.ids = ids;
		this.values = values;
	}
	
	public DinVO(String values) {
		this.ids = new LinkedList();
		this.values = new LinkedList();
		
		strToDinVO(values);
	}
	
	private void strToDinVO(String values) {
		
		if (Valida.isEmpty(values)) return;
		
		values = values.replaceAll("DinVO", "");
		values = values.replaceAll("\\[", "");
		values = values.replaceAll("\\]", "");
		
		if (Valida.isEmpty(values)) return;
		
	    String itens[] = values.split("\\|");
	    
	    for (int it = 0; it < itens.length; it++) {
	        String item[] = itens[it].split("\\:");
	        
	        if (item.length > 0) {
	        	this.addset(item[0], ((item.length > 1)?item[1]:null) );
	        }
	    }
	}
	
	public void setIds(LinkedList ids) {
		this.ids = ids;
	}

	public void setValues(LinkedList values) {
		this.values = values;
	}

	public void remove(String id) {
		int idx = findInIds(id);
		if (idx == -1) return;
		remove(idx);
	}
	
	public void remove(int idx) {
		this.ids.remove(idx);
		this.values.remove(idx);
	}
	
	public void add(String id) {
		add(id, null);
	}
	
	public void add(String id, Object value) {
		if (Valida.isEmpty(id) || (ids.indexOf(id) != -1)) return;
		
		ids.add(id);
		values.add(value);
	}
	
	public void add(String id, int value) {
		add(id, String.valueOf(value));
	}
	
	public void add(String id, long value) {
		add(id, String.valueOf(value));
	}
	
	public void add(String id, double value) {
		add(id, String.valueOf(value));
	}
	
	public void add(String id, boolean value) {
		add(id, (value)?"1":"0" );
	}
	
    public void add(DinVO source) {
        if ((source == null) || (source.size() == 0)) return;
        
        for (int id = 0; id < source.size(); id++) {
        	add(source.getId(id), source.get(id));
        }
    }
	
	public void set(String id, int value) {
		set(id, String.valueOf(value));
	}
	
	public void set(String id, long value) {
		set(id, String.valueOf(value));
	}
	
	public void set(String id, double value) {
		set(id, String.valueOf(value));
	}
	
	public void set(String id, boolean value) {
		set(id, (value)?"1":"0" );
	}
	
    public void set(String id, Object value) {
    	if (findInIds(id) == -1) {
    		add(id, value);
    		return;
    	}
    	set(findInIds(id),value);
    }
    
    
    
    public void set(int idx, Object value) {
        if ((idx == -1) || (Valida.isEmpty(values)) || (idx > (values.size() -1))) return;
        
        values.set(idx,value);
    }
    
    
    
	public void addset(String id) {
		addset(id, null);
	}
	
	public void addset(String id, Object value) {
		if (Valida.isEmpty(id)) return;
		
		if (ids.indexOf(id) != -1) {
			set(id,value);
		} else {
			add(id,value);
		}
	}
	
	public void addset(String id, int value) {
		addset(id, String.valueOf(value));
	}
	
	public void addset(String id, long value) {
		addset(id, String.valueOf(value));
	}
	
	public void addset(String id, double value) {
		addset(id, String.valueOf(value));
	}
	
	public void addset(String id, boolean value) {
		addset(id, (value)?"1":"0" );
	}
	
    public void addset(DinVO source) {
        if ((source == null) || (source.size() == 0)) return;
        
        for (int id = 0; id < source.size(); id++) {
        	addset(source.getId(id), source.get(id));
        }
    }
    
    
	public Object get(String id) {
        return get(findInIds(id));
    }
	
	public Object get(int idx) {
		if (idx == -1) return null;
		return values.get(idx);
	}
	
	public DinVO getDinVO(String id) {
        return getDinVO(findInIds(id));
    }
	
	public DinVO getDinVO(int idx) {
		if (idx == -1) return null;
		return (DinVO)get(idx);
	}
	
	public DinTableVO getDinTableVO(String id) {
        return getDinTableVO(findInIds(id));
    }
	
	public DinTableVO getDinTableVO(int idx) {
		if (idx == -1) return null;
		return (DinTableVO)get(idx);
	}
	
    public String getStringConcat(String ids) {
    	String arrIds[] = ids.split("\\,"); 
    	
    	StringBuffer ret = new StringBuffer();
    	for (int idx = 0; idx < arrIds.length; idx++) {
        	ret.append(getString(arrIds[idx].trim()));
    		
    	}
        return ret.toString();
    }
    
	public String getStringPadEsq(String id, int tam, String pad) {
        return StringUtil.pad(getString(findInIds(id)), tam, pad, StringUtil.PAD_ESQ);
    }
	
	public String getStringPadEsq(int idx, int tam, String pad) {
        return StringUtil.pad(getString(idx), tam, pad, StringUtil.PAD_ESQ);
    }
	
	public String getStringPadDir(String id, int tam, String pad) {
        return StringUtil.pad(getString(findInIds(id)), tam, pad, StringUtil.PAD_DIR);
    }
	
	public String getStringPadDir(int idx, int tam, String pad) {
        return StringUtil.pad(getString(idx), tam, pad, StringUtil.PAD_DIR);
    }
    

	public String getString(String id) {
        return getString(findInIds(id));
    }

	
	public String getString(int idx) {
		try {
			if (get(idx) == null) return "";
			
			return String.valueOf(get(idx));
		} catch (Exception e) {
			return "";
		}
	}
	
	public String[] getSplit(String id, String charSplit) {
        return getSplit(findInIds(id), charSplit);
    }
	public String[] getSplit(int idx, String charSplit) {
        return getString(idx).split(charSplit);
    }
	

	public int getInt(String id) {
        return getInt(findInIds(id));
    }
	public int getInt(int idx) {
		try {
			return Integer.parseInt(getString(idx));
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	
	public long getLong(String id) {
        return getLong(findInIds(id));
    }
	public long getLong(int idx) {
		try {
			return Long.parseLong(getString(idx));
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	
	public double getDouble(String id, int divisor) throws ParseFileException {
		return getDouble(findInIds(id), divisor);
	}
	
	public double getDouble(int idx, int divisor) throws ParseFileException {
		try {
			return (Double.parseDouble((getString(idx).trim())) / divisor);
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	
	public boolean getBoolean(String id) throws ParseFileException {
		return getBoolean(findInIds(id));
	}
	
	public boolean getBoolean(int idx) throws ParseFileException {
		return ("1".equals(getString(idx).trim())) ;
	}

	
	public LinkedList getList(String id) {
        return getList(findInIds(id));
    }
	public LinkedList getList(int idx) {
		try {
			if (get(idx) == null) return null;
			
			return (LinkedList)get(idx);
		} catch (NumberFormatException e) {
			return null;
		}
	}
    
    public String getValor(String id,  int decimais) {
        return getValor(findInIds(id), decimais);
    }
    public String getValor(int idx,  int decimais) {
		String value = String.valueOf(getLong(idx));
		
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
	public String getData(String id, String formato) {
        return getData(findInIds(id), formato, "dd/MM/yyyy");
    }
	public String getData(int idx, String formato) {
        return getData(idx, formato, "dd/MM/yyyy");
    }
	public String getData(String id, String formato, String formatoOut) {
        return getData(findInIds(id), formato, formatoOut);
    }
	public String getData(int idx, String formato, String formatoOut) {
		
		return StringUtil.fmtData(getString(idx), formato, formatoOut);
		/*
		try {
            SimpleDateFormat sdf = new SimpleDateFormat(formato);
            Date value = sdf.parse(getString(idx));
            
			return new SimpleDateFormat(formatoOut).format(value);
		} catch (NumberFormatException e) {
			return "";
		} catch (ParseException pe) {
            return "";
        }
        */
	}
	
	public String getCnpj(String id) {
		return getCnpj(findInIds(id));
	}
	public String getCnpj(int idx) {
		return StringUtil.fmtCnpj(getString(idx));
		/*
		try {
			
			String c = getString(idx).trim();
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
        */
	}
	
	public String getCpf(String id) {
		return getCpf(findInIds(id));
	}

	public String getCpf(int idx) {
		return StringUtil.fmtCpf(getString(idx));
		/*
		try {
			
			String c = getString(idx).trim();
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
        */
	}
	
	public String getCpfCnpj(String id, int tipo) {
		return getCpfCnpj(findInIds(id), tipo);
	}
	
	public String getCpfCnpj(int idx, int tipo) {
		if (tipo == 1) return getCpf(idx);
		
		return getCnpj(idx);
	}
	
	
    public String getId(int idx) {
        return String.valueOf(ids.get(idx));
    }
    
    public void setId(int idx, String newId) {
        ids.set(idx, newId);
    }

	
	public LinkedList getIds() {
		return ids;
	}

	public LinkedList getValues() {
		return values;
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
            if (((String)ids.get(pos)).indexOf(subId) != -1) {
                return pos;
            }
            
        }  
        return -1;
    }
    
    
    public int findInValues(Object value) {
        if ((Valida.isEmpty(values)) || (Valida.isEmpty(value))) return -1;
        return values.indexOf(value);
    }
    
    public int findInValuesByFilter(Object value, DinVOFilter filter) {
        if ((Valida.isEmpty(values)) || (Valida.isEmpty(value))) return -1;
        
        for (int i = 0; i < values.size(); i++) {
        	if (filter.compare(value, values.get(i))) {
        		return i;
        	}
        }
        
        return -1;
    }
    
    public int size() {
        if (Valida.isEmpty(ids)) return 0;
        return ids.size();
    }
    
    public void clear() {
        if (this.ids != null) this.ids.clear();
        if (this.values != null) this.values.clear();
    }
    
    public String toString() {
    	
    	StringBuffer ret = new StringBuffer("DinVO[");
    	
    	int size = (Valida.isEmpty(ids)?0:ids.size());
    	
    	if (size == 0) size = (Valida.isEmpty(values)?0:values.size());
    	
    	if (size == 0) return "vazio";
    	
    	for (int i = 0; i < size; i++) {
    		Object id = ((Valida.isEmpty(ids) || (i > (ids.size() - 1)))?"":ids.get(i)); 
    		Object value = ((Valida.isEmpty(values) || (i > (values.size() - 1)))?"":values.get(i)); 
    		
    		if (i > 0) ret.append("|");
    		
    		ret.append(id).append(":").append(value);
    	}
    	
    	return ret.append("]").toString();
    }
    
    public Object clone() {
    	return new DinVO((LinkedList)ids.clone(), (LinkedList)values.clone());
    }
    
}
