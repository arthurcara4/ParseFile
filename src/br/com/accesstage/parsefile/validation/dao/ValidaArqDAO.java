package br.com.accesstage.parsefile.validation.dao; 

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import oracle.jdbc.OracleTypes;
import br.com.accesstage.parsefile.validation.util.ConnType;
import br.com.accesstage.parsefile.validation.vo.Arquivo;

public class ValidaArqDAO 
{ 
    
	private ConnType connType;
	
    Connection conn = null;
    CallableStatement cs = null;
    
    public ValidaArqDAO(ConnType connType){
        this.connType = connType;
    }
    
    public Connection openConnection() throws NamingException, SQLException, ClassNotFoundException {
    	
    	if (this.connType.getConnType() == ConnType.CONN_POOL) { 
	        InitialContext ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup(this.connType.getConnStr());
			return ds.getConnection();
    	}
    	
    	if (this.connType.getConnType() == ConnType.CONN_JDBC_THIN) { 
    		 Class.forName("oracle.jdbc.driver.OracleDriver");
    		 
    		return 	DriverManager.getConnection(
										    		this.connType.getConnStr(),
										    		this.connType.getUser(),
										    		this.connType.getPass()
									    		);
    	}
    	
    	return null;
    }
    
    public void close() throws Exception {
        try {
            if (cs != null) {
                cs.close();
                cs = null;
            }
            
            if (conn != null) {
                conn.close();
                conn = null;
            }
        } catch (Exception e) {
            throw new Exception("LoginDao: close() - Problemas ao fechar a conexão com o banco de dados.");
        }
    } 
    
    
    public ArrayList buscaTracking(String tracking) throws Exception {
		
		try {
            conn = openConnection();
            
			cs = conn.prepareCall("{call PKG_VALIDADOC.P_BUSCA_TRACKING(?,?)}");
            
            cs.setString(1,tracking);
            cs.registerOutParameter(2,OracleTypes.CURSOR);
            
            cs.execute();
            
            return getArqs((ResultSet)cs.getObject(2));
            
        } catch (NamingException e) {
            throw new Exception("ValidaArqDAO: buscaTracking() - Problemas com contexto inicial",e);
        } catch (SQLException e) {
            throw new Exception("ValidaArqDAO: buscaTracking() - Problemas com a busca",e);
        } catch (ClassNotFoundException e) {
            throw new Exception("ValidaArqDAO: buscaTracking() - Problemas com o dirver do oracle",e);
		} finally {
            close();
		}
    }
    
    public ArrayList buscaFilesService(String service) throws Exception {
		
		try {
            conn = openConnection();
            
			cs = conn.prepareCall("{call PKG_VALIDADOC.P_BUSCA_FILES_SERVICE(?,?)}");
            
            cs.setString(1,service);
            cs.registerOutParameter(2,OracleTypes.CURSOR);
            
            cs.execute();
            
            return getArqs((ResultSet)cs.getObject(2));
            
        } catch (NamingException e) {
            throw new Exception("ValidaArqDAO: buscaTracking() - Problemas com contexto inicial",e);
        } catch (SQLException e) {
            throw new Exception("ValidaArqDAO: buscaTracking() - Problemas com a busca",e);
        } catch (ClassNotFoundException e) {
            throw new Exception("ValidaArqDAO: buscaTracking() - Problemas com o dirver do oracle",e);
		} finally {
            close();
		}
    }
    
    public Hashtable getParams() throws Exception {
        
        ResultSet rs = null;
		try {
            conn = openConnection();
            
			cs = conn.prepareCall("{call PKG_VALIDADOC.P_PARAMS(?)}");
            
            cs.registerOutParameter(1,OracleTypes.CURSOR);
            
            cs.execute();
            
            rs = (ResultSet)cs.getObject(1);
            
            if (rs == null) {
                return null;
            }
            
            Hashtable params = null;
            
            while (rs.next()) {
                if (params == null) params = new Hashtable();
                
                params.put(
                    rs.getString("CHAVE")
                    , rs.getString("VALOR")
                );
            }
            
            
            return params;
            
        } catch (NamingException e) {
            throw new Exception("ValidaArqDAO: getParams() - Problemas com contexto inicial",e);
        } catch (SQLException e) {
            throw new Exception("ValidaArqDAO: getParams() - Problemas com a busca",e);
        } catch (ClassNotFoundException e) {
            throw new Exception("ValidaArqDAO: getParams() - Problemas com o dirver do oracle",e);
		} finally {
            if (rs != null) {
                rs.close();
                rs = null;
            }
            close();
		}
    }
    
    
    
    
    
    private ArrayList getArqs(ResultSet conf) throws Exception {
        try {
            if (conf == null) {
                return  null;
            }
            
            ArrayList arqs = null;
            while (conf.next()) {
                Arquivo arq = new Arquivo();
                
                arq.setArquivo(conf.getString("ARQUIVO"));
                arq.setXmlLayout(conf.getString("XMLLAYOUT"));
                arq.setIdLayout(conf.getString("IDLAYOUT"));
                arq.setSender(conf.getString("SENDER"));
                arq.setReceiver(conf.getString("RECEIVER"));
                arq.setDoctype(conf.getString("DOCTYPE"));
                arq.setTrackid(conf.getString("TRACKID"));
                arq.setDataGer(conf.getString("DATAGER"));
                
                if (arqs == null) arqs = new ArrayList();
                
                arqs.add(arq);             
            }
            
            return arqs;
            
        } catch (Exception e) {
            throw e;
        } finally {
            if (conf != null) {
                conf.close();
                conf = null;
            }
        }
    }
} 
