package controle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import SyncPrimitive.BarrierQueueLock.Lock;
import SyncPrimitive.BarrierQueueLock.Queue;
import bean.Cliente;
import bean.Transacao;

public class ControleGeral {

	public void executa(String leaderAddress, String leaderPath) {
		Scanner sc = new Scanner(System.in);
		System.out.println("Escolha a operação desejada: ");
		System.out.println("Digite 1 para inserir cliente e conta");
		System.out.println("Digite 2 para processar transação ");
		System.out.println("Digite 3 para processar transações pendentes");

		int opcao = sc.nextInt();

		switch (opcao) {
		case 1:
			ControleCliente controleCliente = new ControleCliente();
			ControleConta controleConta = new ControleConta();

			System.out.println("Insira o nome do cliente");
			String nome = sc.next();

			System.out.println("Insira o cpf do cliente");
			String cpf = sc.next();

			System.out.println(leaderPath);
			Cliente c = controleCliente.criarCliente(nome, cpf, leaderPath);

			System.out.println("Insira a agencia ");
			int agencia = sc.nextInt();
			System.out.println("Insira o numero da conta");
			int conta = sc.nextInt();
			System.out.println("Insira o saldo");
			double saldo = sc.nextDouble();

			System.out.println("Insira o limite");
			double limite = sc.nextDouble();

			controleConta.criarConta(c, agencia, conta, saldo, limite,
					leaderPath);
			// Falta tentar setar um watcher pra identificar alteraÃ§Ã£o nestes
			// znodes

			break;
		case 2:

			// Executa a transaÃ§Ã£o utilizando queues e locks
			try {

				Queue filaEscrita = new Queue(leaderAddress, "/filaTransacao",
						leaderPath);
				Lock lock = new Lock(leaderAddress, "/Transacoes", 10000,
						leaderPath);

				Transacao t = new Transacao();
				t.setCliente(new Cliente());

				System.out.println("Insira o código da transação");
				t.setCodigo(sc.nextInt());

				System.out.println("Insira o cpf do cliente");
				t.getCliente().setCpf(sc.next());

				System.out
						.println("Insira o tipo da Operação, digite:\r\n 1 para Saque\r\n"
								+ "2 para Depósitos\r\n"
								+ "3 para Compra-Débito\r\n"
								+ "4 para Compra-Crédito");
				t.setOperacao(sc.nextInt());

				System.out.println("Insira o valor");
				t.setValor(sc.nextDouble());

				System.out.println("Insira um descrição para a transação");
				if (sc.hasNext())
					t.setDescricao(sc.next());

				SimpleDateFormat formatter = new SimpleDateFormat(
						"dd/MM/yyyy hh:mm:ss");
				Date dataAtual = new Date();

				String data = formatter.format(dataAtual);
				t.setData(data);

				System.out.println(t);

				if (dataAtual.getHours() < 19 && dataAtual.getHours() > 8)
					lock.lockTest(lock, t);
				else
					filaEscrita.queueTest(filaEscrita, "p", t, 0);

			}

			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/*
			 * Armazena tbm a transaÃ§Ã£o atravÃ©s do barrier para aquele
			 * cliente (obs: cliente diferentes znodes de barrier diferentes)
			 * apÃ³s 3 transaÃ§Ãµes o barrier Ã© liberado para checagem de
			 * autenticidade e calcular mÃ©dia de gastos e outras mÃ©tricas para
			 * detectar fraudes
			 */
			break;
		case 3:
			/*
			 * Executar transaÃ§Ãµes pendentes
			 */
			Queue filaLeitura = new Queue(leaderAddress, "/filaTransacao",
					leaderPath);
			filaLeitura.queueTest(filaLeitura, "c", new Transacao(), 2);

			break;

		}

	}

}
