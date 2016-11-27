package bean;

public class Conta {
	
	private Integer agencia;
	private Integer numero;
	private Double saldoDebito;
	private Double limiteCredito;
	private String senha;
	
	public Conta() {
		// TODO Auto-generated constructor stub
	}
	
	public Conta(int agencia, int numConta, double saldo, double limite) {
		this.agencia = agencia;
		this.numero = numConta;
		this.saldoDebito = saldo;
		this.limiteCredito = limite;
	}
	
	public Integer getAgencia() {
		return agencia;
	}
	public void setAgencia(Integer agencia) {
		this.agencia = agencia;
	}
	public Integer getNumero() {
		return numero;
	}
	public void setNumero(Integer numero) {
		this.numero = numero;
	}
	public Double getSaldoDebito() {
		return saldoDebito;
	}
	public void setSaldoDebito(Double saldoDebito) {
		this.saldoDebito = saldoDebito;
	}
	public Double getLimiteCredito() {
		return limiteCredito;
	}
	public void setLimiteCredito(Double limiteCredito) {
		this.limiteCredito = limiteCredito;
	}
	public String getSenha() {
		return senha;
	}
	public void setSenha(String senha) {
		this.senha = senha;
	}
	
	@Override
	public String toString() {
		return agencia+","+numero+","+saldoDebito+","+limiteCredito;
	}

}
