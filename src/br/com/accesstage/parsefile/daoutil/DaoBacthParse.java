package br.com.accesstage.parsefile.daoutil;

import java.sql.Connection;
import java.util.LinkedList;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import br.com.accesstage.parsefile.exceptions.ParseFileException;
import br.com.accesstage.parsefile.retornos.DinVO;
import br.com.accesstage.parsefile.retornos.Valores;
import br.com.accesstage.parsefile.utils.Valida;


public class DaoBacthParse {
	
	private DinVO conns;
	private DinVO calls;
	
	public DaoBacthParse() {
		conns = new DinVO();
		calls = new DinVO();
	}
	
	public Connection getConn(String dataSource) throws ParseFileException {
		
		if (Valida.isEmpty(dataSource)) throw new ParseFileException("Erro de Layout: tentativa de conexao com dataSource em branco");
		
		try {
			// conexao do tipo batch não pode ser fechada durante o parse
			dataSource = dataSource.replaceAll("\\#", "");
			
			int pos = conns.findInIds(dataSource); 
			if (pos != -1) {
				Connection conn = (Connection)conns.get(pos);
				if (conn == null)  throw new ParseFileException("Erro de Layout: Conexão com o data source de batch [" + dataSource + "] ja foi instanciada previamente mas esta nula");
				
				return conn;
			}
			
			InitialContext ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup(dataSource);
			Connection conn = ds.getConnection();
			
			conns.addset(dataSource, conn);
			
			return conn;
			
		} catch (Exception ex) {
			ParseFileException pf = new ParseFileException(ex);
			pf.setCodigoErro(ParseFileException.SQL_EXCEPTION);
			throw pf;
		}
	}
	
	public void close() throws ParseFileException {
		try {
			if (!Valida.isEmpty(calls)) {
				for (int cc = 0; cc < calls.size(); cc++) {
					CallableBatch cb = (CallableBatch)calls.get(cc);
					cb.close();
				}
				calls.clear();
			}
			
			// fecha todas as conexoes abertas
			LinkedList ccs = conns.getValues();
			if (!Valida.isEmpty(ccs)) {
				for (int cc = 0; cc < ccs.size(); cc++) {
					Connection conn = (Connection)ccs.get(cc);
					if (conn != null) conn.close();
					
					ccs.set(cc, null);
				}
			}
			
		} catch (Exception ex) {
			ParseFileException pf = new ParseFileException(ex);
			pf.setCodigoErro(ParseFileException.SQL_EXCEPTION);
			throw pf;
		}
	}
	
	/*
	public void addBatch(String sql, int buffer, String id, String dataSource) throws ParseFileException {
		try {
			
			if (Valida.isEmpty(calls)) calls = new ArrayList();
			
			// abre o batch na conn de seu respectivo dataSource
			calls.add( new CallableBatch(sql, getConn(dataSource), buffer, id) );
			
		} catch (Exception ex) {
			throw new ParseFileException(ex);
		}
	}
	*/
	
	public void exec(SqlExecVO vo, DinVO var, Valores val, long numLinha, DinVO tots, boolean read) throws ParseFileException {
		
		try {
			CallableBatch call = null;
			
			// procura o id da chamada na lista de calls
			int pos = calls.findInIds(vo.getId()); 
			// se nao encontrar cria a chamada
			if (pos == -1) {
				call = new CallableBatch(vo, getConn(vo.getDataSource()));
				calls.addset(vo.getId(), call);
			} else {
				call = (CallableBatch)calls.get(pos);
			}
			
			if (call == null) throw new Exception("A chamada Call[" + vo.getId() + "] nao esta inicializada para execucao " + vo.getTipoDesc()); 
			
			
			// o commit é feito internamente
			call.exec(vo, var, val, numLinha, tots, read);
			
		} catch (Exception e) {
			
			try {
				close();
			} catch (Exception ex) {}
			
			ParseFileException pf = new ParseFileException(e);
			pf.setCodigoErro(ParseFileException.SQL_EXCEPTION);
			throw pf;
		}
	}
	
	public void commitBatchs() throws ParseFileException {
		
		try {
			
			for (int cc = 0; cc < calls.size(); cc++) {
				CallableBatch cb = (CallableBatch)calls.get(cc);
				cb.commit();
			}
			
		} catch (Exception e) {
			
			try {
				close();
			} catch (Exception ex) {}
			
			ParseFileException pf = new ParseFileException(e);
			pf.setCodigoErro(ParseFileException.SQL_EXCEPTION);
			throw pf;
		}
	}
	

}
