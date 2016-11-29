package controle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import SyncPrimitive.BarrierQueueLock.Barrier;
import SyncPrimitive.BarrierQueueLock.Lock;
import SyncPrimitive.BarrierQueueLock.Queue;
import bean.Cliente;
import bean.Transacao;

public class ControleGeral {
	
	public void executa(String leaderAddress, String leaderPath){
		Scanner sc = new Scanner(System.in);
		System.out.println("Escolha a operação desejada: ");
		System.out.println("Digite 1 para inserir cliente e conta ");
		System.out.println("Digite 2 para inserir processar transação ");
		System.out.println("Digite 3 para HUE ");
		System.out.println("Digite 4 para processar transações pendentes ");

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

			Cliente c = controleCliente.criarCliente(nome, cpf, leaderPath);

			System.out.println("Insira a agencia ");
			int agencia = sc.nextInt();
			System.out.println("Insira o numero da conta");
			int conta = sc.nextInt();
			System.out.println("Insira o saldo");
			double saldo = sc.nextDouble();

			System.out.println("Insira o limite");
			double limite = sc.nextDouble();

			controleConta.criarConta(c, agencia, conta, saldo, limite, leaderPath);
			// Falta tentar setar um watcher pra identificar alteração nestes
			// znodes

			break;
		case 2:

			// Executa a transação utilizando queues e locks
			try {

				Queue filaEscrita = new Queue(leaderAddress, "/filaTransacao");
				Lock lock = new Lock(leaderAddress, "/Transacoes", 10000);
				Barrier barreira = null;
				int size = 0;
				System.out.println("Deseja fazer múltiplas transações");
				if (sc.next().toUpperCase().equals("S")) {
					System.out.println("Digite o número de transações a serem processadas");
					size = sc.nextInt();
					barreira = new Barrier(leaderAddress, "/MultiTransacoes", size);
				}

				for (int i = 0; i < size; i++) {

					Transacao t = new Transacao();
					t.setCliente(new Cliente());

					System.out.println("Insira o código da transação");
					t.setCodigo(sc.nextInt());

					System.out.println("Insira o cpf do cliente");
					t.getCliente().setCpf(sc.next());

					System.out.println("Insira o tipo da Operação, digite:\r\n 1 para Saque\r\n"
							+ "2 para Depósitos\r\n" + "3 para Compra-Débito\r\n" + "4 para Compra-Crédito");
					t.setOperacao(sc.nextInt());

					System.out.println("Insira o valor");
					t.setValor(sc.nextDouble());

					System.out.println("Insira um descrição para a transação");
					if (sc.hasNext())
						t.setDescricao(sc.next());

					SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
					Date dataAtual = new Date();

					String data = formatter.format(dataAtual);
					t.setData(data);

					System.out.println(t);

					if (size > 0) {
						System.out.println("Processando transação " + (i+1));
						if (dataAtual.getHours() < 19 && dataAtual.getHours() > 8) {
							barreira.barrierTest(lock, barreira, t);
						} else {
							barreira.barrierTest(filaEscrita, barreira, t);
						}
					} else {

						if (dataAtual.getHours() < 19 && dataAtual.getHours() > 8)
							lock.lockTest(lock, t);
						else
							filaEscrita.queueTest(filaEscrita, "p", t, 0);
					}

				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/*
			 * Armazena tbm a transação através do barrier para aquele cliente
			 * (obs: cliente diferentes znodes de barrier diferentes) após 3
			 * transações o barrier é liberado para checagem de autenticidade e
			 * calcular média de gastos e outras métricas para detectar fraudes
			 */
			break;
		case 3:
			/*
			 * Pegar informações de um usuário - dados armazenados no znode cpf
			 * e conta
			 */
			break;
		case 4:
			/*
			 * Executar transações pendentes
			 */
			Queue filaLeitura = new Queue(leaderAddress, "/filaTransacao");
			filaLeitura.queueTest(filaLeitura, "c", new Transacao(), 2);

			break;

		}

	}

}
