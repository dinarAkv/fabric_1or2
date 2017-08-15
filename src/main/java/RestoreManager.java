import org.apache.commons.io.IOUtils;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.ChannelConfiguration;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import src.ByteManager;
import src.NewPK;

import java.io.*;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class RestoreManager {

    public static Channel Restore() throws Exception {
        Main.certificateFile = Paths.get(Main.SERTIFICATEPATH).toFile();
        Main.privateKeyFile = Paths.get(Main.PRIVATKEY).toFile();

        String certificate = new String(IOUtils.toByteArray(new FileInputStream(Main.certificateFile.getAbsolutePath())), "UTF-8");
    //    Properties prop1 = PropertiesManager.getProperties("C:\\Users\\agliullin\\Desktop\\idea projects\\fabric7\\src\\main\\java\\properties\\Hostnames.properties");

        Properties prop2 = PropertiesManager.getProperties("src\\main\\java\\properties\\data.properties");
      //  byte[] bytes = ByteManager.Convert("C:\\Users\\agliullin\\Desktop\\idea projects\\fabric7\\src\\main\\env\\channel\\crypto-config\\peerOrganizations\\org1.example.com\\ca\\ca.org1.example.com-cert.pem");

       // PrivateKey key = OpenChannel.getPrivateKeyFromBytes(ByteManager.Reverse(prop2.getProperty("bytes")));
     //  PrivateKey pk = ByteManager.getPrivateKeyFromBytes(IOUtils.toByteArray(new FileInputStream("C:\\Users\\agliullin\\Desktop\\idea projects\\fabric7\\src\\main\\env\\channel\\crypto-config\\peerOrganizations\\org1.example.com\\ca\\ca.org1.example.com-cert.pem")));

        System.out.println("");
        Main.privateKey = GetSerialize();
        Main.org1_peer_admin = new FCUser("Org1Admin");
        Main.org1_peer_admin.setMspId(Main.MSPID);

        Main.org1_peer_admin.setEnrollment(new FCEnrollment(Main.privateKey, certificate));

        Main.client = HFClient.createNewInstance();
        Main.client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        Main.client.setUserContext(Main.org1_peer_admin);

        Channel newChannel =      Main.client.newChannel("mychannel");

        Properties ordererProp = PropertiesManager.getProperties("src\\main\\java\\properties\\Orderer.properties");
        ordererProp.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[]{5L, TimeUnit.MINUTES});
        ordererProp.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[]{8L, TimeUnit.SECONDS});
        Main.orderer = Main.client.newOrderer("orderer.example.com", "grpc://" + Main.IP + ":7050", ordererProp);
newChannel.addOrderer(Main.orderer);

        Properties peerProp = PropertiesManager.getProperties("src\\main\\java\\properties\\Peer.properties");
        peerProp.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 9000000);
        Main.peer = Main.client.newPeer("peer0.org1.example.com", "grpc://" + Main.IP + ":7051", peerProp);
newChannel.addPeer(Main.peer);
        Properties ehProperties=PropertiesManager.getProperties("src\\main\\java\\properties\\EventHub.properties");
        ehProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[]{5L, TimeUnit.MINUTES});
        ehProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[]{8L, TimeUnit.SECONDS});
        OpenChannel.eventHub = Main.client.newEventHub("peer0.org1.example.com", "grpc://" + Main.IP + ":7053", ehProperties);
newChannel.addEventHub(Main.client.newEventHub("peer0.org1.example.com", "grpc://" + Main.IP + ":7053", ehProperties));

        ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File(Main.CHANELTX));
        if(Main.client==null) System.out.println("client is null");


        newChannel.initialize();
        System.out.println(newChannel.getName() + "restored!");
        return newChannel;

    }


    public static void SetSerialize(PrivateKey newPK) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("src\\main\\java\\properties\\PK.dat"));
        out.writeObject(newPK);
        out.close();
    }


    public static PrivateKey GetSerialize() throws IOException, ClassNotFoundException {
        ObjectInputStream in =  new ObjectInputStream (new FileInputStream("src\\main\\java\\properties\\PK.dat"));
      return  (PrivateKey) in.readObject();

    }
}
