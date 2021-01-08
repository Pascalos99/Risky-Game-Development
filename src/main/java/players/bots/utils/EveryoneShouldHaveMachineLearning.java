package players.bots.utils;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @author steven kelk (actually just me, pascal)
 *
 */
public class EveryoneShouldHaveMachineLearning {

	public static final String networkPath = NeuralNetwork.networkPath;
	
	private static final String ANN_FILE_NAME = "version-";
	
	public static void copyNetwork(String network_name, String new_name) {
		saveNetwork(loadNetwork(network_name), new_name);
	}
	
	public static boolean saveNetwork(NeuralNetwork net, String name) {
		return saveNetwork(new NeuralNetwork[] {net}, name);
	}
	public static boolean saveNetwork(NeuralNetwork[] net, String name) {
		return overrideNetwork(net, name, getLatestVersion(name) + 1);
	}
	public static boolean overrideNetwork(NeuralNetwork net, String name, int version) {
		return overrideNetwork(new NeuralNetwork[] {net}, name, version);
	}
	public static boolean overrideNetwork(NeuralNetwork[] net, String name, int version) {
		File folder = new File(networkPath+name);
		folder.mkdirs();
		File file = new File(networkPath+name+File.separator+ANN_FILE_NAME+String.format("%06d", version)+".network");
    	try {
			NeuralNetwork.storeToFile(net, file);
		} catch (IOException e) {
			System.err.println("Something went wrong while saving network \""+name+"\" version "+version);
			e.printStackTrace();
			return false;
		}
    	return true;
	}
	
	public static boolean exists(String network_name) {
		File folder = new File(networkPath+network_name);
		if (!folder.exists()) return false;
		return getLatestVersion(network_name) != -1;
	}
	public static boolean exists(String network_name, int version) {
		if (version < 0) return false;
		File folder = new File(networkPath+network_name);
		if (!folder.exists()) return false;
		return new File(networkPath+network_name+File.separator+ANN_FILE_NAME+version).exists();
	}
	
	public static NeuralNetwork loadNetwork(String name) throws NetworkIOException {
		return loadNetworks(name)[0];
	}
	public static NeuralNetwork[] loadNetworks(String name) throws NetworkIOException {
		if (getLatestVersion(name) == -1) throw new NetworkIOException("ANN with name \""+name+"\" does not exist");
		return loadNetworks(name, getLatestVersion(name));
	}
	public static NeuralNetwork loadNetwork(String name, int version) throws NetworkIOException {
		return loadNetworks(name, version)[0];
	}
	public static NeuralNetwork[] loadNetworks(String name, int version) throws NetworkIOException {
		if (version < 0) throw new NetworkIOException("failed to load ANN (ID non-valid)");
    	File folder = new File(networkPath+name);
    	File found = null;
    	folder.mkdirs();
    	for (File file : folder.listFiles()) {
    		String num = file.getName().replaceAll("[^\\d]","");
    		if (!num.equals("")) {
    			int val = Integer.parseInt(num);
    			if (val == version) {
    				found = file;
    				break;
    			}}}
    	if (found != null)
			try {
				return NeuralNetwork.readFromFile(found);
			} catch (IOException e) {
				e.printStackTrace();
				throw new NetworkIOException("failed to load ANN (file corrupted)");
			}
    	throw new NetworkIOException("failed to load ANN (version does not exist)");
	}
	
	/**
	 * @param network_name
	 * @return the highest saved version number of the specified network stored in memory, or {@code -1} if none such file exists
	 */
	public static int getLatestVersion(String network_name) {
    	File folder = new File(networkPath+network_name);
    	folder.mkdirs();
    	int max = -1;
    	for (File file : folder.listFiles()) {
    		String num = file.getName().replaceAll("[^\\d]","");
    		if (!num.equals("")) {
    			int val = Integer.parseInt(num);
    			if (val > max) max = val;
    		}
    	}
    	return max;
    }
	
}

class NetworkIOException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public NetworkIOException(String message) {
		super(message);
	}
	public NetworkIOException() {
		super();
	}
}
