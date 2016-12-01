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
		System.out.println("Escolha a opera��o desejada: ");
		System.out.println("Digite 1 para inserir cliente e conta");
		System.out.println("Digite 2 para processar transa��o");
		System.out.println("Digite 3 para processar transa��es pendentes");
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
			
			//Verifica se o cliente j� possui conta
			Stat test1= zk.exists(leaderPath+"/Clientes/"+cpf, false);
			if(test1!=null){
				System.out.println("Esse CPF j� possui conta");
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
			 * Processa transa��es requisitadas pelo usu�rio.
			 * Caso esta transa��o seja requisitada ap�s o hor�rio permitido,
			 * a mesma � armazenada em uma fila para ser executada no dia seguinte, 
			 * mediante processamento do usu�rio
			 */
			try {

				Queue filaEscrita = new Queue(leaderAddress, "/filaTransacao",leaderPath);
				Lock lock = new Lock(leaderAddress, "/Transacoes", 10000,leaderPath);

				Transacao t = new Transacao();
				t.setCliente(new Cliente());

				System.out.println("Insira o c�digo da transa��o");
				t.setCodigo(sc.nextInt());

				System.out.println("Insira o cpf do cliente");
				String tempCPF = sc.next();
				
				//Verfica se a conta do CPF existe
				Stat test2= zk.exists(leaderPath+"/Clientes/" + tempCPF, false);
				if(test2==null){
					System.out.println("Crie uma conta antes de tentar realizar uma transa��o!");
					this.executa(leaderAddress, leaderPath);
				}else t.getCliente().setCpf(tempCPF);

				System.out
						.println("Insira o tipo da Opera��o, digite:\r\n 1 para Saque\r\n"
								+ "2 para Dep�sitos\r\n"
								+ "3 para Compra-D�bito\r\n"
								+ "4 para Compra-Cr�dito");
				
				//Verifica se o input � um dos n�meros de opera��o acima
				int temp = sc.nextInt();
				if(temp>0 && temp<5) t.setOperacao(temp);
				else {
					System.out.println("Opera��o N�o identificada");
					ControleGeral controle = new ControleGeral();
					controle.executa("localhost", leaderPath);
				}

				System.out.println("Insira o valor");
				t.setValor(sc.nextDouble());

				System.out.println("Insira um descri��o para a transa��o");
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
			 * Executa transa��es pendentes, caso existam.
			 */
			Queue filaLeitura = new Queue(leaderAddress, "/filaTransacao", leaderPath);
			filaLeitura.queueTest(filaLeitura, "c", new Transacao(), 2);

			break;
		case 4:
			System.exit(0);
			break;
		default:
			System.out.println("Op��o inv�lida");
			this.executa(leaderAddress, leaderPath);
			break;
		}
		
		} catch(InputMismatchException e){
			System.out.println("Input inv�lido!");
			this.executa(leaderAddress, leaderPath);
		}

	}

}
