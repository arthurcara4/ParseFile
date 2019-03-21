/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.accesstage.parsefile.parsexml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import br.com.accesstage.parsefile.utils.Valida;

/**
 *
 * @author glauco
 */
public class XMLUtil {

    private Node[] nodes;
    private String charset = "ISO-8859-1";

    public void setCharset(String charset) {
    	this.charset = charset;
    }
    public String getCharset() {
    	return this.charset;
    }
    
    public XMLUtil(File sourceFile) throws Exception {
    	if (Valida.isEmpty(sourceFile)) {
    		return;
    	}
    	
        //FileReader fr = null;
        FileInputStream fs = null;
        InputStreamReader fr = null;
        BufferedReader in = null;
        
        StringBuffer xml = new StringBuffer();
        try {
	        //fr = new FileReader(sourceFile);
            fs = new FileInputStream(sourceFile);
            fr = new InputStreamReader(fs, charset);
	        in = new BufferedReader(fr);
	        
	        String line = "";
            while ((line = in.readLine()) != null) {
            	line = line.replaceAll("\\t", "");
            	xml.append(line);
            }
	        
	        
        } catch (Exception e) {
			// TODO: handle exception
        } finally {
            if (in != null) { in.close(); in = null; }
            if (fr != null) { fr.close(); fr = null; }
            if (fs != null) { fs.close(); fs = null; }
		}
        
        if (Valida.isEmpty(xml)) return;
        
    	construtor(xml.toString());
    }
    

    public XMLUtil(String source) throws Exception {
    	construtor(source);
    }
    
    private void construtor(String source) throws Exception {
        Convert conv = new Convert();
        
        int pos = source.lastIndexOf("?>");
        if (pos != -1) source = source.substring(pos + 2);

        nodes = conv.getNodes(source);
    }

    public Node getPath(String id) {
        String[] path = id.split("\\.");

        Node ret = null;

        for (int ti = 0; ti < path.length; ti++) {
            if (ret == null) {
                ret = getNode(path[ti]);
            } else {
                ret = ret.getNode(path[ti]);
            }

            if (ret == null) {
                return null;
            }
        }

        return ret;
    }

    public Node getNode(String id) {
        if (nodes == null) {
            return null;
        }

        for (int ch = 0; ch < nodes.length; ch++) {
            if (id.equals(nodes[ch].getName())) {
                return nodes[ch];
            }
        }
        return null;
    }

    public class Node {

        private String[] atVal;
        private String[] atIds;
        private String value;
        private String name;
        private Node[] childs;

        public Node() {
        }

        public Node(String name, String att, String value) throws Exception {
            setName(name);
            setAtrib(att);
            setValue(value);
        }

        public String[] getAtIds() {
            return atIds;
        }

        public void setAtIds(String[] atIds) {
            this.atIds = atIds;
        }

        public String[] getAtVal() {
            return atVal;
        }

        public void setAtVal(String[] atVal) {
            this.atVal = atVal;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) throws Exception {

            Convert conv = new Convert();

            childs = conv.getNodes(value);

            if (childs == null) {
                this.value = value;
            }
        }

        public Node[] getChilds() {
            return childs;
        }

        public void setChilds(Node[] childs) {
            this.childs = childs;
        }

        public boolean hasChilds() {
            return !Valida.isEmpty(childs);
        }
        
        public Node getNode(String id) {
            if (childs == null) {
                return null;
            }

            for (int ch = 0; ch < childs.length; ch++) {
                if (id.equals(childs[ch].getName())) {
                    return childs[ch];
                }
            }
            return null;
        }
        
        public Node getNodeSubstr(String substr) {
        	return getNodeSubstr(substr, 0);
        }
        public Node getNodeSubstr(String substr, int start) {
            if (childs == null) {
                return null;
            }

            for (int ch = start; ch < childs.length; ch++) {
            	String name = childs[ch].getName();
            	
            	if (!Valida.isEmpty(name)) { 
	                if (name.indexOf(substr) != -1) {
	                    return childs[ch];
	                }
            	}
            }
            return null;
        }
        

        public int getAtribInt(String id) {
            try {
                return Integer.parseInt(getAtrib(id));
            } catch (Exception e) {
                return 0;
            }
        }

        public boolean getAtribBoolean(String id) {
            return ("true".equals(getAtrib(id)));
        }

        public String getAtrib(String id) {
            if (atIds == null) {
                return "";
            }

            for (int at = 0; at < atIds.length; at++) {
                if (id.equals(atIds[at])) {
                    return atVal[at];
                }
            }
            return "";
        }

