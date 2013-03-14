package edu.isi.policy.adapt;

import java.io.IOException;
import java.net.URISyntaxException;

import edu.isi.policy.entity.Transfer;
import edu.isi.policy.exception.EntityNotFoundException;

import edu.isi.policy.util.TransferList;

/**
 * This class demonstrates how to use the Policy Module
 * 
 * @author David Smith
 * 
 */
public class DemoAdaptClient {

    public static void main(String[] args) {

        if (args.length >= 1
                && ("-help".equals(args[0]) || "-h".equals(args[0]))) {
            System.err.println("USAGE: " + DemoAdaptClient.class.getName()
                    + " [properties file]");
            System.exit(0);
        }

        // instantiate the policy module with the properties file and PTM as
        // arguments
        PolicyModule policyModule = null;
        try {
            if(args.length >= 1) {
                // Properties file contents determine the implementation of policy
                // module inner classes,
                // along with default parameters, host-pair maximums, etc
                policyModule = new PolicyModule(args[0]);
            }
            else {
                policyModule = new PolicyModule();
            }
        } catch (IOException e) {
            // properties file cannot be read
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Starting demo...");

        // client creates a new transfer object for a 3rd-party
        // GridFTP transfer that it wants advice on
        Transfer initialTransfer = null;
        try {
            initialTransfer = new Transfer(
                    "gsiftp://server1.isi.edu/home/test/set1/",
                    "gsiftp://client1.isi.edu/home/test/set1/");
        } catch (URISyntaxException e) {
            // bad URI syntax
            e.printStackTrace();
            System.exit(1);
        }

        // tells policy the size of the data transfer
        initialTransfer.setProperty(Constants.DATA_VOLUME_PROPERTY, "1000024");

        System.out.println("Requesting advice on transfer "
                + initialTransfer.getSource() + " -> "
                + initialTransfer.getDestination());

        // requests initial advice from the policy module for the transfer
        initialTransfer = policyModule.addTransfer(initialTransfer);

        // unique identifier of the transfer for future reference
        System.out.println("Transfer inserted into policy, id: "
                + initialTransfer.getId());

        // advice for our recommended instant max transfer streams
        System.out.println("Initial transfer max transfer streams: "
                + initialTransfer.getProperty(Constants.MAX_STREAMS_PROPERTY));

        // advice for our recommended instant max rate
        System.out.println("Initial transfer max rate: "
                + initialTransfer.getProperty(Constants.MAX_RATE_PROPERTY));

        // here is where ADT algorithm runs and comes up with the actual rate
        // and streams it will use
        // based on initial recommendations. client starts to run the transfer
        // asynchronously and tells policy
        // the values it chose
        // for example, ADT chose 3 streams
        initialTransfer.getProperties().clear();
        initialTransfer.setProperty(Constants.ADJUSTED_STREAMS_PROPERTY,
                Integer.toString(3));

        // for example, ADT chose 150 MB/s rate
        initialTransfer.setProperty(Constants.ADJUSTED_RATE_PROPERTY,
                Float.toString(150));

        // notify policy of these chosen values
        try {
            initialTransfer = policyModule.updateTransfer(
                    initialTransfer.getId(), initialTransfer);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // after adjustment timer expires, the client wants new recommendations
        // from policy
        initialTransfer.getProperties().clear();

        try {
            initialTransfer = policyModule.updateTransfer(
                    initialTransfer.getId(), initialTransfer);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        

        // Dump transfers
        _dumpTransfers(policyModule);
        
        // new maximums are sent back from policy based on aggregated resources
        System.out.println("Interval transfer max transfer streams: "
                + initialTransfer.getProperty(Constants.MAX_STREAMS_PROPERTY));
        System.out.println("Interval transfer max rate: "
                + initialTransfer.getProperty(Constants.MAX_RATE_PROPERTY));
        
        // Dump transfers
        _dumpTransfers(policyModule);

        // client calls ADT again with recommendations, notifies policy of its
        // actual adjusted streams

        // adjustments continue until transfer is completed.
        initialTransfer.getProperties().clear();
        initialTransfer.setProperty(Constants.STATUS_PROPERTY,
                Constants.COMPLETED_STATUS);

        // notify policy that transfer completed so that aggregated resources
        // are freed up for other transfers
        try {
            policyModule.updateTransfer(initialTransfer.getId(),
                    initialTransfer);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Dump transfers
        _dumpTransfers(policyModule);
        
        System.out.println("Demo completed!");
    }
    
    private static void _dumpTransfers(PolicyModule policyModule) {
        System.out.println("Dumping transfers: ");
        TransferList transfers = policyModule.getTransfers();
        for (Transfer transfer: transfers) {
            System.out.println(transfer);
        }
    }
}
