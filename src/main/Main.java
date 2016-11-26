package main;

import java.util.Scanner;

import cliente.Cliente;
import conta.Conta;
import controle.ControleCliente;
import controle.ControleConta;

public class Main {

/*
 * Ideia Leader Election - Ao rodar o prog, as infos s�o guardadas em znodes diferentes, mas s� 1 � o l�der definindo a leitura e escrita de infos
 * e sendo responsavel por replicar as infos para os outros znodes.
 * se este znode "morre" um novo deve assumir a responsabilidade de leitura, escrita e replicacao
 */
	public static void main(String[] args) {
		//Para leader election, criar X znodes ao iniciar o prog
		Scanner sc = new Scanner(System.in);
		System.out.println("Escolha a opera��o desejada: ");
		System.out.println("Digite 1 para inserir cliente e conta ");
		System.out.println("Digite 2 para inserir processar transa��o ");
		int opcao = sc.nextInt();

		switch (opcao) {
		case 1:
			ControleCliente controleCliente = new ControleCliente();
			ControleConta controleConta = new ControleConta();
			
			System.out.println("Insira o nome do cliente");
			String nome = sc.next();
			
			System.out.println("Insira o cpf do cliente");
			String cpf = sc.next();
			
			Cliente c = controleCliente.criarCliente(nome, cpf);
			
			System.out.println("Insira a agencia ");
			int agencia = sc.nextInt();
			System.out.println("Insira o numero da conta");
			int conta = sc.nextInt();
			System.out.println("Insira o saldo");
			double saldo = sc.nextDouble();
			
			System.out.println("Insira o limite");
			double limite = sc.nextDouble();
			
			controleConta.criarConta(c, agencia, conta, saldo, limite);
			//Falta tentar setar um watcher pra identificar altera��o nestes znodes
			break;
		case 2:
			//Executa a transa��o utilizando queues e locks 
			
			/* 
			 * Armazena tbm a transa��o atrav�s do barrier para aquele cliente (obs: cliente diferentes znodes de barrier diferentes)
			 *  ap�s 3 transa��es o barrier � liberado para checagem de autenticidade e calcular m�dia de gastos e outras m�tricas para detectar fraudes
			 */
			break;
		case 3:
			/*
			 * Pegar informa��es de um usu�rio - dados armazenados no znode cpf e conta
			 */
			break;

		}
		

		

		Conta conta = new Conta();
		conta.setAgencia(1);
		conta.setNumero(1);
		conta.setSaldoDebito(0.00);
		conta.setLimiteCredito(500.00);
		conta.setSenha("12345");

		ControleConta criarConta = new ControleConta();


	}

}
