package br.com.accesstage.parsefile.daoutil; 

import java.beans.Statement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.ArrayList;

public class VOListParse { 
    
    public ArrayList parseUsuario(ResultSet rs, Class usuClass) throws Exception {
        Object usu = null;
        ArrayList arr = null;
		try {
            if (rs == null) { 
                return null; 
            }
            

            ArrayList fields = new ArrayList();
            // correção feita para considerar também os atributos das superclasses
            for (Class classe = usuClass; classe != null; classe = classe.getSuperclass()) {
                fields.add(classe.getDeclaredFields());
            }
            
            ResultSetMetaData meta = rs.getMetaData(); 
            
            // varre todas as colunas do rs para alimentar o objeto de usuários
            // IMPORTANTE: para que esse processo funcione corretamente o alias das colunas no oracle
            // devem corresponder as propriedades do objeto de uruários
            
             //retirado
			//Field list[] = usuClass.getDeclaredFields();
	            
            arr = new ArrayList();
	            
            while (rs.next()) {
            	
                usu = usuClass.getConstructor(new Class[] {}).newInstance(new Object[] {});
	                
				Statement stmt = null; 
	            for (int col = 1; col <= meta.getColumnCount(); col++) {
	                
	                String coluna =  meta.getColumnName(col);
	                
	                // pega todos os atributos inclusive os das superclasses
	                for (int fdl = 0; fdl < fields.size(); fdl++) {
	                    
	                    Field list[] = (Field[])fields.get(fdl);
	                    
	                    int pos = findInFields(list, coluna);
	                    
	                    if (pos != -1) {
	                        stmt = statement(list[pos], usu, rs.getString(coluna));
	                        stmt.execute();
	                    }
	                }
	            }
	            
	            arr.add(usu);
	            
            }
		}
		catch (Throwable e) {
			throw new Exception("DaoBEA:VOListParse:getList(): problemas com o parse da classe " + usuClass.getName(),e);
		} finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            } catch (SQLException s) {
                throw new Exception("DaoBEA:VOListParse:getList(): problemas ao fechar o resultset ",s);
            }
        }
	
        
        
        return arr;
    } 
    
    private int findInFields(Field list[], String col) {
        for (int i = 0; i < list.length; i++) {
            if (list[i].getName().equalsIgnoreCase(col)) {
                return i;
            }
        }
        
        return -1;
    }
    
    private Statement statement(Field field, Object usu, String value) throws Exception {
        
        String atrib = field.getName();
        
        
        StringBuffer set = new StringBuffer("set")
        .append(atrib.substring(0,1).toUpperCase())
        .append(atrib.substring(1));
        
        // para tipos primitivos é necessario fazer uma conversão
        if ("int".equals(field.getType().toString())) {
        	if (value == null) value = "0";
            return new Statement(usu, set.toString(), new Object[] { new Integer(value)});
        }
        
        if ("long".equals(field.getType().toString())) {
        	if (value == null) value = "0";
            return new Statement(usu, set.toString(), new Object[] { new Long(value)});
        }
        
        // para String ou qualquer outro objeto inicializa o construtor do objeto
        // o tipo é definido peli tipo do atributo da classe usuario
        // IMPORTANTE: O objeto deve possuir um contrutor que possua um parametro String para não 
        Constructor ct = field.getType().getConstructor(new Class[]{String.class});
        if (value == null) {
        	return new Statement(usu, set.toString(), new Object[] {null});
        } else {
        	return new Statement(usu, set.toString(), new Object[] {ct.newInstance(new Object[] {new String(value)})});
        }
        
    }
    
} 
