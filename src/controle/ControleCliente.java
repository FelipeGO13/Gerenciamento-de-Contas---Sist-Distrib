package controle;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import Conexao.ZooKeeperConnection;
import cliente.Cliente;

public class ControleCliente {
private ZooKeeper zk;
	
	private ZooKeeperConnection conexao;
	
	 // Method to create znode in zookeeper ensemble
	private void create(String path, byte[] data) throws 
	      KeeperException,InterruptedException {
	      zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,
	      CreateMode.PERSISTENT);
	}
	
	public Cliente criarCliente(String nome, String cpf){
		Cliente c = new Cliente(nome, cpf);
		znodeCliente(c);
		return c;
	}
	
	public void znodeCliente(Cliente c){
		
		String caminho = "/Clientes/"+c.getCpf();
		String dados = c.getNome()+" "+c.getCpf();
		
		 try {
	         conexao = new ZooKeeperConnection();
	         zk = conexao.connect("localhost");
	         create(caminho, dados.getBytes()); // Create the data to the specified path
	         conexao.close();
	         System.out.println("Cliente adicionado com sucesso");
	      } catch (Exception e) {
	         System.out.println(e.getMessage()); //Catch error message
	      }
	}

}