        public void setAtrib(String att) throws Exception {
            if (Valida.isEmpty(att)) {
                return;
            }

            int count = 0;
            int pos = att.indexOf("=");
            while (pos != -1) {
                count++;
                pos = att.indexOf("=", pos + 1);
            }

            if (count == 0) {
                return;
            }

            atIds = new String[count];
            atVal = new String[count];

            int posi = att.indexOf("=");
            count = 0;
            while (posi != -1) {

                int posf = att.indexOf("=", posi + 1);

                if (posf == -1) {
                    posf = att.length();
                }

                String id = att.substring(0, posi).trim();

                String value = att.substring(posi + 1, posf).trim();

                String aspas = "\"";

                int posia = value.indexOf(aspas);

                if (posia == -1) {
                    aspas = "\'";
                    posia = value.indexOf(aspas);
                    if (posia == -1) {
                        throw new Exception("Valor do atributo: " + id + "sem aspas");
                    }
                }

                int posfa = value.indexOf(aspas, posia + 1);

                if (posfa == -1) {
                    throw new Exception("Valor do atributo: " + id + "sem aspas");
                }

                value = value.substring(posia + 1, posfa).trim();

                atIds[count] = id;
                atVal[count] = value;

                count++;
                att = att.substring(posi + 1 + posfa + 1);

                posi = att.indexOf("=");
            }
        }
    }

    public class Convert {

        private String tagname(String tag) {
            int posf = tag.indexOf(" ");

            if (posf != -1) {
                return tag.substring(1, posf);
            }

            posf = tag.indexOf(">");

            if (tag.indexOf("/") != -1) {
                posf -= 1;
            }

            return tag.substring(1, posf);
        }

        private String tagi(String source) {

            int posi = source.indexOf("<");

            if (posi == -1) {
                return null;
            }

            int posf = source.indexOf(">", posi);

            if (posf == -1) {
                return null;
            }

            return source.substring(posi, posf + 1);
        }

        private String tagf(String name) {
            return new StringBuffer("</").append(name).append(">").toString();
        }

        private String atribs(String tag) {

            int posi = tag.indexOf(" ");

            if (posi == -1) {
                return null;
            }

            int fim = 1;

            if (tag.indexOf("/") != -1) {
                fim += 1;
            }

            return tag.substring(posi, tag.length() - fim);
        }

        private int countChilds(String source) throws Exception {

            int count = 0;

            while (source.length() > 0) {
                String tag = tagi(source);

                if (tag == null) {
                    return count;
                }

                String name = tagname(tag);

                if ("".equals(name.trim())) {
                    throw new Exception("nodes invalidas");
                }

                int posf = tag.indexOf("/>");

                if (posf == -1) {
                    String tagf = tagf(name);
                    posf = source.indexOf(tagf);
                    if (posf == -1) {
                        return 0;
                    }
                    posf += tagf.length();
                } else {
                    posf += 2;
                }
                count++;

                source = source.substring(posf).trim();
            }

            return count;
        }

        public Node[] getNodes(String source) throws Exception {
        	source = source.trim();
            //boolean nodesemCorpo = false;

            int count = countChilds(source);

            if (count == 0) {
                return null;
            }

            Node[] nodes = new Node[count];

            count = 0;
            while (source.length() > 0) {
                String tag = tagi(source);

                String name = tagname(tag);

                if ("".equals(name.trim())) {
                    throw new Exception("nodes invalidas");
                }

                String atribs = atribs(tag);

                int posf = tag.length();

                if (tag.indexOf("/>") != -1) {
                    nodes[count] = new Node();
                    nodes[count].setName(name);
                    nodes[count].setAtrib(atribs);

                } else {
                	source = source.trim();

                    String tagF = new StringBuffer("</").append(name).append(">").toString();

                    int posfi = source.indexOf(tagF);

                    if (posfi == -1) {
                        throw new Exception("Fechamento Tag invalida: " + name);
                    }

                    String value = source.substring(tag.length(), posfi);

                    nodes[count] = new Node();
                    nodes[count].setName(name);
                    nodes[count].setAtrib(atribs);
                    nodes[count].setValue(value);

                    posf = posfi + tagF.length();
                }
                count++;

                source = source.substring(posf);
                
            	if (source != null) {
            		source = source.trim();
            	}
                
            }

            return nodes;
        }
    }
}
