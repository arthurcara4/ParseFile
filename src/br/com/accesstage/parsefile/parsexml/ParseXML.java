package br.com.accesstage.parsefile.parsexml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ParseXML {
	
	public static final int T_ARRAY = 1;
	public static final int T_LIST = 2;
	public static final int T_MAP = 3;
	public static final int T_OBJECT = 4;
	public static final int T_DATE = 5;
	
	//public static final String T_FIELD = "field";
	
	public static final String TRANS_SIMPLEDATE = "simpleDate"; 
	public static final String TRANS_OBJECT = "object"; 
	int modifierPublicStaticFinal = (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL); // 25
	
    private Transformer transformerXml = null;
    //private StreamResult streamXml = null;
    private DOMSource domSource = null;
    
    private DocumentBuilderFactory docBuilderFactory = null;
    private DocumentBuilder docBuilder = null;


    public ParseXML() throws Exception {
        init();
    }
    
    private void init() throws Exception {
        this.transformerXml = TransformerFactory.newInstance().newTransformer();  
        //this.streamXml = new StreamResult( new StringWriter());  
        this.domSource = new DOMSource();  
        
        this.docBuilderFactory = DocumentBuilderFactory.newInstance();
        this.docBuilder = docBuilderFactory.newDocumentBuilder();
    }
	
	public String objToXml(Object obj) throws Exception {
		
    	StringBuffer xml = new StringBuffer();
		Class c = obj.getClass(); 
		
		xml.append("<o cl=\"");
		xml.append(c.getName());
		xml.append("\">");
		
		if (obj == null) {
			xml.append("_null_</o>");
			return xml.toString();
		}
		
		int tClass = tipoClass(c);
		
		// arrays: Object[]
		if (T_ARRAY == tClass) {
			
			Object[] objects = (Object[])obj;

			xml.append("<e>");
			for (int iObj = 0; iObj < objects.length; iObj++) {
				xml.append(this.objToXml(objects[iObj]));
			}
			xml.append("</e>");
		}
		
		// listas: ArrayList, LinkedList, etc
		if (T_LIST == tClass) {
			
			int size = ((Integer)c.getDeclaredMethod("size", new Class[] {}).invoke(obj ,new Object[] {})).intValue();
			xml.append("<il>");
			for (int i = 0; i < size; i++) {
				xml.append(this.objToXml(c.getDeclaredMethod("get", new Class[] {int.class}).invoke(obj ,new Object[] {new Integer(i)})));
			}
			xml.append("</il>");
		}
		
		// Maps: HashTable, HashMap, etc.
		if (T_MAP == tClass) {
			
			Set keys = (Set)c.getDeclaredMethod("keySet", new Class[] {}).invoke(obj ,new Object[] {});
			
			
			if ((keys != null) && (keys.size() > 0 )) {
				Object[] keysObj = keys.toArray();
				
				xml.append("<il>");
				for (int ikey = 0; ikey < keysObj.length; ikey++) {
					xml.append("<i>");
					xml.append("<k>");
					xml.append(this.objToXml(keysObj[ikey]));
					xml.append("</k>");
				
					xml.append("<c>");
					xml.append(this.objToXml(c.getDeclaredMethod("get", new Class[] {Object.class}).invoke(obj ,new Object[] {keysObj[ikey]})));
					xml.append("</c>");
					xml.append("</i>");
				}
				xml.append("</il>");
			}
		}
		
		// listas: ArrayList, LinkedList, etc
		if (T_DATE == tClass) {
			long time = 0;
			try {
				time = ((Long)c.getMethod("getTimeInMillis", new Class[] {}).invoke(obj ,new Object[] {})).longValue();
			} catch (Exception e) {
				try {
					time = ((Long)c.getDeclaredMethod("getTime", new Class[] {}).invoke(obj ,new Object[] {})).longValue();
				} catch (Exception ex) {}
			}
			
			if (time > 0) { 
				xml.append(time);
			}
		}
		
			
		// tipos normais	
		if (T_OBJECT == tClass) {
			StringBuffer xmlFields = new StringBuffer(); 
			Field[] flds = c.getDeclaredFields();
			
			boolean hasPublicFields = false;
			for (int f = 0; f < flds.length; f++) {
				Field fld = flds[f];
				if (fld.getModifiers() != (modifierPublicStaticFinal)) {
					
					try {
						Method get = getMet(c,fld);
						
						if (get != null) {
							Object value = get.invoke(obj, new Object[] {});
							if (value != null) {
								hasPublicFields = true;
								
								String xmlObj = objToXml(value);
								
								if (xmlObj != null) {
									xmlFields.append("<f n=\"").append(fld.getName()).append("\">");
									xmlFields.append(xmlObj);
									xmlFields.append("</f>");
								}
							}
							
							
						}
					} catch (Exception e) {
						
					}
				}
			}
			
			if (hasPublicFields) {
				xml.append("<fl>");
				xml.append(xmlFields.toString());
				xml.append("</fl>");
			} else {
				
				if ((Boolean.class.equals(obj.getClass())) && (obj.toString().equals("false"))) {
					return null;
				}
				
				if ( (Integer.class.equals(obj.getClass()) || Long.class.equals(obj.getClass())) && (obj.toString().equals("0"))) {
					return null;
				}
				
				String campo = String.valueOf(obj);
				
				if ((String.class.equals(obj.getClass())) || (StringBuffer.class.equals(obj.getClass()))){
					campo = new StringBuffer("<![CDATA[").append(obj).append("]]>").toString();
				}
				
				xmlFields.append(campo);
				
				xml.append(xmlFields.toString());
			}
		}
		
		xml.append("</o>");
		
		return xml.toString();
		
		
	}
	
	
	private Method getMet(Class c, Field fld) throws Exception {
		try {
			return c.getDeclaredMethod(getMet(fld.getName(), "get") , new Class[] {});
		} catch (Exception e) {
			if ("boolean".equals(fld.getType().getName())) {
				return c.getDeclaredMethod(getMet(fld.getName(), "is") , new Class[] {});
			}
		}
		
		return null;
	}
	
	private String getMet(String field, String type ) {
		return new StringBuffer(type).append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).toString();
	}
	
	
	public Object xmlToObj(String xml) throws Exception {
		
		xml = new StringBuffer("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><root>").append(xml).append("</root>").toString();
		
		InputStream xmlStream = new ByteArrayInputStream(xml.getBytes());
        
        Document doc = docBuilder.parse (xmlStream);
        
        doc.getDocumentElement().normalize();
        
        NodeList objects = doc.getElementsByTagName("root").item(0).getChildNodes();
        
//        for(int ob = 0; ob < objects.getLength(); ob++) {
        	//Object objNull = (Element)objects.item(ob);
        	Object objNull = (Element)objects.item(0);
        	if(! (objNull instanceof Element)){
        		System.out.println("Encontrado objeto de classe " + objNull.getClass());
        	}
        	
        	Element objectEle = (Element)objects.item(0);
        	
        	return(this.getObject(objectEle));
        	
//        }
        
        //return null;
	}
	
	private Object getObject(Element objectEle) throws Exception{
		
		Class c = Class.forName(objectEle.getAttribute("cl"));
		
		Object ret = null;
		try {
			ret = c.newInstance();
		} catch (Exception e) {}
	
    	NodeList fields = objectEle.getChildNodes();
    	
    	int tClass = tipoClass(c);
    	
        for(int f = 0; f < fields.getLength(); f++) {
        	
        	Element fieldEle = null;
        	try {
	        	fieldEle = (Element)fields.item(f);
        	} catch (Exception e) {
				String valor = fields.item(f).getNodeValue();
				
				valor = valor.replaceAll("\\<\\!\\[CDATA\\[","");
				valor = valor.replaceAll("\\]\\]\\>","");
				
				if (T_DATE == tClass) {
					
					Long time = new Long(valor);
					try {
						c.getMethod("setTimeInMillis", new Class[] {long.class}).invoke(ret ,new Object[] {time});
					} catch (Exception ex) {
						try {
							c.getDeclaredMethod("setTime", new Class[] {long.class}).invoke(ret ,new Object[] {time});
						} catch (Exception exd) {}
					}
					
					return ret;
				}
				
				Object obj = null;
				try {
					obj = c.getConstructor(new Class[] {String.class}).newInstance(new Object[] {valor});
				} catch (Exception ex) {}
				
				return obj;
			}
        	
    		// arrays: Object[]
    		if (T_ARRAY == tClass) {
    			NodeList itens = fieldEle.getChildNodes();
    			
    			for (int iitem = 0; iitem < itens.getLength(); iitem++) {
    				Element objEle = (Element)itens.item(iitem);
    				
                	Object obj = this.xmlToObj(toXmlString(objEle));
                	
                	// instancia o array com a quantidade de elementos na primeira vez que executar
                	if (iitem == 0) {
                		ret = Array.newInstance(obj.getClass(), itens.getLength());
                	}
                	
                	Array.set(ret, iitem, obj); 
    			}
    		}
        	

    		// listas: ArrayList, LinkedList, etc
    		if (T_LIST == tClass) {
        		Method add = c.getDeclaredMethod("add", new Class[] { Object.class }); 
        		
    			NodeList itens = fieldEle.getChildNodes();
    			
    			for (int iitem = 0; iitem < itens.getLength(); iitem++) {
    				Element objEle = (Element)itens.item(iitem);
    				
            		add.invoke(ret, new Object[] { this.xmlToObj(toXmlString(objEle)) } );
    			}
        		
        		
    		}

    		// Maps: HashTable, HashMap, etc.
    		if (T_MAP == tClass) {
        		synchronized (this) {
					
	    			Method put = c.getDeclaredMethod("put", new Class[] { Object.class, Object.class });
	    			
	    			NodeList itens = fieldEle.getChildNodes();
	    			
	    			for (int iitem = 0; iitem < itens.getLength(); iitem++) {
	    				Element item = (Element)itens.item(iitem);
	    				
	    				Element keyEle = (Element)item.getChildNodes().item(0).getChildNodes().item(0);
	    				Element contentEle = (Element)item.getChildNodes().item(1).getChildNodes().item(0);
	    				
	                	Object key = this.xmlToObj(toXmlString(keyEle));
	                	Object content = this.xmlToObj(toXmlString(contentEle));
	                	
	                	put.invoke(ret, new Object[] { key, content } );
	    				
	    			}
    			
        		}
    			
    		}
    		
    		// para objetos somente irá trafegar os atributos que possuam gets e sets publicos para atributos de uma classe
    		if (T_OBJECT == tClass) {
    			
    			NodeList atribs = fieldEle.getChildNodes();
    			
    			if ((atribs == null) || (atribs.getLength() == 0)) {
    				return ret;
    			}
    			
    			for (int iat = 0; iat < atribs.getLength(); iat++) {
    				Element item = (Element)atribs.item(iat);
    				
    				String name = item.getAttribute("n");
    				
    				Element atribEle = (Element)item.getChildNodes().item(0);
    				
                	Object atrib = this.xmlToObj(toXmlString(atribEle));
                	
                	if (atrib != null) {
                		
                		if (ret == null) {
                			throw new Exception("ParseXML - xmlToObj():getObject(): A classe " + c.getName() + " não possui o construtor " + c.getName() + "(). Não foi possivel efetuar a conversão do objeto.");
                		}
	                	Method set = translateMethod(c, name, atrib);
	                	
	                	if (set != null) {
	                		set.invoke(ret, new Object[] { atrib });
	                	}
                	}
    			}
        	}
            	
        	//} catch (Exception e) { }
        }
        
        return ret;
	}
	
	
	//private Method translateMethod(Class c, String name, String classe, boolean array) throws Exception {
	private Method translateMethod(Class c, String name, Object obj) throws Exception {
		try {
			return c.getDeclaredMethod(getMet(name,"set"), new Class[] { obj.getClass()});
			
		} catch (Exception e) {
			
			if (Integer.class.equals(obj.getClass())) {
				return c.getDeclaredMethod(getMet(name,"set"), new Class[] { int.class });
			}
			if (Boolean.class.equals(obj.getClass())) {
				return c.getDeclaredMethod(getMet(name,"set"), new Class[] { boolean.class });
			}
			if (Character.class.equals(obj.getClass())) {
				return c.getDeclaredMethod(getMet(name,"set"), new Class[] { char.class });
			}
			if (Byte.class.equals(obj.getClass())) {
				return c.getDeclaredMethod(getMet(name,"set"), new Class[] { byte.class });
			}
			if (Short.class.equals(obj.getClass())) {
				return c.getDeclaredMethod(getMet(name,"set"), new Class[] { short.class });
			}
			if (Long.class.equals(obj.getClass())) {
				return c.getDeclaredMethod(getMet(name,"set"), new Class[] { long.class });
			}
			if (Float.class.equals(obj.getClass())) {
				return c.getDeclaredMethod(getMet(name,"set"), new Class[] { float.class });
			}
			if (Double.class.equals(obj.getClass())) {
				return c.getDeclaredMethod(getMet(name,"set"), new Class[] { double.class });
			}
		}
		
		return null;
	}
	
	/*
	private Object setFieldXml(String name, String value, String classe, String transf, Node node) throws Exception
	{
    	
    	if (TRANS_OBJECT.equals(transf)) {
    		return this.xmlToObj(toXmlString(node));
    	}
        
        if (value == null) {
            return null;
        }
        
		Class fieldC = Class.forName(classe);
		
    	if ((transf == null) || ("".equals(transf))) {
    		Constructor cons = fieldC.getConstructor(new Class[] { java.lang.String.class } );
    	    return  cons.newInstance(new Object[]{ value });
    	}
    	
    	if (TRANS_SIMPLEDATE.equals(transf)) {
    		Method m = fieldC.getDeclaredMethod("setTime", new Class[]{ long.class });
    		
    		Object date = fieldC.newInstance();
    		
    		m.invoke(date, new Object[] { new Long(value) });
    		
    		return date;
    		
    	}
    	
    	return null;
		
		
	}
	
	private Object setArrayXml(String name, String value, String classe) throws Exception {
		
		// split em array de strings
		String[] valores = value.split("\\|");
		int size = valores.length;
		
		Class fieldC = Class.forName(classe);
		
		// cria um ArrayList com a classe e o conteúdo real do array 
		ArrayList arr = new ArrayList();
		for (int i = 0; i < valores.length; i++) {
		    Constructor cons = fieldC.getConstructor(new Class[] { java.lang.String.class } );
		    arr.add(cons.newInstance(new Object[]{ valores[i] }));
		}
		
		// efetua um reflexion para um objeto array real
	    Object arrayField = Array.newInstance(fieldC, size);
	    System.arraycopy(arr.toArray(), 0, arrayField, 0, Math.min(Array.getLength(arr.toArray()), size));
		
	    return arrayField;
	}
	
	private Object setListXml(String name, NodeList items, String classe) throws Exception {
		
		Class c = Class.forName(classe);
		
		Object list = c.newInstance();
		
		//ArrayList list = new ArrayList();
		
		for (int i = 0; i < items.getLength(); i++) {
        	Element itemdEle = (Element)items.item(i);
        	
        	Object itemObj =  this.xmlToObj(toXmlString(itemdEle));
        	
        	c.getDeclaredMethod("add", new Class[] {Object.class}).invoke(list ,new Object[] { itemObj });
        	//list.add(itemObj);
		}
		
		return list;
	}
	
	
    public String fieldToXml(String name, Object obj) throws Exception {
    	if (obj == null) return "";
    	
    	String classe = obj.getClass().getName();
    	
		int tClass = tipoClass(obj.getClass());
    	
    	//int pos = type.indexOf("[L");
    	//if (pos != -1) {
    	//	type = type.substring(pos + 2).replaceAll("\\;", "");
    	//	return arrayXml(name, obj, type);
    	//}
    	
		// listas: ArrayList, LinkedList, etc
		if (T_LIST == tClass) {
    		return listXml(name, obj, classe);
    	}
    	
    	return fieldXml(name, obj, classe);
    	//return null;
    }
    
    private String tagField(String name, String type, String classe, String transform) {
    	StringBuffer xml = new StringBuffer();
    	xml.append("<field name=\"").append(name).append("\" ");
    	xml.append("type=\"").append(type).append("\" ");
    	if (transform != null) { 
	    	xml.append("transform=\"").append(transform).append("\" ");
    	}
    	xml.append("class=\"").append(classe).append("\" >");
    	return xml.toString();
    }
    
    private String fieldXml(String name, Object value, String classe) throws Exception{
    	if (value == null) return "";
    	
    	String transf = tranformation(classe);
    	
    	StringBuffer xml = new StringBuffer();
    	//xml.append(tagField(name, T_FIELD, classe, transf));
    	
    	if (transf == null) {
    		xml.append(String.valueOf(value));
    	}
    	
    	if (transf == TRANS_SIMPLEDATE) {
    		Method m = value.getClass().getDeclaredMethod("getTime", new Class[]{});
    		xml.append(String.valueOf(m.invoke(value, new Object[]{})));
    	}
    	
    	if (transf == TRANS_OBJECT) {
    		xml.append(this.objToXml(value));
    	}
    	
    	xml.append("</field>");
    	
    	return xml.toString();
    }
    
    private String tranformation(String classe) {
    	
		if ( (Integer.class.getName().equals(classe)) 
				|| (String.class.getName().equals(classe)) 
				|| (Boolean.class.getName().equals(classe)) 
				|| (Character.class.getName().equals(classe)) 
				|| (Byte.class.getName().equals(classe)) 
				|| (Short.class.getName().equals(classe)) 
				|| (Long.class.getName().equals(classe))
				|| (Float.class.getName().equals(classe))
				|| (Double.class.getName().equals(classe)) ) {
			return null;
		}
		
		if ( (java.sql.Date.class.getName().equals(classe))
			|| (java.util.Date.class.getName().equals(classe)) ) {
			return TRANS_SIMPLEDATE;
		}
		
		return TRANS_OBJECT;
			
    }
    
    private String arrayXml(String name, Object values, String classe) {
    	if (values == null) return "";
    	
    	String[] array = (String[])values;
    	
    	StringBuffer xml = new StringBuffer();
    	//xml.append(tagField(name, T_ARRAY, classe, null));
    	
    	for (int i = 0; i < array.length; i++) {
        	xml.append(array[i]);
        	xml.append((i == (array.length - 1))?"":"|");
    	}
    	xml.append("</field>");
    	
    	return xml.toString();
    }
    
    private String listXml(String name, Object value, String classe)  throws Exception {
    	if (value == null) return "";
    	    	
    	StringBuffer xml = new StringBuffer();
    	//xml.append(tagField(name, T_LIST, classe, null));
    	
		int tClass = tipoClass(value.getClass());
    	
		// listas: ArrayList, LinkedList, etc
		if (T_LIST == tClass) {
	    	AbstractList list = (AbstractList)value;
	    	
	    	for (int i = 0; i < list.size(); i++) {
	    		
	        	try {
		    		Object obj =  list.get(i);
		        	xml.append(this.objToXml(obj));
	        	} catch (Exception e) { }
		        	
	    	}
	    	xml.append("</field>");
    	}
		
    	return xml.toString();
    }
    */
    
	private String toXmlString(Element node) throws Exception {
        return toXmlString((Node)node);
    }
        
	private String toXmlString(Node node) throws Exception {
        //Transformer transformerXml = null;
        StreamResult streamXml = null;
        //DOMSource domSource = null;
        
        //transformerXml = TransformerFactory.newInstance().newTransformer();  
        streamXml = new StreamResult( new StringWriter());  
        //domSource = new DOMSource();  


        domSource.setNode(node);  
        transformerXml.transform(domSource, streamXml); 
        
        // retira a tag de cabecalho xml <?xml version="1.0" encoding="UTF-8"?>
        //String xml = streamXml.getWriter().toString().substring(39);
        
        String xml = streamXml.getWriter().toString();
        
        int pos = xml.indexOf("?>");
        
        if (pos != -1) {
        	xml = xml.substring(pos+2);
        }
        
        xml = xml.replaceAll("\\n", "");
        xml = xml.replaceAll("\\r", "");
        xml = xml.replaceAll("\\t", "");
        
        return xml;
    }
	
    private int tipoClass(Class c) {
    	
		if (c.isArray()) {
			return T_ARRAY;
		}
    	
		if (Hashtable.class.equals(c)) {
			return T_MAP;
		}

		if ((Date.class.equals(c)) || (Calendar.class.equals(c))) {
			return T_DATE;
		}
    	
    	while (c.getSuperclass() != null) {
    		if (AbstractList.class.equals(c.getSuperclass())) {
    			return T_LIST;
    		}
    		
    		if (AbstractMap.class.equals(c.getSuperclass())) {
    			return T_MAP;
    		}
    		
    		if ((Date.class.equals(c.getSuperclass())) || (Calendar.class.equals(c.getSuperclass()))) {
    			return T_DATE;
    		}
    		
    		c = c.getSuperclass();
    	}
    	return T_OBJECT;
    	
    }
}
