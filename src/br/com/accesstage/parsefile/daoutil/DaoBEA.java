package br.com.accesstage.parsefile.daoutil; 

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import oracle.jdbc.OracleTypes;
import br.com.accesstage.parsefile.retornos.DinTableVO;
import br.com.accesstage.parsefile.retornos.DinVO;
import br.com.accesstage.parsefile.utils.Valida;

public class DaoBEA {
    
    private static final int PROC = 1;
    private static final int FUNC = 2;
    
    private String jndi;
    private Connection conn;
    private CallableStatement call;
    
    private String procName;
    
    
    private LinkedList params;
    
    public DaoBEA(String jndi) {
        setJndi(jndi);
    }
    
    //** gets e setters **//
    
	public String getJndi() {
		return jndi;
	}
	public void setJndi(String jndi) {
		this.jndi = jndi;
	}
    
    
    //** metodos da classe **//
    public void openConn() throws Exception {
    	openConn(true);
    }
    
    public void openConn(boolean clearParams) throws Exception {
        close();
        
		InitialContext ctx = new InitialContext();
		DataSource ds = (DataSource)ctx.lookup(jndi);
		conn = ds.getConnection();
		
		if(clearParams) clearParams();
    }
    
    public void close() throws Exception {
        
        closeCall();
        if (conn != null) conn.close(); conn = null;
    }
    
    public void executeProc(String procName) throws Exception{
        
        this.procName = procName;
        
        montaCall(PROC);
        
        montaRetorno();
    }
    
    private void closeCall() throws Exception {
        if (call != null) call.close(); call = null;
    }
    
    private void montaCall(int tipo) throws Exception {
        
        //validParam();
        
        StringBuffer sb = new StringBuffer();
        
        if (tipo == PROC) sb.append("{ CALL ");
        
        if (tipo == FUNC) sb.append("{ CALL ? := ");
        
        sb.append(procName);
        
        if (!Valida.isEmpty(params) ){
	        sb.append(" (");
	        
	        for (int pp = 0 + ((tipo == FUNC)?1:0) ; pp < params.size(); pp++) {
	            if (pp > 0) sb.append(",");
	                
	            sb.append("?");
	        }
	        sb.append(")");
        }
        sb.append(" }");
        
        // fecha o call caso estiver aberto por outra chamada 
        closeCall();
        
        call = conn.prepareCall(sb.toString());
        
        if (!Valida.isEmpty(params) ) {
        
	        for(int pp = 0; pp < params.size(); pp++) {
	            
	            Param par = (Param)params.get(pp);
	            
	            if (par == null) throw new Exception ("DaoBEA:montaCall(): O parametro na posicao: " +  (pp + 1) + " nao foi inicializado (null). Proc chamada: " + procName);
	            
	            if (par.sentido == Param.IN) {
	                
	                if (par.value == null) {
	                    call.setNull((pp + 1), par.getTipo());
	                } else {
	                    // seta todos os parametros como String pois o oracle 
	                    // faz as devidas conversões quando recebe o valor
	                    call.setString((pp + 1), (String)par.getValue());
	                }
	            }
	                        
	            if (par.sentido == Param.OUT) {
	                call.registerOutParameter((pp + 1), par.getTipo());
	            }
	            
	        }
        }
        
        call.execute();

    }
    
    private void montaRetorno() throws Exception {
    	
    
        if (!Valida.isEmpty(params) ) {
        
	        for(int pp = 0; pp < params.size(); pp++) {
	            
	            Param par = (Param)params.get(pp);
	            
	            if (par.sentido == Param.OUT) {
	                
	                if (par.getTipo() == OracleTypes.CURSOR) {
	                    //((Param)params.get(pp)).setValue( toDinTableVO((ResultSet)call.getObject(pp + 1)) );
	                	ResultSet rs = null;
	                	try {
	                		rs = (ResultSet)call.getObject(pp + 1);
	                	} catch (SQLException e) {
							if (!"Cursor is closed.".equals(e.getLocalizedMessage())) {
								throw e;
							}
						}
	                	
	                    ((Param)params.get(pp)).setValue( rs );
	                } else {
	                    ((Param)params.get(pp)).setValue( call.getString(pp + 1) );
	                } 
	            }
	        }
        }
    }
    
