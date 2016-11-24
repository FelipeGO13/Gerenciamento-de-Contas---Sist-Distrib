package conta;

public class Conta {
	
	private Integer agencia;
	private Integer numero;
	private Double saldoDebito;
	private Double limiteCredito;
	private String senha;
	
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
	
	

}
