package controle;

import java.io.FileOutputStream;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import Conexao.ZooKeeperConnection;
import bean.Cliente;

public class ControleCliente {
	private ZooKeeper zk;

	private ZooKeeperConnection conexao;

	// Method to create znode in zookeeper ensemble
	private void create(String path, byte[] data) throws KeeperException, InterruptedException {
		zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
	}

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
			create(caminho, dados.getBytes()); // Create the data to the
												// specified path
			conexao.close();
			System.out.println("Cliente adicionado com sucesso");

			for (int i = 0; i < 3; i++) {
				zk = conexao.connect("localhost");
				if (Integer.parseInt(leaderPath.substring(7)) != i) {
					String editPath = leaderPath.substring(0, 7) + i + "/Clientes/" + c.getCpf();
					zk.create(editPath, dados.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

				}
			}

		} catch (Exception e) {
			System.out.println(e.getMessage()); // Catch error message
		}
	}

}
