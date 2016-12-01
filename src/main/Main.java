package main;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import Conexao.ZooKeeperConnection;
import SyncPrimitive.LeaderElection;

public class Main {

	public static void main(String[] args) {

		String address = "localhost";
		ZooKeeperConnection zkConnect = new ZooKeeperConnection();
		ZooKeeper zk;
		/**
		 * Cria, caso não existam, znodes necessários para execução do programa
		 */
		try {
			zk = zkConnect.connect(address);
			for (int i = 0; i < 7; i++) {
				Stat s1 = zk.exists("/Server" + i, false);
				if (s1 == null) {
					zk.create("/Server" + i, new Integer(i).toString()
							.getBytes(), Ids.OPEN_ACL_UNSAFE,
							CreateMode.PERSISTENT);
					zk.create("/Server" + i + "/Clientes", new byte[0],
							Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
			}
			Stat s2 = zk.exists("/Replicacao",  false);
			if(s2 == null){
				zk.create("/Replicacao" , new byte[0],Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		/**
		 * Programa elege um znode para servir de "servidor" ativo e assim iniciar o
		 * processamento das operações.
		 */
		LeaderElection leader = new LeaderElection(address);
		leader.leaderElection(address);

	}

}
