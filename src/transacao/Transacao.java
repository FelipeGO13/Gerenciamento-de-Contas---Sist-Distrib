package transacao;

import cliente.Cliente;

public class Transacao {

	private Cliente cliente;
	private int operacao;
	private Double valor;


	public Transacao() {

	}
	
	public Transacao(Cliente cliente, int operacao, Double valor) {
		this.cliente = cliente;	
		this.operacao = operacao;
		this.valor = valor;
	}
	
	public Cliente getCliente(){
		return cliente;
	}
	
	public void setCliente(Cliente cliente){
		this.cliente = cliente;
	}
	
	public int getOperacao(){
		return operacao;
	}
	
	public void setOperacao(int operacao){
		this.operacao = operacao;
	}
	
	public Double getValor(){
		return valor;
	}
	
	public void setValor(Double valor){
		this.valor = valor;
	}
	

	
	
}
