package main;

import java.util.Scanner;

import SyncPrimitive.BarrierQueueLock.Queue;
import bean.Cliente;
import bean.Transacao;
import controle.ControleCliente;
import controle.ControleConta;
import controle.ControleTransacao;

public class Main {

/*
 * Ideia Leader Election - Ao rodar o prog, as infos são guardadas em znodes diferentes, mas só 1 é o líder definindo a leitura e escrita de infos
 * e sendo responsavel por replicar as infos para os outros znodes.
 * se este znode "morre" um novo deve assumir a responsabilidade de leitura, escrita e replicacao
 */
	public static void main(String[] args) {
		//Para leader election, criar X znodes ao iniciar o prog
		
		String leaderAddress = "localhost";
		
		Scanner sc = new Scanner(System.in);
		System.out.println("Escolha a operação desejada: ");
		System.out.println("Digite 1 para inserir cliente e conta ");
		System.out.println("Digite 2 para inserir processar transação ");
		int opcao = sc.nextInt();

		switch (opcao) {
		case 1:
			ControleCliente controleCliente = new ControleCliente();
			ControleConta controleConta = new ControleConta();			
			ControleTransacao controleTransacao = new ControleTransacao();
			
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
			//Falta tentar setar um watcher pra identificar alteração nestes znodes
			
			break;
		case 2:
			
			//Executa a transação utilizando queues e locks 
			
			//Barrier barreira = new Barrier();
			Queue fila = new Queue(leaderAddress, "/filaTransacao");
			
			Transacao t = new Transacao();
			System.out.println("Insira o codigo");
			t.setCodigo(sc.nextInt());
			t.setCliente(new Cliente());
			
			System.out.println("Insira o cpf do cliente");
			t.getCliente().setCpf(sc.next());
			
			System.out.println("Insira o valor");
			t.setValor(sc.nextDouble());
			
			fila.queueTest(fila, "p", t, 0);
			
			/* 
			 * Armazena tbm a transação através do barrier para aquele cliente (obs: cliente diferentes znodes de barrier diferentes)
			 *  após 3 transações o barrier é liberado para checagem de autenticidade e calcular média de gastos e outras métricas para detectar fraudes
			 */
			break;
		case 3:
			/*
			 * Pegar informações de um usuário - dados armazenados no znode cpf e conta
			 */
			break;

		}

	}

}
