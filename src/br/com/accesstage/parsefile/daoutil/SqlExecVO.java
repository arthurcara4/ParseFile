package br.com.accesstage.parsefile.daoutil;


public class SqlExecVO {
	public static final int T_BACTH = 1;
	public static final int T_PROC = 2;
	
	private String id;
	private String idGrupo;
	private String sql;
	private String parametros;
	private String pre;
	private String pos;
	private String dataSource;
	private int    buffer;
	private int    tipo;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		//if (tipo == T_BACTH) this.id = new StringBuffer("b_").append(id).toString();
		//if (tipo == T_PROC) this.id = new StringBuffer("p_").append(id).toString();
		
		//if (Valida.isEmpty(id)) this.id = id;
		this.id = id;
	}
	public String getIdGrupo() {
		return idGrupo;
	}
	public String getIdGrupoFmt() {
		return new StringBuffer(",").append(idGrupo).append(",").toString();
	}
	public void setIdGrupo(String idGrupo) {
		this.idGrupo = idGrupo;
	}
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public String getParametros() {
		return parametros;
	}
	public void setParametros(String parametros) {
		this.parametros = parametros;
	}
	public String getPre() {
		return pre;
	}
	public void setPre(String pre) {
		this.pre = pre;
	}
	public String getPos() {
		return pos;
	}
	public void setPos(String pos) {
		this.pos = pos;
	}
	public String getDataSource() {
		return dataSource;
	}
	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}
	public int getBuffer() {
		return buffer;
	}
	public void setBuffer(int buffer) {
		this.buffer = buffer;
	}
	public int getTipo() {
		return tipo;
	}
	public void setTipo(int tipo) {
		this.tipo = tipo;
	}
	public String getTipoDesc() {
		if (tipo == T_BACTH) return "BATCH";
		if (tipo == T_PROC) return "PROC";
		return "";
	}
	
	
	
}
