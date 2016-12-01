package controle;

import java.io.IOException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.InputMismatchException;
import java.util.Scanner;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import Conexao.ZooKeeperConnection;
import SyncPrimitive.BarrierQueueLock.Lock;
import SyncPrimitive.BarrierQueueLock.Queue;
import bean.Cliente;
import bean.Transacao;

public class ControleGeral {

	@SuppressWarnings({ "deprecation", "static-access" })
	public void executa(String leaderAddress, String leaderPath) throws KeeperException, InterruptedException, IOException {
		ZooKeeperConnection zkConnect = new ZooKeeperConnection();
		ZooKeeper zk;
		
		Scanner sc = new Scanner(System.in);
		System.out.println("Escolha a operação desejada: ");
		System.out.println("Digite 1 para inserir cliente e conta");
		System.out.println("Digite 2 para processar transação");
		System.out.println("Digite 3 para processar transações pendentes");
		System.out.println("Digite 4 para sair");
		
		int opcao= 0;
		
		//Testa se os inputs sao do tipo certo (InputMismatchException). Esta em volta de todo o switch
		try{
			zk = zkConnect.connect("localhost");
			opcao = sc.nextInt();
		

		switch (opcao) {
		case 1:
			ControleCliente controleCliente = new ControleCliente();
			ControleConta controleConta = new ControleConta();

			System.out.println("Insira o nome do cliente");
			String nome = sc.next();

			System.out.println("Insira o cpf do cliente");
			String cpf = sc.next();
			
			//Verifica se o cliente já possui conta
			Stat test1= zk.exists(leaderPath+"/Clientes/"+cpf, false);
			if(test1!=null){
				System.out.println("Esse CPF já possui conta");
				this.executa(leaderAddress, leaderPath);
			}

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

			controleConta.criarConta(c, agencia, conta, saldo, limite,leaderPath);

			break;
		case 2:
			/**
			 * Processa transações requisitadas pelo usuário.
			 * Caso esta transação seja requisitada após o horário permitido,
			 * a mesma é armazenada em uma fila para ser executada no dia seguinte, 
			 * mediante processamento do usuário
			 */
			try {

				Queue filaEscrita = new Queue(leaderAddress, "/filaTransacao",leaderPath);
				Lock lock = new Lock(leaderAddress, "/Transacoes", 10000,leaderPath);

				Transacao t = new Transacao();
				t.setCliente(new Cliente());

				System.out.println("Insira o código da transação");
				t.setCodigo(sc.nextInt());

				System.out.println("Insira o cpf do cliente");
				String tempCPF = sc.next();
				
				//Verfica se a conta do CPF existe
				Stat test2= zk.exists(leaderPath+"/Clientes/" + tempCPF, false);
				if(test2==null){
					System.out.println("Crie uma conta antes de tentar realizar uma transação!");
					this.executa(leaderAddress, leaderPath);
				}else t.getCliente().setCpf(tempCPF);

				System.out
						.println("Insira o tipo da Operação, digite:\r\n 1 para Saque\r\n"
								+ "2 para Depósitos\r\n"
								+ "3 para Compra-Débito\r\n"
								+ "4 para Compra-Crédito");
				
				//Verifica se o input é um dos números de operação acima
				int temp = sc.nextInt();
				if(temp>0 && temp<5) t.setOperacao(temp);
				else {
					System.out.println("Operação Não identificada");
					ControleGeral controle = new ControleGeral();
					controle.executa("localhost", leaderPath);
				}

				System.out.println("Insira o valor");
				t.setValor(sc.nextDouble());

				System.out.println("Insira um descrição para a transação");
				if (sc.hasNext())
					t.setDescricao(sc.next());

				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
				Date dataAtual = new Date();

				String data = formatter.format(dataAtual);
				t.setData(data);

				if (dataAtual.getHours() < 19 && dataAtual.getHours() > 8)
					lock.lockTest(lock, t);
				else
					filaEscrita.queueTest(filaEscrita, "p", t, 0);
			}

			catch (Exception e) {
				e.printStackTrace();
			}

			break;
		case 3:
			/**
			 * Executa transações pendentes, caso existam.
			 */
			Queue filaLeitura = new Queue(leaderAddress, "/filaTransacao", leaderPath);
			filaLeitura.queueTest(filaLeitura, "c", new Transacao(), 2);

			break;
		case 4:
			System.exit(0);
			break;
		default:
			System.out.println("Opção inválida");
			this.executa(leaderAddress, leaderPath);
			break;
		}
		
		} catch(InputMismatchException e){
			System.out.println("Input inválido!");
			this.executa(leaderAddress, leaderPath);
		}

	}

}
