package controle;

import java.io.FileOutputStream;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import Conexao.ZooKeeperConnection;
import bean.Cliente;
import bean.Conta;


public class ControleConta {
	
	private ZooKeeper zk;
	
	private ZooKeeperConnection conexao;
	
	 // Method to create znode in zookeeper ensemble
	private void create(String path, byte[] data) throws 
	      KeeperException,InterruptedException {
	      zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,
	      CreateMode.PERSISTENT);
	}
	
	public void criarConta(Cliente c, int agencia, int numConta, double saldo, double limite){
		Conta conta = new Conta(agencia, numConta, saldo, limite);
		c.setConta(conta);
		
		String caminho = "/Clientes/"+c.getCpf()+"/conta";
		String dados = c.getConta().getAgencia()+","+c.getConta().getNumero()+","+c.getConta().getSaldoDebito()+","+c.getConta().getLimiteCredito();
		
		 try {
	         conexao = new ZooKeeperConnection();
	         zk = conexao.connect("localhost");
	         create(caminho, dados.getBytes()); // Create the data to the specified path
	         conexao.close();
	         
	         FileOutputStream fos = new FileOutputStream(c.getCpf() + ".txt");
	         String inicio = "Histórico de transações\r\n Cliente: " + c.getNome() + "\r\n CPF: " + c.getCpf() +  "\r\n Agência: " + conta.getAgencia() +  "\r\n Conta: " + conta.getNumero();
             fos.write(inicio.getBytes());
             fos.close();
	         
	         System.out.println("Conta criada com sucesso!");
	      } catch (Exception e) {
	         System.out.println(e.getMessage()); //Catch error message
	      }
	}

}
