package controle;

import java.io.FileOutputStream;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import Conexao.ZooKeeperConnection;
import SyncPrimitive.BarrierQueueLock.Barrier;
import bean.Cliente;
import bean.Conta;

public class ControleConta {

	private ZooKeeper zk;

	private ZooKeeperConnection conexao;

	public void criarConta(Cliente c, int agencia, int numConta, double saldo, double limite, String leaderPath) {
		Conta conta = new Conta(agencia, numConta, saldo, limite);
		c.setConta(conta);

		String caminho = leaderPath + "/Clientes/" + c.getCpf() + "/conta";
		String dados = c.getConta().getAgencia() + "," + c.getConta().getNumero() + "," + c.getConta().getSaldoDebito()
				+ "," + c.getConta().getLimiteCredito();

		try {
			conexao = new ZooKeeperConnection();
			zk = conexao.connect("localhost");
			zk.create(caminho, dados.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			conexao.close();

			FileOutputStream fos = new FileOutputStream(c.getCpf() + ".txt");
			String inicio = "Histórico de transações\r\n Cliente: " + c.getNome() + "\r\n CPF: " + c.getCpf()
					+ "\r\n Agência: " + conta.getAgencia() + "\r\n Conta: " + conta.getNumero();
			fos.write(inicio.getBytes());
			fos.close();

			System.out.println("Conta criada com sucesso!");

			Barrier barreira = new Barrier("localhost", "/Replicacao",
					3);
			barreira.barrierTest(barreira, leaderPath, c.toString(), dados, c, 1);
			
		} catch (Exception e) {
			System.out.println(e.getMessage()); // Catch error message
		}
	}
	
	public void replicarConta(String serverAtivo, String dados, Cliente c) {
		for (int i = 0; i < 5; i++) {
			try {
				conexao = new ZooKeeperConnection();
				zk = conexao.connect("localhost");
				if (Integer.parseInt(serverAtivo.substring(7)) != i) {
					String editPath = serverAtivo.substring(0, 7) + i
								+ "/Clientes/" + c.getCpf() + "/conta";
					zk.create(editPath, dados.getBytes(),
							ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
