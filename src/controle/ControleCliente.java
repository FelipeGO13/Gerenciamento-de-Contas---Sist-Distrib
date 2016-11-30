package controle;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import Conexao.ZooKeeperConnection;
import bean.Cliente;

public class ControleCliente {
	private ZooKeeper zk;

	private ZooKeeperConnection conexao;

	public Cliente criarCliente(String nome, String cpf, String leaderPath) {
		Cliente c = new Cliente(nome, cpf);
		znodeCliente(c, leaderPath);
		return c;
	}

	public void znodeCliente(Cliente c, String leaderPath) {

		String caminho = leaderPath + "/Clientes/" + c.getCpf();
		String dados = c.getNome() + " " + c.getCpf();

		try {
			conexao = new ZooKeeperConnection();
			zk = conexao.connect("localhost");
			zk.create(caminho, dados.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);
			System.out.println("Cliente adicionado com sucesso");

		} catch (Exception e) {
			System.out.println(e.getMessage()); // Catch error message
		}
	}

	public void replicarCliente(String serverAtivo, String dados, Cliente c) {
		conexao = new ZooKeeperConnection();
		try {
			zk = conexao.connect("localhost");
			for (int i = 0; i < 5; i++) {

				if (Integer.parseInt(serverAtivo.substring(7)) != i) {
					String editPath = serverAtivo.substring(0, 7) + i
							+ "/Clientes/" + c.getCpf();

					zk.create(editPath, dados.getBytes(),
							ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
