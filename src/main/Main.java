package main;

import cliente.Cliente;
import conta.Conta;
import controle.CriarCliente;
import controle.CriarConta;

public class Main {

	public static void main(String[] args) {
		Cliente c = new Cliente("Joao", "11122233355");
		CriarCliente criarCliente = new CriarCliente();
		criarCliente.criarCliente(c);
		
		Conta conta = new Conta();
		conta.setAgencia(1);
		conta.setNumero(1);
		conta.setSaldoDebito(0.00);
		conta.setLimiteCredito(500.00);
		conta.setSenha("12345");
		
		c.setConta(conta);
		
		CriarConta criarConta =  new CriarConta();
		
		criarConta.criarConta(c);
		
		

	}

}
