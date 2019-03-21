package br.com.accesstage.parsefile.daoutil;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;

import oracle.jdbc.OracleTypes;
import br.com.accesstage.parsefile.exceptions.ParseFileException;
import br.com.accesstage.parsefile.layout.Totalizador;
import br.com.accesstage.parsefile.retornos.DinVO;
import br.com.accesstage.parsefile.retornos.Valores;
import br.com.accesstage.parsefile.utils.Valida;

public class CallableBatch {
	
	private String id;
	private CallableStatement call;
	private int buffer;
	private int paramsCount;
	private ArrayList numLins;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public CallableStatement getCall() {
		return call;
	}
	public void setCall(CallableStatement call) {
		this.call = call;
	}
	public int getBuffer() {
		return buffer;
	}
	public void setBuffer(int buffer) {
		this.buffer = buffer;
	}
	public int getParamsCount() {
		return paramsCount;
	}
	public void setParamsCount(int paramsCount) {
		this.paramsCount = paramsCount;
	}
	
	public CallableBatch() {}
	
	public CallableBatch(SqlExecVO vo, Connection conn) throws Exception {
		if (vo.getTipo() == SqlExecVO.T_BACTH) {
			setCall(vo.getSql(), conn);
		}
		if (vo.getTipo() == SqlExecVO.T_PROC) {
			setCallProc(vo.getSql(), conn, vo.getParametros());
		}
		setBuffer(vo.getBuffer());
		numLins = new ArrayList();
		setId(vo.getId());
	}
	
	public void setCall(String parseCall, Connection conn) throws Exception {
		
		if (conn == null) throw new Exception("Erro de Layout: Conexao com o banco nula para execucao em batch.");
	
		if (Valida.isEmpty(parseCall)) return;
		
		// conta quantos parametros tem a entada sql
		paramsCount = parseCall.split("\\?").length-1;
		
		call = conn.prepareCall(parseCall);
	}
	
	public void setCallProc(String parseCall, Connection conn, String params) throws Exception {
		
		if (conn == null) throw new Exception("Erro de Layout: Conexao com o banco nula para execucao em sql.");
	
		if (Valida.isEmpty(parseCall)) return;
		
		// conta quantos parametros tem a entada sql
		paramsCount = params.split("\\,").length;
		
		StringBuffer sb = new StringBuffer();
		sb.append("{ CALL ");
		sb.append(parseCall);
		
		if (paramsCount > 0) { 
			sb.append("(");
			
	        for (int pp = 0 ; pp < paramsCount; pp++) {
	            if (pp > 0) sb.append(",");
	            sb.append("?");
	        }
	        sb.append(")");
		}
		sb.append(" }");
		
		call = conn.prepareCall(sb.toString());
	}
	
	
	public void exec(SqlExecVO vo, DinVO var, Valores val, long numLinha, DinVO tots, boolean read) throws Exception {
		
		try {
			String campos = "";
			if (!Valida.isEmpty(vo.getParametros())) campos = vo.getParametros();
			String params[] = campos.split("\\,");
			
			if (params.length != paramsCount) throw new Exception("Erro de Layout: quantiade de parametros de execucao " + vo.getTipoDesc() + " invalida. " + vo.getTipoDesc() + "Id[" + id + "] linhaId[" + val.getLinhaId() + "] informado[" + params.length + "] esperado[" + paramsCount + "]");

			if (!Valida.isEmpty(params)) {
				
				call.clearParameters();
				
				if (!Valida.isEmpty(params)) {
					for (int p = 0; p < params.length; p++) {
						String campo = params[p];
						
						if (!Valida.isEmpty(campo)) {
							campo = campo.trim();
							
							if (campo.indexOf("v:") != -1) {
								campo = campo.replaceAll("v:", "");
								call.setString(p+1, var.getString(campo));
							}
							if (campo.indexOf("t:") != -1) {
								campo = campo.replaceAll("t:", "");
								
								if (Valida.isEmpty(tots.get(campo))) {
									throw new Exception("Erro de Layout: Totalizador " + campo + " nao encontrado nos parametros SQL");
								}
								
								if (read) call.setLong(p+1, ((Totalizador)tots.get(campo)).getContadorRead());
								if (!read) call.setLong(p+1, ((Totalizador)tots.get(campo)).getContadorWrite());
							}
							if (campo.indexOf("c:") != -1) {
								campo = campo.replaceAll("c:", "");
								call.setString(p+1, val.getString(campo));
							}
							if (campo.indexOf("vo:") != -1) {
								call.registerOutParameter(p+1, OracleTypes.VARCHAR);
							}
							
						}
					}
				}
			}
			
			if (vo.getTipo() == SqlExecVO.T_BACTH) {
				call.addBatch();
				numLins.add(String.valueOf(numLinha));
				
				if (numLins.size() >= getBuffer()) {
					commit();
				}
			}
			
			if (vo.getTipo() == SqlExecVO.T_PROC) {
				call.execute();
				
				if (!Valida.isEmpty(params)) {
					
					for (int p = 0; p < params.length; p++) {
						String campo = params[p];
						
						if (!Valida.isEmpty(campo)) {
							campo = campo.trim();
							if (campo.indexOf("vo:") != -1) {
								String valor = call.getString(p+1);
								campo = campo.replaceAll("vo:", "");
								var.addset(campo, valor);
							}
						}
					}
				}
			} 
		} catch (ParseFileException pe) {
			try {
				close();
			} catch (Exception ex) {
				throw new ParseFileException(ex);
			}
			throw pe;
			
		} catch (Exception e) {
			
			try {
				close();
			} catch (Exception ex) {
				throw new ParseFileException(ex);
			}
			
			throw new ParseFileException(e);
		}
	}
	
	public void commit() throws Exception {
		try {
			int[] result = call.executeBatch();
			
			if (Valida.isEmpty(result)) return;
			
			for (int rr = 0; rr < result.length; rr++) {
				if (result[rr] == Statement.EXECUTE_FAILED) {
					throw new ParseFileException(ParseFileException.BATCH_EXCEPTION, "Erro de Layout: Ocorreu um erro ao executar o bacth. batchId[" + id + "] numLinha[" + numLins.get(rr) + "]");
				}
			}
			numLins = new ArrayList();
			
		} catch (Exception e) {
			numLins = new ArrayList();
			throw e;
		}
			
	}
	
	public void close() throws Exception{
		if (call != null) {call.close(); call = null;}
	}
	
	public int retPos(int pos1, int pos2) {
		if ( (pos1 == -1) && (pos2 == -1) ) return -1;
		if (pos1 == -1)   return pos2;
		if (pos2 == -1)   return pos1;
		if (pos1 < pos2)  return pos1;
		if (pos2 < pos1)  return pos2;
		return -1;
	}
	
	
	

}