    private DinTableVO toDinTableVO(ResultSet rs) throws Exception {
         
         if (rs == null) return null;
         
         DinTableVO vo = new DinTableVO();
         
         ResultSetMetaData mt = rs.getMetaData();
         
         if ((mt == null) || (mt.getColumnCount() == 0)) return null;
         
         for (int cl = 0; cl < mt.getColumnCount(); cl++) {
            vo.addId(mt.getColumnName(cl + 1));
         }
         
         int lin = -1;
         while (rs.next()) {
             ++lin;
             for (int cl = 0; cl < mt.getColumnCount(); cl++) {
                vo.setValor(lin, cl, rs.getString(cl + 1));
             }
         }
         
         rs.close();
         
         return vo;
    }
    
    private DinVO toDinVO(ResultSet rs) throws Exception {
        
        if (rs == null) return null;
        
        DinVO vo = null;
        
        ResultSetMetaData mt = rs.getMetaData();
        
        if ((mt == null) || (mt.getColumnCount() == 0)) return null;
        
        if (rs.next()) {
        	vo = new DinVO();
            for (int cl = 0; cl < mt.getColumnCount(); cl++) {
               vo.addset(mt.getColumnName(cl + 1), rs.getString(cl + 1));
            }
        }
        
        rs.close();
        
        return vo;
   }
    
    
    //** metodos de passagem de parametros **//
    
    public void clearParams() {
        if (params == null) return;
        params.clear();
        params = null;
    }
    
    public void addInNull(int tipo) {
        addInNull(-1, tipo);
    }
    public void addInNull(int pos, int tipo) {
        addParam(pos, null, null, tipo, Param.IN);
    }
    
    public void addIn(String value) {
        addIn(-1, value);
    }
    public void addIn(int pos, String value) {
        addParam(pos, null, value, OracleTypes.VARCHAR, Param.IN);
    }
    public void addIn(int value) {
        addIn(-1, value);
    }
    public void addIn(int pos, int value) {
        addParam(pos, null, String.valueOf(value), OracleTypes.NUMBER, Param.IN);
    }
    public void addIn(long value) {
        addIn(-1, value);
    }
    public void addIn(int pos, long value) {
        addParam(pos, null, String.valueOf(value), OracleTypes.NUMBER, Param.IN);
    }
    
    
    public void addOutString(String id) {
        addOutString(-1, id);
    }
    public void addOutString(int pos, String id) {
        addParam(pos, id, null, OracleTypes.VARCHAR, Param.OUT);
    }
    public void addOutCursor(String id) {
        addOutCursor(-1, id);
    }
    public void addOutCursor(int pos, String id) {
        addParam(pos, id, null, OracleTypes.CURSOR, Param.OUT);
    }
    public void addOutLong(String id) {
        addOutString(-1, id);
    }
    public void addOutLong(int pos, String id) {
        addParam(pos, id, null, OracleTypes.NUMBER, Param.OUT);
    }
    public void addOutInt(String id) {
        addOutString(-1, id);
    }
    public void addOutInt(int pos, String id) {
        addParam(pos, id, null, OracleTypes.NUMBER, Param.OUT);
    }
    
