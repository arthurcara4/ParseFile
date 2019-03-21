package br.com.accesstage.parsefile.validation.util; 

public class ConnType {
	public static final int CONN_POOL = 1;
	public static final int CONN_JDBC_THIN = 2;
    
    private String connStr;
    private int connType;
    private String user;
    private String pass;
    
    public ConnType(String pool) {
    	this.connType = CONN_POOL;
    	this.connStr = pool;
    }

    public ConnType(String connStr, String user, String pass){
        this.connType = CONN_JDBC_THIN;
        
        this.connStr = connStr;
        this.user = user;
        this.pass = pass;
    }

	public String getConnStr() {
		return connStr;
	}
	public void setConnStr(String connStr) {
		this.connStr = connStr;
	}
	public int getConnType() {
		return connType;
	}
	public void setConnType(int connType) {
		this.connType = connType;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}


}