    public void addParam(int pos, String id, String value, int tipo, int sentido) {
        
        if (params == null) params = new LinkedList();
        
        if (pos == -1) pos = params.size() + 1;
        
        while (pos > (params.size())) params.add(null);
        
        params.set((pos - 1), new Param(id, value, tipo, sentido));
    }
    
    
    public DinTableVO getDinTableVO(String id) throws Exception {
         Param par = getParam(id);
         if (par == null) return null;
         
         if (par.getTipo() != OracleTypes.CURSOR) {
            throw new Exception("DaoBEA:getResultSet(): O parametro OUT: " + id + " não é do tipo Cursor");   
         }
         
         return toDinTableVO((ResultSet)par.getValue()); 
    }
    
    
    public int getInt(String id) throws Exception {
        String val = getString(id);
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            throw new Exception("DaoBEA:getInt(): O campos de saida: " + id + " não é um valor inteiro (" + val + ").");
        }
    }

    public long getLong(String id) throws Exception {
        String val = getString(id);
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            throw new Exception("DaoBEA:getInt(): O campos de saida: " + id + " não é um valor inteiro (" + val + ").");
        }
    }
    
    public String getString(String id) throws Exception {
         Param par = getParam(id);
         if (par == null) return null;
         
         if (par.getTipo() == OracleTypes.CURSOR) {
            throw new Exception("DaoBEA:getResultSet(): O parametro OUT: " + id + " É um CURSOR não pode ser pego como valor");   
         }
         
         return (String)par.getValue();
    }
    
    public DinVO getDinVO(String id) throws Exception {
         Param par = getParam(id);
         if (par == null) return null;
         
         if (par.getTipo() != OracleTypes.CURSOR) {
            throw new Exception("DaoBEA:getResultSet(): O parametro OUT: " + id + " não é do tipo Cursor");   
         }
         
         return toDinVO((ResultSet)par.getValue());
    }
    
    public ResultSet getResultSet(String id) throws Exception {
        Param par = getParam(id);
        if (par == null) return null;
        
        if (par.getTipo() != OracleTypes.CURSOR) {
           throw new Exception("DaoBEA:getResultSet(): O parametro OUT: " + id + " não é do tipo Cursor");   
        }
        return (ResultSet)par.getValue();
   }
    
    public ArrayList getList(String id, Class classe) throws Exception {
        Param par = getParam(id);
        if (par == null) return null;
        
        if (par.getTipo() != OracleTypes.CURSOR) {
           throw new Exception("DaoBEA:getResultSet(): O parametro OUT: " + id + " não é do tipo Cursor");   
        }
        
        ResultSet rs = (ResultSet)par.getValue();
        
        if (rs == null) return null;
        
        VOListParse pl = new VOListParse();
        
        if (Valida.isEmpty(pl)) return null;
        
        //ArrayList arr = pl.parseUsuario(rs,classe);
        
        return pl.parseUsuario(rs,classe);
   }
    
    private Param getParam(String id) throws Exception {
        if ((id == null) || ("".equals(id.trim())) ) return null;
        //validParam();
        
        if (!Valida.isEmpty(params) ) { 
	        for (int pp = 0; pp < params.size(); pp++)  {
	            Param par = (Param)params.get(pp);
	            
	            if (id.equals(par.getId())) {
	                return par;
	            } 
	        }
        }
        
        return null;
    }
    
    //private void validParam() throws Exception {
    //    if ((params == null) || (params.size() == 0) || (params.isEmpty())) throw new Exception ("DaoBEA:montaCall(): Nao foi informado nenhum parametro (IN ou OUT). Proc chamada: " + procName);
    //}
    
    
    private class Param {
        public static final int IN = 1;
        public static final int OUT = 2;
        //public static final int INOUT = 3;
        
        private String id;
        private Object value;
        private int tipo;
        private int sentido;
        
        //public Param() {}
        
        public Param(String id, Object value, int tipo, int sentido) {
            setId(id);
            setValue(value);
            setTipo(tipo);
            setSentido(sentido);
        }
        
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public Object getValue() {
            return value;
        }
        public void setValue(Object value) {
            this.value = value;
        }
        public int getTipo() {
            return tipo;
        }
        public void setTipo(int tipo) {
            this.tipo = tipo;
        }
        //public int getSentido() {
        //    return sentido;
        //}
        public void setSentido(int sentido) {
            this.sentido = sentido;
        }
    }
} 
